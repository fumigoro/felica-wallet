package com.example.y3033067.nfcreader;

import android.util.Log;

public class ReadWithoutEncryption{
    final byte COMMAND_CODE = 0x06;
    byte[] idm;
    byte serviceCodeNumber;
    int[] serviceCodes;
    byte blockNumber;
    short[] blocks;

    final int MAX_BLOCK_SIZE = 15;
    final int MAX_SERVICE_SIZE = 16;

    public ReadWithoutEncryption(byte[] targetIDm, int targetServiceCode, int startBlock, int endBlock) {
        //読み出すサービス数が1の場合のコンストラクタ
        this.idm = new byte[8];
        //IDmをメンバーに代入
        System.arraycopy(targetIDm,0,this.idm,0,targetIDm.length);
        //サービス数を1
        this.serviceCodeNumber=0x01;
        //サービスコードをメンバーに代入
        this.serviceCodes = new int[1];
        this.serviceCodes[0] = targetServiceCode;
        //ブロック数をバイトに変換しメンバにせっと
        this.blockNumber =  (byte) (0x000000ff & (endBlock-startBlock));
        this.blocks = new short[this.blockNumber];
        //ブロックリスト
        for(int i=0;i<blockNumber;i++){
            //ブロックの上位バイトに0x80をセット 「Felicaユーザマニュアル抜粋」の4.3項
            this.blocks[i] = (short) ((0x0000ffff & (startBlock+i)) | 0x8000);
        }
    }


    public byte[] generateCommandPacket() throws Exception {
        //IDmが8byteかチェック
        if(idm.length!=8){
            throw new Exception("Invalid IDm size.");
        }
        //サービス数とサービスコードリストの長さが矛盾していないかチェック
        if((int) serviceCodeNumber != serviceCodes.length){
            throw new Exception("ServiceNumber does not match 'serviceCodes.length'.");
        }
        //同時読み出し可能なサービス数を超えていないかチェック
        if(1>serviceCodeNumber && serviceCodeNumber>MAX_SERVICE_SIZE){
            Log.d("TAG","serviceCodeNumber:"+serviceCodeNumber);
            throw new Exception("serviceCode number is out of range.");
        }
        //ブロック数とブロックコードリストの長さが矛盾していないかチェック
        if( blockNumber != blocks.length){
            Log.d("TAG","blockNumber:"+blockNumber+"/blocks:"+blocks.length);
            throw new Exception("BlockNumber does not match 'blocks.length'.");
        }
        //同時読み出し可能なブロック数を超えていないかチェック
        if(1>blockNumber && blockNumber>MAX_BLOCK_SIZE){
            Log.d("TAG","blockNumber:"+blockNumber);
            throw new Exception("Block number is out of range.");
        }

        /*
         * コマンド生成部分
         * */
        byte[] command = new byte[12+serviceCodeNumber*2+blockNumber*2];

        if(command.length>0xff){
            throw new Exception("Command packet size over");
        }
        Log.d("cmd","==========================");
        Log.d("cmd","Type:ReadWithoutEncryption");
        command[0] = (byte) (0x000000ff & (command.length));
        command[1] = COMMAND_CODE; // コマンドコード
        Log.d("cmd","CommandCode:"+String.format("%02X",command[1]));
        //IDm（[2]~[9]）
        for(int i=0;i<8;i++){
            command[i+2] = idm[i];

        }
        Log.d("cmd","IDm:"+hex2string(idm));
        command[10] = serviceCodeNumber; //サービス数
        Log.d("cmd","ServiceNumber:" +String.format("%02X",command[10]));

        //サービスコードリスト（[11]~[11+serviceCodeNumber*2]）
        for(int i=0;i<serviceCodeNumber;i++){
            //リトルエンディアンで入れる
            command[11+i*2+1] = (byte) (0x000000ff & (serviceCodes[i] >>> 8));
            command[11+i*2] = (byte) (0x000000ff & (serviceCodes[i]));
            Log.d("cmd","ServiceCodeList["+i+"]:0x"
                    +String.format("%02X",command[11+i*2+1])
                    +String.format("%02X",command[11+i*2]));
        }
        command[11+serviceCodeNumber*2] = blockNumber; //ブロック数
        Log.d("cmd","BlockNumber:"+String.format("%02X",command[11+serviceCodeNumber*2]));

        //ブロックリスト（[11+serviceCodeNumber*2+3]~[11+serviceCodeNumber*2+3+blockNumber*2]）
        for(int i=0;i<blockNumber;i++){
            //リトルエンディアンではない（コンストラクタ内で予め順番設定済み）
            command[11+serviceCodeNumber*2+1+i*2] = (byte) (0x000000ff & (blocks[i] >>> 8));
            command[11+serviceCodeNumber*2+1+i*2+1] = (byte) (0x000000ff & (blocks[i]));
            Log.d("cmd","BlockList["+i+"]:{D0:0x"
                    +String.format("%02X",command[11+serviceCodeNumber*2+1+i*2])
                    +",D1:0x"
                    +String.format("%02X",command[11+serviceCodeNumber*2+1+i*2+1])
                    +"}"
            );
        }
        Log.d("cmd","FullCommand:"+hex2string(command));
        Log.d("TAG","ReadWithoutEncryption: Command generated successfully.");

        return command;
    }

    public String hex2string(byte[] bytes){
        StringBuilder str1 = new StringBuilder();
        if(bytes==null){
            return str1.toString();
        }
//        Log.d("TAG","length:"+bytes.length);
        for (byte b : bytes) {
            try {
                str1.append(String.format("%02X", b)+":");
            }catch (Error e){
                e.printStackTrace();
            }

        }
        return str1.toString();
    }


}

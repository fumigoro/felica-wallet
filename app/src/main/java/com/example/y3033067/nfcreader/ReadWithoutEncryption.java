package com.example.y3033067.nfcreader;

import android.util.Log;

public class ReadWithoutEncryption {
    final byte COMMAND_CODE = 0x06;
    byte[] idm;
    byte serviceCodeNumber;
    int[] serviceCodes;
    byte blockNumber;
    short[] blocks;

    final int MAX_BLOCK_SIZE = 15;
    final int MAX_SERVICE_SIZE = 16;

    /**
     * @param targetIDm         IDm
     * @param targetServiceCode 読み込みたいサービのサービススコード
     * @param startBlock        ブロックの開始位置
     * @param endBlock          　ブロックの終了位置
     */
    public ReadWithoutEncryption(byte[] targetIDm, int targetServiceCode, int startBlock, int endBlock) {
        //読み出すサービス数が1の場合のコンストラクタ
        this.idm = new byte[8];
        //IDmをメンバーに代入
        System.arraycopy(targetIDm, 0, this.idm, 0, targetIDm.length);
        //サービス数を1
        this.serviceCodeNumber = 0x01;
        //サービスコードをメンバーに代入
        this.serviceCodes = new int[1];
        this.serviceCodes[0] = targetServiceCode;
        //ブロック数をバイトに変換しメンバにせっと
        this.blockNumber = (byte) (0x000000ff & (endBlock - startBlock));
        this.blocks = new short[this.blockNumber];
        //ブロックリスト
        for (int i = 0; i < blockNumber; i++) {
            //ブロックの上位バイトに0x80をセット 「Felicaユーザマニュアル抜粋」の4.3項
            this.blocks[i] = (short) ((0x0000ffff & (startBlock + i)) | 0x8000);
        }
    }

    /**
     * コマンドを実行するために必要なコマンド配列を生成
     *
     * @return コマンド配列
     * @throws Exception　例外
     */
    public byte[] generateCommandPacket() throws Exception {

        //======= 例外処理 =======

        //IDmが8byteかチェック
        if (idm.length != 8) {
            Log.e("TAG", "idmLength:" + idm.length);
            throw new Exception("Invalid IDm size.");
        }
        //サービス数とサービスコードリストの長さが矛盾していないかチェック
        if ((int) serviceCodeNumber != serviceCodes.length) {
            Log.e("TAG", "serviceCodeNumber:" + serviceCodeNumber +
                    "/serviceCodes:" + serviceCodes.length);
            throw new Exception("ServiceNumber does not match 'serviceCodes.length'.");
        }
        //同時読み出し可能なサービス数を超えていないかチェック
        if (serviceCodeNumber < 1 || MAX_SERVICE_SIZE < serviceCodeNumber) {
            Log.e("TAG", "serviceCodeNumber:" + serviceCodeNumber);
            throw new Exception("serviceCode number is out of range.");
        }
        //ブロック数とブロックコードリストの長さが矛盾していないかチェック
        if (blockNumber != blocks.length) {
            Log.e("TAG", "blockNumber:" + blockNumber + "/blocks:" + blocks.length);
            throw new Exception("BlockNumber does not match 'blocks.length'.");
        }
        //同時読み出し可能なブロック数を超えていないかチェック
        if (blockNumber < 1 || MAX_BLOCK_SIZE < blockNumber) {
            Log.e("TAG", "blockNumber:" + blockNumber);
            throw new Exception("Block number is out of range.");
        }

        //======= コマンド生成開始 =======

        //コマンド配列を作成
        byte[] command = new byte[12 + serviceCodeNumber * 2 + blockNumber * 2];

        Log.d("cmd", "==========================");
        Log.d("cmd", "Type:ReadWithoutEncryption");

        //配列の長さ
        command[0] = (byte) (0x000000ff & (command.length));

        //コマンドコード
        command[1] = COMMAND_CODE;
        Log.d("cmd", "CommandCode:" + String.format("%02X", command[1]));

        //IDm
        //配列IDmの中身をcommandへコピー
        System.arraycopy(idm, 0, command, 2, 8);
        //command[2]~[9]を使用
        Log.d("cmd", "IDm:" + hex2string(idm));

        //サービス数
        command[10] = serviceCodeNumber;
        Log.d("cmd", "ServiceNumber:" + String.format("%02X", command[10]));

        //サービスコードリスト（[11]~[11+serviceCodeNumber*2]）
        for (int i = 0; i < serviceCodeNumber; i++) {
            //リトルエンディアンで入れる
            command[11 + i * 2 + 1] = (byte) (0x000000ff & (serviceCodes[i] >>> 8));
            command[11 + i * 2] = (byte) (0x000000ff & (serviceCodes[i]));
            Log.d("cmd", "ServiceCodeList[" + i + "]:0x"
                    + String.format("%02X", command[11 + i * 2 + 1])
                    + String.format("%02X", command[11 + i * 2]));
        }

        //ブロック数
        command[11 + serviceCodeNumber * 2] = blockNumber;
        Log.d("cmd", "BlockNumber:" + String.format("%02X", command[11 + serviceCodeNumber * 2]));

        //ブロックリスト（[11+serviceCodeNumber*2+3]~[11+serviceCodeNumber*2+3+blockNumber*2]）
        for (int i = 0; i < blockNumber; i++) {
            //リトルエンディアンではない（コンストラクタ内で予め順番設定済み）
            command[11 + serviceCodeNumber * 2 + 1 + i * 2] = (byte) (0x000000ff & (blocks[i] >>> 8));
            command[11 + serviceCodeNumber * 2 + 1 + i * 2 + 1] = (byte) (0x000000ff & (blocks[i]));
            Log.d("cmd", "BlockList[" + i + "]:{D0:0x"
                    + String.format("%02X", command[11 + serviceCodeNumber * 2 + 1 + i * 2])
                    + ",D1:0x"
                    + String.format("%02X", command[11 + serviceCodeNumber * 2 + 1 + i * 2 + 1])
                    + "}"
            );
        }

        //完了
        Log.d("cmd", "FullCommand:" + hex2string(command));
        Log.d("TAG", "ReadWithoutEncryption: Command generated successfully.");

        return command;
    }

    public String hex2string(byte[] bytes) {
        StringBuilder str1 = new StringBuilder();
        if (bytes == null) {
            return str1.toString();
        }
//        Log.d("TAG","length:"+bytes.length);
        for (byte b : bytes)
            try {
                str1.append(String.format("%02X", b)).append(":");
            } catch (Error e) {
                e.printStackTrace();
            }
        return str1.toString();
    }


}

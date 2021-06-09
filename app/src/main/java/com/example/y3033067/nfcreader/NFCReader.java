package com.example.y3033067.nfcreader;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class NFCReader {

    private NfcF nfc;

    public NFCReader(Tag tag) {
        this.nfc = NfcF.get(tag);
        // システムコード
        /*
         * Ayuca:0x83ee
         * CampusPay:0x8e4b
         */
    }

    /**
     * Pollingコマンドを用いてIDｍを取得
     *
     * @param systemCode システムコード
     * @return IDｍ
     * @throws IOException nfc.transceive()から発生する例外
     */
    public byte[] getIDm(int systemCode) throws IOException {
        //Pollingコマンド用のビット列を生成
        byte[] command = generatePollingCommand(systemCode);
        //ビット列を送信（コマンド実行）
        byte[] res = nfc.transceive(command);
        Log.d("general", hex2string(res));
        //応答内容を返却
        return Arrays.copyOfRange(res, 2, 10);
    }

    /**
     * 読み込みたい場所を指定しデータを取得
     * @param targetIDm         IDｍ
     * @param targetServiceCode 読み込みたい場所のサービスコード
     * @param startBlock        読み込みたいブロックの開始位置
     * @param endBlock          終了位置
     * @param blockData         読み込んだ内容を格納するリスト(の参照)
     * @throws Exception ReadWithoutEncryptionクラスのhandleStatusFlag()から発生。
     *                   応答内容を元にエラーの解析を行う
     */
    public void getBlockData(byte[] targetIDm, int targetServiceCode, int startBlock, int endBlock, ArrayList<Byte[]> blockData) throws Exception {
        ReadWithoutEncryption rwe = new ReadWithoutEncryption(targetIDm, targetServiceCode, startBlock, endBlock);
        // コマンドを送信して結果を取得
        byte[] res = nfc.transceive(rwe.generateCommandPacket());
        //応答のエラーハンドリング
        //エラーがあった場合はExceptionを発生させるメソッド
        rwe.handleStatusFlag(res[10], res[11]);

        //要求に応じて返されたブロックデータの数
        int blockNumber = res[12];
        //ブロックデータ自体はindex13以降に格納されている
        int index = 13;

        //ブロックデータを要素16のバイト配列のリストに整形
        for (int i = 0; i < blockNumber; i++) {
            Byte[] blockTmp = new Byte[16];
            for (int j = 0; j < 16; j++) {
                blockTmp[j] = res[index];
                index += 1;
            }
            //渡されたリストの参照を使ってデータを格納
            blockData.add(blockTmp);
        }
    }


    /**
     * Pollingコマンドのビット列を生成
     *
     * @param systemCode システムコード
     * @return コマンド配列
     */
    private byte[] generatePollingCommand(int systemCode) {
        //int->2バイトのバイト配列変換
        byte[] systemCodeB = new byte[2];
        systemCodeB[0] = (byte) (0x000000ff & systemCode >>> 8);
        systemCodeB[1] = (byte) (0x000000ff & systemCode);

        //コマンド配列
        byte[] command = new byte[6];

        //データ長
        command[0] = (byte) command.length;
        //コマンドコード（0x00で固定）
        command[1] = 0x00;
        //システムコード(リトルエンディアン)
        command[2] = systemCodeB[0];
        command[3] = systemCodeB[1];
        //リクエストコード
        /*
         * 0x00:要求なし
         * 0x01:システムコード要求
         * 0x02:通信性能要求
         * */
        command[4] = 0x00;
        //タイムスロット
        command[5] = 0x0f;

        return command;
    }

    /**
     * バイト配列を16進数表記で1バイトずつ区切った文字列に変換する
     * @param bytes バイト配列(プリミティブbyte)
     * @return 文字列
     */
    //こっちは基本データ型のbyte型
    public String hex2string(Byte[] bytes) {
        StringBuilder string = new StringBuilder();
        for (byte b : bytes) {
            try {
                string.append(String.format("%02X:", b));
            } catch (Error e) {
                e.printStackTrace();
            }
        }
        return string.toString();
    }

    /**
     * バイト配列を16進数表記で1バイトずつ区切った文字列に変換する
     * @param bytes バイト配列(Byteクラス)
     * @return 文字列
     */
    //こっちはByteクラスのByte型
    public String hex2string(byte[] bytes) {
        StringBuilder string = new StringBuilder();
        for (byte b : bytes) {
            try {
                string.append(String.format("%02X:", b));
            } catch (Error e) {
                e.printStackTrace();
            }
        }
        return string.toString();
    }


}

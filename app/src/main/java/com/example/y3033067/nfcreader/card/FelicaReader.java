package com.example.y3033067.nfcreader.card;

import android.nfc.Tag;
import android.nfc.tech.NfcF;

import com.example.y3033067.nfcreader.felica.ReadWithoutEncryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * カードの読み取りのパラメーター指定や読み取ったバイナリの解釈を行うクラス
 * 特定のカードに関するパラメータは持たず、基本的にはこのクラスを継承したサブクラスをインスタンス化することを想定
 */
public class FelicaReader {

    private NfcF nfc;
    private byte[] targetIDm;
    private byte[] primarySystemIDm;

    public FelicaReader(Tag tag) {
        this.nfc = NfcF.get(tag);
    }

    /**
     * Pollingコマンドを用いてIDｍを取得
     *
     * @param systemCode システムコード
     * @return IDｍ
     * @throws IOException nfc.transceive()から発生する例外
     */
    public byte[] readIDm(int systemCode) throws IOException {
        //Pollingコマンド用のビット列を生成
        byte[] command = generatePollingCommand(systemCode);
        //ビット列を送信（コマンド実行）
        byte[] res = nfc.transceive(command);
        targetIDm = Arrays.copyOfRange(res, 2, 10);
        //応答内容を返却
        return targetIDm;

    }

    /**
     * 読み込みたい場所を指定しデータを取得
     *
     * @param _targetIDm        IDｍ
     * @param targetServiceCode 読み込みたい場所のサービスコード
     * @param startBlock        読み込みたいブロックの開始位置
     * @param endBlock          終了位置
     * @param blockData         読み込んだ内容を格納するリスト(の参照)
     * @throws Exception ReadWithoutEncryptionクラスのhandleStatusFlag()から発生。
     *                   応答内容を元にエラーの解析を行う
     */
    public void getBlockData(byte[] _targetIDm, int targetServiceCode, int startBlock, int endBlock, ArrayList<Byte[]> blockData) throws Exception {
        ReadWithoutEncryption rwe = new ReadWithoutEncryption(_targetIDm, targetServiceCode, startBlock, endBlock);
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
     *
     */
    public int getCardType() {
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            //最初はどのカードかわからないのでSystemCodeはワイルドカード指定
            byte[] IDm = readIDm(0xffff);
            primarySystemIDm = IDm;
            //通信終了
            nfc.close();

            StringBuilder IDmString = new StringBuilder();
            IDmString.append(String.format("%02X", IDm[0]));
            IDmString.append(String.format("%02X", IDm[1]));

            switch (String.valueOf(IDmString)) {
                case "0112":
                    //Ayuca
                    return CardParams.TYPE_CODE_AYUCA;
                case "0114":
                    //CampusPay
                    return CardParams.TYPE_CODE_CAMPUS_PAY;
                case "012E":
                    //学生証
                    return CardParams.TYPE_CODE_STUDENT_ID;
                default:
                    return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }


    }

    public String getIDm(String split) {
        return hex2string(primarySystemIDm, split);
    }

    /**
     * バイト配列を16進数表記で1バイトずつ区切った文字列に変換する
     *
     * @param bytes バイト配列(Byteクラス)
     * @return 文字列
     */
    //こっちはByteクラスのByte型
    public String hex2string(byte[] bytes, String split) {
        StringBuilder string = new StringBuilder();
        for (byte b : bytes) {
            try {
                string.append(String.format("%02X%s", b, split));
            } catch (Error e) {
                e.printStackTrace();
            }
        }
        return string.toString();
    }
}

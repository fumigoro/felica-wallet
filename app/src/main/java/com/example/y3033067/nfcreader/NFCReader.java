package com.example.y3033067.nfcreader;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class NFCReader {
    private int targetServiceCode;
    private int targetSystemCode;
    private NfcF nfc;
    ArrayList<Byte[]> blockData;

    public NFCReader() {
        // System 1のシステムコード -> 0x83EE
        /*
         * Ayuca:0x83ee
         * CampusPay:0x8e4b
         */
        targetServiceCode = 0x898F;
        targetSystemCode = 0x83EE;
        blockData = new ArrayList<>();

    }

    public ArrayList<Byte[]> readTag(Tag tag) {
        blockData = new ArrayList<>();
        try {
            this.connect(tag);

            byte[] targetIDm = getIDm(targetSystemCode);

            this.read(targetIDm,targetServiceCode,0,10,blockData);
            this.read(targetIDm,targetServiceCode,10,20,blockData);

            nfc.close();

            return blockData;
        } catch (Exception e) {
            Log.e("general", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Pollingコマンドの取得。
     *
     * @param systemCode byte[] 指定するシステムコード
     * @return Pollingコマンド
     */
    private byte[] getPollingCommand(byte[] systemCode) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        bout.write(0x00);           // データ長バイトのダミー
        bout.write(0x00);           // コマンドコード
        bout.write(systemCode[0]);  // systemCode
        bout.write(systemCode[1]);  // systemCode
        bout.write(0x01);           // リクエストコード
        bout.write(0x0f);           // タイムスロット

        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length; // 先頭１バイトはデータ長
        return msg;
    }

    private byte[] getIDm(int systemCode) throws IOException {
        byte[] systemCodeB = new byte[2];
        systemCodeB[0] = (byte) (0x000000ff & systemCode >>> 8);
        systemCodeB[1] = (byte) (0x000000ff & systemCode);
        byte[] command = getPollingCommand(systemCodeB);
        byte[] pollingRes = nfc.transceive(command);

        return Arrays.copyOfRange(pollingRes, 2, 10);
    }

    public void connect(Tag tag) throws IOException {
        this.nfc = NfcF.get(tag);
        nfc.connect();
    }

    /**
     *
     * @param targetIDm
     * @param targetServiceCode
     * @param startBlock
     * @param endBlock
     * @param blockData
     * @throws Exception
     */
    public void read(byte[] targetIDm, int targetServiceCode, int startBlock, int endBlock, ArrayList<Byte[]> blockData) throws Exception {
        ReadWithoutEncryption rwe = new ReadWithoutEncryption(targetIDm, targetServiceCode, startBlock, endBlock);
        // コマンドを送信して結果を取得
        byte[] res = nfc.transceive(rwe.generateCommandPacket());
        handleStatusFlag(res[10], res[11]);
        int blockNumber = res[12];

        int index = 13;
        for (int i = 0; i < blockNumber; i++) {
            Byte[] blockTmp = new Byte[16];
            for (int j = 0; j < 16; j++) {
                blockTmp[j] = res[index];
                index += 1;
            }
            blockData.add(blockTmp);
        }

    }

    public String hex2string(Byte[] bytes) {
        StringBuilder str1 = new StringBuilder();
        if (bytes == null) {
            return str1.toString();
        }
        for (byte b : bytes) {
            try {
                str1.append(String.format("%02X", b)).append(":");
            } catch (Error e) {
                e.printStackTrace();
            }

        }
        return str1.toString();
    }

    /**
     * @param status1
     * @param status2
     * @throws Exception エラー内容出典：
     *                   FeliCaカード ユーザーズマニュアル 抜粋版
     *                   https://www.sony.co.jp/Products/felica/business/tech-support/st_usmnl.html
     *                   4.5節　P84~P86
     */
    private void handleStatusFlag(byte status1, byte status2) throws Exception {
        switch (status1) {
            case (byte) 0x00:
                break;
            case (byte) 0xff:
                Log.e("Exception", "コマンドパケットにリストを含まないコマンドでのエラー・リストに依存しないエラー");
                break;
            default:
                Log.e("Exception", "ブロックリストまたはサービスコードリストに関するエラー/CD:0x" + String.format("%02X", status1));
                break;
        }
        if (status1 != 0x00) {
            switch (status2) {
                case (byte) 0x01:
                    throw new Exception("パースのデクリメント時に計算結果がゼロ未満になります。または、パースのキャッシュバック時に計算結果が、4バイトを超える数字になります。");
                case (byte) 0x02:
                    throw new Exception("パースのキャッシュバック時に、指定されたデータがキャッシュバックデータの値を超えています。");
                case (byte) 0x70:
                    throw new Exception("メモリエラー (致命的エラー)");
                case (byte) 0x71:
                    Log.w("", "メモリ書き換え回数が上限を超えています (警告であり、書き込み処理は行われます)。製品により書き換え回数の上限値は異なります。また、ステータスフラグ1が00hの製品と、FFhの製品があります。");
            }
        }
    }
}

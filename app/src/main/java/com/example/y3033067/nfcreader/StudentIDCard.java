package com.example.y3033067.nfcreader;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.util.Log;

import java.util.ArrayList;

public class StudentIDCard extends NFCReader{
    private final int SYSTEM_CODE;

    private final int SERVICE_CODE_ID;
    private NfcF nfc;
    ArrayList<Byte[]> blockData;


    public StudentIDCard(Tag tag){
        super(tag);
        this.nfc = NfcF.get(tag);
        SYSTEM_CODE = SystemCode.STUDENT_ID;
        SERVICE_CODE_ID = 0x020B;//学籍番号
    }


    /**
     * ICカードから学籍番号を取得して返す
     * 通信開始から終了まで1連の流れを行う
     *
     * @return 取得したデータ
     */
    public ArrayList<Byte[]> getID() {
        blockData = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.getIDm(SYSTEM_CODE);
            //データを取得
            super.getBlockData(targetIDm, SERVICE_CODE_ID, 0, 1, blockData);

            //通信終了
            nfc.close();
            return blockData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}

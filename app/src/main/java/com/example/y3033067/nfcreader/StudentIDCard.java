package com.example.y3033067.nfcreader;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class StudentIDCard extends NFCReader implements NFCReaderIf{
    private final int SYSTEM_CODE;

    private final int SERVICE_CODE_ID;
    private NfcF nfc;
    ArrayList<Byte[]> blockData;
    private Byte[] studentID;


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
    private ArrayList<Byte[]> readID() {
        blockData = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.readIDm(SYSTEM_CODE);
            //データを取得
            super.getBlockData(targetIDm, SERVICE_CODE_ID, 0, 1, blockData);

            //通信終了
            nfc.close();
            studentID = blockData.get(0);
            return blockData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public String getStudentID() {


        byte[] studentID2 = new byte[16];
        for(int i=0; i<studentID.length;i++){
            studentID2[i] = studentID[i];
        }
        studentID2[0] = 0x20;//空白
        Log.d("TAG",super.hex2string(studentID2,":"));

        String resultString = "test";
        try {
            resultString = new String(studentID2, StandardCharsets.US_ASCII);
            Log.d("TAG","res:"+resultString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString;
    }
    @Override
    public ArrayList<CardHistory> getHistories() {
        return null;
    }

    @Override
    public int getSFBalance() {
        return 0;
    }

    @Override
    public int getPointBalance() {
        return 0;
    }

    @Override
    public void readAllData() {
        readID();
    }
}

package com.example.y3033067.nfcreader;

import android.annotation.SuppressLint;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Ayuca extends NFCReader{
    private final int SYSTEM_CODE;
    private final int SERVICE_CODE_HISTORY;
    private final int SERVICE_CODE_BALANCE;
    private final int SERVICE_CODE_INFO;
    private NfcF nfc;
    ArrayList<Byte[]> historyData, cardInfo, balance;
    ArrayList<CardHistory> histories;


    public Ayuca(Tag tag){
        super(tag);
        this.nfc = NfcF.get(tag);
        SYSTEM_CODE = 0x83EE;
        SERVICE_CODE_HISTORY = 0x898F;//履歴
        SERVICE_CODE_BALANCE = 0x884B;//残高
        SERVICE_CODE_INFO = 0x804B;//カード情報
    }

    /**
     * ICカードから利用履歴を取得して返す
     * 通信開始から終了まで1連の流れを行う
     *
     * @return 取得したデータ
     */
    public ArrayList<Byte[]> getHistory() {
        historyData = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.getIDm(SYSTEM_CODE);
            //データを取得
            super.getBlockData(targetIDm, SERVICE_CODE_HISTORY, 0, 10, historyData);
            super.getBlockData(targetIDm, SERVICE_CODE_HISTORY, 10, 20, historyData);

            //通信終了
            nfc.close();
            return historyData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * ICカードからカード情報を取得して返す
     * 通信開始から終了まで1連の流れを行う
     *
     * @return 取得したデータ
     */
    public ArrayList<Byte[]> getCardInfo() {
        cardInfo = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.getIDm(SYSTEM_CODE);
            //データを取得
            super.getBlockData(targetIDm, SERVICE_CODE_INFO, 0, 2, cardInfo);

            //通信終了
            nfc.close();
            return cardInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<CardHistory> parseHistory(){
        StringBuilder stringB = new StringBuilder();
        String year,month,day,hour,minute,discount,device,type,price,balance,point;
        String stringTmp;

        for(int i=0;i<historyData.size();i++){
            for(int j=0;j<historyData.get(0).length;j++){
                stringTmp = (Integer.toBinaryString((Byte.toUnsignedInt(historyData.get(i)[j]))));
                stringB.append(String.format("%8s", stringTmp).replace(' ', '0')); // 0埋め
            }
            year = stringB.substring(0, 7);
            month = stringB.substring(7, 11);
            day = stringB.substring(11,16);
            Log.d("TAG",year+"/"+month+"/"+day);

        }
        return histories;
    }


}

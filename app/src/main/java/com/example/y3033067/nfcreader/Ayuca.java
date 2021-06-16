package com.example.y3033067.nfcreader;

import android.nfc.Tag;
import android.nfc.tech.NfcF;

import java.util.ArrayList;

public class Ayuca extends NFCReader{
    private final int SYSTEM_CODE;
    private final int SERVICE_CODE_HISTORY;
    private final int SERVICE_CODE_BALANCE;
    private final int SERVICE_CODE_INFO;
    private NfcF nfc;
    ArrayList<Byte[]> historyData, cardInfo, balance;


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


}

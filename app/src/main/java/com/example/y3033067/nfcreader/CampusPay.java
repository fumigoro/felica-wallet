package com.example.y3033067.nfcreader;

import android.annotation.SuppressLint;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Date;

public class CampusPay extends NFCReader {
    private final int SYSTEM_CODE;
    private final int SERVICE_CODE_HISTORY;
    private final int SERVICE_CODE_BALANCE;
    private final int SERVICE_CODE_INFO;
    private NfcF nfc;
    ArrayList<Byte[]> blockData;
    ArrayList<Byte[]> historyData, cardInfo, balance;
    ArrayList<CardHistory> histories;

    public CampusPay(Tag tag){
        super(tag);
        this.nfc = NfcF.get(tag);
        SYSTEM_CODE = SystemCode.CAMPUS_PAY;
        SERVICE_CODE_HISTORY = 0x50CF;//履歴
        SERVICE_CODE_BALANCE = 0x50D7;//残高
        SERVICE_CODE_INFO = 0x50CB;//カード情報
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
            Log.d("TAG", super.hex2string(targetIDm));
            //データを取得
            super.getBlockData(targetIDm, SERVICE_CODE_HISTORY, 0, 10, historyData);

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
            super.getBlockData(targetIDm, SERVICE_CODE_INFO, 0, 6, cardInfo);

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
    public ArrayList<CardHistory> parseHistory() {

        StringBuilder stringB;
        String discount, start, end, device, type;
        int year, month, day, hour, minute, second, price, balance, point;
        String stringTmp;
        CardHistory history;
        for (int i = 0; i < historyData.size(); i++) {
            stringB = new StringBuilder();
            for (int j = 0; j < historyData.get(0).length; j++) {
                stringTmp = String.format("%02X", historyData.get(i)[j]);
                stringB.append(stringTmp); // 0埋め
            }
//            Log.d("TAG",String.valueOf(stringB));
            year = Integer.parseInt(stringB.substring(0, 4), 10);
            month = Integer.parseInt(stringB.substring(4, 6), 10);
            day = Integer.parseInt(stringB.substring(6, 8), 10);
            hour = Integer.parseInt(stringB.substring(8, 10), 10);
            minute = Integer.parseInt(stringB.substring(10, 12), 10);
            second = Integer.parseInt(stringB.substring(12, 14), 10);
            discount = "";
            start = "";
            end = "";
            device = "";
            type = String.format("0x%X", Integer.parseInt(stringB.substring(14, 16), 10));

            price = Integer.parseInt(stringB.substring(16, 22), 10);
            balance = Integer.parseInt(stringB.substring(22, 28), 10);
            point = (int)((double)price/1.1*0.01);
            Log.d("TAG",
                    year + "/" +
                            month + "/" +
                            day + " " +
                            hour + ":" +
                            minute + ":" +
                            second + " "+
                            discount + " " +
                            start + "~" +
                            end + " " +
                            device + " " +
                            type + " " +
                            price + " " +
                            balance + " " +
                            point
            );

            history = new CardHistory(new Date(year, month, day, hour, minute, second), price, point, balance, type, discount, device, start, end);
            histories.add(history);
        }
        return histories;
    }


}

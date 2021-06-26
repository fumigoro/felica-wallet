package com.example.y3033067.nfcreader;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Date;

public class CampusPay extends NFCReader implements NFCReaderIf{
    private final int SYSTEM_CODE;
    private final int SERVICE_CODE_HISTORY;
    private final int SERVICE_CODE_BALANCE;
    private final int SERVICE_CODE_INFO;
    private final NfcF nfc;
    private String IDm;
    ArrayList<Byte[]> historyData, cardInfo, balance;
    ArrayList<CardHistory> histories;

    public CampusPay(Tag tag) {
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
    public ArrayList<Byte[]> readHistories() {
        historyData = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.getIDm(SYSTEM_CODE);
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
    public ArrayList<Byte[]> readCardInfo() {
        cardInfo = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.getIDm(SYSTEM_CODE);
            IDm = super.hex2string(targetIDm,"");
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

    public ArrayList<Byte[]> readBalance() {
        balance = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.getIDm(SYSTEM_CODE);
            //データを取得
            super.getBlockData(targetIDm, SERVICE_CODE_BALANCE, 0, 1, balance);

            //通信終了
            nfc.close();
            return balance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     *
     */
    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<CardHistory> getHistories() {

        StringBuilder stringB;
        String discount, start, end, device, type;
        int year, month, day, hour, minute, second, price, balance, typeFlag;
        String stringTmp;
        CardHistory history;
        histories = new ArrayList<>();
        for (int i = 0; i < historyData.size(); i++) {
            stringB = new StringBuilder();
            for (int j = 0; j < historyData.get(0).length; j++) {
                stringTmp = String.format("%02X", historyData.get(i)[j]);
                stringB.append(stringTmp); // 0埋め
            }

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
            typeFlag = Integer.parseInt(stringB.substring(14, 16), 10);

            switch (typeFlag){
                case 0x5:
                    //SF利用
                    type = "決済";
                    break;
                case 0x1:
                    //チャージ
                    type = "チャージ";
                    break;
                default:
                    //不明
                    type = String.format("%X", typeFlag);
            }


            price = Integer.parseInt(stringB.substring(16, 22), 10);
            balance = Integer.parseInt(stringB.substring(22, 28), 10);

            Log.d("TAG",
                    year + "/" +
                            month + "/" +
                            day + " " +
                            hour + ":" +
                            minute + ":" +
                            second + " " +
                            type + " " +
                            price + " " +
                            balance + " "
            );
            /*MEMO:
            Dateの月設定は1小さい値を入れる
            Dateの年は-1900下値を入れる
            * */
            history = new CardHistory(new Date(year-1900, month-1, day, hour, minute, second),
                    price, 0,  balance, type, typeFlag, discount, device, start, end);
            histories.add(history);
        }
        return histories;
    }
    /**
     * 残高を返す
     * @return 残高
     * readAllData()を先に実行する必要がある
     */
    @Override
    public int getSFBalance(){
        StringBuilder stringB = new StringBuilder();
        if(balance.get(0).length<8){
            return 0;
        }
        for (int j = 0; j < 4; j++) {
            stringB.append(String.format("%02X", balance.get(0)[3-j]));
        }
        return Integer.parseInt(stringB.substring(0, 8), 16);
    }

    @Override
    public int getPointBalance() {
        return 0;
    }

    /**
     * カードから読み込み可能なすべてのデータを読み込む
     * カードから残高関連、履歴、カード情報を読み出す
     */
    @Override
    public void readAllData(){
        balance = readBalance();
        historyData = readHistories();
        cardInfo = readCardInfo();
    }

    @Override
    public String getIDm() {
        return IDm;
    }

}

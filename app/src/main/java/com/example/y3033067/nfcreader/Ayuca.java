package com.example.y3033067.nfcreader;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Date;

public class Ayuca extends NFCReader {
    private final int SYSTEM_CODE;
    private final int SERVICE_CODE_HISTORY;
    private final int SERVICE_CODE_BALANCE;
    private final int SERVICE_CODE_INFO;
    private NfcF nfc;
    ArrayList<Byte[]> historyData, cardInfo, balance;
    ArrayList<CardHistory> histories;


    public Ayuca(Tag tag) {
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
     * 残高情報を読み込む
     *
     * @return 残高情報
     */
    public ArrayList<Byte[]> getBalance() {
        balance = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.getIDm(SYSTEM_CODE);
            //データを取得
            super.getBlockData(targetIDm, SERVICE_CODE_BALANCE, 0, 3, balance);

            //通信終了
            nfc.close();
            return balance;
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
    public ArrayList<CardHistory> parseHistory() {
        StringBuilder stringB;
        String discount, start, end, device, type, pointMessage;
        int year, month, day, hour, minute, price, balance, point, pointFlag, pointBalance, additionalPoint, typeFlag, deviceFlag;
        /*
        point : 今回取引でのポイント増減
        additionalPoint : 今回取引で付与されたボーナスポイント

         */
        String stringTmp;
        CardHistory history;
        histories = new ArrayList<>();
        for (int i = historyData.size()-1; i >= 0; i--) {
            stringB = new StringBuilder();
            history = new CardHistory();
            for (int j = 0; j < historyData.get(0).length; j++) {
                stringTmp = (Integer.toBinaryString(Byte.toUnsignedInt(historyData.get(i)[j])));
                stringB.append(String.format("%8s", stringTmp).replace(' ', '0')); // 0埋め
            }

            year = Integer.parseInt(stringB.substring(0, 7), 2);
            month = Integer.parseInt(stringB.substring(7, 11), 2);
            day = Integer.parseInt(stringB.substring(11, 16), 2);
            hour = Integer.parseInt(stringB.substring(16, 22), 2);
            minute = Integer.parseInt(stringB.substring(22, 28), 2);
            discount = String.format("%X", Integer.parseInt(stringB.substring(28, 32), 2));
            start = String.format("%04X", Integer.parseInt(stringB.substring(32, 48), 2));
            end = String.format("%04X", Integer.parseInt(stringB.substring(48, 64), 2));
            deviceFlag = Integer.parseInt(stringB.substring(64, 68), 2);
            typeFlag = Integer.parseInt(stringB.substring(68, 72), 2);

            //仮
            device = String.format("%X", Integer.parseInt(stringB.substring(64, 68), 2));
            type = String.format("%X", Integer.parseInt(stringB.substring(68, 72), 2));

            price = Integer.parseInt(stringB.substring(72, 88), 2);
            balance = Integer.parseInt(stringB.substring(88, 104), 2);
            pointBalance = Integer.parseInt(stringB.substring(104, 124), 2);
            pointFlag = Integer.parseInt(stringB.substring(124, 128), 2);

            // 今回取引でのポイント増減
            point = 0;
            // ボーナスポイント付与とポイント使用(還元)のみの増減
            additionalPoint = 0;
            //履歴最終行(前回の利用金額等が残っていない)かどうか確認
            if (i < historyData.size() - 1) {
                //最終行でない場合
                //ポイント取り扱いフラグをあげる
                history.setPointFlag(true);
                //1件前の履歴のポイント数を取得
                int previousPointBalance = histories.get(historyData.size() - i - 2).getPointBalance();
                //1件前の取引後残高を取得
                int previousBalance = histories.get(historyData.size() - i - 2).getBalance();
                //今回の決済でのSF残高利用金額を計算
                int sfUsedPrice = previousBalance - balance;
                //今回増えたポイント
                point = (pointBalance - previousPointBalance);

                switch (pointFlag) {
                    case 1:
                        //ポイント還元が行われていた場合

                        /*その月の最初の履歴が積み増しだった場合の挙動が不明確なため、
                        この行の履歴がSF利用(運賃支払)であるか確認
                        岐阜バス公式のポイント還元が行われる条件に「翌月以降の初降車時」とあるので、
                        SF利用の場合(typeFlag == 0x3がtrueの場合)にのみ
                        ポイント還元が行われると考えて問題なさそうだが、確かめていないので一応チェック
                        */
                        if (typeFlag != 0x3) {
                            //SFでない場合
                            break;
                        }

                        //↑のifで積み増しを弾いているので、今回使用のSF残高>=0 が保証されている（はず）
                        //だが念の為確認
                        if (sfUsedPrice < 0) {
                            break;
                        }

                        //ポイント使用額を計算（使用分は負の数で表すとする）
                        additionalPoint = -1 * (price - sfUsedPrice);
                        point -= additionalPoint*10;
                        break;
                    case 2:
                        //ボーナスポイントの付与が行われていた場合
                        //付与されたボーナスポイント数を計算
                        additionalPoint = (int) (pointBalance - previousPointBalance - sfUsedPrice * 0.2) / 10;

                }


            }

            history.setPointParams(point,additionalPoint);

            Log.d("TAG",
                    year+"/"+month+"/"+day+ " "+hour+":"+minute
                            + " D" + discount
                            + " " + start
                            + "~" + end
                            + " D" + discount
                            + " T" + type
                            + " P" + price
                            + " B" + balance
                            + " PB" + pointBalance
                            + " P+-:" + point
                            + " PF:" + pointFlag
                            + " AP" + additionalPoint
            );


            history.setAllParams(new Date(year + 2000, month, day, hour, minute, 0),
                    price, pointBalance, balance, type, typeFlag, discount, device, start, end);

            histories.add(history);
        }


        return histories;
    }


}

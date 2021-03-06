package com.example.y3033067.nfcreader.card;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.y3033067.nfcreader.storage.CardData;

import java.util.ArrayList;
import java.util.Date;
/**
 * 生協電子マネーカードに関しての読み取りパラメータの指定やデータの解釈を行うクラス
 */
public class CampusPay extends FelicaReader implements FelicaCard {
    // Felica システムコード
    private final int SYSTEM_CODE;
    // Felica サービスコード
    private final int SERVICE_CODE_HISTORY;
    private final int SERVICE_CODE_BALANCE;
    private final int SERVICE_CODE_INFO;

    private final NfcF nfc;
    // 読み取った生のバイナリを保存するリスト
    ArrayList<Byte[]> historyData, cardInfo, balance;
    ArrayList<CardHistory> histories;

    private String IDm;

    public CampusPay(Tag tag) {
        super(tag);
        this.nfc = NfcF.get(tag);
        SYSTEM_CODE = CardParams.SYSTEM_CODE_CAMPUS_PAY;
        SERVICE_CODE_HISTORY = 0x50CF;//履歴
        SERVICE_CODE_BALANCE = 0x50D7;//残高
        SERVICE_CODE_INFO = 0x50CB;//カード情報
    }

    /**
     * 利用履歴が保存されたサービスを読み込む
     *
     * @return 読み取ったダンプデータ
     */
    private ArrayList<Byte[]> readHistories() {
        historyData = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.readIDm(SYSTEM_CODE);
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
     * カード情報が保存されているサービスを読む
     *
     * @return 読み取ったダンプデータ
     */
    private ArrayList<Byte[]> readCardInfo() {
        cardInfo = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.readIDm(SYSTEM_CODE);
            String IDm = super.hex2string(targetIDm, "");
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
     * 残高情報が記録されたサービスを読み込む
     *
     * @return 読み取ったダンプデータ
     */
    private ArrayList<Byte[]> readBalance() {
        balance = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            byte[] targetIDm = super.readIDm(SYSTEM_CODE);
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
     * 　読み取った履歴データを解釈しCardHistoryクラスのリストで返す
     *
     * @return 利用履歴
     */
    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<CardHistory> getHistories() {

        StringBuilder stringB;
        String device, type;
        int year, month, day, hour, minute, second, price, balance, typeFlag;
        String stringTmp;
        CardHistory history;
        histories = new ArrayList<>();

        //Byte配列から16進数文字列に変換
        for (int i = 0; i < historyData.size(); i++) {
            stringB = new StringBuilder();
            for (int j = 0; j < historyData.get(0).length; j++) {
                stringTmp = String.format("%02X", historyData.get(i)[j]);
                stringB.append(stringTmp); // 0埋め
            }

            //取引年月日
            year = Integer.parseInt(stringB.substring(0, 4), 10);
            month = Integer.parseInt(stringB.substring(4, 6), 10);
            day = Integer.parseInt(stringB.substring(6, 8), 10);
            // 取引時刻
            hour = Integer.parseInt(stringB.substring(8, 10), 10);
            minute = Integer.parseInt(stringB.substring(10, 12), 10);
            second = Integer.parseInt(stringB.substring(12, 14), 10);
            // 取引端末
            device = "";
            // 取引種別フラグ
            typeFlag = Integer.parseInt(stringB.substring(14, 16), 10);

            // 取引種別を解釈
            switch (typeFlag) {
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

            // 取引金額
            price = Integer.parseInt(stringB.substring(16, 22), 10);
            //取引後残高
            balance = Integer.parseInt(stringB.substring(22, 28), 10);

            /*MEMO:
            Dateの月設定は1小さい値を入れる
            Dateの年は-1900下値を入れる
            * */
            history = new CardHistory(new Date(year - 1900, month - 1, day, hour, minute, second),
                    price, balance, type, typeFlag, device);
            histories.add(history);
        }
        return histories;
    }

    /**
     * 残高を返す
     *
     * @return 残高
     * readAllData()を先に実行する必要がある
     */
    @Override
    public int getSFBalance() {
        StringBuilder stringB = new StringBuilder();
        if (balance.get(0).length < 8) {
            return 0;
        }
        //ここだけリトルエンディアンなので注意
        for (int j = 0; j < 4; j++) {
            stringB.append(String.format("%02X", balance.get(0)[3 - j]));
        }
        return Integer.parseInt(stringB.substring(0, 8), 16);
    }

    /**
     * 読み取ったデータからポイント残高を返す
     *
     * @return ポイント残高
     */
    @Override
    public int getPointBalance() {
        StringBuilder stringB = new StringBuilder();
        if (cardInfo.get(2).length < 8) {
            return 0;
        }
        for (int j = 0; j < 4; j++) {
            stringB.append(String.format("%02X", cardInfo.get(2)[j]));
        }
        Log.d("TAG", stringB.toString());
        return Integer.parseInt(stringB.toString(), 16);
    }

    /**
     * カードから読み込み可能なすべてのデータを読み込む
     * カードから残高関連、履歴、カード情報を読み出す
     */
    @Override
    public void readAllData() {
        balance = readBalance();
        historyData = readHistories();
        cardInfo = readCardInfo();
        //複数のシステムがあり、プライマリのほうのIDｍを再度取得するために実行
        getCardType();
    }

    /**
     * カード内のデータを保存するためのCardDataクラスのインスタンスを作り、読み取った内容をセットする
     *
     * @return CardDataクラスのインスタンス
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public CardData getNewCardData() {
        CardData cd = new CardData("大学生協電子マネー", "", 2, getSFBalance(),
                getPointBalance(), getIDm(""), getHistories());
        return cd;
    }

    /**
     * 既存のCardDataクラスのインスタンスを参照し、今読み取った新しいデータを書き加える
     *
     * @param cd CardDataクラスのインスタンスの参照
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateCardData(CardData cd) {
        cd.update(getSFBalance(), getPointBalance(), getHistories());
    }

}

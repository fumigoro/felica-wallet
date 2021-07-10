package com.example.y3033067.nfcreader;

import android.app.Activity;
import android.content.res.AssetManager;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Ayuca extends NFCReader  implements NFCReaderIf  {
    private final int SYSTEM_CODE;
    private final int SERVICE_CODE_HISTORY;
    private final int SERVICE_CODE_BALANCE;
    private final int SERVICE_CODE_INFO;
    private final NfcF nfc;
    ArrayList<Byte[]> historyData, cardInfo, balance;
    ArrayList<CardHistory> histories;
    byte[] targetIDm;
    //アセットの読み込みに使う
    private Activity activity;
    AyucaCode ayucaCode;


    public Ayuca(Tag tag) {
        super(tag);
        this.nfc = NfcF.get(tag);
        SYSTEM_CODE = 0x83EE;
        SERVICE_CODE_HISTORY = 0x898F;//履歴
        SERVICE_CODE_BALANCE = 0x884B;//残高
        SERVICE_CODE_INFO = 0x804B;//カード情報
    }

    /**
     * 利用履歴が保存されたサービスを読み込む
     *
     * @return 読み取ったダンプデータ
     */
    private ArrayList<Byte[]> readHistory() {
        historyData = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            targetIDm = super.readIDm(SYSTEM_CODE);
            //データを取得
            super.getBlockData(targetIDm, SERVICE_CODE_HISTORY, 0, 10, historyData);
            super.getBlockData(targetIDm, SERVICE_CODE_HISTORY, 10, 20, historyData);

            //通信終了
            nfc.close();
//            //        取得したデータをログに表示
//            Log.d("TAG", "利用履歴");
//            for(int i = 0; i< historyData.size(); i++){
//                Log.d("TAG", String.format("<%02X> ",i)+super.hex2string(historyData.get(i),":"));
//            }
//            Log.d("TAG", "================");

            return historyData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 残高情報が保存されたサービスを読み込む
     *
     * @return 読み取ったダンプデータ
     */
    private ArrayList<Byte[]> readBalance() {
        balance = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            targetIDm = super.readIDm(SYSTEM_CODE);
            //データを取得
            super.getBlockData(targetIDm, SERVICE_CODE_BALANCE, 0, 3, balance);

            //通信終了
            nfc.close();
//            Log.d("TAG", "残高情報");
//            for(int i = 0; i< balance.size(); i++){
//                Log.d("TAG", String.format("<%02X> ",i)+super.hex2string(balance.get(i),":"));
//            }
            return balance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * カード情報が保存されたサービスを読み込む
     * @return 読み取ったダンプデータ
     */
    private ArrayList<Byte[]> readCardInfo() {
        cardInfo = new ArrayList<>();
        try {
            //通信開始

            nfc.connect();

            //PollingコマンドでIDｍを取得
            targetIDm = super.readIDm(SYSTEM_CODE);
            //データを取得
            super.getBlockData(targetIDm, SERVICE_CODE_INFO, 0, 2, cardInfo);

            //通信終了
            nfc.close();

//            Log.d("TAG", "カード情報");
//            for(int i = 0; i< cardInfo.size(); i++){
//                Log.d("TAG", String.format("<%02X> ",i)+super.hex2string(cardInfo.get(i),":"));
//            }
            return cardInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 利用履歴を返す
     * @return 利用履歴
     * readAllData()を先に実行する必要がある
     */
    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<CardHistory> getHistories() {
        StringBuilder stringB;
        String discount, start, end, device, type;
        int year, month, day, hour, minute, price, balance,  pointFlag,   typeFlag, deviceFlag;
        int point,pointBalance;
        int grantedNormalPoint, grantedBonusPoint, usedPoint;

        String stringTmp;
        CardHistory history;
        histories = new ArrayList<>();
        if(historyData == null){
            return histories;
        }
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
            start = getStationName(Integer.parseInt(stringB.substring(32, 48), 2));
            end = getStationName(Integer.parseInt(stringB.substring(48, 64), 2));
            deviceFlag = Integer.parseInt(stringB.substring(64, 68), 2);
            typeFlag = Integer.parseInt(stringB.substring(68, 72), 2);


            switch (deviceFlag){
                case 0x5:
                    device = "車載機";
                    break;
                case 0x7:
                    device = "窓口";
                    break;
                case 0x8:
                    device = "入金機";
                    break;
                default:
                    device = String.format("不明機(%04X)",deviceFlag);
            }
            switch (typeFlag){
                case 0x3:
                    //SF利用
                    type = "決済";
                    break;
                case 0x9:
                    //チャージ
                    type = "チャージ";
                    break;
                case 0xA:
                    //新規発行
                    type = "新規発行";
                    break;
                default:
                    //不明
                    type = String.format("%X", typeFlag);
            }

            price = Integer.parseInt(stringB.substring(72, 88), 2);
            balance = Integer.parseInt(stringB.substring(88, 104), 2);
            pointBalance = Integer.parseInt(stringB.substring(104, 124), 2);
//            pointFlag = Integer.parseInt(stringB.substring(124, 128), 2);

            grantedNormalPoint = 0;
            usedPoint = 0;
            grantedBonusPoint = 0;

            //履歴最終行(前回の利用金額等が残っていない)かどうか確認
            if (i < historyData.size() - 1) {
                //最終行でない場合

                //1件前の履歴のポイント数を取得
                int previousPointBalance = histories.get(historyData.size() - i - 2).getPointBalance();
                //1件前の取引後残高を取得
                int previousBalance = histories.get(historyData.size() - i - 2).getBalance();
                //今回の決済でのSF残高利用金額を計算
                int sfUsedPrice = previousBalance - balance;
                //今回増えたポイント
                point = (pointBalance - previousPointBalance);

                if(typeFlag == 0x03){
                    grantedNormalPoint = (int) (sfUsedPrice * 10 * 0.02);
                    usedPoint = (price - sfUsedPrice)*10;
                    grantedBonusPoint = point - grantedNormalPoint + usedPoint;
                    //ポイント取り扱いフラグをあげる
                    history.setPointFlag(true);
                    history.setPointParams(grantedNormalPoint,grantedBonusPoint,usedPoint);
                }
            }

            /*MEMO:
            Dateの月設定は1小さい値を入れる
            Dateの年は-1900下値を入れる
            * */
            history.setAllParams(new Date(year + 2000 - 1900, month-1, day, hour, minute, 0),
                    price, pointBalance, balance, type, typeFlag, discount, device, start, end);

            histories.add(history);
        }

        Collections.reverse(histories);
        return histories;
    }

    /**
     * 現金積み増し分の残高を返す
     * @return 残高
     * readAllData()を先に実行する必要がある
     */
    @Override
    public int getSFBalance(){
        String stringTmp;
        StringBuilder stringB = new StringBuilder();
        for (int j = 0; j < balance.get(0).length; j++) {
            //16進数のまま文字列へ
            stringTmp = String.format("%02X", balance.get(0)[j]);
            stringB.append(stringTmp);
        }
        return Integer.parseInt(stringB.substring(0, 4), 16);
    }

    /**
     * 読み取ったデータからポイント残高を返す
     * @return ポイント残高
     */
    @Override
    public int getPointBalance() {
        String stringTmp;
        StringBuilder stringB = new StringBuilder();
        for (int j = 0; j < balance.get(0).length; j++) {
            //16進数のまま文字列へ
            stringTmp = String.format("%02X", balance.get(0)[j]);
            stringB.append(stringTmp);
        }
        return Integer.parseInt(stringB.substring(0, 4), 16);
    }

    /**
     * カードから読み込み可能なすべてのデータを読み込む
     * カードから残高関連、履歴、カード情報を読み出す
     */
    @Override
    public void readAllData(){
        balance = readBalance();
        historyData = readHistory();
        cardInfo = readCardInfo();
    }

    public void loadAssetFile(Activity _activity){
        activity = _activity;
        Gson gson = new Gson();
        try{
            AssetManager assetManager = activity.getResources().getAssets(); //アセット呼び出し
            InputStream inputStream = assetManager.open("code_ayuca.json"); //Jsonファイル
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder json = new StringBuilder();
            int i = 0;
            String jsonTmp;
            while ((jsonTmp = bufferedReader.readLine()) != null){
                json.append(jsonTmp);
            }
            ayucaCode = gson.fromJson(json.toString(),AyucaCode.class);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private String getStationName(int code){
        if(ayucaCode==null){
            return String.format("%04X",code);
        }
        return ayucaCode.getStation(code);
    }



}

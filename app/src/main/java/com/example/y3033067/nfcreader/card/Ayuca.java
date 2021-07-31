package com.example.y3033067.nfcreader.card;

import android.app.Activity;
import android.content.res.AssetManager;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.y3033067.nfcreader.storage.CardData;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Ayuca extends FelicaReader implements FelicaCard {
    // Felicaシステムコード
    private final int SYSTEM_CODE;
    // Felica サービスコード
    private final int SERVICE_CODE_HISTORY;
    private final int SERVICE_CODE_BALANCE;
    private final int SERVICE_CODE_INFO;

    private final NfcF nfc;
    // 読み取った生のバイナリを入れるリスト
    ArrayList<Byte[]> historyData, cardInfo, balance;
    ArrayList<CardHistory> histories;

    //Polingで取得した生のIDｍバイナリを入れる
    byte[] targetIDm;
    //アセットの読み込みに使うメインのActivity
    private Activity activity;

    // Ayucaのバス停コードを持つオブジェクト
    AyucaStationCode ayucaCode;

    public Ayuca(Tag tag) {
        super(tag);
        this.nfc = NfcF.get(tag);
        SYSTEM_CODE = CardParams.SYSTEM_CODE_AYUCA;
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
        int year, month, day, hour, minute, price, balance, typeFlag, deviceFlag;
        int point,pointBalance;
        int grantedNormalPoint, grantedBonusPoint, usedPoint;
        int sfUsedPrice = 0;
        //利用履歴を持つクラスのリスト
        histories = new ArrayList<>();
        // 履歴0件の場合は処理終了
        if(historyData == null){
            return histories;
        }

        //1件ずつバイナリを解釈しCardHistoryのインスタンスとしてhistoriesリストに入れていく
        for (int i = historyData.size()-1; i >= 0; i--) {
            stringB = new StringBuilder();
            CardHistory history = new CardHistory();
            // 16進数の生データを2進数へ変換
            for (int j = 0; j < historyData.get(0).length; j++) {
                String stringTmp = (Integer.toBinaryString(Byte.toUnsignedInt(historyData.get(i)[j])));
                stringB.append(String.format("%8s", stringTmp).replace(' ', '0'));
            }
            //　処理年月日
            year = Integer.parseInt(stringB.substring(0, 7), 2);
            month = Integer.parseInt(stringB.substring(7, 11), 2);
            day = Integer.parseInt(stringB.substring(11, 16), 2);
            //処理時刻
            hour = Integer.parseInt(stringB.substring(16, 22), 2);
            minute = Integer.parseInt(stringB.substring(22, 28), 2);
            //割引フラグ
            discount = String.format("%X", Integer.parseInt(stringB.substring(28, 32), 2));
            //乗車バス停
            start = getStationName(Integer.parseInt(stringB.substring(32, 48), 2));
            //降車バス停
            end = getStationName(Integer.parseInt(stringB.substring(48, 64), 2));
            //処理端末フラグ
            deviceFlag = Integer.parseInt(stringB.substring(64, 68), 2);
            //処理種別フラグ
            typeFlag = Integer.parseInt(stringB.substring(68, 72), 2);

            // 処理端末を解釈
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
            //処理種別を解釈
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

            //取引金額
            price = Integer.parseInt(stringB.substring(72, 88), 2);
            //取引後残高
            balance = Integer.parseInt(stringB.substring(88, 104), 2);
            //取引後ポイント残高
            pointBalance = Integer.parseInt(stringB.substring(104, 124), 2);

            // ポイントに関する計算
            //　NOTE:アプリ内ではポイント数は全て実値を10倍した整数として扱う
            // 今回の決済で付与された通常ポイント
            grantedNormalPoint = 0;
            // 今回の決済で使用(還元)されたポイント
            usedPoint = 0;
            // 今回の決済で付与されたボーナスポイント
            grantedBonusPoint = 0;

            //履歴最終行(前回の利用金額等が残っていない)かどうか確認
            //最終行の場合、取引前の残高(直前の取引後の残高)が分からず正確なポイント計算ができないため
            if (i < historyData.size() - 1) {
                //最終行でない場合

                //1件前の履歴のポイント数を取得
                int previousPointBalance = histories.get(historyData.size() - i - 2).getPointBalance();
                //1件前の取引後残高を取得
                int previousBalance = histories.get(historyData.size() - i - 2).getBalance();
                //今回の決済でのSF残高利用金額を計算
                sfUsedPrice = previousBalance - balance;
                //今回増えたポイント
                point = (pointBalance - previousPointBalance);

                //運賃支払の場合
                if(typeFlag == 0x03){
                    //通常ポイントは現金残高から現金残高からの支払い額の2%
                    //実際の値を10倍し整数として保存するため10倍する
                    grantedNormalPoint = (int) (sfUsedPrice * 10 * 0.02);
                    //使用したポイントは、取引金額-現金残高からの支払い額
                    usedPoint = (price - sfUsedPrice)*10;
                    //ボーナスポイントは、今回増えたポイント-通常ポイント+使用ポイント
                    grantedBonusPoint = point - grantedNormalPoint + usedPoint;

                    //ポイント取り扱いフラグをあげる
                    history.setPointFlag(true);
                    //CardHistoryのインスタンスにポイント関係のデータをセット
                    history.setPointParams(grantedNormalPoint,grantedBonusPoint,usedPoint);
                }else{
                    //運賃支払以外の取引の場合
                    //ポイントの増減は発生しないため0扱い
                    sfUsedPrice = 0;
                }
            }else{
                //最終行の場合

                if(typeFlag == 0x03){
                    //運賃支払の場合は月間利用金額の計算時に必要なため、
                    //ポイント還元はなかったものとして取引金額をそのまま入れる
                    sfUsedPrice = price;
                }

            }

            /*MEMO:
            Dateの月設定は1小さい値を入れる
            Dateの年は-1900下値を入れる
            * */
            history.setAllParams(new Date(year + 2000 - 1900, month-1, day, hour, minute, 0),
                    price, sfUsedPrice, pointBalance, balance, type, typeFlag, discount, device, start, end);

            histories.add(history);
        }
        //最新の履歴が戦闘になるように順番を反転
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
        //16進数文字列をパースし整数へ
        return Integer.parseInt(stringB.substring(0, 4), 16);
    }

    /**
     * 読み取ったデータからポイント残高を返す
     * @return ポイント残高
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int getPointBalance() {
        String stringTmp;
        StringBuilder stringB = new StringBuilder();
        for (int j = 0; j < balance.get(1).length; j++) {
            //2進数文字列へ変換
            stringTmp = (Integer.toBinaryString(Byte.toUnsignedInt(balance.get(1)[j])));
            stringB.append(String.format("%8s", stringTmp).replace(' ', '0')); // 0埋め
        }
        //2進数文字列をパースし整数へ
        return Integer.parseInt(stringB.substring(32, 52), 2);
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
        //複数のシステムがあり、プライマリのほうのIDｍを再度取得するために実行
        getCardType();
    }

    /**
     * アセットにあるファイルからAyucaのバス停コード一覧を読み込んでAyucaCodeのインスタンスとして保存する
     * @param _activity メインのActivity
     */
    public void loadAssetFile(Activity _activity){
        activity = _activity;
        // JsonとJavaオブジェクトの変換を行うライブラリ
        Gson gson = new Gson();

        try{
            //アセット呼び出し
            AssetManager assetManager = activity.getResources().getAssets();
            //Jsonファイルを開く
            InputStream inputStream = assetManager.open("code_ayuca.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder json = new StringBuilder();
            int i = 0;
            String jsonTmp;
            //中身を読み込み
            while ((jsonTmp = bufferedReader.readLine()) != null){
                json.append(jsonTmp);
            }
            //Javaオブジェクトへ変換
            ayucaCode = gson.fromJson(json.toString(), AyucaStationCode.class);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * バス停コードを渡すとバス停名を返す
     * @param code バス停コード
     * @return バス停名
     */
    private String getStationName(int code){
        if(ayucaCode==null){
            return String.format("%04X",code);
        }
        return ayucaCode.getStation(code);
    }

    /**
     * カード内のデータを保存するためのCardDataクラスのインスタンスを作り、読み取った内容をセットする
     * @return CardDataクラスのインスタンス
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public CardData getNewCardData(){
        CardData cd = new CardData("Ayuca","",1,getSFBalance(),
                getPointBalance(),getIDm(""),getHistories());
        return cd;
    }

    /**
     * 既存のCardDataクラスのインスタンスを参照し、今読み取った新しいデータを書き加える
     * @param cd CardDataクラスのインスタンスの参照
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateCardData(CardData cd){
        cd.update(getSFBalance(),getPointBalance(),getHistories());
    }

}

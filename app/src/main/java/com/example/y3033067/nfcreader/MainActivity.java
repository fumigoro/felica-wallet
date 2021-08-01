package com.example.y3033067.nfcreader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.y3033067.nfcreader.card.Ayuca;
import com.example.y3033067.nfcreader.card.CampusPay;
import com.example.y3033067.nfcreader.card.CardHistory;
import com.example.y3033067.nfcreader.card.CardParams;
import com.example.y3033067.nfcreader.card.FelicaReader;
import com.example.y3033067.nfcreader.card.StudentIDCard;
import com.example.y3033067.nfcreader.storage.CardData;
import com.example.y3033067.nfcreader.storage.Storage;
import com.example.y3033067.nfcreader.ui.HistoryUI;
import com.example.y3033067.nfcreader.ui.TabAdapter;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    TabLayout tabLayout;
    TextView cardID, cardName, cardBalance;
    HistoryUI[] historyUI;
    View[] historyWrapper;
    String[] myPageIDmList;

    Storage userDataStorage;
    File userDataFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nfcreader);
        // xmlからTabLayoutの取得
        tabLayout = findViewById(R.id.tab_layout);
        // xmlからViewPagerを取得
        ViewPager viewPager = findViewById(R.id.pager);

        // 表示Pageに必要な項目を設定
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        // ViewPagerにページを設定
        viewPager.setAdapter(adapter);
        // ViewPagerをTabLayoutを設定
        tabLayout.setupWithViewPager(viewPager);

        //Felica読み取りのインテント
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndef};

        // FelicaはNFC-TypeFなのでNfcFのみ指定
        techListsArray = new String[][]{
                new String[]{NfcF.class.getName()}
        };

        // NfcAdapterを取得
        mAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        //保存済みデータを読み込み
        String fileName = "data.json";
        Context context = getApplicationContext();
        userDataFile = new File(context.getFilesDir(), fileName);
        userDataStorage = roadUserDataFile();
        if (userDataStorage == null) {
            userDataStorage = new Storage();
        }
        myPageIDmList = new String[5];

    }


    @Override
    protected void onResume() {
        super.onResume();
        // NFCの読み込みを有効化
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);

    }


    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Intentに入っているTagの基本データ取得
        setButtonListener();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }

        FelicaReader card = new FelicaReader(tag);
        //読み取ったカードの種類を判別
        int type = card.getCardType();

        //既知のカードか否か、保存するか否かのフラグを設定
        //更新or新規保存orなにもしないの3択
        int cardDataFlag;
        CardData newCardData = userDataStorage.getCardData(card.getIDm(""));
        if (newCardData != null) {
            cardDataFlag = 1; //更新
        } else {
            cardDataFlag = 2; //新規保存
        }
        //読み取った履歴を保存するオブジェクト
        ArrayList<CardHistory> histories = new ArrayList<>();
        //カードIDｍを表示
        final String cardIDText = String.format("Felica IDm：%s", card.getIDm(" "));
        String cardBalanceText = "";
        String cardNameText = "非対応カード";
        //カード種別に応じた処理
        switch (type) {
            case CardParams.TYPE_CODE_AYUCA:
                //Ayuca
                Ayuca ayuca = new Ayuca(tag);
                //バス停コード一覧ファイルを読み込み
                ayuca.loadAssetFile((Activity) findViewById(R.id.fragment_show).getContext());
                //カードからデータを読み取り
                ayuca.readAllData();
                //カードの履歴を取得
                histories = ayuca.getHistories();

                //画面表示テキスト
                cardNameText = "Ayuca";
                cardBalanceText = String.format("￥%,d", ayuca.getSFBalance());

                switch (cardDataFlag) {
                    case 1:
                        //更新
                        ayuca.updateCardData(newCardData);
                        break;
                    case 2:
                        //新規保存
                        newCardData = ayuca.getNewCardData();
                        userDataStorage.addCard(newCardData);
                        break;
                }
                //マイページのUIへデータを入れる
                if (newCardData != null) {
                    //空メッセージを非表示に
                    findViewById(R.id.myCard_monthly_empty_message).setVisibility(View.GONE);
                    findViewById(R.id.myCard_empty_message).setVisibility(View.GONE);

                    View myCard = findViewById(R.id.myCard_1);
                    View muCardMonthly = findViewById(R.id.myCard_monthly_1);
                    newCardData.setMyPageInfo(myCard, muCardMonthly);
                    myPageIDmList[0] = ayuca.getIDm("");
                }
                break;

            case 2:
                //CampusPay
                CampusPay campusPay = new CampusPay(tag);
                //カードからデータを読み取り
                campusPay.readAllData();
                histories = campusPay.getHistories();

                cardNameText = "大学生協電子マネー";
                cardBalanceText = String.format("￥%,d", campusPay.getSFBalance());

                switch (cardDataFlag) {
                    case 1:
                        //更新
                        campusPay.updateCardData(newCardData);
                        break;
                    case 2:
                        //新規保存
                        newCardData = campusPay.getNewCardData();
                        userDataStorage.addCard(newCardData);
                        break;
                }
                //マイページのUIへデータを入れる
                if (newCardData != null) {
                    findViewById(R.id.myCard_monthly_empty_message).setVisibility(View.GONE);
                    findViewById(R.id.myCard_empty_message).setVisibility(View.GONE);
                    View myCard = findViewById(R.id.myCard_2);
                    View muCardMonthly = findViewById(R.id.myCard_monthly_2);
                    newCardData.setMyPageInfo(myCard, muCardMonthly);
                    myPageIDmList[1] = campusPay.getIDm("");
                }
                break;
            case 3:
                //学生証
                StudentIDCard idCard = new StudentIDCard(tag);
                //カードからデータを読み取り
                idCard.readAllData();

                cardNameText = "岐阜大学学生証";
                cardBalanceText = idCard.getStudentID();

                break;
        }

        //読み取り表示部へ表示
        cardID = findViewById(R.id.card_id);
        cardName = findViewById(R.id.card_name);
        cardBalance = findViewById(R.id.card_balance);
        cardName.setText(cardNameText);
        cardBalance.setText(cardBalanceText);
        cardID.setText(cardIDText);

        //読み取り表示の履歴部分を表示
        if (newCardData != null) {
            showHistory(type, newCardData.getHistories());
        }

        userDataStorage.printList();
        updateMyPage();
//        userDataStorage.reset();
        saveUserDataFile();
//        タブ切り替え
//        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        //読み取り待機のリング状のUIを消し、読み取り結果を表示
        findViewById(R.id.read_waiting_view).setVisibility(View.GONE);
        findViewById(R.id.history_scrollView).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
        saveUserDataFile();
    }

    /**
     * 履歴をUIに表示する
     *
     * @param type      カード種別
     * @param histories 履歴
     */
    private void showHistory(int type, ArrayList<CardHistory> histories) {
        Context context = getApplicationContext();
        TextView name_start, name_end, price, bonusPoint, point, date, usedPoint;
        ImageView line;
        historyWrapper = new View[40];
        historyUI = new HistoryUI[40];

        //カード履歴の表示部分の大枠を取得
        historyWrapper[0] = findViewById(R.id.history_1);
        historyWrapper[1] = findViewById(R.id.history_2);
        historyWrapper[2] = findViewById(R.id.history_3);
        historyWrapper[3] = findViewById(R.id.history_4);
        historyWrapper[4] = findViewById(R.id.history_5);
        historyWrapper[5] = findViewById(R.id.history_6);
        historyWrapper[6] = findViewById(R.id.history_7);
        historyWrapper[7] = findViewById(R.id.history_8);
        historyWrapper[8] = findViewById(R.id.history_9);
        historyWrapper[9] = findViewById(R.id.history_10);
        historyWrapper[10] = findViewById(R.id.history_11);
        historyWrapper[11] = findViewById(R.id.history_12);
        historyWrapper[12] = findViewById(R.id.history_13);
        historyWrapper[13] = findViewById(R.id.history_14);
        historyWrapper[14] = findViewById(R.id.history_15);
        historyWrapper[15] = findViewById(R.id.history_16);
        historyWrapper[16] = findViewById(R.id.history_17);
        historyWrapper[17] = findViewById(R.id.history_18);
        historyWrapper[18] = findViewById(R.id.history_19);
        historyWrapper[19] = findViewById(R.id.history_20);
        historyWrapper[20] = findViewById(R.id.history_21);
        historyWrapper[21] = findViewById(R.id.history_22);
        historyWrapper[22] = findViewById(R.id.history_23);
        historyWrapper[23] = findViewById(R.id.history_24);
        historyWrapper[24] = findViewById(R.id.history_25);
        historyWrapper[25] = findViewById(R.id.history_26);
        historyWrapper[26] = findViewById(R.id.history_27);
        historyWrapper[27] = findViewById(R.id.history_28);
        historyWrapper[28] = findViewById(R.id.history_29);
        historyWrapper[29] = findViewById(R.id.history_30);
        historyWrapper[30] = findViewById(R.id.history_31);
        historyWrapper[31] = findViewById(R.id.history_32);
        historyWrapper[32] = findViewById(R.id.history_33);
        historyWrapper[33] = findViewById(R.id.history_34);
        historyWrapper[34] = findViewById(R.id.history_35);
        historyWrapper[35] = findViewById(R.id.history_36);
        historyWrapper[36] = findViewById(R.id.history_37);
        historyWrapper[37] = findViewById(R.id.history_38);
        historyWrapper[38] = findViewById(R.id.history_39);
        historyWrapper[39] = findViewById(R.id.history_40);

        //UI部品の取得と文字色の設定
        for (int i = 0; i < historyWrapper.length; i++) {
            name_start = historyWrapper[i].findViewById(R.id.name_start);
            name_end = historyWrapper[i].findViewById(R.id.name_end);
            price = historyWrapper[i].findViewById(R.id.price);
            date = historyWrapper[i].findViewById(R.id.date);
            point = historyWrapper[i].findViewById(R.id.point);
            bonusPoint = historyWrapper[i].findViewById(R.id.bonusPoint);
            usedPoint = historyWrapper[i].findViewById(R.id.usedPoint);
            line = historyWrapper[i].findViewById(R.id.line);

            if (type == 1 && i >= CardParams.MAX_HISTORY_ITEMS_AYUCA) {
                //Ayucaかつデータが21件以上の場合
                //それは内部保存データからの読み込み分のため金額の文字色をグレーに設定
                price.setTextColor(Color.rgb(0xBE, 0xBE, 0xBE));
            } else if (type == 2 && i >= CardParams.MAX_HISTORY_ITEMS_CAMPUS_PAY) {
                //生協電子マネーかつデータが11件以上の場合
                //それは内部保存データからの読み込み分のため金額の文字色をグレーに設定
                price.setTextColor(Color.rgb(0xBE, 0xBE, 0xBE));
            } else {
                price.setTextColor(Color.rgb(0x81, 0xD4, 0xFA));
            }
            historyUI[i] = new HistoryUI(name_start, name_end, price, date, point, bonusPoint, usedPoint,
                    line, historyWrapper[i], context);
        }
        //取得したUIにデータを入れる
        for (int i = 0; i < historyUI.length; i++) {
            //UIが多めに作ってあるため。履歴を最後まで表示しきったら、それ以降は不可視化
            if (i < histories.size()) {
                historyUI[i].setText(histories.get(i), type);
                historyWrapper[i].setVisibility(View.VISIBLE);
            } else {
                historyWrapper[i].setVisibility(View.GONE);
            }
        }
        //境界部の「ここから下は以前保存したデータです」メッセージ表示切り替え
        if (type == 1 && histories.size() > CardParams.MAX_HISTORY_ITEMS_AYUCA) {
            //Ayucaかつ21件以上
            findViewById(R.id.saved_data_massage_20).setVisibility(View.VISIBLE);
            findViewById(R.id.saved_data_massage_10).setVisibility(View.GONE);
        } else if (type == 2 && histories.size() > CardParams.MAX_HISTORY_ITEMS_CAMPUS_PAY) {
            //生協電子マネーかつ11件以上
            findViewById(R.id.saved_data_massage_20).setVisibility(View.GONE);
            findViewById(R.id.saved_data_massage_10).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.saved_data_massage_20).setVisibility(View.GONE);
            findViewById(R.id.saved_data_massage_10).setVisibility(View.GONE);
        }
    }

    /**
     * 保存済みのデータファイルを読み込む
     *
     * @return Storageクラス
     */
    private Storage roadUserDataFile() {
        Storage userData;
        Gson gson = new Gson();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(userDataFile));
            StringBuilder json = new StringBuilder();
            String jsonTmp;
            while ((jsonTmp = bufferedReader.readLine()) != null) {
                json.append(jsonTmp);
            }
            userData = gson.fromJson(json.toString(), Storage.class);
            return userData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * カード登録データをファイルに書き出し保存する
     */
    private void saveUserDataFile() {
        Gson gson = new Gson();
        String jsonData = gson.toJson(userDataStorage);
        //既存内容を上書き
        try (FileWriter writer = new FileWriter(userDataFile, false)) {
            writer.write(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRestart() {
        super.onRestart();
        setButtonListener();
        updateMyPage();
    }

    /**
     * マイページのカード一覧にあるデータ削除ボタンのリスナー設定
     */
    private void setButtonListener() {
        Context context = this;
        findViewById(R.id.myCard_1).findViewById(R.id.delete_button).setOnClickListener(
                v -> {
                    //ダイアナログを表示
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder
                            .setTitle("データを削除しますか？") // タイトル
                            .setMessage("この操作は元に戻せません") // メッセージ
                            .setNegativeButton("削除", (dialog, id) -> {
                                //承諾時の処理
                                userDataStorage.delete(myPageIDmList[0]);
                                updateMyPage();
                                userDataStorage.printList();
                            })
                            // キャンセル時の処理
                            .setPositiveButton("キャンセル", (dialog, id) -> {
                            })
                            .create();
                    AlertDialog dialog = builder.create();
                    // 表示
                    dialog.show();

                }
        );
        findViewById(R.id.myCard_2).findViewById(R.id.delete_button).setOnClickListener(
                v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder
                            .setTitle("データを削除しますか？") // タイトル
                            .setMessage("この操作は元に戻せません") // メッセージ
                            .setNegativeButton("削除", (dialog, id) -> {
                                userDataStorage.delete(myPageIDmList[1]);
                                updateMyPage();
                                userDataStorage.printList();
                            })
                            .setPositiveButton("キャンセル", (dialog, id) -> {

                            })
                            .create();
                    AlertDialog dialog = builder.create();
                    // AlertDialogを表示
                    dialog.show();
                }
        );
        findViewById(R.id.myCard_3).findViewById(R.id.delete_button).setOnClickListener(
                v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder
                            .setTitle("データを削除しますか？") // タイトル
                            .setMessage("この操作は元に戻せません") // メッセージ
                            .setNegativeButton("削除", (dialog, id) -> {
                                userDataStorage.delete(myPageIDmList[2]);
                                updateMyPage();
                                userDataStorage.printList();
                            })
                            .setPositiveButton("キャンセル", (dialog, id) -> {

                            })
                            .create();
                    AlertDialog dialog = builder.create();
                    // AlertDialogを表示
                    dialog.show();
                }
        );
        findViewById(R.id.myCard_4).findViewById(R.id.delete_button).setOnClickListener(
                v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder
                            .setTitle("データを削除しますか？") // タイトル
                            .setMessage("この操作は元に戻せません") // メッセージ
                            .setNegativeButton("削除", (dialog, id) -> {
                                userDataStorage.delete(myPageIDmList[3]);
                                updateMyPage();
                                userDataStorage.printList();
                            })
                            .setPositiveButton("キャンセル", (dialog, id) -> {

                            })
                            .create();
                    AlertDialog dialog = builder.create();
                    // AlertDialogを表示
                    dialog.show();
                }
        );
        findViewById(R.id.myCard_5).findViewById(R.id.delete_button).setOnClickListener(
                v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder
                            .setTitle("データを削除しますか？") // タイトル
                            .setMessage("この操作は元に戻せません") // メッセージ
                            .setNegativeButton("削除", (dialog, id) -> {
                                userDataStorage.delete(myPageIDmList[4]);
                                updateMyPage();
                                userDataStorage.printList();
                            })
                            .setPositiveButton("キャンセル", (dialog, id) -> {

                            })
                            .create();
                    AlertDialog dialog = builder.create();
                    // AlertDialogを表示
                    dialog.show();
                }
        );
    }

    /**
     * マイページのUI表示を更新する
     */
    private void updateMyPage() {
        ArrayList<CardData> cards = userDataStorage.getAllCardData();
        View[] myCards = new View[5];
        View[] myCardsMonthly = new View[5];
        //登録済みカードと今月の支払額UIを取得
        myCards[0] = findViewById(R.id.myCard_1);
        myCardsMonthly[0] = findViewById(R.id.myCard_monthly_1);
        myCards[1] = findViewById(R.id.myCard_2);
        myCardsMonthly[1] = findViewById(R.id.myCard_monthly_2);
        myCards[2] = findViewById(R.id.myCard_3);
        myCardsMonthly[2] = findViewById(R.id.myCard_monthly_3);
        myCards[3] = findViewById(R.id.myCard_4);
        myCardsMonthly[3] = findViewById(R.id.myCard_monthly_4);
        myCards[4] = findViewById(R.id.myCard_5);
        myCardsMonthly[4] = findViewById(R.id.myCard_monthly_5);
        //表示を初期化
        for (View myCard : myCards) {
            myCard.setVisibility(View.GONE);
        }
        for (View myCardMonthly : myCardsMonthly) {
            myCardMonthly.setVisibility(View.GONE);
        }
        //空メッセージを可視化
        findViewById(R.id.myCard_monthly_empty_message).setVisibility(View.VISIBLE);
        findViewById(R.id.myCard_empty_message).setVisibility(View.VISIBLE);

        //カードが登録されている場合
        if (cards.size() > 0) {
            //からメッセージを非表示
            findViewById(R.id.myCard_monthly_empty_message).setVisibility(View.GONE);
            findViewById(R.id.myCard_empty_message).setVisibility(View.GONE);
            //データを入れる
            for (int i = 0; i < cards.size(); i++) {
                switch (cards.get(i).getCardType()) {
                    case CardParams.TYPE_CODE_AYUCA:
                        //Ayucaの場合
                        cards.get(i).setMyPageInfo(myCards[0], myCardsMonthly[0]);
                        myPageIDmList[0] = cards.get(i).getIDm();
                        break;
                    case CardParams.TYPE_CODE_CAMPUS_PAY:
                        //大学生協電子マネーの場合
                        cards.get(i).setMyPageInfo(myCards[1], myCardsMonthly[1]);
                        myPageIDmList[1] = cards.get(i).getIDm();
                        break;
                }

            }
        }
        //グラフの描画
        showGraph(userDataStorage);

    }

    /**
     * 登録済みカードそれぞれの過去5ヶ月分の利用金額をグラフで表示する
     *
     * @param userDataStorage 登録済みカードデータ
     */
    void showGraph(Storage userDataStorage) {
        View[] chartWrapper = new View[2];
        //グラフ表示領域
        chartWrapper[0] = findViewById(R.id.history_chart1);
        chartWrapper[1] = findViewById(R.id.history_chart2);

        TextView title;
        HorizontalBarChart chart;
        int count = Math.min(chartWrapper.length, userDataStorage.getAllCardData().size());
        //各グラフにタイトルの設定とグラフ描画エリアの取得
        for (int i = 0; i < count; i++) {
            title = chartWrapper[i].findViewById(R.id.chart_title);
            chart = chartWrapper[i].findViewById(R.id.usage_chart);
            setChartParams(chart, userDataStorage.getAllCardData().get(i));
            String titleText = userDataStorage.getAllCardData().get(i).getCardName() + " 月額利用履歴";
            title.setText(titleText);
        }

    }

    /**
     * グラフを描画するHorizontalBarChartのインスタンスに必要なパラメータを入れる
     *
     * @param chart    HorizontalBarChartのインスタンス
     * @param cardData カードデータ
     */
    @SuppressLint("DefaultLocale")
    private void setChartParams(HorizontalBarChart chart, CardData cardData) {
        // グラフ表示する月数
        final int CHART_ITEM_NUM = 5;

        //今月から5ヶ月前までの各月を表すCalendarオブジェクトを生成
        ArrayList<Calendar> date = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        for (int i = 0; i < CHART_ITEM_NUM; i++) {
            date.add((Calendar) calendar.clone());
            calendar.add(Calendar.MONTH, -1);
        }
        Collections.reverse(date);


        // ===== Entry =====
        // 月ごとのEntryを作成
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < CHART_ITEM_NUM; i++) {
            int value = cardData.getMonthlyUsage(date.get(i).get(Calendar.YEAR), date.get(i).get(Calendar.MONTH) + 1);
            entries.add(new BarEntry((float) i, value));
            Log.d("chart", i + ":" + value + "," + date.get(i).get(Calendar.YEAR) + "/" + date.get(i).get(Calendar.MONTH) + 1);
        }

//        ArrayList<String> xValues = new ArrayList<>();
//        for (int i = 0; i < CHART_ITEM_NUM; i++) {
//            xValues.add(String.format("%d月", date.get(i).get(Calendar.MONTH) + 1));
//        }

        // ===== DataSet =====
        BarDataSet dataSet = new BarDataSet(entries, cardData.getCardName());
        //ハイライト無効化
        dataSet.setHighlightEnabled(false);
        //色の設定
        switch (cardData.getCardType()) {
            case CardParams.TYPE_CODE_AYUCA:
                break;
            case CardParams.TYPE_CODE_CAMPUS_PAY:
                dataSet.setColor(Color.rgb(0xFF, 0x91, 0x01));
                break;
        }

        // ===== DataSetのリスト =====
        ArrayList<IBarDataSet> bars = new ArrayList<>();
        bars.add(dataSet);

        // ===== BarData =====
        BarData data = new BarData(bars);
        data.setBarWidth(0.7f);
        data.setValueTextSize(12f);
        data.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value <= 0) {
                    return "利用なし";
                } else {
                    return String.format("%,d", (int) value);
                }

            }
        });
        chart.setData(data);

        //5ヶ月分のラベルを作成
        final String[] labels = new String[CHART_ITEM_NUM + 1];
        for (int i = 0; i < CHART_ITEM_NUM; i++) {
            labels[i] = String.format("%d月", date.get(i).get(Calendar.MONTH) + 1);
        }

        // 軸の設定
        // X軸(左端)
        XAxis xl = chart.getXAxis();
        //有効化
        xl.setEnabled(true);
        //ラベルの個数を設定
        xl.setLabelCount(5);
        //ラベルを設定
        xl.setValueFormatter(new IndexAxisValueFormatter(labels));
        //軸線を表示
        xl.setDrawAxisLine(false);
        //ラベルを表示
        xl.setDrawLabels(true);
        //位置指定
        xl.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        //文字サイズ指定
        xl.setTextSize(12f);
        //グリッド線
        xl.setDrawGridLines(false);

        // 左右の軸を無効化
        chart.getAxisRight().setEnabled(true);
        chart.getAxisLeft().setEnabled(false);

        //グラフ上の表示
        // バー上に値表示
        chart.setDrawValueAboveBar(true);
        // グラフの説明を非表示
        chart.getDescription().setEnabled(false);
        // グラフのタッチを無効化
        chart.setClickable(false);
        // 凡例を無効化
        chart.getLegend().setEnabled(false);
        // グラフ描画
        chart.invalidate();
        // Y方向のアニメーション
        chart.animateY(500);

    }

}

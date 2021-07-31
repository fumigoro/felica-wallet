package com.example.y3033067.nfcreader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.utils.Easing;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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

import com.example.y3033067.nfcreader.storage.CardData;
import com.example.y3033067.nfcreader.storage.Storage;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
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
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private Ayuca ayuca;
    private CampusPay campusPay;
    private StudentIDCard idCard;
    TabLayout tabLayout;
    TextView cardID,cardName,cardBalance;
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
        tabLayout =  findViewById(R.id.tab_layout);
        // xmlからViewPagerを取得
        ViewPager viewPager = findViewById(R.id.pager);

        // 表示Pageに必要な項目を設定
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        // ViewPagerにページを設定
        viewPager.setAdapter(adapter);
        // ViewPagerをTabLayoutを設定
        tabLayout.setupWithViewPager(viewPager);

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
        userDataStorage =  roadUserDataFile();
        if(userDataStorage==null){
            userDataStorage = new Storage();
        }
        myPageIDmList = new String[5];
//        updateMyPage();


    }



    @Override
    protected void onResume() {
        super.onResume();
        // NFCの読み込みを有効化
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
//        setButtonListener();

    }


    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onNewIntent(Intent intent) {

        // IntentにTagの基本データが入ってくるので取得
        super.onNewIntent(intent);
        setButtonListener();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }

        NFCReader card = new NFCReader(tag);
        //カードの種類を判別
        int type = card.getCardType();


        //userData.search(card.getIDm(""));
        //既知のカードか否か、保存するか否かのフラグを設定
        //更新or新規保存orなにもしないの3択

        int cardDataFlag = 0;
        CardData newCardData = userDataStorage.getCardData(card.getIDm(""));
        if(newCardData!=null){
            cardDataFlag = 1; //更新
        }else if(true){//確認
                cardDataFlag = 2; //新規保存
        }
        Log.d("flag",cardDataFlag+"");

        ArrayList<CardHistory> histories = new ArrayList<>();

        final String cardIDText = String.format("Felica IDm：%s",card.getIDm(" "));
        String cardBalanceText = "";
        String cardNameText = "非対応カード";

        switch(type){
            case 1:
                //Ayuca
                ayuca = new Ayuca(tag);
                //バス停コード一覧ファイルを読み込み
                ayuca.loadAssetFile((Activity)findViewById(R.id.fragment_show).getContext());
                //カードからデータを読み取り
                ayuca.readAllData();
                //カードの履歴を取得
                histories = ayuca.getHistories();

                //画面表示テキスト
                cardNameText = "Ayuca";
                cardBalanceText = String.format("￥%,d",ayuca.getSFBalance());

                switch (cardDataFlag){
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
                if(newCardData!=null){
                    findViewById(R.id.myCard_monthly_empty_message).setVisibility(View.GONE);
                    findViewById(R.id.myCard_empty_message).setVisibility(View.GONE);
                    View myCard = findViewById(R.id.myCard_1);
                    View muCardMonthly = findViewById(R.id.myCard_monthly_1);
                    newCardData.setMyPageInfo(myCard,muCardMonthly);
                    myPageIDmList[0] = ayuca.getIDm("");
                }
                break;

            case 2:
                //CampusPay
                campusPay = new CampusPay(tag);
                //カードからデータを読み取り
                campusPay.readAllData();
                histories = campusPay.getHistories();

                cardNameText = "生協電子マネー";
                cardBalanceText = String.format("￥%,d",campusPay.getSFBalance());

                switch (cardDataFlag){
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
                if(newCardData!=null){
                    findViewById(R.id.myCard_monthly_empty_message).setVisibility(View.GONE);
                    findViewById(R.id.myCard_empty_message).setVisibility(View.GONE);
                    View myCard = findViewById(R.id.myCard_2);
                    View muCardMonthly = findViewById(R.id.myCard_monthly_2);
                    newCardData.setMyPageInfo(myCard,muCardMonthly);
                    myPageIDmList[1] = campusPay.getIDm("");
                }
                break;
            case 3:
                //学生証
                idCard = new StudentIDCard(tag);
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
        if(newCardData!=null){
            showHistory(type,newCardData.getHistories());
        }

        userDataStorage.printList();
        updateMyPage();
//        userDataStorage.reset();
        saveUserDataFile();
//        タブ切り替え
//        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
        saveUserDataFile();
    }


    private void showHistory(int type,ArrayList<CardHistory> histories){
        Context context = getApplicationContext();
        TextView name_start,name_end,price,bonusPoint,point,date,usedPoint;
        ImageView line;
        historyWrapper = new View[40];
        historyUI = new HistoryUI[40];

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

        for(int i = 0; i< historyWrapper.length; i++){
            name_start = historyWrapper[i].findViewById(R.id.name_start);
            name_end = historyWrapper[i].findViewById(R.id.name_end);
            price = historyWrapper[i].findViewById(R.id.price);
            date = historyWrapper[i].findViewById(R.id.date);
            point = historyWrapper[i].findViewById(R.id.point);
            bonusPoint = historyWrapper[i].findViewById(R.id.bonusPoint);
            usedPoint = historyWrapper[i].findViewById(R.id.usedPoint);
            line = historyWrapper[i].findViewById(R.id.line);
            if(type==1 && i>=CardParams.MAX_HISTORY_ITEMS_AYUCA){
                price.setTextColor(Color.rgb(0xBE,0xBE,0xBE));
            }else if(type==2 && i>=CardParams.MAX_HISTORY_ITEMS_CAMPUS_PAY){
                price.setTextColor(Color.rgb(0xBE,0xBE,0xBE));
            }else{
                price.setTextColor(Color.rgb(0x81,0xD4,0xFA));
            }
            historyUI[i] = new HistoryUI(name_start,name_end,price,date,point,bonusPoint,usedPoint,
                    line, historyWrapper[i],context);
        }

        for(int i=0;i<historyUI.length;i++){
            if(i<histories.size()){
                historyUI[i].setText(histories.get(i),type);
                historyWrapper[i].setVisibility(View.VISIBLE);
            }else{
                historyWrapper[i].setVisibility(View.GONE);
            }
        }
        //境界部のメッセージ表示切り替え
        if(type==1 && histories.size()>CardParams.MAX_HISTORY_ITEMS_AYUCA){
            findViewById(R.id.saved_data_massage_20).setVisibility(View.VISIBLE);
            findViewById(R.id.saved_data_massage_10).setVisibility(View.GONE);
        }else if(type==2 && histories.size()>CardParams.MAX_HISTORY_ITEMS_CAMPUS_PAY){
            findViewById(R.id.saved_data_massage_20).setVisibility(View.GONE);
            findViewById(R.id.saved_data_massage_10).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.saved_data_massage_20).setVisibility(View.GONE);
            findViewById(R.id.saved_data_massage_10).setVisibility(View.GONE);
        }
    }

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

    // ファイルを保存
    private void saveUserDataFile(){
        Gson gson = new Gson();
        String jsonData = gson.toJson(userDataStorage);
            //既存内容を上書き
            try (FileWriter writer = new FileWriter(userDataFile,false)) {
                writer.write(jsonData);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

    }
    @Override
    public void onRestart() {
        super.onRestart();
        setButtonListener();
        updateMyPage();
    }
    Context a = this;
    private  void setButtonListener(){
        findViewById(R.id.myCard_1).findViewById(R.id.delete_button).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(a);
                    builder
                            .setTitle("データを削除しますか？") // タイトル
                            .setMessage("この操作は元に戻せません") // メッセージ
                            .setNegativeButton("削除", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    userDataStorage.delete(myPageIDmList[0]);
                                    updateMyPage();
                                    userDataStorage.printList();
                                }
                            })
                            .setPositiveButton("キャンセル", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            })
                            .create();
                    AlertDialog dialog = builder.create();
                    // AlertDialogを表示
                    dialog.show();

                }
            }
        );
        findViewById(R.id.myCard_2).findViewById(R.id.delete_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                        builder
                                .setTitle("データを削除しますか？") // タイトル
                                .setMessage("この操作は元に戻せません") // メッセージ
                                .setNegativeButton("削除", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        userDataStorage.delete(myPageIDmList[1]);
                                        updateMyPage();
                                        userDataStorage.printList();
                                    }
                                })
                                .setPositiveButton("キャンセル", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                })
                                .create();
                        AlertDialog dialog = builder.create();
                        // AlertDialogを表示
                        dialog.show();
                    }
                }
        );
        findViewById(R.id.myCard_3).findViewById(R.id.delete_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                        builder
                                .setTitle("データを削除しますか？") // タイトル
                                .setMessage("この操作は元に戻せません") // メッセージ
                                .setNegativeButton("削除", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        userDataStorage.delete(myPageIDmList[2]);
                                        updateMyPage();
                                        userDataStorage.printList();
                                    }
                                })
                                .setPositiveButton("キャンセル", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                })
                                .create();
                        AlertDialog dialog = builder.create();
                        // AlertDialogを表示
                        dialog.show();
                    }
                }
        );
        findViewById(R.id.myCard_4).findViewById(R.id.delete_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                        builder
                                .setTitle("データを削除しますか？") // タイトル
                                .setMessage("この操作は元に戻せません") // メッセージ
                                .setNegativeButton("削除", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        userDataStorage.delete(myPageIDmList[3]);
                                        updateMyPage();
                                        userDataStorage.printList();
                                    }
                                })
                                .setPositiveButton("キャンセル", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                })
                                .create();
                        AlertDialog dialog = builder.create();
                        // AlertDialogを表示
                        dialog.show();
                    }
                }
        );
        findViewById(R.id.myCard_5).findViewById(R.id.delete_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                        builder
                                .setTitle("データを削除しますか？") // タイトル
                                .setMessage("この操作は元に戻せません") // メッセージ
                                .setNegativeButton("削除", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        userDataStorage.delete(myPageIDmList[4]);
                                        updateMyPage();
                                        userDataStorage.printList();
                                    }
                                })
                                .setPositiveButton("キャンセル", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                })
                                .create();
                        AlertDialog dialog = builder.create();
                        // AlertDialogを表示
                        dialog.show();
                    }
                }
        );
    }

    private void updateMyPage(){
        ArrayList<CardData> cards= userDataStorage.getAllData();
        View[] myCards = new View[5];
        View[] myCardsMonthly = new View[5];
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
        for(View myCard : myCards){
            myCard.setVisibility(View.GONE);
        }
        for(View myCardMonthly : myCardsMonthly){
            myCardMonthly.setVisibility(View.GONE);
        }
        findViewById(R.id.myCard_monthly_empty_message).setVisibility(View.VISIBLE);
        findViewById(R.id.myCard_empty_message).setVisibility(View.VISIBLE);
        if(cards.size()>0){
            findViewById(R.id.myCard_monthly_empty_message).setVisibility(View.GONE);
            findViewById(R.id.myCard_empty_message).setVisibility(View.GONE);
            for(int i=0;i<cards.size();i++){
                switch (cards.get(i).getCardType()){
                    case 1:
                        cards.get(i).setMyPageInfo(myCards[0],myCardsMonthly[0]);
                        myPageIDmList[0] = cards.get(i).getIDm();
                        break;
                    case 2:
                        cards.get(i).setMyPageInfo(myCards[1],myCardsMonthly[1]);
                        myPageIDmList[1] = cards.get(i).getIDm();
                        break;
                }

            }
        }
        showGraph(userDataStorage);


    }

    void showGraph(Storage userDataStorage){
        View[] chartWrapper = new View[2];
        chartWrapper[0] = findViewById(R.id.history_chart1);
        chartWrapper[1] = findViewById(R.id.history_chart2);

        TextView title;
        HorizontalBarChart chart;
        int count = Math.min(chartWrapper.length,userDataStorage.getAllData().size());
        for (int i=0;i<count;i++){
            title = chartWrapper[i].findViewById(R.id.chart_title);
            chart = chartWrapper[i].findViewById(R.id.usage_chart);
            setChartParams(chart,userDataStorage.getAllData().get(i));
            title.setText(userDataStorage.getAllData().get(i).getCardName()+" 月額利用履歴");
        }

    }

    @SuppressLint("DefaultLocale")
    private void setChartParams(HorizontalBarChart chart, CardData cardData){
        ArrayList<BarEntry> entries = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int chartItemNum = 5;
        calendar.setTime(new Date());
        ArrayList<Calendar> date = new ArrayList<>();
        ArrayList<String> xValues = new ArrayList<>();

        for(int i=0;i<chartItemNum;i++){
            date.add((Calendar) calendar.clone());
            calendar.add(Calendar.MONTH, -1);
        }
        Collections.reverse(date);

        for(int i=0;i<chartItemNum;i++){
            int value = cardData.getMonthlyUsage(date.get(i).get(Calendar.YEAR),date.get(i).get(Calendar.MONTH)+1);
            entries.add(new BarEntry((float)i ,value));
            Log.d("chart",i+":"+value+","+date.get(i).get(Calendar.YEAR)+"/"+date.get(i).get(Calendar.MONTH)+1);
        }


        for(int i=0;i<chartItemNum;i++){
            xValues.add(String.format("%d月",date.get(i).get(Calendar.MONTH)+1));
        }

        ArrayList<IBarDataSet> bars = new ArrayList<>();
        BarDataSet dataSet = new BarDataSet(entries, cardData.getCardName());

        //ハイライトさせない
        dataSet.setHighlightEnabled(false);
        switch (cardData.getCardType()){
            case CardParams.TYPE_CODE_AYUCA:
                break;
            case CardParams.TYPE_CODE_CAMPUS_PAY:
                dataSet.setColor(Color.rgb(0xFF,0x91,0x01));
                break;
        }

        bars.add(dataSet);
        //Y軸に表示するLabelのリスト
        final String[] labels = new String[chartItemNum+1];
//        labels[0] = "";
        for(int i=0;i<chartItemNum;i++){
            labels[i] = String.format("%d月",date.get(i).get(Calendar.MONTH)+1);
        }
        for(int i=0;i<labels.length;i++){
            Log.d("TAG","Label:"+labels[i]);
        }
        BarData data = new BarData(bars);
        data.setBarWidth(0.7f);
        data.setValueTextSize(12f);


        data.setValueFormatter(new IndexAxisValueFormatter(){
            @Override
            public String getFormattedValue(float value) {
                if(value<=0){
                    return "利用なし";
                }else{
                    return String.format("%,d",(int)value);
                }

            }
        });
        chart.setData(data);

        //Y軸(左)
        XAxis xl = chart.getXAxis();
        xl.setEnabled(true);
        xl.setLabelCount(5);

        xl.setValueFormatter(new IndexAxisValueFormatter(labels));
        xl.setDrawAxisLine(true);
        xl.setDrawLabels(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTextSize(12f);
//        xl.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        xl.setDrawGridLines(false);
        xl.setDrawAxisLine(true);

        YAxis yAxis = chart.getAxisRight();
        yAxis.setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
//        //X軸
//        XAxis xAxis = chart.getXAxis();
//        xAxis.setDrawLabels(false);
//        xAxis.setDrawGridLines(false);


        //グラフ上の表示
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.setClickable(false);
        chart.getLegend().setEnabled(false);
        chart.invalidate();
        chart.animateY(500);

        Log.d("TAG","fin");
    }

}

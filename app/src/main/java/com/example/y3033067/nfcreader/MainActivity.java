package com.example.y3033067.nfcreader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


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
    View[] historyView;

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

        // IntentにTagの基本データが入ってくるので取得
        super.onNewIntent(intent);

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
        historyUI =  getHistoryUI();//履歴表示のUI部品を一括で取得
        for(int i=0;i<historyUI.length;i++){
            if(i<histories.size()){
                historyUI[i].setText(histories.get(i),type);
                historyView[i].setVisibility(View.VISIBLE);
            }else{
                historyView[i].setVisibility(View.GONE);
            }
        }
        userDataStorage.printList();
//        userDataStorage.reset();


        saveUserDataFile();
//        タブ切り替え
//        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    private HistoryUI[] getHistoryUI(){
        Context context = getApplicationContext();
        TextView name_start,name_end,price,bonusPoint,point,date,usedPoint;
        ImageView line;
        View wrapper;
        historyView = new View[20];
        historyUI = new HistoryUI[20];

        historyView[0] = findViewById(R.id.history_1);
        historyView[1] = findViewById(R.id.history_2);
        historyView[2] = findViewById(R.id.history_3);
        historyView[3] = findViewById(R.id.history_4);
        historyView[4] = findViewById(R.id.history_5);
        historyView[5] = findViewById(R.id.history_6);
        historyView[6] = findViewById(R.id.history_7);
        historyView[7] = findViewById(R.id.history_8);
        historyView[8] = findViewById(R.id.history_9);
        historyView[9] = findViewById(R.id.history_10);
        historyView[10] = findViewById(R.id.history_11);
        historyView[11] = findViewById(R.id.history_12);
        historyView[12] = findViewById(R.id.history_13);
        historyView[13] = findViewById(R.id.history_14);
        historyView[14] = findViewById(R.id.history_15);
        historyView[15] = findViewById(R.id.history_16);
        historyView[16] = findViewById(R.id.history_17);
        historyView[17] = findViewById(R.id.history_18);
        historyView[18] = findViewById(R.id.history_19);
        historyView[19] = findViewById(R.id.history_20);

        for(int i=0;i<historyView.length;i++){
            name_start = historyView[i].findViewById(R.id.name_start);
            name_end = historyView[i].findViewById(R.id.name_end);
            price = historyView[i].findViewById(R.id.price);
            date = historyView[i].findViewById(R.id.date);
            point = historyView[i].findViewById(R.id.point);
            bonusPoint = historyView[i].findViewById(R.id.bonusPoint);
            usedPoint = historyView[i].findViewById(R.id.usedPoint);
            line = historyView[i].findViewById(R.id.line);
            historyUI[i] = new HistoryUI(name_start,name_end,price,date,point,bonusPoint,usedPoint,
                    line,historyView[i],context);
        }
        return historyUI;
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
}

package com.example.y3033067.nfcreader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private Ayuca ayuca;
    private CampusPay campusPay;
    private StudentIDCard idCard;
    TabLayout tabLayout;
    TextView cardID,cardLog,cardName,cardBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcreader);
        // xmlからTabLayoutの取得
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // xmlからViewPagerを取得
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

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
        Log.d("TAG", "RUN");

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
        Log.d("TAG","Load");
        ImageView image = findViewById(R.id.reader_ring);
        Drawable ringLoading = getResources().getDrawable(R.drawable.shape_ring_loading);
        image.setImageDrawable(ringLoading);
        // IntentにTagの基本データが入ってくるので取得
        super.onNewIntent(intent);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }
        //参考にしたコードから追加した部分はここから
        NFCReader card = new NFCReader(tag);
        int type = card.getCardType();
        Log.d("TAG", "Type:"+type);
        ArrayList<CardHistory> ch = new ArrayList<>();
        String displayText = "";
        cardID = findViewById(R.id.card_id);
        cardLog = findViewById(R.id.log);
        cardName = findViewById(R.id.card_name);
        cardBalance = findViewById(R.id.card_balance);
        cardName.setText("非対応カード");
        cardBalance.setText("");
        cardID.setText(String.format("Felica IDm：%s",card.getIDm(" ")));

        switch(type){
            case 1:
                //Ayuca
                Log.d("TAG", "Ayuca");
                cardName.setText("Ayuca");
                ayuca = new Ayuca(tag);
                ayuca.loadAssetFile((Activity)findViewById(R.id.fragment_read).getContext());
                //カードからデータを読み取り
                ayuca.readAllData();
                ch = ayuca.getHistories();

                cardBalance.setText(String.format("残高￥%d",ayuca.getSFBalance()));
                Log.d("TAG","残高：￥"+(ayuca.getSFBalance()));
                break;
            case 2:
                //CampusPay
                Log.d("TAG", "CampusPay");
                cardName.setText("生協電子マネー");

                campusPay = new CampusPay(tag);
                //カードからデータを読み取り
                campusPay.readAllData();
                ch = campusPay.getHistories();

                cardBalance.setText(String.format("残高￥%d",campusPay.getSFBalance()));
                Log.d("TAG","残高：￥"+(campusPay.getSFBalance()));
                break;
            case 3:
                //学生証
                cardName.setText("岐阜大学学生証");
                Log.d("TAG", "学生証");
                idCard = new StudentIDCard(tag);
                //カードからデータを読み取り
                idCard.readAllData();

                displayText = idCard.getStudentID();
                break;
        }


        //ここまで
        //============================================================
        for(int i=0;i<ch.size();i++){
            displayText += ("＝＝＝＝＝＝＝＝＝＝"+"\n");
            displayText += ((i+1)+"件目"+ch.get(i).getDate()+"\n");
            displayText += (""+ch.get(i).getDevice()+ch.get(i).getType()+"\n");
            displayText += ("金額￥"+ch.get(i).getPrice()+"\n");
            displayText += ("残高￥"+ch.get(i).getBalance()+"\n");
            if(type==1 && ch.get(i).getTypeFlag()==0x03){
                displayText += ("乗車区間："+ch.get(i).getStart()+" → "+ch.get(i).getEnd()+"\n");

            }
            if(ch.get(i).getPointFlag()){
                displayText += ("通常ポイント付与："+(double)ch.get(i).getGrantedNormalPoint()/10+"\n");
                if(ch.get(i).getGrantedBonusPoint()>0){
                    displayText += ("ボーナスポイント付与："+(double)ch.get(i).getGrantedBonusPoint()/10+"\n");
                }
                if(ch.get(i).getUsedPoint()<0){
                    displayText += ("ポイント利用："+(double)ch.get(i).getUsedPoint()/10+"\n");
                }
                displayText += ("ポイント残高："+(double)ch.get(i).getPointBalance()/10+"\n");
            }

        }
        cardLog.setText(displayText);


        Drawable ringWaiting = getResources().getDrawable(R.drawable.shape_ring_waiting);
//        image.setImageDrawable(ringWaiting);

//        タブ切り替え
        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }


}
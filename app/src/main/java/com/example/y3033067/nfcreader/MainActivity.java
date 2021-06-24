package com.example.y3033067.nfcreader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private Ayuca ayuca;
    private CampusPay campusPay;
    private StudentIDCard idCard;
    ArrayList<Byte[]> historyData,cardInfo,cardBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcreader);

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndef};

        // FelicaはNFC-TypeFなのでNfcFのみ指定でOK
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onNewIntent(Intent intent) {
        // IntentにTagの基本データが入ってくるので取得
        super.onNewIntent(intent);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }
        //============================================================
        //参考にしたコードから追加した部分はここから
        NFCReader card = new NFCReader(tag);
        int type = card.getCardType();
        Log.d("TAG", "Type:"+type);

        switch(type){
            case 1:
                //Ayuca
                Log.d("TAG", "Ayuca");
                ayuca = new Ayuca(tag);
                //カードからデータを読み取り
                historyData = ayuca.getHistory();
                cardInfo = ayuca.getCardInfo();
                cardBalance = ayuca.getBalance();
                ayuca.parseHistory();
                for(int i = 0; i< cardBalance.size(); i++){
                    Log.d("TAG", String.format("<%02X> ",i)+card.hex2string(cardBalance.get(i)));
                }
                break;
            case 2:
                //CampusPay
                Log.d("TAG", "CampusPay");
                campusPay = new CampusPay(tag);
                //カードからデータを読み取り
                historyData = campusPay.getHistory();
                cardInfo = campusPay.getCardInfo();
                campusPay.parseHistory();
                break;
            case 3:
                //学生証
                Log.d("TAG", "学生証");
                idCard = new StudentIDCard(tag);
                //カードからデータを読み取り
                cardInfo = idCard.getID();
                historyData = new ArrayList<>();
                break;


        }
        //読み取りが失敗した場合nullが返る
        if(historyData ==null){
            Log.e("","Loaded data null/history");
            return;
        }
        if(cardInfo ==null){
            Log.e("","Loaded data null/cardInfo");
            return;
        }
//        取得したデータをログに表示
        Log.d("TAG", "カード情報");
        for(int i = 0; i< cardInfo.size(); i++){
            Log.d("TAG", String.format("<%02X> ",i)+card.hex2string(cardInfo.get(i)));
        }
        Log.d("TAG", "利用履歴");
        for(int i = 0; i< historyData.size(); i++){
            Log.d("TAG", String.format("<%02X> ",i)+card.hex2string(historyData.get(i)));
        }
        Log.d("TAG", "================");

        //ここまで
        //============================================================


    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }


}
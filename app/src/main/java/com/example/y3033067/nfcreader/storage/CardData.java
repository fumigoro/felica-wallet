package com.example.y3033067.nfcreader.storage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.ScrollView;

import com.example.y3033067.nfcreader.CardHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CardData {
    private final String card_name;
    private final String card_type;
    private String balance;
    private String point;
    private final String IDm;
    private String last_modified;
    private final ArrayList<CardHistory> histories;
    private final String card_nickname;
//    private int initialFlag;

    public CardData(String _card_name, String _card_nickname,String _card_type, String _balance, String _point,
                    String _IDm, ArrayList<CardHistory> _histories){
        card_name = _card_name;
        card_nickname = _card_nickname;
        card_type = _card_type;
        balance = _balance;
        point = _point;
        IDm = _IDm;
        last_modified = getFormatDate(new Date());
        histories = (ArrayList<CardHistory>)_histories.clone();
    }

    public void update(int _balance,int _point,ArrayList<CardHistory> newHistories){
        balance = String.valueOf(_balance);
        point = String.valueOf(_point);
        for(int i=0;i<newHistories.size();i++){
            //保存済みの履歴より新しい物があれば追加
            if(histories.get(0).getDate().after(newHistories.get(i).getDate())){
                histories.add(0,newHistories.get(i));
            }
        }
    }

    private String getFormatDate(Date date){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d H:mm");
        return dateFormat.format(date);
    }

    public int getMonthlyUsage(int year,int month){
        Date monthStart = new Date(year-1900,month-1,1);
        Date monthEnd = new Date(year-1900,month,1);
        int monthlyUsage = 0;
        for (CardHistory history : histories){
            Log.d("TAG",history.getDate()+":"+monthStart+","+monthEnd);
            if(history.getDate().after(monthStart) && history.getDate().before(monthEnd)){
                Log.d("TAG",history.getSfUsedPrice()+"");
                monthlyUsage += history.getSfUsedPrice();
            }
        }
        return monthlyUsage;
    }



}

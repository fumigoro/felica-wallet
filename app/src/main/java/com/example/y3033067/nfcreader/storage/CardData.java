package com.example.y3033067.nfcreader.storage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.y3033067.nfcreader.CardHistory;
import com.example.y3033067.nfcreader.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CardData {
    private final String card_name;
    private final String card_type;
    private int balance;
    private int point;
    private final String IDm;
    private String last_modified;
    private final ArrayList<CardHistory> histories;
    private final String card_nickname;

    public CardData(String _card_name, String _card_nickname,String _card_type, int _balance, int _point,
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
        balance = _balance;
        point = _point;
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
        int monthlyUsage = 0;
        //今月の取引分の履歴を抽出
        ArrayList<CardHistory> thisMonthHistory = getMonthlyHistory(year,month);
        for (CardHistory history : thisMonthHistory){
            monthlyUsage += history.getSfUsedPrice();
        }
        return monthlyUsage;
    }

    public ArrayList<CardHistory> getMonthlyHistory(int year,int month){
        Date monthStart = new Date(year-1900,month-1,1);
        Date monthEnd = new Date(year-1900,month,1);
        ArrayList<CardHistory> result = new ArrayList<>();
        for (CardHistory history : histories){
            if(history.getDate().after(monthStart) && history.getDate().before(monthEnd)){
                result.add(history);
            }
        }
        return result;
    }

    public int getMonthlyPayCount(int year ,int month){
        int monthlyCount = 0;
        //今月の取引分の履歴を抽出
        ArrayList<CardHistory> thisMonthHistory = getMonthlyHistory(year,month);
        for (CardHistory history : thisMonthHistory){
            if(history.getType().equals("決済")){
                monthlyCount += 1;
            }
        }
        return monthlyCount;
    }

    public int getMonthlyPayCount(){
        Date today = new Date();
        return getMonthlyPayCount(today.getYear()+1900,today.getMonth()+1);
    }
    public int getMonthlyUsage(){
        Date today = new Date();
        return getMonthlyUsage(today.getYear()+1900,today.getMonth()+1);
    }

    @SuppressLint("DefaultLocale")
    public void setMyPageInfo(View myCard, View myCardMonthly){
        myCard.setVisibility(View.VISIBLE);
        myCardMonthly.setVisibility(View.VISIBLE);

        TextView balanceText = myCard.findViewById(R.id.card_sum_balance);
        balanceText.setText(String.format("残高 ￥%,d / %.1fP",balance,point/10.0));

        TextView lastModified = myCard.findViewById(R.id.last_read);
        lastModified.setText(String.format("最終読取 %s",last_modified));

        TextView mViewCardName = myCardMonthly.findViewById(R.id.mView_card_name);
        mViewCardName.setText(card_name);

        TextView mViewPrice = myCardMonthly.findViewById(R.id.mView_price);
        mViewPrice.setText(String.format("￥%,d",getMonthlyUsage()));

        TextView mViewCardCount = myCardMonthly.findViewById(R.id.mView_payCount);
        mViewCardCount.setText(String.format("%d回の支払い",getMonthlyPayCount()));

    }

}

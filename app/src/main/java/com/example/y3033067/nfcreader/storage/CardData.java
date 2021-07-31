package com.example.y3033067.nfcreader.storage;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.y3033067.nfcreader.card.CardHistory;
import com.example.y3033067.nfcreader.card.CardParams;
import com.example.y3033067.nfcreader.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 読み取ったカードのデータを保持するクラス
 * カード1枚分で1インスタンスとなる
 */
public class CardData {
    private final String card_name;
    private final int card_type;
    private int balance;
    private int point;
    private final String IDm;
    private String last_modified;
    private final ArrayList<CardHistory> histories;
    private final String card_nickname;

    public CardData(String _card_name, String _card_nickname, int _card_type, int _balance, int _point,
                    String _IDm, ArrayList<CardHistory> _histories) {
        card_name = _card_name;
        card_nickname = _card_nickname;
        card_type = _card_type;
        balance = _balance;
        point = _point;
        IDm = _IDm;

        last_modified = getFormatDate(new Date());
        histories = (ArrayList<CardHistory>) _histories.clone();
    }

    public void update(int _balance, int _point, ArrayList<CardHistory> newHistories) {
        balance = _balance;
        point = _point;
        last_modified = getFormatDate(new Date());
        CardHistory latest = histories.get(0);
        for (int i = newHistories.size() - 1; i >= 0; i--) {
            //保存済みの履歴より新しい物があれば先頭へ追加
            if (newHistories.get(i).getDate().after(latest.getDate())) {
                histories.add(0, newHistories.get(i));
            }
        }
    }

    /**
     * Dateオブジェクトを渡すと指定した日付形式で文字列化する
     * @param date Dateオブジェクト
     * @return 日付文字列
     */
    private String getFormatDate(Date date) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d H:mm");
        return dateFormat.format(date);
    }

    /**
     * 指定した年月の月額利用額を返す
     * @param year 年
     * @param month 月
     * @return 月額利用額
     */
    public int getMonthlyUsage(int year, int month) {
        int monthlyUsage = 0;
        //指定月の利用金額を合計する
        ArrayList<CardHistory> thisMonthHistory = getMonthlyHistory(year, month);
        for (CardHistory history : thisMonthHistory) {
            monthlyUsage += history.getSfUsedPrice();
        }
        return monthlyUsage;
    }

    /**
     * 指定した年月の利用履歴だけを絞り込んで返す
     * @param year 年
     * @param month 月
     * @return 利用履歴リスト
     */
    private ArrayList<CardHistory> getMonthlyHistory(int year, int month) {
        Date monthStart = new Date(year - 1900, month - 1, 1);
        Date monthEnd = new Date(year - 1900, month, 1);
        ArrayList<CardHistory> result = new ArrayList<>();
        for (CardHistory history : histories) {
            if (history.getDate().after(monthStart) && history.getDate().before(monthEnd)) {
                result.add(history);
            }
        }
        return result;
    }

    /**
     * 指定した年月の支払回数を返す
     * @param year 年
     * @param month 月
     * @return 支払回数
     */
    public int getMonthlyPayCount(int year, int month) {
        int monthlyCount = 0;
        //今月の取引分の履歴を抽出
        ArrayList<CardHistory> thisMonthHistory = getMonthlyHistory(year, month);
        for (CardHistory history : thisMonthHistory) {
            if (history.getType().equals("決済")) {
                monthlyCount += 1;
            }
        }
        return monthlyCount;
    }

    /**
     * 今月の支払回数を返す
     * @return 支払回数
     */
    public int getMonthlyPayCount() {
        Date today = new Date();
        return getMonthlyPayCount(today.getYear() + 1900, today.getMonth() + 1);
    }

    /**
     * 今月の利用履歴だけを絞り込んで返す
     * @return 利用履歴リスト
     */
    public int getMonthlyUsage() {
        Date today = new Date();
        return getMonthlyUsage(today.getYear() + 1900, today.getMonth() + 1);
    }

    /**
     * マイページのUI上にデータを表示する
     * @param myCard 表示するView
     * @param myCardMonthly  表示するView
     */
    @SuppressLint("DefaultLocale")
    public void setMyPageInfo(View myCard, View myCardMonthly) {
        //表示部を見えるように
        myCard.setVisibility(View.VISIBLE);
        myCardMonthly.setVisibility(View.VISIBLE);

        //登録済みカーのド名称
        TextView sumCardName = myCard.findViewById(R.id.sum_card_name);
        sumCardName.setText(card_name);

        //残高
        TextView balanceText = myCard.findViewById(R.id.card_sum_balance);
        balanceText.setText(String.format("残高 ￥%,d / %.1fP", balance, point / 10.0));
        //最終読み取り日
        TextView lastModified = myCard.findViewById(R.id.last_read);
        lastModified.setText(String.format("最終読取 %s", last_modified));

        //今月の支払額のカード名称
        TextView mViewCardName = myCardMonthly.findViewById(R.id.mView_card_name);
        mViewCardName.setText(card_name);

        //今月の支払額
        TextView mViewPrice = myCardMonthly.findViewById(R.id.mView_price);
        mViewPrice.setText(String.format("￥%,d", getMonthlyUsage()));

        //今月の支払額回数
        TextView mViewCardCount = myCardMonthly.findViewById(R.id.mView_payCount);
        mViewCardCount.setText(String.format("%d回の支払い", getMonthlyPayCount()));

        //カードのアイコン画像
        ImageView cardImage = myCard.findViewById(R.id.card_sum_image);
        switch (getCardType()) {
            case CardParams.TYPE_CODE_AYUCA:
                cardImage.setImageResource(R.drawable.shape_ayuca);
                Log.d("UI", "ayuca" + card_type);
                break;
            case CardParams.TYPE_CODE_CAMPUS_PAY:
                cardImage.setImageResource(R.drawable.shape_campuspay);
                Log.d("UI", "cp" + card_type);
                break;
        }


    }

    public String getIDm() {
        return IDm;
    }

    public int getCardType() {
        return card_type;
    }

    public ArrayList<CardHistory> getHistories() {
        return histories;
    }

    public String getCardName() {
        return card_name;
    }

}

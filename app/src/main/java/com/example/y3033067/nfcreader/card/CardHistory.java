package com.example.y3033067.nfcreader.card;

import java.util.Date;

/**
 * 利用履歴1件分の様々なデータを保持するクラス
 * メソッドは単純なgetterとsetterのみのため個別のコメントは省略
 */
public class CardHistory {
    private Date date;//発生日時
    private int price;//金額
    private int balance;//処理後の残高
    private String type;//履歴の種類(決済,チャージなど)
    private int typeFlag;//履歴の種類のフラグ（生データ）
    private String discount;//乗継割引の情報(Ayuca用)
    private String device;//処理を行った装置情報
    private String start;//乗車した駅・停留所
    private String end;//降車した駅・停留所

    private Boolean pointFlag;//ポイント関係のデータを扱うか否かのフラグ
    private int pointBalance;//ポイント残高
    private int grantedNormalPoint;//付与された通常ポイント
    private int grantedBonusPoint;//付与されたボーナスポイント
    private int usedPoint;//使用した（還元された）ポイント
    private int sfUsedPrice;//現金チャージ分から支払った金額

    public CardHistory() {
        pointFlag = false;
    }

    public CardHistory(Date _date, int _price, int _balance, String _type, int _typeFlag, String _device) {
        date = _date;
        price = _price;
        pointBalance = 0;
        balance = _balance;
        type = _type;
        typeFlag = _typeFlag;
        discount = "";
        device = _device;
        start = "";
        end = "";
        sfUsedPrice = price;
        pointFlag = false;
    }


    public void setAllParams(Date _date, int _price, int _sfUsedPrice, int _pointBalance, int _balance, String _type, int _typeFlag, String _discount, String _device, String _start, String _end) {
        date = _date;
        price = _price;
        sfUsedPrice = _sfUsedPrice;
        pointBalance = _pointBalance;
        balance = _balance;
        type = _type;
        typeFlag = _typeFlag;
        discount = _discount;
        device = _device;
        start = _start;
        end = _end;
    }

    public void setPointFlag(Boolean _pointFlag) {
        pointFlag = _pointFlag;
    }

    public void setPointParams(int _grantedNormalPoint, int _grantedBonusPoint, int _usedPoint) {
        grantedNormalPoint = _grantedNormalPoint;
        grantedBonusPoint = _grantedBonusPoint;
        usedPoint = _usedPoint;
    }

    public Date getDate() {
        return date;
    }

    public int getPrice() {
        return price;
    }

    public int getPointBalance() {
        return pointBalance;
    }

    public int getBalance() {
        return balance;
    }

    public String getType() {
        return type;
    }

    public String getDevice() {
        return device;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public int getTypeFlag() {
        return typeFlag;
    }

    public Boolean getPointFlag() {
        return pointFlag;
    }

    public int getGrantedNormalPoint() {
        return grantedNormalPoint;
    }

    public int getGrantedBonusPoint() {
        return grantedBonusPoint;
    }

    public int getUsedPoint() {
        return usedPoint;
    }

    public int getSfUsedPrice() {
        return sfUsedPrice;
    }

}

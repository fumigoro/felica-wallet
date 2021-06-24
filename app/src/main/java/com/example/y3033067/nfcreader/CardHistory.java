package com.example.y3033067.nfcreader;

import java.util.Date;

public class CardHistory {
    private Date date;//発生日時
    private int price;//金額
    private int pointBalance;//ポイント残高
    private int point;//特別なポイントの増減
    private int balance;//処理後の残高
    private String type;//履歴の種類(決済,チャージなど)
    private int typeFlag;//履歴の種類のフラグ（生データ）
    private String discount;//乗継割引当の情報(Ayuca用)
    private String device;//処理を行った装置情報
    private String start;//乗車した駅・停留所
    private String end;//降車した駅・停留所
    private Boolean pointFlag;//ポイント関係のデータを扱うか否かのフラグ
    private int additionalPoint;

    public CardHistory() {
        pointFlag = false;
    }

    public CardHistory(Date _date, int _price, int _pointBalance, int _balance, String _type, int _typeFlag, String _discount, String _device, String _start, String _end) {
        date = _date;
        price = _price;
        pointBalance = _pointBalance;
        balance = _balance;
        type = _type;
        typeFlag = _typeFlag;
        discount = _discount;
        device = _device;
        start = _start;
        end = _end;
        pointFlag = false;
    }


    public void setAllParams(Date _date, int _price, int _pointBalance, int _balance, String _type, int _typeFlag, String _discount, String _device, String _start, String _end) {
        date = _date;
        price = _price;
        pointBalance = _pointBalance;
        balance = _balance;
        type = _type;
        typeFlag = _typeFlag;
        discount = _discount;
        device = _device;
        start = _start;
        end = _end;
    }


    public void setDate(Date _date) {
        date = _date;
    }

    public void setPrice(int _price) {
        price = _price;
    }

    public void setPointBalance(int _point) {
        pointBalance = _point;
    }

    public void setBalance(int _balance) {
        balance = _balance;
    }

    public void setType(String _type) {
        type = _type;
    }

    public void setDiscount(String _discount) {
        discount = _discount;
    }

    public void setDevice(String _device) {
        device = _device;
    }

    public void setStart(String _start) {
        start = _start;
    }

    public void setEnd(String _end) {
        end = _end;
    }

    public void setPointFlag(Boolean _pointFlag) {
        pointFlag = _pointFlag;
    }
    public void setPointParams(int _point,int _additionalPoint){
        point = _point;
        additionalPoint = _additionalPoint;
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

    public String getDiscount() {
        return discount;
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

    public int getTypeFlag(){
        return typeFlag;
    }

    public Boolean getPointFlag() {
        return pointFlag;
    }


}

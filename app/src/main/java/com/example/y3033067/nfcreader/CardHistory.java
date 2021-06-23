package com.example.y3033067.nfcreader;

import java.util.Date;

public class CardHistory {
    private Date date;//発生日時
    private int price;//金額
    private int point;//ポイント数
    private int balance;//処理後の残高
    private String type;//履歴の種類(決済,チャージなど)
    private String discount;//乗継割引当の情報(Ayuca用)
    private String device;//処理を行った装置情報
    private String start;//乗車した駅・停留所
    private String end;//降車した駅・停留所

    public CardHistory(){

    }
    public CardHistory(Date _date,int _price,int _point,int _balance,String _type,String _discount,String _device,String _start,String _end){
        date = _date;
        price = _price;
        point = _point;
        balance = _balance;
        type = _type;
        discount = _discount;
        device = _device;
        start = _start;
        end = _end;

    }
    public void setDate(Date _date){
        date = _date;
    }
    public void setPrice(int _price){
        price = _price;
    }
    public void setPoint(int _point){
        point = _point;
    }
    public void setBalance(int _balance){
        balance = _balance;
    }
    public void setType(String _type){
        type = _type;
    }
    public void setDiscount(String _discount){
        discount = _discount;
    }
    public void setDevice(String _device){
        device = _device;
    }
    public void setStart(String _start){
        start = _start;
    }
    public void setEnd(String _end){
        end = _end;
    }

    public Date getDate(){
        return date;
    }
    public int getPrice(){
        return price;
    }
    public int getPoint(){
        return point;
    }
    public int getBalance(){
        return balance;
    }
    public String getType(){
        return type;
    }
    public String getDiscount(){
        return discount;
    }
    public String getDevice(){
        return device;
    }
    public String getStart(){
        return start; }
    public String getEnd(){
        return end;
    }


}

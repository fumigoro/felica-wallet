package com.example.y3033067.nfcreader.storage;

import android.widget.ScrollView;

import com.example.y3033067.nfcreader.CardHistory;

import java.util.List;

public class CardData {
    public String card_name;
    public String card_type;
    public String balance;
    public String point;
    public String IDm;
    public String last_modified;
    public List<CardHistory> histories;

    public CardData(String _card_name, String _card_type, String _balance, String _point,
                    String _IDm, String _last_modified, List<CardHistory> _histories){
        card_name = _card_name;
        card_type = _card_type;
        balance = _balance;
        point = _point;
        IDm = _IDm;
        last_modified = _last_modified;
        histories = _histories;
    }
}

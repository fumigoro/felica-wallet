package com.example.y3033067.nfcreader.storage;

import java.util.ArrayList;

public class Storage {
    ArrayList<CardData> cards;
    public Storage(){
        cards = new ArrayList<CardData>();
    }
    public void addCard(CardData data){
        cards.add(data);
    }
}

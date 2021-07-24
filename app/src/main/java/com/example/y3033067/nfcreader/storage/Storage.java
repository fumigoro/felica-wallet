package com.example.y3033067.nfcreader.storage;

import android.util.Log;

import java.util.ArrayList;

public class Storage {
    ArrayList<CardData> cards;
    public Storage(){
        cards = new ArrayList<CardData>();
    }
    public void addCard(CardData data){
        cards.add(data);
    }
    public CardData getCardData(String IDm){
        for (CardData card :cards){
            if(card.getIDm().equals(IDm)){
                return card;
            }
        }
        return null;
    }
    public void printList(){
        for (CardData card :cards){
            Log.d("storage",card.getIDm());
        }
        Log.d("storage","==");
    }

    public void reset(){
        cards = new ArrayList<CardData>();
    }
}

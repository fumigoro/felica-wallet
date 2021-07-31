package com.example.y3033067.nfcreader.card;

import java.util.ArrayList;

public interface FelicaCard {
    public ArrayList<CardHistory> getHistories();
    public int getSFBalance();
    public int getPointBalance();
    public void readAllData();
}

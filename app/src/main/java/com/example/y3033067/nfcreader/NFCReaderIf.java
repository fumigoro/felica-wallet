package com.example.y3033067.nfcreader;

import android.util.Log;

import java.util.ArrayList;

public interface NFCReaderIf {
    public ArrayList<CardHistory> getHistories();
    public int getSFBalance();
    public int getPointBalance();
    public void readAllData();
}

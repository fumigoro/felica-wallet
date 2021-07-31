package com.example.y3033067.nfcreader.card;

import java.util.ArrayList;

/**
 * カードの読み取りのパラメーター指定や読み取ったバイナリの解釈を行うクラスに用いるインターフェイス
 * 必須のメソッドの実装を矯正するためだけに使用
 */
public interface FelicaCard {
    // 利用履歴を返す
    ArrayList<CardHistory> getHistories();

    // 現金残高を返す
    int getSFBalance();

    //ポイント残高を返す
    int getPointBalance();

    //全てのデータを読み取る
    void readAllData();
}

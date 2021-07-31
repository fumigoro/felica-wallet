package com.example.y3033067.nfcreader.storage;

import android.util.Log;

import java.util.ArrayList;

/**
 * 登録カードのCardDataオブジェクトをまとめて保持するクラス
 */
public class Storage {
    ArrayList<CardData> cards;

    public Storage() {
        cards = new ArrayList<CardData>();
    }

    /**
     * カードを新規追加する
     * @param data カードデータ
     */
    public void addCard(CardData data) {
        cards.add(data);
    }

    /**
     * 渡された引数に合致するIDｍを持つカードのデータを返す
     * @param IDm IDｍ
     * @return カードデータ
     */
    public CardData getCardData(String IDm) {
        for (CardData card : cards) {
            if (card.getIDm().equals(IDm)) {
                return card;
            }
        }
        return null;
    }

    /**
     * 現在保存されているカードの一覧をログ表示
     */
    public void printList() {
        for (CardData card : cards) {
            Log.d("storage", card.getIDm());
        }
        Log.d("storage", "==");
    }

    /**
     * 保存済みカードを全て削除
     */
    public void reset() {
        cards = new ArrayList<CardData>();
    }

    /**
     * IDｍを指定してカードデータを削除
     * @param IDm
     */
    public void delete(String IDm) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getIDm().equals(IDm)) {
                cards.remove(i);
                Log.d("TAG", "Deleted:" + IDm);
                break;
            }
        }
    }

    /**
     * 登録済みの全てのカードのデータを返す
     * @return 全カードデータ
     */
    public ArrayList<CardData> getAllCardData() {
        return cards;
    }
}

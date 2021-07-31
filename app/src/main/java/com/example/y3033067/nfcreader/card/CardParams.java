package com.example.y3033067.nfcreader.card;

/**
 * 対応カードに関する、読み取りに必要な各種パラメータをもつクラス
 */
public class CardParams {
    //システムコード
    public static final int SYSTEM_CODE_AYUCA = 0x83EE;
    public static final int SYSTEM_CODE_CAMPUS_PAY = 0xFE00;
    public static final int SYSTEM_CODE_STUDENT_ID = 0x0447;

    //アプリ内で独自に用いているカード種別フラグ
    public static final int TYPE_CODE_AYUCA = 1;
    public static final int TYPE_CODE_CAMPUS_PAY = 2;
    public static final int TYPE_CODE_STUDENT_ID = 3;

    //各カードの利用履歴保存件数
    public static final int MAX_HISTORY_ITEMS_AYUCA = 20;
    public static final int MAX_HISTORY_ITEMS_CAMPUS_PAY = 10;

}

package com.example.y3033067.nfcreader.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.y3033067.nfcreader.card.CardHistory;
import com.example.y3033067.nfcreader.card.CardParams;

import java.text.SimpleDateFormat;

/**
 * 履歴表示部の大量のUI部品に対しテキスト挿入と表示管理を行うクラス
 */
public class HistoryUI {
    private final TextView name_start, name_end, price, bonusPoint, point, usedPoint, date;
    private final ImageView line;
    private final View wrapper;
    private final Context context;

    public HistoryUI(TextView _name_start, TextView _name_end, TextView _price, TextView _date,
                     TextView _point, TextView _bonusPoint, TextView _usedPoint,
                     ImageView _line, View _wrapper, Context _context) {
        name_start = _name_start;
        name_end = _name_end;
        price = _price;
        date = _date;
        point = _point;
        bonusPoint = _bonusPoint;
        usedPoint = _usedPoint;
        line = _line;
        wrapper = _wrapper;
        context = _context;
    }

    /**
     * カード履歴を渡すと全ての履歴を表示する
     * @param ch カード履歴
     * @param cardType カード種別
     */
    @SuppressLint("DefaultLocale")
    public void setText(CardHistory ch, int cardType) {
        String name_startText = "";
        String name_endText = "";
        String priceText = "";
        String dateText = "";
        String pointText = "";
        String bonusPointText = "";
        String usedPointText = "";

        //全てのUIを可視化
        name_start.setVisibility(View.VISIBLE);
        name_end.setVisibility(View.GONE);
        price.setVisibility(View.VISIBLE);
        date.setVisibility(View.VISIBLE);
        point.setVisibility(View.VISIBLE);
        //これら2つのみデフォルト非表示
        bonusPoint.setVisibility(View.GONE);
        usedPoint.setVisibility(View.GONE);

        //UIの高さを動的に計算するためlayoutParamsを使用
        ViewGroup.LayoutParams params = wrapper.getLayoutParams();
        int wrapperHeight = dp2Px(context, 75);

        switch (cardType) {
            //Ayuca
            case CardParams.TYPE_CODE_AYUCA: {

                switch (ch.getTypeFlag()) {
                    //履歴が乗車だった場合
                    case 0x03: {
                        //SF利用
                        //乗車と降車のバス停を入力
                        name_startText = ch.getStart();
                        name_end.setVisibility(View.VISIBLE);
                        name_endText = "→" + ch.getEnd();

                        //取引価格
                        priceText = String.format("￥%,d", ch.getPrice());

                        //ポイント関係
                        if (ch.getPointFlag()) {
                            //獲得通常ポイントの表示
                            if (ch.getGrantedNormalPoint() > 0) {
//                                point.setVisibility(View.VISIBLE);
                                pointText = (double) ch.getGrantedNormalPoint() / 10 + "P獲得";
                            }
                            //ボーナスポイントの表示
                            if (ch.getGrantedBonusPoint() > 0) {
                                bonusPoint.setVisibility(View.VISIBLE);
                                wrapperHeight += dp2Px(context, 18);
                                bonusPointText = "ボーナス" + (int) ((double) ch.getGrantedBonusPoint() / 10) + "P獲得";
                            }
                            //還元されたポイントの表示
                            if (ch.getUsedPoint() > 0) {
                                usedPoint.setVisibility(View.VISIBLE);
                                wrapperHeight += dp2Px(context, 18);
                                usedPointText = "P使用￥" + (int) ((double) ch.getUsedPoint() / 10) + "割引";
                                //取引価格
                                priceText = String.format("￥%,d", ch.getPrice() - (int) ((double) ch.getUsedPoint() / 10));
                            }

                        }
                        break;

                    }
                    //チャージ
                    case 0x09: {
                        name_startText = ch.getDevice() + "チャージ";
                        //取引価格
                        priceText = String.format("+￥%,d", ch.getPrice());
                        break;
                    }
                    //新規発行
                    case 0xA: {

                    }
                }

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("M月d日 H:mm");
                dateText = dateFormat.format(ch.getDate());

                break;
            }
            //生協電子マネー
            case CardParams.TYPE_CODE_CAMPUS_PAY: {
                point.setVisibility(View.GONE);
                wrapperHeight -= dp2Px(context, 20);

                name_startText = ch.getType();
                switch (ch.getTypeFlag()) {
                    case 0x5:
                        //SF利用
                        priceText = String.format("￥%,d", ch.getPrice());
                        break;
                    case 0x1:
                        //チャージ
                        priceText = String.format("+￥%,d", ch.getPrice());
                        break;
                }

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("M月d日 H:mm:ss");
                dateText = dateFormat.format(ch.getDate());

                break;
            }
        }

        //UIの高さを動的に指定
        params.height = wrapperHeight;
        wrapper.setLayoutParams(params);

        //TextViewに文字を設定
        name_start.setText(name_startText);
        name_end.setText(name_endText);
        price.setText(priceText);
        date.setText(dateText);
        point.setText(pointText);
        bonusPoint.setText(bonusPointText);
        usedPoint.setText(usedPointText);

    }

    /**
     * dp単位をpxに単位置換する
     * @param context コンテキスト
     * @param dp dp
     * @return px
     */
    public static int dp2Px(Context context, int dp) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * d) + 0.5);
    }
}

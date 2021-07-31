package com.example.y3033067.nfcreader.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.y3033067.nfcreader.card.CardHistory;

import java.text.SimpleDateFormat;

public class HistoryUI {
    private final TextView name_start,name_end,price,bonusPoint,point,usedPoint,date;
    private final ImageView line;
    private final View wrapper;
    private final Context context;

    public HistoryUI(TextView _name_start,TextView _name_end,TextView _price,TextView _date,
                     TextView _point,TextView _bonusPoint,TextView _usedPoint,
                     ImageView _line,View _wrapper,Context _context){
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

    public void setVisible(Boolean visible){
        int visibility;
        if(visible){
            visibility = View.VISIBLE;
        }else{
            visibility = View.GONE;
        }
        name_start.setVisibility(visibility);
        name_end.setVisibility(visibility);
        price.setVisibility(visibility);
        date.setVisibility(visibility);
        point.setVisibility(visibility);
        bonusPoint.setVisibility(visibility);
        usedPoint.setVisibility(visibility);
        line.setVisibility(visibility);
    }

    @SuppressLint("DefaultLocale")
    public void setText(CardHistory ch, int cardType){
        String name_startText = "";
        String name_endText = "";
        String priceText = "";
        String dateText = "";
        String pointText = "";
        String bonusPointText = "";
        String usedPointText = "";

        name_start.setVisibility(View.VISIBLE);
        name_end.setVisibility(View.GONE);
        price.setVisibility(View.VISIBLE);
        date.setVisibility(View.VISIBLE);
        point.setVisibility(View.VISIBLE);
        bonusPoint.setVisibility(View.GONE);
        usedPoint.setVisibility(View.GONE);

        ViewGroup.LayoutParams params = wrapper.getLayoutParams();
        int wrapperHeight = dp2Px(context,75);

        switch (cardType){
            //Ayuca
            case 1: {

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
                            if (ch.getGrantedNormalPoint() > 0) {
//                                point.setVisibility(View.VISIBLE);
                                pointText = (double) ch.getGrantedNormalPoint() / 10 + "P獲得";
                            }

                            if (ch.getGrantedBonusPoint() > 0) {
                                bonusPoint.setVisibility(View.VISIBLE);
                                wrapperHeight += dp2Px(context,18);
                                bonusPointText = "ボーナス" + (int) ((double) ch.getGrantedBonusPoint() / 10) + "P獲得";
                            }

                            if (ch.getUsedPoint() > 0) {
                                usedPoint.setVisibility(View.VISIBLE);
                                wrapperHeight += dp2Px(context,18);
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
            case 2: {
                point.setVisibility(View.GONE);
                wrapperHeight -= dp2Px(context,20);

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

        params.height = wrapperHeight;

        wrapper.setLayoutParams(params);

        name_start.setText(name_startText);
        name_end.setText(name_endText);
        price.setText(priceText);
        date.setText(dateText);
        point.setText(pointText);
        bonusPoint.setText(bonusPointText);
        usedPoint.setText(usedPointText);

    }

    //dpをpxに単位置換
    public static int dp2Px(Context context, int dp){
        float d = context.getResources().getDisplayMetrics().density;
        return (int)((dp * d) + 0.5);
    }
}

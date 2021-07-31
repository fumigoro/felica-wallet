package com.example.y3033067.nfcreader.card;

/**
 * Ayucaのバス停コードをもつクラス
 */
public class AyucaStationCode {

    /**
     * バス停コード1つ分のクラス
     */
    private static class Station {
        //メンバー：コード、バス停名
        private String code, name;

        public int getCode() {
            int codeInt = 0x0;
            try {
                codeInt = Integer.parseInt(code, 16);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return codeInt;
        }

        public String getName() {
            return name;
        }
    }

    //バス停コードの配列
    private Station[] station;

    /**
     * バス停コードを渡すとバス停名を返す
     *
     * @param code バス停コード
     * @return バス停名
     */
    public String getStation(int code) {
        for (Station value : station) {
            if (value.getCode() == code) {
                return value.getName();
            }
        }
        return String.format("不明(%04X)", code);
    }

}

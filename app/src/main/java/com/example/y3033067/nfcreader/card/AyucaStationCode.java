package com.example.y3033067.nfcreader.card;

public class AyucaStationCode {

    private class Station{
        private String code,name;
        public int getCode(){
            int codeInt = 0x0;
            try{
                codeInt = Integer.parseInt(code,16);
            }catch(Exception e){
                e.printStackTrace();
            }
            return codeInt;
        }
        public String getName(){
            return name;
        }
    }

    private Station[] station;
    public String getStation(int code){
        for (Station value : station) {
            if (value.getCode() == code) {
                return value.getName();
            }
        }
        return String.format("不明(%04X)",code);
    }

}

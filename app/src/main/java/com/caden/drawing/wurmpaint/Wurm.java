package com.caden.drawing.wurmpaint;

class Wurm {

    private String wurmDate;
    private String wurmTime;
    private String wurmBatchName;
    private int wurmImgNo;

    Wurm(String wurmDate, String wurmTime, String wurmBatchName, int wurmImgNo) {
        this.wurmDate = wurmDate;
        this.wurmTime = wurmTime;
        this.wurmBatchName = wurmBatchName;
        this.wurmImgNo = wurmImgNo;
    }

    Wurm() {
        this("", "", "", 0);
    }

    String getWurmDate() {
        return this.wurmDate;
    }

    String getWurmTime() {
        return this.wurmTime;
    }

    String getWurmBatchName() {
        return this.wurmBatchName;
    }

    int getWurmImgNo() {
        return this.wurmImgNo;
    }
}

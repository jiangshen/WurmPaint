package com.example.caden.drawingtest;

public class Wurm {

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

    public String getWurmDate() {
        return this.wurmDate;
    }

    public String getWurmTime() {
        return this.wurmTime;
    }

    public String getWurmBatchName() {
        return this.wurmBatchName;
    }

    public int getWurmImgNo() {
        return this.wurmImgNo;
    }
}

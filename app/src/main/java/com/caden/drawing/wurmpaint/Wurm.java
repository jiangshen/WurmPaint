package com.caden.drawing.wurmpaint;

/**
 * Defines a Wurm data class
 */
public class Wurm {

    private String wurmDate;
    private String wurmTime;
    private String wurmBatchName;
    private int wurmImgNo;

    public Wurm(String wurmDate, String wurmTime, String wurmBatchName, int wurmImgNo) {
        this.wurmDate = wurmDate;
        this.wurmTime = wurmTime;
        this.wurmBatchName = wurmBatchName;
        this.wurmImgNo = wurmImgNo;
    }

    public Wurm() {
        this("", "", "", 0);
    }

    // Note: Public Getters and Setters for FireBase
    public String getWurmDate() {
        return wurmDate;
    }

    public void setWurmDate(String wurmDate) {
        this.wurmDate = wurmDate;
    }

    public String getWurmTime() {
        return wurmTime;
    }

    public void setWurmTime(String wurmTime) {
        this.wurmTime = wurmTime;
    }

    public String getWurmBatchName() {
        return wurmBatchName;
    }

    public void setWurmBatchName(String wurmBatchName) {
        this.wurmBatchName = wurmBatchName;
    }

    public int getWurmImgNo() {
        return wurmImgNo;
    }

    public void setWurmImgNo(int wurmImgNo) {
        this.wurmImgNo = wurmImgNo;
    }
}
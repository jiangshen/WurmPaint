package com.example.caden.drawingtest;

import android.graphics.Color;

/**
 * Created by caden on 10/13/17.
 */

public class ImageManager {

    static byte[] imgData;
    static byte[] drawnImg;
    static int currID;

    static int brushColor;

    static String imgFileName;

    public static void setImage(byte[] data) {
        imgData = data;
    }

    public static void setImageID(int id) {
        currID = id;
    }

    public static void setBrushColor(int c) {
        brushColor = c;
    }

    public static void setImgFileName(String s) {
        imgFileName = s;
    }

    public static void setDrawnImg(byte[] data) {
        drawnImg = data;
    }

}

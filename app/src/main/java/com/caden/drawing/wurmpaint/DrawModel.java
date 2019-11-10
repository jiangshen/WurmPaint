package com.caden.drawing.wurmpaint;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the Drawing Model
 */
public class DrawModel {

    public static class LineElem {
        float x;
        float y;

        //internal representation for manipulation
        public LineElem(float x, float y) {
            this.x = x;
            this.y = y;
        }

        // Note: Public Getters and Setters for FireBase
        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }

    /**
     * Defines a single Line
     */
    static class Line {

        /**
         * A line consists of a set of elements (small parts of a line)
         */
        List<LineElem> elems = new ArrayList<>();

        Line() {
        }
        //add, get, and get index of an element
        void addElem(LineElem elem) {
            elems.add(elem);
        }

        int getElemSize() {
            return elems.size();
        }

        LineElem getElem(int index) {
            return elems.get(index);
        }

        List<LineElem> getAllLineElem() {
            return elems;
        }
    }

    private Line mCurrentLine;

    // Pixel width & height = 28
    private int mWidth;
    private int mHeight;

    /**
     * A model consists of lines which consists of elements
     * A line begins when a user starts drawing and ends when they lift their finger up
     */
    private List<Line> mLines = new ArrayList<>();

    //given a set 28 by 28 sized window
    public DrawModel(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    // Start drawing line and add it to memory
    void startLine(float x, float y) {
        mCurrentLine = new Line();
        mCurrentLine.addElem(new LineElem(x, y));
        mLines.add(mCurrentLine);
    }

    void endLine() {
        mCurrentLine = null;
    }

    void addLineElem(float x, float y) {
        if (mCurrentLine != null) {
            mCurrentLine.addElem(new LineElem(x, y));
        }
    }

    int getLineSize() {
        return mLines.size();
    }

    Line getLine(int index) {
        return mLines.get(index);
    }

    void clear() {
        mLines.clear();
    }
}

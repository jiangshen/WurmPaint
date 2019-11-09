package com.caden.drawing.wurmpaint;

import java.util.ArrayList;
import java.util.List;

//a collection of getter and set functions
//to draw a character model
public class DrawModel {
    //initialize beginning of the line coordinate
    static class LineElem {
        float x;
        float y;

        //internal representation for manipulation
        private LineElem(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    //for a single line
    static class Line {
        //a line consists of a set of elements (small parts of a line)
        private List<LineElem> elems = new ArrayList<>();

        private Line() {
        }
        //add, get, and get index of an element
        private void addElem(LineElem elem) {
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

    private int mWidth;  // pixel width = 28
    private int mHeight; // pixel height = 28

    //so a model consists of lines which consists of elements
    //a line begins when a user starts drawing and ends when
    //they lift their finger up
    private List<Line> mLines = new ArrayList<>();

    //given a set 28 by 28 sized window
    DrawModel(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    //start drawing line and add it to memory
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

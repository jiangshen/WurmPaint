package com.example.caden.drawingtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class DrawView extends View {

    private Paint mPaint = new Paint();
    private DrawModel mModel;

    // 28x28 pixel Bitmap
    private Bitmap mOffscreenBitmap;
    private Canvas mOffscreenCanvas;

    private Matrix mMatrix = new Matrix();
    private Matrix mInvMatrix = new Matrix();
    private int mDrawnLineSize = 0;
    private boolean mHasBeenSetup = false;

    private float mTmpPoints[] = new float[2];

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setModel(DrawModel model) {
        this.mModel = model;
    }

    //reset the view, so empty the drawing (set everything to white and redraw the 28x28
    //rectangle
    public void reset() {
        mDrawnLineSize = 0;
        if (mOffscreenBitmap != null) {
            // used to be gray #E0E0E0
            mPaint.setColor(Color.parseColor("#000000"));
            int width = mOffscreenBitmap.getWidth();
            int height = mOffscreenBitmap.getHeight();
            mOffscreenCanvas.drawRect(new Rect(0, 0, width, height), mPaint);
            addBackground();
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void addBackground() {
        if (ImageManager.imgData != null) {
            Bitmap img = BitmapFactory.decodeByteArray(ImageManager.imgData, 0,
                    ImageManager.imgData.length);
//            use the width of the screen, and for height, scale it proportional to how much width was scaled
//            (mOffscreenBitmap.getWidth() - img.getHeight()) / 2

//            Log.d("img", img.getWidth() + "..." + img.getHeight());
//            Log.d("offscreenbmp", mOffscreenBitmap.getWidth() + "..." + mOffscreenBitmap.getHeight());
//            Log.d("drawview", this.getWidth() + "..." + this.getHeight());
//            Log.d("calculated", 0 + "..." + 0 + "..." + mOffscreenBitmap.getWidth() + "..." + img.getHeight() * (mOffscreenBitmap.getWidth()/img.getWidth()));
//           For now draw same dimensions as mOffscreenBitmap
            Rect dest = new Rect(0, 0, mOffscreenBitmap.getWidth(),
                    mOffscreenBitmap.getHeight());
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            mOffscreenCanvas.drawBitmap(img, null, dest, paint);
        }
    }

    // create the view, for a given length and width
    private void setup() {
        mHasBeenSetup = true;

        // View size
        float width = getWidth();
        float height = getHeight();

        // Model (bitmap) size
        float modelWidth = mModel.getWidth();
        float modelHeight = mModel.getHeight();

        float scaleW = width / modelWidth;
        float scaleH = height / modelHeight;

        float scale = scaleW;
        if (scale > scaleH) scale = scaleH;

        float newCx = modelWidth * scale / 2;
        float newCy = modelHeight * scale / 2;
        float dx = width / 2 - newCx;
        float dy = height / 2 - newCy;

        mMatrix.setScale(scale, scale);
        mMatrix.postTranslate(dx, dy);
        mMatrix.invert(mInvMatrix);
        mHasBeenSetup = true;
    }

    @Override
    // when the user begins drawing, initialize
    // the model renderer class and draw it on the canvas
    public void onDraw(Canvas canvas) {
        if (mModel == null) {
            return;
        }
        if (!mHasBeenSetup) {
            setup();
        }
        if (mOffscreenBitmap == null) {
            return;
        }

        int startIndex = mDrawnLineSize - 1;
        if (startIndex < 0) {
            startIndex = 0;
        }

        DrawRenderer.renderModel(mOffscreenCanvas, mModel, mPaint, startIndex);
        canvas.drawBitmap(mOffscreenBitmap, mMatrix, mPaint);

        mDrawnLineSize = mModel.getLineSize();
    }

    /**
     * Convert screen position to local pos (pos in bitmap)
     */
    public void calcPos(float x, float y, PointF out) {
        mTmpPoints[0] = x;
        mTmpPoints[1] = y;
        mInvMatrix.mapPoints(mTmpPoints);
        out.x = mTmpPoints[0];
        out.y = mTmpPoints[1];
    }

    public void onResume() {
        createBitmap();
    }

    public void onPause() {
        releaseBitmap();
    }

    //to draw the canvas we need the bitmap
    private void createBitmap() {
        if (mOffscreenBitmap != null) {
            mOffscreenBitmap.recycle();
        }
        mOffscreenBitmap = Bitmap.createBitmap(mModel.getWidth(), mModel.getHeight(),
                Bitmap.Config.ARGB_8888);
        mOffscreenCanvas = new Canvas(mOffscreenBitmap);
        reset();
    }

    private void releaseBitmap() {
        if (mOffscreenBitmap != null) {
            mOffscreenBitmap.recycle();
            mOffscreenBitmap = null;
            mOffscreenCanvas = null;
        }
        reset();
    }

    public Bitmap getBitmapData() {
        return this.mOffscreenBitmap;
    }
}

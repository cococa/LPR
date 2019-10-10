
package com.yzq.zxinglibrary.view;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.google.zxing.ResultPoint;
import com.yzq.zxinglibrary.R;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;

public final class ViewfinderView extends View {

    /*界面刷新间隔时间*/
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;
    private static final int SCAN_RECT_RADIUS = 20; //radius
    private static final int LINE_WIDTH = 12;
    private static final int LINE_PADDING = 80; //int padding = 70;

    private CameraManager cameraManager;
    private Paint paint;//, reactPaint, frameLinePaint;
    private Bitmap resultBitmap;
    private int maskColor; // 取景框外的背景颜色
    private int resultColor;// result Bitmap的颜色
    private int resultPointColor; // 特征点的颜色
    private int reactColor;//四个角的颜色
    private int scanLineColor;//扫描线的颜色
    private int frameLineColor = -1;//边框线的颜色

    private static final int WHITE_COLOR = Color.parseColor("#ffffff");
    private static final int TRANS_COLOR = Color.parseColor("#30B0B0B0"); // 透明色

    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;
    // 扫描线移动的y
    private int scanLineTop;

    private ZxingConfig config;
    private ValueAnimator valueAnimator;
    private Rect frame;


    public ViewfinderView(Context context) {
        this(context, null);

    }

    public ViewfinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public void setZxingConfig(ZxingConfig config) {
        this.config = config;
        reactColor = ContextCompat.getColor(getContext(), config.getReactColor());

        if (config.getFrameLineColor() != -1) {
            frameLineColor = ContextCompat.getColor(getContext(), config.getFrameLineColor());
        }

        scanLineColor = ContextCompat.getColor(getContext(), config.getScanLineColor());
        initPaint();

    }


    public ViewfinderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        maskColor = ContextCompat.getColor(getContext(), R.color.viewfinder_mask);
        resultColor = ContextCompat.getColor(getContext(), R.color.result_view);
        resultPointColor = ContextCompat.getColor(getContext(), R.color.possible_result_points);

        possibleResultPoints = new ArrayList<ResultPoint>(10);
        lastPossibleResultPoints = null;
        getDefaultDisplay();
        setD(1);
    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        /*四个角的画笔*/
//        reactPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        reactPaint.setColor(reactColor);
//        reactPaint.setStyle(Paint.Style.FILL);
//        reactPaint.setStrokeWidth(dp2px(1));
//
//        /*边框线画笔*/
//
//        if (frameLineColor != -1) {
//            frameLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            frameLinePaint.setColor(ContextCompat.getColor(getContext(), config.getFrameLineColor()));
//            frameLinePaint.setStrokeWidth(dp2px(1));
//            frameLinePaint.setStyle(Paint.Style.STROKE);
//        }


    }

    private void initAnimator() {

        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(frame.top, frame.bottom);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

//                    scanLineTop = (int) animation.getAnimatedValue();
//                    invalidate();

                }
            });

            valueAnimator.start();
        }
    }

    int width_ = 0;
    int height_ = 0;

    Point p;

    private void getDefaultDisplay() {
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        p = new Point(display.getWidth(), display.getHeight());
        Log.e("cocoa", "cocoa point= " + p.x + "  " + p.y);
    }


    public void setMode(int mode) {
        if (p == null) {
            getDefaultDisplay();
        }
        setD(mode);
        requestLayout();
    }

    private void setD(int mode) {
        if (mode == 1) {
            this.width_ = (int) (p.x * 0.75);
            this.height_ = width_;
        } else {
            this.width_ = (int) (p.x * 0.75);
            this.height_ = (int) (height_ * 0.65);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(width_);
        int height = measureHeight(height_);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.AT_MOST) {
        } else if (specMode == MeasureSpec.EXACTLY) {
        }
        return specSize;
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.AT_MOST) {
        } else if (specMode == MeasureSpec.EXACTLY) {
        }
        return specSize;
    }


    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    public void stopAnimator() {
        if (valueAnimator != null) {
            valueAnimator.end();
            valueAnimator.cancel();
            valueAnimator = null;
        }

    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {

        if (cameraManager == null) {
            return;
        }

        // frame为取景框
        frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        initAnimator();

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        /*绘制遮罩*/
//        drawMaskView(canvas, frame, width, height);

        /*绘制取景框边框*/
        drawFrameBounds(canvas, new Rect(LINE_WIDTH, LINE_WIDTH, width - LINE_WIDTH - LINE_WIDTH, height - LINE_WIDTH - LINE_WIDTH));

//        if (resultBitmap != null) {
//            // Draw the opaque result bitmap over the scanning rectangle
//            // 如果有二维码结果的Bitmap，在扫取景框内绘制不透明的result Bitmap
//            paint.setAlpha(CURRENT_POINT_OPACITY);
//            canvas.drawBitmap(resultBitmap, null, frame, paint);
//        }
    }


    /**
     * 绘制取景框边框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameBounds(Canvas canvas, Rect frame) {

        /*扫描框的边框线*/
//        if (frameLineColor != -1) {
//            canvas.drawRect(frame, frameLinePaint);
//        }

//        int corLength = (int) (width * 0.07);
//        int corWidth = (int) (corLength * 0.2);

//        corWidth = corWidth > 15 ? 15 : corWidth;
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(WHITE_COLOR);
        paint.setStrokeWidth(LINE_WIDTH);
        RectF r2 = new RectF();
        r2.set(frame);
        canvas.drawRoundRect(r2, SCAN_RECT_RADIUS, SCAN_RECT_RADIUS, paint);

        paint.setStyle(Paint.Style.FILL);

        paint.setColor(TRANS_COLOR);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


        canvas.drawRect(frame.left + LINE_PADDING, frame.top - LINE_WIDTH, frame.right - LINE_PADDING, frame.top + LINE_WIDTH, paint);
        canvas.drawRect(frame.left + LINE_PADDING, frame.bottom - LINE_WIDTH, frame.right - LINE_PADDING, frame.bottom + LINE_WIDTH
                , paint);
        canvas.drawRect(frame.left - LINE_WIDTH, frame.top + LINE_PADDING, frame.left + LINE_WIDTH, frame.bottom - LINE_PADDING
                , paint);
        canvas.drawRect(frame.right - LINE_WIDTH, frame.top + LINE_PADDING, frame.right + LINE_WIDTH, frame.bottom - LINE_PADDING
                , paint);
    }


    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

}

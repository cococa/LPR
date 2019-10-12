package com.yzq.zxinglibrary.view;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.yzq.zxinglibrary.R;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.camera.CameraManager;


public class ScanView extends View {

    private static final int SCAN_RECT_RADIUS = 20; //radius
    private static final int LINE_WIDTH = 12;
    private static final int LINE_PADDING = 80; //int padding = 70;
    private static final String TAG = "ScanView";


    private static final int TRANS_COLOR = Color.parseColor("#30B0B0B0"); // 透明色
    private static final int MAIN_COLOR = Color.parseColor("#00CC99");


    public ScanView(Context context) {
        super(context);
    }

    public ScanView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

    }

    public ScanView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

    }

    private CameraManager cameraManager;
    private int mode = ZxingConfig.SCAN_TYPE_QRCODE;


    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }


    public void setScanType(int mode) {
        this.mode = mode;
        invalidate();
    }

    private boolean flashSwitch = false;

    public void setFlashSwitch(boolean flashSwitch) {
        this.flashSwitch = flashSwitch;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        int halfWidth = width / 2;
        int frameLeft = (int) (width * 0.1);
        int frameTop = 250;
        int frameWdith = (int) (width * 0.8);
        int frameHeight = frameWdith;
        if (mode == ZxingConfig.SCAN_TYPE_PLATE) {
            frameHeight = (int) (frameWdith * 0.6);
        }
        drawFrameBounds(canvas, new Rect(frameLeft, frameTop, frameLeft + frameWdith, frameTop + frameHeight));
        drawTopText(canvas, new Point(halfWidth, frameTop / 2));
        drawBootomText(canvas, new Point(halfWidth, frameTop + frameHeight + 200));
        drawFlash(canvas, new Point(halfWidth, frameTop + frameHeight - 120));
    }


    private void drawFlash(Canvas canvas, Point point) {

        Resources res = getResources();
        Paint paint = new Paint();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        String str = res.getString(R.string.open_flash);
        int bitmapRes = R.drawable.light_off;
        paint.setColor(Color.WHITE);
        if (flashSwitch) {
            paint.setColor(MAIN_COLOR);
            str = res.getString(R.string.close_flash);
            bitmapRes = R.drawable.light_on;
        }


        paint.setTextSize(getResources().getDimension(R.dimen.text12));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(str, point.x, point.y, paint);
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        int w = rect.width();
        int h = rect.height();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), bitmapRes);
        canvas.drawBitmap(bitmap, point.x - (bitmap.getWidth() / 2), point.y - bitmap.getHeight() - 50, paint);

        rect.top = point.y - bitmap.getHeight() - 50;
        rect.bottom = point.y + 20;
        rect.left = point.x - (w / 2) - 20;
        rect.right = point.x + (w / 2) + 20;

        openFlashRectF = new RectF(rect);


    }


    private RectF bottomTextRectF;
    private RectF openFlashRectF;

    private void drawBootomText(Canvas canvas, Point point) {
        Paint paint = new Paint();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(MAIN_COLOR);

        String str = getResources().getString(R.string.scan_failed_text);
        Rect rect = new Rect();
        paint.setTextSize(getResources().getDimension(R.dimen.text12));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(str, point.x, point.y, paint);
        paint.getTextBounds(str, 0, str.length(), rect);

        int w = rect.width();
        int h = rect.height();
        rect.top = point.y - h - 20;
        rect.bottom = point.y + 40;
        rect.left = point.x - (w / 2) - 60;
        rect.right = point.x + (w / 2) + 60;

        bottomTextRectF = new RectF(rect);
        paint.setStrokeWidth(dp2px(1));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(bottomTextRectF, 60, 60, paint);

    }


    private void drawTopText(Canvas canvas, Point point) {
        int strRes = mode == ZxingConfig.SCAN_TYPE_PLATE ? R.string.scan_plate : R.string.scan_qrcode;
        String str = getResources().getString(strRes);
        Paint paint = new Paint();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(getResources().getDimension(R.dimen.text14));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(str, point.x, point.y, paint);
    }

    private void drawFrameBounds(Canvas canvas, Rect frame) {

        int halfWidth = LINE_WIDTH / 2;
        RectF r2 = new RectF();
        r2.set(frame);


        Paint paint = new Paint();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);


        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#30000000"));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);


        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        RectF r1 = new RectF();
        r1.left = r2.left + halfWidth;
        r1.right = r2.right - halfWidth;
        r1.top = r2.top + halfWidth;
        r1.bottom = r2.bottom - halfWidth;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawRoundRect(r1, SCAN_RECT_RADIUS, SCAN_RECT_RADIUS, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(LINE_WIDTH);
        canvas.drawRoundRect(r2, SCAN_RECT_RADIUS, SCAN_RECT_RADIUS, paint);


        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(TRANS_COLOR);
        canvas.drawRect(frame.left + LINE_PADDING, frame.top - halfWidth, frame.right - LINE_PADDING, frame.top + halfWidth, paint);
        canvas.drawRect(frame.left + LINE_PADDING, frame.bottom - halfWidth, frame.right - LINE_PADDING, frame.bottom + halfWidth
                , paint);
        canvas.drawRect(frame.left - halfWidth, frame.top + LINE_PADDING, frame.left + halfWidth, frame.bottom - LINE_PADDING
                , paint);
        canvas.drawRect(frame.right - halfWidth, frame.top + LINE_PADDING, frame.right + halfWidth, frame.bottom - LINE_PADDING
                , paint);


    }

    OnClickListener l;

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.l = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            boolean failedClick = bottomTextRectF.contains(event.getX(), event.getY());
            boolean openFlashClick = openFlashRectF.contains(event.getX(), event.getY());

            Log.i(TAG, TAG + openFlashClick + openFlashRectF.toString());

            if (failedClick) {
                click(R.id.scan_failed);
            }
            if (openFlashClick) {
                click(R.id.open_flash);
            }
        }
        return super.onTouchEvent(event);
    }


    private void click(int id) {
        this.setId(id);
        if (l != null) {
            l.onClick(this);
        }
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}



package com.yzq.zxinglibrary.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.yzq.zxinglibrary.R;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.camera.CameraManager;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.view.ScanView;
import com.yzq.zxinglibrary.view.ViewfinderViewMutl;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;


/**
 * @author: yzq
 * @date: 2017/10/26 15:22
 * @declare :扫一扫
 */

public class MultipleCaptureActivity extends AppCompatActivity implements ActivityI, SurfaceHolder.Callback, View.OnClickListener {

    private static final String TAG = MultipleCaptureActivity.class.getSimpleName();
    public ZxingConfig config;
    private SurfaceView previewView;
    private ScanView scanView;
    private LinearLayout scanQrcode;
    private LinearLayout scanPlate;
    private LinearLayout scanContainer;
    private AppCompatImageView backIv;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private SurfaceHolder surfaceHolder;
    private int mode = ZxingConfig.SCAN_TYPE_QRCODE;

    public View getViewfinderView() {
        return scanView;
    }

    @Override
    public void switchFlashImg(int flashState) {
        scanView.setFlashSwitch(flashState == Constant.FLASH_OPEN);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public Context getContext() {
        return this;
    }

    public void drawViewfinder() {
//        viewfinderView.drawViewfinder();
    }

    @Override
    public void setActivityResult(int code, Intent intent) {
        setResult(code, intent);
    }

    @Override
    public ZxingConfig getConfig() {
        return config;
    }


    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 保持Activity处于唤醒状态
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.BLACK);
        }

        setContentView(R.layout.activity_multiple_capture);

        config = getIntent().getParcelableExtra(Constant.INTENT_ZXING_CONFIG);

        if (config == null) {
            config = new ZxingConfig();
        }

        initView();
        initScanType();
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        beepManager.setPlayBeep(config.isPlayBeep());
        beepManager.setVibrate(config.isShake());
    }


    public int getMode() {
        return mode;
    }

    private void setScanType(int mode) {
        this.mode = mode;
        scanView.setScanType(mode);
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    System.loadLibrary("lpr");

//                    Log.i(TAG, "OpenCV loaded successfully");
//                    mOpenCvCameraView.enableView();
//                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    private void initView() {

        scanQrcode = findViewById(R.id.scan_qrcode);
        scanPlate = findViewById(R.id.scan_plate);
        scanContainer = findViewById(R.id.scan_container);


        scanQrcode.setOnClickListener(this);
        scanPlate.setOnClickListener(this);

        previewView = findViewById(R.id.preview_view);
        previewView.setOnClickListener(this);

        scanView = findViewById(R.id.scan_view);
        scanView.setOnClickListener(this);
//        viewfinderView.setZxingConfig(config);


        backIv = findViewById(R.id.backIv);
        backIv.setOnClickListener(this);


    }

    private void initScanType() {
        setScanType(config.getDefaultScanType());

        if (config.isShowVerification()) {
            // 显示扫码核销
            scanContainer.setBackgroundResource(R.drawable.scaner_container_bg);
            scanQrcode.setVisibility(View.VISIBLE);

        } else {
            // 隐藏扫码
            scanContainer.setBackgroundResource(R.drawable.scaner_item_null_bg);
            scanQrcode.setVisibility(View.GONE);
        }


        if (config.getDefaultScanType() == ZxingConfig.SCAN_TYPE_PLATE) {
            scanQrcode.setBackgroundResource(R.drawable.scaner_item_null_bg);
            scanPlate.setBackgroundResource(R.drawable.scaner_item_bg);

        } else {
            scanQrcode.setBackgroundResource(R.drawable.scaner_item_bg);
            scanPlate.setBackgroundResource(R.drawable.scaner_item_null_bg);
        }
    }


    /**
     * @param pm
     * @return 是否有闪光灯
     */
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * @param rawResult 返回的扫描结果
     */
    public void handleDecode(String rawResult) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        Intent intent = getIntent();
        intent.putExtra(Constant.CODED_CONTENT, rawResult);
        setResult(RESULT_OK, intent);
        this.finish();
    }


    @Override
    protected void onResume() {
        super.onResume();

        cameraManager = new CameraManager(getApplication(), config);

        scanView.setCameraManager(cameraManager);
        handler = null;

        surfaceHolder = previewView.getHolder();
        if (hasSurface) {

            initCamera(surfaceHolder);
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder.addCallback(this);
        }

        beepManager.updatePrefs();
        inactivityTimer.onResume();

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("扫一扫");
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();

        if (!hasSurface) {
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
//        viewfinderView.stopAnimator();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.open_flash) {
            /*切换闪光灯*/
            cameraManager.switchFlashLight(handler);
        } else if (id == R.id.backIv) {
            finish();
        } else if (id == R.id.scan_qrcode) {
            scanQrcode.setBackgroundResource(R.drawable.scaner_item_bg);
            scanPlate.setBackgroundResource(R.drawable.scaner_item_null_bg);
            setScanType(ZxingConfig.SCAN_TYPE_QRCODE);

        } else if (id == R.id.scan_plate) {
            scanQrcode.setBackgroundResource(R.drawable.scaner_item_null_bg);
            scanPlate.setBackgroundResource(R.drawable.scaner_item_bg);
            setScanType(ZxingConfig.SCAN_TYPE_PLATE);
        } else if (id == R.id.scan_failed) {
            setResult(ZxingConfig.SCAN_RESULT_TYPE_FAILED);
            finish();
        }

    }


}

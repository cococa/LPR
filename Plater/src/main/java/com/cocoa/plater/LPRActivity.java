package com.cocoa.plater;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.plater.android.BaseLoaderCallback;
import org.plater.android.LoaderCallbackInterface;
import org.plater.android.OpenCVLoader;
import org.plater.android.Utils;
import org.plater.core.CvType;
import org.plater.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LPRActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    public long handle;
    private SurfaceView mSurfaceView;
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private ViewFinderView viewFinderView;
    private PalmTask mFaceTask;
    private boolean isStarus = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lpr);
        mSurfaceView = findViewById(R.id.sv_camera_ui);
        viewFinderView = findViewById(R.id.view_finder_view);
        surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void finishValue(String card) {
        Intent intent = new Intent();
        intent.putExtra("card", card);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //一般在这里实现相机打开
        //相机在这里设置相关参数也是可以的
        camera = Camera.open(0);
        camera.setDisplayOrientation(90);
        //参数设置
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        //参数设置赋给Camera对象
        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.setPreviewCallback(this);
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //在这里也可以设置Camera的参数
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //主要在这里实现Camera的释放
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (null != mFaceTask) {
            switch (mFaceTask.getStatus()) {
                case RUNNING:
                    return;
                case PENDING:
                    mFaceTask.cancel(false);
                    break;
            }
        }
        if (!isStarus) {
            return;
        }
        mFaceTask = new PalmTask(data);
        mFaceTask.execute();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void onManagerConnected(int status) {
            //在加载openCV 成功后, 开始加载 vcd so 文件
            if (status == LoaderCallbackInterface.SUCCESS) {
                System.loadLibrary("lpr");
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        handle = DeepAssetUtil.initRecognizer(LPRActivity.this);
                        return null;
                    }
                }.execute();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    /**
     * 自定义的FaceTask类，开启一个线程分析数据
     */
    @SuppressLint("StaticFieldLeak")
    private class PalmTask extends AsyncTask<Void, Void, String> {
        private byte[] mData;

        //构造函数
        PalmTask(byte[] data) {
            this.mData = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            Camera.Size size = camera.getParameters().getPreviewSize();
            try {
                YuvImage image = new YuvImage(mData, ImageFormat.NV21, size.width, size.height, null);


                ByteArrayOutputStream stream = new ByteArrayOutputStream(mData.length);
                //image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                if (!image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, stream)) {
                    return null;
                }


                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                //旋转图片
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                //mBmp = Bitmap.createBitmap(bmp, viewFinderView.center.top + actionHeight, viewFinderView.center.left,viewFinderView.centerHeight, viewFinderView.centerWidth, matrix, true);
                stream.close();
                Log.e("Sys", "cocoa:----" +viewFinderView.center.top +"   left"+ viewFinderView.center.left +" width="+viewFinderView.centerWidth+ " height"+viewFinderView.centerHeight);
                Bitmap bitmap = Bitmap.createBitmap(bmp, 0,0,
                        size.width,size.height, matrix, true);


                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Log.e("Sys", "cocoa:----" +width +"  height"+ height);

                Mat m = new Mat(width, height, CvType.CV_8UC4);
//                    Mat m = new Mat(width, height, CvType.CV_8UC2);
                Utils.bitmapToMat(bitmap, m);
                return PlateRecognition.SimpleRecognization(m.getNativeObjAddr(), handle);

            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            if (!TextUtils.isEmpty(str)) {
                isStarus = false;
                String[] list = str.split(",");
                if (list.length > 1) {
                    new AlertDialog.Builder(LPRActivity.this)
                            .setTitle("请点击选择")
                            .setItems(list, (dialog, which) -> finishValue(list[which]))
                            .setNegativeButton("重新识别", (dialog, which) -> isStarus = true)
                            .show();
                } else
                    new AlertDialog.Builder(LPRActivity.this)
                            .setTitle("识别结果")
                            .setMessage(str)
                            .setPositiveButton("确定", (dialog, which) -> finishValue(str))
                            .setNegativeButton("重新识别", (dialog, which) -> isStarus = true)
                            .show();
            }

        }
    }
}
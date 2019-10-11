/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yzq.zxinglibrary.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.cocoa.plater.DeepAssetUtil;
import com.cocoa.plater.PlateRecognition;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.yzq.zxinglibrary.android.ActivityI;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.camera.CameraManager;
import com.yzq.zxinglibrary.common.Constant;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

public final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final ActivityI activity;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;
    long target;
    private CameraManager cameraManager;

    DecodeHandler(ActivityI activity, CameraManager cameraManager, Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        this.cameraManager = cameraManager;
        multiFormatReader.setHints(hints);
        this.activity = activity;
        target = DeepAssetUtil.initRecognizer(activity.getContext());
    }


    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case Constant.DECODE:

                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case Constant.QUIT:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * 解码
     */
    private void decode(byte[] data, int width, int height) {
        if (activity.getMode() == ZxingConfig.SCAN_TYPE_QRCODE) {
            Result rawResult = null;
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
                }
            }
            int tmp = width; // Here we are swapping, that's the difference to #11
            width = height;
            height = tmp;
            data = rotatedData;

            PlanarYUVLuminanceSource source = activity.getCameraManager()
                    .buildLuminanceSource(data, width, height);
            if (source != null) {
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                try {
                    rawResult = multiFormatReader.decodeWithState(bitmap);
                } catch (ReaderException re) {
                    Log.e("cocoa", "cocoa ==>" + re.toString());
                } finally {
                    multiFormatReader.reset();
                }
            }
            Handler handler = activity.getHandler();
            if (rawResult != null) {

                if (handler != null) {
                    Message message = Message.obtain(handler,
                            Constant.DECODE_SUCCEEDED, rawResult.getText());
                    message.sendToTarget();
                }
            } else {
                Log.e("cocoa", "cocoa ==>" + "failed");
                if (handler != null) {
                    Message message = Message.obtain(handler, Constant.DECODE_FAILED);
                    message.sendToTarget();
                }
            }
        } else {
            Handler handler = activity.getHandler();
            try {
                YuvImage image = new YuvImage(data, ImageFormat.NV21, width, height, null);


                ByteArrayOutputStream stream = new ByteArrayOutputStream(data.length);
                //image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                if (!image.compressToJpeg(new Rect(0, 0, width, height), 100, stream)) {
                    return;
                }
                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                //旋转图片
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                //mBmp = Bitmap.createBitmap(bmp, viewFinderView.center.top + actionHeight, viewFinderView.center.left,viewFinderView.centerHeight, viewFinderView.centerWidth, matrix, true);
                stream.close();
                stream = null;

                Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0,
                        width, height, matrix, true);

                Rect destrect = cameraManager.getFramingRect();

                Rect rect = new Rect();
                rect.top = (int) (destrect.top * 0.75);
                rect.left = (int) (destrect.left * 0.75);
                rect.right = (int) (destrect.right * 0.75);
                rect.bottom = (int) (destrect.bottom * 0.75);
                Log.e("cocoa", "cocoa " + rect.left + "-" + rect.top + "-" + rect.right + "-" + rect.bottom + "   width=" + bitmap.getWidth() + "  height=" + bitmap.getHeight());


                bitmap = Bitmap.createBitmap(bitmap, 0, rect.top, bitmap.getWidth(), rect.bottom - rect.top + 300, null, false);

                File appDir = new File(Environment.getExternalStorageDirectory() + "/A001");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                FileOutputStream fos = new FileOutputStream(appDir + "/" + System.currentTimeMillis() + ".jpeg");
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                fos.close();


                Mat m = new Mat(width, height, CvType.CV_8UC4);
//                    Mat m = new Mat(width, height, CvType.CV_8UC2);
                Utils.bitmapToMat(bitmap, m);
                String plate = PlateRecognition.SimpleRecognization(m.getNativeObjAddr(), target);
                Log.e("cocoa", "cocoa ==>" + plate);
                if (!TextUtils.isEmpty(plate)) {
                    Message message = Message.obtain(handler,
                            Constant.DECODE_SUCCEEDED, plate);
                    message.sendToTarget();
                } else {
                    Message message = Message.obtain(handler, Constant.DECODE_FAILED);
                    message.sendToTarget();
                }

            } catch (Exception ex) {
                Log.e("cocoa", "cocoa ==>" + ex.toString());
                Message message = Message.obtain(handler, Constant.DECODE_FAILED);
                message.sendToTarget();
            }
        }
    }

}

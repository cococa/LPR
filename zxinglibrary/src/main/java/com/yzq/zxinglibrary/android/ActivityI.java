package com.yzq.zxinglibrary.android;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.camera.CameraManager;

public interface ActivityI {

    void handleDecode(String rawResult);

    View getViewfinderView();

    void switchFlashImg(int flashState);

    void finishActivity();

    void drawViewfinder();

    void setActivityResult(int code, Intent intent);

    ZxingConfig getConfig();

    Handler getHandler();

    CameraManager getCameraManager();

    Context getContext();

    int getMode();
}

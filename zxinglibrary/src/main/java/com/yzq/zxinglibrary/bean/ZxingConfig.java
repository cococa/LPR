package com.yzq.zxinglibrary.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;

import com.yzq.zxinglibrary.R;

import java.io.Serializable;

/**
 * @author: yzq
 * @date: 2017/10/27 14:48
 * @declare :zxing配置类
 */


public class ZxingConfig implements Parcelable {


    public static final String INTENT_ZXING_CONFIG = "zxingConfig";


    public static final int SCAN_TYPE_PLATE = 0x123; // 车牌扫描
    public static final int SCAN_TYPE_QRCODE = 0x223; // 二维码扫描

    public static final int SCAN_RESULT_TYPE_FAILED = 0x323; //


    /*是否播放声音*/
    private boolean isPlayBeep = true;
    /*是否震动*/
    private boolean isShake = true;
    /*是否显示下方的其他功能布局*/
    private boolean isShowbottomLayout = true;
    /*是否显示闪光灯按钮*/
    private boolean isShowFlashLight = true;
    /*是否显示相册按钮*/
    private boolean isShowAlbum = true;
    /*是否解析条形码*/
    private boolean isDecodeBarCode = true;
    /*是否全屏扫描*/
    private boolean isFullScreenScan = true;
    // 是否显示扫码核销
    private boolean showVerification = false;
    // 是否显示扫码核销
    private int defaultScanType = SCAN_TYPE_QRCODE;


    /*四个角的颜色*/
    @ColorRes
    private int reactColor = R.color.react;
    /*扫描框颜色*/
    @ColorRes
    private int frameLineColor = -1;


    /*扫描线颜色*/
    @ColorRes
    private int scanLineColor = R.color.scanLineColor;

    public int getFrameLineColor() {
        return frameLineColor;
    }

    public void setFrameLineColor(@ColorRes int frameLineColor) {
        this.frameLineColor = frameLineColor;
    }

    public int getScanLineColor() {
        return scanLineColor;
    }

    public void setScanLineColor(@ColorRes int scanLineColor) {
        this.scanLineColor = scanLineColor;
    }

    public int getReactColor() {
        return reactColor;
    }

    public void setReactColor(@ColorRes int reactColor) {
        this.reactColor = reactColor;
    }

    public boolean isFullScreenScan() {
        return isFullScreenScan;
    }

    public void setFullScreenScan(boolean fullScreenScan) {
        isFullScreenScan = fullScreenScan;
    }

    public boolean isDecodeBarCode() {
        return isDecodeBarCode;
    }

    public void setDecodeBarCode(boolean decodeBarCode) {
        isDecodeBarCode = decodeBarCode;
    }

    public boolean isPlayBeep() {
        return isPlayBeep;
    }

    public void setPlayBeep(boolean playBeep) {
        isPlayBeep = playBeep;
    }

    public boolean isShake() {
        return isShake;
    }

    public void setShake(boolean shake) {
        isShake = shake;
    }

    public boolean isShowbottomLayout() {
        return isShowbottomLayout;
    }

    public void setShowbottomLayout(boolean showbottomLayout) {
        isShowbottomLayout = showbottomLayout;
    }

    public boolean isShowFlashLight() {
        return isShowFlashLight;
    }

    public void setShowFlashLight(boolean showFlashLight) {
        isShowFlashLight = showFlashLight;
    }

    public boolean isShowAlbum() {
        return isShowAlbum;
    }

    public void setShowAlbum(boolean showAlbum) {
        isShowAlbum = showAlbum;
    }

    public boolean isShowVerification() {
        return showVerification;
    }

    public void setShowVerification(boolean showVerification) {
        this.showVerification = showVerification;
    }

    public int getDefaultScanType() {
        return defaultScanType;
    }

    public void setDefaultScanType(int defaultScanType) {
        this.defaultScanType = defaultScanType;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isPlayBeep ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShake ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShowbottomLayout ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShowFlashLight ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isShowAlbum ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isDecodeBarCode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isFullScreenScan ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showVerification ? (byte) 1 : (byte) 0);
        dest.writeInt(this.defaultScanType);
        dest.writeInt(this.reactColor);
        dest.writeInt(this.frameLineColor);
        dest.writeInt(this.scanLineColor);
    }

    public ZxingConfig() {
    }

    protected ZxingConfig(Parcel in) {
        this.isPlayBeep = in.readByte() != 0;
        this.isShake = in.readByte() != 0;
        this.isShowbottomLayout = in.readByte() != 0;
        this.isShowFlashLight = in.readByte() != 0;
        this.isShowAlbum = in.readByte() != 0;
        this.isDecodeBarCode = in.readByte() != 0;
        this.isFullScreenScan = in.readByte() != 0;
        this.showVerification = in.readByte() != 0;
        this.defaultScanType = in.readInt();
        this.reactColor = in.readInt();
        this.frameLineColor = in.readInt();
        this.scanLineColor = in.readInt();
    }

    public static final Creator<ZxingConfig> CREATOR = new Creator<ZxingConfig>() {
        @Override
        public ZxingConfig createFromParcel(Parcel source) {
            return new ZxingConfig(source);
        }

        @Override
        public ZxingConfig[] newArray(int size) {
            return new ZxingConfig[size];
        }
    };
}

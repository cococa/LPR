package com.pcl.lpr1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.cocoa.plater.LPRActivity;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;


public class MainActivity extends AppCompatActivity {

    private final int REQUEST_LPR_CODE = 100;
    private final int REQUEST_CODE_SCAN = 111;

    private TextView message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestDangerousPermissions();

        message = findViewById(R.id.message);

        findViewById(R.id.brn_lpr).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, LPRActivity.class), REQUEST_LPR_CODE);
        });
        findViewById(R.id.qrcode).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            ZxingConfig config = new ZxingConfig();
            // config.setPlayBeep(false);//是否播放扫描声音 默认为true
            //  config.setShake(false);//是否震动  默认为true
            // config.setDecodeBarCode(false);//是否扫描条形码 默认为true
//                                config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
//                                config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
//                                config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
            config.setFullScreenScan(true);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
            config.setShowVerification(true);
            config.setDefaultScanType(ZxingConfig.SCAN_TYPE_QRCODE);
            intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        });

    }

    @SuppressLint("ShowToast")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 识别成功回调，车牌识别
        if (requestCode == REQUEST_LPR_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String card = data.getStringExtra("card");
                new AlertDialog.Builder(this)
                        .setMessage(card)
                        .setNegativeButton("OK", (dialog, which) -> {
                        })
                        .show();
//                Toast.makeText(this, card, Toast.LENGTH_SHORT);
            }
        } else if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                message.setText("扫描结果为：" + content);
            }
        } else if ((requestCode == REQUEST_CODE_SCAN || requestCode == REQUEST_LPR_CODE) && resultCode == RESULT_CANCELED) {

                message.setText("扫描不到？试试手动输入");

        }
    }

    /**
     * 请求权限
     */
    public void requestDangerousPermissions() {
        String[] strings = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, strings, 100);
    }
}

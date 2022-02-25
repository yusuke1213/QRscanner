package com.example.qrscanner;

import static android.media.AudioManager.STREAM_MUSIC;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    //描画用
    private SurfaceView surfaceView;
    // バーコード検出機
    private BarcodeDetector barcodeDetector;
    //カメラプレビュー用
    private CameraSource cameraSource;
    //許可要求のプレビュー　カメラ権限のリクエスト　何番もで可 1でも201でも200でも
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    //トーンオン用
    private ToneGenerator toneGen1;
    //13桁のバーコード番号表示用
    private TextView barcodeText;
    //バーコードデータ取得用
    private String barcodeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100); //音の設定。

        surfaceView = findViewById(R.id.surfaceView);
        barcodeText = findViewById(R.id.barcode_text);

        //バーコードスキャン関数。
        barcode();

    }

    private void barcode() {
        //バーコード検出器
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS) //バーコードフォーマット
                .build();                               //バーコードディテクタインスタンスを構築

        //カメラソース
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)  //オートフォーカス
                .build();                   //カメラ ソースのインスタンスを作成

        //描画のコールバックを受け取る
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        //カメラプレビュー開始
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
                cameraSource.release();
            }
        });

        //バーコード検出器に Processor をset
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            // バーコードを受け取った時
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    barcodeText.post(new Runnable() {
                        @Override
                        public void run() {
                            //読んだら止める
                            cameraSource.stop();
                            //バーコード取得
                            barcodeData = barcodes.valueAt(0).displayValue;
                            //データセット
                            barcodeText.setText(barcodeData);
                            //音を鳴らす
                            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                        }
                    });
                }
            }
        });
    }

    public void ReLoad(View view) {
        finish();
        startActivity(getIntent());
    }
}

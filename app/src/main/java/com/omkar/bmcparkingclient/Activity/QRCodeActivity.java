package com.omkar.bmcparkingclient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    @Override
    public void handleResult(Result result) {
        Toast.makeText(getApplicationContext(),result.getText(),Toast.LENGTH_LONG).show();
        Bundle b = new Bundle();
        b.putString("status", "done");
        b.putString("result",result.toString());
        Intent intent = new Intent();
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(QRCodeActivity.this); // Programmatically initialize the scanner view
        setContentView(mScannerView);
        mScannerView.setResultHandler(QRCodeActivity.this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();
    }
    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera(); // Stop camera on pause
    }


}

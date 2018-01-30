package com.omkar.bmcparkingclient;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //region DECLARATION
    public static final String TAG = MainActivity.class.getSimpleName();
    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mWriteTagFilters;
    private PendingIntent mNfcPendingIntent;

    private ImageView camera_iv;
    private Button discover_button;
    private Button write_button;
    private ImageButton image_button;
    private boolean isWrite = false;
    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region INITIALIZATION
        EditText vehicleNumber_et = findViewById(R.id.et_vehicleNumber);
        camera_iv  = findViewById(R.id.iv_car);
        discover_button = findViewById(R.id.button_discover);
        write_button = findViewById(R.id.button_write);
        image_button = findViewById(R.id.button_image);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //endregion
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);

        IntentFilter discovery = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        // Intent filters for writing to a tag
        mWriteTagFilters = new IntentFilter[] { discovery };
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onNewIntent(Intent intent) {

    }
}

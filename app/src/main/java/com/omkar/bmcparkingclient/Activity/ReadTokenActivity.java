package com.omkar.bmcparkingclient.Activity;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.omkar.bmcparkingclient.Helpers.PillowNfcManager;
import com.omkar.bmcparkingclient.Helpers.WriteTagHelper;
import com.omkar.bmcparkingclient.R;

import java.util.Date;

public class ReadTokenActivity extends AppCompatActivity {
    PillowNfcManager nfcManager;
    WriteTagHelper writeHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_token);
        nfcManager = new PillowNfcManager(this);
        nfcManager.onActivityCreate();
        nfcManager.setOnTagReadListener(new PillowNfcManager.TagReadListener() {
            @Override
            public void onTagRead(String tagRead) {
                Toast.makeText(ReadTokenActivity.this, "tag read:"+tagRead, Toast.LENGTH_LONG).show();
            }
        });
        writeHelper= new WriteTagHelper(this, nfcManager);
        nfcManager.setOnTagWriteErrorListener(writeHelper);
        nfcManager.setOnTagWriteListener(writeHelper);

        Button writeButton = (Button) findViewById(R.id.write_button);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = new Date().toString();
                writeHelper.writeText(text);
//				// If don't want to use the Write helper you can use the following code
//				nfcManager.writeText(text);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcManager.onActivityResume();
    }


    @Override
    protected void onPause() {
        nfcManager.onActivityPause();
        super.onPause();

    }
    @Override
    public void onNewIntent(Intent intent){
        nfcManager.onActivityNewIntent(intent);
    }


}

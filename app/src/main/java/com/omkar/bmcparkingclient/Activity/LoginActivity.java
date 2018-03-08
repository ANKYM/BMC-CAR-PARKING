package com.omkar.bmcparkingclient.Activity;

import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.omkar.bmcparkingclient.Helpers.ConnectionDetector;
import com.omkar.bmcparkingclient.Helpers.Encryption;
import com.omkar.bmcparkingclient.R;

import me.philio.pinentry.PinEntryView;

public class LoginActivity extends AppCompatActivity {
    private PinEntryView loginPinView;
    SharedPreferences userDetails;
    private static final String user_log_prefs = "User_Log";
    Encryption encryption;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginPinView = findViewById(R.id.loginPinView);
        encryption = Encryption.getDefault("Key", "random", new byte[16]);
        userDetails = getSharedPreferences(user_log_prefs, MODE_PRIVATE);
        userId = encryption.decryptOrNull(userDetails.getString("userId", ""));


        loginPinView.setOnPinEnteredListener(new PinEntryView.OnPinEnteredListener() {
            @Override
            public void onPinEntered(String pin) {
                if (ConnectionDetector.isConnected(getApplicationContext())) {

                } else {

                    Snackbar.make(getWindow().getDecorView().getRootView(), "Please Check Internet Connection", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }
            }
        });
    }

    private void LoginUser ()
    {

    }



}

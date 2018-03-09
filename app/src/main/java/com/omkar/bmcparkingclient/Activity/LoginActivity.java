package com.omkar.bmcparkingclient.Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.omkar.bmcparkingclient.Helpers.ConnectionDetector;
import com.omkar.bmcparkingclient.Helpers.Encryption;
import com.omkar.bmcparkingclient.Model.ParkingAttendant;
import com.omkar.bmcparkingclient.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import me.philio.pinentry.PinEntryView;

public class LoginActivity extends AppCompatActivity {
    private PinEntryView loginPinView;
    SharedPreferences userDetails;
    private static final String user_log_prefs = "User_Log";
    Encryption encryption;
    private String userId;
    private Dialog dialog;

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
                    try {
                        LoginUser ();
                    } catch (JSONException e) {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Please Check Internet Connection", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                    } catch (UnsupportedEncodingException e) {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Please Check Internet Connection", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                    }
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

    private void LoginUser () throws JSONException, UnsupportedEncodingException {
        JSONObject requestParams = new JSONObject();
        requestParams.put("userId", userId);
        requestParams.put("mobilePin", loginPinView.getText().toString());
        requestParams.put("deviceName","" );
        requestParams.put("deviceModel", "");
        StringEntity entity = new StringEntity(requestParams.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), "http://192.168.1.11:3660/Service.svc/LoginUsingPin", entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onStart() {
                dialog = ProgressDialog.show(LoginActivity.this, "Please Wait", "Please Wait", true);

            }

            @Override
            public void onRetry(int retryNo) {
                super.onRetry(retryNo);
            }

            @Override
            public void onCancel() {
                dialog.dismiss();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dialog.dismiss();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(new String(responseBody));
                    String jsonArrayString = jsonObject.getString("data");
                    if(!jsonArrayString.equals("No Records Found"))
                    {
                        try {
                            JSONArray jsonArray = new JSONArray(jsonArrayString);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject user = (JSONObject) jsonArray.get(i);
                                ParkingAttendant.setUserID(user.getString("user_id"));
                                ParkingAttendant.setUserRole( user.getInt("user_role"));
                                ParkingAttendant.setUserPermission(user.getInt("user_permission"));
                            }
                            Intent loginIntent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(loginIntent);
                            finish();
                        }
                        catch (Exception ex)
                        {
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Something Went Wrong.", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                        }

                    }else
                    {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Something Went Wrong.", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dialog.dismiss();
                Snackbar.make(getWindow().getDecorView().getRootView(), "Something Went Wrong.", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }
        });
    }



}

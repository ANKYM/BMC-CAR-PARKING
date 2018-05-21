package com.omkar.bmcparkingclient.Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jaredrummler.android.device.DeviceName;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.omkar.bmcparkingclient.Helpers.ConnectionDetector;
import com.omkar.bmcparkingclient.Helpers.Encryption;
import com.omkar.bmcparkingclient.Helpers.ServiceDetails;
import com.omkar.bmcparkingclient.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import me.philio.pinentry.PinEntryView;

public class SetPinActivity extends AppCompatActivity {
    private PinEntryView firstPinView, secondPinView;
    private Button setPinButton;
    String manufacturer = "";
    String model = "";
    String userId ="";
    private Dialog dialog;

    SharedPreferences userDetails;
    private static final String user_log_prefs = "User_Log";
    Encryption encryption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);
        firstPinView = (PinEntryView) findViewById(R.id.firstPinView);
        secondPinView = (PinEntryView) findViewById(R.id.secondPinView);
        setPinButton = (Button) findViewById(R.id.button_set_pin);
        firstPinView.addTextChangedListener(new MyTextWatcher(firstPinView));
        secondPinView.addTextChangedListener(new MyTextWatcher(secondPinView));
        encryption = Encryption.getDefault("Key", "random", new byte[16]);
        userDetails = getSharedPreferences(user_log_prefs, MODE_PRIVATE);
        userId = encryption.decryptOrNull(userDetails.getString("userId", ""));

        firstPinView.setOnPinEnteredListener(new PinEntryView.OnPinEnteredListener() {
            @Override
            public void onPinEntered(String pin) {

            }
        });
        secondPinView.setOnPinEnteredListener(new PinEntryView.OnPinEnteredListener() {
            @Override
            public void onPinEntered(String pin) {
                if (pin.equals(firstPinView.getText().toString())) {
                    setPinButton.setEnabled(true);
                } else {
                    secondPinView.clearText();
                    Toast.makeText(SetPinActivity.this, "Wrong PIN entered , please confirm PIN", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setPinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(ConnectionDetector.isConnected(getApplicationContext()))
                    {
                        RegisterPin();
                    }else
                    {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Please Check Your Internet Connection", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        DeviceName.with(getApplicationContext()).request(new DeviceName.Callback() {
            @Override
            public void onFinished(DeviceName.DeviceInfo info, Exception error) {
                try {
                    manufacturer = info.manufacturer;
                    model = info.model;
                } catch (Exception e) {

                }
            }
        });
    }
    //region TextChange listener to implementation required logic
    private class MyTextWatcher implements TextWatcher {
        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


        }

        @Override
        public void afterTextChanged(Editable editable) {

            int id = view.getId();
            switch (id) {
                case R.id.firstPinView:
                    String confirm_pin = secondPinView.getText().toString();
                    if (!TextUtils.isEmpty(confirm_pin)) {
                        secondPinView.clearText();
                    }
                    if (firstPinView.getText().toString().length() < 4) {
                        setPinButton.setEnabled(false);
                    }
                    break;

                case R.id.secondPinView:
                    String pin_entry = firstPinView.getText().toString();

                    if (TextUtils.isEmpty(pin_entry)) {
                        View focusView = null;
                        Toast.makeText(SetPinActivity.this, "Enter Pin first", Toast.LENGTH_SHORT).show();
                        focusView = firstPinView;
                        focusView.requestFocus();
                    }
                    if (secondPinView.getText().toString().length() < 4) {
                        setPinButton.setEnabled(false);
                    }
                    break;
            }
        }

    }
    //endregion

    private void RegisterPin() throws UnsupportedEncodingException, JSONException {
        JSONObject requestParams = new JSONObject();
        requestParams.put("userId", userId);
        requestParams.put("mobilePin", secondPinView.getText().toString());
        requestParams.put("deviceName",manufacturer );
        requestParams.put("deviceModel", model);
        StringEntity entity = new StringEntity(requestParams.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(60000);
        client.post(getApplicationContext(), ServiceDetails._URL+"SetLoginPin", entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onStart() {
                dialog = ProgressDialog.show(SetPinActivity.this, "Please Wait", "Please Wait", true);

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
                    String responseData = jsonObject.getString("data");
                    if(responseData.equals("true"))
                    {
                        userDetails = getSharedPreferences(user_log_prefs, MODE_PRIVATE);
                        SharedPreferences.Editor session_editor = userDetails.edit();
                        session_editor.putBoolean("isRegister", true);
                        session_editor.commit();
                        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(loginIntent);
                        finish();
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

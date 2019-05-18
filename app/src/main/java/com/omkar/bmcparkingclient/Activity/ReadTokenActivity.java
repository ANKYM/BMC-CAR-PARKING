package com.omkar.bmcparkingclient.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.omkar.bmcparkingclient.Helpers.DateFormatter;
import com.omkar.bmcparkingclient.Helpers.Encryption;
import com.omkar.bmcparkingclient.Helpers.PillowNfcManager;
import com.omkar.bmcparkingclient.Helpers.ServiceDetails;
import com.omkar.bmcparkingclient.Model.ParkingAttendant;
import com.omkar.bmcparkingclient.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.support.v4.content.ContextCompat.getDrawable;

public class ReadTokenActivity extends AppCompatActivity {
    PillowNfcManager nfcManager;
    Encryption encryption;
    Dialog dialog;
    JSONObject tokenParameters = null;
    CardView ll_nfc_warning, ll_park_details, ll_add_comments;
    TextView tv_vehicle_no, tv_owner_mobile_no, tv_check_time, tv_pre_booking_time, tv_issuer_id, tv_comments,tv_total_charges;
    ImageView iv_vehicle_type;
    MaterialEditText et_comments;
    Button Button_CheckOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_token);
        ll_nfc_warning = findViewById(R.id.ll_nfc_warning);
        ll_park_details = findViewById(R.id.ll_park_details);
        ll_add_comments = findViewById(R.id.ll_add_comments);
        tv_vehicle_no = findViewById(R.id.tv_vehicle_no);
        tv_owner_mobile_no = findViewById(R.id.tv_owner_mobile_no);
        tv_check_time = findViewById(R.id.tv_check_time);
        tv_pre_booking_time = findViewById(R.id.tv_pre_booking_time);
        iv_vehicle_type = findViewById(R.id.iv_vehicle_type);
        tv_issuer_id = findViewById(R.id.tv_issuer_id);
        tv_comments = findViewById(R.id.tv_comments);
        et_comments = findViewById(R.id.et_comments);
        tv_total_charges = findViewById(R.id.tv_total_charges);;

        Button_CheckOut = findViewById(R.id.Button_CheckOut);;
        nfcManager = new PillowNfcManager(this);
        nfcManager.onActivityCreate();
        encryption = Encryption.getDefault("Key", "random", new byte[16]);
        nfcManager.setOnTagReadListener(new PillowNfcManager.TagReadListener() {
            @Override
            public void onTagRead(String tagRead) {
                try {
                    tokenParameters = new JSONObject(tagRead);
                    if (tokenParameters.getString("lotID").equals(ParkingAttendant.getUserLotId())) {
                        GetParkedVehicleDetails();
                    } else {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Wrong Parking Lot Token", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ReadTokenActivity.this, "Unable TO Read Token", Toast.LENGTH_LONG).show();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }
        });

        Button_CheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    UpdateParkedVehicleDetails();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
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
    public void onNewIntent(Intent intent) {
        nfcManager.onActivityNewIntent(intent);
    }


    private void GetParkedVehicleDetails() throws JSONException, UnsupportedEncodingException {
        JSONObject requestParams = new JSONObject();
        requestParams.put("RFID", tokenParameters.getString("RfID"));
        StringEntity entity = new StringEntity(requestParams.toString());
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(getApplicationContext(), ServiceDetails._URL + "GetParkedVehicleDetails", entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onStart() {
                dialog = ProgressDialog.show(ReadTokenActivity.this, "Please Wait", "Please Wait", true);

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
                    if (!jsonArrayString.equals("No Records Found")) {
                        try {
                            JSONArray resultArray = new JSONArray(jsonArrayString);
                            JSONObject result = (JSONObject) resultArray.get(0);
                            populateData(result);
                        } catch (Exception ex) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Something Went Wrong.", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                        }

                    } else {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "No Data Found", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
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

    private void populateData(JSONObject result) throws JSONException {
        ll_nfc_warning.setVisibility(View.GONE);
        ll_park_details.setVisibility(View.VISIBLE);
        ll_add_comments.setVisibility(View.VISIBLE);
        tv_vehicle_no.setText(result.getString("vehicle_no"));
        tv_owner_mobile_no.setText(result.getString("vehicle_owner_mobile_no"));
        tv_check_time.setText(DateFormatter.returnDate(result.getString("check_in_time")));
        tv_pre_booking_time.setText(DateFormatter.returnDate(result.getString("prebooktimimg")));
        tv_issuer_id.setText(result.getString("parking_lot_issuer_id"));
        tv_total_charges.setText("\u20B9" + " " +result.getString( "Charges"));
        tv_comments.setText(result.getString("comments"));
        if (result.getString("vehicle_type").equals("Bus")) {
            iv_vehicle_type.setImageResource(R.drawable.bus);
        } else if (result.getString("vehicleType").equals("Bike")) {
            iv_vehicle_type.setImageResource(R.drawable.bike);
        } else {
            iv_vehicle_type.setImageResource(R.drawable.car);
        }

    }

    private void UpdateParkedVehicleDetails() throws JSONException, UnsupportedEncodingException {
        JSONObject requestParams = new JSONObject();
        requestParams.put("RFID", tokenParameters.getString("RfID"));
        requestParams.put("comments", et_comments.getText().toString());
        requestParams.put("parkingLotReciverId", ParkingAttendant.getUserID());
        StringEntity entity = new StringEntity(requestParams.toString());
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(getApplicationContext(), ServiceDetails._URL + "UpdateParkedVehicleDetails", entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onStart() {
                dialog = ProgressDialog.show(ReadTokenActivity.this, "Please Wait", "Please Wait", true);

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
                    if (!jsonArrayString.equals("false")) {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "SuccessFully Uploaded Details", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                        finish();
                    } else {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Unable To Upload", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
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

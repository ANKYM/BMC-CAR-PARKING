package com.omkar.bmcparkingclient.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.omkar.bmcparkingclient.Helpers.Encryption;
import com.omkar.bmcparkingclient.Helpers.GenerateRFID;
import com.omkar.bmcparkingclient.Helpers.NFCWriteException;
import com.omkar.bmcparkingclient.Helpers.PillowNfcManager;
import com.omkar.bmcparkingclient.Helpers.ServiceDetails;
import com.omkar.bmcparkingclient.Model.ParkingAttendant;
import com.omkar.bmcparkingclient.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class IssueTokenActivity extends AppCompatActivity implements PillowNfcManager.TagWriteErrorListener, PillowNfcManager.TagWriteListener {

    PillowNfcManager nfcManager;
    AlertDialog alertDialog;

    Encryption encryption;
    Button rbScan, ButtonIssueToken;
    Spinner spinner_type;
    String[] vehicle_types = {"Select Vehicle type", "Bike", "Car", "Bus"};
    String preBookFlag = "0",preBookTiming="",vehicleOwner="",RFID="";

    MaterialEditText et_OwnerMobileNO, et_vehicle_no,et_comments;
    public static final int QR_ACTIVITY = 0;
    Dialog dialog;
    int dialogViewId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcManager = new PillowNfcManager(this);
        nfcManager.onActivityCreate();
        nfcManager.setOnTagWriteErrorListener(this);
        nfcManager.setOnTagWriteListener(this);

        setContentView(R.layout.activity_issue_token);
        rbScan = findViewById(R.id.rbScan);
        et_OwnerMobileNO = findViewById(R.id.et_OwnerMobileNO);
        et_vehicle_no = findViewById(R.id.et_vehicle_no);
        et_comments = findViewById(R.id.et_comments);
        ButtonIssueToken = findViewById(R.id.ButtonIssueToken);
        spinner_type = findViewById(R.id.spinner_type);
        dialogViewId = R.layout.write_nfc_dialog_view;
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_dropdown_item,
                        vehicle_types);
        spinner_type.setAdapter(spinnerArrayAdapter);
        encryption = Encryption.getDefault("Key", "random", new byte[16]);
        rbScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IssueTokenActivity.this, QRCodeActivity.class);
                startActivityForResult(intent, QR_ACTIVITY);
            }
        });
        ButtonIssueToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(et_vehicle_no.getText().toString().length() < 9)) {
                    if (!(et_OwnerMobileNO.getText().toString().length() < 9)) {
                        if (!spinner_type.getSelectedItem().equals("Select Vehicle type")) {
                            try {
                                WriteNFCTag();
                            } catch (JSONException e) {
                                ServiceDetails.hideKeyboard(IssueTokenActivity.this);
                                Snackbar.make(getWindow().getDecorView().getRootView(), "Something Went Wrong ", Snackbar.LENGTH_LONG).show();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                        } else {
                            ServiceDetails.hideKeyboard(IssueTokenActivity.this);
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Please Select Vehicle Type", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        ServiceDetails.hideKeyboard(IssueTokenActivity.this);
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Please Enter 10 digit Mobile Number ", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    ServiceDetails.hideKeyboard(IssueTokenActivity.this);
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Please Enter 10 digit Vehicle Number ", Snackbar.LENGTH_LONG).show();
                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case QR_ACTIVITY:
                //region QR_ACTIVITY
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    JSONObject jsonObj;
                    String status = bundle.getString("status");
                    if (status.equalsIgnoreCase("done")) {
                        String result = bundle.getString("result");
                        try {
                            jsonObj = new JSONObject(result);
                            String decreedData = encryption.decryptOrNull(jsonObj.getString("EncryptedData"));
                            GetBookVehicleDetails(decreedData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    } else {

                    }
                }
                //endregion
                break;
        }
    }
    private void GetBookVehicleDetails(String Token) throws JSONException, UnsupportedEncodingException {
        JSONObject requestParams = new JSONObject();
        requestParams.put("QRCode", Token);
        StringEntity entity = new StringEntity(requestParams.toString());
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(getApplicationContext(), ServiceDetails._URL + "GetBookVehicleDetailsFromQRCode", entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onStart() {
                dialog = ProgressDialog.show(IssueTokenActivity.this, "Please Wait", "Please Wait", true);

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
                            JSONArray jsonArray = new JSONArray(jsonArrayString);
                            JSONObject BookingDetails = new JSONObject();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                BookingDetails = (JSONObject) jsonArray.get(i);
                            }
                            if (BookingDetails.getString("lot_id").equals(ParkingAttendant.getUserLotId())) {
                                if(BookingDetails.getInt("booking_status")==0) {
                                    populateDetails(BookingDetails);
                                }
                                else {
                                    ServiceDetails.hideKeyboard(IssueTokenActivity.this);
                                    Snackbar.make(getWindow().getDecorView().getRootView(), "Vehicle Already Parked", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                        }
                                    }).show();
                                }
                            } else {
                                ServiceDetails.hideKeyboard(IssueTokenActivity.this);
                                Snackbar.make(getWindow().getDecorView().getRootView(), "User Book Wrong Parking Lot", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).show();
                            }


                        } catch (Exception ex) {
                            ServiceDetails.hideKeyboard(IssueTokenActivity.this);
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Something Went Wrong.", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                        }

                    } else {
                        ServiceDetails.hideKeyboard(IssueTokenActivity.this);
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
    private void populateDetails(JSONObject BookingDetails) throws JSONException {
        et_OwnerMobileNO.setText(BookingDetails.getString("vehicle_owner_mobile_no"));
        et_vehicle_no.setText(BookingDetails.getString("vehicle_no"));
        spinner_type.setSelection(Arrays.asList(vehicle_types).indexOf(BookingDetails.getString("vehicle_type")));
        preBookTiming = BookingDetails.getString("booking_time");
        vehicleOwner = BookingDetails.getString("owner_id");
        preBookFlag = "1";
    }
    private void PutParkedVehicleDetails() throws JSONException, UnsupportedEncodingException {
        JSONObject requestParams = new JSONObject();
        requestParams.put("RfID", RFID);
        requestParams.put("lotID", ParkingAttendant.getUserLotId());
        requestParams.put("vehicleNo", et_vehicle_no.getText().toString());
        requestParams.put("preBookFlag", preBookFlag);
        requestParams.put("vehicleOwner", vehicleOwner);
        requestParams.put("vehicleOwnerMobileNo", et_OwnerMobileNO.getText().toString());
        requestParams.put("vehicleType", spinner_type.getSelectedItem());
        requestParams.put("preBookTiming", preBookTiming);
        requestParams.put("parkingLotIssuerID", ParkingAttendant.getUserID());
        requestParams.put("comments", et_comments.getText().toString());
        StringEntity entity = new StringEntity(requestParams.toString());
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(getApplicationContext(), ServiceDetails._URL + "PutParkedVehicleDetails", entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onStart() {
                dialog = ProgressDialog.show(IssueTokenActivity.this, "Please Wait", "Please Wait", true);

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
                        ServiceDetails.hideKeyboard(IssueTokenActivity.this);
                        Snackbar.make(getWindow().getDecorView().getRootView(), "SuccessFully Inserted", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                        finish();
                    } else {
                        ServiceDetails.hideKeyboard(IssueTokenActivity.this);
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Unable To Insert Details", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
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
                ServiceDetails.hideKeyboard(IssueTokenActivity.this);
                Snackbar.make(getWindow().getDecorView().getRootView(), "Something Went Wrong.", Snackbar.LENGTH_LONG).setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
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



    private void WriteNFCTag() throws JSONException, UnsupportedEncodingException {
        RFID = GenerateRFID.getRFID();
        JSONObject requestParams = new JSONObject();
        requestParams.put("RfID", RFID);
        requestParams.put("lotID", ParkingAttendant.getUserLotId());
        requestParams.put("vehicleNo", et_vehicle_no.getText().toString());
        requestParams.put("preBookFlag", preBookFlag);
        requestParams.put("vehicleOwner", vehicleOwner);
        requestParams.put("vehicleOwnerMobileNo", et_OwnerMobileNO.getText().toString());
        requestParams.put("vehicleType", spinner_type.getSelectedItem());
        requestParams.put("preBookTiming", preBookTiming);
        requestParams.put("parkingLotIssuerID", ParkingAttendant.getUserID());
        requestParams.put("comments", et_comments.getText().toString());
        String Data = String.valueOf(requestParams);
        writeText(Data);
    }

    public AlertDialog createWaitingDialog(){
        LayoutInflater inflater = LayoutInflater.from(IssueTokenActivity.this);
        View view = inflater.inflate(dialogViewId, null, false);
        ImageView image = new ImageView(IssueTokenActivity.this);
        image.setImageResource(R.drawable.ic_home_black_24dp);
        AlertDialog.Builder builder = new AlertDialog.Builder(IssueTokenActivity.this);
        builder.setTitle("Writing NFC TAg")
                .setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        nfcManager.undoWriteText();
                    }
                });
        return builder.create();
    }

    @Override
    public void onTagWritten() {
        dialog.dismiss();
        try {
            PutParkedVehicleDetails();
            Toast.makeText(IssueTokenActivity.this, "TAG Successfully Written", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onTagWriteError(NFCWriteException exception) {
        dialog.dismiss();
        Toast.makeText(IssueTokenActivity.this, exception.getType().toString(), Toast.LENGTH_LONG).show();
    }

    public void writeText(String text){
        alertDialog = createWaitingDialog();
        alertDialog.show();
        nfcManager.writeText(text);
    }

    @Override
    public void onNewIntent(Intent intent){
        nfcManager.onActivityNewIntent(intent);
    }

}

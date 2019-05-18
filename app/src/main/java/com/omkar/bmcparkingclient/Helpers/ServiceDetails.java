package com.omkar.bmcparkingclient.Helpers;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by omkar on 01-Feb-18.
 */

public class ServiceDetails {
    public final static String _URL = "http://18.222.25.225:5500/Service.svc/";
    //public final static String _URL = "http://192.168.1.12:5500/Service.svc/";
    public static void hideKeyboard(Activity activity) {
        View v = activity.getWindow().getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}

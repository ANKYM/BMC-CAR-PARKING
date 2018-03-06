package com.omkar.bmcparkingclient.Activity;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import com.chaos.view.PinView;
import com.omkar.bmcparkingclient.R;

public class SetPinActivity extends AppCompatActivity {
    PinView firstPinView, secondPinView;
    Button button_set_pin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);
        firstPinView = (PinView) findViewById(R.id.firstPinView);
        secondPinView = (PinView) findViewById(R.id.secondPinView);
        button_set_pin = findViewById(R.id.button_set_pin);
        firstPinView.setAnimationEnable(true);
        secondPinView.setAnimationEnable(true);
        firstPinView.addTextChangedListener(new MyTextWatcher(firstPinView));
        secondPinView.addTextChangedListener(new MyTextWatcher(firstPinView));
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            int id = view.getId();
            switch (id) {
                case R.id.firstPinView:
                    String confirm_pin = firstPinView.getText().toString();
                    if (!TextUtils.isEmpty(confirm_pin)) {
                        secondPinView.setText("");
                        secondPinView.setEnabled(true);
                    }

                    break;
                case R.id.secondPinView:
                    String second_pin = firstPinView.getText().toString();
                    if (!TextUtils.isEmpty(second_pin)) {
                        secondPinView.setText("");
                    }
                    break;
            }
        }
    }
}

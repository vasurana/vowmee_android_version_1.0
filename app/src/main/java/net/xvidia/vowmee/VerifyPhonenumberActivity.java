package net.xvidia.vowmee;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Ravi_office on 06-Dec-15.
 */
public class VerifyPhonenumberActivity extends AppCompatActivity {


    private EditText mPhonenumberEditText;
    private Button sendOtpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        mPhonenumberEditText = (EditText) findViewById(R.id.register_phone_number);
        sendOtpButton = (Button) findViewById(R.id.button_send_otp);
        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VerifyPhonenumberActivity.this, RegisterActivity.class));
                finish();
            }
        });

        mPhonenumberEditText.setText(getMy10DigitPhoneNumber());
    }
    private String getMyPhoneNumber(){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    private String getMy10DigitPhoneNumber(){
        String s = getMyPhoneNumber();
        return s != null && s.length() > 2 ? s : null;
    }
}

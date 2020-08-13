package com.dalilu.ui.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.dalilu.R;
import com.dalilu.databinding.ActivityRegisterPhoneNumberBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.dalilu.utils.LanguageManager;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import java.util.Objects;

public class RegisterPhoneNumberActivity extends AppCompatActivity {
    private static final String TAG = "Register Phone";
    ActivityRegisterPhoneNumberBinding activityRegisterPhoneNumberBinding;
    Spinner languageSelectSpinner;
    TextInputLayout txtNumber;
    CountryCodePicker countryCodePicker;
    ProgressBar loading;
    Button btnNext;
    String getPhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityRegisterPhoneNumberBinding = DataBindingUtil.setContentView(this, R.layout.activity_register_phone_number);

        languageSelectSpinner = activityRegisterPhoneNumberBinding.spinner;
        txtNumber = activityRegisterPhoneNumberBinding.textInputLayoutPhone;
        loading = activityRegisterPhoneNumberBinding.pbLoading;
        btnNext = activityRegisterPhoneNumberBinding.btnNext;
        countryCodePicker = activityRegisterPhoneNumberBinding.ccp;
        countryCodePicker.registerCarrierNumberEditText(txtNumber.getEditText());
        countryCodePicker.setNumberAutoFormattingEnabled(true);

        activityRegisterPhoneNumberBinding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    DisplayViewUI.displayToast(RegisterPhoneNumberActivity.this, getString(R.string.selectLanguage));
                    btnNext.setEnabled(false);

                } else if (position == 1) {//english is selected
                    btnNext.setEnabled(true);
                    LanguageManager.setNewLocale(RegisterPhoneNumberActivity.this, LanguageManager.LANGUAGE_KEY_ENGLISH);
                    // TODO: 7/31/2020  fix app language
                    // recreate();

                } else if (position == 2) {//french is selected
                    btnNext.setEnabled(true);
                    LanguageManager.setNewLocale(RegisterPhoneNumberActivity.this, LanguageManager.LANGUAGE_KEY_FRENCH);
                    // recreate();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (btnNext.isEnabled()) {
            btnNext.setOnClickListener(view -> {
                new Handler().postDelayed(() -> loading.setVisibility(View.VISIBLE), 2000);

                getPhoneNumber();
            });

        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: " + " granted");
            } else finish();
        }
    }

    private void getPhoneNumber() {

        String getPhoneNumber = Objects.requireNonNull(txtNumber.getEditText()).getText().toString();
        if (!getPhoneNumber.trim().isEmpty()) {
            if (DisplayViewUI.isNetworkConnected(RegisterPhoneNumberActivity.this)) {
                getPhone = countryCodePicker.getFormattedFullNumber();
                Intent verifyNumberIntent = new Intent(RegisterPhoneNumberActivity.this, VerifyPhoneNumberActivity.class);
                verifyNumberIntent.putExtra(AppConstants.PHONE_NUMBER, getPhone);
                startActivity(verifyNumberIntent);

            } else {
                DisplayViewUI.displayAlertDialogMsg(RegisterPhoneNumberActivity.this, getResources().getString(R.string.noInternet), "ok",
                        (dialog, which) -> dialog.dismiss());
            }
        } else if (getPhoneNumber.trim().isEmpty()) {
            txtNumber.setErrorEnabled(true);
            txtNumber.setError(getString(R.string.phoneReq));
        } else {
            txtNumber.setErrorEnabled(false);
        }
    }
}
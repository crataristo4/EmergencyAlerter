package com.dalilu.clickhandler;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.dalilu.R;
import com.dalilu.ui.auth.VerifyPhoneNumberActivity;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import java.util.Objects;

public class ItemClickHandler {
    CountryCodePicker countryCodePicker;
    // Spinner languageSelectSpinner;
    Button btnNext;
    private Context context;
    private TextInputLayout txtPhoneNumber;
    private ProgressBar pbLoading;

    public ItemClickHandler() {
    }

    public ItemClickHandler(Context context, TextInputLayout txtPhoneNumber,
                            CountryCodePicker countryCodePicker, ProgressBar pbLoading,
                            Button btnNext) {
        this.context = context;
        this.txtPhoneNumber = txtPhoneNumber;
        this.pbLoading = pbLoading;
        this.countryCodePicker = countryCodePicker;
        //  this.languageSelectSpinner = languageSelectSpinner;
        this.btnNext = btnNext;
    }

    public void startPhoneRegistration(View view) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                if (btnNext.isEnabled()) {
                    pbLoading.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(() -> validateInputs(view), 3000);


                }


            } else {
                DisplayViewUI.displayAlertDialogMsg(context, context.getString(R.string.noInternet), "ok",
                        (dialog, which) -> dialog.dismiss());
            }

        }


    }


    private void validateInputs(final View view) {

/*
        languageSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {

                    btnNext.setEnabled(false);

                } else if (position == 1) {//english is selected
                    btnNext.setEnabled(true);
                    LanguageManager.setNewLocale(context, LanguageManager.LANGUAGE_KEY_ENGLISH);
                    // TODO: 7/31/2020  fix app language
                    // recreate();

                } else if (position == 2) {//french is selected
                    btnNext.setEnabled(true);
                    LanguageManager.setNewLocale(context, LanguageManager.LANGUAGE_KEY_FRENCH);
                    // recreate();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
*/


        countryCodePicker.registerCarrierNumberEditText(txtPhoneNumber.getEditText());
        countryCodePicker.setNumberAutoFormattingEnabled(true);


        String phoneNumber = Objects.requireNonNull(txtPhoneNumber.getEditText()).getText().toString();

        if (!phoneNumber.trim().isEmpty() && phoneNumber.length() > 8) {
            String getFullFormatPhoneNumber = countryCodePicker.getFormattedFullNumber();
            Intent verifyNumberIntent = new Intent(context, VerifyPhoneNumberActivity.class);
            verifyNumberIntent.putExtra(AppConstants.PHONE_NUMBER, getFullFormatPhoneNumber);
            verifyNumberIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(verifyNumberIntent);

        } else if (phoneNumber.length() < 9) {
            pbLoading.setVisibility(View.GONE);
            txtPhoneNumber.setErrorEnabled(true);
            txtPhoneNumber.setError(context.getResources().getString(R.string.invalidPhone));
        } else if (phoneNumber.trim().isEmpty()) {
            pbLoading.setVisibility(View.GONE);
            txtPhoneNumber.setErrorEnabled(true);
            txtPhoneNumber.setError(context.getResources().getString(R.string.phoneReq));
        } else {

            pbLoading.setVisibility(View.GONE);

            txtPhoneNumber.setErrorEnabled(false);
        }

    }

}

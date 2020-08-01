package com.dalilu.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.dalilu.BaseActivity;
import com.dalilu.FinishAccountSetupActivity;
import com.dalilu.R;
import com.dalilu.databinding.ActivityPhoneAuthBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.dalilu.utils.LanguageManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends BaseActivity {

    ActivityPhoneAuthBinding activityPhoneAuthBinding;
    String phoneNumber = "+16505554572";
    String smsCode = "123456";
    Button btnDone, btnVerify;
    private String mVerificationCode;
    private PhoneAuthProvider.ForceResendingToken mToken;
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationCode = s;
            mToken = forceResendingToken;

        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //this method automatically handles the code sent
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                Objects.requireNonNull(txtVerifyCode.getEditText()).setText(code);
                activityPhoneAuthBinding.btnVerify.setText(R.string.plsWait);

                // verifyCode(code);
            } else {
                activityPhoneAuthBinding.btnVerify.setText(R.string.verify);
            }


        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
                DisplayViewUI.displayToast(PhoneAuthActivity.this, e.getMessage());
                activityPhoneAuthBinding.txtResendCode.setVisibility(View.VISIBLE);


            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
                DisplayViewUI.displayToast(PhoneAuthActivity.this, e.getMessage());
                activityPhoneAuthBinding.txtResendCode.setVisibility(View.GONE);


            }
            DisplayViewUI.displayToast(PhoneAuthActivity.this, e.getMessage());
            activityPhoneAuthBinding.txtResendCode.setVisibility(View.VISIBLE);
        }
    };
    private CountryCodePicker countryCodePicker;
    private ProgressBar loading;
    private TextInputLayout txtPhoneNumber, txtVerifyCode;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private String uid;
    private String getPhone, getPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(this::configureFakenUMBER, 5000);


        activityPhoneAuthBinding = DataBindingUtil.setContentView(this, R.layout.activity_phone_auth);
        btnDone = activityPhoneAuthBinding.btnRegisterPhoneNumber;
        countryCodePicker = activityPhoneAuthBinding.ccp;
        loading = activityPhoneAuthBinding.progressBarVerify;
        txtPhoneNumber = activityPhoneAuthBinding.textInputLayoutPhone;
        txtVerifyCode = activityPhoneAuthBinding.textInputLayoutConfirmCode;
        countryCodePicker.registerCarrierNumberEditText(txtPhoneNumber.getEditText());
        countryCodePicker.setNumberAutoFormattingEnabled(true);

        activityPhoneAuthBinding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    DisplayViewUI.displayToast(PhoneAuthActivity.this, getString(R.string.selectLanguage));
                    btnDone.setEnabled(false);

                } else if (position == 1) {//english is selected
                    btnDone.setEnabled(true);
                    LanguageManager.setNewLocale(PhoneAuthActivity.this, LanguageManager.LANGUAGE_KEY_ENGLISH);
                    // TODO: 7/31/2020  fix app language
                    // recreate();

                } else if (position == 2) {//french is selected
                    btnDone.setEnabled(true);
                    LanguageManager.setNewLocale(PhoneAuthActivity.this, LanguageManager.LANGUAGE_KEY_FRENCH);
                    // recreate();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btnDone.setOnClickListener(v -> {
            if (btnDone.isEnabled()) {

                getPhoneNumber = Objects.requireNonNull(txtPhoneNumber.getEditText()).getText().toString();
                if (!getPhoneNumber.trim().isEmpty()) {

                    if (DisplayViewUI.isNetworkConnected(PhoneAuthActivity.this)) {
                        getPhone = countryCodePicker.getFormattedFullNumber();
                        sendVerificationCode(phoneNumber);
                        // TODO: 19-Jul-20
                        showHideLayout();
                    } else {
                        DisplayViewUI.displayAlertDialogMsg(PhoneAuthActivity.this, getResources().getString(R.string.noInternet), "ok",
                                (dialog, which) -> dialog.dismiss());
                    }
                } else if (getPhoneNumber.trim().isEmpty()) {
                    txtPhoneNumber.setErrorEnabled(true);
                    txtPhoneNumber.setError(getString(R.string.phoneReq));
                } else {
                    txtPhoneNumber.setErrorEnabled(false);
                }
            }
        });

        activityPhoneAuthBinding.btnVerify.setOnClickListener(v -> {
            String getCodeFromUser = Objects.requireNonNull(txtVerifyCode.getEditText()).getText().toString();
            if (!getCodeFromUser.trim().isEmpty() && getCodeFromUser.length() == 6) {
                verifyCode(smsCode); // TODO: 22-Apr-20  change args to input

            }

            if (getCodeFromUser.length() < 6) {
                txtVerifyCode.setErrorEnabled(true);
                txtVerifyCode.setError(getString(R.string.codeShort));
            }


        });

        activityPhoneAuthBinding.txtResendCode.setOnClickListener(v -> resendToken(getPhone, mToken));

    }

    private void configureFakenUMBER() {
        // Configure faking the auto-retrieval with the whitelisted numbers.
        FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();

        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode);

        PhoneAuthProvider phoneAuthProvider = PhoneAuthProvider.getInstance();
        phoneAuthProvider.verifyPhoneNumber(
                phoneNumber,
                60L,
                TimeUnit.SECONDS,
                this, //activity *//*
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Instant verification is applied and a credential is directly returned.
                        // ...
                        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                loading.setVisibility(View.GONE);

                                user = firebaseAuth.getCurrentUser();
                                assert user != null;
                                uid = user.getUid();

                                Intent intent = new Intent(PhoneAuthActivity.this, FinishAccountSetupActivity.class);
                                intent.putExtra(AppConstants.UID, uid);
                                intent.putExtra(AppConstants.PHONE_NUMBER, user.getPhoneNumber());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();


                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                loading.setVisibility(View.GONE);
                                activityPhoneAuthBinding.txtResendCode.setVisibility(View.VISIBLE);
                                DisplayViewUI.displayToast(PhoneAuthActivity.this, task.getException().getMessage());
                            }

                        });

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                });

    }

    private void showHideLayout() {
        loading.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            loading.setVisibility(View.GONE);
            activityPhoneAuthBinding.constrainLayoutConfrimNumber.setVisibility(View.GONE);
            activityPhoneAuthBinding.constrainLayoutVerifyPhone.setVisibility(View.VISIBLE);
        }, 10000);
    }

    private void sendVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,   // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
    }

    private void verifyCode(String code) {
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationCode, code);

        signInWithCredentials(phoneAuthCredential);
    }

    private void resendToken(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, mCallbacks, token);

    }

    private void signInWithCredentials(PhoneAuthCredential phoneAuthCredential) {

        loading.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                loading.setVisibility(View.GONE);

                user = firebaseAuth.getCurrentUser();
                uid = firebaseAuth.getUid();

                Intent intent = new Intent(PhoneAuthActivity.this, FinishAccountSetupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();


            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                loading.setVisibility(View.GONE);
                activityPhoneAuthBinding.txtResendCode.setVisibility(View.VISIBLE);
                DisplayViewUI.displayToast(PhoneAuthActivity.this, task.getException().getMessage());
            }

        });

    }

    @Override
    public void onBackPressed() {
    }


}
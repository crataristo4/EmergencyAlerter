package com.dalilu.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.databinding.DataBindingUtil;

import com.dalilu.FinishAccountSetupActivity;
import com.dalilu.R;
import com.dalilu.databinding.ActivityVerifyPhoneNumberBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumberActivity extends AppCompatActivity {
    private static String code = "";
    ActivityVerifyPhoneNumberBinding activityVerifyPhoneNumberBinding;
    FirebaseAuth mAuth;
    AppCompatEditText edtCode;
    String phoneNumber;
    private FirebaseUser user;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityVerifyPhoneNumberBinding = DataBindingUtil.setContentView(this, R.layout.activity_verify_phone_number);


        Intent intent = getIntent();
        if (intent != null) {
            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();
            assert user != null;
            uid = user.getUid();
            phoneNumber = intent.getStringExtra(AppConstants.PHONE_NUMBER);
            activityVerifyPhoneNumberBinding.phone.setText(phoneNumber);
        }

        edtCode = activityVerifyPhoneNumberBinding.code;

        edtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 6) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(code, s.toString());
                    mAuth.signInWithCredential(credential).addOnSuccessListener(authResult -> {
                        Intent intent12 = new Intent(VerifyPhoneNumberActivity.this, FinishAccountSetupActivity.class);
                        intent12.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
                        intent12.putExtra(AppConstants.UID, uid);
                        startActivity(intent12);
                        finish();
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            DisplayViewUI.displayToast(VerifyPhoneNumberActivity.this, e.getMessage());
                        }
                    });
                }

            }
        });

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        mAuth.signInWithCredential(credential).addOnCompleteListener(VerifyPhoneNumberActivity.this,
                                task -> {
                                    Intent intent1 = new Intent(VerifyPhoneNumberActivity.this, FinishAccountSetupActivity.class);
                                    intent1.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
                                    intent1.putExtra(AppConstants.UID, uid);
                                    startActivity(intent1);
                                    finish();
                                });
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        DisplayViewUI.displayToast(VerifyPhoneNumberActivity.this, e.getMessage());
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        code = s;
                    }
                });

    }

    public void wrongNumber(View view) {
        onBackPressed();
    }
}
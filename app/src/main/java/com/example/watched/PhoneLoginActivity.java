package com.example.watched;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.core.Tag;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText edtPhone, edtVerification;
    private Button btnSend, btnVerify;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        init();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = edtPhone.getText().toString();

                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "phone number is requied", Toast.LENGTH_SHORT).show();
                }else {
                    loadingBar.setTitle("Phone verification");
                    loadingBar.setMessage("please wait...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this,
                        "Invalid phone number, please enter connect phone number with your country code...",
                        Toast.LENGTH_SHORT).show();

                edtPhone.setVisibility(View.VISIBLE);
                btnSend.setVisibility(View.VISIBLE);

                edtVerification.setVisibility(View.INVISIBLE);
                btnVerify.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check verify...", Toast.LENGTH_SHORT).show();

                edtPhone.setVisibility(View.INVISIBLE);
                btnSend.setVisibility(View.INVISIBLE);

                edtVerification.setVisibility(View.VISIBLE);
                btnVerify.setVisibility(View.VISIBLE);
            }
        };
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtPhone.setVisibility(View.INVISIBLE);
                btnSend.setVisibility(View.INVISIBLE);

                String verificationCode = edtVerification.getText().toString();
                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "please write verification code first...", Toast.LENGTH_SHORT).show();
                }
                else {
                    loadingBar.setTitle("verification code");
                    loadingBar.setMessage("please wait, while we are verifying veritication code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void init() {
        edtPhone = findViewById(R.id.edt_phone);
        edtVerification = findViewById(R.id.edt_verification);
        btnSend = findViewById(R.id.btn_send);
        btnVerify = findViewById(R.id.btn_verify);
        loadingBar = new ProgressDialog(this);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "congratulation, you're logged in successfully...", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            String mesage = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "error: " + mesage, Toast.LENGTH_SHORT).show();
                        }
                    }

                    private void sendUserToMainActivity() {
                        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    }
                });
    }
}

package com.example.watched.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watched.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnPhone;
    private TextView tvForgetPassword, tvNeedNewAccount;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private DatabaseReference userRef;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        tvNeedNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(LoginActivity.this, "please enter email", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText( LoginActivity.this, "please enter password", Toast.LENGTH_SHORT).show();
                }else {
                    loadingBar.setTitle("Sign in");
                    loadingBar.setMessage("please wait...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){

                                        String currentUserId = mAuth.getCurrentUser().getUid();
                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                        userRef.child(currentUserId).child("device_token")
                                                .setValue(deviceToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            SendUserToMainActivity();
                                                            Toast.makeText(LoginActivity.this, "logged is successful...", Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });
                                        }
                                            else {
                                                String massage = task.getException().toString();
                                                Toast.makeText(LoginActivity.this, "error: " + massage, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                            }
                    });
                }
            }
        });
       btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneIntent);
            }
        });
    }
    private void init() {
        btnLogin = findViewById(R.id.btn_login);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        tvForgetPassword = findViewById(R.id.tv_forget_password);
        tvNeedNewAccount = findViewById(R.id.tv_need_new_account);

        btnPhone = findViewById(R.id.btn_Phone);
        loadingBar = new ProgressDialog(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null){
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}

package com.example.watched;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btncreateAccount;
    private TextView tvAlreadyHaveAnAccount;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private DatabaseReference rootRef;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();

        mAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseDatabase.getInstance().getReference();

        tvAlreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });
        btncreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }
    private void init() {
        edtEmail = findViewById(R.id.edit_text_email);
        edtPassword = findViewById(R.id.edit_text_password);
        btncreateAccount = findViewById(R.id.btn_create_account);
        tvAlreadyHaveAnAccount = findViewById(R.id.tv_already_have_an_account);

        loadingBar = new ProgressDialog(this);
    }

    private void CreateNewAccount(){
         String email = edtEmail.getText().toString();
         String password = edtPassword.getText().toString();

         if (TextUtils.isEmpty(email)){
             Toast.makeText(RegisterActivity.this, "please enter email", Toast.LENGTH_SHORT).show();
         }
         if (TextUtils.isEmpty(password)){
             Toast.makeText(RegisterActivity.this, "please enter password", Toast.LENGTH_SHORT).show();
         }
         else {
             loadingBar.setTitle("Creating new Account");
             loadingBar.setMessage("please wait, while we are creating new account for you...");
             loadingBar.setCanceledOnTouchOutside(true);
             loadingBar.show();
             mAuth.createUserWithEmailAndPassword(email, password)
                     .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                         @Override
                         public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful() == true){

                                String currentUserId = mAuth.getCurrentUser().getUid();
                                rootRef.child("User").child(currentUserId).setValue("");

                                //SendUserToLoginActivity();
                                SenUserToMainActivity();
                                Toast.makeText(getApplicationContext(), "Account create successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else {
                                String massege = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "error:" + massege, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                         }
                     });
         }
    }
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
    private void SenUserToMainActivity(){
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

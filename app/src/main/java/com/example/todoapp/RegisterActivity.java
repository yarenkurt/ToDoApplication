package com.example.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText registerEmail,registerPassword;
    private Button registerButton;
    private FirebaseAuth mAuth;

    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        toolbar = findViewById(R.id.registerToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register Page");

        mAuth = FirebaseAuth.getInstance() ;
        loader = new ProgressDialog(this);

        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registerEmail.getText().toString().trim();
                String password= registerPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    registerEmail.setError("Email is required!!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    registerPassword.setError("Password is required!!");
                    return;
                }else{
                    loader.setMessage("Registration in Progress!");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent(RegisterActivity.this,HomeActivity.class);
                                startActivity(intent);
                                finish();
                                loader.dismiss();
                            }else {
                                String error = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Registration Failed!"+ error, Toast.LENGTH_SHORT).show();
                                loader.dismiss();

                            }
                        }
                    });
                }
            }
        });
    }
}
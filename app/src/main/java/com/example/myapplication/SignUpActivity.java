package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.User;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialCardView btnSignup;
    private TextView tvGoLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        init();
    }

    private void init() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignup = findViewById(R.id.btn_signup);
        tvGoLogin = findViewById(R.id.tv_go_login);

        btnSignup.setOnClickListener(v -> registerUser());

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = authResult.getUser().getUid();

                    User user = new User(uid, name, email, "", System.currentTimeMillis());
                    usersRef.child(uid).setValue(user)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Sign Up Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

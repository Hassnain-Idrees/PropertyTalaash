package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        mAuth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);

        loadUserName();

        findViewById(R.id.btn_drawer_add_property).setOnClickListener(v -> {
            drawerLayout.close();
            startActivity(new Intent(this, AddPropertyActivity.class));
        });

        findViewById(R.id.tv_logout).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        TextView tvName = findViewById(R.id.tv_drawer_name);
        TextView tvContact = findViewById(R.id.tv_drawer_contact);

        if (tvContact != null) tvContact.setText(user.getEmail());

        FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            if (tvName != null && name != null) {
                                tvName.setText(name);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }
}
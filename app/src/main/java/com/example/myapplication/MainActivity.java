package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.fragments.FavoritesFragment;
import com.example.myapplication.fragments.HomeFragment;
import com.example.myapplication.fragments.SearchResultsFragment;
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
        setupDrawerActions();

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
        ImageView ivAvatar = findViewById(R.id.iv_drawer_avatar);

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
                            if (ivAvatar != null) {
                                String picPath = snapshot.child("profilePic").getValue(String.class);
                                if (picPath != null && !picPath.isEmpty()) {
                                    java.io.File f = new java.io.File(picPath);
                                    if (f.exists()) {
                                        Glide.with(MainActivity.this)
                                                .load(f)
                                                .circleCrop()
                                                .error(R.drawable.default_profile)
                                                .into(ivAvatar);
                                    } else {
                                        Glide.with(MainActivity.this)
                                                .load(picPath)
                                                .circleCrop()
                                                .error(R.drawable.default_profile)
                                                .into(ivAvatar);
                                    }
                                } else {
                                    Glide.with(MainActivity.this)
                                            .load(R.drawable.default_profile)
                                            .circleCrop()
                                            .into(ivAvatar);
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }

    private void setupDrawerActions() {
        View itemHome = findViewById(R.id.item_home);
        View itemSearch = findViewById(R.id.item_search);
        View itemFavorites = findViewById(R.id.item_favorites);
        View itemSaved = findViewById(R.id.item_saved_searches);
        View itemAbout = findViewById(R.id.item_about);
        View itemContact = findViewById(R.id.item_contact);
        View itemTerms = findViewById(R.id.item_terms);

        if (itemHome != null) {
            itemHome.setOnClickListener(v -> {
                drawerLayout.close();
                openFragment(new HomeFragment());
            });
        }

        if (itemSearch != null) {
            itemSearch.setOnClickListener(v -> {
                drawerLayout.close();
                openFragment(new SearchResultsFragment());
            });
        }

        if (itemFavorites != null) {
            itemFavorites.setOnClickListener(v -> {
                drawerLayout.close();
                openFragment(new FavoritesFragment());
            });
        }

        if (itemSaved != null) {
            itemSaved.setOnClickListener(v -> {
                drawerLayout.close();
                openFragment(new SearchResultsFragment());
                Toast.makeText(this, "Saved searches will appear here.", Toast.LENGTH_SHORT).show();
            });
        }

        if (itemAbout != null) {
            itemAbout.setOnClickListener(v -> {
                drawerLayout.close();
                startActivity(new Intent(this, AboutUsActivity.class));
            });
        }

        if (itemContact != null) {
            itemContact.setOnClickListener(v -> {
                drawerLayout.close();
                startActivity(new Intent(this, ContactUsActivity.class));
            });
        }

        if (itemTerms != null) {
            itemTerms.setOnClickListener(v -> {
                drawerLayout.close();
                startActivity(new Intent(this, TermsActivity.class));
            });
        }
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
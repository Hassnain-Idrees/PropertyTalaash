package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FavHelper {

    public static SharedPreferences getPrefs(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user != null) ? user.getUid() : "guest";
        return context.getSharedPreferences("favorites_" + uid, 0);
    }
}


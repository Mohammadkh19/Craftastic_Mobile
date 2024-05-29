package com.example.craftastic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;

public class LoginRedirecting {

    public static boolean redirectToLogin(Context context){
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent loginIntent = new Intent(context, Login.class);
            String className = context.getClass().getSimpleName();
            loginIntent.putExtra("REDIRECT_TO", className);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(loginIntent);
            return true;
        }
        return false;
    }
}

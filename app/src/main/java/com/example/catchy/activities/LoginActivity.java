package com.example.catchy.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.catchy.R;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import co.revely.gradient.RevelyGradient;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignup;
    private ImageView ivDisc;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        if (ParseUser.getCurrentUser() != null) {
            goSpotifyAuth(true);
        }


        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        ivDisc = findViewById(R.id.ivDisc);
        layout = findViewById(R.id.layout);

        Glide.with(this)
                .load(R.drawable.blue_disc)
                .into(ivDisc);

        RevelyGradient
                .linear()
                .colors(new int[]{Color.parseColor("#000000"), Color.parseColor("#00EDFF")}).angle(270f).alpha(0.13f)
                .onBackgroundOf(layout);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick login button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick signup button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                signUpUser(username, password);
            }
        });

    }

    private void goSpotifyAuth(boolean returning) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("returning", returning);
        Log.d(TAG, "Entering main activity");
        startActivity(i);
        finish();
    }

    private void signUpUser(String username, String password) {
        Log.i(TAG, "Attempting to sign up user " + username);
        ParseUser newUser = new ParseUser();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(LoginActivity.this, "Successfully signed up", Toast.LENGTH_SHORT).show();
                    goMainActivity();
                    goSpotifyAuth(false);
                } else {
                    Toast.makeText(LoginActivity.this, "Issue with sign up", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(LoginActivity.this, "Issue with login", Toast.LENGTH_SHORT).show();
                    return;
                }
                goSpotifyAuth(false);
                Toast.makeText(LoginActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

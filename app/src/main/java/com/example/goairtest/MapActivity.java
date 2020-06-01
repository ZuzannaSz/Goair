package com.example.goairtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MapActivity extends Activity {
    private Button login, home, settings;
    private TextView loginText;
    private FirebaseAuth auth;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        setup();
        authView();
        handleLogin();
        navigation();
    }
    public void navigation()
    {
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MapActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
    public void setup()
    {
        login = findViewById(R.id.loginMain);
        loginText = findViewById(R.id.loginView);
        home = findViewById(R.id.homeButton);
        settings = findViewById(R.id.settingsButton);
    }
    public void handleLogin()
    {
        login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FirebaseUser currentUser = auth.getCurrentUser();
                if(currentUser==null)
                {

                    Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                else
                {
                    //popup here asking if you want to log out
                    AuthUI.getInstance().signOut(MapActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(MapActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                                    login.setText("Login");
                                    loginText.setText("");
                                }
                            });
                }
            }
        });
    }
    public void authView() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser==null) {
            login.setText("Login");
        }
        else {
            login.setText("Logout");
            loginText.setText(currentUser.getEmail());
        }
    }
}

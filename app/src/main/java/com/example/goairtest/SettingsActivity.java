package com.example.goairtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends Activity {
    private Button login, map, home;
    private TextView loginText;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ImageView img;
    private TextView username, email;
    private DatabaseHandler db;
    private Context context;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        setup();
        authView();
        handleLogin();
        navigation();
        db = new DatabaseHandler(this);
        context = this;
        img = findViewById(R.id.profileSet);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            Uri uri = user.getPhotoUrl();
            //Toast.makeText(SettingsActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();
            if(uri!=null)
            {
                Glide.with(this).load(uri).into(img);
            }
            username = findViewById(R.id.nameSet);
            email = findViewById(R.id.mail);
            username.setText(user.getDisplayName());
            email.setText(user.getEmail());
        }
        Button ch = findViewById(R.id.changePassword);
        ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user!=null) {
                    Intent intent = new Intent(SettingsActivity.this,EditProfileActivity.class);
                    startActivity(intent);
                }
            }
        });
        Button about = findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
        Button help = findViewById(R.id.settings_help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.dialog_help, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(promptsView);
                alertDialogBuilder.setCancelable(false)
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
        Button reset = findViewById(R.id.resetData);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteAll();
                Toast.makeText(SettingsActivity.this, "Data deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void navigation() {
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(SettingsActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
    public void setup() {
        login = findViewById(R.id.loginMain);
        loginText = findViewById(R.id.loginView);
        map = findViewById(R.id.mapButton);
        home = findViewById(R.id.homeButton);
    }
    public void handleLogin() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser == null) {
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    AuthUI.getInstance().signOut(SettingsActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(SettingsActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                                    login.setText("Login");
                                    loginText.setText("");
                                }
                            });
                }
            }
        });
    }
    public void authView(){
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

     /*   Uri uri = user.getPhotoUrl();
       ImageView img = findViewById(R.id.profile);
        if(uri!=null)
        {
            Glide.with(this).load(uri).error(R.mipmap.ic_launcher)
                    .placeholder(R.drawable.person)
                    .into(img);
            //Picasso.with(getContext()).load(uri.toString()).into(img);
        }*/
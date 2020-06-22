package com.example.goairtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;


public class EditProfileActivity extends Activity {
    int GET_IMAGE = 123;
    final Context context = this;
    private ImageView img;
    Uri path;
    private EditText username, password;
    private Button upload, delete, passwd;
    private  FirebaseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        Uri uri = user.getPhotoUrl();
        img = findViewById(R.id.profile);
        if(uri!=null) {
            Glide.with(this).load(uri).into(img);
        }
        username = findViewById(R.id.usernameEdit);
        username.setText(user.getDisplayName());
        upload = findViewById(R.id.sendUpdate);
        delete = findViewById(R.id.delete);
        passwd= findViewById(R.id.changePassword);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
        passwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void changePassword() {
        String passwordConf;
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_password, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        final EditText oldPass = promptsView.findViewById(R.id.oldPassword);
        final EditText newPass = promptsView.findViewById(R.id.newPassword);
        newPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        oldPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        final ToggleButton see = promptsView.findViewById(R.id.showlogin);
        see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean on = see.isChecked();
                if (on) {
                    newPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    oldPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                }
                else {
                    newPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    oldPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String oldPassword = oldPass.getText().toString();
                                String newPassword = newPass.getText().toString();
                                user = FirebaseAuth.getInstance().getCurrentUser();
                                assert user != null;
                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(user.getEmail(), oldPassword);

                                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("TAG", "User re-authenticated.");
                                            }
                                        });

                                user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    finish();
                                                    Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                }
                                                else {
                                                    Toast.makeText(EditProfileActivity.this, "Couldn't change password",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void uploadData() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        String usernametxt = username.getText().toString();
        UserProfileChangeRequest profileUpdates;
        if(path==null) {
            profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(usernametxt).build();
        }
        else {
            profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(usernametxt).setPhotoUri(path).build();
        }
        assert user != null;
        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    public void deleteAccount() {
        String passwordConf;
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_input, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = promptsView.findViewById(R.id.passwordDialog);
        TextView text = promptsView.findViewById(R.id.popupText);
        userInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
        text.setText("Do you really want to delete your account? \\n Confirm with password");
        final ToggleButton see = promptsView.findViewById(R.id.showlogin);
        see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean on = see.isChecked();
                if (on) {
                    userInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else {
                    userInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String passwordConf = userInput.getText().toString();
                                user = FirebaseAuth.getInstance().getCurrentUser();
                                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), passwordConf);
                                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("TAG", "User re-authenticated.");
                                            }
                                        });
                                assert user != null;
                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    finish();
                                                    Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                }
                                                else {
                                                    Toast.makeText(EditProfileActivity.this, "Couldn't delete account",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    public void imageHandle(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GET_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_IMAGE && resultCode == RESULT_OK && data.getData() !=null) {
            path = data.getData();
            Glide.with(this).load(path).error(R.mipmap.ic_launcher).placeholder(R.drawable.person).into(img);
            Toast.makeText(EditProfileActivity.this, path.toString(), Toast.LENGTH_SHORT).show();

        }
    }

}

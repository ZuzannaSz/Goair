package com.example.goairtest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

import static com.firebase.ui.auth.AuthUI.TAG;

public class LoginActivity extends Activity {
    private FirebaseAuth auth;
    private EditText email, password;
    private Button login, signup, recover;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        auth= FirebaseAuth.getInstance();
        email = findViewById(R.id.loginmail);
        password = findViewById(R.id.loginpassword);
        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        login = findViewById(R.id.login);
        signup = findViewById(R.id.loginsignup);
        recover = findViewById(R.id.forgotpassword);
        login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                userLogin();
            }
        });

        signup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
        recover.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
    public void userLogin()
    {
        String emailtxt, passwordtxt;
        emailtxt = email.getText().toString();
        passwordtxt= password.getText().toString();
        auth.signInWithEmailAndPassword(emailtxt,passwordtxt)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login successfull",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }
    public void toggleHandle(View view)
    {
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        } else {
             password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }
}

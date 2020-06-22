package com.example.goairtest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


public class ResetPasswordActivity extends Activity {
    private FirebaseAuth auth;
    private EditText email;
    private Button send;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        auth= FirebaseAuth.getInstance();
        email = findViewById(R.id.recovery);
        send = findViewById(R.id.recoverpassword);
        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                recoverPassword();
            }
        });

    }
    public void recoverPassword() {
        String emailtxt;
        emailtxt = email.getText().toString();
        try {
            auth.sendPasswordResetEmail(emailtxt);
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        catch (IllegalArgumentException e) {
            Toast.makeText(ResetPasswordActivity.this, "Empty or incorrect email, please try again",
                    Toast.LENGTH_SHORT).show();
        }

    }
}

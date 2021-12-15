package com.app.filmtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    //TODO: LA DOCUMENTACION FUE OBTENIDA EN:
    //TODO: General de Firebase: https://firebase.google.com/docs/android/setup?hl=es#available-libraries
    //TODO: Para Registro y login con Firebase: https://firebase.google.com/docs/auth/android/start?hl=es

    //Firebase
    private FirebaseAuth mAuth;

    //View Components
    private TextView tvGoToRegister;
    private Button logIn;
    private TextInputLayout email;
    private TextInputLayout password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //Firebase
        mAuth = FirebaseAuth.getInstance();


        //View Components
        logIn = findViewById(R.id.loginButtonLogIn);
        email = findViewById(R.id.loginEmailField);
        password = findViewById(R.id.loginPasswordField);

        //Event - Log In
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn(email.getEditText().getText().toString(),
                      password.getEditText().getText().toString());
            }
        });



        //Event - Go to Sign Up
        tvGoToRegister = findViewById(R.id.textViewGoToRegister);
        tvGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }


    private void logIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Te has logeado(debug).",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "NO te has logeado(debug).",
                                    Toast.LENGTH_SHORT).show();
                            /*Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();*/
                            //updateUI(null);
                        }
                    }
                });
    }
}
package com.app.filmtracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.filmtracker.poo.SingletonMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    //TODO: LA DOCUMENTACION FUE OBTENIDA EN:
    //TODO: General de Firebase: https://firebase.google.com/docs/android/setup?hl=es#available-libraries
    //TODO: Para Registro y login con Firebase: https://firebase.google.com/docs/auth/android/start?hl=es
    //TODO: Para OAuth 2.0 con Google y Firebase: https://firebase.google.com/docs/auth/android/google-signin?hl=es



    //Firebase
    private FirebaseAuth mAuth;

    //View Components
    private TextView tvGoToLogin;
    private Button registerButton;
    private TextInputLayout txInputUsername;
    private TextInputLayout txInputPassword;
    private TextInputLayout txInputConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Firebase
        mAuth = (FirebaseAuth) SingletonMap.getInstance().get(SingletonMap.FIREBASE_AUTH_INSTANCE);
        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
            SingletonMap.getInstance().put(SingletonMap.FIREBASE_AUTH_INSTANCE, mAuth);
        }


        //View Components
        txInputUsername = findViewById(R.id.registerEmailField);
        txInputPassword = findViewById(R.id.registerPasswordField);
        txInputConfirmPassword = findViewById(R.id.registerPasswordConfirmField);


        //Event - Register button
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txInputUsername.setError(null);
                txInputPassword.setError(null);
                txInputConfirmPassword.setError(null);

                String email = txInputUsername.getEditText().getText().toString();
                String password = txInputPassword.getEditText().getText().toString();
                String passwordConfirm = txInputConfirmPassword.getEditText().getText().toString();

                if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {  //Debe poseer la siguiente estructura: cadena@cadena.cadena
                    txInputUsername.setErrorEnabled(true);
                    txInputUsername.setError(getString(R.string.register_bad_format_email));
                } else if(!password.equalsIgnoreCase(passwordConfirm)){
                    txInputPassword.setError(getString(R.string.register_password_not_equals));
                    txInputPassword.setErrorEnabled(true);

                    txInputConfirmPassword.setErrorEnabled(true);
                    txInputConfirmPassword.setError(getString(R.string.register_password_not_equals));
                } else if(password.length() < 6){   //Firebase pide una longitud de 6 como minimo
                    txInputPassword.setError(getString(R.string.register_password_too_short));
                    txInputPassword.setErrorEnabled(true);
                } else {
                    createAccount(email, password);
                }
            }
        });


        //Event - Back to login
        tvGoToLogin = findViewById(R.id.textViewGoToLogin);
        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToLogIn();
            }
        });


    }



    private void backToLogIn(){
        finish();
    }


    private void createAccount(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, display a message to the user.
                            Log.d(TAG, "createUserWithEmail:success");
                            /*FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);*/
                            Toast.makeText(RegisterActivity.this,
                                    getText(R.string.register_successful),
                                    Toast.LENGTH_SHORT).show();
                            backToLogIn();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    getText(R.string.register_failure),
                                    Toast.LENGTH_LONG).show();
                            /*updateUI(user);*/
                        }
                    }
                });
    }
}
package com.app.filmtracker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.filmtracker.poo.SingletonMap;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    //TODO: LA DOCUMENTACION FUE OBTENIDA EN:
    //TODO: General de Firebase: https://firebase.google.com/docs/android/setup?hl=es#available-libraries
    //TODO: Para Registro y login con Firebase: https://firebase.google.com/docs/auth/android/start?hl=es
    //TODO: Para OAuth 2.0 con Google y Firebase: https://firebase.google.com/docs/auth/android/google-signin?hl=es



    //Firebase
    private FirebaseAuth mAuth;

    //OAuth 2.0 Google
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    //View Components
    private TextView tvGoToRegister;
    private Button logIn;
    private Button buttonLogInOAuthGoogle;
    private TextInputLayout email;
    private TextInputLayout password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //FirebaseAuth
        mAuth = (FirebaseAuth) SingletonMap.getInstance().get(SingletonMap.FIREBASE_AUTH_INSTANCE);
        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
            SingletonMap.getInstance().put(SingletonMap.FIREBASE_AUTH_INSTANCE, mAuth);
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLoggedUserAndGoNextActivity(currentUser);


        //OAuth 2.0 GOOGLE
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("582952554042-86tb70lgjs7op7kbkk2lh67pmgj7moml.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //View Components
        logIn = findViewById(R.id.loginButtonLogIn);
        email = findViewById(R.id.loginEmailField);
        password = findViewById(R.id.loginPasswordField);
        buttonLogInOAuthGoogle = findViewById(R.id.loginButtonLogInOAuthGoogle);


        //Event - Log In Default
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn(email.getEditText().getText().toString(),
                      password.getEditText().getText().toString());
            }
        });


        //Event - Go to Sign Up Default
        tvGoToRegister = findViewById(R.id.textViewGoToRegister);
        tvGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


        //Event - Log In/ Sign Up OAuth 2.0 Google
        buttonLogInOAuthGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInOAuthGoogle();
            }
        });
    }




    //----------------------------------------Default Log In---------------------------------------
    private void logIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Te has logeado(debug).",
                                    Toast.LENGTH_SHORT).show();
                            checkLoggedUserAndGoNextActivity(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "NO te has logeado(debug).",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //---------------------------------------OAuth 2.0 GOOGLE--------------------------------------
    private void signInOAuthGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleOAuthResultLauncher.launch(signInIntent);
        //startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, "No te has logeado en google(debug).",
                        Toast.LENGTH_SHORT).show();
                // Google Sign In failed, update UI appropriately
            }
        }
    }*/


    ActivityResultLauncher<Intent> googleOAuthResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(LoginActivity.this, "No te has logeado en google(debug).",
                                    Toast.LENGTH_SHORT).show();
                            // Google Sign In failed, update UI appropriately
                        }
                    }
                }
            });


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LoginActivity.this, "Te has logeado en google!(debug).",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkLoggedUserAndGoNextActivity(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Ha ocurrido un error con el token(?)(debug).",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void checkLoggedUserAndGoNextActivity(FirebaseUser user){
        if(user!=null){
            SingletonMap.getInstance().put(SingletonMap.FIREBASE_USER_INSTANCE, user);

            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
            finish();
            startActivity(intent);
        }
    }

}
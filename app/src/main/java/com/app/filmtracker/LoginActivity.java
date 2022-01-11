package com.app.filmtracker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                            Toast.makeText(LoginActivity.this, getString(R.string.logged_correct),
                                    Toast.LENGTH_SHORT).show();
                            checkLoggedUserAndGoNextActivity(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, getString(R.string.logged_incorrect),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //---------------------------------------OAuth 2.0 GOOGLE--------------------------------------
    private void signInOAuthGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleOAuthResultLauncher.launch(signInIntent);
    }



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
                            Toast.makeText(LoginActivity.this, getString(R.string.logged_incorrect),
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
                            Toast.makeText(LoginActivity.this, getString(R.string.logged_correct),
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkLoggedUserAndGoNextActivity(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, getString(R.string.logged_incorrect),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void checkLoggedUserAndGoNextActivity(FirebaseUser user){
        if(user!=null){
            SingletonMap.getInstance().put(SingletonMap.FIREBASE_USER_INSTANCE, user);
            checkUserDataSavedInFireStore(user);

            Intent intent = new Intent(LoginActivity.this, PrincipalActivity.class);
            finish();
            startActivity(intent);
        }
    }

    //Si se registra con OAuth, es posible que aun no tengamos todos sus datos registrados
    //Comprobaremos en FireStorage si se ha guardado o no.
    private void checkUserDataSavedInFireStore(FirebaseUser user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            if(documents == null || documents.isEmpty())
                                saveUserDataInFireStore(user);
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.login_data_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void saveUserDataInFireStore(FirebaseUser user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userMap = new HashMap<>();

        userMap.put("email", user.getEmail());
        if(user.getDisplayName() != null && !user.getDisplayName().isEmpty())
            userMap.put("full_name",  user.getDisplayName());
        else
            userMap.put("full_name",  "");
        userMap.put("username", user.getEmail().replace("@gmail.com", ""));
        userMap.put("has_image", false);

        db.collection("User")
                .add(userMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            if(user.getPhotoUrl() != null && !user.getPhotoUrl().toString().isEmpty())
                                new UploadImageFireStorage().execute(user.getPhotoUrl().toString(), user.getEmail(), task.getResult().getId());
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.login_data_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });



    }

    protected class UploadImageFireStorage extends AsyncTask<String, Void, Bitmap> {

        private String userEmail;
        private String userId;

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urldisplay = strings[0];
            this.userEmail = strings[1];
            this.userId = strings[2];

            Bitmap iconProfile = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                iconProfile = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return iconProfile;
        }

        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference().child(this.userEmail + "/" +"image_profile");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //Do nothing
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("User")
                            .document(userId)
                            .update("has_image", true);
                }
            });
        }
    }

}
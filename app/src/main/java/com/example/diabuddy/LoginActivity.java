package com.example.diabuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.diabuddy.onboarding.OnboardingActivity;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "GoogleActivity";
    private static final int REQ_ONE_TAP = 9001;
    private static final int RC_SIGN_OUT = 9002;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    private String type = null;

    private ProgressBar loading;
    private CardView signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loading = findViewById(R.id.loading);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            type = extras.getString("type");
        }

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in. (don't)
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load();
                signIn();
            }
        });

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        login(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken !=  null) {
                    // Got an ID token from Google. Use it to authenticate with your backend.
                    Log.d(TAG, "Got ID token.");
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        login(user);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                                        noMoreLoad();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                        builder.setMessage("Invalid login! Please try again.")
                                                .setTitle("Invalid Login");
                                        AlertDialog invalidLoginDialog = builder.create();
                                        invalidLoginDialog.show();
                                    }
                                }
                            });
                }
            } catch (ApiException e) {
                switch (e.getStatusCode()) {
                    case CommonStatusCodes.NETWORK_ERROR:
                        Log.d(TAG, "One-tap encountered a network error.");
                        AlertDialog.Builder networkErrorBuilder = new AlertDialog.Builder(LoginActivity.this);
                        networkErrorBuilder.setMessage("A network error occurred. Please try again.")
                                .setTitle("Network Error");
                        AlertDialog networkErrorDialog = networkErrorBuilder.create();
                        networkErrorDialog.show();
                        break;
                    default: // includes CANCELED
                        Log.d(TAG, "Couldn't get credential from result."
                                + e.getLocalizedMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage("Invalid login! Please try again.")
                                .setTitle("Invalid Login");
                        AlertDialog invalidLoginDialog = builder.create();
                        invalidLoginDialog.show();
                        break;
                }
            }
        } else if (requestCode == RC_SIGN_OUT) {
            oneTapClient.signOut();
            FirebaseAuth.getInstance().signOut();
            noMoreLoad();
        }
    }

    private void signIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(
                                    result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // No saved credentials found. Launch the One Tap sign-up flow, or
                        // do nothing and continue presenting the signed-out UI.
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                });
    }

    private void login(FirebaseUser user) {
        if (user == null) return;
        load();

        String uid = user.getUid();

        final DocumentReference docRef = db.collection("users").document(uid);
        docRef.addSnapshotListener(new EventListener() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    if (type != null && type.equals("reminder")) {
                        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                        nm.cancelAll();
                        Intent i = new Intent(LoginActivity.this, UserActivity.class);
                        i.putExtra("message", 3); // reminder, 0 indexed
                        startActivityForResult(i, RC_SIGN_OUT);
                    } else  if (type != null && type.equals("warning")) {
                        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                        nm.cancelAll();
                        Intent i = new Intent(LoginActivity.this, UserActivity.class);
                        i.putExtra("message", 4); // warning, 0 indexed
                        startActivityForResult(i, RC_SIGN_OUT);
                    } else {
                        Intent i = new Intent(LoginActivity.this, UserActivity.class);
                        startActivityForResult(i, RC_SIGN_OUT);
                    }
                } else {
                    createNewUser(uid);
                    Intent i = new Intent(LoginActivity.this, OnboardingActivity.class);
                    startActivityForResult(i, RC_SIGN_OUT);
                }
            }
        });

    }

    private void load() {
        loading.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.INVISIBLE);
        signInButton.setClickable(false);
    }

    private void noMoreLoad() {
        loading.setVisibility(View.GONE);
        signInButton.setVisibility(View.VISIBLE);
        signInButton.setClickable(true);
    }

    private void createNewUser(String uid) { // create the necessary paths
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("bgUnit", "mmol/L");
        user.put("diagnosis", Calendar.getInstance().get(Calendar.YEAR));
        user.put("dob", new Timestamp(new Date()));
        user.put("height", 0.0);
        user.put("hyper", 10.0);
        user.put("hypo", 4.0);
        user.put("weight", 0.0);

        // challenges
        List<String> emptyList = Arrays.asList();
        user.put("challenges-70", emptyList);
        user.put("challenges-80", emptyList);
        user.put("challenges-90", emptyList);
        user.put("challenges-100", emptyList);
        user.put("challenges-notes", emptyList);
        user.put("challenges-nutrition", 0);

        db.collection("users").document(uid).set(user);

        // messages
        Map<String, Object> firstGroup = new HashMap<>();
        firstGroup.put("content", Arrays.asList("Welcome! How may I help you?"));
        firstGroup.put("type", Arrays.asList(1));
        DocumentReference firstGroupDocRef = db.collection("messages").document("group0");
        firstGroupDocRef.set(firstGroup);
        Map<String, Object> messageGroups = new HashMap<>();
        messageGroups.put("groups", Arrays.asList(firstGroupDocRef));
        db.collection("messages").document("messages").set(messageGroups);

        /*
        TODO?
        siterotation inProgress = 0
         */
    }
}
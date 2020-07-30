package com.cubenama.cubingcompanionadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SignInActivity extends AppCompatActivity {

    // Database reference
    private FirebaseFirestore db;

    private SharedPreferences userAccessSharedPreferences;

    // Current app version
    private static final long VERSION_ID = 3;

    private ProgressBar progressBar;
    private EditText adminKeyEditText;
    private Button login;
    private CheckBox rememberMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        
        db = FirebaseFirestore.getInstance();

        adminKeyEditText = findViewById(R.id.adminKey);
        rememberMeCheckBox = findViewById(R.id.rememberMe);
        progressBar = findViewById(R.id.progressBar);
        login = findViewById(R.id.login);
        
        userAccessSharedPreferences = getSharedPreferences("organizer_details", Context.MODE_PRIVATE);

        // Get login preferences
        if (userAccessSharedPreferences.getBoolean("remember_me", false)) {
            adminKeyEditText.setText(userAccessSharedPreferences.getString("admin_key", ""));
            rememberMeCheckBox.setChecked(true);
        }

        // Check app version validity
        checkVersionValidity();
    }

    private void checkVersionValidity() {
        // Check if current application version is valid
        db.collection(getString(R.string.db_field_name_app_details)).document(getString(R.string.db_field_name_app_version)).get().addOnCompleteListener(appVersionTask -> {
            progressBar.setVisibility(View.GONE);

            if(appVersionTask.getResult().getLong(getString(R.string.db_field_name_admin_min_version_id)) <= VERSION_ID)
                authenticateUser();
            else
            {
                Toast.makeText(this, "A newer version is available. Please update to continue.", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(this::finish, 3000);
            }
        });
    }

    private void authenticateUser() {
        login.setOnClickListener(v -> {
            String adminKey = adminKeyEditText.getText().toString();
            // Get organizer details

            progressBar.setVisibility(View.VISIBLE);
            db.collection(getString(R.string.db_field_name_organizer_details)).whereEqualTo(getString(R.string.db_field_name_key), adminKey).get().addOnCompleteListener(organizerDetailsTask ->
            {
                progressBar.setVisibility(View.GONE);
                if(organizerDetailsTask.getResult().isEmpty())
                    Toast.makeText(this, "Administration key invalid", Toast.LENGTH_SHORT).show();
                else {
                    DocumentSnapshot organizerDetails = organizerDetailsTask.getResult().getDocuments().get(0);
                    // Store organizer details
                    userAccessSharedPreferences.edit().putString("access", organizerDetails.getString(getString(R.string.db_field_name_access))).apply();
                    userAccessSharedPreferences.edit().putString("organizer_key", organizerDetails.getString(getString(R.string.db_field_name_key))).apply();
                    userAccessSharedPreferences.edit().putString("name", organizerDetails.getString(getString(R.string.db_field_name_name))).apply();

                    // Store login preferences
                    if (rememberMeCheckBox.isChecked()) {
                        userAccessSharedPreferences.edit().putBoolean("remember_me", true).apply();
                        userAccessSharedPreferences.edit().putString("admin_key", adminKey).apply();
                    }
                    else
                        userAccessSharedPreferences.edit().clear().apply();

                    // Start main activity
                    Intent menuActivityIntent = new Intent(this, MenuActivity.class);
                    startActivity(menuActivityIntent);

                    // End login activity
                    finish();
                }
            });
        });
    }
}

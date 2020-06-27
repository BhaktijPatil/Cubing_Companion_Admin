package com.cubenama.cubingcompanionadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    // Database reference
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        final SharedPreferences userAccessSharedPreferences = getSharedPreferences("organizer_details", Context.MODE_PRIVATE);

        final Button loginButton = findViewById(R.id.loginButton);
        final EditText devKeyEditText = findViewById(R.id.passwordInput);

        loginButton.setOnClickListener(v -> {

            final String organizerKey = devKeyEditText.getText().toString();

            // Get organizer details
            CollectionReference organizers = db.collection("organizer_details");
            organizers.get().addOnCompleteListener(task ->
            {
                for(QueryDocumentSnapshot organizer : task.getResult())
                {
                    // Valid key
                    if(organizer.getString("key").equals(organizerKey))
                    {
                        // Store organizer details
                        userAccessSharedPreferences.edit().putString("access", organizer.getString("access")).apply();
                        userAccessSharedPreferences.edit().putString("organizer_key", organizerKey).apply();
                        userAccessSharedPreferences.edit().putString("name", organizer.getString("name")).apply();
                        // Start main activity
                        Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(mainActivityIntent);
                        // End login activity
                        finish();
                    }
                    // Invalid key
                    else
                        Toast.makeText(LoginActivity.this, "Organizer key invalid", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

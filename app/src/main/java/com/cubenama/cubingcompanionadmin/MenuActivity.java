package com.cubenama.cubingcompanionadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        CardView manageCompsButton = findViewById(R.id.manageCompsCardView);
        CardView usersDbButton = findViewById(R.id.usersDbCardView);
        CardView appVersionButton = findViewById(R.id.appVersionCardView);

        manageCompsButton.setOnClickListener(v -> startActivity(new Intent(this, CompetitionsActivity.class)));
        usersDbButton.setOnClickListener(v -> Toast.makeText(this, "coming soon ...", Toast.LENGTH_SHORT).show());
        appVersionButton.setOnClickListener(v -> Toast.makeText(this, "coming soon ...", Toast.LENGTH_SHORT).show());
    }
}

package com.cubenama.cubingcompanionadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button manageCompsButton = findViewById(R.id.manageCompButton);
        Button manageResultsButton = findViewById(R.id.compResultsButton);

        manageCompsButton.setOnClickListener(v-> startActivity(new Intent(this, CompsListActivity.class)));

    }
}

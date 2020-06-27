package com.cubenama.cubingcompanionadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CompsListActivity extends AppCompatActivity {

    // Database reference
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comps_list);

        SharedPreferences userAccessSharedPreferences = getSharedPreferences("organizer_details", Context.MODE_PRIVATE);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Setup recycler views and adapters
        List<Comp> compList = new ArrayList<>();
        RecyclerView compListRecyclerView = findViewById(R.id.compList);

        CompAdapter compAdapter = new CompAdapter(compList, position -> Toast.makeText(this, compList.get(position).name, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager compListLayoutManager = new LinearLayoutManager(this);

        compListRecyclerView.setLayoutManager(compListLayoutManager);
        compListRecyclerView.setAdapter(compAdapter);

        db.collection("competition_details").addSnapshotListener((queryDocumentSnapshots, e) ->
        {
            compList.clear();
           for (QueryDocumentSnapshot comp : queryDocumentSnapshots)
           {
               if(comp.getString("organizer").equals(userAccessSharedPreferences.getString("name","")))
               {
                   compList.add(new Comp(comp.getId(), comp.getString("name")));
               }
           }
           compAdapter.notifyDataSetChanged();
        });

        FloatingActionButton addCompButton = findViewById(R.id.addCompButton);
        addCompButton.setOnClickListener(v->{
            Intent manageCompIntent = new Intent(this, ManageCompActivity.class);
            manageCompIntent.putExtra("id", "new");
            startActivity(manageCompIntent);
        });

    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}

package com.cubenama.cubingcompanionadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ResultEventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Create database instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Show loading screen
        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Setup recycler views and adapters
        List<CompetitionEvent> competitionEventList = new ArrayList<>();
        RecyclerView competitionResultsRecyclerView = findViewById(R.id.resulEventstListRecyclerView);

        ResultEventAdapter resultEventAdapter = new ResultEventAdapter(competitionEventList, position -> Toast.makeText(this, competitionEventList.get(position).eventId, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager resultEventLayoutManager = new LinearLayoutManager(this);

        competitionResultsRecyclerView.setLayoutManager(resultEventLayoutManager);
        competitionResultsRecyclerView.setAdapter(resultEventAdapter);


        // Get competition ID
        String compId = getIntent().getStringExtra("comp_id");

        DocumentReference competitionDetailsReference = db.collection(getString(R.string.db_field_name_comp_details)).document(compId);
        CollectionReference eventDetailsReference = competitionDetailsReference.collection(getString(R.string.db_field_name_events));

        Switch resultsVerified = findViewById(R.id.resultsVerified);
        resultsVerified.setTypeface(ResourcesCompat.getFont(this, R.font.abel));
        competitionDetailsReference.get().addOnCompleteListener(resultStatusTask -> {
            resultsVerified.setChecked(resultStatusTask.getResult().getBoolean(getString(R.string.db_field_name_results_verified)));
            resultsVerified.setOnCheckedChangeListener((buttonView, isChecked) -> {
                progressBar.setVisibility(View.VISIBLE);
                competitionDetailsReference.update(getString(R.string.db_field_name_results_verified), isChecked).addOnCompleteListener(task -> progressBar.setVisibility(View.GONE));
            });
        });


        eventDetailsReference.get().addOnCompleteListener(eventDetailsTask -> {
            competitionEventList.clear();
            // Individual events are obtained here
            for(QueryDocumentSnapshot event : eventDetailsTask.getResult())
            {
                CollectionReference roundDetailsReference = eventDetailsReference.document(event.getId()).collection(getString(R.string.db_field_name_rounds));
                roundDetailsReference.orderBy(getString(R.string.db_field_name_id)).get().addOnCompleteListener(roundDetailsTask -> {

                    // Create a new event instance
                    CompetitionEvent competitionEvent = new CompetitionEvent(event.getId(), event.getString(getString(R.string.db_field_name_name)), event.getLong(getString(R.string.db_field_name_solve_count)), event.getString(getString(R.string.db_field_name_result_calc_method)));
                    Log.d("CC_COMP_SCHEDULE", "Event ID : " + competitionEvent.eventId + " Round Count : " + roundDetailsTask.getResult().size());

                    // Individual rounds for each event are obtained here
                    for(QueryDocumentSnapshot round : roundDetailsTask.getResult())
                    {
                        CompetitionEventRound competitionEventRound = new CompetitionEventRound(competitionEvent.eventName, round.getLong(getString(R.string.db_field_name_id)), round.getId(), round.getLong(getString(R.string.db_field_name_qualification_criteria)), round.getTimestamp(getString(R.string.db_field_name_start_time)), round.getTimestamp(getString(R.string.db_field_name_end_time)));
                        competitionEvent.competitionEventRounds.add(competitionEventRound);
                    }
                    competitionEventList.add(competitionEvent);

                    // Sort Events by name
                    Collections.sort(competitionEventList, (event1, event2) -> event1.eventName.compareTo(event2.eventName));
                    resultEventAdapter.notifyDataSetChanged();
                    // Dismiss loading screen
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}

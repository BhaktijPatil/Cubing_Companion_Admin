package com.cubenama.cubingcompanionadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    // Database reference
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Create database instance
        db = FirebaseFirestore.getInstance();
        
        // Get  ID
        Intent intent = getIntent();
        String comp_id = intent.getStringExtra("comp_id");

        // Store comp ID in shared preferences
        SharedPreferences compDetailsSharedPreferences = getSharedPreferences("comp_details", Context.MODE_PRIVATE);
        compDetailsSharedPreferences.edit().putString("comp_id", comp_id).apply();

        // Reference to event schedule
        CollectionReference schedule = db.collection("competition_details").document(comp_id).collection("schedule");
        
        // Setup recycler views and adapters
        List<Event> eventList = new ArrayList<>();
        RecyclerView eventRecyclerView = findViewById(R.id.eventList);

        EventAdapter eventAdapter = new EventAdapter(eventList, position -> Toast.makeText(this, eventList.get(position).eventId, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager eventLayoutManager = new LinearLayoutManager(this);

        eventRecyclerView.setLayoutManager(eventLayoutManager);
        eventRecyclerView.setAdapter(eventAdapter);

        // Add new event
        FloatingActionButton addEvent = findViewById(R.id.newEventButton);
        addEvent.setOnClickListener(v-> {
            // Get inflater for the current activity
            LayoutInflater inflater = getLayoutInflater();
            // Inflate the popup window layout (add event)
            View popupManageEventView = inflater.inflate(R.layout.popup_manage_event, null);
            // Setup popup window
            PopupWindow popupWindow = new PopupWindow(popupManageEventView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
            // Show popup view at the center
            popupWindow.showAtLocation(popupManageEventView, Gravity.CENTER, 0, 0);
            EditText eventName = popupManageEventView.findViewById(R.id.newEventName);
            EditText solveCount = popupManageEventView.findViewById(R.id.solveCount);

            // Select method for result calculation
            Spinner resultCalcMethodSpinner = popupManageEventView.findViewById(R.id.resultCalcMethodSpinner);
            // Create an adapter for resultCalcMethodSpinner
            BetterListAdapter betterListAdapter = new BetterListAdapter(this, getResources().getStringArray(R.array.result_calc_methods));
            // Dropdown text layout
            betterListAdapter.setDropDownViewResource(R.layout.spinner_item_result_calc_method);
            // Apply the adapter to the resultCalcMethodSpinner
            resultCalcMethodSpinner.setAdapter(betterListAdapter);
            // Set default to average
            resultCalcMethodSpinner.setSelection(0);
            
            // Listener to add new event
            Button addEventButton = popupManageEventView.findViewById(R.id.addEventButton);
            addEventButton.setOnClickListener(view->{
                if(eventName.getText().toString().equals("") || solveCount.getText().toString().equals(""))
                {
                    Toast.makeText(this, "Fill out all fields to proceed.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create event HashMap
                Map<String, Object> eventDetails = new HashMap<>();
                eventDetails.put("solve_count", Long.parseLong(solveCount.getText().toString()));
                eventDetails.put("name", eventName.getText().toString());
                eventDetails.put("result_calc_method", resultCalcMethodSpinner.getSelectedItem().toString());

                schedule.add(eventDetails).addOnCompleteListener(task -> {
                    Toast.makeText(this, "Event added successfully.", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                });
            });
        });

        // Get current events
        schedule.addSnapshotListener((queryDocumentSnapshots, e) -> {
            eventList.clear();
            // No events exist
            if(queryDocumentSnapshots.isEmpty())
                Toast.makeText(this, "No events found.", Toast.LENGTH_SHORT).show();

            // Individual events are obtained here
            for(QueryDocumentSnapshot event : queryDocumentSnapshots)
            {
                CollectionReference event_rounds = schedule.document(event.getId()).collection("rounds");
                event_rounds.orderBy("round_id").get().addOnCompleteListener(innerTask -> {

                    // Create a new event instance
                    Event compEvent = new Event(event.getLong("solve_count"), event.getId(), event.getString("name"));
                    Log.d("CC_COMP_SCHEDULE", "Event ID : " + compEvent.eventId + " Round Count : " + innerTask.getResult().size());

                    // Individual rounds for each event are obtained here
                    for(QueryDocumentSnapshot round : innerTask.getResult())
                    {
                        EventRound eventRound = new EventRound(round.getId(), round.getLong("round_id"), round.getLong("participant_count"), round.getTimestamp("start_time"), round.getTimestamp("end_time"));
                        compEvent.eventRounds.add(eventRound);
                    }
                    eventList.add(compEvent);
                    eventAdapter.notifyDataSetChanged();
                });
            }
        });

        // Refresh activity
        ImageView refresh = findViewById(R.id.refreshButton);
        refresh.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        });
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}

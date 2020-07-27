package com.cubenama.cubingcompanionadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDetailsActivity extends AppCompatActivity {

    // Database reference
    private FirebaseFirestore db;

    private SharedPreferences compDetailsSharedPreferences;

    CollectionReference eventDetailsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Get comp ID in shared preferences
        compDetailsSharedPreferences = getSharedPreferences("comp_details", Context.MODE_PRIVATE);

        // Get comp ID
        String compId = getIntent().getStringExtra("comp_id");

        // Reference to event schedule
        eventDetailsReference = db.collection(getString(R.string.db_field_name_comp_details)).document(compId).collection(getString(R.string.db_field_name_events));

        // Setup recycler views and adapters
        List<CompetitionEvent> eventList = new ArrayList<>();
        RecyclerView eventListRecyclerView = findViewById(R.id.eventListRecyclerView);

        CompetitionEventAdapter competitionEventAdapter = new CompetitionEventAdapter(eventList, position -> Toast.makeText(this, eventList.get(position).eventId, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager eventLayoutManager = new LinearLayoutManager(this);

        eventListRecyclerView.setLayoutManager(eventLayoutManager);
        eventListRecyclerView.setAdapter(competitionEventAdapter);

        // Add new event
        Button addEvent = findViewById(R.id.addEventButton);
        addEvent.setOnClickListener(v -> showEventManager(null));

        // Get current event details
        eventDetailsReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            eventList.clear();
            // No events exist
            if(queryDocumentSnapshots.isEmpty())
                Toast.makeText(this, "No events found.", Toast.LENGTH_SHORT).show();

            // Individual events are obtained here
            for(QueryDocumentSnapshot event : queryDocumentSnapshots)
            {
                CollectionReference roundDetailsReference = eventDetailsReference.document(event.getId()).collection(getString(R.string.db_field_name_rounds));
                roundDetailsReference.orderBy(getString(R.string.db_field_name_id)).get().addOnCompleteListener(roundDetailsTask -> {

                    // Create a new event instance
                    CompetitionEvent competitionEvent = new CompetitionEvent(event.getId(), event.getString(getString(R.string.db_field_name_name)), event.getLong(getString(R.string.db_field_name_solve_count)),  event.getString(getString(R.string.db_field_name_result_calc_method)));
                    Log.d("CC_COMP_SCHEDULE", "Event ID : " + competitionEvent.eventId + " Round Count : " + roundDetailsTask.getResult().size());

                    // Individual rounds for each event are obtained here
                    for(QueryDocumentSnapshot round : roundDetailsTask.getResult())
                    { 
                        competitionEvent.competitionEventRounds.add(new CompetitionEventRound(competitionEvent.eventName, round.getLong(getString(R.string.db_field_name_id)), round.getId(), round.getLong(getString(R.string.db_field_name_qualification_criteria)), round.getTimestamp(getString(R.string.db_field_name_start_time)), round.getTimestamp(getString(R.string.db_field_name_end_time))));
                    }
                    eventList.add(competitionEvent);
                    competitionEventAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }



    // Show dialog to manager round
    void showRoundManager(CompetitionEvent competitionEvent, int roundIndex)
    {
        final Dialog editRoundDialog = new Dialog(this);
        editRoundDialog.setContentView(R.layout.dialog_manage_round);
        editRoundDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText qualificationCriteria = editRoundDialog.findViewById(R.id.qualificationCriteria);
        EditText roundNo = editRoundDialog.findViewById(R.id.roundNo);

        TextView roundStartTime = editRoundDialog.findViewById(R.id.roundStartTime);
        TextView roundEndTime = editRoundDialog.findViewById(R.id.roundEndTime);

        ImageView roundStartTimeButton = editRoundDialog.findViewById(R.id.roundStartTimeButton);
        ImageView roundEndTimeButton = editRoundDialog.findViewById(R.id.roundEndTimeButton);

        Button updateButton = editRoundDialog.findViewById(R.id.updateButton);
        Button deleteButton = editRoundDialog.findViewById(R.id.deleteButton);
        Button scramblesButton = editRoundDialog.findViewById(R.id.scramblesButton);

        // Listeners for timeStamp values
        roundStartTimeButton.setOnClickListener(roundStartView -> getDate(roundStartTime, competitionEvent.competitionEventRounds.get(roundIndex).startTime, "round_start_time"));
        roundEndTimeButton.setOnClickListener(roundEndView -> getDate(roundEndTime, competitionEvent.competitionEventRounds.get(roundIndex).endTime, "round_end_time"));

        CollectionReference roundDetailsReference = eventDetailsReference.document(competitionEvent.eventId).collection(getString(R.string.db_field_name_rounds));

        // Update round
        if(roundIndex != 999) {
            roundNo.setText(String.valueOf(competitionEvent.competitionEventRounds.get(roundIndex).roundNo));
            qualificationCriteria.setText(String.valueOf(competitionEvent.competitionEventRounds.get(roundIndex).qualificationCriteria));
            roundStartTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm a", competitionEvent.competitionEventRounds.get(roundIndex).startTime));
            roundEndTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm a", competitionEvent.competitionEventRounds.get(roundIndex).endTime));
            
            updateButton.setText("Update");
            deleteButton.setVisibility(View.VISIBLE);
            scramblesButton.setVisibility(View.VISIBLE);

            // Listeners for timeStamp values
            roundStartTimeButton.setOnClickListener(roundStartView -> getDate(roundStartTime, competitionEvent.competitionEventRounds.get(roundIndex).startTime, "round_start_time"));
            roundEndTimeButton.setOnClickListener(roundEndView -> getDate(roundEndTime, competitionEvent.competitionEventRounds.get(roundIndex).endTime, "round_end_time"));

            // Listener to delete round
            deleteButton.setOnClickListener(v -> {
                roundDetailsReference.document(competitionEvent.competitionEventRounds.get(roundIndex).roundId).delete().addOnCompleteListener(task -> Toast.makeText(this, "Round deleted successfully.", Toast.LENGTH_SHORT).show());
                editRoundDialog.dismiss();
                refreshActivity();
            });

            // Listener to manage scrambles
            scramblesButton.setOnClickListener(v -> showScramblesManager(competitionEvent, roundIndex));
        }
        // Add round
        else {
            // Listeners for timeStamp values
            roundStartTimeButton.setOnClickListener(roundStartView -> getDate(roundStartTime, null, "round_start_time"));
            roundEndTimeButton.setOnClickListener(roundEndView -> getDate(roundEndTime, null, "round_end_time"));
        }
        
        // Listener to add new round
        updateButton.setOnClickListener(view -> {
            if(qualificationCriteria.getText().toString().equals("") || roundNo.getText().toString().equals("") || roundStartTime.getText().toString().equals(getString(R.string.date_time_placeholder)) || roundEndTime.getText().toString().equals(getString(R.string.date_time_placeholder)))
                Toast.makeText(this, "Fill out all fields to proceed.", Toast.LENGTH_SHORT).show();
            else
            {
                // Create round HashMap
                Map<String, Object> roundDetails = new HashMap<>();
                
                roundDetails.put(getString(R.string.db_field_name_qualification_criteria), Long.parseLong(qualificationCriteria.getText().toString()));
                roundDetails.put(getString(R.string.db_field_name_id), Long.parseLong(roundNo.getText().toString()));
                roundDetails.put(getString(R.string.db_field_name_start_time), new Timestamp(compDetailsSharedPreferences.getLong("round_start_time",0),0));
                roundDetails.put(getString(R.string.db_field_name_end_time), new Timestamp(compDetailsSharedPreferences.getLong("round_end_time",0),0));

                // Update round
                if(roundIndex != 999)
                    roundDetailsReference.document(competitionEvent.competitionEventRounds.get(roundIndex).roundId).update(roundDetails).addOnCompleteListener(task -> {
                        Toast.makeText(this, "Round updated successfully.", Toast.LENGTH_SHORT).show();
                        refreshActivity();
                    });
                // Add round
                else {
                    // Create default scrambles
                    ArrayList<String> defaultScrambles = new ArrayList<>();
                    for (long i = 1; i <= competitionEvent.solveCount; i++)
                        defaultScrambles.add("Scramble No." + i);
                    roundDetails.put(getString(R.string.db_field_name_scrambles), defaultScrambles);

                    roundDetailsReference.add(roundDetails).addOnCompleteListener(task -> {
                        Toast.makeText(this, "Round added successfully.", Toast.LENGTH_SHORT).show();
                        editRoundDialog.dismiss();
                        refreshActivity();
                    });
                }
            }
        });
        editRoundDialog.show();
    }



    // Show dialog to manage scrambles
    void showScramblesManager(CompetitionEvent competitionEvent, int roundIndex)
    {
        final Dialog editScramblesDialog = new Dialog(this);
        editScramblesDialog.setContentView(R.layout.dialog_manage_scrambles);
        editScramblesDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button updateButton = editScramblesDialog.findViewById(R.id.updateButton);

        // Setup recycler views and adapters
        ArrayList<String> scrambleList = new ArrayList<>();
        RecyclerView scrambleListRecyclerView = editScramblesDialog.findViewById(R.id.scrambleRecyclerView);

        ScrambleAdapter scrambleAdapter = new ScrambleAdapter(scrambleList, position -> Toast.makeText(this, scrambleList.get(position), Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager scrambleLayoutManager = new LinearLayoutManager(this);

        scrambleListRecyclerView.setLayoutManager(scrambleLayoutManager);
        scrambleListRecyclerView.setAdapter(scrambleAdapter);

        // Get current event details
        DocumentReference roundDetailsReference = eventDetailsReference.document(competitionEvent.eventId).collection(getString(R.string.db_field_name_rounds)).document(competitionEvent.competitionEventRounds.get(roundIndex).roundId);
        roundDetailsReference.get().addOnCompleteListener(task -> {
            scrambleList.clear();
            scrambleList.addAll((ArrayList<String>) task.getResult().get(getString(R.string.db_field_name_scrambles)));
            scrambleAdapter.notifyDataSetChanged();

            updateButton.setOnClickListener(v -> {
                ArrayList<String> tempList = new ArrayList<>();
                // Retrieve scrambles from recycler view
                for (int i = 0; i < scrambleList.size(); i++)
                    tempList.add(((EditText) scrambleListRecyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.scrambleEditText)).getText().toString());
                // Update scrambles to DB
                roundDetailsReference.update(getString(R.string.db_field_name_scrambles), tempList).addOnCompleteListener(updateScramblesTask -> {
                    Toast.makeText(this, "Scrambles updated successfully.", Toast.LENGTH_SHORT).show();
                    editScramblesDialog.dismiss();
                });
            });
        });

        editScramblesDialog.show();
    }



    // Show dialog to manage event
    void showEventManager(CompetitionEvent competitionEvent)
    {
        final Dialog editEventDialog = new Dialog(this);
        editEventDialog.setContentView(R.layout.dialog_manage_event);
        editEventDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText eventName = editEventDialog.findViewById(R.id.eventName);
        EditText solveCount = editEventDialog.findViewById(R.id.solveCount);

        Button updateButton = editEventDialog.findViewById(R.id.updateButton);
        Button deleteButton = editEventDialog.findViewById(R.id.deleteButton);

        // Select method for result calculation
        Spinner resultCalcMethodSpinner = editEventDialog.findViewById(R.id.resultCalcMethodSpinner);
        // Create an adapter for resultCalcMethodSpinner
        SpinnerTextAdapter spinnerTextAdapter = new SpinnerTextAdapter(this, getResources().getStringArray(R.array.result_calc_methods));
        // Dropdown text layout
        spinnerTextAdapter.setDropDownViewResource(R.layout.spinner_item_result_calc_method);
        // Apply the adapter to the resultCalcMethodSpinner
        resultCalcMethodSpinner.setAdapter(spinnerTextAdapter);

        if(competitionEvent != null)
        {
            eventName.setText(competitionEvent.eventName);
            solveCount.setText(String.valueOf(competitionEvent.solveCount));
            updateButton.setText("Update");
            
            String[] resultCalcMethod = getResources().getStringArray(R.array.result_calc_methods);
            for (int index = 0; index < resultCalcMethod.length; index ++) 
            {
                if(resultCalcMethod[index].equals(competitionEvent.resultCalcMethod))
                {
                    resultCalcMethodSpinner.setSelection(index);
                    break;
                }
            }

            // Listener to delete event
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                eventDetailsReference.document(competitionEvent.eventId).delete().addOnCompleteListener(task -> Toast.makeText(this, "Event deleted successfully.", Toast.LENGTH_SHORT).show());
                editEventDialog.dismiss();
            });
        }
        else
            // Set default to average
            resultCalcMethodSpinner.setSelection(0);

        // Listener to add new event
        updateButton.setOnClickListener(view -> {
            if(eventName.getText().toString().equals("") || solveCount.getText().toString().equals(""))
                Toast.makeText(this, "Fill out all fields to proceed.", Toast.LENGTH_SHORT).show();
            else
            {
                // Create event HashMap
                Map<String, Object> eventDetails = new HashMap<>();
                eventDetails.put(getString(R.string.db_field_name_name), eventName.getText().toString());
                eventDetails.put(getString(R.string.db_field_name_solve_count), Long.parseLong(solveCount.getText().toString()));
                eventDetails.put(getString(R.string.db_field_name_result_calc_method), resultCalcMethodSpinner.getSelectedItem().toString());

                if(competitionEvent == null)
                    eventDetailsReference.add(eventDetails).addOnCompleteListener(task -> Toast.makeText(this, "Event added successfully.", Toast.LENGTH_SHORT).show());
                else
                    eventDetailsReference.document(competitionEvent.eventId).update(eventDetails).addOnCompleteListener(task -> Toast.makeText(this, "Event updated successfully.", Toast.LENGTH_SHORT).show());

                editEventDialog.dismiss();
            }
        });
        editEventDialog.show();
    }



    // Function to get Timestamp from DatePicker & TimePicker
    private void getDate(TextView textView, Timestamp prevTime, String sharedPrefField)
    {
        final Calendar calendar = Calendar.getInstance();

        if(prevTime != null)
            calendar.setTimeInMillis(prevTime.getSeconds()*1000);

        int currYear = calendar.get(Calendar.YEAR);
        int currMonth = calendar.get(Calendar.MONTH);
        int currDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currMinute = calendar.get(Calendar.MINUTE);

        // Setup TimePicker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            // Setup DatePicker Dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view2, year, monthOfYear, dayOfMonth) -> {
                // Convert to Timestamp
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                // Set button text to selected value
                textView.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm a", new Timestamp(calendar.getTimeInMillis()/1000,0)));
                // Store comp dates
                compDetailsSharedPreferences.edit().putLong(sharedPrefField, calendar.getTimeInMillis()/1000).apply();
            }, currYear, currMonth, currDay);
            datePickerDialog.show();
        }, currHour, currMinute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }



    // Refresh activity without animations
    private void refreshActivity()
    {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}

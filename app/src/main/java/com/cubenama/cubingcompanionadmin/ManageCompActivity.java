package com.cubenama.cubingcompanionadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ManageCompActivity extends AppCompatActivity {

    SharedPreferences compDetailsSharedPreferences;
    SharedPreferences userAccessSharedPreferences;

    // Database reference
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_comp);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        EditText compNameEditText = findViewById(R.id.compNameEditText);
        EditText competitorLimitEditText = findViewById(R.id.competitorLimitEditText);

        compDetailsSharedPreferences = getSharedPreferences("comp_details", Context.MODE_PRIVATE);
        userAccessSharedPreferences = getSharedPreferences("organizer_details", Context.MODE_PRIVATE);

        Button regStartSelectButton = findViewById(R.id.regStartButton);
        Button regEndSelectButton = findViewById(R.id.regEndButton);
        Button compStartSelectButton = findViewById(R.id.startDateButton);
        Button compEndSelectButton = findViewById(R.id.endDateButton);
        Button updateButton = findViewById(R.id.updateCompButtom);
        Button manageScheduleButton = findViewById(R.id.manageScheduleButton);

        String comp_id = getIntent().getStringExtra("id");

        // Existing competition
        if(!comp_id.equals("new"))
        {
            // Get competition details from backend
            db.collection("competition_details").document(comp_id).get().addOnCompleteListener(task -> {
                compNameEditText.setText(task.getResult().getString("name"));
                competitorLimitEditText.setText(task.getResult().getLong("competitor_limit").toString());
                compStartSelectButton.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MM-yyyy  hh:mm", task.getResult().getTimestamp("start_time")));
                compEndSelectButton.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MM-yyyy  hh:mm", task.getResult().getTimestamp("end_time")));
                regStartSelectButton.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MM-yyyy  hh:mm", task.getResult().getTimestamp("registration_start_time")));
                regEndSelectButton.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MM-yyyy  hh:mm", task.getResult().getTimestamp("registration_end_time")));
            });
        }


        // Listeners for timeStamp values
        compStartSelectButton.setOnClickListener(v->{
            getDate(compStartSelectButton, "compStartDate");
        });
        compEndSelectButton.setOnClickListener(v->{
            getDate(compEndSelectButton, "compEndDate");
        });
        regStartSelectButton.setOnClickListener(v->{
            getDate(regStartSelectButton, "regStartDate");
        });
        regEndSelectButton.setOnClickListener(v->{
            getDate(regEndSelectButton, "regEndDate");
        });

        // Listener for opening schedule manager
        manageScheduleButton.setOnClickListener(v->{
            // Existing competition
            if(!comp_id.equals("new"))
            {
                // Open comp schedule manager
                Intent scheduleIntent = new Intent(this, ScheduleActivity.class);
                scheduleIntent.putExtra("comp_id", comp_id);
                startActivity(scheduleIntent);
            }
            // New competition
            else
                Toast.makeText(this, "Fill in competition details to proceed.", Toast.LENGTH_SHORT).show();
        });

        // Listener for updating comp
        updateButton.setOnClickListener(v->{
            // Check for issues
            if(!isNameValid(compNameEditText.getText().toString()))
            {
                Toast.makeText(this, "Competition Name invalid.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!isNumberValid(competitorLimitEditText.getText().toString()))
            {
                Toast.makeText(this, "Competitor limit invalid.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(compStartSelectButton.getText().toString().equals("Select") || compEndSelectButton.getText().toString().equals("Select") || regStartSelectButton.getText().toString().equals("Select") || regEndSelectButton.getText().toString().equals("Select"))
            {
                Toast.makeText(this, "Fill out all fields to proceed.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create comp HashMap
            Map<String, Object> compDetails = new HashMap<>();
            compDetails.put("competitor_limit", Long.parseLong(competitorLimitEditText.getText().toString()));
            compDetails.put("name", compNameEditText.getText().toString());
            compDetails.put("organizer", userAccessSharedPreferences.getString("name",""));
            compDetails.put("start_time", new Timestamp(compDetailsSharedPreferences.getLong("compStartDate",0),0));
            compDetails.put("end_time", new Timestamp(compDetailsSharedPreferences.getLong("compEndDate",0),0));
            compDetails.put("registration_start_time", new Timestamp(compDetailsSharedPreferences.getLong("regStartDate",0),0));
            compDetails.put("registration_end_time", new Timestamp(compDetailsSharedPreferences.getLong("regEndDate",0),0));

            // Existing competition
            if(!comp_id.equals("new"))
            {
                // Update competition on backend
                db.collection("competition_details").document(comp_id).update(compDetails).addOnCompleteListener(task -> {
                    Toast.makeText(this, "Competition details updated", Toast.LENGTH_SHORT).show();
                });
            }
            // New competition
            else {
                // Add competition on backend
                db.collection("competition_details").add(compDetails).addOnCompleteListener(task -> {
                    Toast.makeText(this, "Competition Uploaded.", Toast.LENGTH_SHORT).show();
                    // Design comp schedule next
                    Intent scheduleIntent = new Intent(this, ScheduleActivity.class);
                    scheduleIntent.putExtra("comp_id", task.getResult().getId());
                    startActivity(scheduleIntent);
                });
            }
        });
    }



    // Function to get Timestamp from DatePicker & TimePicker
    private void getDate(Button button, String field)
    {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int currYear = c.get(Calendar.YEAR);
        int currMonth = c.get(Calendar.MONTH);
        int currDay = c.get(Calendar.DAY_OF_MONTH);
        int currHour = c.get(Calendar.HOUR_OF_DAY);
        int currMinute = c.get(Calendar.MINUTE);

        // Setup TimePicker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            // Setup DatePicker Dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view2, year, monthOfYear, dayOfMonth) -> {
                // Convert to Timestamp
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                // Set button text to selected value
                button.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MM-yyyy  hh:mm", new Timestamp(calendar.getTimeInMillis()/1000,0)));
                // Store comp dates
                compDetailsSharedPreferences.edit().putLong(field, calendar.getTimeInMillis()/1000).apply();
            }, currYear, currMonth, currDay);
            datePickerDialog.show();
        }, currHour, currMinute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }



    // Function to check validity of comp name
    private boolean isNameValid(String name)
    {
        // Name can only have letters and numbers
        return name.matches("^[A-Z0-9a-z ]*$");
    }



    // Function to check validity of a number
    private boolean isNumberValid(String name)
    {
        return name.matches("^[0-9]*$");
    }

}

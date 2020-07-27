package com.cubenama.cubingcompanionadmin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.fonts.FontFamily;
import android.graphics.fonts.FontStyle;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.graphics.Typeface.createFromAsset;

public class CompetitionsActivity extends AppCompatActivity {

    // Database reference
    private FirebaseFirestore db;

    SharedPreferences userAccessSharedPreferences;
    SharedPreferences compDetailsSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competitions);

        compDetailsSharedPreferences = getSharedPreferences("comp_details", Context.MODE_PRIVATE);
        userAccessSharedPreferences = getSharedPreferences("organizer_details", Context.MODE_PRIVATE);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        ProgressBar progressBar = findViewById(R.id.progressBar);

                // Setup recycler views and adapters
        List<Comp> compList = new ArrayList<>();
        RecyclerView compListRecyclerView = findViewById(R.id.compListRecyclerView);

        CompAdapter compAdapter = new CompAdapter(compList, position -> Toast.makeText(this, compList.get(position).toString(), Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager compListLayoutManager = new LinearLayoutManager(this);

        compListRecyclerView.setLayoutManager(compListLayoutManager);
        compListRecyclerView.setAdapter(compAdapter);

        // Load existing comps
        db.collection(getString(R.string.db_field_name_comp_details)).addSnapshotListener((queryDocumentSnapshots, e) ->
        {
            compList.clear();
            for (QueryDocumentSnapshot comp : queryDocumentSnapshots)
            {
                if(comp.getString(getString(R.string.db_field_name_organizer)).equals(userAccessSharedPreferences.getString("name","")))
                    compList.add(new Comp(comp.getId(), comp.getString(getString(R.string.db_field_name_name)), comp.getString(getString(R.string.db_field_name_organizer)), comp.getLong(getString(R.string.db_field_name_competitor_limit)), comp.getBoolean(getString(R.string.db_field_name_results_verified)) , comp.getTimestamp(getString(R.string.db_field_name_start_time)), comp.getTimestamp(getString(R.string.db_field_name_end_time)), comp.getTimestamp(getString(R.string.db_field_name_reg_start_time)), comp.getTimestamp(getString(R.string.db_field_name_reg_end_time))));
            }
            compAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        });

        // Button to add new comp
        Button addCompButton = findViewById(R.id.addCompButton);
        addCompButton.setOnClickListener(v -> showCompDetailsDialog(null));
    }


    // Function to show dialog box with competition details
    void showCompDetailsDialog(Comp competition)
    {
        // Confirmation dialog
        final Dialog editCompDialog = new Dialog(this);
        editCompDialog.setContentView(R.layout.dialog_edit_comp);
        editCompDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText compName = editCompDialog.findViewById(R.id.compName);
        EditText competitorLimit = editCompDialog.findViewById(R.id.competitorLimit);

        TextView startTime = editCompDialog.findViewById(R.id.compStartTime);
        TextView endTime = editCompDialog.findViewById(R.id.compEndTime);
        TextView regStartTime = editCompDialog.findViewById(R.id.regStartTime);
        TextView regEndTime = editCompDialog.findViewById(R.id.regEndTime);

        ImageView startTimeButton = editCompDialog.findViewById(R.id.compStartTimeButton);
        ImageView endTimeButton = editCompDialog.findViewById(R.id.compEndTimeButton);
        ImageView regStartTimeButton = editCompDialog.findViewById(R.id.regStartTimeButton);
        ImageView regEndTimeButton = editCompDialog.findViewById(R.id.regEndTimeButton);
        ImageView deleteCompButton = editCompDialog.findViewById(R.id.deleteCompButton);

        Button updateButton = editCompDialog.findViewById(R.id.updateButton);
        Button resultsButton = editCompDialog.findViewById(R.id.resultsButton);
        Button scheduleButton = editCompDialog.findViewById(R.id.scheduleButton);

        if(competition != null)
        {
            compName.setText(competition.name);
            competitorLimit.setText(String.valueOf(competition.competitorLimit));

            startTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm a", competition.startTime));
            endTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm a", competition.endTime));
            regStartTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm a", competition.regStartTime));
            regEndTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy  hh:mm a", competition.regEndTime));

            startTimeButton.setOnClickListener(v -> getDate(startTime, competition.startTime, "start_time"));
            endTimeButton.setOnClickListener(v -> getDate(startTime, competition.endTime, "end_time"));
            regStartTimeButton.setOnClickListener(v -> getDate(startTime, competition.regStartTime, "reg_start_time"));
            regEndTimeButton.setOnClickListener(v -> getDate(startTime, competition.regEndTime, "reg_end_time"));

            scheduleButton.setOnClickListener(v -> {
                editCompDialog.dismiss();
                Intent eventDetailsIntent = new Intent(this, EventDetailsActivity.class);
                eventDetailsIntent.putExtra("comp_id", competition.id);
                startActivity(eventDetailsIntent);
            });

            resultsButton.setOnClickListener(v -> {
                editCompDialog.dismiss();
                Intent resultEventsIntent = new Intent(this, ResultEventsActivity.class);
                resultEventsIntent.putExtra("comp_id", competition.id);
                startActivity(resultEventsIntent);
            });
        }
        else
        {
            startTimeButton.setOnClickListener(v -> getDate(startTime, null, "start_time"));
            endTimeButton.setOnClickListener(v -> getDate(endTime, null, "end_time"));
            regStartTimeButton.setOnClickListener(v -> getDate(regStartTime, null, "reg_start_time"));
            regEndTimeButton.setOnClickListener(v -> getDate(regEndTime, null, "reg_end_time"));

            updateButton.setText("Add");
            scheduleButton.setVisibility(View.GONE);
            resultsButton.setVisibility(View.GONE);
        }

        updateButton.setOnClickListener(v -> {
            // Validity check
            if(!isNameValid(compName.getText().toString()))
            {
                Toast.makeText(this, "Competition Name invalid.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!isNumberValid(competitorLimit.getText().toString()))
            {
                Toast.makeText(this, "Competitor limit invalid.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(startTime.getText().toString().equals(getString(R.string.date_time_placeholder)) || endTime.getText().toString().equals(getString(R.string.date_time_placeholder)) || regStartTime.getText().toString().equals(getString(R.string.date_time_placeholder)) || regEndTime.getText().toString().equals(getString(R.string.date_time_placeholder)))
            {
                Toast.makeText(this, "Fill out all fields to proceed.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create comp HashMap
            Map<String, Object> compDetails = new HashMap<>();
            compDetails.put(getString(R.string.db_field_name_competitor_limit), Long.parseLong(competitorLimit.getText().toString()));
            compDetails.put(getString(R.string.db_field_name_name), compName.getText().toString());
            compDetails.put(getString(R.string.db_field_name_organizer), userAccessSharedPreferences.getString("name",""));
            compDetails.put(getString(R.string.db_field_name_start_time), new Timestamp(compDetailsSharedPreferences.getLong("start_time",0),0));
            compDetails.put(getString(R.string.db_field_name_end_time), new Timestamp(compDetailsSharedPreferences.getLong("end_time",0),0));
            compDetails.put(getString(R.string.db_field_name_reg_start_time), new Timestamp(compDetailsSharedPreferences.getLong("reg_start_time",0),0));
            compDetails.put(getString(R.string.db_field_name_reg_end_time), new Timestamp(compDetailsSharedPreferences.getLong("reg_end_time",0),0));
            compDetails.put(getString(R.string.db_field_name_results_verified), false);

            if(competition != null) {
                // Update competition on backend
                db.collection(getString(R.string.db_field_name_comp_details)).document(competition.id).update(compDetails).addOnCompleteListener(task -> {
                    Toast.makeText(this, "Competition details updated", Toast.LENGTH_SHORT).show();
                });
            }
            else {
                // Add competition on backend
                db.collection(getString(R.string.db_field_name_comp_details)).add(compDetails).addOnCompleteListener(task -> {
                    Toast.makeText(this, "Competition created. Please add events.", Toast.LENGTH_SHORT).show();
                    Intent eventDetailsIntent = new Intent(this, EventDetailsActivity.class);
                    eventDetailsIntent.putExtra("comp_id", task.getResult().getId());
                    Log.d("CC_COMP", "New competition ID : " + task.getResult().getId());
                    startActivity(eventDetailsIntent);
                    editCompDialog.dismiss();
                });
            }

        });

        deleteCompButton.setOnClickListener(v -> {
            AlertDialog deleteDialog = new AlertDialog.Builder(this)
                    .setTitle("Delete Competition")
                    .setMessage("Confirm competition deletion. Once deleted, no data can be recovered. Schedule, Results and other documents related to the competition will also be erased.")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        // Delete competition
                        db.collection(getString(R.string.db_field_name_comp_details)).document(competition.id).delete().addOnCompleteListener(task -> {
                            Toast.makeText(this, "Competition has been deleted.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            editCompDialog.dismiss();
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            // Set font
            TextView message = deleteDialog.findViewById(android.R.id.message);
            Typeface typeface = ResourcesCompat.getFont(this, R.font.abel);
            message.setTypeface(typeface);
        });

        editCompDialog.show();
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



    // Function to check validity of comp name
    private boolean isNameValid(String name)
    {
        // Name can only have letters and numbers
        return name.matches("^[A-Z0-9a-z._ ]*$");
    }



    // Function to check validity of a number
    private boolean isNumberValid(String name)
    {
        return name.matches("^[0-9]*$");
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}

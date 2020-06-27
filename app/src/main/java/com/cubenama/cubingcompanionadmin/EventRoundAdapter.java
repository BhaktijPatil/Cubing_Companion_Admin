package com.cubenama.cubingcompanionadmin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventRoundAdapter extends RecyclerView.Adapter<EventRoundAdapter.MyViewHolder>{

    private List<EventRound> eventRoundsList;
    private String eventId;
    private ScheduleActivity.ClickListener clickListener;

    Context context;

    private SharedPreferences compDetailsSharedPreferences;

    // Database reference
    private FirebaseFirestore db;

    @NonNull
    @Override
    public EventRoundAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_round, parent, false);

        context = parent.getContext();

        // Create database instance
        db = FirebaseFirestore.getInstance();
        // Get comp ID in shared preferences
        compDetailsSharedPreferences = context.getSharedPreferences("comp_details", Context.MODE_PRIVATE);

        return new EventRoundAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull EventRoundAdapter.MyViewHolder holder, int position) {
        EventRound eventRound = eventRoundsList.get(position);

        // Assign values to list row
        holder.roundId.setText("Round : " + eventRound.roundNo);
        holder.participantCount.setText("Criteria : Top " + eventRound.participantCount);
        holder.roundStartTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy   hh:mm", eventRound.startTimestamp));
        holder.roundEndTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy   hh:mm", eventRound.endTimestamp));

        // Create a reference to current round
        DocumentReference roundReference = db.collection("competition_details").document(compDetailsSharedPreferences.getString("comp_id","0")).collection("schedule").document(eventId).collection("rounds").document(eventRound.roundId);
        
        // Open popup to edit round
        holder.roundDetails.setOnClickListener(v-> {
            // Get inflater for the current activity
            LayoutInflater inflater = LayoutInflater.from(context);
            // Inflate the popup window layout (manage round)
            View popupManageRoundView = inflater.inflate(R.layout.popup_manage_round, null);
            // Setup popup window
            PopupWindow popupWindow = new PopupWindow(popupManageRoundView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
            // Show popup view at the center
            popupWindow.showAtLocation(popupManageRoundView, Gravity.CENTER, 0, 0);

            EditText participantCount = popupManageRoundView.findViewById(R.id.participantCount);
            EditText roundId = popupManageRoundView.findViewById(R.id.roundId);
            Button roundStartTime = popupManageRoundView.findViewById(R.id.roundStartTime);
            Button roundEndTime = popupManageRoundView.findViewById(R.id.roundEndTime);

            // Get current details
            roundId.setText(String.valueOf(eventRound.roundNo));
            participantCount.setText(String.valueOf(eventRound.participantCount));
            roundStartTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy   hh:mm", eventRound.startTimestamp));
            roundEndTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy   hh:mm", eventRound.endTimestamp));

            // Listeners for timeStamp values
            roundStartTime.setOnClickListener(roundStartView->{
                getDate(roundStartTime, "roundStartTime");
            });
            roundEndTime.setOnClickListener(roundEndView->{
                getDate(roundEndTime, "roundEndTime");
            });

            // Listener to delete round
            Button deleteRoundButton = popupManageRoundView.findViewById(R.id.deleteRoundButton);
            deleteRoundButton.setVisibility(View.VISIBLE);
            deleteRoundButton.setOnClickListener(view-> {
                roundReference.delete().addOnCompleteListener(task -> {
                    Toast.makeText(context, "Round deleted.", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                });
            });

            // Listener to add scrambles
            Button scramblesButton = popupManageRoundView.findViewById(R.id.scramblesButton);
            scramblesButton.setVisibility(View.VISIBLE);
            scramblesButton.setOnClickListener(view-> {
                // Dismiss current window
                popupWindow.dismiss();
                // Inflate the popup window layout (manage scrambles)
                View popupManageScramblesView = inflater.inflate(R.layout.popup_manage_scrambles, null);
                // Setup popup window
                PopupWindow popupScramblesWindow = new PopupWindow(popupManageScramblesView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
                // Show popup view at the center
                popupScramblesWindow.showAtLocation(popupManageScramblesView, Gravity.CENTER, 0, 0);

                // Setup recycler views and adapters
                List<String> scrambleList = new ArrayList<>();
                RecyclerView scrambleRecyclerView = popupManageScramblesView.findViewById(R.id.scrambleRecyclerView);

                ScrambleAdapter scrambleAdapter = new ScrambleAdapter(scrambleList, position1 -> Toast.makeText(context, scrambleList.get(position), Toast.LENGTH_SHORT).show());
                RecyclerView.LayoutManager scrambleLayoutManager = new LinearLayoutManager(context);

                scrambleRecyclerView.setLayoutManager(scrambleLayoutManager);
                scrambleRecyclerView.setAdapter(scrambleAdapter);

                // Load existing scrambles
                roundReference.get().addOnCompleteListener(roundTask -> {
                    scrambleList.addAll(0, (ArrayList<String>) roundTask.getResult().get("scrambles"));
                    scrambleAdapter.notifyDataSetChanged();
                });

                // Listener to update scrambles
                Button updateScrambles = popupManageScramblesView.findViewById(R.id.updateScramblesButton);
                updateScrambles.setOnClickListener(v1->{
                    ArrayList<String> tempList = new ArrayList<>();
                    // Get current scrambles
                    for (int i = 0; i < scrambleList.size(); i++)
                        tempList.add(((EditText) scrambleRecyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.scrambleEditText)).getText().toString());
                    // Dismiss popup
                    roundReference.update("scrambles", tempList).addOnCompleteListener(task -> {
                        Toast.makeText(context, "Scrambles updated successfully.", Toast.LENGTH_SHORT).show();
                        popupScramblesWindow.dismiss();
                    });
                });

            });

            // Listener to update round
            Button updateRoundButton = popupManageRoundView.findViewById(R.id.addRoundButton);
            updateRoundButton.setText("Update");
            updateRoundButton.setOnClickListener(view->{
                if(participantCount.getText().toString().equals("") || roundId.getText().toString().equals("") || roundStartTime.getText().toString().equals("Select") || roundEndTime.getText().toString().equals("Select"))
                {
                    Toast.makeText(context, "Fill out all fields to proceed.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create round HashMap
                Map<String, Object> roundDetails = new HashMap<>();
                roundDetails.put("participant_count", Long.parseLong(participantCount.getText().toString()));
                roundDetails.put("round_id", Long.parseLong(roundId.getText().toString()));
                roundDetails.put("start_time", new Timestamp(compDetailsSharedPreferences.getLong("roundStartTime",0),0));
                roundDetails.put("end_time", new Timestamp(compDetailsSharedPreferences.getLong("roundEndTime",0),0));

                roundReference.update(roundDetails).addOnCompleteListener(task -> {
                    Toast.makeText(context, "Round updated successfully.", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                });
            });
        });

    }



    @Override
    public int getItemCount() {
        return eventRoundsList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ConstraintLayout roundDetails;
        TextView roundId;
        TextView participantCount;
        TextView roundStartTime;
        TextView roundEndTime;


        private WeakReference<ScheduleActivity.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, ScheduleActivity.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            roundId = itemView.findViewById(R.id.roundId);
            participantCount = itemView.findViewById(R.id.qualifyingCriteria);
            roundEndTime = itemView.findViewById(R.id.roundEndTime);
            roundStartTime = itemView.findViewById(R.id.roundStartTime);
            roundDetails = itemView.findViewById(R.id.roundDetails);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    EventRoundAdapter(Event event, ScheduleActivity.ClickListener clickListener){
        this.eventRoundsList = event.eventRounds;
        this.clickListener = clickListener;
        this.eventId = event.eventId;
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
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
            // Setup DatePicker Dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view2, year, monthOfYear, dayOfMonth) -> {
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
        }, currHour, currMinute, DateFormat.is24HourFormat(context));
        timePickerDialog.show();
    }



}
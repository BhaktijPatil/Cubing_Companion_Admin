package com.cubenama.cubingcompanionadmin;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder>{

    private List<Event> eventsList;
    private ScheduleActivity.ClickListener clickListener;
    private Context context;

    private SharedPreferences compDetailsSharedPreferences;

    // Database reference
    private FirebaseFirestore db;

    @NonNull
    @Override
    public EventAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_event, parent, false);

        context = parent.getContext();

        // Create database instance
        db = FirebaseFirestore.getInstance();
        // Get comp ID in shared preferences
        compDetailsSharedPreferences = context.getSharedPreferences("comp_details", Context.MODE_PRIVATE);

        return new EventAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull EventAdapter.MyViewHolder holder, int position) {
        Event event = eventsList.get(position);

        // Setup inner recycler view
        EventRoundAdapter eventRoundAdapter = new EventRoundAdapter(event, innerPosition -> Toast.makeText(context, Integer.toString(innerPosition + 1), Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager eventRoundLayoutManager = new LinearLayoutManager(context);

        holder.competitionEventRecyclerView.setLayoutManager(eventRoundLayoutManager);
        holder.competitionEventRecyclerView.setAdapter(eventRoundAdapter);

        eventRoundAdapter.notifyDataSetChanged();

        // Assign values to list row
        holder.eventNameTextView.setText(event.eventName);
        holder.solveCountTextView.setText("Bo" + event.solveCount);

        // Listener to modify/delete event
        holder.eventNameTextView.setOnClickListener(v->{
            // Get inflater for the current activity
            LayoutInflater inflater = LayoutInflater.from(context);
            // Inflate the popup window layout (add event)
            View popupManageEventView = inflater.inflate(R.layout.popup_manage_event, null);
            // Setup popup window
            PopupWindow popupWindow = new PopupWindow(popupManageEventView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
            // Show popup view at the center
            popupWindow.showAtLocation(popupManageEventView, Gravity.CENTER, 0, 0);

            // Select method for result calculation
            Spinner resultCalcMethodSpinner = popupManageEventView.findViewById(R.id.resultCalcMethodSpinner);
            // Create an adapter for resultCalcMethodSpinner
            BetterListAdapter betterListAdapter = new BetterListAdapter((Activity) context, context.getResources().getStringArray(R.array.result_calc_methods));
           // Dropdown text layout
            betterListAdapter.setDropDownViewResource(R.layout.spinner_item_result_calc_method);
            // Apply the adapter to the resultCalcMethodSpinner
            resultCalcMethodSpinner.setAdapter(betterListAdapter);
            // Set default to average
            resultCalcMethodSpinner.setSelection(0);
            
            EditText eventName = popupManageEventView.findViewById(R.id.newEventName);
            EditText solveCount = popupManageEventView.findViewById(R.id.solveCount);

            // Get current details
            db.collection("competition_details").document(compDetailsSharedPreferences.getString("comp_id","0")).collection("schedule").document(event.eventId).get().addOnCompleteListener(task -> {
                eventName.setText(task.getResult().getString("name"));
                solveCount.setText(task.getResult().getLong("solve_count").toString());

            });

            // Listener to update event
            Button updateEventButton = popupManageEventView.findViewById(R.id.addEventButton);
            updateEventButton.setText("Update");
            updateEventButton.setOnClickListener(view->{
                if(eventName.getText().toString().equals("") || solveCount.getText().toString().equals(""))
                {
                    Toast.makeText(context, "Fill out all fields to proceed.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create event HashMap
                Map<String, Object> eventDetails = new HashMap<>();
                eventDetails.put("solve_count", Long.parseLong(solveCount.getText().toString()));
                eventDetails.put("name", eventName.getText().toString());
                eventDetails.put("result_calc_method", resultCalcMethodSpinner.getSelectedItem().toString());

                db.collection("competition_details").document(compDetailsSharedPreferences.getString("comp_id","0")).collection("schedule").document(event.eventId).update(eventDetails).addOnCompleteListener(task -> {
                    Toast.makeText(context, "Event updated successfully.", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                });
            });

            // Listener to delete event
            Button deleteEventButton = popupManageEventView.findViewById(R.id.deleteEventButton);
            deleteEventButton.setVisibility(View.VISIBLE);
            deleteEventButton.setOnClickListener(view->{
                db.collection("competition_details").document(compDetailsSharedPreferences.getString("comp_id","0")).collection("schedule").document(event.eventId).delete().addOnCompleteListener(task -> {
                    Toast.makeText(context, "Event deleted.", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                });
            });
        });

        // Listener to add new round
        holder.addRoundButton.setOnClickListener(v->{
            // Get inflater for the current activity
            LayoutInflater inflater = LayoutInflater.from(context);
            // Inflate the popup window layout (add event)
            View popupLinkAccountView = inflater.inflate(R.layout.popup_manage_round, null);
            // Setup popup window
            PopupWindow popupWindow = new PopupWindow(popupLinkAccountView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
            // Show popup view at the center
            popupWindow.showAtLocation(popupLinkAccountView, Gravity.CENTER, 0, 0);
            EditText participantCount = popupLinkAccountView.findViewById(R.id.participantCount);
            EditText roundId = popupLinkAccountView.findViewById(R.id.roundId);

            Button roundStartTime = popupLinkAccountView.findViewById(R.id.roundStartTime);
            Button roundEndTime = popupLinkAccountView.findViewById(R.id.roundEndTime);

            // Listeners for timeStamp values
            roundStartTime.setOnClickListener(roundStartView->{
                getDate(roundStartTime, "roundStartTime");
            });
            roundEndTime.setOnClickListener(roundEndView->{
                getDate(roundEndTime, "roundEndTime");
            });

            // Listener to add new round
            Button addRoundButton = popupLinkAccountView.findViewById(R.id.addRoundButton);
            addRoundButton.setOnClickListener(view->{
                if(participantCount.getText().toString().equals("") || roundId.getText().toString().equals("") || roundStartTime.getText().toString().equals("Select") || roundEndTime.getText().toString().equals("Select"))
                {
                    Toast.makeText(context, "Fill out all fields to proceed.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add default scrambles
                ArrayList<String> default_scrambles = new ArrayList<>();
                for (long i = 1; i <= event.solveCount; i++)
                    default_scrambles.add("Scramble No." + i);

                // Create round HashMap
                Map<String, Object> roundDetails = new HashMap<>();
                roundDetails.put("scrambles", default_scrambles);
                roundDetails.put("participant_count", Long.parseLong(participantCount.getText().toString()));
                roundDetails.put("round_id", Long.parseLong(roundId.getText().toString()));
                roundDetails.put("start_time", new Timestamp(compDetailsSharedPreferences.getLong("roundStartTime",0),0));
                roundDetails.put("end_time", new Timestamp(compDetailsSharedPreferences.getLong("roundEndTime",0),0));

                db.collection("competition_details").document(compDetailsSharedPreferences.getString("comp_id","0")).collection("schedule").document(event.eventId).collection("rounds").add(roundDetails).addOnCompleteListener(task -> {
                    Toast.makeText(context, "Round added successfully.", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                });
            });
        });
    }



    @Override
    public int getItemCount() {
        return eventsList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView eventNameTextView;
        TextView solveCountTextView;
        Button addRoundButton;
        RecyclerView competitionEventRecyclerView = itemView.findViewById(R.id.roundList);

        private WeakReference<ScheduleActivity.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, ScheduleActivity.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            eventNameTextView = itemView.findViewById(R.id.eventName);
            addRoundButton = itemView.findViewById(R.id.addRoundButton);
            solveCountTextView =itemView.findViewById(R.id.solveCount);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }



    EventAdapter(List<Event> eventsList, ScheduleActivity.ClickListener clickListener){
        this.eventsList = eventsList;
        this.clickListener = clickListener;
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

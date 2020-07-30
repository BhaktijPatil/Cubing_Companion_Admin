package com.cubenama.cubingcompanionadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ResultDetailsActivity extends AppCompatActivity {

    // Database reference
    private FirebaseFirestore db;
    CollectionReference resultDetailsReference;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_details);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Show loading screen
        progressBar = findViewById(R.id.progressBar);

        TextView eventNameTextView = findViewById(R.id.eventNameTextView);
        TextView roundIdTextView = findViewById(R.id.roundIdTextView);
        TextView singleHeaderTextView = findViewById(R.id.bestSingleHeaderTextView);
        TextView finalResultHeaderTextView = findViewById(R.id.finalResultHeaderTextView);
        TextView noResultsTextView = findViewById(R.id.noResultsTextView);

        ImageView oopsImageView = findViewById(R.id.oopsImageView);
        ConstraintLayout resultDetailsHeaderLayout = findViewById(R.id.resultDetailsHeaderLayout);

        if(getIntent().getStringExtra("result_calc_method").equals("Single"))
            singleHeaderTextView.setVisibility(View.INVISIBLE);
        finalResultHeaderTextView.setText(getIntent().getStringExtra("result_calc_method"));

        eventNameTextView.setText(getIntent().getStringExtra("event_name"));
        roundIdTextView.setText("Round " + getIntent().getLongExtra("round_no", 1));

        // Setup recycler views and adapters
        List<RoundResult> roundResultList = new ArrayList<>();
        RecyclerView roundResultRecyclerView = findViewById(R.id.roundResultRecyclerView);

        RoundResultAdapter roundResultAdapter = new RoundResultAdapter(roundResultList, position -> Toast.makeText(this, roundResultList.get(position).wcaId, Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager roundResultLayoutManager = new LinearLayoutManager(this);

        roundResultRecyclerView.setLayoutManager(roundResultLayoutManager);
        roundResultRecyclerView.setAdapter(roundResultAdapter);

        resultDetailsReference = db.collection(getString(R.string.db_field_name_comp_details))
                .document(getIntent().getStringExtra("comp_id"))
                .collection(getString(R.string.db_field_name_events))
                .document(getIntent().getStringExtra("event_id"))
                .collection(getString(R.string.db_field_name_rounds))
                .document(getIntent().getStringExtra("round_id"))
                .collection(getString(R.string.db_field_name_results));

        resultDetailsReference.orderBy(getString(R.string.db_field_name_final_result), Query.Direction.ASCENDING).get().addOnCompleteListener(resultDetailsTask -> {
            progressBar.setVisibility(View.GONE);
            // No results found
            if(resultDetailsTask.getResult().isEmpty())
            {
                Log.d("CC_RESULTS", "No results found for the round.");
                resultDetailsHeaderLayout.setVisibility(View.GONE);
                oopsImageView.setVisibility(View.VISIBLE);
                noResultsTextView.setVisibility(View.VISIBLE);
                return;
            }
            List<RoundResult> podiumResultList = new ArrayList<>();
            roundResultList.clear();
            long podiumCount = 3;
            for(QueryDocumentSnapshot result : resultDetailsTask.getResult())
            {
                RoundResult roundResult = new RoundResult(result.getId(), result.getString(getString(R.string.db_field_name_name)), result.getString(getString(R.string.db_field_name_wca_id)), result.getLong(getString(R.string.db_field_name_final_result)), result.getLong(getString(R.string.db_field_name_single)), (ArrayList<Long>) result.get(getString(R.string.db_field_name_time_list)), result.getBoolean(getString(R.string.db_field_name_is_verified)));
                // Add to separate list for top 3 verified results
                if(roundResult.isVerified && podiumCount != 0)
                {
                    podiumResultList.add(roundResult);
                    podiumCount --;
                }
                else
                    roundResultList.add(roundResult);
            }
            // Add verified results at the beginning
            roundResultList.addAll(0, podiumResultList);
            roundResultAdapter.notifyDataSetChanged();
            resultDetailsHeaderLayout.setVisibility(View.VISIBLE);
            noResultsTextView.setVisibility(View.GONE);
            oopsImageView.setVisibility(View.GONE);
        });
    }



    // Function to DNF the solve
    void dnfSolve(TextView textView, ImageView dnfImageView)
    {
        // Set listener to undo DNF
        String prevTime = textView.getText().toString();
        textView.setText("DNF");
        dnfImageView.setOnClickListener(v-> undoDNF(textView, dnfImageView, prevTime));
    }



    // Function to undo DNF
    void undoDNF(TextView textView, ImageView dnfImageView, String prevTime)
    {
        // Restore previous time
        textView.setText(prevTime);
        // Listener to Redo DNF
        dnfImageView.setOnClickListener(v-> dnfSolve(textView, dnfImageView));
    }



    // Function to set solve time in textview
    void setTime(long time, TextView textView)
    {
        if(time == ResultCodes.DNF_CODE)
            textView.setText("DNF");
        else if(time == ResultCodes.DNS_CODE)
            textView.setText("DNS");
        else {
            DateFormat dateFormat = new SimpleDateFormat("mm:ss.SS");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            textView.setText(dateFormat.format(time));
        }
    }



    // Show dialog to edit time
    void showEditTimeDialog(TextView textView)
    {
        final Dialog editTimeDialog = new Dialog(this);
        editTimeDialog.setContentView(R.layout.dialog_time_picker);
        editTimeDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button cancelButton = editTimeDialog.findViewById(R.id.cancelButton);
        Button updateButton = editTimeDialog.findViewById(R.id.updateButton);

        EditText minutesEditText = editTimeDialog.findViewById(R.id.minutesEditText);
        EditText secondsEditText = editTimeDialog.findViewById(R.id.secondsEditText);
        EditText milliSecondsEditText = editTimeDialog.findViewById(R.id.milliSecondsEditText);

        cancelButton.setOnClickListener(view -> editTimeDialog.dismiss());
        updateButton.setOnClickListener(view -> {
            String minutes = minutesEditText.getText().toString();
            String seconds = secondsEditText.getText().toString();
            String millis = milliSecondsEditText.getText().toString();

            // Check time validity
            if(minutes.length() != 2 || seconds.length() != 2 || millis.length() != 2)
                Toast.makeText(this, "Time should be in the format MM:SS.mm .", Toast.LENGTH_SHORT).show();
            else if(Long.parseLong(millis) > 99 && Long.parseLong(millis) < 0)
                Toast.makeText(this, "Invalid milli seconds value.", Toast.LENGTH_SHORT).show();
            else if(Long.parseLong(seconds) > 59 && Long.parseLong(seconds) < 0)
                Toast.makeText(this, "Invalid seconds value.", Toast.LENGTH_SHORT).show();
            else if(Long.parseLong(minutes) > 59 && Long.parseLong(minutes) < 0)
                Toast.makeText(this, "Invalid minutes value.", Toast.LENGTH_SHORT).show();
            else {
                textView.setText(minutes + ":" + seconds + "." + millis);
                editTimeDialog.dismiss();
            }
        });
        editTimeDialog.show();
    }



    // Needed to handle button on clicks within recycler view
    public interface ClickListener {
        void onPositionClicked(int position);
    }
}

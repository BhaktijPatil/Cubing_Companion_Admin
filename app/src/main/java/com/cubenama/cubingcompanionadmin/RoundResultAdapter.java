package com.cubenama.cubingcompanionadmin;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class RoundResultAdapter extends RecyclerView.Adapter<RoundResultAdapter.MyViewHolder>{
    private List<RoundResult> roundResultList;
    private ResultDetailsActivity.ClickListener clickListener;
    private Context context;

    @NonNull
    @Override
    public RoundResultAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_result_details, parent, false);

        context = parent.getContext();
        return new RoundResultAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull RoundResultAdapter.MyViewHolder holder, int position) {
        RoundResult result = roundResultList.get(position);

        // Change rank colour for those proceeding to next round
        if(position < ((Activity)context).getIntent().getLongExtra("qualification_criteria", 0))
            holder.rankTextView.setTextColor(context.getColor(R.color.colorAccent));
        
        // Assign values to list row
        holder.rankTextView.setText(String.valueOf(position + 1));
        holder.nameTextView.setText(result.name);

        if(((Activity)context).getIntent().getStringExtra("result_calc_method").equals("Single"))
            holder.singleTextView.setVisibility(View.INVISIBLE);

        holder.resultVerifiedSwitch.setChecked(result.isVerified);

        holder.singleTextView.setText(formatTime(result.single));
        holder.resultTextView.setText(formatTime(result.result));

        // Upload verification status
        holder.resultVerifiedSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ((ResultDetailsActivity)context).progressBar.setVisibility(View.VISIBLE);
            Log.d("CC_RESULT_ID", result.id);
            ((ResultDetailsActivity)context).resultDetailsReference.document(result.id).update(context.getString(R.string.db_field_name_is_verified), isChecked).addOnCompleteListener(task -> {
                ((ResultDetailsActivity)context).progressBar.setVisibility(View.GONE);
                // Refresh activity
                ((ResultDetailsActivity) context).finish();
                ((ResultDetailsActivity) context).overridePendingTransition(0, 0);
                context.startActivity(((ResultDetailsActivity) context).getIntent());
                ((ResultDetailsActivity) context).overridePendingTransition(0, 0);
            });
        });

        // Show dialog with result details
        holder.resultDetailsLayout.setOnClickListener(v-> {
            final Dialog manageResultsDialog = new Dialog(context);
            manageResultsDialog.setContentView(R.layout.dialog_manage_result);
            manageResultsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // Setup recycler views and adapters
            RecyclerView timeListRecyclerView = manageResultsDialog.findViewById(R.id.timeListRecyclerView);

            ManageSolveAdapter manageSolveAdapter = new ManageSolveAdapter(context, result.timeList, pos -> Toast.makeText(context, String.valueOf(result.timeList.get(position)), Toast.LENGTH_SHORT).show());
            RecyclerView.LayoutManager manageSolveLayoutManager = new LinearLayoutManager(context);

            timeListRecyclerView.setLayoutManager(manageSolveLayoutManager);
            timeListRecyclerView.setAdapter(manageSolveAdapter);
            manageSolveAdapter.notifyDataSetChanged();

            TextView finalResultTextView = manageResultsDialog.findViewById(R.id.finalResultTextView);
            ((ResultDetailsActivity)context).setTime(result.result, finalResultTextView);
            TextView competitorNameTextView = manageResultsDialog.findViewById(R.id.competitorNameTextView);
            competitorNameTextView.setText(result.name + "'s Result :");

            ImageView dnfImageView = manageResultsDialog.findViewById(R.id.dnfImageView);
            dnfImageView.setOnClickListener(view -> ((ResultDetailsActivity)context).dnfSolve(finalResultTextView, dnfImageView));

            ImageView setResultImageView = manageResultsDialog.findViewById(R.id.setResultImageView);
            setResultImageView.setOnClickListener(view -> ((ResultDetailsActivity)context).showEditTimeDialog(finalResultTextView));
            
            Button updateButton = manageResultsDialog.findViewById(R.id.updateButton);
            updateButton.setOnClickListener(view -> {
                ArrayList<Long> tempList = new ArrayList<>();
                // Retrieve times from recycler view
                for (int i = 0; i < result.timeList.size(); i++)
                    tempList.add(convertTime(((TextView) timeListRecyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.solveTimeTextView)).getText().toString()));

                Map<String, Object> resultDetails = new HashMap<>();
                resultDetails.put(context.getString(R.string.db_field_name_time_list), tempList);
                resultDetails.put(context.getString(R.string.db_field_name_final_result), convertTime(finalResultTextView.getText().toString()));
                
                // Update new times to DB
                ((ResultDetailsActivity)context).resultDetailsReference.document(result.id).update(resultDetails).addOnCompleteListener(updateResultsTask -> {
                    manageResultsDialog.dismiss();
                    // Refresh activity
                    ((ResultDetailsActivity) context).finish();
                    ((ResultDetailsActivity) context).overridePendingTransition(0, 0);
                    context.startActivity(((ResultDetailsActivity) context).getIntent());
                    ((ResultDetailsActivity) context).overridePendingTransition(0, 0);
                });
            });

            manageResultsDialog.show();
        });
    }



    // Function to convert mm:ss.SS to milliseconds
    private long convertTime(String solveTime)
    {
        if(solveTime.equals("DNF"))
            return ResultCodes.DNF_CODE;
        if(solveTime.equals("DNS"))
            return ResultCodes.DNS_CODE;

        long minutes = Long.parseLong(solveTime.substring(0,2));
        long seconds = Long.parseLong(solveTime.substring(3,5));
        long milliseconds = Long.parseLong(solveTime.substring(6,8)) * 10;
        // Calculate solve time in milliseconds
        return minutes * 60 * 1000 + seconds * 1000 + milliseconds;
    }



    @Override
    public int getItemCount() { return roundResultList.size(); }

    @Override
    public int getItemViewType(int position) { return position; }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView rankTextView;
        TextView nameTextView;
        TextView singleTextView;
        TextView resultTextView;

        Switch resultVerifiedSwitch;

        ConstraintLayout resultDetailsLayout;

        private WeakReference<ResultDetailsActivity.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, ResultDetailsActivity.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            nameTextView = itemView.findViewById(R.id.competitorNameTextView);
            singleTextView = itemView.findViewById(R.id.bestSingleTextView);
            resultTextView = itemView.findViewById(R.id.finalResultTextView);
            resultDetailsLayout = itemView.findViewById(R.id.resultDetailsLayout);
            resultVerifiedSwitch = itemView.findViewById(R.id.resultVerifiedSwitch);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    RoundResultAdapter(List<RoundResult> roundResultList, ResultDetailsActivity.ClickListener clickListener){
        this.roundResultList = roundResultList;
        this.clickListener = clickListener;
    }

    private String formatTime(long time)
    {
        DateFormat dateFormat = new SimpleDateFormat("mm:ss.SS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        if(time == ResultCodes.DNF_CODE)
            return "DNF";
        if(time == ResultCodes.DNS_CODE)
            return "DNS";
        else
            return dateFormat.format(time);
    }
}

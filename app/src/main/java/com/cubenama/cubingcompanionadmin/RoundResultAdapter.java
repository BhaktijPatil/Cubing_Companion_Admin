package com.cubenama.cubingcompanionadmin;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
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
                ((ResultDetailsActivity) context).startActivity(((ResultDetailsActivity) context).getIntent());
                ((ResultDetailsActivity) context).overridePendingTransition(0, 0);
            });
        });

        // Show dialog with result details
        holder.resultDetailsLayout.setOnClickListener(v-> {
//            final Dialog resultDetailsDialog = new Dialog(context, R.style.Theme_AppCompat_NoActionBar);
//            resultDetailsDialog.setContentView(R.layout.dialog_box_result_details);
//            Window window = resultDetailsDialog.getWindow();
//            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            window.setGravity(Gravity.CENTER);
//            window.setBackgroundDrawableResource(android.R.color.transparent);
//
//            TextView nameTextView = resultDetailsDialog.findViewById(R.id.nameTextView);
//            TextView wcaIdTextView = resultDetailsDialog.findViewById(R.id.wcaIdTextView);
//            TextView timeListTextView = resultDetailsDialog.findViewById(R.id.timeListTextView);
//            TextView singleTextView = resultDetailsDialog.findViewById(R.id.singleTextView);
//            TextView finalResultTextView = resultDetailsDialog.findViewById(R.id.finalResultTextView);
//
//            ImageView verifiedImageView = resultDetailsDialog.findViewById(R.id.verifiedImageView);
//
//            // Show tick if results are verified
//            if(result.isVerified)
//                verifiedImageView.setVisibility(View.VISIBLE);
//            else
//                verifiedImageView.setVisibility(View.GONE);
//
//            wcaIdTextView.setText(result.wcaId);
//            nameTextView.setText(result.name);
//            singleTextView.setText("Single : " + formatTime(result.single));
//            finalResultTextView.setText(((Activity)context).getIntent().getStringExtra("result_calc_method") + " : " + formatTime(result.result));
//
//            String times = "";
//            for(long time : result.timeList)
//            {
//                times += formatTime(time) + ",  ";
//            }
//
//            timeListTextView.setText(times.substring(0, times.length() - 3));
//            resultDetailsDialog.show();
        });
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

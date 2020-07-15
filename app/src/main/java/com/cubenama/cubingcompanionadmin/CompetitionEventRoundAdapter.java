package com.cubenama.cubingcompanionadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.WeakReference;

public class CompetitionEventRoundAdapter extends RecyclerView.Adapter<CompetitionEventRoundAdapter.MyViewHolder>{

    private CompetitionEvent competitionEvent;
    private EventDetailsActivity.ClickListener clickListener;

    private Context context;

    @NonNull
    @Override
    public CompetitionEventRoundAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_round, parent, false);

        context = parent.getContext();

        return new CompetitionEventRoundAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull CompetitionEventRoundAdapter.MyViewHolder holder, int position) {
        CompetitionEventRound competitionEventRound = competitionEvent.competitionEventRounds.get(position);

        // Assign values to list row
        holder.roundId.setText("Round " + competitionEventRound.roundNo);
        holder.qualificationCriteria.setText("Qualification criteria : Top " + competitionEventRound.qualificationCriteria);
        holder.roundStartTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy   hh:mm a", competitionEventRound.startTime));
        holder.roundEndTime.setText(new DateTimeFormat().firebaseTimestampToDate("dd-MMM-yyyy   hh:mm a", competitionEventRound.endTime));

        holder.roundDetails.setOnClickListener(v -> ((EventDetailsActivity)context).showRoundManager(competitionEvent, position));
    }



    @Override
    public int getItemCount() {
        return competitionEvent.competitionEventRounds.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ConstraintLayout roundDetails;
        TextView roundId;
        TextView qualificationCriteria;
        TextView roundStartTime;
        TextView roundEndTime;

        private WeakReference<EventDetailsActivity.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, EventDetailsActivity.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            roundId = itemView.findViewById(R.id.roundId);
            qualificationCriteria = itemView.findViewById(R.id.qualificationCriteria);
            roundEndTime = itemView.findViewById(R.id.roundEndTime);
            roundStartTime = itemView.findViewById(R.id.roundStartTime);
            roundDetails = itemView.findViewById(R.id.roundDetails);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    CompetitionEventRoundAdapter(CompetitionEvent event, EventDetailsActivity.ClickListener clickListener){
        this.competitionEvent = event;
        this.clickListener = clickListener;
    }
}
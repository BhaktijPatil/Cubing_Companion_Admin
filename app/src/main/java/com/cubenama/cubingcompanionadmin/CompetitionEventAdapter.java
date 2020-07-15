package com.cubenama.cubingcompanionadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

public class CompetitionEventAdapter extends RecyclerView.Adapter<CompetitionEventAdapter.MyViewHolder>{

    private List<CompetitionEvent> eventsList;
    private EventDetailsActivity.ClickListener clickListener;
    private Context context;

    @NonNull
    @Override
    public CompetitionEventAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_event, parent, false);

        context = parent.getContext();

        return new CompetitionEventAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull CompetitionEventAdapter.MyViewHolder holder, int position) {
        CompetitionEvent event = eventsList.get(position);

        // Setup inner recycler view
        CompetitionEventRoundAdapter competitionEventRoundAdapter = new CompetitionEventRoundAdapter(event, innerPosition -> Toast.makeText(context, Integer.toString(innerPosition + 1), Toast.LENGTH_SHORT).show());
        RecyclerView.LayoutManager eventRoundLayoutManager = new LinearLayoutManager(context);

        holder.competitionEventRecyclerView.setLayoutManager(eventRoundLayoutManager);
        holder.competitionEventRecyclerView.setAdapter(competitionEventRoundAdapter);

        competitionEventRoundAdapter.notifyDataSetChanged();

        // Assign values to list row
        holder.eventNameTextView.setText(event.eventName);
        switch (event.resultCalcMethod)
        {
            case "Average" : holder.solveCountTextView.setText("Ao" + event.solveCount);break;
            case "Mean" : holder.solveCountTextView.setText("Mo" + event.solveCount);break;
            case "Single" : holder.solveCountTextView.setText("So" + event.solveCount);break;
        }
        // Listener to modify/delete event
        holder.eventCardView.setOnClickListener(v -> ((EventDetailsActivity) context).showEventManager(event));
        // Listener to add round
        holder.addRoundButton.setOnClickListener(v -> ((EventDetailsActivity) context).showRoundManager(event, 999));
    }



    @Override
    public int getItemCount() { return eventsList.size(); }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView eventCardView;
        TextView eventNameTextView;
        TextView solveCountTextView;
        Button addRoundButton;
        RecyclerView competitionEventRecyclerView = itemView.findViewById(R.id.roundList);

        private WeakReference<EventDetailsActivity.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, EventDetailsActivity.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            eventCardView = itemView.findViewById(R.id.eventCardView);
            eventNameTextView = itemView.findViewById(R.id.eventName);
            addRoundButton = itemView.findViewById(R.id.addRoundButton);
            solveCountTextView =itemView.findViewById(R.id.solveCount);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }



    CompetitionEventAdapter(List<CompetitionEvent> eventsList, EventDetailsActivity.ClickListener clickListener){
        this.eventsList = eventsList;
        this.clickListener = clickListener;
    }
}

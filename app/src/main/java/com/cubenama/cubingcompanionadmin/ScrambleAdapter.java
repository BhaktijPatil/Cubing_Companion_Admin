package com.cubenama.cubingcompanionadmin;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

public class ScrambleAdapter extends RecyclerView.Adapter<ScrambleAdapter.MyViewHolder>{

    private EventDetailsActivity.ClickListener clickListener;
    private List<String> scrambleList;

    @NonNull
    @Override
    public ScrambleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_edit_scramble, parent, false);

        return new ScrambleAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull ScrambleAdapter.MyViewHolder holder, int position) {
        String scramble = scrambleList.get(position);
        // Assign values to list row
        holder.scrambleEditText.setText(scramble);
    }



    @Override
    public int getItemCount() {
        return scrambleList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        EditText scrambleEditText;

        private WeakReference<EventDetailsActivity.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, EventDetailsActivity.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            scrambleEditText = itemView.findViewById(R.id.scrambleEditText);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    ScrambleAdapter(List<String> scrambleList, EventDetailsActivity.ClickListener clickListener){
        this.scrambleList = scrambleList;
        this.clickListener = clickListener;
    }
}
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

public class ScrambleAdapter extends RecyclerView.Adapter<ScrambleAdapter.MyViewHolder>{

    private ScheduleActivity.ClickListener clickListener;
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

        private WeakReference<ScheduleActivity.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, ScheduleActivity.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            scrambleEditText = itemView.findViewById(R.id.scrambleEditText);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    ScrambleAdapter(List<String> scrambleList, ScheduleActivity.ClickListener clickListener){
        this.scrambleList = scrambleList;
        this.clickListener = clickListener;
    }
}
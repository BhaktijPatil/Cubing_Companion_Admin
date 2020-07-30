package com.cubenama.cubingcompanionadmin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class ManageSolveAdapter extends RecyclerView.Adapter<ManageSolveAdapter.MyViewHolder> {
    private ResultDetailsActivity.ClickListener clickListener;
    private List<Long> timeList;
    private Context context;

    @NonNull
    @Override
    public ManageSolveAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_edit_solve, parent, false);

        return new ManageSolveAdapter.MyViewHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageSolveAdapter.MyViewHolder holder, int position) {
        Long solveTime = timeList.get(position);

        // Show solve time
        ((ResultDetailsActivity)context).setTime(solveTime, holder.solveTimeTextView);

        // Listener to DNF solve
        holder.dnfImageView.setOnClickListener(v ->  ((ResultDetailsActivity)context).dnfSolve(holder.solveTimeTextView, holder.dnfImageView));
        // Listener to edit time
        holder.editTimeImageView.setOnClickListener(v -> ((ResultDetailsActivity)context).showEditTimeDialog(holder.solveTimeTextView));
    }



    @Override
    public int getItemCount() {
        return timeList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView solveTimeTextView;
        ImageView editTimeImageView;
        ImageView dnfImageView;

        private WeakReference<ResultDetailsActivity.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, ResultDetailsActivity.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            solveTimeTextView = itemView.findViewById(R.id.solveTimeTextView);
            editTimeImageView = itemView.findViewById(R.id.editTimeImageView);
            dnfImageView = itemView.findViewById(R.id.dnfImageView);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }



    ManageSolveAdapter(Context context, List<Long> timeList, ResultDetailsActivity.ClickListener clickListener){
        this.context = context;
        this.timeList = timeList;
        this.clickListener = clickListener;
    }
}

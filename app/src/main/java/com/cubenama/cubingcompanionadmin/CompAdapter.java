package com.cubenama.cubingcompanionadmin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

public class CompAdapter extends RecyclerView.Adapter<CompAdapter.MyViewHolder>{

    private List<Comp> compList;
    private CompsListActivity.ClickListener clickListener;
    private Context context;



    @NonNull
    @Override
    public CompAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_comp, parent, false);

        context = parent.getContext();

        return new CompAdapter.MyViewHolder(itemView, clickListener);
    }



    @Override
    public void onBindViewHolder(@NonNull CompAdapter.MyViewHolder holder, int position) {
        Comp comp = compList.get(position);

        // Assign values to list row
        holder.compName.setText(comp.name);
        holder.compName.setOnClickListener(v->{
            Intent manageCompIntent = new Intent(context, ManageCompActivity.class);
            manageCompIntent.putExtra("id", comp.id);
            context.startActivity(manageCompIntent);
        });
    }



    @Override
    public int getItemCount() {
        return compList.size();
    }



    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView compName;

        private WeakReference<CompsListActivity.ClickListener> listenerRef;

        MyViewHolder(@NonNull View itemView, CompsListActivity.ClickListener listener) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            compName = itemView.findViewById(R.id.compName);
        }
        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition());
        }
    }

    CompAdapter(List<Comp> compList, CompsListActivity.ClickListener clickListener){
        this.compList = compList;
        this.clickListener = clickListener;
    }
}
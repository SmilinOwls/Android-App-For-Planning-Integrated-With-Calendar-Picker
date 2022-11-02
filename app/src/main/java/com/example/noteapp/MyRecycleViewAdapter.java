package com.example.noteapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyRecycleViewAdapter extends RecyclerView.Adapter<MyRecycleViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<Events> eventsArrayList;

    public MyRecycleViewAdapter(Context context, ArrayList<Events> eventsArrayList) {
        this.context = context;
        this.eventsArrayList = eventsArrayList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView eventDate, eventName, eventTime;
        Button eventDelete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventName = itemView.findViewById(R.id.eventName);
            eventTime = itemView.findViewById(R.id.eventTime);
            eventDelete = itemView.findViewById(R.id.eventDelete);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_single_display,null);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Events events = eventsArrayList.get(position);
        holder.eventName.setText(events.getEVENT());
        holder.eventDate.setText(events.getDATE());
        holder.eventTime.setText(events.getTIME());
        holder.eventDelete.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                DBSelector dbSelector;
                dbSelector = new DBSelector(context);
                dbSelector.DeleteEvent(events.getEVENT(),events.getDATE(),events.getTIME(),dbSelector.getWritableDatabase());
                dbSelector.close();
                eventsArrayList.remove(events);
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return eventsArrayList.size();
    }
}

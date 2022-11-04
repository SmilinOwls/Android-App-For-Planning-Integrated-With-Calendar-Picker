package com.example.noteapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MyRecycleViewAdapter extends RecyclerView.Adapter<MyRecycleViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<Events> eventsArrayList;
    DBSelector dbSelector;

    public MyRecycleViewAdapter(Context context, ArrayList<Events> eventsArrayList) {
        this.context = context;
        this.eventsArrayList = eventsArrayList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView eventDate, eventName, eventTime;
        Button eventDelete;
        ImageButton eventAlert;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventName = itemView.findViewById(R.id.eventName);
            eventTime = itemView.findViewById(R.id.eventTime);
            eventDelete = itemView.findViewById(R.id.eventDelete);
            eventAlert = itemView.findViewById(R.id.eventAlert);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_single_display,null);

        return new MyViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Events events = eventsArrayList.get(position);
        holder.eventName.setText(events.getEVENT());
        holder.eventDate.setText(events.getDATE());
        holder.eventTime.setText(events.getTIME());
        holder.eventDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbSelector = new DBSelector(context);
                dbSelector.DeleteEvent(events.getEVENT(),events.getDATE(),events.getTIME(),dbSelector.getWritableDatabase());
                dbSelector.close();
                eventsArrayList.remove(events);
                notifyDataSetChanged();
            }
        });


        if(checkNotified(events.getDATE(),events.getTIME(),events.getEVENT())){
            holder.eventAlert.setImageResource(R.drawable.ic_action_notification_active);
        } else{
            holder.eventAlert.setImageResource(R.drawable.ic_action_notification_inactive);
        }

        holder.eventAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String event = events.getEVENT(),
                        date = events.getDATE(),
                        time = events.getTIME();
                if(checkNotified(date,time,event)){
                    holder.eventAlert.setImageResource(R.drawable.ic_action_notification_inactive);
                    alertCancel(getEventID(date,time,event));
                    updateEvent(event,date,time,"off");
                    notifyDataSetChanged();
                } else{

                    holder.eventAlert.setImageResource(R.drawable.ic_action_notification_active);
                    Calendar dateCal = Calendar.getInstance(),
                            timeCal = Calendar.getInstance();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("K:mm", Locale.ENGLISH);

                    try {
                        dateCal.setTime(Objects.requireNonNull(dateFormat.parse(events.getDATE())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    try {
                        timeCal.setTime(Objects.requireNonNull(timeFormat.parse(events.getTIME())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    dateCal.set(Calendar.HOUR_OF_DAY,timeCal.get(Calendar.HOUR_OF_DAY));
                    dateCal.set(Calendar.MINUTE,timeCal.get(Calendar.MINUTE));
                    alertRelease(dateCal,getEventID(date,time,event),event,time);
                    updateEvent(event,date,time,"on");
                    notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return eventsArrayList.size();
    }

    protected boolean checkNotified(String date, String time, String event){
        boolean isNotified = false;
        DBSelector dbSelector;
        dbSelector = new DBSelector(context);
        Cursor cursor = dbSelector.ReadEventIDAndNotify(event,date,time,dbSelector.getReadableDatabase());

        while (cursor.moveToNext()){
            @SuppressLint("Range")
            String notify = cursor.getString(cursor.getColumnIndex(DBDefinition.NOTIFICATION));
            if(notify.equals("on")){
                isNotified = true;
            } else{
                isNotified = false;
            }
        }

        dbSelector.close();
        return isNotified;
    }

    @SuppressLint("Range")
    protected int getEventID(String date, String time, String event){
        int ID = 0;
        dbSelector = new DBSelector(context);
        Cursor cursor = dbSelector.ReadEventIDAndNotify(event,date,time,dbSelector.getReadableDatabase());
        while (cursor.moveToNext()){
            ID = cursor.getInt(cursor.getColumnIndex(DBDefinition.ID));
        }
        dbSelector.close();
        return ID;
    }

    protected void alertRelease(Calendar calendar, int id, String event, String time){
        Intent intent = new Intent(context.getApplicationContext(),MyAlertBroadcast.class);
        Bundle bundle = new Bundle();
        bundle.putString("EVENT",event);
        bundle.putInt("ID",id);
        bundle.putString("TIME",time);
        intent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,id,intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.get(Calendar.MILLISECOND),pendingIntent);
    }

    protected void alertCancel(int id){
        Intent intent = new Intent(context.getApplicationContext(),MyAlertBroadcast.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,id,intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    protected void updateEvent(String event, String date, String time, String notification){
        dbSelector = new DBSelector(context);
        dbSelector.UpdateOnNotifyEvent(event,date,time,notification,dbSelector.getWritableDatabase());
        dbSelector.close();
    }
}

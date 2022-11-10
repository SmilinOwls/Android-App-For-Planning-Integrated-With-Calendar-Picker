package com.example.noteapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class CustomCalendarGridView extends LinearLayout {

    ImageButton nextBtn, previousBtn;
    TextView currentDate;
    GridView gridView;
    private static final int MAX_CALENDAR_DAYS = 42; // = 7(columns)*6(rows)
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    Context context;

    private final int PLACE_PICKER_REQUEST = 1;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy",Locale.ENGLISH);
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM",Locale.ENGLISH);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy",Locale.ENGLISH);

    List<Date> dateList = new ArrayList<>();
    List<Events> eventsList = new ArrayList<>();

    DBSelector dbSelector;
    MyArrayAdapter myArrayAdapter;
    int lastClicked = -1;
    Calendar curCal = Calendar.getInstance(Locale.ENGLISH);
    public CustomCalendarGridView(Context context) {
        super(context);

    }

    public CustomCalendarGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.calendar_layout,this);

        nextBtn = (ImageButton) view.findViewById(R.id.nextBtn);
        previousBtn = (ImageButton) view.findViewById(R.id.previousBtn);
        currentDate = (TextView) view.findViewById(R.id.currentDate);
        gridView = (GridView) view.findViewById(R.id.gridView);
        updateCalendar();

        previousBtn.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextBtn.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH,1);
            updateCalendar();
        });


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            EditText eventName;
            ImageButton eventTimeSelector;
            TextView eventTime;
            Button eventDone;
            AlertDialog alertDialog;
            CheckBox eventAlert;
            EditText eventPlace;
            ImageButton eventPlaceSelector;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                final View newView = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_event_layout,null);
                builder.setView(newView);

                eventName = (EditText) newView.findViewById(R.id.eventName);
                eventTimeSelector = (ImageButton) newView.findViewById(R.id.timeSetForEvent);
                eventTime = (TextView) newView.findViewById(R.id.timeEvent);
                eventDone = (Button) newView.findViewById(R.id.doneBtn);
                eventAlert = (CheckBox) newView.findViewById(R.id.alert);
                IntStream.range(0, parent.getChildCount())
                        .<TextView>mapToObj(i -> parent.getChildAt(i).findViewById(R.id.singleCalendarDay))
                        .forEach(textView -> textView.setBackgroundResource(R.drawable.default_state_item));

                view.findViewById(R.id.singleCalendarDay).setBackgroundResource(R.drawable.pressed_state_item);
                lastClicked = position;


                Calendar calendar1 = Calendar.getInstance();
                eventTimeSelector.setOnClickListener(v -> {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("K:mm a",Locale.ENGLISH);
                    Calendar calendar = Calendar.getInstance();
                    int hours = calendar.get(Calendar.HOUR_OF_DAY);
                    int minutes = calendar.get(Calendar.MINUTE);
                    TimePickerDialog timePickerDialog =
                            new TimePickerDialog(newView.getContext(), R.style.MyAlertDialogStyle, (view1, hourOfDay, minute) -> {
                                calendar1.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                calendar1.set(Calendar.MINUTE,minute);
                                calendar1.setTimeZone(TimeZone.getDefault());
                                eventTime.setText(simpleDateFormat.format(calendar1.getTime()));
                            },hours,minutes,false);
                    timePickerDialog.show();
                });


                Date selectedDate = dateList.get(position);


                eventDone.setOnClickListener(v -> {
                    // Save event which has been done
                    // then store it in SQLite database
                    dbSelector = new DBSelector(context);

                    AtomicReference<String> notify = new AtomicReference<>("off");
                    if(eventAlert.isChecked()) {
                        notify.set("on");
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(selectedDate);
                        cal.set(Calendar.HOUR_OF_DAY,calendar1.get(Calendar.HOUR_OF_DAY));
                        cal.set(Calendar.MINUTE,calendar1.get(Calendar.MINUTE));

                        String event_name = eventName.getText().toString(),
                                event_time = eventTime.getText().toString();

                        alertRelease(cal,getEventID(simpleDateFormat.format(selectedDate),event_time,event_name),event_name,event_time);
                    }
                    else{
                        // Do nothing
                    }

                    dbSelector.SaveEvent(notify.get(),
                            eventName.getText().toString(),
                            eventTime.getText().toString(),
                            simpleDateFormat.format(selectedDate),
                            monthFormat.format(selectedDate),
                            yearFormat.format(selectedDate),
                            dbSelector.getWritableDatabase());
                    updateCalendar();
                    alertDialog.dismiss();
                });

                alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        updateCalendar();
                    }
                });

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(alertDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                alertDialog.show();
                alertDialog.getWindow().setAttributes(lp);
            }
        });

        // When the single day cell is on long clicked, a dialog
        // will show and tell what events are going on
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            AlertDialog alertDialog;
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);

                final View eventShowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_display_layout,null);
                builder.setView(eventShowView);

                RecyclerView recyclerView = eventShowView.findViewById(R.id.eventDisplay);
                recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

                ArrayList<Events> arrayList = eventsListOfDate(simpleDateFormat.format(dateList.get(position)));

                MyRecycleViewAdapter myRecycleViewAdapter = new MyRecycleViewAdapter(eventShowView.getContext(),arrayList);
                recyclerView.setAdapter(myRecycleViewAdapter);
                myRecycleViewAdapter.notifyDataSetChanged();


                alertDialog = builder.create();
                alertDialog.show();

                alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        updateCalendar();
                    }
                });
                return true;
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST){
            if(resultCode == Activity.RESULT_OK){
                Place place = PlacePicker.getPlace(context,data);
                currentDate.setText(place.getAddress());
            }
        }
    }

    // Reload or initialise the current date, then
    // updating it to the new month on changed
    // by clicking next or previous button
    protected void updateCalendar(){
        dateList.clear();
        String bufferConvert = dateFormat.format(calendar.getTime());
        currentDate.setText(bufferConvert);
        Calendar monthCal = (Calendar) calendar.clone();
        monthCal.set(Calendar.DAY_OF_MONTH,1);
        int firstDayOfMonth = monthCal.get(Calendar.DAY_OF_WEEK);
        monthCal.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth + 1);

        eventsListOfMonth(monthFormat.format(calendar.getTime()),yearFormat.format(calendar.getTime()));

        IntStream.range(0, MAX_CALENDAR_DAYS).forEach(i -> {
            dateList.add(monthCal.getTime());
            monthCal.add(Calendar.DAY_OF_MONTH, 1);
        });

        myArrayAdapter = new MyArrayAdapter(context,dateList,eventsList,calendar,lastClicked,curCal);
        gridView.setAdapter(myArrayAdapter);
    }

    // list of events which all takes place in a single date
    protected ArrayList<Events> eventsListOfDate(String date){
        ArrayList<Events> arrayList = new ArrayList<>();

        dbSelector = new DBSelector(context);
        Cursor cursor = dbSelector.ReadEvent(date,dbSelector.getReadableDatabase());
        while (cursor.moveToNext()){
            @SuppressLint("Range")
            String EVENT = cursor.getString(cursor.getColumnIndex(DBDefinition.EVENT));
            @SuppressLint("Range")
            String TIME = cursor.getString(cursor.getColumnIndex(DBDefinition.TIME));
            @SuppressLint("Range")
            String DATE = cursor.getString(cursor.getColumnIndex(DBDefinition.DATE));
            @SuppressLint("Range")
            String MONTH = cursor.getString(cursor.getColumnIndex(DBDefinition.MONTH));
            @SuppressLint("Range")
            String YEAR = cursor.getString(cursor.getColumnIndex(DBDefinition.YEAR));

            Events event = new Events(EVENT,TIME,DATE,MONTH,YEAR);
            arrayList.add(event);
        }

        cursor.close();
        dbSelector.close();

        return arrayList;
    }

    // list of events which all takes place in a month
    protected void eventsListOfMonth(String month, String year){
        eventsList.clear();
        dbSelector = new DBSelector(context);
        Cursor cursor = dbSelector.ReadEventPerMonth(month,year,dbSelector.getReadableDatabase());
        while (cursor.moveToNext()){
            @SuppressLint("Range")
            String EVENT = cursor.getString(cursor.getColumnIndex(DBDefinition.EVENT));
            @SuppressLint("Range")
            String TIME = cursor.getString(cursor.getColumnIndex(DBDefinition.TIME));
            @SuppressLint("Range")
            String DATE = cursor.getString(cursor.getColumnIndex(DBDefinition.DATE));
            @SuppressLint("Range")
            String MONTH = cursor.getString(cursor.getColumnIndex(DBDefinition.MONTH));
            @SuppressLint("Range")
            String YEAR = cursor.getString(cursor.getColumnIndex(DBDefinition.YEAR));

            Events event = new Events(EVENT,TIME,DATE,MONTH,YEAR);
            eventsList.add(event);
        }

        cursor.close();
        dbSelector.close();
    }

    @SuppressLint("Range")
    protected int getEventID(String date, String time, String event){
        AtomicInteger ID = new AtomicInteger();
        dbSelector = new DBSelector(context);
        Cursor cursor = dbSelector.ReadEventIDAndNotify(event,date,time,dbSelector.getReadableDatabase());
        while (cursor.moveToNext()){
            ID.set(cursor.getInt(cursor.getColumnIndex(DBDefinition.ID)));
        }

        dbSelector.close();
        return ID.get();
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
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.get(Calendar.MILLISECOND),pendingIntent);

    }
}

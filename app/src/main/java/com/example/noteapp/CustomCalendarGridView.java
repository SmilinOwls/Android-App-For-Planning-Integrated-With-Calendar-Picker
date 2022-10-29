package com.example.noteapp;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.IntStream;

public class CustomCalendarGridView extends LinearLayout {

    ImageButton nextBtn, previousBtn;
    TextView currentDate;
    GridView gridView;
    private static int MAX_CALENDAR_DAYS = 42; // = 7(columns)*6(rows)
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    Context context;

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy",Locale.ENGLISH);
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM",Locale.ENGLISH);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy",Locale.ENGLISH);

    List<Date> dateList = new ArrayList<>();
    List<Events> eventsList = new ArrayList<>();

    DBSelector dbSelector;
    MyArrayAdapter myArrayAdapter;

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

                eventTimeSelector.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        int hours = calendar.get(Calendar.HOUR_OF_DAY);
                        int minutes = calendar.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog =
                                new TimePickerDialog(newView.getContext(), R.style.MyAlertDialogStyle, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        Calendar calendar1 = Calendar.getInstance();
                                        calendar1.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                        calendar1.set(Calendar.MINUTE,minute);
                                        calendar1.setTimeZone(TimeZone.getDefault());
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("K:mm a",Locale.ENGLISH);
                                        eventTime.setText(simpleDateFormat.format(calendar1.getTime()));
                                    }
                                },hours,minutes,false);
                        timePickerDialog.show();
                    }
                });

                eventDone.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Save event which has been done
                        // then store it in SQLite database
                        Date selectedDate = dateList.get(position);
                        dbSelector = new DBSelector(context);
                        dbSelector.SaveEvent(eventName.getText().toString(),eventTime.getText().toString(),dateFormat.format(selectedDate),monthFormat.format(selectedDate),yearFormat.format(selectedDate),dbSelector.getWritableDatabase());
                        updateCalendar();
                        alertDialog.dismiss();
                    }
                });

                alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(alertDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                alertDialog.show();
                alertDialog.getWindow().setAttributes(lp);
            }
        });
    }

    // Initialise the current date, then
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

        IntStream.range(0, MAX_CALENDAR_DAYS).forEach(i -> {
            dateList.add(monthCal.getTime());
            monthCal.add(Calendar.DAY_OF_MONTH, 1);
        });

        myArrayAdapter = new MyArrayAdapter(context,dateList,eventsList,calendar);
        gridView.setAdapter(myArrayAdapter);
    }
}

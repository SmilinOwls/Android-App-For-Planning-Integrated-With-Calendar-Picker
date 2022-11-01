package com.example.noteapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public class MyArrayAdapter extends ArrayAdapter {

    List<Date> dateList;
    List<Events> eventsList;
    Calendar currentDate;
    LayoutInflater layoutInflater;
    int lastClicked;
    int curDay = 0;
    int curMonth = 0;
    int curYear = 0;

    public MyArrayAdapter(@NonNull Context context, List<Date> dateList,List<Events> eventsList, Calendar currentDate,int lastClicked,Calendar calendar) {
        super(context, R.layout.item_calendar_layout);
        layoutInflater = LayoutInflater.from(context);
        this.dateList = dateList;
        this.eventsList = eventsList;
        this.currentDate = currentDate;
        this.lastClicked = lastClicked;
        curDay = calendar.get(Calendar.DAY_OF_MONTH);
        curMonth = calendar.get(Calendar.MONTH) + 1;
        curYear = calendar.get(Calendar.YEAR);
    }

    @SuppressLint({"CutPasteId", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if(convertView == null){
            view = layoutInflater.inflate(R.layout.item_calendar_layout,null);
        } else{
            view = convertView;
        }

        Calendar calendar = Calendar.getInstance();
        Date date = dateList.get(position);
        calendar.setTime(date);

        int dateOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int itemMonth = currentDate.get(Calendar.MONTH) + 1;
        int itemYear = currentDate.get(Calendar.YEAR);

        TextView templateDate = view.findViewById(R.id.singleCalendarDay);

        if(year == itemYear && month == itemMonth){
            // do nothing
        } else{
            templateDate.setBackgroundResource(R.drawable.focused_state_item);
            templateDate.setTextColor(Color.parseColor("#ECECEC"));
        }

        if(lastClicked == position){
            templateDate.setBackgroundResource(R.drawable.pressed_state_item);
        }

        if(curYear == year && curMonth == month && curDay == day){
            templateDate.setBackgroundResource(R.drawable.current_date);
            templateDate.setTextColor(getContext().getResources().getColor(R.color.white));
        }

        TextView txtSingleDay = (TextView) view.findViewById(R.id.singleCalendarDay);
        txtSingleDay.setText(String.valueOf(dateOfMonth));

        TextView txtEvent = (TextView) view.findViewById(R.id.eventID);

        Calendar eventCal = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        ArrayList<String> eventArrayList = new ArrayList<>();

        IntStream.range(0,eventsList.size()).forEach(i -> {
            try {
                eventCal.setTime(simpleDateFormat.parse(eventsList.get(i).getDATE()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(day == eventCal.get(Calendar.DAY_OF_MONTH)
            && month == eventCal.get(Calendar.MONTH) + 1
            && year == eventCal.get(Calendar.YEAR)){
                eventArrayList.add(eventsList.get(i).getEVENT());
                txtEvent.setText(eventArrayList.size() + " events");
            }

        });


        return view;
    }

    @Override
    public int getPosition(@Nullable Object item) {
        return dateList.indexOf(item);
    }

    @Override
    public int getCount() {
        return dateList.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return dateList.get(position);
    }
}

package com.example.noteapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyArrayAdapter extends ArrayAdapter {

    List<Date> dateList;
    List<Events> eventsList;
    Calendar currentDate;
    LayoutInflater layoutInflater;

    public MyArrayAdapter(@NonNull Context context, List<Date> dateList,List<Events> eventsList, Calendar currentDate) {
        super(context, R.layout.item_calendar_layout);
        layoutInflater = LayoutInflater.from(context);
        this.dateList = dateList;
        this.eventsList = eventsList;
        this.currentDate = currentDate;

    }

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
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int curMonth = currentDate.get(Calendar.MONTH) + 1;
        int curYear = currentDate.get(Calendar.YEAR);

        if(year == curYear && month == curMonth){

        }

        TextView textView = (TextView) view.findViewById(R.id.singleCalendarDay);
        textView.setText(String.valueOf(dateOfMonth));
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

package com.example.noteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;

public class MainActivity extends AppCompatActivity {

    CustomCalendarGridView customCalendarGridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customCalendarGridView = (CustomCalendarGridView) findViewById(R.id.main_view);
    }
}
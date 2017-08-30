package com.example.lps.simpleannotation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.InjectView;


public class MainActivity extends AppCompatActivity {
@InjectView(R.id.textview)
    TextView mTextView;
    @InjectView(R.id.btn)
    Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}

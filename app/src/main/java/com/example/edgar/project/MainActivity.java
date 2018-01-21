package com.example.edgar.project;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;


public class MainActivity extends Activity {

    RadioGroup vwAnswers;
    RadioButton inExam;
    Button begin;
    Spinner spinnerSub, spinnerYr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        begin = (Button) findViewById(R.id.btnBegin);
        spinnerSub = (Spinner) findViewById(R.id.spinnerSubject);
        spinnerYr = (Spinner) findViewById(R.id.spinnerYear);
        vwAnswers = (RadioGroup) findViewById(R.id.radioAnswers);
        inExam = (RadioButton) findViewById(R.id.inExam);
        inExam.setChecked(true);

        ArrayAdapter<CharSequence> subjectAdapter = ArrayAdapter.createFromResource(this, R.array.subjects, R.layout.spinner_layout);
        spinnerSub.setAdapter(subjectAdapter);
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this, R.array.years, R.layout.spinner_layout_year);
        spinnerYr.setAdapter(yearAdapter);
    }

    public void getQuestions(View view){
        boolean AnswersInTheEndOfExam = false;
        if(isOnline()){
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
            String sub = ((String) spinnerSub.getSelectedItem()).toLowerCase();
            String year = String.valueOf(spinnerYr.getSelectedItem());
            sub = sub.replace(' ', '_');
            String subYear = sub + year;

            int selected = vwAnswers.getCheckedRadioButtonId();
            RadioButton rb = (RadioButton) findViewById(selected);
            if((rb.getText().toString()).equals("At the end of exam")){
                AnswersInTheEndOfExam = true;
            }


            Intent in = new Intent(this, QuizClass.class);
            in.putExtra("subject", subYear);
            in.putExtra("viewAnswers", AnswersInTheEndOfExam);
            startActivity(in);
        } else{
            Toast.makeText(this, "Enable internet connectivity...", Toast.LENGTH_LONG).show();
        }
    }


    public boolean isOnline(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }




}

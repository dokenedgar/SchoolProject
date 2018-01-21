package com.example.edgar.project;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class index extends Activity {
    Button start, resume, highscore;
    SharedPreferences prefs, highscoreprefs;
    Boolean continueExam = false;
    Intent intent;
    //public static final String RAW_JSON = "saved_rawJSON";
    //QuizClass qc = new QuizClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_layout);

        start = (Button) findViewById(R.id.btnstart);
        resume = (Button) findViewById(R.id.btnresume);
        highscore = (Button) findViewById(R.id.btnhighscore);

      //  prefs = getPreferences(MODE_PRIVATE);
        prefs = this.getSharedPreferences("RAWJSON", MODE_PRIVATE);
        highscoreprefs = this.getSharedPreferences("HIGHSCORE", MODE_PRIVATE);

    }


    public void resumeExam(View view){
        String test = prefs.getString(QuizClass.RAW_JSON, "");
        if(test.equals("")){
            Toast.makeText(this, "No saved exam found..", Toast.LENGTH_LONG).show();
        }
        else{
            continueExam = true;
            Toast.makeText(this, "saved exam found..", Toast.LENGTH_LONG).show();
            intent = new Intent(this, QuizClass.class);
            intent.putExtra("resume", continueExam);
            startActivity(intent);

        }
    }

    public void startNew(View view){

        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void getHighScore(View view){
        final String HIGH_SCORE = "high_score";
         String HIGH_SCORE_SUB = "high_score_subject";
        SharedPreferences prefs;
        //prefs = getPreferences(MODE_PRIVATE);
        int score = highscoreprefs.getInt(HIGH_SCORE,0);
        String sub = highscoreprefs.getString(HIGH_SCORE_SUB,"");

        if(score == 0){
            Toast.makeText(this, "No high score found", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Score = "+score +" and Subject = "+sub, Toast.LENGTH_LONG).show();
        }
    }

}

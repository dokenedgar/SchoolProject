package com.example.edgar.project;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.Sharer;
import com.facebook.share.internal.ShareConstants;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;


public class Scores extends Activity {
    public static final String HIGH_SCORE = "high_score";
    public static final String HIGH_SCORE_SUB = "high_score_subject";
    SharedPreferences prefs;

    TextView txtview, highscore;
    public static int currentScores;
    String subject, stringScores;
    LoginButton login;
    CallbackManager callbackManager;

    ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.scores_layout);
        //new block
        //Get the final scores and total questions from previous activity
        Bundle extras = getIntent().getExtras();
        String totalQuestions = String.valueOf(extras.getInt("TotalQuestions"));
        currentScores = extras.getInt("FinalScores");
        subject = extras.getString("subject");
        // stringScores = String.valueOf(currentScores);
        // stringScores = Integer.toString(currentScores); converts int variable value to a string

        txtview = (TextView) findViewById(R.id.txtResult);
        txtview.setText("Scores:"+currentScores+" out of "+totalQuestions+" questions in "+subject);
        //prefs = getPreferences(MODE_PRIVATE);
        prefs = this.getSharedPreferences("HIGHSCORE", MODE_PRIVATE);

        highscore = (TextView) findViewById(R.id.txtHighscore);
        highscore.setText("Subject: "+prefs.getString(HIGH_SCORE_SUB, "")+" Points:"+prefs.getInt(HIGH_SCORE, 0));

        getHighScores();


        callbackManager = CallbackManager.Factory.create();
       // login = (LoginButton) findViewById(R.id.login_button);
      //  shareDialog = new ShareDialog(this);


        //login.setReadPermissions("public_profile");
       // login.setPublishPermissions("publish_actions");

        //login button method
     /*   login.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(AccessToken.getCurrentAccessToken() != null) {

                }
            }
        });*/

 /*       login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(AccessToken.getCurrentAccessToken() != null){
                    RequestData();
                    //ShareLinkContent content = new ShareLinkContent.Builder().build();
                   // shareDialog.show(content);
                   ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                           //.putString("og:type", "dokenspace:score")
                           //.putString("og:title", "45")
                           // .putString("scores", "http://samples.ogp.me/909559859190769")
                           .putString("og:type", "books.book")
                           .putString("og:title", "A Game of Thrones")
                           .putString("og:description", "In the frozen wastes to the north of Winterfell, sinister and supernatural forces are mustering.")
                           .putString("books:isbn", "0-553-57340-3")
                           .build();

                    //create action n link the obj to d action
                    ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                            //.setActionType("dokenspace:show")
                            //.putObject("score", object)
                            .setActionType("books.reads")
                            .putObject("books:book", object)
                            .build();

                    //finally, create d content model to represent d open graph story
                    ShareOpenGraphContent graphcontent = new ShareOpenGraphContent.Builder()
                            //.setPreviewPropertyName("score")
                            //.setAction(action)
                            .setPreviewPropertyName("books:book")
                            .setAction(action)
                            .build();
                    //ShareContent contentt = new ShareContent() {
                    //}
                    shareDialog.show(graphcontent);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
*/
        // Create an object
        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                .putString("og:type", "dokenspace:scores")
                .putString("og:title", "FINAL SCORE "+ Integer.toString(currentScores))
                .putString("og:description", "Think you can do better? Why not try the quiz now.")
               /*          .putString("scores", "http://samples.ogp.me/909559859190769")
                .putString("og:type", "books.book")
                .putString("og:title", "A Game of Thrones")
                .putString("og:description", "In the frozen wastes to the north of Winterfell, sinister and supernatural forces are mustering.")
                .putString("books:isbn", "0-553-57340-3")
                how bout adding score.title
                */
                .build();

// Create an action
        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("dokenspace:post")
                .putObject("scores", object)
              /*  .setActionType("dokenspace:show")
               */
                .build();

// Create the content
        ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("scores")
                .setAction(action)
              /*  .setPreviewPropertyName("books:book")
                .setAction(action)*/
                .build();

        ShareButton shareButton = (ShareButton)findViewById(R.id.shareButton);
        shareButton.setShareContent(content);
        shareButton.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.i("TAG", "Sharing success!");
                Toast.makeText(Scores.this, "Score shared on Facebook successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(Scores.this, "Sharing cancelled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("TAG", "SHARING ERROR! - "+ error.getMessage());
                Toast.makeText(Scores.this, "Share on Facebook operation failed!", Toast.LENGTH_SHORT).show();
            }
        });

/*
        //Get the final scores and total questions from previous activity
        Bundle extras = getIntent().getExtras();
        String totalQuestions = String.valueOf(extras.getInt("TotalQuestions"));
        currentScores = extras.getInt("FinalScores");
        subject = extras.getString("subject");
       // stringScores = String.valueOf(currentScores);
       // stringScores = Integer.toString(currentScores); converts int variable value to a string

        txtview = (TextView) findViewById(R.id.txtResult);
        txtview.setText("Scores:"+currentScores+" out of "+totalQuestions+" questions in "+subject);
        prefs = getPreferences(MODE_PRIVATE);

        highscore = (TextView) findViewById(R.id.txtHighscore);
        highscore.setText("Subject: "+prefs.getString(HIGH_SCORE_SUB, "")+" Points:"+prefs.getInt(HIGH_SCORE, 0));

        getHighScores();
*/
    }

    public void getHighScores(){
        if(currentScores > prefs.getInt(HIGH_SCORE,0)){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(HIGH_SCORE, currentScores);
            editor.putString(HIGH_SCORE_SUB, subject);

            editor.commit();
            highscore.setText("Subject: "+prefs.getString(HIGH_SCORE_SUB, "")+" Points:"+prefs.getInt(HIGH_SCORE, 0));
            Toast.makeText(this, "NEW HIGH SCORE!", Toast.LENGTH_LONG).show();
        }
    }

    public void RequestData() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                JSONObject json = response.getJSONObject();
                try{
                    if(json != null){
                       // String text = "<b>Name :</b> "+json.getString("name")+"<br><br><b>Email :</b> "+json.getString("email")+"<br><br><b>Profile link :</b> "+json.getString("link");
                       // details_txt.setText(Html.fromHtml(text));
                       // profile.setProfileId(json.getString("id"));
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}
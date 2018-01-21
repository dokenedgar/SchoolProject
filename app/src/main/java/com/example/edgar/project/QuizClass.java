package com.example.edgar.project;

        import android.app.Activity;
        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Color;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.Environment;
        import android.text.Html;
        import android.text.Spanned;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.RadioButton;
        import android.widget.RadioGroup;
        import android.widget.TextView;
        import android.widget.Toast;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.BufferedInputStream;
        import java.io.BufferedOutputStream;
        import java.io.BufferedReader;
        import java.io.DataOutputStream;
        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.util.ArrayList;


public class QuizClass extends Activity {
    static String fileName;
    File outFile;

    RadioGroup answers;
    RadioButton optA, optB, optC, optD;
    String question_no, question, imageUrl, optionA, optionB, optionC, optionD, correctAnswer;
    int qid, totalQuestions, scores, imgHeigt, imgWidth;
    quizQuestions Q = new quizQuestions();
    TextView txtQuestionNumber, txtquestions, examHeading;
    Button previous, next, save;
    ImageView questionImage;
    Bitmap questionBitmap; //not needed i think...
    LinearLayout lLayout;

    boolean AnswersAtEndOfExam, reachedEndOfExam;
    HttpURLConnection urlConnection;
    String subjectYear, json_string, completeRawJSON;  //stores JSON
    ArrayList<quizQuestions> questionslist = new ArrayList<>(); //stores the parsed JSON, separated into ques, optionsA-D, n correct answer
    String selectedAnswers[];  //stores the options that the user chose for each question; the users answer

    //for saving exam so user can resume later
    public static final String SUBJECT_YEAR = "saved_subject_year";
    public static final String QID = "saved_qid";
    public static final String SCORES = "saved_scores";
    public static final String ANSWERSATENDOFEXAM = "saved_AnswersAtEndOfExam";
    public static final String RAW_JSON = "saved_rawJSON";
    //array of selected answers (string)
    public static final String USERS_ANSWERS = "saved_usersanswer";
    SharedPreferences prefs;
    ProgressDialog pd;

    public QuizClass() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_class_layout);
        answers = (RadioGroup) findViewById(R.id.answersgroup);
        optA = (RadioButton) findViewById(R.id.optionA);
        optB = (RadioButton) findViewById(R.id.optionB);
        optC = (RadioButton) findViewById(R.id.optionC);
        optD = (RadioButton) findViewById(R.id.optionD);
        txtQuestionNumber = (TextView) findViewById(R.id.txtQuestNo);
        txtquestions = (TextView) findViewById(R.id.txtquestions);
        examHeading = (TextView) findViewById(R.id.currentExam);
        previous = (Button) findViewById(R.id.btnPrevious);
        next = (Button) findViewById(R.id.btnNext);
        save = (Button) findViewById(R.id.btnSave);
        questionImage = (ImageView) findViewById(R.id.questionImage);
        lLayout = (LinearLayout) findViewById(R.id.linearLayout);
        reachedEndOfExam = false;
        //prefs = getPreferences(MODE_PRIVATE);
        prefs = this.getSharedPreferences("RAWJSON", MODE_PRIVATE);
      //  pd = new ProgressDialog(this);



        if(savedInstanceState != null){ //what to do when bundle contains values, i.e probably activity is being re-created
            questionslist =  savedInstanceState.getParcelableArrayList("QuestionsArraylist");//come back to 'cast'
            selectedAnswers = savedInstanceState.getStringArray("usersAnswers");
            qid = savedInstanceState.getInt("QID");
            totalQuestions = savedInstanceState.getInt("TOTALQUESTIONS");
            scores = savedInstanceState.getInt("Scores");
            AnswersAtEndOfExam = savedInstanceState.getBoolean("viewAnswers");
            subjectYear = savedInstanceState.getString("currentExam");
            //After getting back the saved values, the question that the user was at should be displayed...
            DisplayQuestion();
            CheckIfAnswered();

       //     qid = savedInstanceState.getInt("key");
         //   optA.setText("some text here "+qid);
            Toast.makeText(this, "Bundle not empty", Toast.LENGTH_LONG).show();
            //optC.setText(totalQuestions);
        }
        else{

        Bundle extraData = getIntent().getExtras();
            if(extraData.containsKey("resume"))
            {
                //user wants to continue a saved exam
                /*
                 editor.putString(SUBJECT_YEAR, subjectYear);
        editor.putString(RAW_JSON, completeRawJSON);
        editor.putInt(QID, qid);
        editor.putInt(SCORES, scores);
        editor.putBoolean(ANSWERSATENDOFEXAM, AnswersAtEndOfExam);
        editor.putString(USERS_ANSWERS, selectedAnswers.toString());
                 */
                subjectYear = prefs.getString(SUBJECT_YEAR, "");
                completeRawJSON = prefs.getString(RAW_JSON, "");
                qid = prefs.getInt(QID, 0);
                scores = prefs.getInt(SCORES, 0);
                AnswersAtEndOfExam = prefs.getBoolean(ANSWERSATENDOFEXAM, false);
               // selectedAnswers = prefs.getString(USERS_ANSWERS, "");
                String json = prefs.getString(USERS_ANSWERS, "");
                parseJson(completeRawJSON);
                selectedAnswers = new String[questionslist.size()];
                totalQuestions = (questionslist.size()) - 1;

                if (json != null) {
                    try {
                        JSONArray a = new JSONArray(json);
                        for (int i = 0; i < a.length(); i++) {
                            String answer = a.optString(i);
                            //urls.add(url);
                            if(answer.equals("null")){
                                //do  not assign the value of answer to selectedAnswer if the value is "null"
                            }else{
                                selectedAnswers[i] = answer;
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                DisplayQuestion();
                CheckIfAnswered();

            }
            else{
                //user is starting new exam
                subjectYear = extraData.getString("subject");
                AnswersAtEndOfExam = extraData.getBoolean("viewAnswers");
                qid = 0;
                scores = 0;
                new loadQuestions().execute();
            }

        }

        examHeading.setText(subjectYear);
    }

    //@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("QuestionsArraylist", questionslist);
        outState.putStringArray("usersAnswers", selectedAnswers);
        outState.putInt("QID", qid);
        outState.putInt("TOTALQUESTIONS", totalQuestions);
        outState.putInt("Scores", scores);
        outState.putBoolean("viewAnswers", AnswersAtEndOfExam);
        outState.putString("currentExam", subjectYear);

    }


    //ADD BUTTONS NEXT AND PREVIOUS, AND THEIR FUNCTIONALITY

    //NextButton
    public void nextQuestion(View view){
        qid++;
        //totalQuestions = (questionslist.size()) - 1; this should be computed inside onPostExecute()

        if(qid == totalQuestions) { //qid equals number of available questions, i.e user is at the last question, change next btn's txt to get result
            next.setText("Get Result");
            Toast.makeText(QuizClass.this, "Thats the last question", Toast.LENGTH_LONG).show();
            DisplayQuestion();
            if (AnswersAtEndOfExam){//remember chosen options, but dont indicate whether correct or not
                if(reachedEndOfExam){
                        //show options chosen as well as whether correct or not
                    CheckIfAnswered();
                 }
                else{//not reached end of exam, just recall users options for answered questions
                    DisplayChosenOptions();
                }

            }
            else{
                CheckIfAnswered();
            }

        }

        else if(qid > totalQuestions){

            qid--;
            optA.setClickable(false);
            optB.setClickable(false);
            optC.setClickable(false);
            optD.setClickable(false);
            reachedEndOfExam = true;
            CheckIfAnswered();
            if(AnswersAtEndOfExam){//makes scores equal to zero
                scores = 0;
                for(int i=0; i<=totalQuestions; i++){
                    if(selectedAnswers[i] != null){
                        Q = questionslist.get(i);
                        correctAnswer = Q.getCorrectAnswer();
                        String usersAnswer = selectedAnswers[i];
                        if(usersAnswer.equals(correctAnswer)){
                            scores++;
                        }
                    }
                }
            }
            Intent in = new Intent(QuizClass.this, Scores.class);
            in.putExtra("TotalQuestions", questionslist.size());
            in.putExtra("FinalScores", scores);
            in.putExtra("subject", subjectYear);
            startActivity(in);
        }
        else{
            next.setText("NEXT");
            DisplayQuestion();
            if (AnswersAtEndOfExam){//remember chosen options, but dont indicate whether correct or not
                if(reachedEndOfExam){
                    //show options chosen as well as whether correct or not
                    CheckIfAnswered();
                }
                else{//not reached end of exam, just recall users options for answered questions
                    DisplayChosenOptions();
                }

            }
            else{
                CheckIfAnswered();
            }

        }
    }

    //Back button
    public void previousQuestion(View view){
        next.setText("NEXT");
        qid--;

        if(qid<0){ //qid is below first number of available questions
            Toast.makeText(QuizClass.this, "Thats the first question", Toast.LENGTH_LONG).show();
            qid++;
        }
        else{
            DisplayQuestion(); //if end of exam has been reached ans score has been shown, call checkIfAnswered, else call
            //CheckIfAnswered(); //method that only shows what the user has chosen
            if (AnswersAtEndOfExam){//remember chosen options, but dont inicate whether correct or not
                if(reachedEndOfExam){
                    //show options chosen as well as whether correct or not
                    CheckIfAnswered();
                }
                else{//not reached end of exam, just recall users options for answered questions
                    DisplayChosenOptions();
                }

            }
            else{
                CheckIfAnswered();
            }

        }
    }

    public void saveExam(View view){
        //save selectedAnswers, qid, scores,AnswersAtEndOfExam, subjectYear,
        //RawJSON--> parsed to questionslist, --> deduce totalQuestions

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(SUBJECT_YEAR, subjectYear);
        editor.putString(RAW_JSON, completeRawJSON);
        editor.putInt(QID, qid);
        editor.putInt(SCORES, scores);
        editor.putBoolean(ANSWERSATENDOFEXAM, AnswersAtEndOfExam);
        //editor.putString(USERS_ANSWERS, selectedAnswers.toString());
        //editor.putStringSet(USERS_ANSWERS, selectedAnswers);
        JSONArray jArray = new JSONArray();
        for (int i = 0; i<selectedAnswers.length; i++){
            jArray.put(selectedAnswers[i]);
        }
        //if(selectedAnswers.length > 0){
            editor.putString(USERS_ANSWERS, jArray.toString());
        //}
        //else{
          //  editor.putString(USERS_ANSWERS, null);
        //}
        if (editor.commit()){
            Toast.makeText(this, "Exam saved successfully", Toast.LENGTH_SHORT).show();
        }

    }

    public void getAnswer (View view){
        int selected = answers.getCheckedRadioButtonId();
        RadioButton rbselected = (RadioButton) findViewById(selected);
        //adds option selected by the user to array selected answer at index qid, i.e map question ID to chosen option
        //selectedAnswers.add(qid, String.valueOf(rbselected.getText()));
        selectedAnswers[qid] = String.valueOf(rbselected.getText());

        if(AnswersAtEndOfExam){ //if the var is true, i.e user wants to view answers at the end of exam
            Toast.makeText(this, "will show answers at the end", Toast.LENGTH_SHORT).show();
           // if ((rbselected.getText().toString()).equals(correctAnswer)){ //if selected button is equal to answer, set selected btn color to green
             //   scores++;
            //}
        }//recall users choice wen user chose to view ans at the end
        else{

            optA.setTextColor(Color.BLACK);
            optB.setTextColor(Color.BLACK);
            optC.setTextColor(Color.BLACK);
            optD.setTextColor(Color.BLACK);
            optA.setClickable(false);
            optB.setClickable(false);
            optC.setClickable(false);
            optD.setClickable(false);


            if ((rbselected.getText().toString()).equals(correctAnswer)){ //if selected button is equal to answer, set selected btn color to green
                rbselected.setTextColor(Color.GREEN);
                scores++;
            }
            else if((optA.getText().toString()).equals(correctAnswer)) {  //bt if option A was the answer but wasn't the selected btn, set its color to green
                optA.setTextColor(Color.GREEN);                 // and the selected btn color to red
                rbselected.setTextColor(Color.RED);
            }
            else if((optB.getText().toString()).equals(correctAnswer)) {  //bt if option B was the answer but wasn't the selected btn, set its color to green
                optB.setTextColor(Color.GREEN);                 // and the selected btn color to red
                rbselected.setTextColor(Color.RED);
            }
            else if((optC.getText().toString()).equals(correctAnswer)) {  //bt if option C was the answer but wasn't the selected btn, set its color to green
                optC.setTextColor(Color.GREEN);                 // and the selected btn color to red
                rbselected.setTextColor(Color.RED);
            }
            else if((optD.getText().toString()).equals(correctAnswer)) {  //bt if option D was the answer but wasn't the selected btn, set its color to green
                optD.setTextColor(Color.GREEN);                 // and the selected btn color to red
                rbselected.setTextColor(Color.RED);
            }
        }

    }




    public class loadQuestions extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // pd = new ProgressDialog(QuizClass.this);
         //   pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         //   pd.setMessage("Loading... Please wait...");
         //   pd.setIndeterminate(true);
         //   pd.setCanceledOnTouchOutside(false);
         //   pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            //getting questions as raw json from server
            URL url;
            try {
                url = new URL ("http://dokenedgar.net23.net/index.php");
               // url = new URL ("http://192.168.240.2/Droidz/quizQuestions.php");
                urlConnection = (HttpURLConnection) url.openConnection();
                String urlParameters = "subject="+subjectYear;
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                DataOutputStream ds = new DataOutputStream(urlConnection.getOutputStream());
                ds.writeBytes(urlParameters);
                ds.flush();
                ds.close();

                InputStream is = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();

                while ((json_string=br.readLine()) != null){
                    sb.append(json_string+"\n");
                }
                br.close();
                is.close();
                urlConnection.disconnect();
                completeRawJSON = sb.toString();

                //Parsing json to get questions
                parseJson(completeRawJSON);
               /*
                JSONObject jsonObject = new JSONObject(completeRawJSON);
                JSONArray jsonArray = jsonObject.getJSONArray("JSONQuestions");

                for(int i=0; i<jsonArray.length(); i++){
                    quizQuestions quizQ = new quizQuestions();
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    quizQ.setQid(jObject.getString("question_no"));
                    quizQ.setQuestion(jObject.getString("question"));
                    quizQ.setImgUrl(jObject.getString("questionImage"));
                    quizQ.setOptionA(jObject.getString("optionA"));
                    quizQ.setOptionB(jObject.getString("optionB"));
                    quizQ.setOptionC(jObject.getString("optionC"));
                    quizQ.setOptionD(jObject.getString("optionD"));
                    quizQ.setCorrectAnswer(jObject.getString("answer"));

                    questionslist.add(quizQ);
                }
                */
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }//closing brace for method doInBackground()

        @Override
        protected void onPostExecute(Void aVoid) {
           // pd.dismiss();
            Toast.makeText(QuizClass.this, "Done loading questioms...", Toast.LENGTH_SHORT).show();
            //set size of the array that stores the options that the user has selected, this is equal to the number of question objects in the arraylist
            selectedAnswers = new String[questionslist.size()];
            //sets total number of questions in arraylist, using -1 bcos size() counts the objects in the arraylist starting from 1, where as arraylist index starts from 0
            totalQuestions = (questionslist.size()) - 1;
            DisplayQuestion();
        }
    }//closing b race for class loadQuestions()

    public void parseJson(String JSONstring){

        try {
            JSONObject jsonObject = null;
            //jsonObject = new JSONObject(completeRawJSON);
            jsonObject = new JSONObject(JSONstring);
            JSONArray jsonArray = jsonObject.getJSONArray("JSONQuestions");

        for(int i=0; i<jsonArray.length(); i++){
            quizQuestions quizQ = new quizQuestions();
            JSONObject jObject = jsonArray.getJSONObject(i);
            quizQ.setQid(jObject.getString("question_no"));
            quizQ.setQuestion(jObject.getString("question"));
            quizQ.setImgUrl(jObject.getString("questionImage"));
            quizQ.setOptionA(jObject.getString("optionA"));
            quizQ.setOptionB(jObject.getString("optionB"));
            quizQ.setOptionC(jObject.getString("optionC"));
            quizQ.setOptionD(jObject.getString("optionD"));
            quizQ.setCorrectAnswer(jObject.getString("answer"));

            questionslist.add(quizQ);
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void DisplayQuestion(){

        //imgHeigt = lLayout.getHeight()/2;
        //imgWidth = lLayout.getWidth();

        optA.setTextColor(Color.BLACK);
        optB.setTextColor(Color.BLACK);
        optC.setTextColor(Color.BLACK);
        optD.setTextColor(Color.BLACK);
        answers.clearCheck();    //remove any selection from the radioGroup and set the radio buttons to clickable

        Q = questionslist.get(qid); // get the question object at index qid from arraylist questionlist and set the various properties
        question_no = Q.getQid();
        question = Q.getQuestion();
        imageUrl = Q.getImgUrl();
        optionA = Q.getOptionA();
        optionB = Q.getOptionB();
        optionC = Q.getOptionC();
        optionD = Q.getOptionD();
        correctAnswer = Q.getCorrectAnswer();

        //create Spanned variables for taking in the string JSON. Using Spanned because there might be some html tags that will need to be rendered, like
        //bold, italics, subscript and superscript etc and its the Spanned vars that will be passed to setText, not the raw string data
        Spanned spanQid = Html.fromHtml(question_no);
        Spanned spanQuestion = Html.fromHtml(question);
        Spanned spanOptionA = Html.fromHtml(optionA); //Html.escapeHtml()
        Spanned spanOptionB = Html.fromHtml(optionB);
        Spanned spanOptionC = Html.fromHtml(optionC);
        Spanned spanOptionD = Html.fromHtml(optionD);
        Spanned spanCorrectAnswer = Html.fromHtml(correctAnswer);

        //Check if question has an image
        if(imageUrl.equals("null")){
            //do nothing, n remove any previously shown image
            //questionImage.setVisibility(View.INVISIBLE);
            questionImage.setVisibility(View.GONE);
        }
        else{//check if the image file exists in external storage,if it does load it into imageview, if it doesn't,
            //fetch image from given url by starting another instance of a class extending asyncTask

            //most recently added to achieve saving images locally on device ****************
            fileName = imageUrl;
            //if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || Environment.isExternalStorageRemovable() ){
                  //outFile = new File (((Context)this).getExternalFilesDir(null), fileName);
                outFile = new File (((Context)this).getFilesDir(), fileName);

              //  Toast.makeText(this, "media mount test passed",Toast.LENGTH_SHORT).show();

                if(!outFile.exists()) //outfile does not exist, begin new download of image. why not perform the check inside downloadImage()
                                       //so the check and new download are performed on the background thread. maybe make outFile and fileName static
                {
                    questionImage.setImageDrawable(null);
                    new downloadImage().execute();
                    questionImage.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "image has to be downloaded",Toast.LENGTH_SHORT).show();
                }
                else{ //outfile exists, so update imageview with that image
                    questionImage.setImageURI(Uri.parse("file://" + outFile.getAbsolutePath()));
                    Toast.makeText(this, "image from file",Toast.LENGTH_SHORT).show();
                }
           // }

            //*****************Close of most recently added************
           /* questionImage.setImageDrawable(null);
            new downloadImage().execute();
            questionImage.setVisibility(View.VISIBLE);
            */
        }
        //Display next question
        txtQuestionNumber.setText(question_no);
        txtquestions.setText(spanQuestion);//(spanQid+".  "+spanQuestion);
        optA.setText(spanOptionA);
        optB.setText(spanOptionB);
        optC.setText(spanOptionC);
        optD.setText(spanOptionD);

    }

    public void CheckIfAnswered(){
        if (selectedAnswers[qid] == null){ //checks whether the question has been answered already. null (true) means the question has not been answered

            optA.setClickable(true);
            optB.setClickable(true);
            optC.setClickable(true);
            optD.setClickable(true);

            if(reachedEndOfExam){
                optA.setClickable(false);
                optB.setClickable(false);
                optC.setClickable(false);
                optD.setClickable(false);
                //show correct answers even though user chose nothing
                if(correctAnswer.equals(optA.getText().toString())){
                    optA.setTextColor(Color.GREEN);
                }
                else if (correctAnswer.equals(optB.getText().toString())){
                    optB.setTextColor(Color.GREEN);
                }
                else if (correctAnswer.equals(optC.getText().toString())){
                    optC.setTextColor(Color.GREEN);
                }
                else if (correctAnswer.equals(optD.getText().toString())){
                    optD.setTextColor(Color.GREEN);
                }

            }

        } else{   //question has been attempted, therefore disable clickability and show whether attempt was correct or not

            String chosenAnswer = selectedAnswers[qid];    //clickability is also diaabled so that another option cant be selected

            optA.setClickable(false);
            optB.setClickable(false);
            optC.setClickable(false);
            optD.setClickable(false);

            if (chosenAnswer.equals(correctAnswer) && chosenAnswer.equals(optA.getText().toString())) { //what user chose = d correct answer, and = option A
                optA.setTextColor(Color.GREEN);
                optA.setChecked(true);
            } else if (correctAnswer.equals(optA.getText().toString())) {     //option A was not chosen, but is the correct answer
                optA.setTextColor(Color.GREEN);
            } else if (chosenAnswer.equals(optA.getText().toString())) {  //option A was chosen, but its not the correct answer
                optA.setTextColor(Color.RED);
                optA.setChecked(true);
            }

            if (chosenAnswer.equals(correctAnswer) && chosenAnswer.equals(optB.getText().toString())) { //what user chose = d correct answer, and = option B
                optB.setTextColor(Color.GREEN);
                optB.setChecked(true);
            } else if (correctAnswer.equals(optB.getText().toString())) {    //option B was not chosen, but is the correct answer
                optB.setTextColor(Color.GREEN);
            } else if (chosenAnswer.equals(optB.getText().toString())) {  //option B was chosen, but its not the correct answer
                optB.setTextColor(Color.RED);
                optB.setChecked(true);
            }
            if (chosenAnswer.equals(correctAnswer) && chosenAnswer.equals(optC.getText().toString())) { //what user chose = d correct answer, and = option C
                optC.setTextColor(Color.GREEN);
                optC.setChecked(true);
            } else if (correctAnswer.equals(optC.getText().toString())) {              //option C was not chosen, but is the correct answer
                optC.setTextColor(Color.GREEN);
            } else if (chosenAnswer.equals(optC.getText().toString())) {  //option C was chosen, but its not the correct answer
                optC.setTextColor(Color.RED);
                optC.setChecked(true);
            }
            if (chosenAnswer.equals(correctAnswer) && chosenAnswer.equals(optD.getText().toString())) { //what user chose = d correct answer, and = option D
                optD.setTextColor(Color.GREEN);
                optD.setChecked(true);
            } else if (correctAnswer.equals(optD.getText().toString())) {        //option D was not chosen, but is the correct answer
                optD.setTextColor(Color.GREEN);
            } else if (chosenAnswer.equals(optD.getText().toString())) {  //option D was chosen, but its not the correct answer
                optD.setTextColor(Color.RED);
                optD.setChecked(true);
            }

        }//closing brace for the ELSE in method CheckIfAnswered()
    } //closing brace for method CheckIfAnswered()

    public void DisplayChosenOptions(){
        if (selectedAnswers[qid] == null){ //checks whether the question has been answered already. null (true) means the question has not been answered

            optA.setClickable(true);
            optB.setClickable(true);
            optC.setClickable(true);
            optD.setClickable(true);

        } else{   //question has been attempted,

            String chosenAnswer = selectedAnswers[qid];

        //    optA.setClickable(false);
          //  optB.setClickable(false);
           // optC.setClickable(false);
           // optD.setClickable(false);

            if (chosenAnswer.equals(optA.getText().toString())) {
                optA.setChecked(true);
            }

             else if (chosenAnswer.equals(optB.getText().toString())) {
                optB.setChecked(true);
            }
             else if (chosenAnswer.equals(optC.getText().toString())) {
                optC.setChecked(true);
            }
             else if (chosenAnswer.equals(optD.getText().toString())) {
                optD.setChecked(true);
            }

        }//closing brace for the ELSE in method CheckIfAnswered()
    } //closing brace for method CheckIfAnswered()



    public class downloadImage extends AsyncTask<Void, Void, Bitmap>{
        Bitmap bitmap;
        //outFile = new File (((Context)this).getFilesDir(), fileName);
        @Override
        protected Bitmap doInBackground(Void... params) {

            try {
                URL url = new URL(imageUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                //outFile = new File (getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
                outFile = new File(QuizClass.this.getFilesDir(), QuizClass.fileName);

                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));
                BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());

                //InputStream is = urlConnection.getInputStream();
                //copy the inputstream(image from server) to output stream(local file in external storage)
                final byte[] buf = new byte[1024];
                int numBytes;
                while(-1 != (numBytes = is.read(buf))){
                    os.write(buf,0,numBytes);
                }

                bitmap = BitmapFactory.decodeStream(is);
                is.close();
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //questionBitmap = bitmap;
            imgHeigt = lLayout.getHeight()/2;//from Display()
            imgWidth = lLayout.getWidth();
            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, imgWidth, imgHeigt, true);
            questionImage.setImageBitmap(newBitmap);

        }
    }

} //close class MainActivity
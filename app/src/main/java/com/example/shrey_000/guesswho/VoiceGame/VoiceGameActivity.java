package com.example.shrey_000.guesswho.VoiceGame;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shrey_000.guesswho.HomeActivity;
import com.example.shrey_000.guesswho.PersonalCollection.ViewPersonalCollectionActivity;
import com.example.shrey_000.guesswho.R;
import com.example.shrey_000.guesswho.ScoreActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import Utilities.DataStoreFactory;
import Utilities.DataStoreManager;
import Utilities.RandomGenerator;
import Utilities.ScoreCalculatorFaceGame;


public class VoiceGameActivity extends AppCompatActivity{
    private String username;
    private HashMap<String, HashMap<String, ArrayList<String>>> availableFiles = null;
    private String correctAns = "";
    private int selectedID = 0;
    private int correctID = 0;
    private RandomGenerator rg;
    private Random random;
    private boolean playing;
    private DataStoreManager dataStoreManager = DataStoreFactory.createDataStoreManager();
    private MediaPlayer player;
    private Handler mHandler;
    private int playedNum;
    private int scoreTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_game);

        mHandler = new Handler();
        playedNum = 0;

        username = getIntent().getStringExtra("username");
        availableFiles = (HashMap<String, HashMap<String, ArrayList<String>>>) getIntent().getSerializableExtra("availableFiles");
        scoreTotal = getIntent().getIntExtra("score",0);

        displayScore(scoreTotal);

        random = new Random();

        if(availableFiles == null){
            availableFiles = dataStoreManager.getVoice(username);
        }

        rg =  new RandomGenerator(4);

        final HashMap<String, String> questionAndAnswer = getQuestionAndAnswer(availableFiles);
        String[] options = rg.randomizeOptionOrder(availableFiles.keySet().toArray());

        correctAns = questionAndAnswer.get("answer");

        if(!Arrays.asList(options).contains(correctAns))
            options[random.nextInt(3)] = correctAns;

        Log.d("pollo", questionAndAnswer.get("answer"));
        Log.d("pollo", questionAndAnswer.get("filename"));
        Log.d("pollo", questionAndAnswer.get("timing"));

        // random button options
        ((Button)findViewById(R.id.choice1Voice)).setText(options[0]);
        ((Button)findViewById(R.id.choice2Voice)).setText(options[1]);
        ((Button)findViewById(R.id.choice3Voice)).setText(options[2]);
        ((Button)findViewById(R.id.choice4Voice)).setText(options[3]);

        correctID = ((Button)findViewById(R.id.choice1Voice)).getText().equals(correctAns) ? R.id.choice1Voice :
                ((Button)findViewById(R.id.choice2Voice)).getText().equals(correctAns) ? R.id.choice2Voice :
                        ((Button)findViewById(R.id.choice3Voice)).getText().equals(correctAns) ? R.id.choice3Voice :
                                R.id.choice4Voice;

        final ImageButton playButton = (ImageButton) findViewById(R.id.buttonPlayVoice);
        playing = false;

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int startTime = (int)(Double.parseDouble(questionAndAnswer.get("timing").split("-")[0])*1000);
                int endTime = (int)(Double.parseDouble(questionAndAnswer.get("timing").split("-")[1])*1000);
                if(!playing){
                    try {
                        findViewById(R.id.buttonPlayVoice).setEnabled(false);
                        playedNum++;
                        player = new MediaPlayer();
                        player.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/GuessWho/recording/" + questionAndAnswer.get("filename") + ".wav");
                        player.prepare();
                        player.seekTo(startTime);
                        player.start();

                        final Runnable mStopAction = new Runnable() {
                            @Override
                            public void run() {
                                playButton.performClick();
                            }
                        };
                        mHandler.postDelayed(mStopAction, endTime - startTime);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else{
                    player.stop();
                    findViewById(R.id.buttonPlayVoice).setEnabled(true);
                }
                setPlayButton(playButton.getId(), playing);
                playing = !playing;
            }
        });
    }

    /**
     * Change the button shape based on the current state of the media player
     * @param buttonId the id of button to be changed
     * @param playing state of the player
     */
    public void setPlayButton(final int buttonId, final boolean playing) {
        final Runnable runnableUi = new Runnable(){
            @Override
            public void run() {
                ImageButton button = (ImageButton) findViewById(buttonId);
                if(!playing){
                    button.setImageResource(R.drawable.ic_stop);
                }
                else{
                    button.setImageResource(R.drawable.ic_play);
                }
            }
        };
        new Thread(){
            public void run(){
                mHandler.post(runnableUi);
            }
        }.start();
    }

    /**
     * Get one particular question and answer from the pool of questions and answers
     * @param dataSet Set of all questions and answers
     * @return HashMap of the question and the answer
     */
    public HashMap<String, String> getQuestionAndAnswer(HashMap<String, HashMap<String, ArrayList<String>>> dataSet){
        HashMap<String, String> output = new HashMap<>();
        int total = dataSet.size();
        int num = random.nextInt(total);
        int ques = random.nextInt(5);
        int i = 0;

        for(String name : dataSet.keySet()){
            if(i++ == num){
                String fileName = (String) dataSet.get(name).keySet().toArray()[0];
                output.put("answer", name);
                output.put("filename", fileName);
                output.put("timing", dataSet.get(name).get(fileName).get(ques));
                break;
            }
        }
        return output;
    }

    /**
     * initialize the correct answer
     * @param correctAns
     */
    public void setCorrectAns(String correctAns){
        this.correctAns = correctAns;
    }

    /**
     * Action to perform when choice 1 is clicked
     * @param view view where the action has to be performed
     */
    public void onChoice1(View view){
        selectedID = R.id.choice1Voice;
        view.setBackgroundColor(Color.DKGRAY);
        findViewById(R.id.choice2Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.choice3Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.choice4Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.nextVoice).setEnabled(true);
    }

    /**
     * Action to perform when choice 2 is clicked
     * @param view view where the action has to be performed
     */
    public void onChoice2(View view){
        selectedID = R.id.choice2Voice;
        view.setBackgroundColor(Color.DKGRAY);
        findViewById(R.id.choice1Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.choice3Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.choice4Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.nextVoice).setEnabled(true);
    }

    /**
     * Action to perform when choice 3 is clicked
     * @param view view where the action has to be performed
     */
    public void onChoice3(View view){
        selectedID = R.id.choice3Voice;
        view.setBackgroundColor(Color.DKGRAY);
        findViewById(R.id.choice1Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.choice2Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.choice4Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.nextVoice).setEnabled(true);
    }

    /**
     * Action to perform when choice 4 is clicked
     * @param view view where the action has to be performed
     */
    public void onChoice4(View view){
        selectedID = R.id.choice4Voice;
        view.setBackgroundColor(Color.DKGRAY);
        findViewById(R.id.choice1Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.choice2Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.choice3Voice).setBackgroundColor(Color.GRAY);
        findViewById(R.id.nextVoice).setEnabled(true);
    }

    /**
     * Check the current answer. Then move to the next question
     * @param view view where the action has to be performed
     * @throws InterruptedException
     */
    public void goToNext(View view) throws InterruptedException {
        checkAnswer();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //start your activity here
                Intent intent = new Intent(getApplicationContext(), VoiceGameActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("availableFiles", availableFiles);
                intent.putExtra("score", scoreTotal);
                startActivity(intent);
            }

        }, 2500L);
    }

    /**
     * Check if the selected answer is correct or not.
     */
    private void checkAnswer() {
        if(correctID == selectedID) {
            findViewById(selectedID).setBackgroundColor(Color.GREEN);
            ((Button)findViewById(selectedID)).setTextColor(Color.WHITE);
            scoreTotal += playedNum > 0 ? (int) 100/playedNum : 0;
        }
        else {
            findViewById(selectedID).setBackgroundColor(Color.RED);
            ((Button)findViewById(selectedID)).setTextColor(Color.WHITE);
            findViewById(correctID).setBackgroundColor(Color.GREEN);
            ((Button)findViewById(correctID)).setTextColor(Color.WHITE);
            final Animation animation = new AlphaAnimation(1.0f, 0.5f); // Change alpha from fully visible to invisible
            animation.setDuration(800); // duration - half a second
            animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
            animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
            animation.setRepeatMode(Animation.REVERSE);
            findViewById(correctID).startAnimation(animation);
        }
    }

    /**
     * Display the updated score on the top-right corner of the view
     * @param newScore updated score
     */
    private void displayScore(int newScore) {
        TextView scoreView = (TextView) findViewById(R.id.scoreViewVoice);
        String scoreText = "Score: " + newScore;
        scoreView.setText(scoreText);
    }

    @Override
    public void onBackPressed(){
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_back) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which){
                    DataStoreManager dbm = DataStoreFactory.createDataStoreManager();
                    dbm.insertScore(username,"voice",scoreTotal);
                    Intent intent = new Intent(getApplicationContext(), ScoreActivity.class);
                    intent.putExtra("username",username);
                    intent.putExtra("game","voice");
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){

                };
            });
            alertDialog.setTitle("Confirm Exit?");
            alertDialog.setMessage("Are you sure you want to exit?");
            alertDialog.setCancelable(true);
            alertDialog.create();
            alertDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
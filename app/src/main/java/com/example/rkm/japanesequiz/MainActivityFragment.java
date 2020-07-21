package com.example.rkm.japanesequiz;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final int NUMBER_OF_SCRIPTS_INCLUDED_IN_QUIZ = 10;

    private List<String> allScriptsNameList;
    private List<String> scriptsNamesQuizList;

    // Set (Interface) cannot have duplicate values
    private Set<String> scriptTypesInQuiz;
    private String correctScriptAnswer;
    private int numberOfAllGuesses;
    private int numberOfRightAnswers;
    private int numberOfScriptsGuessRows;
    private SecureRandom secureRandomNumber;
    private Handler handler;
    private Animation wrongAnswerAnimation;
    private LinearLayout scriptQuizLinearLayout;
    private TextView txtQuestionNumber;
    private ImageView imgQuiz;
    private LinearLayout[] rowsOfGuessButtonsScriptQuiz;
    private TextView txtAnswer;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);


        allScriptsNameList = new ArrayList<>();
        scriptsNamesQuizList = new ArrayList<>();
        secureRandomNumber = new SecureRandom();
        handler = new Handler();

        wrongAnswerAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.wrong_answer_animation);
        wrongAnswerAnimation.setRepeatCount(1);

        scriptQuizLinearLayout = (LinearLayout) view.findViewById(R.id.scriptQuizLinearLayout);
        txtQuestionNumber = (TextView) view.findViewById(R.id.txtQuestionNumber);
        imgQuiz = (ImageView) view.findViewById(R.id.imgQuiz);

        rowsOfGuessButtonsScriptQuiz = new LinearLayout[3];
        rowsOfGuessButtonsScriptQuiz[0] = (LinearLayout) view.findViewById(R.id.firstRowLinearLayout);
        rowsOfGuessButtonsScriptQuiz[1] = (LinearLayout) view.findViewById(R.id.secondRowLinearLayout);
        rowsOfGuessButtonsScriptQuiz[2] = (LinearLayout) view.findViewById(R.id.thirdRowLinearLayout);
        txtAnswer = (TextView) view.findViewById(R.id.txtAnswer);

        for (LinearLayout row : rowsOfGuessButtonsScriptQuiz) {

            for (int column = 0; column < row.getChildCount(); column++) {

                Button btnGuess = (Button) row.getChildAt(column);
                btnGuess.setOnClickListener(btnGuessListener);
                btnGuess.setTextSize(24);

            }
        }

        txtQuestionNumber.setText(getString(R.string.question_text, 1, NUMBER_OF_SCRIPTS_INCLUDED_IN_QUIZ));
        return view;

    }

    private View.OnClickListener btnGuessListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Button btnGuess = ((Button) view);
            String guessValue = btnGuess.getText().toString();
            String answerValue = getTheExactScriptName(correctScriptAnswer);
            ++numberOfAllGuesses;

            // when user guess the right answer

            if (guessValue.equals(answerValue)) {
                ++numberOfRightAnswers;

                txtAnswer.setText(answerValue + "! " + "Correct");
                txtAnswer.setTextColor(Color.parseColor("#067206"));

                disableQuizGuessButton();

                if (numberOfRightAnswers == NUMBER_OF_SCRIPTS_INCLUDED_IN_QUIZ) {

                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.results_string_status)
                            .setMessage(getString(R.string.results_string_value, numberOfAllGuesses,
                                    1000 / (double) numberOfAllGuesses))
                            .setPositiveButton(R.string.reset_script_quiz, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    restQuiz();


                                }
                            })
                            .setCancelable(false)
                            .show();


                }
                // when user choose wrong answer
                else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animateScriptQuiz(true);
                        }

                    }, 1000);
                }
            } else {

                imgQuiz.startAnimation(wrongAnswerAnimation);

                txtAnswer.setText(R.string.wrong_answer_message);
                txtAnswer.setTextColor(Color.parseColor("#aa372f"));
                btnGuess.setEnabled(false);
            }
        }

    };
    // the following method gets the exact script name from assets folder
    // indexOf('-') + 1 <--- script name k andar jo - hy us k bad wala sara text show krwata
    // replace('-',' ') <--- aur us name k andar jo _ hy us ko blank space sy tbdeel krta

    private String getTheExactScriptName(String scriptName) {
        return scriptName.substring(scriptName.indexOf('-') + 1).replace('_', ' ');

    }
    // disabling buttons with non matching answers, when clicked once

    private void disableQuizGuessButton() {
        for (int row = 0; row < numberOfScriptsGuessRows; row++) {

            LinearLayout guessRowLinearLayout = rowsOfGuessButtonsScriptQuiz[row];
            for (int buttonIndex = 0; buttonIndex < guessRowLinearLayout.getChildCount(); buttonIndex++) {
                guessRowLinearLayout.getChildAt(buttonIndex).setEnabled(false);
            }
        }

    }

    public void restQuiz() {

        AssetManager assets = getActivity().getAssets();
        allScriptsNameList.clear();

        // getting quiz images and names from assets
        try {
            for (String scriptType : scriptTypesInQuiz) {

                String[] scriptImagePathsInQuiz = assets.list(scriptType);

                for (String scriptImagePathInQuiz : scriptImagePathsInQuiz) {

                    allScriptsNameList.add(scriptImagePathInQuiz.replace(".png", ""));
                }
            }
        } catch (IOException e) {
            Log.e("Script Quiz", "Error", e);
        }

        numberOfRightAnswers = 0;
        numberOfAllGuesses = 0;
        scriptsNamesQuizList.clear();

        int counter = 1;
        int numberOfAvailableScripts = allScriptsNameList.size();

        while (counter <= NUMBER_OF_SCRIPTS_INCLUDED_IN_QUIZ) {
            int randomIndex = secureRandomNumber.nextInt(numberOfAvailableScripts);
            String scriptImageName = allScriptsNameList.get(randomIndex);

            if (!scriptsNamesQuizList.contains(scriptImageName)) {
                scriptsNamesQuizList.add(scriptImageName);
                ++counter;
            }
        }
        showNextQuiz();
    }

    private void animateScriptQuiz(boolean animateOutScriptImage) {
        if (numberOfRightAnswers == 0) {

            return;
        }
        int xTopLeft = 0;
        int yTopLeft = 0;

        int xBottomRight = scriptQuizLinearLayout.getLeft() + scriptQuizLinearLayout.getRight();
        int yBottomRight = scriptQuizLinearLayout.getTop() + scriptQuizLinearLayout.getBottom();

        // Here is max value for radius
        int radius = Math.max(scriptQuizLinearLayout.getWidth(), scriptQuizLinearLayout.getHeight());

        Animator animator;

        if (animateOutScriptImage) {

            animator = ViewAnimationUtils.createCircularReveal(scriptQuizLinearLayout,
                    xBottomRight, yBottomRight, radius, 0);

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    showNextQuiz();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

        } else {
            animator = ViewAnimationUtils.createCircularReveal(scriptQuizLinearLayout,
                    xTopLeft, yTopLeft, radius, 0);
        }

        animator.setDuration(700);
        animator.start();

    }

    private void showNextQuiz() {
        String nextscriptImageName = scriptsNamesQuizList.remove(0);
        correctScriptAnswer = nextscriptImageName;
        txtAnswer.setText("");

        txtQuestionNumber.setText(getString(R.string.question_text, numberOfRightAnswers + 1,
                NUMBER_OF_SCRIPTS_INCLUDED_IN_QUIZ));

        String scriptType = nextscriptImageName.substring(0, nextscriptImageName.indexOf("-"));

        AssetManager assets = getActivity().getAssets();

        // getting image from assets folder and showing it to the user
        try (InputStream stream = assets.open(scriptType + "/" + nextscriptImageName + ".png")) {

            Drawable scriptImage = Drawable.createFromStream(stream, nextscriptImageName);
            imgQuiz.setImageDrawable(scriptImage);
            animateScriptQuiz(false);
        } catch (IOException e) {
            Log.e("Japanese Quiz", "There is an error getting " + nextscriptImageName, e);
        }

        Collections.shuffle(allScriptsNameList);

        // following 3 lines allScriptsNameList ma sy correctScriptAnswer ka index correctscriptNameIndex
        // ma save krati hain. Then other thing

        int correctscriptNameIndex = allScriptsNameList.indexOf(correctScriptAnswer);
        String correctscriptName = allScriptsNameList.remove(correctscriptNameIndex);
        allScriptsNameList.add(correctscriptName);

        for (int row = 0; row < numberOfScriptsGuessRows; row++) {

            // Enabling btns

            for (int column = 0; column < rowsOfGuessButtonsScriptQuiz[row].getChildCount();
                 column++) {

                Button btnGuess = (Button) rowsOfGuessButtonsScriptQuiz[row].getChildAt(column);
                btnGuess.setEnabled(true);

                // Showing scriptsnames on btns

                String scriptImageName = allScriptsNameList.get((row * 2) + column);
                btnGuess.setText(getTheExactScriptName(scriptImageName));
            }
        }
        //secureRandomNumber generates random nmbrs AND numberOfScriptsGuessRows
        // shows number of script guess rows
        //Here substituting one of the guess options with correct answer
        int row = secureRandomNumber.nextInt(numberOfScriptsGuessRows);
        int column = secureRandomNumber.nextInt(2);
        LinearLayout randomRow = rowsOfGuessButtonsScriptQuiz[row];
        String correctscriptImageName = getTheExactScriptName(correctscriptName);
        ((Button) randomRow.getChildAt(column)).setText(correctscriptImageName);


    }

    public void modifyScriptGuessRows(SharedPreferences sharedPreferences) {

        final String NUMBER_OF_GUESS_OPTIONS = sharedPreferences.getString(MainActivity.GUESSES, null);
        numberOfScriptsGuessRows = Integer.parseInt(NUMBER_OF_GUESS_OPTIONS) / 2;

        for (LinearLayout horizontalLinearLayout : rowsOfGuessButtonsScriptQuiz) {
            horizontalLinearLayout.setVisibility(View.GONE);
        }

        for (int row = 0; row < numberOfScriptsGuessRows; row++) {
            rowsOfGuessButtonsScriptQuiz[row].setVisibility(View.VISIBLE);

        }

    }

    public void modifyTypeOfScriptInQuiz(SharedPreferences sharedPreferences) {
        scriptTypesInQuiz = sharedPreferences.getStringSet(MainActivity.SCRIPT_TYPE, null);
    }

    public void modifyBGColor(SharedPreferences sharedPreferences) {

        String bgColor = sharedPreferences.getString(MainActivity.QUIZ_BACKGROUND_COLOR, null);

        switch (bgColor) {

            case "White":
                scriptQuizLinearLayout.setBackgroundColor(Color.WHITE);

                for (LinearLayout row : rowsOfGuessButtonsScriptQuiz) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE);
                        button.setTextColor(Color.WHITE);
                    }
                }

//                txtAnswer.setTextColor(Color.GREEN);
                txtQuestionNumber.setTextColor(Color.BLACK);

                break;

            case "Black":
                scriptQuizLinearLayout.setBackgroundColor(Color.BLACK);

                for (LinearLayout row : rowsOfGuessButtonsScriptQuiz) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.YELLOW);
                        button.setTextColor(Color.BLACK);
                    }
                }


                txtQuestionNumber.setTextColor(Color.WHITE);

                break;




        }
    }
}
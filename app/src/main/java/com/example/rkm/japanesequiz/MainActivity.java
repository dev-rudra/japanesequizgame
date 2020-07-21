package com.example.rkm.japanesequiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {


    public static final String  GUESSES = "settings_numberOfGuesses";
    public static final String SCRIPT_TYPE = "setting_script_type";
    public static final String QUIZ_BACKGROUND_COLOR = "settings_quiz_background_color";
    public static final String QUIZ_FONT = "settings_quiz_font";


    private boolean isSettingsChanged = false;
        MainActivityFragment myScriptQuizFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        PreferenceManager.setDefaultValues(MainActivity.this, R.xml.quiz_preferences, false);


        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).
                registerOnSharedPreferenceChangeListener(settingsChangeListener);


        myScriptQuizFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.scriptQuizFragment);

        myScriptQuizFragment.modifyScriptGuessRows(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myScriptQuizFragment.modifyTypeOfScriptInQuiz(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myScriptQuizFragment.modifyBGColor(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myScriptQuizFragment.restQuiz();
        isSettingsChanged = false;






    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        Intent preferencesIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);

    }

    private SharedPreferences.OnSharedPreferenceChangeListener settingsChangeListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


            isSettingsChanged = true;

            if (key.equals(GUESSES)) {

                myScriptQuizFragment.modifyScriptGuessRows(sharedPreferences);
                myScriptQuizFragment.restQuiz();

            } else if (key.equals(SCRIPT_TYPE)) {

                Set<String> scriptTypes = sharedPreferences.getStringSet(SCRIPT_TYPE, null);

                if (scriptTypes != null && scriptTypes.size() > 0) {

                    myScriptQuizFragment.modifyTypeOfScriptInQuiz(sharedPreferences);
                    myScriptQuizFragment.restQuiz();

                } else {

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    scriptTypes.add(getString(R.string.default_script_type));
                    editor.putStringSet(SCRIPT_TYPE, scriptTypes);
                    editor.apply();

                    Toast.makeText(MainActivity.this,
                            R.string.toast_message, Toast.LENGTH_SHORT).show();

                }

            } else if (key.equals(QUIZ_FONT)) {
                myScriptQuizFragment.restQuiz();
            } else if (key.equals(QUIZ_BACKGROUND_COLOR)) {

                myScriptQuizFragment.modifyBGColor(sharedPreferences);
                myScriptQuizFragment.restQuiz();

            }

            Toast.makeText(MainActivity.this, R.string.change_message, Toast.LENGTH_SHORT).show();




        }
    };

}

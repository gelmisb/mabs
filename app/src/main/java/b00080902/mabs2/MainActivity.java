/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package b00080902.mabs2;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends FragmentActivity{


    private TextView txtSpeechInput;
    private ImageButton btnSpeak, btnSignOut;
    private Button btnList;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    DatabaseReference myRef;
    FirebaseDatabase database;
    private String one, two, three, four, fullResponse;
    private ListView itemList;
    private static CustomListAdapter adapter;
    private ArrayList<String> full ;
    private NewsModel model;
    int itemNo = 4;
    int position = 0;

    private BottomNavigationView bottomNavigationView;


    /** Called when the activity is first created. */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = new NewsModel();
        database = FirebaseDatabase.getInstance();



        // Fullscreen without a title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Overwriting the fullscreen parameters
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        // Set the right content view
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //  Fixed Portrait orientation
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Items list

        // Initials for recording
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });


        // User sign out using Firebase
        btnSignOut = (ImageButton) findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), WelcomeScreen.class);
                startActivity(intent);
            }
        });






        bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                int id = item.getItemId();

                if (id == R.id.navigation_home) {
                    // Check that the activity is using the layout version with
                    // the fragment_container FrameLayout
                    if (findViewById(R.id.fragment_container) != null) {

                        // Create fragment and give it an argument specifying the article it should show
                        HomeFragment newFragment = new HomeFragment();
                        Bundle args = new Bundle();
                        args.putInt(ArticleFragment.ARG_POSITION, position);
                        newFragment.setArguments(args);

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        transaction.replace(R.id.fragment_container, newFragment);
                        transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
                    }

                    return true;

                } else if (id == R.id.navigation_list) {

                    // Check that the activity is using the layout version with
                    // the fragment_container FrameLayout
                    if (findViewById(R.id.fragment_container) != null) {

                        // Create fragment and give it an argument specifying the article it should show
                        ArticleFragment newFragment = new ArticleFragment();
                        Bundle args = new Bundle();
                        args.putInt(ArticleFragment.ARG_POSITION, position);
                        newFragment.setArguments(args);

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        transaction.replace(R.id.fragment_container, newFragment);
                        transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
                    }

                    return true;

                } else if (id == R.id.navigation_balance) {
                    Toast.makeText(getApplicationContext(), "Balance", Toast.LENGTH_SHORT).show();

                    return true;

                } else if (id == R.id.navigation_info) {
                    Toast.makeText(getApplicationContext(), "Info", Toast.LENGTH_SHORT).show();

                    return true;
                }

                return false;
            }
        });




        // Check whether the activity is using the layout version with
        // the fragment_container FrameLayout. If so, we must add the first fragment

    }

    public void onArticleSelected(int position) {
        // The user selected the headline of an article from the HeadlinesFragment


        // Capture the article fragment from the activity layout
        ArticleFragment articleFrag = (ArticleFragment)
                getSupportFragmentManager().findFragmentById(R.id.article);

        if (articleFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            articleFrag.updateArticleView(position);

        } else {
            // If the frag is not available, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            ArticleFragment newFragment = new ArticleFragment();
            Bundle args = new Bundle();
            args.putInt(ArticleFragment.ARG_POSITION, position);
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }




    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    itemNo++;

                    // Stores result
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    // Shows output to user
                    Toast.makeText(getApplicationContext(), "Was it: " + result.get(0), Toast.LENGTH_SHORT).show();

                    fullResponse = result.get(0);

                    // Splits the string
                    String[] alphabets= result.get(0).split("\\s");

                    // Segments the string
                    one = alphabets[0];
                    two = alphabets[1];
//                    three = alphabets[2];
//                    three = alphabets[2];

                    // gets the current time
                    Date currentTime = Calendar.getInstance().getTime();
                    // Logs the details for the dev
                    Log.i("IMPORTANT", one);
                    Log.i("IMPORTANT1", two);


                    // Writes to DB
                    writeNewItem(itemNo + "", one, two, currentTime.toString());
                }
                break;
            }

        }


    }
    private void writeNewItem(String itemID, String name, String value, String date) {
        Date currentTime = Calendar.getInstance().getTime();

        Article items = new Article(name, value, date);
        model.addArticle(new Article(one, two, currentTime.toString()));

        myRef = database.getReference("items");
        myRef.child(itemID).setValue(items);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
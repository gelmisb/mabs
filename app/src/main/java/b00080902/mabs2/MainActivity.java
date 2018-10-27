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


import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    // UI config
    private TextView txtSpeechInput;
    private ImageButton btnSpeak, btnSignOut, homePage;
    private ListView itemList;
    private static CustomListAdapter adapter;
    private ArrayList<String> full ;
    private NewsModel model;
    private BottomNavigationView bottomNavigationView;

    // STT params
    private final int REQ_CODE_SPEECH_INPUT = 100;

    // Database config
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    // Iteration params
    private String one, two, three, four, fullResponse;
    int itemNo ;
    int position = 0;
    private ShowcaseView sv;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Instantiate initial params for DB
        model = new NewsModel();
        database = FirebaseDatabase.getInstance();

        // Set the right content view
        setContentView(R.layout.activity_main);

        // Fixed Portrait orientation
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /**
         * UI initial params
         */
        // To show speech results
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);

        // Button to sign out
        btnSignOut = (ImageButton) findViewById(R.id.btnSignOut);

        // Home page top button
        homePage = (ImageButton) findViewById(R.id.homePage);

        // Bottom nav bar
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigationView);


        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            // set your height here
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics);
            // set your width here
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }

        homePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://www.mabs.ie/en/"));
                startActivity(browserIntent);
            }
        });

        // User sign out using Firebase
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
//

        // Create fragment and give it an argument specifying the article it should show
        HomeFragment newFragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(HomeFragment.ARG_POSITION, position);
        newFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {



                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                int id = item.getItemId();

                // If home navigation is clicked
                if (id == R.id.navigation_home) {


                    if (vibe != null)
                        vibe.vibrate(50);

                    // Check that the activity is using the layout version with
                    // the fragment_container FrameLayout
                    if (findViewById(R.id.fragment_container) != null) {

                        // Create fragment and give it an argument specifying the article it should show
                        HomeFragment newFragment = new HomeFragment();
                        Bundle args = new Bundle();
                        args.putInt(HomeFragment.ARG_POSITION, position);
                        newFragment.setArguments(args);

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        transaction.replace(R.id.fragment_container, newFragment);
                        transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
//                        showcaseDialogTutorial();

                    }

                    return true;

                } else if (id == R.id.navigation_list) {


                    if (vibe != null)
                        vibe.vibrate(50);

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

                } else if (id == R.id.navigation_categories) {


                    if (vibe != null)
                        vibe.vibrate(50);

                    if (findViewById(R.id.fragment_container) != null) {

                        // Create fragment and give it an argument specifying the article it should show
                        CategoryFragment newFragment = new CategoryFragment();
                        Bundle args = new Bundle();
                        args.putInt(CategoryFragment.ARG_POSITION, position);
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


                    if (vibe != null)
                        vibe.vibrate(50);

                    if (findViewById(R.id.fragment_container) != null) {

                        // Create fragment and give it an argument specifying the article it should show
                        BalanceFragment newFragment = new BalanceFragment();
                        Bundle args = new Bundle();
                        args.putInt(BalanceFragment.ARG_POSITION, position);
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

                } else if (id == R.id.navigation_info) {


                    if (vibe != null)
                        vibe.vibrate(50);

                    // Create fragment and give it an argument specifying the article it should show
                    InfoFragment newFragment = new InfoFragment();
                    Bundle args = new Bundle();
                    args.putInt(InfoFragment.ARG_POSITION, position);
                    newFragment.setArguments(args);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack so the user can navigate back
                    transaction.replace(R.id.fragment_container, newFragment);
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();

                    return true;
                }

                return false;
            }
        });
    }


    @Override
    public void onBackPressed() {



        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);

        mp.start();

        if (vibe != null)
            vibe.vibrate(50);



        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Do you want to logout?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), WelcomeScreen.class);
                startActivity(intent);
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        alert.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    private void showcaseDialogTutorial(){

        boolean run;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        run = preferences.getBoolean("run?", true);

//        if(run){//If the buyer already went through the showcases it won't do it again.

        //This creates the first showcase.

        sv = new ShowcaseView.Builder(this)
                .setTarget( new ViewTarget( findViewById(R.id.my_toolbar)))
                .setContentTitle("Add new item manually")
                .setStyle(R.style.AppTheme_AppBarOverlay)
                .setContentText("To add new item manually, click this and specify item, value and choose the category")
                .hideOnTouchOutside()
                .build();
        sv.setButtonText("Next");

        //When the button is clicked then the switch statement will check the counter and make the new showcase.
        sv.overrideButtonClick(new View.OnClickListener() {
            int count1 = 0;

            @Override
            public void onClick(View v) {
                count1++;
                switch (count1) {
                    case 1:
                        sv.setTarget(new ViewTarget(findViewById(R.id.navigationView)));
                        sv.setContentTitle("Add new item hands-free");
                        sv.setContentText("Specify item, value and choose the category");
                        sv.setButtonText("Next");
                        break;

                    case 2:
//                            sv.setTarget(new ViewTarget(((View) myView.findViewById(R.id.my_toolbar))));
//                            sv.setContentTitle("Toolbar");
//                            sv.setContentText("Here you can change the settings to your preferences");
//                            sv.setButtonText("Next");
//                            break;

                    case 3:
//                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(myView.getContext());
//
//                            // Defining the editor
//                            SharedPreferences.Editor editor = preferences.edit();
//
//                            // Putting the information
//                            editor.putBoolean("run?", false);
//
//                            // Submitting the request
//                            editor.apply();


                        sv.hide();
                        break;
                }
            }
        });
    }
//    }

}
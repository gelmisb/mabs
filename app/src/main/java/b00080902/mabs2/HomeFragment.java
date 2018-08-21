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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.firebase.ui.auth.AuthUI.getInstance;

public class HomeFragment extends Fragment implements View.OnClickListener{

    // Positioning for the fragments
    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;

    // Database initials
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    // Reference for items MVC
    private NewsModel model;

    // UI config
    private TextView expenses, income, userHi;
    private ImageButton btnSpeak;

    // STT params
    private final int REQ_CODE_SPEECH_INPUT = 100;

    // Iteration params
    private String one, two, three;
    int itemNo ;
    int position = 0;

    // Global for further uses
    private View myView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null)
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);

        // Initialising the view the right parameters
        myView = inflater.inflate(R.layout.fragment_home, container, false);


        // Enable the ability for the user to speak to the application & UI params
        income = (TextView) myView.findViewById(R.id.income);
        userHi = (TextView) myView.findViewById(R.id.userHi);
        btnSpeak = (ImageButton) myView.findViewById(R.id.btnSpeak);

        // Cached item number - will reset when the app is deleted or reset
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        itemNo = preferences.getInt("Item", 0);

        // Firebase username
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getDisplayName();

        // Welcoming user
        userHi.setText(name + "!");

        // Access to DB
        model = new NewsModel();
        database = FirebaseDatabase.getInstance();


        // Microphone to listen to the user when activated
        btnSpeak.setOnClickListener(this);


        // Show the results
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recallDB();
        }

        // Inflate the layout for this fragment
        return myView;
    }



    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            updateArticleView(args.getInt(ARG_POSITION));
        } else if (mCurrentPosition != -1) {
            // Set article based on saved instance state defined during onCreateView
            updateArticleView(mCurrentPosition);
        }
    }


    /**
     * For calling the database to retrieve the full expenses for the day
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void recallDB(){

        //  Getting today's date
        // gets the current time
        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);


        // Getting the DB reference
        myRef = database.getReference("items");

        // Getting the event listener
        myRef.orderByChild("date").startAt(formattedDate).endAt(formattedDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Using generic type becuase it will suit for any type of object that could be processed
                GenericTypeIndicator<ArrayList<Article>> genericTypeIndicator =new GenericTypeIndicator<ArrayList<Article>>(){};

                // Initialising the array
                ArrayList<Article> fullItemList = dataSnapshot.getValue(genericTypeIndicator);

                // Full some of the expenses
                int sum = 0 ;

                // Asserting
                assert fullItemList != null;
                for(int i = 0; i < fullItemList.size(); i++){

                    if(fullItemList.get(i) != null){

                        // Retrieve each item
                        String liveprice = fullItemList.get(i).getValue();

                        // Remove all € signs
                        String newStr = liveprice.replace("€", "");

                        // Remove all commas
                        String newStr1 = newStr.replace(",", "");

                        // Replace all letters with 0
                        String newStr2 = newStr1.replaceAll("[A-Za-z]", "0");

                        // Add everything together
                        sum = sum + Integer.parseInt(newStr2);
                    } // end of if statement
                } // end of for loop statement


                // Show it to the user
                expenses = myView.findViewById(R.id.expenses);

                // Show the total to the user
                expenses.setText("Today's expenses: €" + NumberFormat.getNumberInstance(Locale.US).format(sum));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Failed", "Failed to read value.", error.toException());
            }
        });
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
            Toast.makeText(getActivity().getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    // Stores result
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);


                    // Splits the string
                    String[] alphabets= result.get(0).split("\\s");

                    // Segments the string
                    if (Arrays.asList(alphabets).contains(null)) {
                        Toast.makeText(getActivity().getApplicationContext(), "Oops! There was a problem, try again! ", Toast.LENGTH_LONG).show();


                    } else {


                        try {

                            one = alphabets[0];
                            two = alphabets[1];
                            three = alphabets[2];

                            String s1 = one.substring(0, 1).toUpperCase();
                            String nameCapitalized = s1 + one.substring(1);

                            String s2 = two.substring(0, 1).toUpperCase();
                            String nameCapitalized1 = s2 + two.substring(1);

                            String s3 = three.substring(0, 1).toUpperCase();
                            String nameCapitalized2 = s3 + three.substring(1);


                            one = nameCapitalized;
                            two = nameCapitalized1;
                            three = nameCapitalized2;



                            // gets the current time
                            Date c = Calendar.getInstance().getTime();

                            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                            String formattedDate = df.format(c);

                            // Logs the details for the dev
                            Log.i("Name ", one);
                            Log.i("Value ", two);
                            Log.i("Category ", three);


                            // Writes to DB
                            writeNewItem(itemNo + "", one, two, three, formattedDate);


                        } catch (ArrayIndexOutOfBoundsException e) {

                            Toast.makeText(getActivity().getApplicationContext(), "Oops! There was an error, try again! ", Toast.LENGTH_LONG).show();

                        }
                    }
                }
            }

            break;
        }

    }



    // returns true if the string does not have a number at the beginning
    public boolean isNoNumberAtBeginning(String s){
        Log.i("Chosen One", " " + s);

        return  s.matches(".*[a-zA-Z]+.*");
    }
    private void writeNewItem(String itemID, String name, String value, String category, String date) {


        Log.i("You", "didn't get an error!");

        if(isNoNumberAtBeginning(value)){
            Toast.makeText(getActivity().getApplicationContext(), "The value you have entered was wrong!", Toast.LENGTH_LONG).show();
        } else {

            Toast toast = Toast.makeText(getActivity().getApplicationContext(), " '" + one  + "' has been added to your list", Toast.LENGTH_LONG);
            View view = toast.getView();
            view.setBackgroundResource(R.drawable.toastieslayout);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.show();

            Article items = new Article(name, value , date, category);
            model.addArticle(new Article(one, two, date, category));

            myRef = database.getReference("items");
            myRef.child(itemID).setValue(items);

            itemNo++ ;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("Item", itemNo);
            editor.apply();
        }


    }


    public void updateArticleView(int position) {

        mCurrentPosition = position;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }


    @Override
    public void onClick(View v) {
        promptSpeechInput();

    }
}
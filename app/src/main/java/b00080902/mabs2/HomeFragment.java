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
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.firebase.ui.auth.AuthUI.getInstance;

public class HomeFragment extends Fragment implements View.OnClickListener{


    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private NewsModel model;
    private TextView expenses, income;
    private ImageButton btnSpeak;

    // STT params
    private final int REQ_CODE_SPEECH_INPUT = 100;

    // Iteration params
    private String one, two, three, four, fullResponse;
    int itemNo ;
    int position = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null)
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);

        View myView = inflater.inflate(R.layout.fragment_home, container, false);


        // Enable the ability for the user to speak to the application
        income = (TextView) myView.findViewById(R.id.income);
        btnSpeak = (ImageButton) myView.findViewById(R.id.btnSpeak);


        // Access to DB
        model = new NewsModel();
        database = FirebaseDatabase.getInstance();


        // Microphone to listen to the user when activated
        btnSpeak.setOnClickListener(this);


        // Show the results
        recallDB();

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


    public void recallDB(){

        myRef = database.getReference("items");


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                GenericTypeIndicator<ArrayList<Article>> genericTypeIndicator =new GenericTypeIndicator<ArrayList<Article>>(){};

                ArrayList<Article> fullItemList = dataSnapshot.getValue(genericTypeIndicator);

                int sum = 0 ;

                for(int i = 0; i < fullItemList.size(); i++){

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

                }

                // Check if it's the right sum
                Log.d("Sum", "Sum is: : " + sum + "");

                // Show it to the user
                expenses = (TextView) Objects.requireNonNull(getView()).findViewById(R.id.expenses);

                // Show the total to the user
                expenses.setText("Your total expenses: €" + sum);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {



                    // Stores result
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    // Shows output to user
                    Toast.makeText(getActivity().getApplicationContext(), "Was it: " + result.get(0), Toast.LENGTH_SHORT).show();

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

                    itemNo++;

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("Item", itemNo);
                    editor.apply();
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
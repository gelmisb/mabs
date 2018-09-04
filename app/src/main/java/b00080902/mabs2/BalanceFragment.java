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
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class BalanceFragment extends Fragment implements View.OnClickListener {


    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private NewsModel model;
    private TextView expenses, income, userHi;
    private ImageButton btnSpeak;

    // STT params
    private final int REQ_CODE_SPEECH_INPUT = 100;

    // Iteration params
    private String one, two, three, four, fullResponse;
    int itemNo ;
    int position = 0;

    // UI config
    private ListView itemList;
    private static CustomListAdapter adapter;

    private TextView balanceText , incomeText, expensesText;
    private View myView;

    private int incomeSum, expensesSum, total;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null)
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);



        myView = inflater.inflate(R.layout.fragment_balance, container, false);
        balanceText  = (TextView) myView.findViewById(R.id.balance);
        incomeText  = (TextView) myView.findViewById(R.id.income);
        expensesText  = (TextView) myView.findViewById(R.id.expenses);


        // Enable the ability for the user to speak to the application
        btnSpeak = (ImageButton) myView.findViewById(R.id.btnSpeak);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(myView.getContext());
        itemNo = preferences.getInt("Item", 0);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getDisplayName();


        // Access to DB
        model = new NewsModel();
        database = FirebaseDatabase.getInstance();


        // Microphone to listen to the user when activated
        btnSpeak.setOnClickListener(this);


        // Show the results
        categoryTotal();

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

    public void updateArticleView(int position) {

        mCurrentPosition = position;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }

    public void categoryTotal(){

        myRef = database.getReference("items");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                // Using generic type because it will suit for any type of object that could be processed
                // Initialising the array
                ArrayList<Article> fullItemList = new ArrayList<Article>();

                // Sorting the array
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    fullItemList.add(child.getValue(Article.class));
                }
                int sum = 0 ;

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
                    }
                }


                expensesSum = sum;

                // Check if it's the right sum
                Log.d("Sum", "Expenses : " + expensesSum + "");


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Failed", "Failed to read value.", error.toException());
            }
        });

        myRef = database.getReference("income");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Article> fullItemList = new ArrayList<Article>();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    fullItemList.add(child.getValue(Article.class));
                }
                assert fullItemList != null;
                for (int i = 0 ; i < fullItemList.size(); i++){
                    if(fullItemList.get(i) == null){
                        fullItemList.remove(i);

                    } else {
                        Log.i("list" + i, fullItemList.get(i).getItem());
                    }

                    // Retrieve each item
                    String liveprice = fullItemList.get(i).getValue();

                    // Remove all € signs
                    String newStr = liveprice.replace("€", "");

                    // Remove all commas
                    String newStr1 = newStr.replace(",", "");

                    // Replace all letters with 0
                    String newStr2 = newStr1.replaceAll("[A-Za-z]", "0");

                    // Add everything together
                    incomeSum = incomeSum + Integer.parseInt(newStr2);
                }


                Log.d("Sum", "Income : " + incomeSum + "");

                total = incomeSum - expensesSum;


                balanceText.setText("Current balance: €" + NumberFormat.getNumberInstance(Locale.US).format(total));
                incomeText.setText("Total income: €" + NumberFormat.getNumberInstance(Locale.US).format(incomeSum));
                expensesText.setText("Total expenses: €" + NumberFormat.getNumberInstance(Locale.US).format(expensesSum));

                PopulateView(fullItemList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(myView.getContext(), "Oops, something went wrong, try again", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(myView.getContext(),
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
                        Toast.makeText(myView.getContext(), "Oops! There was a problem, try again! ", Toast.LENGTH_LONG).show();


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

                            Toast.makeText(myView.getContext(), "Oops! There was an error, try again! ", Toast.LENGTH_LONG).show();

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
            Toast.makeText(myView.getContext(), "The value you have entered was wrong!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(myView.getContext(),  " '" + one  + "' has been added to your list", Toast.LENGTH_LONG).show();
            Article items = new Article(name, value , date, category);
            model.addArticle(new Article(one, two, date, category));

            myRef = database.getReference("income");
            myRef.child(itemID).setValue(items);

            itemNo++ ;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(myView.getContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("Item", itemNo);
            editor.apply();
        }
    }


    /**
     * Populating the view with the retrieved data
     * which is encapsulated using a list view
     * with a custom listView adapter
     *
     * @param model
     */
    public void PopulateView(ArrayList<Article> model){
        itemList = myView.findViewById(R.id.showcase);

        adapter = new CustomListAdapter(model, myView.getContext());

        itemList.setAdapter(adapter);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Vibrator vibe = (Vibrator) myView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.whoop);
                mp.start();
                if (vibe != null) {
                    vibe.vibrate(100);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        promptSpeechInput();
    }
}
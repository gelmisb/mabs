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

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    private ImageButton btnSpeak, addNew;

    // STT params
    private final int REQ_CODE_SPEECH_INPUT = 100;

    // Iteration params
    private String one, two, three;
    int itemNo ;
    int position = 0;

    // Global for further uses
    private View myView;

    private String m_Text = "";

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
        addNew = (ImageButton) myView.findViewById(R.id.addNew);

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
        addNew.setOnClickListener(this);


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

                // Using generic type because it will suit for any type of object that could be processed
                // Initialising the array
                ArrayList<Article> fullItemList = new ArrayList<Article>();

                // Sorting the array
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    fullItemList.add(child.getValue(Article.class));
                }

                // Full some of the expenses
                int sum = 0 ;

                // Asserting
                if(fullItemList != null) {

                    // For the size of the list
                    for (int i = 0; i < fullItemList.size(); i++) {

                        if (fullItemList.get(i) != null) {

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

                }

                // Show it to the user
                expenses = myView.findViewById(R.id.expenses);

                // Show the total to the user
                if(sum != 0)
                    expenses.setText("Today's expenses: €" + NumberFormat.getNumberInstance(Locale.US).format(sum));

                else
                    expenses.setText("Today's expenses: €0");

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

    /***
     * Writing a new item into the firebase database
     *
     * This method first double checks for any
     * errors and then proceeds to write any
     * given information to the database
     *
     *
     * @param itemID
     * @param name
     * @param value
     * @param category
     * @param date
     */
    private void writeNewItem(String itemID, String name, String value, String category, String date) {


        // Checking whether the given string is suited for processing
        if(isNoNumberAtBeginning(value)){

            // Notify the user of any errors
            Toast.makeText(getActivity().getApplicationContext(), "The value you have entered was wrong!", Toast.LENGTH_LONG).show();
        } else {

            // Notify the user that the item has been added
            // Creating a custom toast
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), " '" + name  + "' has been added to your list", Toast.LENGTH_LONG);

            // Defines the view for display purposes
            View view = toast.getView();

            // Setting the background
            view.setBackgroundResource(R.drawable.toastieslayout);

            // Setting the gravity, this stays at the center of the screen at the very bottom
            toast.setGravity(Gravity.BOTTOM, 0, 100);

            // Showing the created toast
            toast.show();

            // Adding a new item through MVC
            Article items = new Article(name, value , date, category);

            // Defining the MVC model
            model.addArticle(new Article(one, two, date, category));


            // Getting the reference of the database
            myRef = database.getReference("items");

            // Setting the value for the count
            myRef.child(itemID).setValue(items);

            // Increasing the item number when the
            // item has been fully submitted
            itemNo++ ;

            // This is for storing the item number locally
            // This means that we can track the items not only through
            // the firebase given analytics but also through the device
            // as long as the application is not reset

            // Getting sharedPreferences ready for caching
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

            // Defining the editor
            SharedPreferences.Editor editor = preferences.edit();

            // Putting the information
            editor.putInt("Item", itemNo);

            // Submitting the request
            editor.apply();
        }
    }


    /**
     * Updates the fragment view
     *
     * @param position
     */
    public void updateArticleView(int position) { mCurrentPosition = position; }


    /**
     * SavedInstanceState to follow the path of activity
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {

        // Switch statements were most efficient to use here
        // DO NOT CHANGE
        switch (v.getId()){

            case R.id.btnSpeak:

                promptSpeechInput();

                break;

            case R.id.addNew:

                onAddNewItem();

                break;


            default:
                break;

        }
    }


    /**
     * Adding a new item manually
     * This has been created upon request
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onAddNewItem() {

        //Alert dialog addition
        AlertDialog.Builder builder = new AlertDialog.Builder(myView.getContext());

        // Setting a native title
        builder.setTitle("Add new item");

        // Creating a layout for multiple editText's
        LinearLayout layout = new LinearLayout(myView.getContext());

        // Setting a native orientation
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add a TextView here for the "Title" label, as noted in the comments
        final EditText inputBox = new EditText(myView.getContext());
        // Setting a hint for the user
        inputBox.setHint("Item");

        // Add another TextView here for the "Description" label
        final EditText valueBox = new EditText(myView.getContext());
        // Setting a hint for the user
        valueBox.setHint("Value");

        // Add a TextView here for the "Title" label, as noted in the comments
        final EditText categoryBox = new EditText(myView.getContext());
        // Setting a hint for the user
        categoryBox.setHint("Category");


        // Adding editText's to the layout
        layout.addView(inputBox);
        layout.addView(valueBox);
        layout.addView(categoryBox);

        // Setting alert message
        builder.setMessage("Please input the item details manually");

        // What happens when the user chooses to add the item
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                //  Getting the strings from the editText
                String item = inputBox.getText().toString();

                //  Getting the strings from the editText
                String value = valueBox.getText().toString();

                //  Getting the strings from the editText
                String cat = categoryBox.getText().toString();

                // gets the current time
                Date c = Calendar.getInstance().getTime();

                // Formatting the date to match Firebase format
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");

                // Casting to a string format
                String formattedDate = df.format(c);

                // Writes to DB
                writeNewItem(itemNo + "", item, value, cat, formattedDate);
            }
        });


        // What happens when the user chooses to cancel this action
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        // Setting the view for thew layout
        builder.setView(layout);

        // Showing the alert builder
        builder.show();

    }
}
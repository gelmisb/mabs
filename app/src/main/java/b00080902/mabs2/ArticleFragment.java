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

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.firebase.ui.auth.AuthUI.TAG;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;


/**
 *  An activity for displaying
 *  the items in a custom list
 */
public class ArticleFragment extends Fragment implements View.OnClickListener {


    // Defining the textViews
    private static TextView start, end, totalDay, totalItems;

    // Some UI configs
    private Button selectDate1, selectDate2, showList;
    private EditText searching;


    // Fragment params
    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;

    // Database config
    private DatabaseReference myRef;
    private static View myView;
    private FirebaseDatabase database;

    // UI config
    private ListView itemList;
    private static CustomListAdapter adapter;
    private NewsModel model;

    // For displaying the sum to the user
    int sum = 0 ;
    int allItems = 0 ;
    CustomDialog customDialog;
    private String userName, userID;

    /**
     * If activity recreated (such as from screen rotate), restore
     * the previous article selection set by onSaveInstanceState().
     * This is primarily necessary when in the two-pane layout.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null)
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);

        // Getting the current view
        // this allows to retrieve the current context
        // from the base activity
        myView = inflater.inflate(R.layout.article_view, container, false);

        showList = (Button) myView.findViewById(R.id.showList);


        // Defining the textViews
        start = (TextView) myView.findViewById(R.id.startDate);
        end = (TextView) myView.findViewById(R.id.endDate);
        totalDay = (TextView) myView.findViewById(R.id.totalDay);
        totalItems = (TextView) myView.findViewById(R.id.totalItems);

        // EditText for the the input
        searching = (EditText) myView.findViewById(R.id.searching);


        // Buttons for the dates
        selectDate1 = (Button) myView.findViewById(R.id.picDate);
        selectDate2 = (Button) myView.findViewById(R.id.picDate2);

        // To commit any changes


        // Since this fragment is partially an OnClickListener
        // The buttons are added straight to the fragment
        selectDate1.setOnClickListener(this);
        selectDate2.setOnClickListener(this);
        showList.setOnClickListener(this);


        // Defining the model for MVC
        model = new NewsModel();

        // Getting the reference for the database
        database = FirebaseDatabase.getInstance();

        // Firebase username
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        userName = user.getDisplayName();
        userID = user.getUid();



        // Showing the full list at the very beginning
        fullList();

        // Inflate the layout for this fragment
        return myView;
    }



    /**
     * During startup, check if there are
     * arguments passed to the fragment
     */
    @Override
    public void onStart() {
        super.onStart();

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
     * Structuring the database so it would be able
     * to retrieve the wanted information about the
     * items using a genericTypeIndicator for arrayLists
     *
     */
    public void recallDB() {

        // Getting the DB reference
        myRef = database.getReference(userID);


        // Getting the dates
        final String from = start.getText().toString();
        final String to = end.getText().toString();
        final String name = searching.getText().toString();


        sum = 0;
        allItems = 0;

        myRef.orderByChild("date").startAt(from).endAt(to).addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (from.isEmpty() || to.isEmpty())
                    Toast.makeText(getActivity().getApplicationContext(), "Incorrect dates were entered", Toast.LENGTH_SHORT).show();


                myRef.orderByChild("item").equalTo(name).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long value = dataSnapshot.getChildrenCount();

                        if (value > 0)
                            try {

                                ArrayList<Article> fullItemList = new ArrayList<Article>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    fullItemList.add(child.getValue(Article.class));
                                }
                                assert fullItemList != null;
                                for (int i = 0; i < fullItemList.size(); i++) {
                                    if (fullItemList.get(i) == null) {
                                        fullItemList.remove(i);

                                    } else {

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

                                        allItems += 1;
                                    }
                                }
                                totalDay.setText("Total: €" + sum);
                                totalItems.setText("Items: " + allItems);

                                PopulateView(fullItemList);
                            } catch (NullPointerException e) {
                                Toast.makeText(getActivity().getApplicationContext(), "No items found for this date", Toast.LENGTH_SHORT).show();
                            }

                        else {
                            Toast.makeText(getActivity().getApplicationContext(), "No items found for this date", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Toast.makeText(getActivity().getApplicationContext(), "Oops, something went wrong, try again", Toast.LENGTH_SHORT).show();
                        Log.w("Failed", "Failed to read value.", error.toException());
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity().getApplicationContext(), "Oops, something went wrong, try again", Toast.LENGTH_SHORT).show();

            }
        });

    }
    /**
     * Structuring the database so it would be able
     * to retrieve the wanted information about the
     * items using a genericTypeIndicator for arrayLists
     *
     */
    public void fullList(){

        // Getting the DB reference
        myRef = database.getReference(userID);



        myRef.orderByChild("type").equalTo("Expenses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                try{

                    ArrayList<Article> fullItemList = new ArrayList<Article>();
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        fullItemList.add(child.getValue(Article.class));
                    }
                    assert fullItemList != null;
                    for (int i = 0 ; i < fullItemList.size(); i++){
                        if(fullItemList.get(i) == null){
                            fullItemList.remove(i);

                        } else {
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

                            allItems += 1;
                        }
                    }
                    totalDay.setText("Total: €" + sum);
                    totalItems.setText("Items: " + allItems);

                    PopulateView(fullItemList);
                } catch (NullPointerException e){
                    Toast.makeText(getActivity().getApplicationContext(), "No items found for this date", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getActivity().getApplicationContext(), "Oops, something went wrong, try again", Toast.LENGTH_SHORT).show();
                Log.w("Failed", "Failed to read value.", error.toException());
            }
        });
    }


    /**
     * Populating the view with the retrieved data
     * which is encapsulated using a list view
     * with a custom listView adapter
     *
     * @param model
     */
    public void PopulateView(ArrayList<Article> model){
        itemList = myView.findViewById(R.id.itemList);

        adapter = new CustomListAdapter(model, "expenses", getActivity().getBaseContext());

        itemList.setAdapter(adapter);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Vibrator vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                MediaPlayer mp = MediaPlayer.create(getActivity().getBaseContext(), R.raw.whoop);
                mp.start();
                if (vibe != null) {
                    vibe.vibrate(100);
                }
            }
        });

    }


    /**
     * Updating the view in order
     * to show the user the list they have already submitted
     *
     * @param position
     */
    public void updateArticleView(int position) {


        mCurrentPosition = position;

    }

    /**
     * Beginning from the savedInstanceState
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }


    @Override
    public void onClick(View v) {

        Vibrator vibe = (Vibrator) myView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        MediaPlayer mp = MediaPlayer.create(myView.getContext(), R.raw.whoop);


        switch (v.getId()){
            case R.id.picDate:

                mp.start();

                if (vibe != null)
                    vibe.vibrate(50);

                DialogFragment newFragment = new SelectDateFragment();
                assert getFragmentManager() != null;
                newFragment.show(getFragmentManager(), "DatePicker");

                break;

            case R.id.picDate2:

                mp.start();

                if (vibe != null)
                    vibe.vibrate(50);

                DialogFragment newFragment1 = new SelectDateFragment1();
                assert getFragmentManager() != null;
                newFragment1.show(getFragmentManager(), "DatePicker");

                break;

            case R.id.showList:

                mp.start();

                if (vibe != null)
                    vibe.vibrate(50);
//
//                customDialog = new CustomDialog();
//                customDialog.setupDialog(myView.getContext());
                recallDB();


                break;

            default:
                break;
        }


    }

    /**
     * Create a SelectDateFragment class that extends DialogFragment.
     * Define the onCreateDialog() method to return an instance of DatePickerDialog
     */
    public static class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            start = (TextView) myView.findViewById(R.id.startDate);

            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm + 1, dd);
        }

        public void populateSetDate(int year, int month, int day) {
            start.setText(day + "-0" + month+ "-" + year);
        }
    }

    /**
     * Create a SelectDateFragment class that extends DialogFragment.
     * Define the onCreateDialog() method to return an instance of DatePickerDialog
     */
    public static class SelectDateFragment1 extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm + 1, dd);
        }

        public void populateSetDate(int year, int month, int day) {
            end.setText(day + "-0" + month+ "-" + year);
        }
    }
}



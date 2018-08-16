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

public class ArticleFragment extends Fragment implements View.OnClickListener {

    private static TextView start, end;

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

    private Calendar myCalendar;
    private Button selectDate1, selectDate2, showList;
    private DatePickerDialog.OnDateSetListener date;

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

        myView = inflater.inflate(R.layout.article_view, container, false);

        start = (TextView) myView.findViewById(R.id.startDate);
        end = (TextView) myView.findViewById(R.id.endDate);




        selectDate1 = (Button) myView.findViewById(R.id.picDate);
        selectDate2 = (Button) myView.findViewById(R.id.picDate2);
        showList = (Button) myView.findViewById(R.id.showList);

        selectDate1.setOnClickListener(this);
        selectDate2.setOnClickListener(this);
        showList.setOnClickListener(this);



        model = new NewsModel();
        database = FirebaseDatabase.getInstance();


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
    public void recallDB(){

        String from = start.getText().toString();
        String to = end.getText().toString();

        myRef = database.getReference("items");


        if(from.isEmpty() || to.isEmpty())
            Toast.makeText(getActivity().getApplicationContext(), "Incorrect dates were entered", Toast.LENGTH_SHORT).show();


        myRef.orderByChild("date").startAt(from).endAt(to).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long value=dataSnapshot.getChildrenCount();
                Log.d("Number","no of children: "+value);

                if(value > 0 )
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
                                Log.i("list" + i, fullItemList.get(i).getItem());

                            }
                        }

                        PopulateView(fullItemList);
                    } catch (NullPointerException e){
                        Toast.makeText(getActivity().getApplicationContext(), "No items found for this date", Toast.LENGTH_SHORT).show();
                    } else {
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

        adapter = new CustomListAdapter(model, getActivity().getBaseContext());

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


        switch (v.getId()){
            case R.id.picDate:
                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getFragmentManager(), "DatePicker");

                break;

            case R.id.picDate2:
                DialogFragment newFragment1 = new SelectDateFragment1();
                newFragment1.show(getFragmentManager(), "DatePicker");

                break;

            case R.id.showList:
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



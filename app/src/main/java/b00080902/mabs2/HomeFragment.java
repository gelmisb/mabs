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

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.Objects;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.firebase.ui.auth.AuthUI.getInstance;

public class HomeFragment extends Fragment {


    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private NewsModel model;
    private TextView expenses, income;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
        }

        income = (TextView)getActivity().findViewById(R.id.income);


        // Access to DB
        model = new NewsModel();
        database = FirebaseDatabase.getInstance();

        // Show the results
        recallDB();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);


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

                    String liveprice = fullItemList.get(i).getValue();

                    String newStr = liveprice.replaceAll("[€,]", "").trim();

                    sum = sum + Integer.parseInt(newStr);

                }

                Log.d("Sum", "Sum is: : " + sum + "");
                expenses = (TextView) Objects.requireNonNull(getActivity()).findViewById(R.id.expenses);

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
     * Populating the view with the retrieved data
     * which is encapsulated using a list view
     * with a custom listView adapter
     *
     * @param model
     */
    public void PopulateView(ArrayList<Article> model){



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


}
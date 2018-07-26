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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.firebase.ui.auth.AuthUI.getInstance;

public class ArticleFragment extends Fragment {


    // Fragment params
    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;

    // Database config
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    // UI config
    private ListView itemList;
    private static CustomListAdapter adapter;
    private NewsModel model;



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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null)
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);


        model = new NewsModel();
        database = FirebaseDatabase.getInstance();


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.article_view, container, false);
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

        myRef = database.getReference("items");


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long value=dataSnapshot.getChildrenCount();
                Log.d("Number","no of children: "+value);

                GenericTypeIndicator<ArrayList<Article>> genericTypeIndicator =new GenericTypeIndicator<ArrayList<Article>>(){};

                ArrayList<Article> fullItemList = dataSnapshot.getValue(genericTypeIndicator);

                PopulateView(fullItemList);
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
        itemList = Objects.requireNonNull(getActivity()).findViewById(R.id.itemList);

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

        recallDB();

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


}
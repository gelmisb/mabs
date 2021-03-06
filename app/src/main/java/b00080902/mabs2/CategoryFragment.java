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
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CategoryFragment extends Fragment implements View.OnClickListener {


    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private NewsModel model;
    private TextView expenses, income;
    private Button press, press1, press2, press3, press4, press5, press6, press7, press8;

    private View myView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null)
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);

        myView = inflater.inflate(R.layout.fragment_category, container, false);

        income = (TextView)myView.findViewById(R.id.income);
        press = (Button)myView.findViewById(R.id.housing);
        press1 = (Button)myView.findViewById(R.id.fuel);
        press2 = (Button)myView.findViewById(R.id.food);
        press3 = (Button)myView.findViewById(R.id.house);
        press4 = (Button)myView.findViewById(R.id.leisure);
        press5 = (Button)myView.findViewById(R.id.tel);
        press6 = (Button)myView.findViewById(R.id.other);
        press7 = (Button)myView.findViewById(R.id.transport);
        press8 = (Button)myView.findViewById(R.id.util);



        press.setOnClickListener(this);
        press1.setOnClickListener(this);
        press2.setOnClickListener(this);
        press3.setOnClickListener(this);
        press4.setOnClickListener(this);
        press5.setOnClickListener(this);
        press6.setOnClickListener(this);
        press7.setOnClickListener(this);
        press8.setOnClickListener(this);


        // Access to DB
        model = new NewsModel();
        database = FirebaseDatabase.getInstance();

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




    @Override
    public void onClick(View v) {


        Vibrator vibe = (Vibrator) myView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        MediaPlayer mp = MediaPlayer.create(myView.getContext(), R.raw.whoop);

        Intent intent = new Intent(myView.getContext(), CatHouse.class);

        switch (v.getId()) {

            case R.id.house:


                intent.putExtra("cat", "House");

                break;

            case R.id.housing:

                intent.putExtra("cat", "Housing");

                break;
            case R.id.fuel:

                intent.putExtra("cat", "Fuel");

                break;
            case R.id.food:

                intent.putExtra("cat", "Food");

                break;
            case R.id.tel:

                intent.putExtra("cat", "Telephone");

                break;
            case R.id.other:

                intent.putExtra("cat", "Other");

                break;
            case R.id.transport:

                intent.putExtra("cat", "Transport");

                break;
            case R.id.util:

                intent.putExtra("cat", "Utilities");

                break;

            case R.id.leisure:

                intent.putExtra("cat", "Leisure");

                break;


            default:

                break;

        }
        startActivity(intent);


        mp.start();

        if (vibe != null)
            vibe.vibrate(100);

    }
}
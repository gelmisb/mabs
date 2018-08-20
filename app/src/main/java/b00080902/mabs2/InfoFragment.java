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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InfoFragment extends Fragment implements View.OnClickListener {


    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;

    private Button call, find;

    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private NewsModel model;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null)
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);

        View myView = inflater.inflate(R.layout.fragment_info, container, false);

        call = (Button) myView.findViewById(R.id.call);
        find = (Button) myView.findViewById(R.id.find);

        call.setOnClickListener(this);
        find.setOnClickListener(this);


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


        switch (v.getId()){

            case R.id.call:
                Intent callIntent = new Intent(Intent.ACTION_DIAL );
                callIntent.setData(Uri.parse("tel:0761 07 2000"));

                if (ActivityCompat.checkSelfPermission(getActivity().getBaseContext(),
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                }
                startActivity(callIntent);

                break;

            case R.id.find:

                Uri gmmIntentUri = Uri.parse("geo:0,0?q=mabs");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);


                break;

            default:
                break;


        }


    }
}
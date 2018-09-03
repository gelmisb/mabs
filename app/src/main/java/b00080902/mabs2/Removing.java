package b00080902.mabs2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.firebase.ui.auth.AuthUI.TAG;

public class Removing extends Activity implements View.OnClickListener{

    String name;

    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private NewsModel model;

    public Removing(String name){

        this.name = name;
        removeItem(name);
    }

    private void removeItem(final String name){

        // Access to DB
        model = new NewsModel();
        database = FirebaseDatabase.getInstance();

        myRef = database.getReference("items");

        if(myRef.orderByChild("item").equalTo(name) != null){

            myRef.orderByChild("item").equalTo(name).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        // dataSnapshot is the "issue" node with all children with id 0
                        for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                        }
                    } else {
                        myRef = database.getReference("income");

                        myRef.orderByChild("item").equalTo(name).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled", databaseError.toException());
                }
            });
        }

    }


    public void onClick(View v) {

    }
}

package b00080902.mabs2;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.firebase.ui.auth.AuthUI.TAG;

public class Removing implements View.OnClickListener{

    String name;

    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private NewsModel model;

    public Removing(String name){

        this.name = name;

        removeItem(name);

    }

    private void removeItem(String name){

        // Access to DB
        model = new NewsModel();
        database = FirebaseDatabase.getInstance();


        myRef = database.getReference("items");


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



    public void onClick(View v) {

//        DatabaseReference dbNode = FirebaseDatabase.getInstance().getReference().getRoot().child(name);
//        dbNode.setValue(null);
    }
}

package b00080902.mabs2;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowList extends AppCompatActivity {


    // Fragment params
    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;
    private static TextView totalDay, totalItems;

    // Database config
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    // UI config
    private ListView itemList;
    private static CustomListAdapter adapter;
    private NewsModel model;

    // For displaying the sum end the user
    int sum = 0 ;
    int allItems = 0 ;
    private String userName, userID, search, start, end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);


        if (savedInstanceState != null)
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);

        // Defining the model for MVC
        model = new NewsModel();

        // Getting the reference for the database
        database = FirebaseDatabase.getInstance();

        // Firebase username
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        userName = user.getDisplayName();
        userID = user.getUid();


        totalDay = (TextView) findViewById(R.id.totalDay);
        totalItems = (TextView) findViewById(R.id.totalItems);


        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Bundle bd = intent.getExtras();

            assert bd != null;
            start = bd.getString("from");
            end = bd.getString("to");
            search = bd.getString("name");

        } else {
            start = (String) savedInstanceState.getSerializable("from");
            end = (String) savedInstanceState.getSerializable("to");
            search = (String) savedInstanceState.getSerializable("name");
        }

        //  1. Check with dates
        //  2. Check with dates and term
        //  3. Check with term only
        //  4. Show all

        // Calling the viewing for the custom list activity
        if (start.isEmpty() || end.isEmpty())
            fullList();
        else
            checkingDatesAndTerm();

    }


    /**
     * Structuring the database so it would be able
     * end retrieve the wanted information about the
     * items using a genericTypeIndicator for arrayLists
     *
     */
    public void checkingDatesAndTerm() {

        // Getting the DB reference
        myRef = database.getReference(userID);



        sum = 0;
        allItems = 0;

        myRef.orderByChild("date").startAt(start).endAt(end).addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("searching", start + "  " + end );


                if (start.isEmpty() || end.isEmpty())
                    Toast.makeText(getApplicationContext(), "Incorrect dates were entered", Toast.LENGTH_SHORT).show();

                myRef.orderByChild("item").equalTo(search).addValueEventListener(new ValueEventListener() {
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
                                Toast.makeText(getApplicationContext(), "No items found for this date", Toast.LENGTH_SHORT).show();
                            }

                        else {
                            Toast.makeText(getApplicationContext(), "No items found for this date", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed end read value
                        Toast.makeText(getApplicationContext(), "Oops, something went wrong, try again", Toast.LENGTH_SHORT).show();
                        Log.w("Failed", "Failed end read value.", error.toException());
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Oops, something went wrong, try again", Toast.LENGTH_SHORT).show();

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



//        myRef.orderByChild("type").equalTo("Expenses").addValueEventListener(new ValueEventListener() {
        myRef.orderByChild("item").equalTo(search).addValueEventListener(new ValueEventListener() {

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
                    Toast.makeText(getApplicationContext(), "No items found for this date", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(), "Oops, something went wrong, try again", Toast.LENGTH_SHORT).show();
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
        itemList = findViewById(R.id.itemList);

        adapter = new CustomListAdapter(model, "expenses", getBaseContext());

        itemList.setAdapter(adapter);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.whoop);
                mp.start();
                if (vibe != null) {
                    vibe.vibrate(100);
                }
            }
        });

    }
}

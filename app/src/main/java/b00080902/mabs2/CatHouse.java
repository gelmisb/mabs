package b00080902.mabs2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
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

public class CatHouse extends AppCompatActivity {



    // Database config
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    // UI config
    private ListView itemList;
    private static CustomListAdapter adapter;
    private NewsModel model;

    private String category = "";
    private String userName, userID;
    private ImageButton backButton;
    private TextView categoryHeading ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        // Set the right content view
        setContentView(R.layout.activity_cat_house);

        //  Fixed Portrait orientation
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        categoryHeading  = (TextView) findViewById(R.id.catTotal);


        backButton = (ImageButton)findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop talking when the application is closed.
                finish();

            }
        });

        Intent intent = getIntent();
        category = intent.getStringExtra("cat");


        // Firebase username
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        userName = user.getDisplayName();
        userID = user.getUid();


        // Instantiate initial params for DB
        model = new NewsModel();
        database = FirebaseDatabase.getInstance();

        recallDB();
        categoryTotal();
    }

    public void recallDB(){

        // Getting the DB reference
        myRef = database.getReference(userID);


        myRef.orderByChild("category").equalTo(category).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long value=dataSnapshot.getChildrenCount();
                Log.d("Number","no of children: "+value);

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
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(), "Oops, something went wrong, try again", Toast.LENGTH_SHORT).show();
                Log.w("Failed", "Failed to read value.", error.toException());
            }
        });
    }

    public void categoryTotal(){

        myRef = database.getReference(userID);

        myRef.orderByChild("category").equalTo(category).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                GenericTypeIndicator<ArrayList<Article>> genericTypeIndicator =new GenericTypeIndicator<ArrayList<Article>>(){};
//
//                ArrayList<Article> fullItemList = dataSnapshot.getValue(genericTypeIndicator);



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

                int sum = 0 ;

                for(int i = 0; i < fullItemList.size(); i++){

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

                }
                String s1 = category.substring(0, 1).toUpperCase();
                String nameCapitalized = s1 + category.substring(1);


                categoryHeading.setText(nameCapitalized  + ": €" + sum);

                PopulateView(fullItemList);
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
        itemList = findViewById(R.id.showcase);

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

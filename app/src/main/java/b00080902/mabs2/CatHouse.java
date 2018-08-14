package b00080902.mabs2;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CatHouse extends AppCompatActivity {



    // Database config
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    // UI config
    private ListView itemList;
    private static CustomListAdapter adapter;
    private NewsModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Fullscreen without a title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Overwriting the fullscreen parameters
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        // Set the right content view
        setContentView(R.layout.activity_cat_house);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //  Fixed Portrait orientation
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        // Instantiate initial params for DB
        model = new NewsModel();
        database = FirebaseDatabase.getInstance();

        recallDB();
    }

    public void recallDB(){

        myRef = database.getReference("items");

        myRef.orderByChild("category").equalTo("house").addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long value=dataSnapshot.getChildrenCount();
                Log.d("Number","no of children: "+value);

//                GenericTypeIndicator<ArrayList<Article>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Article>>(){};
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

/*
    public void recallDB(){

        myRef = database.getReference("items");

//                    item, String value, String date, String category)

        myRef.orderByChild("category").equalTo("house").addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                GenericTypeIndicator<ArrayList<Article>> genericTypeIndicator =new GenericTypeIndicator<ArrayList<Article>>(){};

                ArrayList<Article> list  = dataSnapshot.getValue(genericTypeIndicator);

//                for (DataSnapshot child: dataSnapshot.getChildren()) {
//                    Log.i("Real", child.getValue(Article.class).getItem() + "");
//                    list.add(new Article(
//                          child.getValue(Article.class).getItem(),
//                          child.getValue(Article.class).getValue(),
//                          child.getValue(Article.class).getDate(),
//                          child.getValue(Article.class).getCategory())
//                      );
//                    break;
//
//                }

                PopulateView(list);



//                    list.add(child.getValue(Friends.class));

//
//                for(DataSnapshot datas: dataSnapshot.getChildren()){
//                    String date =datas.child("date").getValue()   + "";
//                    String cat = datas.getValue(Article.class).getCategory();
//                    String item = datas.getValue(Article.class).getItem();
//                    String value = datas.getValue(Article.class).getValue();
//
//                    Article a = new Article(date, cat, item, value);
//
//                    Log.i("Super", a.toString());
//
//
//                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("Failed", "Failed to read value.", error.toException());
            }
        });
    }
*/
    /**
     * Populating the view with the retrieved data
     * which is encapsulated using a list view
     * with a custom listView adapter
     *
     * @param model
     */
    public void PopulateView(ArrayList<Article> model){
        itemList = findViewById(R.id.showcase);

        adapter = new CustomListAdapter(model, getBaseContext());

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

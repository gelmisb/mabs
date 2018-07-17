package b00080902.mabs2;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements HeadlinesFragment.OnHeadlineSelectedListener{

    private static final String TAG_FRAGMENT_ONE = "fragment_one";
    private static final String TAG_FRAGMENT_TWO = "fragment_two";
    private static final String TAG_FRAGMENT_THREE = "fragment_three";

    private TextView txtSpeechInput;
    private ImageButton btnSpeak, btnSignOut;
    private Button btnList;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    DatabaseReference myRef;
    FirebaseDatabase database;
    private String one, two, three, four, fullResponse;
    private ListView itemList;
    private static CustomListAdapter adapter;
    private ArrayList<String> full ;
    private NewsModel model;
    int itemNo = 4;


    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private BottomNavigationView bottomNavigationView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = new NewsModel();


        // Fullscreen without a title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Overwriting the fullscreen parameters
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        // Set the right content view
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //  Fixed Portrait orientation
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);





        database = FirebaseDatabase.getInstance();

//        final BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigationView);
//
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//
//
//
//                int id = item.getItemId();
//
//                if (id == R.id.navigation_home) {
//
//                    return true;
//                } else if (id == R.id.navigation_list) {
//
//                    ListFragment lf = ListFragment.newInstance();
////                    openFragment( android.support.v4.app.Fragment.);
//                    return true;
//                } else if (id == R.id.navigation_balance) {
//                    return true;
//
//                } else if (id == R.id.navigation_info) {
//                    return true;
//                }
//
//                return false;
//            }
//        });


        /////////////////////////////////////////////////////////////////////////////////////

        fragmentManager = getSupportFragmentManager();

        Fragment fragment = fragmentManager.findFragmentByTag("Lists");
        if (fragment == null) {
            fragment = ListFragment.newInstance();
        }
        replaceFragment(fragment, "Lists");

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_home:
//                        // I'm aware that this code can be optimized by a method which accepts a class definition and returns the proper fragment
//                        fragment = fragmentManager.findFragmentByTag("Home");
//                        if (fragment == null) {
//                            fragment = Home.newInstance();
//                        }
//                        replaceFragment(fragment, TAG_FRAGMENT_ONE);
//                        break;
                    case R.id.navigation_list:
                        fragment = fragmentManager.findFragmentByTag("Lists");
                        if (fragment == null) {
                            fragment = ListFragment.newInstance();
                        }
                        replaceFragment(fragment, "Lists");
                        break;
//                    case R.id.navigation_balance:
//                        fragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT_THREE);
//                        if (fragment == null) {
//                            fragment = ThirdFragment.newInstance();
//                        }
//                        replaceFragment(fragment, TAG_FRAGMENT_THREE);
//                        break;
                }
                return true;
            }
        });
        /// //////////////////////////////////////////////////////////////////////////////////////////////




        // Initials for recording
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        // Showing the lists of all the items that  have been added
        btnList = (Button) findViewById(R.id.btnList);
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent  = new Intent(getApplicationContext(), Lists.class);
//                startActivity(intent);
            }
        });

        // User sign out using Firebase
        btnSignOut = (ImageButton) findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), WelcomeScreen.class);
                startActivity(intent);
            }
        });

        recallDB();
    }


    private void replaceFragment(@NonNull Fragment fragment, @NonNull String tag) {
        if (!fragment.equals(currentFragment)) {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, fragment, tag)
                    .commit();
            currentFragment = fragment;
        }
    }

//    private void openFragment(Fragment fragment) {
//        transaction = getFragmentManager().beginTransaction();
//        fragment = android.app.Fragment;
//        transaction.replace(R.id.container, fragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
//    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void PopulateView(ArrayList<Article> model){

        itemList = (ListView) findViewById(R.id.itemList);

        adapter = new CustomListAdapter(model,getBaseContext());

        itemList.setAdapter(adapter);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);
                mp.start();
                if (vibe != null) {
                    vibe.vibrate(100);
                }
            }
        });

    }


    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    itemNo++;

                    // Stores result
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    // Shows output to user
                    txtSpeechInput.setText(result.get(0));

                    fullResponse = result.get(0);

                    // Splits the string
                    String[] alphabets= result.get(0).split("\\s");

                    // Segments the string
                    one = alphabets[0];
                    two = alphabets[1];
//                    three = alphabets[2];
//                    three = alphabets[2];

                    // gets the current time
                    Date currentTime = Calendar.getInstance().getTime();
                    // Logs the details for the dev
                    Log.i("IMPORTANT", one);
                    Log.i("IMPORTANT1", two);


                    // Writes to DB
                    writeNewItem(itemNo + "", one, two, currentTime.toString());
                }
                break;
            }

        }


    }
    private void writeNewItem(String itemID, String name, String value, String date) {
        Date currentTime = Calendar.getInstance().getTime();

        Article items = new Article(name, value, date);
        model.addArticle(new Article(one, two, currentTime.toString()));

        myRef = database.getReference("items");
        myRef.child(itemID).setValue(items);
    }

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
                Log.w("Failed", "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onArticleSelected(int position) {
        
    }
}

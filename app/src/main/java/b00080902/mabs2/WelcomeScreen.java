package b00080902.mabs2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class WelcomeScreen extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
        setContentView(R.layout.activity_welcome_screen);

        //  Fixed Portrait orientation
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);



        ImageButton log = (ImageButton)findViewById(R.id.log);

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);
                mp.start();

                if (vibe != null)
                    vibe.vibrate(100);

                userLogedIn();

            }
        });
    }


    public void userLogedIn(){
        // Check if user has already accessed the application or not
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Intent success = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(success);
        } else {
            // No user is signed in
            // Ask user to sign in
            Intent failure = new Intent(getApplicationContext(), SignIn.class);
            startActivity(failure);
        }
    }
}

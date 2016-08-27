package lam.project.foureventplannerdroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import lam.project.foureventplannerdroid.complete_profile.StepManager;
import lam.project.foureventplannerdroid.model.User;
import lam.project.foureventplannerdroid.utils.UserManager;


public class SplashActivity extends AppCompatActivity {

    private static final long MIN_WAIT_INTERVAL = 1500L;
    private static final long MAX_WAIT_INTERVAL = 3000L;
    private static final int GO_AHEAD_WHAT = 1;

    private long mStartTime;
    private boolean mIsDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        final ImageView logoImageView = (ImageView) findViewById(R.id.splash_imageview);
        assert logoImageView != null;
        logoImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
                if(elapsedTime >= MIN_WAIT_INTERVAL){

                    goAhead();
                }

                return false;
            }
        });
    }

    private void goAhead() {

       Intent intent;
       User user = UserManager.get(this).getUser();

        //se l'utente online non c'è
        if(user == null ){

            StepManager.get(this).setStep(StepManager.INCOMPLETE);

            intent = new Intent(this,RegistrationActivity.class);

        } else {

            intent = new Intent(this,MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}

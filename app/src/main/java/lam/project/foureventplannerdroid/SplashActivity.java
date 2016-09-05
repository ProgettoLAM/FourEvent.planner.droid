package lam.project.foureventplannerdroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import lam.project.foureventplannerdroid.complete_profile.StepManager;
import lam.project.foureventplannerdroid.model.Planner;
import lam.project.foureventplannerdroid.utils.shared_preferences.PlannerManager;


public class SplashActivity extends AppCompatActivity {

    private static final String IS_DONE_KEY = "IS_DONE_KEY";
    private static final String START_TIME_KEY = "START_TIME_KEY";

    private static final long MIN_WAIT_INTERVAL = 1500L;
    private static final long MAX_WAIT_INTERVAL = 3000L;
    private static final int GO_AHEAD_WHAT = 1;

    private long mStartTime = -1L;
    private boolean mIsDone;

    //Gestione del messaggio
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            //Se il messaggio è di proseguire, si aspetta il tempo minimo e poi si va avanti
            if(msg.what == GO_AHEAD_WHAT){

                long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
                if(elapsedTime >= MIN_WAIT_INTERVAL && !mIsDone){

                    mIsDone = true;
                    goAhead();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        if(mStartTime == -1L) {

            mStartTime = SystemClock.uptimeMillis();
        }
        //Si invia il messaggio di proseguire
        final Message goAheadMessage = mHandler.obtainMessage(GO_AHEAD_WHAT);
        mHandler.sendMessageAtTime(goAheadMessage,mStartTime+MAX_WAIT_INTERVAL);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        //Salvataggio del valore booleano se il tempo massimo è trascorso e del tempo stesso
        outState.putBoolean(IS_DONE_KEY,mIsDone);
        outState.putLong(START_TIME_KEY,mStartTime);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Recupero del valore booleano se il tempo è trascorso o no
        this.mIsDone = savedInstanceState.getBoolean(IS_DONE_KEY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Activity su display intero, nascondendo anche i comandi di navigazione
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        if(savedInstanceState != null) {
            //Si fa il restore del tempo trascorso dall'avvio della app
            this.mStartTime = savedInstanceState.getLong(START_TIME_KEY);
        }

        final ImageView logoImageView = (ImageView) findViewById(R.id.splash_imageview);
        assert logoImageView != null;

        //Con il click sul logo, si va avanti nella app, aspettando il tempo massimo
        logoImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
                if(elapsedTime >= MIN_WAIT_INTERVAL && !mIsDone){

                    mIsDone = true;
                    goAhead();
                }

                return false;
            }
        });
    }

    /**
     * Si va avanti, prendendo il planner corrente se presente e reindirizzarlo nella MainActivity,
     * altrimenti nella registrazione
     */
    private void goAhead() {

        Intent intent;
        Planner planner = PlannerManager.get(this).getUser();

        if(planner == null ){

            StepManager.get(this).setStep(StepManager.INCOMPLETE);
            intent = new Intent(this,RegistrationActivity.class);

        } else {

            intent = new Intent(this,MainActivity.class);
        }

        finish();
        startActivity(intent);
    }
}


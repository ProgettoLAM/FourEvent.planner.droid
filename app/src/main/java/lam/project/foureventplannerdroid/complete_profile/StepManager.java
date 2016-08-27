package lam.project.foureventplannerdroid.complete_profile;

import android.content.Context;
import android.content.SharedPreferences;

import lam.project.foureventplannerdroid.R;


/**
 * Created by spino on 12/08/16.
 */
public class StepManager {

    private static StepManager sInstance;

    private final SharedPreferences mSharedPreferences;

    private final String STEP = "step";

    public final static int INCOMPLETE = 0;

    public final static int COMPLETE = 1;


    private StepManager(final Context context){

        String SHARED_PREFERENCES_NAME = context.getResources().getString(R.string.shared_preferences_name);

        mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static StepManager get(Context context){

        if(sInstance == null){

            sInstance = new StepManager(context);
        }

        return sInstance;
    }

    public static StepManager get(){

        if(sInstance == null){

            throw new IllegalStateException("Invoke get(context) before!");
        }

        return sInstance;
    }

    public int getStep(){

        return mSharedPreferences.getInt(STEP, INCOMPLETE);
    }

    public boolean setStep(int step){

        return mSharedPreferences.edit().putInt(STEP,step).commit();
    }
}

package lam.project.foureventplannerdroid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Planner;


/**
 * Created by spino on 30/07/16.
 */
public final class PlannerManager {

    private static PlannerManager sInstance;

    private final SharedPreferences mSharedPreferences;

    private Planner mChacedPlanner;

    private PlannerManager(final Context context){

        String SHARED_PREFERENCES_NAME = context.getResources().getString(R.string.shared_preferences_name);

        mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static PlannerManager get(Context context){

        if(sInstance == null){

            sInstance = new PlannerManager(context);
        }

        return sInstance;
    }

    public static PlannerManager get(){

        if(sInstance == null){

            throw new IllegalStateException("Invoke get(context) before!");
        }

        return sInstance;
    }

    public @NonNull
    Planner getUser(){

        if(mChacedPlanner != null){

            return mChacedPlanner;
        }

        final String userAsString = mSharedPreferences.getString(Planner.Keys.USER,null);

        if (userAsString != null){

            try{

                mChacedPlanner = Planner.fromJson(new JSONObject(userAsString));
            }
            catch (JSONException je){

                je.printStackTrace();
            }
        }

        return mChacedPlanner;
    }

    public boolean save(@NonNull final Planner planner){

        mChacedPlanner = planner;

        try{

            JSONObject item = planner.toJson();
            return mSharedPreferences.edit().putString(Planner.Keys.USER,item.toString()).commit();
        }
        catch (JSONException js){

            js.printStackTrace();
            return false;
        }

    }

    public boolean remove() {

        mChacedPlanner = null;
        return mSharedPreferences.edit().remove(Planner.Keys.USER).commit();
    }
}

package lam.project.foureventplannerdroid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.User;


/**
 * Created by spino on 30/07/16.
 */
public final class UserManager {

    private static UserManager sInstance;

    private final SharedPreferences mSharedPreferences;

    private User mChacedUser;

    private UserManager(final Context context){

        String SHARED_PREFERENCES_NAME = context.getResources().getString(R.string.shared_preferences_name);

        mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static UserManager get(Context context){

        if(sInstance == null){

            sInstance = new UserManager(context);
        }

        return sInstance;
    }

    public static UserManager get(){

        if(sInstance == null){

            throw new IllegalStateException("Invoke get(context) before!");
        }

        return sInstance;
    }

    public @NonNull User getUser(){

        if(mChacedUser != null){

            return mChacedUser;
        }

        final String userAsString = mSharedPreferences.getString(User.Keys.USER,null);

        if (userAsString != null){

            try{

                mChacedUser = User.fromJson(new JSONObject(userAsString));
            }
            catch (JSONException je){

                je.printStackTrace();
            }
        }

        return mChacedUser;
    }

    public boolean save(@NonNull final User user){

        mChacedUser = user;

        try{

            JSONObject item = user.toJson();
            return mSharedPreferences.edit().putString(User.Keys.USER,item.toString()).commit();
        }
        catch (JSONException js){

            js.printStackTrace();
            return false;
        }

    }

    public boolean remove() {

        mChacedUser = null;
        return mSharedPreferences.edit().remove(User.Keys.USER).commit();
    }
}

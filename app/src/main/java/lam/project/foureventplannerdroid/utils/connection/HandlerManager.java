package lam.project.foureventplannerdroid.utils.connection;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import lam.project.foureventplannerdroid.R;

public class HandlerManager {

    private final static String MESSAGE = "message";
    private final static String EXCEPTION = "exception";

    private static HandlerManager instance;

    private HandlerManager() {    }

    public static HandlerManager getInstance() {

        if(instance == null)
            instance = new HandlerManager();

        return instance;
    }

    public String handleError (VolleyError error) {

        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null) {

            String json = new String(response.data);

            try {

                JSONObject obj = new JSONObject(json);

                return obj.getString(MESSAGE);

            } catch (JSONException e) {

                e.printStackTrace();
                return EXCEPTION;
            }
        }

        return EXCEPTION;
    }
}

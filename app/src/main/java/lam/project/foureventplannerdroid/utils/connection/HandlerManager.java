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
    private Context mContext;

    private static HandlerManager instance;

    private HandlerManager(Context context) {

        this.mContext = context;
    }

    public static HandlerManager getInstance(Context context) {

        if(instance == null)
            instance = new HandlerManager(context);

        return instance;
    }

    public static HandlerManager getInstance() {

        if(instance == null)
            throw new IllegalArgumentException("Errore");

        return instance;
    }

    public void handleError (VolleyError error, View view) {

        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null) {

            String json = new String(response.data);

            try {

                JSONObject obj = new JSONObject(json);

                Snackbar snackbar = Snackbar.make(view, obj.getString(MESSAGE), Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(mContext, R.color.lightGreen));
                snackbar.show();

            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
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

    public void handleSuccess (JSONObject response, View view) {

        try{

            Snackbar snackbar = Snackbar.make(view,response.getString("message"), Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(mContext, R.color.lightGreen));
            snackbar.show();
        }
        catch (JSONException e) {

            e.printStackTrace();
        }
    }
}

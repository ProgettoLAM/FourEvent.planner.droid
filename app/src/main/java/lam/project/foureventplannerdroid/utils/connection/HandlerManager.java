package lam.project.foureventplannerdroid.utils.connection;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Manager per la gestione delle eccezioni
 */
public class HandlerManager {

    private final static String MESSAGE = "Message";
    private final static String EXCEPTION = "Exception";

    public static String handleError (VolleyError error) {

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
        } else {

            return EXCEPTION;
        }
    }
}

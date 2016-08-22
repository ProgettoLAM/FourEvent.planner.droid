package lam.project.foureventplannerdroid.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Vale on 01/08/2016.
 */

public class VolleyRequest {

    private static RequestQueue mRequestQueue;
    private static VolleyRequest instance;
    private static Context context;

    private VolleyRequest(Context context) {

        VolleyRequest.context = context;

        if(mRequestQueue == null) {

            mRequestQueue = initRequestQueue();
            mRequestQueue.start();
        }
    }

    private RequestQueue initRequestQueue() {
        return Volley.newRequestQueue(context);
    }

    //Singleton, si crea una sola istanza della classe
    public static VolleyRequest get(Context context) {

        if(instance == null) {

            instance = new VolleyRequest(context);
        }

        return instance;
    }

    public static VolleyRequest get(){

        if(instance == null){

            throw new IllegalStateException("Context not initialized");
        }

        return instance;
    }

    public void add(Request request) {
        mRequestQueue.add(request);
    }

    public interface QueueProvider {

        RequestQueue getRequestQueue();
    }
}

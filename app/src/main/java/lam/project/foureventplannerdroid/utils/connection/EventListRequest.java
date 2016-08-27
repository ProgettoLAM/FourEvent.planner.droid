package lam.project.foureventplannerdroid.utils.connection;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import lam.project.foureventplannerdroid.model.Event;


/**
 * Created by Vale on 29/07/2016.
 */

public class EventListRequest extends JsonRequest<List<Event>> {

    public EventListRequest(String url, Response.Listener<List<Event>> listener, Response.ErrorListener errorListener) {
        super(Request.Method.GET, url, null, listener, errorListener);
    }

    @Override
    protected Response<List<Event>> parseNetworkResponse(NetworkResponse response) {
        List<Event> events = new LinkedList<>();
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONArray jsonArray = new JSONArray(jsonString);

            for(int i = 0; i < jsonArray.length(); i++) {
                final JSONObject item = jsonArray.getJSONObject(i);
                final Event event = Event.fromJson(item);
                events.add(event);
            }

            return Response.success(events, HttpHeaderParser.parseCacheHeaders(response));
        }
        catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
        catch (JSONException je) {
            return Response.error(new ParseError(je));

        }
    }
}

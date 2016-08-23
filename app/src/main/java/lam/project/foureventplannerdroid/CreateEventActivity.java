package lam.project.foureventplannerdroid;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.utils.CustomRequest;
import lam.project.foureventplannerdroid.utils.FourEventUri;
import lam.project.foureventplannerdroid.utils.VolleyRequest;

public class CreateEventActivity extends AppCompatActivity {

    private String mEmail;

    private String mTitle;
    private String mTag;
    private String mAddress;
    private String mDescription;
    private String mStartDate;
    private String mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        mEmail = "spino9330@gmail.com";
    }

    public void createEvent(final View view) {

        getParams();

        String url = FourEventUri.Builder.create(FourEventUri.Keys.EVENT)
                .appendEncodedPath(mEmail).getUri();

        

        try {

            Event event = Event.Builder.create(mTitle,mDescription,mStartDate)
                    .withTag(mTag)
                    .withAddress(mAddress)
                    .withImage(mImage)
                    .build();

            CustomRequest createEventRequest = new CustomRequest(
                    Request.Method.PUT,
                    url,
                    event.toJson(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Snackbar.make(view,"Completato con successo!",Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            System.out.println(error.toString());
                        }
                    }

            );

            VolleyRequest.get(this).add(createEventRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getParams() {

        mTitle = ((TextView)(findViewById(R.id.event_title))).getText().toString();
        mTag = ((TextView)(findViewById(R.id.event_tag))).getText().toString();
        mAddress = ((TextView)(findViewById(R.id.event_address))).getText().toString();
        mDescription = ((TextView)(findViewById(R.id.event_description))).getText().toString();
        mStartDate = ((TextView)(findViewById(R.id.event_start_date))).getText().toString();
        mImage = ((TextView)(findViewById(R.id.event_image))).getText().toString();
    }

}

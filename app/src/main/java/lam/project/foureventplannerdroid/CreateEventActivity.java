package lam.project.foureventplannerdroid;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import fr.ganfra.materialspinner.MaterialSpinner;
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

    private static TextView endDate;
    private static TextView startDate;

    private MaterialSpinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        setTitle(R.string.create_event);

        startDate = (TextView) findViewById(R.id.event_start_date);
        endDate = (TextView) findViewById(R.id.event_end_date);

        startDate.addTextChangedListener(watcher);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getSupportFragmentManager(), "DatePicker");
            }
        });

        /*String[] ITEMS = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (MaterialSpinner) findViewById(R.id.event_tag);
        spinner.setAdapter(adapter);*/


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

    public static class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm+1, dd);
        }

        public void populateSetDate(int year, int month, int day) {
            if(startDate.getText().toString().equals("Data di inizio")) {
                startDate.setText("I - "+day+"/"+month+"/"+year);
                startDate.setTextColor(getResources().getColor(R.color.darkerText));
            }
            else {
                endDate.setText("F - "+day+"/"+month+"/"+year);
                endDate.setTextColor(getResources().getColor(R.color.darkerText));

            }
        }

    }

    private final TextWatcher watcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            endDate.setVisibility(View.VISIBLE);
            endDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFragment = new SelectDateFragment();
                    newFragment.show(getSupportFragmentManager(), "DatePicker");
                }
            });
        }
    };


}

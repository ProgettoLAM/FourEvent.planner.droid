package lam.project.foureventplannerdroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import lam.project.foureventplannerdroid.model.Category;
import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.utils.CustomRequest;
import lam.project.foureventplannerdroid.utils.FourEventUri;
import lam.project.foureventplannerdroid.utils.Utility;
import lam.project.foureventplannerdroid.utils.VolleyRequest;

public class CreateEventActivity extends AppCompatActivity implements View.OnClickListener,
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    private String mEmail;

    private String mTitle;
    private String mTag;
    private String mAddress;
    private String mDescription;
    private String mStartDate;

    private TextView startDate;
    private TextView startTime;
    private TextView endDate;
    private TextView endTime;
    private ImageView imgEvent;

    private String mImageUri;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        setTitle(R.string.create_event);

        //Per disabilitare autofocus all'apertura della Activity
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        startDate = (TextView) findViewById(R.id.event_start_date);
        startTime = (TextView) findViewById(R.id.event_start_time);
        endDate = (TextView) findViewById(R.id.event_end_date);
        endTime = (TextView) findViewById(R.id.event_end_time);

        imgEvent = (ImageView) findViewById(R.id.event_image);
        imgEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        startDate.addTextChangedListener(watcher);
        startTime.addTextChangedListener(watcher);
        endDate.addTextChangedListener(watcher);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();

            }
        });

        MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.event_tag);
        spinner.setItems(Category.Keys.categories);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {}
        });

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

        //TODO completare
        String uri = FourEventUri.Builder.create(FourEventUri.Keys.EVENT)
                .appendPath("img")
                .appendPath("img00.jpg")
                .getUri();

        Picasso.with(this).load(uri).resize(1200,600).into(imgEvent);

    }

    private final TextWatcher watcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        public void afterTextChanged(Editable s) {
            startTime.setVisibility(View.VISIBLE);
            startTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timePicker();
                }
            });
            if(!startTime.getText().toString().equals("Ora di inizio")) {
                endDate.setVisibility(View.VISIBLE);
                endDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        datePicker();
                    }
                });
            }
            if(!endDate.getText().toString().equals("Data di fine")) {
                endTime.setVisibility(View.VISIBLE);
                endTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timePicker();
                    }
                });
            }
        }
    };

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
        String minuteString = minute < 10 ? "0"+minute : ""+minute;
        String time = hourString+" : "+minuteString;
        if(startTime.getText().toString().equals("Ora di inizio")) {
            startTime.setText(time);
        }
        else
            endTime.setText(time);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = dayOfMonth+"/"+(++monthOfYear)+"/"+year;
        if(startDate.getText().toString().equals("Data di inizio")) {
            startDate.setText(date);
        }
        else
            endDate.setText(date);
    }

    @Override
    public void onClick(View v) {}

    private void datePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                CreateEventActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    private void timePicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                CreateEventActivity.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );

        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
            }
        });
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    private void selectImage() {

        final CharSequence[] items = { "Scatta una foto", "Scegli dalla galleria", "Annulla" };

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateEventActivity.this);
        builder.setTitle("Aggiungi un'immagine");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(CreateEventActivity.this);
                if (items[item].equals("Scatta una foto")) {
                    userChoosenTask = "Scatta una foto";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Scegli dalla galleria")) {
                    userChoosenTask = "Scegli dalla galleria";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Annulla")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //Codice per negare i permessi
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        imgEvent.setImageBitmap(bm);
    }

    private void onCaptureImageResult(Intent data) {

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        mImageUri = System.currentTimeMillis() + ".jpg";

        File destination = new File(Environment.getExternalStorageDirectory(),
                mImageUri);
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgEvent.setImageBitmap(thumbnail);
    }

}

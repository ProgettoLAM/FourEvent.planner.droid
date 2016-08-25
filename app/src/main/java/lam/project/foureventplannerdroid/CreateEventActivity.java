package lam.project.foureventplannerdroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jaredrummler.materialspinner.MaterialSpinner;
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
import java.util.List;
import java.util.Locale;

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
    private TextView addressEvent;

    private String mImageUri;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask;

    private GoogleApiClient mGoogleApiClient;

    final static String DIALOG_ERROR_TAG = "DIALOG_ERROR_TAG";
    private static final int REQUEST_RESOLVE_ERROR = 2;
    private static final String RESOLVING_ERROR_STATE_KEY = "RESOLVING_ERROR_STATE_KEY";

    private static final int REQUEST_ACCESS_LOCATION = 2;
    private static final int MAX_GEOCODER_RESULTS = 5;

    private boolean mResolvingError;

    public static Location mCurrentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        setTitle(R.string.create_event);

        //Per disabilitare autofocus all'apertura della Activity
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        if(savedInstanceState != null) {
            mResolvingError = savedInstanceState.getBoolean(RESOLVING_ERROR_STATE_KEY, false);
        }

        startDate = (TextView) findViewById(R.id.event_start_date);
        startTime = (TextView) findViewById(R.id.event_start_time);
        endDate = (TextView) findViewById(R.id.event_end_date);
        endTime = (TextView) findViewById(R.id.event_end_time);

        addressEvent = (TextView) findViewById(R.id.result_address);

        String address = getIntent().getStringExtra("Address");
        addressEvent.setText(address);

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

        //Creazione dell'oggetto nel quale si passano le informazioni relative ai servizi da inizializzare
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                //Interfaccia per ricevere notifiche sulla connessione ai Google Play Services
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    int CAUSE_SERVICE_DISCONNECTED = 1;
                    int CAUSE_NETWORK_LOST = 2;

                    @Override
                    public void onConnected(Bundle bundle) {

                        manageLocationPermission();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {}
                })

                //Interfaccia per gestire eventuali errori legati al ciclo di vita del GoogleApiClient
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {

                    //ConnectionResult: oggetto per accedere a strumenti per la risoluzione dei problemi
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        //Se è in corso una risoluzione dei problemi, si esce subito
                        if (mResolvingError) {

                            return;

                            //Se la connessione non è andata a buon fine, si controlla se ha una risoluzione
                        } else if (connectionResult.hasResolution()) {

                            try {

                                mResolvingError = true;

                                /*Si richiama un metodo nel quale è presente un Intent e si passa
                                  un requestCode per riconoscere la risposta che ritorna
                                 */
                                connectionResult.startResolutionForResult(CreateEventActivity.this,
                                        REQUEST_RESOLVE_ERROR);

                            } catch (IntentSender.SendIntentException e) {

                                mGoogleApiClient.connect();
                            }
                        } else {

                            mResolvingError = true;
                        }
                    }
                })
                .build();


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

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    //Permessi per scattare una foto/scegliere un'immagine dalla galleria e per accedere alla location
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

        if (requestCode == REQUEST_ACCESS_LOCATION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startLocationListener();
            } else {

                new AlertDialog.Builder(this)
                        .setTitle("Title")
                        .setMessage("Message")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                finish();
                            }
                        }).create().show();
            }
        }
    }

    /*Risultato della scelta dell'immagine in base al codice che ritorna:
      - se ritorna "SELECT_FILE" si richiama il metodo per la scelta dalla galleria
      - se ritorna "REQUEST_CAMERA" si richiama il metodo per la scelta dalla fotocamera
      E gestione del caso della risoluzione automatica dell'errore in caso di non avvenuta connessione
      ai LocationServices
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
        if (requestCode == REQUEST_RESOLVE_ERROR) {

            mResolvingError = false;

            if (resultCode == RESULT_OK) {

                //Si ritenta la connessione in caso il risultato abbia avuto successo
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {

                    mGoogleApiClient.connect();
                }
            }
        }
    }

    //Risultato dell'immagine scelta dalla galleria
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

    //Risultato dell'immagine scattata dalla fotocamera
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

    public void selectAddress(final View view) {
        Intent intent = new Intent(this, MapEventActivity.class);
        startActivity(intent);
    }

    //Si inizia la connessione ai servizi
    @Override
    protected void onStart() {
        super.onStart();
        //Per evitare cicli continui in caso di fase di risoluzione di un problema
        if (!mResolvingError) {

            mGoogleApiClient.connect();
        }
    }

    //Disconnessione ai servizi
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Per evitare che in caso di chiusura inaspettata dell'app, si perda il valore di mResolvingError
        outState.putBoolean(RESOLVING_ERROR_STATE_KEY, mResolvingError);

    }

    /*Nel caso in cui non si ha uno strumento per la risoluzione dei problemi, si richiama questa classe
          dove viene fatto visualizzare un Dialog di errore attraverso un Singleton
         */
    public static class ErrorDialogFragment extends DialogFragment {

        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int errorCode = this.getArguments().getInt(DIALOG_ERROR_TAG);

            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR
            );
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((CreateEventActivity) getActivity()).onDialogDismissed();
        }
    }

    private void onDialogDismissed() {}

    public void manageLocationPermission() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("title")
                        .setMessage("message")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(CreateEventActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_ACCESS_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_ACCESS_LOCATION);
            }

        } else if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("title")
                        .setMessage("message")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(CreateEventActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_ACCESS_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_LOCATION);
            }

        } else {
            //Acquisizione dell'informazione di location (posizione restituita solo attraverso Location)
            mCurrentLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            getGeocodeLocation(mCurrentLocation);
        }
    }

    //Richiesta di aggiornamento della Location
    public void startLocationListener() {
        updateLocation();
    }

    //Informazione di Location inoltrata una sola volta ed aggiornata esplicitamente dopo un'azione
    private void updateLocation() {

        //Richiesta di Location
        LocationRequest locationRequest = LocationRequest.create()
                //Ha impatto sulle risorse utilizzate dall'app (influenza consumo batteria)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                //Per ottenere una sola informazione di Location si passa 1
                .setNumUpdates(1)
                //Tempo oltre il quale la richiesta non ha più valore e viene rimossa
                .setExpirationDuration(500L);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //Ricezione e memorizzazione del risultato sulla location
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, new LocationListener() {
                    //Invocato quando la richiesta è disponibile (passata come parametro)
                    @Override
                    public void onLocationChanged(Location location) {

                        mCurrentLocation = location;

                        getGeocodeLocation(mCurrentLocation);
                    }
                });
    }

    //Geocoder per la ricezione della posizione dell'user
    public void getGeocodeLocation(final Location location) {

        if (location != null) {
            //Si controlla se il server del servizio di Geocoder è attivo
            if(Geocoder.isPresent()) {

                /*Accesso al servizio in un Thread separato da quello principale.
                  Tra i parametri si passa il numero di risultati che si vogliono ricevere (1)
                 */
                final GeoCoderAsyncTask geoCoderAsyncTask =
                        new GeoCoderAsyncTask(this,MAX_GEOCODER_RESULTS);

                geoCoderAsyncTask.execute(location);

            } else {
            }

        } else {
        }
    }

    //Specializzazione dell'AsyncTask del Geocoder
    public class GeoCoderAsyncTask extends AsyncTask<Location, Object, List<Address>> {

        private int mMaxResult;
        private CreateEventActivity mActivityRef;

        GeoCoderAsyncTask(CreateEventActivity mActivityRef, final int MAX_GEOCODE_RESULT){

            this.mActivityRef = mActivityRef;
            this.mMaxResult = MAX_GEOCODE_RESULT;
        }

        //List di Address è la classe che contiene i vari risultati del Geocoder
        @Override
        protected List<android.location.Address> doInBackground(Location... params) {

            CreateEventActivity activity = mActivityRef;

            if(activity == null) {

                return null;
            }

            //Creazione dell'istanza dove viene passato il context e la lingua di default
            final Geocoder geocoder = new Geocoder(activity, Locale.getDefault());

            final Location location = params[0];

            List<android.location.Address> geoAddress = null;

            try {

                //Restituisce una lista di Address, dove vengono passate le coordinate ed il risultato max
                geoAddress = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), mMaxResult);

            } catch (IOException e) {

                e.printStackTrace();
            }

            return geoAddress;
        }

        //Per la visualizzazione della lista di location (in questo caso 1) sulla UI
        @Override
        protected void onPostExecute(List<android.location.Address> addresses) {}
    }

}

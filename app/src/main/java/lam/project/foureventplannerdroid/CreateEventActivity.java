package lam.project.foureventplannerdroid;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
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
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lam.project.foureventplannerdroid.model.Category;
import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.utils.DateConverter;
import lam.project.foureventplannerdroid.utils.shared_preferences.ImageManager;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.Utility;
import lam.project.foureventplannerdroid.utils.connection.MultipartRequest;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;

import static lam.project.foureventplannerdroid.MainActivity.mCurrentPlanner;

public class CreateEventActivity extends AppCompatActivity implements View.OnClickListener,
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    private String mTitle;
    private String mTag = "MUSICA";
    private String mAddress;
    private String mDescription;

    private Date mStartDateTime;
    private Date mEndDateTime;

    private String mStartDate;
    private String mStartTime;
    private String mEndDate;
    private String mEndTime;

    private String mImageName;

    private String mImageUri;
    private int nTicket;
    private String progress;

    private TextView startDate;
    private TextView startTime;
    private TextView endDate;
    private TextView endTime;
    private ImageView imgEvent;
    private TextView addressEvent;
    private TextView price;


    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private int REQUEST_ADDRESS = 3;
    private String userChoosenTask;

    private GoogleApiClient mGoogleApiClient;

    final static String DIALOG_ERROR_TAG = "DIALOG_ERROR_TAG";
    private static final int REQUEST_RESOLVE_ERROR = 2;
    private static final String RESOLVING_ERROR_STATE_KEY = "RESOLVING_ERROR_STATE_KEY";

    private static final int REQUEST_ACCESS_LOCATION = 2;
    private static final int MAX_GEOCODER_RESULTS = 5;

    private boolean mResolvingError;

    public static Location mCurrentLocation;

    private ViewGroup view;

    //Region DateTimeListener

    private static Integer DATE_TIME_SENDER;

    //Listener della data e ora di inizio e di fine
    View.OnClickListener dateTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            int id = v.getId();

            switch (id){

                case R.id.event_start_date:

                    DATE_TIME_SENDER = R.id.event_start_date;
                    startDatePicker();
                    break;

                case R.id.event_start_time:

                    DATE_TIME_SENDER = R.id.event_start_time;
                    startTimePicker();
                    break;

                case R.id.event_end_date:

                    if(mStartDateTime != null) {

                        DATE_TIME_SENDER = R.id.event_end_date;
                        endDatePicker();
                    }
                    break;

                case R.id.event_end_time:

                    if(mStartDateTime != null) {

                        DATE_TIME_SENDER = R.id.event_end_time;
                        endTimePicker();
                    }
                    break;
            }
        }
    };

    //Endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_scrolling);

        if(savedInstanceState != null) {
            mResolvingError = savedInstanceState.getBoolean(RESOLVING_ERROR_STATE_KEY, false);
        }
        initView();

    }

    /**
     * Metodo per inizializzare gli elementi della creazione dell'evento
     */
    private void initView() {

        //Per disabilitare l'autofocus all'apertura della Activity
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        view = (ViewGroup) getWindow().getDecorView();

        //Creazione del number picker per prendere il numero massimo di biglietti
        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.np);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(30);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setOrientation(NumberPicker.HORIZONTAL);

        //Listener del number picker, nel quale si salva il valore in una variabile
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                nTicket = newVal;
            }
        });

        //Barra per scegliere il prezzo dell'evento
        SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
        price = (TextView) findViewById(R.id.price);

        //Listener del cambiamento della seekbar
        seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progressValue, boolean fromUser) {
                        //Salvataggio in una variabile del valore scelto
                        progress = String.valueOf(progressValue);
                        price.setText(progress + " €");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //Visualizzazione del numero su cui il planner si è fermato
                        price.setText(progress + " €");
                    }
                });


        startDate = (TextView) findViewById(R.id.event_start_date);
        startTime = (TextView) findViewById(R.id.event_start_time);
        endDate = (TextView) findViewById(R.id.event_end_date);
        endTime = (TextView) findViewById(R.id.event_end_time);

        addressEvent = (TextView) findViewById(R.id.result_address);

        imgEvent = (ImageView) findViewById(R.id.event_image);

        //Al click dell'immagine si sceglie una da caricare
        imgEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        startDate.setOnClickListener(dateTimeListener);
        startTime.setOnClickListener(dateTimeListener);

        endDate.setOnClickListener(dateTimeListener);
        endTime.setOnClickListener(dateTimeListener);

        //Spinner per scegliere la categoria e salvataggio in una variabile dell'item selezionato
        MaterialSpinner tagEvent = (MaterialSpinner) findViewById(R.id.event_tag);
        tagEvent.setItems(Category.Keys.categories);
        tagEvent.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                mTag = item;
            }
        });

        setGoogleServices();
    }

    /**
     * Espansione del layout che racchiude il numero di biglietti ed il prezzo
     * @param view view della Activity
     */
    public void expandablePrice(View view) {

        ExpandableRelativeLayout expandableLayout = (ExpandableRelativeLayout) findViewById(R.id.expandableLayout);
        expandableLayout.toggle();
        if(expandableLayout.isExpanded()) {
            ((Button) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ticket, 0,
                    R.drawable.ic_arrow_right, 0);
        }
        else {
            ((Button) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ticket, 0,
                    R.drawable.ic_arrow_down, 0);
        }
    }

    //Region creazione + upload dell'evento

    /**
     * Creazione dell'evento, prendendo i valori dei campi compilati
     * @param view della Activity
     */
    public void createEvent(final View view) {

        mTitle = ((TextView)(findViewById(R.id.event_title))).getText().toString();
        mAddress = addressEvent.getText().toString();
        mDescription = ((TextView)(findViewById(R.id.event_description))).getText().toString();
        mStartDate = startDate.getText().toString();
        mStartTime = startTime.getText().toString();
        mEndDate = endDate.getText().toString();
        mEndTime = endTime.getText().toString();

        //Si controlla se non siano stati compilati i campi obbligatori
        if(mTitle == null || mAddress == null || mDescription == null || mStartDateTime == null || mImageUri == null) {

                    Snackbar snackbar = Snackbar.make(view, "Compila i dati obbligatori!", Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(ContextCompat
                            .getColor(getApplicationContext(), R.color.lightRed));
                    snackbar.show();
        }
        //Altrimenti si salva l'evento
        else {
            saveEvent();
        }
    }

    /**
     * Salvataggio dell'evento
     */
    private void saveEvent() {

        //Creo l'url per la richiesta
        String url = FourEventUri.Builder.create(FourEventUri.Keys.EVENT).getUri();

        try {

            //Converto in millisecondi, sottoforma di stringa, la data iniziale
            String dateTimeStart = DateConverter.dateToMillis(mStartDateTime);

            //Creazione dell'evento con i dati inseriti
            Event event = Event.Builder.create(mTitle, mDescription, dateTimeStart,
                    mCurrentPlanner.email).withTag(mTag).withAddress(mAddress)
                    .withImage(mImageUri).withPrice(progress).build();

            //Se è presente la data di fine
            if(mEndDateTime != null) {
                String dateTimeEnd = DateConverter.dateToMillis(mEndDateTime);
                event.addEndDate(dateTimeEnd);
            }
            //Se il numero di tickets è maggiore di 0
            if(nTicket > 0) {
                event.addMaxTicket(nTicket);
            }

            //Creazione di un progress dialog nell'attesa
            final ProgressDialog progressDialog = ProgressDialog.show(this,null,"Salvataggio in corso, attendere",true,false);
            progressDialog.show();

            CustomRequest createEventRequest = new CustomRequest(
                    Request.Method.PUT, url, event.toJson(), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Snackbar.make(view, "Evento creato", Snackbar.LENGTH_SHORT).show();

                            finish();

                            progressDialog.dismiss();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            progressDialog.dismiss();
                        }
                    }

            );

            VolleyRequest.get(this).add(createEventRequest);

        } catch (JSONException e) { e.printStackTrace();}
    }

    //Endregion

    //Region picker date e time
    @Override
    public void onTimeSet(RadialPickerLayout view, int hour, int minute, int second) {

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.set(0,0,0,hour,minute);

        String time = DateConverter.timeFromCalendar(dateCalendar);

        //In base all'id passato, si setta l'ora di inizio o di fine
        switch (DATE_TIME_SENDER) {

            case R.id.event_start_time:

                mStartTime = time;
                startTime.setText(mStartTime);

                if(mStartTime != null)
                    mStartDateTime = DateConverter.dateFromString(mStartDate+" "+mStartTime);

                break;

            case R.id.event_end_time:

                mEndTime = time;
                endTime.setText(mEndTime);

                if(mEndDate != null)
                    mEndDateTime = DateConverter.dateFromString(mEndDate+" "+mEndTime);

            default:
                break;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int month, int day) {

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.set(year, month, day, 0, 0);

        String date = DateConverter.dateFromCalendar(dateCalendar);

        //In base all'id passato si setta la data di inizio o di fine
        switch (DATE_TIME_SENDER) {

            case R.id.event_start_date:

                mStartDate = date;
                startDate.setText(mStartDate);

                if(mStartTime != null)
                    mStartDateTime = DateConverter.dateFromString(mStartDate+" "+mStartTime);

                break;

            case R.id.event_end_date:

                mEndDate = date;
                endDate.setText(mEndDate);

                if(mEndTime != null)
                    mEndDateTime = DateConverter.dateFromString(mEndDate+" "+mEndTime);

            default:
                break;
        }
    }

    /**
     * Avvio del date picker della data di inizio
     */
    private void startDatePicker() {

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog startDateDialog = DatePickerDialog.newInstance(CreateEventActivity.this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        startDateDialog.setMinDate(calendar);

        startDateDialog.setThemeDark(true);
        startDateDialog.show(getFragmentManager(),"StartDatePickerDialog");
    }

    /**
     * Avvio della data di fine del date picker
     */
    private void endDatePicker() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mStartDateTime);

        DatePickerDialog endDateDialog = DatePickerDialog.newInstance(CreateEventActivity.this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        endDateDialog.setMinDate(calendar);

        endDateDialog.setThemeDark(true);
        endDateDialog.show(getFragmentManager(),"EndDatePickerDialog");
    }

    /**
     * Avvio del time picker dell'ora di inizio
     */
    private void startTimePicker() {

        Calendar calendar = Calendar.getInstance();
        TimePickerDialog startTimeDialog = TimePickerDialog.newInstance(
                CreateEventActivity.this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        if(mStartDate != null && mStartDate.equals(DateConverter.dateFromCalendar(calendar)))
            startTimeDialog.setMinTime(new Timepoint(calendar.get(Calendar.HOUR_OF_DAY)+1, calendar.get(Calendar.MINUTE)));

        startTimeDialog.setThemeDark(true);
        startTimeDialog.show(getFragmentManager(), "StartTimePickerDialog");


    }

    /**
     * Avvio del time picker dell'ora di fine
     */
    private void endTimePicker() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mStartDateTime);
        TimePickerDialog endTimeDialog = TimePickerDialog.newInstance(
                CreateEventActivity.this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        if(mEndDate != null && mEndDate.equals(mStartDate))
            endTimeDialog.setMinTime(new Timepoint(calendar.get(Calendar.HOUR_OF_DAY)+1, calendar.get(Calendar.MINUTE)));

        endTimeDialog.setThemeDark(true);
        endTimeDialog.show(getFragmentManager(),"EndTimePickerDialog");

    }

    @Override
    public void onClick(View v) {}

    //Endregion

    //Region selezione dell'immagine

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

    //Endregion

    //Region fetch/scatta immagine + upload sul server

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo")) cameraIntent();

                    else if(userChoosenTask.equals("Choose from Library")) galleryIntent();
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

    //Risultato della scelta dell'immagine in base al codice che ritorna:
    //se ritorna "SELECT_FILE" si richiama il metodo per la scelta dalla galleria
    //se ritorna "REQUEST_CAMERA" si richiama il metodo per la scelta dalla fotocamera
    //e gestione del caso della risoluzione automatica dell'errore in caso di non avvenuta connessione
    //ai LocationServices
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == SELECT_FILE){

                onSelectFromGalleryResult(data);

            } else if (requestCode == REQUEST_CAMERA) {

                onCaptureImageResult(data);
            }
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

        if(requestCode == REQUEST_ADDRESS) {

            if(resultCode == RESULT_OK) {

                String address = data.getStringExtra("Address");
                addressEvent.setText(address);
            }
        }
    }

    /**
     * Risultato dell'immagine scelta dalla galleria
     * @param data intent che deriva dal risultato della Activity
     */
    private void onSelectFromGalleryResult(Intent data) {

        mImageName = String.valueOf(new Date().getTime());

        try {
            Bitmap thumbnail = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            File createdImage = ImageManager.get().writeImage(mImageName,thumbnail);

            if(createdImage != null){
                imgEvent.setImageBitmap(thumbnail);
                uploadImage(createdImage);
            }

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Risultato dell'immagine scattata dalla fotocamera
     * @param data intent che deriva dal risultato della Activity
     */
    private void onCaptureImageResult(Intent data) {

        mImageName = String.valueOf(new Date().getTime());

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        File createdImage = ImageManager.get().writeImage(mImageName,thumbnail);

        if(createdImage != null){
            imgEvent.setImageBitmap(thumbnail);
            uploadImage(createdImage);
        }
    }

    /**
     * Caricamento dell'immagine sul server
     * @param toUploadFile File preso dalla galleria del dispositivo o scattato dalla fotocamera
     */
    private void uploadImage(File toUploadFile) {

        String url = FourEventUri.Builder.create(FourEventUri.Keys.EVENT)
                .appendPath("img").appendPath(mImageName).getUri();

        final ProgressDialog loading = ProgressDialog.show(this, "Immagine dell'evento", "Caricamento in corso..", false, false);

        MultipartRequest mMultipartRequest = new MultipartRequest(url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Snackbar errorSnackbar = Snackbar.make(view,
                        "Errore nel caricamento dell'immagine", Snackbar.LENGTH_LONG);

                errorSnackbar.getView().setBackgroundColor(ContextCompat
                        .getColor(getApplicationContext(), R.color.lightRed));

                errorSnackbar.show();

                loading.dismiss();


            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Snackbar successSnackbar = Snackbar.make(startDate, "Immagine caricata!",
                        Snackbar.LENGTH_SHORT);

                successSnackbar.getView().setBackgroundColor(ContextCompat
                        .getColor(getApplicationContext(), R.color.lightGreen));

                successSnackbar.show();

                mImageUri = response;
                loading.dismiss();

            }
        },toUploadFile,"filename");

        VolleyRequest.get(this).add(mMultipartRequest);
    }

    //Endregion

    //Region Google Maps API

    /**
     * Creazione dell'oggetto nel quale si passano le informazioni relative ai servizi da inizializzare
     */
    private void setGoogleServices() {

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
                        if (mResolvingError) { return;}

                        //Se la connessione non è andata a buon fine, si controlla se ha una risoluzione
                        else if (connectionResult.hasResolution()) {

                            try {

                                mResolvingError = true;

                                //Si richiama un metodo nel quale è presente un Intent e si passa
                                //un requestCode per riconoscere la risposta che ritorna
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
    }

    @Override
    protected void onStart() {

        super.onStart();

        //Per evitare cicli continui in caso di fase di risoluzione di un problema
        if (!mResolvingError) { mGoogleApiClient.connect();}
    }

    @Override
    protected void onStop() {

        //Disconnessione ai servizi
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Per evitare che in caso di chiusura inaspettata dell'app, si perda il valore di mResolvingError
        outState.putBoolean(RESOLVING_ERROR_STATE_KEY, mResolvingError);

    }

    /**
     * Nel caso in cui non si ha uno strumento per la risoluzione dei problemi, si richiama questa classe
     * dove viene fatto visualizzare un Dialog di errore attraverso un Singleton
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

    /**
     * Gestione dei permessi per accedere alla location
     */
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

    /**
     * Richiesta di aggiornamento della Location
     */
    public void startLocationListener() {
        updateLocation();
    }

    /**
     * Informazione di Location inoltrata una sola volta ed aggiornata esplicitamente dopo un'azione
     */
    private void updateLocation() {

        //Richiesta di Location
        LocationRequest locationRequest = LocationRequest.create()

                //Ha impatto sulle risorse utilizzate dall'app (influenza consumo batteria)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                //Per ottenere una sola informazione di Location si passa 1
                .setNumUpdates(1)

                //Tempo oltre il quale la richiesta non ha più valore e viene rimossa
                .setExpirationDuration(500L);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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

    /**
     * Geocoder per la ricezione della posizione dell'user
     * @param location posizione dell'user
     */
    public void getGeocodeLocation(final Location location) {

        if (location != null) {

            //Si controlla se il server del servizio di Geocoder è attivo
            if(Geocoder.isPresent()) {

                //Accesso al servizio in un Thread separato da quello principale.
                //Tra i parametri si passa il numero di risultati che si vogliono ricevere (1)
                final GeoCoderAsyncTask geoCoderAsyncTask =
                        new GeoCoderAsyncTask(this,MAX_GEOCODER_RESULTS);

                geoCoderAsyncTask.execute(location);

            }
        }
    }

    /**
     * Classe con la specializzazione del Geocoder asynctask
     */
    public class GeoCoderAsyncTask extends AsyncTask<Location, Object, List<Address>> {

        private int mMaxResult;
        private CreateEventActivity mActivityRef;

        GeoCoderAsyncTask(CreateEventActivity mActivityRef, final int MAX_GEOCODE_RESULT){

            this.mActivityRef = mActivityRef;
            this.mMaxResult = MAX_GEOCODE_RESULT;
        }

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

        @Override
        protected void onPostExecute(List<android.location.Address> addresses) {
            //Per la visualizzazione della lista di location (in questo caso 1) sulla UI
        }
    }

    /**
     * Prelevare indirizzo che proviene dalla MapActivity
     * @param view view della Activity
     */
    public void selectAddress(final View view) {
        Intent intent = new Intent(this, MapEventActivity.class);
        startActivityForResult(intent, REQUEST_ADDRESS);
    }

    //Endregion
}

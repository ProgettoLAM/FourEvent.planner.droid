package lam.project.foureventplannerdroid;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.model.Record;
import lam.project.foureventplannerdroid.utils.connection.HandlerManager;
import lam.project.foureventplannerdroid.utils.shared_preferences.PlannerManager;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;
import lam.project.foureventplannerdroid.utils.qr_code.ScannerActivity;

import static com.google.android.gms.wearable.DataMap.TAG;

public class EventDetailActivity extends Activity {

    private AlertDialog dialog;

    private Button mBtnPopular;

    private NfcAdapter mNfcAdapter;
    private boolean mIsSearching;
    private ProgressDialog mProgressDialog;

    private Button.OnClickListener listenerButton;
    private TextView detailsParticipation;
    private TextView pricePopular;
    private TextView detailCheckIn;

    private Event mCurrentEvent;

    private PieChart ageChart;
    private PieChart genderChart;
    private float[] yDataGender = new float[2];
    private String[] xDataGender = {"Maschi", "Femmine"};
    private float[] yDataAge = new float[3];
    private String[] xDataAge = {"16-24", "25-35", ">35"};

    public static String OPEN_FRAGMENT_WALLET = "Portafoglio";
    public static final String SEPARATOR = " / ";

    private ViewGroup mViewGroup;

    public static final String AGES = "ages";
    public static final String GENDER_STATS = "gender_stats";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        initView();
    }

    /**
     * Metodo per inizializzare gli elementi della view
     */
    private void initView() {

        mViewGroup = (ViewGroup) findViewById(R.id.event_detail_container);

        ageChart = (PieChart) findViewById(R.id.age_chart);
        genderChart = (PieChart) findViewById(R.id.gender_chart);

        detailsParticipation = (TextView) findViewById(R.id.details_ticket);
        pricePopular = (TextView) findViewById(R.id.price_popular);
        detailCheckIn = (TextView) findViewById(R.id.details_checkin);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mBtnPopular = (Button) findViewById(R.id.btn_popular);
        Button mButtonNFC = (Button) findViewById(R.id.button_nfc);

        //Al click del bottone per la sincronizzazione del NFC
        mButtonNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {nfcButton();
            }
        });

        //Se l'Nfc non è supportato, si deve utilizzare il codice QR
        if (mNfcAdapter == null) {
            Toast.makeText(this,"NFC non supportato, utilizzare codice QR",Toast.LENGTH_SHORT).show();
        }

        //Si salva in una variabile l'evento corrente cliccato dalla recycler view
        mCurrentEvent = getIntent().getParcelableExtra(Event.Keys.EVENT);
        enableDisablePopularButton();

        //Listener del numero di biglietti di cui si vuole incrementare
        listenerButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    String value = ((Button) v).getText().toString().split(" ")[0];
                    Float amount = Float.parseFloat(value);
                    String numParticipation = (String) v.getTag();

                    buyParticipation(amount, numParticipation);

                }
                catch (JSONException ex) {
                    Log.d("Error", ex+"");
                }
            }
        };

        getMoreDetails();

    }


    //region NFC methods

    private void nfcButton() {

        //Se l'Nfc non è attivo, si reindirizza l'utente alle impostazioni del dispositivo per attivarlo
        if(!mNfcAdapter.isEnabled()) {

            Toast.makeText(mViewGroup.getContext(),"Perfavore attiva l'NFC e torna indietro per tornare all'applicazione!",
                    Toast.LENGTH_LONG).show();

            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));

        //Altrimenti si inizia la sincronizzazione del tag Nfc
        } else if(mNfcAdapter.isEnabled()) {

            mIsSearching = true;
            mProgressDialog = ProgressDialog.show(mViewGroup.getContext(),"Ricerca braccialetto","Ricerca braccialetto NFC in corso...",true,true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        //Se si sta sincronizzando il tag, si prende e si invia nella lettura Ndef
        if(mIsSearching && intent.hasExtra(NfcAdapter.EXTRA_TAG)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new NdefReaderTask().execute(tag);

            mProgressDialog.dismiss();
            mIsSearching = false;
        }
    }

    private void disableForegroundDispatchSystem() {

        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void enableForegroundDispatchSystem() {

        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.setFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        IntentFilter[] intentFilters = new IntentFilter[]{};

        mNfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);
    }

    @Override
    protected void onResume() {

        //Importante che nel onResume l'Activity sia nel foreground, altrimenti lancia un'eccezione
        enableForegroundDispatchSystem();
        super.onResume();
    }

    @Override
    protected void onPause() {

        //Importante che sia prima del onPause, altrimenti lancia un'eccezione
        disableForegroundDispatchSystem();
        super.onPause();
    }

    //endregion

    //region NdefReaderTask + gestione risultato

    /**
     * Classe per la lettura Ndef del tag Nfc
     */
    public class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF non è supportato da questo tag
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {

                showResult(result);
            }
        }
    }

    /**
     * Risultato della lettura del tag NFC
     * @param text testo letto
     */
    public void showResult(String text) {

        //Creo l'url per la richiesta
        String url = FourEventUri.Builder.create(FourEventUri.Keys.TICKET)
                .appendPath("tag")
                .appendEncodedPath(text)
                .getUri();

        CustomRequest getTicketDetailRequest = new CustomRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            //Si inserisce in un array la risposta
                            JSONArray array = response.getJSONArray("user_checked");


                            String checked = String.valueOf(array.length());
                            if(!mCurrentEvent.isFree()) {

                                checked += SEPARATOR + mCurrentEvent.mMaxTicket;
                            }
                            detailCheckIn.setText(checked);

                            Snackbar.make(mViewGroup,"Cliente abilitato all'entrata",Snackbar.LENGTH_LONG).show();

                        } catch (JSONException e) { e.printStackTrace();}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Snackbar errorSnackbar = Snackbar.make(mViewGroup,"Errore",Snackbar.LENGTH_LONG);
                        errorSnackbar.show();
                    }
                });

        VolleyRequest.get(this).add(getTicketDetailRequest);

    }

    //endregion

    /**
     * Click del bottone del QR per avviare la fotocamera e scannerizzare il codice dell'user
     * @param view dell'Activity
     */
    public void qrButton(final View view) {

        Intent intent = new Intent(this, ScannerActivity.class);
        startActivity(intent);
    }

    /**
     * Metodo per aggiungere i dati ai due pieChart
     * @param yData dati dei vari componenti
     * @param xData label dei componenti
     * @param mChart nome del chart
     */
    private void addData(float[] yData, String[] xData, PieChart mChart) {

        //Disattivare la rotazione al touch
        mChart.setRotationAngle(0);
        mChart.setRotationEnabled(false);

        //Configurazione pieChart per il gender
        mChart.setUsePercentValues(true);
        mChart.setDescription(null);

        //Attivare l'hole e configurarlo
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(R.color.white);
        mChart.setHoleRadius(7);
        mChart.setTransparentCircleRadius(10);
        mChart.setDrawSliceText(false);

        //Personalizzazione della legenda
        Legend l = genderChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        l.setXEntrySpace(10);
        l.setYEntrySpace(2);

        ArrayList<Entry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        for(int i = 0; i < yData.length; i++)
            yVals.add(new Entry(yData[i], i));

        for(int i = 0; i < xData.length; i++)
            xVals.add(xData[i]);

        //Creazione del pie dataset
        PieDataSet dataSet = new PieDataSet(yVals, null);
        dataSet.setSliceSpace(0);
        dataSet.setSelectionShift(5);

        //Aggiungere i colori al chart

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        //Istanziare l'oggetto PieData
        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);
        data.setValueTextColor(R.color.white);

        //Si aggiunge l'oggetto data al chart
        mChart.setData(data);

        //Undo tutti gli highlights
        mChart.highlightValues(null);

        //Update piechart
        mChart.invalidate();

    }

    private void getMoreDetails() {

        String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                .appendPath("detail")
                .appendEncodedPath(mCurrentEvent.mId)
                .getUri();

        CustomRequest getEventDetails = new CustomRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        refreshView(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Snackbar.make(mViewGroup,error.toString(),Snackbar.LENGTH_LONG).show();
                    }
                });

        VolleyRequest.get(this).add(getEventDetails);
    }

    private void refreshView(JSONObject response) {

        try{

            Event event = Event.fromJson(response);
            event.mParticipation = response.getJSONArray(Event.Keys.PARTICIPATION).length();

            int checkedUsers = 0;
            if(response.has(Event.Keys.CHECKED))
                checkedUsers = response.getJSONArray(Event.Keys.CHECKED).length();

            String checked = String.valueOf(checkedUsers);
            String participation = String.valueOf(event.mParticipation);

            if(!event.isFree()) {

                final String separator = " / ";

                checked += separator + event.mMaxTicket;
                participation += separator + event.mMaxTicket;

                findViewById(R.id.btn_more_ticket).setVisibility(View.VISIBLE);

            }

            enableDisablePopularButton();

            detailsParticipation.setText(participation);
            detailCheckIn.setText(checked);

            setChartsByResponse(response);

        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

    private void enableDisablePopularButton() {

        if(mCurrentEvent.isPopular()) {

            mBtnPopular.setEnabled(false);
            mBtnPopular.setAlpha(.5f);

        } else {

            mBtnPopular.setEnabled(true);
            mBtnPopular.setAlpha(1);
        }
    }

    private void setChartsByResponse(JSONObject response) throws JSONException {

        JSONArray jsonGenders = response.getJSONArray(GENDER_STATS);

        for(int i=0; i<jsonGenders.length(); i++) {

            JSONObject gender = jsonGenders.getJSONObject(i);

            String id = gender.getString("_id");

            float count = gender.getInt("count");
            if(id.equals("M"))
                yDataGender[0] = count*10;

            else
                yDataGender[1] = count*10;
        }
        addData(yDataGender, xDataGender, genderChart);

        JSONArray jsonAges = response.getJSONArray(AGES);

        //{"16-24", "25-35", ">35"}
        int age;
        for(int i=0; i<jsonAges.length(); i++){

            age = jsonAges.getInt(i);

            if(16<=age && age<=24)
                yDataAge[0] ++;

            else if(25<=age && age<=35)
                yDataAge[1] ++;

            else if(age > 35)
                yDataAge[2] ++;
        }

        yDataAge[0] = yDataAge[0]*10;
        yDataAge[1] = yDataAge[1]*10;
        yDataAge[2] = yDataAge[2]*10;

        addData(yDataAge,xDataAge,ageChart);
    }

    //region premium task

    /**
     * Metodo per incrementare il numero di biglietti massimi
     * @param view view dell'Activity
     */
    public void moreTickets(final View view) {

        //Si crea un dialog tramite il Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Aumenta il numero dei biglietti");

        View viewInflated = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_tickets, mViewGroup, false);

        viewInflated.findViewById(R.id.btn_1_ticket).setOnClickListener(listenerButton);
        viewInflated.findViewById(R.id.btn_2_ticket).setOnClickListener(listenerButton);
        viewInflated.findViewById(R.id.btn_3_ticket).setOnClickListener(listenerButton);
        viewInflated.findViewById(R.id.btn_4_ticket).setOnClickListener(listenerButton);

        builder.setView(viewInflated);

        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog = builder.show();
    }

    /**
     * Metodo per inserire l'evento tra i popolari
     * @param view view della Activity
     */
    public void popularEvent(final View view) {

        String message;
        String title;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DialogInterface.OnClickListener positiveListener;
        String positiveListenerText;
        final int price = Integer.parseInt(pricePopular.getText().toString());

        //Si controlla che il prezzo sia minore della somma del portafoglio del planner e che
        //l'evento non sia già tra i popolari
        if (!mCurrentEvent.isPopular() && price <= MainActivity.mCurrentPlanner.balance) {

            message = "Pubblicizzare l'evento ha un costo di " + price + "€." +
                    "\n\nHai un totale di " + MainActivity.mCurrentPlanner.balance + " €.\nVuoi pubblicizzarlo?";

            title = "Inserisci l'evento tra i popolari";

            positiveListenerText = "Acquista";
            positiveListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {

                    String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                            .appendPath("popular")
                            .appendEncodedPath(MainActivity.mCurrentPlanner.email).getUri();

                    try {

                        JSONObject record = Record.Builder
                                .create(-price, Record.Keys.SPONSOR +": "+ mCurrentEvent.mTitle, MainActivity.mCurrentPlanner.email)
                                .withEvent(mCurrentEvent.mId)
                                .build().toJson();

                        CustomRequest createRecordRequest = new CustomRequest(Request.Method.POST,
                                url, record,
                                new Response.Listener<JSONObject>() {

                                    @Override
                                    public void onResponse(JSONObject response) {

                                        Snackbar snackbar = Snackbar.make(detailsParticipation, "Evento inserito tra i popolari!",
                                                Snackbar.LENGTH_LONG);

                                        //Si setta l'evento a popolare
                                        mCurrentEvent.updateIsPopular();

                                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.lightGreen));
                                        snackbar.show();
                                    }
                                },

                                new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                        Snackbar snackbarError = Snackbar.make(detailsParticipation, HandlerManager.handleError(error),
                                                Snackbar.LENGTH_LONG);

                                        snackbarError.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.lightRed));
                                        snackbarError.show();
                                            }
                        });

                        VolleyRequest.get(view.getContext()).add(createRecordRequest);

                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            };

        //Se l'evento è già tra i popolari
        } else if(mCurrentEvent.isPopular()) {

            title = "Avviso";
            message = "L'evento è già tra i popolari!";

            positiveListenerText = null;
            positiveListener = null;
        }
        //Se non si ha abbastanza credito per acquistarlo
        else {
            title = "Credito insufficiente";
            message = "Non hai abbastanza crediti per pubblicizzare l'evento, ricarica il portafoglio!!";

            positiveListenerText = "Ricarica portafoglio";
            positiveListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent openFragmentBIntent = new Intent(getApplicationContext(), MainActivity.class);
                    openFragmentBIntent.putExtra(OPEN_FRAGMENT_WALLET, "Portafoglio");
                    startActivity(openFragmentBIntent);
                }
            };
        }

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("Cancella", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.setPositiveButton(positiveListenerText, positiveListener);

        builder.show();
    }

    /**
     * Metodo per l'invio di un messaggio ai partecipanti
     * @param view della Activity
     */
    public void messageParticipation(final View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View viewInflated = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_message, mViewGroup, false);

        builder.setView(viewInflated);

        builder.setPositiveButton("Invia", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        builder.show();

    }

    /**
     * Metodo per l'acquisto di un numero maggiore di biglietti dell'evento
     * @param amount importo da pagare
     * @param numParticipation numero dei partecipanti
     * @throws JSONException
     */
    private void buyParticipation(final Float amount, final String numParticipation) throws JSONException {

        //Si salva la somma posseduta dal planner
        final float balance = MainActivity.mCurrentPlanner.balance;
        String maxTicket = "newMax";
        dialog.dismiss();

        //Se il prezzo del numero di biglietti è minore della somma del planner
        if(amount < balance) {

            //Creo un progress dialog nell'attesa
            final ProgressDialog progressDialog = new ProgressDialog(this);

            progressDialog.setMessage("Aumento dei biglietti in corso...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            //Creo l'url per la richiesta
            String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER).appendPath("maxticket")
                    .appendEncodedPath(MainActivity.mCurrentPlanner.email).getUri();

            try {
                //Creo il record scrivendo il prezzo, il titolo, l'email del planner e id dell'evento
                JSONObject record = Record.Builder
                        .create(-amount, Record.Keys.BUY_TICKETS+ ": "+mCurrentEvent.mTitle,
                                MainActivity.mCurrentPlanner.email)
                        .withEvent(mCurrentEvent.mId)
                        .build().toJson();

                //Inserisco il numero massimo di biglietti nel record
                record.put(maxTicket, numParticipation);

                CustomRequest createRecordRequest = new CustomRequest(Request.Method.POST,
                        url, record,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {

                                handleResponse(response, numParticipation, amount);

                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Snackbar snackbarError = Snackbar.make(mViewGroup, HandlerManager.handleError(error),
                                Snackbar.LENGTH_LONG);

                        snackbarError.getView().setBackgroundColor(ContextCompat
                                .getColor(getApplicationContext(), R.color.lightRed));
                        snackbarError.show();
                    }
                });

                VolleyRequest.get().add(createRecordRequest);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    //endregion

    //region handle response + error

    /**
     * Gestione della risposta dell'acquisto dei biglietti
     * @param response risposta dal server
     * @param numParticipation numero di biglietti scelti
     * @param amount somma del portafoglio del planner
     */
    private void handleResponse(JSONObject response, String numParticipation, final float amount) {

        try {
            //Update dell'importo del portafoglio del planner
            MainActivity.mCurrentPlanner.updateBalance(amount);

            //Si salva il planner con la somma aggiornata
            PlannerManager.get().save(MainActivity.mCurrentPlanner);

            dialog.dismiss();
            //Si setta il nuovo numero massimo di partecipanti
            detailsParticipation.setText(" / " + numParticipation);

            Snackbar responseSnackBar = Snackbar.make(mViewGroup,
                    response.getString("message"), Snackbar.LENGTH_LONG);

            responseSnackBar.getView().setBackgroundColor(ContextCompat
                    .getColor(getApplicationContext(), R.color.lightGreen));

            responseSnackBar.show();

        } catch (JSONException e) {

            e.printStackTrace();
            dialog.dismiss();
        }

    }


    //endregion
}
package lam.project.foureventplannerdroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.model.Record;
import lam.project.foureventplannerdroid.utils.PlannerManager;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.HandlerManager;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;
import lam.project.foureventplannerdroid.utils.qr_code.ScannerActivity;

public class EventDetailActivity extends Activity {

    private AlertDialog dialog;

    //Creating a broadcast receiver for gcm registration
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private Button.OnClickListener listenerButton;
    private TextView detailsParticipation;
    private TextView pricePopular;
    private TextView priceMessage;

    private Event mCurrentEvent;

    private PieChart ageChart;
    private PieChart genderChart;
    private float[] yDataGender = {30,70};
    private String[] xDataGender = {"Maschi", "Femmine"};
    private float[] yDataAge = {50, 20, 30};
    private String[] xDataAge = {"16-24", "25-35", ">35"};

    public static String OPEN_FRAGMENT_WALLET = "Portafoglio";

    private ViewGroup v;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        v = (ViewGroup) getWindow().getDecorView();

        ageChart = (PieChart) findViewById(R.id.age_chart);
        genderChart = (PieChart) findViewById(R.id.gender_chart);

        detailsParticipation = (TextView) findViewById(R.id.details_ticket);
        pricePopular = (TextView) findViewById(R.id.price_popular);
        priceMessage = (TextView) findViewById(R.id.price_message);

        addData(yDataGender, xDataGender, genderChart);
        addData(yDataAge, xDataAge, ageChart);

        mCurrentEvent = getIntent().getParcelableExtra(Event.Keys.EVENT);

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

    }

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

       /* for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);*/

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

       /* for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);*/

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        //Instanziare l'oggetto PieData
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

    public void moreTickets(final View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Aumenta il numero dei biglietti");

        View viewInflated = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_tickets, v, false);

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

    public void qrButton(final View view) {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivity(intent);
    }

    public void popularEvent(final View view) {

        String message;
        String title;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DialogInterface.OnClickListener positiveListener;
        String positiveListenerText;
        final int price = Integer.parseInt(pricePopular.getText().toString());

        if (price <= MainActivity.mCurrentPlanner.balance) {

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

                                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.lightGreen));
                                        snackbar.show();

                                    }
                                },

                                new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                        Snackbar snackbarError = Snackbar.make(detailsParticipation, HandlerManager.getInstance().handleError(error),
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

        } else {

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

    public void messageParticipation(final View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View viewInflated = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_message, v, false);

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

    private void buyParticipation(final Float amount, final String numParticipation) throws JSONException {

        final float balance = MainActivity.mCurrentPlanner.balance;
        String maxTicket = "newMax";
        dialog.dismiss();

        if(amount < balance) {

            final ProgressDialog progressDialog = new ProgressDialog(this);

            progressDialog.setMessage("Aumento dei biglietti in corso...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);


            String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER).appendPath("maxticket")
                    .appendEncodedPath(MainActivity.mCurrentPlanner.email).getUri();

            try {

                JSONObject record = Record.Builder
                        .create(-amount, Record.Keys.BUY_TICKETS+ ": "+mCurrentEvent.mTitle, MainActivity.mCurrentPlanner.email)
                        .withEvent(mCurrentEvent.mId)
                        .build().toJson();

                record.put(maxTicket,300);

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

                        Snackbar snackbarError = Snackbar.make(v, HandlerManager.getInstance().handleError(error),
                                Snackbar.LENGTH_LONG);

                        snackbarError.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.lightRed));
                        snackbarError.show();
                    }
                });

                VolleyRequest.get().add(createRecordRequest);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    //region handle response + error


    private void handleResponse(JSONObject response, String numParticipation, final float amount) {

        try {

            //TODO modificare in questo modo anche wallet per le ricariche
            //Record insertedRecord = Record.fromJson(response.getJSONObject(Record.Keys.RECORD));
            MainActivity.mCurrentPlanner.updateBalance(amount);

            PlannerManager.get().save(MainActivity.mCurrentPlanner);

            dialog.dismiss();

            detailsParticipation.setText("10 /" + numParticipation);

            Snackbar responseSnackBar = Snackbar.make(v,
                    response.getString("message"), Snackbar.LENGTH_LONG);

            responseSnackBar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.lightGreen));

            responseSnackBar.show();

        } catch (JSONException e) {

            e.printStackTrace();
            dialog.dismiss();
        }

    }

}


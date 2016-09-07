package lam.project.foureventplannerdroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import lam.project.foureventplannerdroid.complete_profile.StepManager;
import lam.project.foureventplannerdroid.model.Planner;
import lam.project.foureventplannerdroid.utils.shared_preferences.PlannerManager;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;


public class LoginActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;

    private String email;
    private String password;

    private ImageView ic_warning_email;
    private ImageView ic_warning_password;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        //Activity con display intero
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        emailField = (EditText) findViewById(R.id.email_log);
        passwordField = (EditText) findViewById(R.id.pass_log);

        emailField.addTextChangedListener(watcher);
        passwordField.addTextChangedListener(watcher);

        ic_warning_email = (ImageView) findViewById(R.id.ic_alert_email);
        ic_warning_password = (ImageView) findViewById(R.id.ic_alert_pass);

    }

    /**
     * Login dell'utente
     * @param view view dell'Activity
     */
    public void login(final View view) {

        //Se i campi della view sono stati compilati
        if(controlUser()) {

            //Creazione di un progress dialog per l'attesa
            progressDialog = new ProgressDialog(this);

            progressDialog.setMessage("Login in corso...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            progressDialog.show();

            email = emailField.getText().toString();
            password = passwordField.getText().toString();

            try {

                //Creo l'url per la richiesta
                String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                        .appendPath("authenticate").getUri();

                //Invio al server un JsonObject con email e password dell'utente per il confronto
                final JSONObject user = new JSONObject("{\"email\": \""+email+"\", \"password\" : \""+password+"\"}");

                CustomRequest request = new CustomRequest(Request.Method.POST, url, user,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {

                                    //Se l'utente è verificato si rimuove il suo id e si inserisce
                                    // nel modello dell'utente
                                    String email = response.getString("_id");
                                    response.remove("_id");
                                    response.put(Planner.Keys.EMAIL,email);

                                    next(Planner.fromJson(response));
                                }
                                catch (JSONException ex ) { ex.printStackTrace();}

                                progressDialog.dismiss();

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Snackbar snackbarError = Snackbar.make(view, "Email / password sbagliata",
                                Snackbar.LENGTH_LONG);

                        snackbarError.getView().setBackgroundColor(ContextCompat
                                .getColor(getApplicationContext(), R.color.lightRed));
                        snackbarError.show();

                        progressDialog.dismiss();
                    }
                });

                VolleyRequest.get(this).add(request);
            }
            catch (JSONException ex) { ex.printStackTrace();}
        }
    }

    /**
     * Si prosegue per la MainActivity
     * @param planner utente corrente
     */
    private void next(Planner planner){

        PlannerManager.get(this).save(planner);

        //Non visualizzo lo stepper
        StepManager.get(this).setStep(StepManager.COMPLETE);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //region controllo dei campi

    /**
     * Listener della scrittura in un campo di testo
     */
    private final TextWatcher watcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        public void afterTextChanged(Editable s) {

            //Se la lunghezza del campo della email e/o password è diversa da 0,
            // l'icona di warning non viene più visualizzata
            if (emailField.getText().toString().length() != 0) {
                ic_warning_email.setVisibility(View.INVISIBLE);
            }
            if(passwordField.getText().toString().length() != 0){
                ic_warning_password.setVisibility(View.INVISIBLE);
            }
        }
    };

    /**
     * Controllo dell'user per il login
     * @return un booleano, se i campi non sono vuoti ritorna true
     */
    public boolean controlUser() {

        email = emailField.getText().toString();
        password = passwordField.getText().toString();

        if(email.equals("")) {
            ic_warning_email.setVisibility(View.VISIBLE);
            return false;
        }
        if(password.equals("")) {
            ic_warning_password.setVisibility(View.VISIBLE);
            return false;
        }

        else
            return true;
    }

    //endregion

}

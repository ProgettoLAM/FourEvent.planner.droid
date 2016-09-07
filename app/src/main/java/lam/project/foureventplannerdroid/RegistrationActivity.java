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
import android.widget.TextView;

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


public class RegistrationActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;
    private EditText passwordField2;

    private ImageView ic_close;
    private ImageView ic_check;
    private ImageView ic_warning_email;
    private ImageView ic_warning_password;

    private TextView min_char_pass;

    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_registration);

        initView();
    }

    /**
     * Metodo per inizializzare i campi per la registrazione
     */
    private void initView() {

        //Activity su display intero
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        emailField = (EditText) findViewById(R.id.email_reg);
        passwordField = (EditText) findViewById(R.id.pass_reg);
        passwordField2 = (EditText) findViewById(R.id.pass2_reg);
        min_char_pass = (TextView) findViewById(R.id.min_char_pass);

        ic_close = (ImageView) findViewById(R.id.ic_close_reg);
        ic_check = (ImageView) findViewById(R.id.ic_check_reg);
        ic_warning_email = (ImageView) findViewById(R.id.ic_alert_email);
        ic_warning_password = (ImageView) findViewById(R.id.ic_alert_pass);

        //Aggiungo i listener all'evento change text
        emailField.addTextChangedListener(watcher);
        passwordField.addTextChangedListener(watcher);
        passwordField2.addTextChangedListener(watcher);

    }

    /**
     * Click del bottone di registrazione
     * @param view view della registrazione
     */
    public void register(final View view){

        //Se non sono presenti campi vuoti
        if(controlUser()) {

            //Creo e mostro il progress dialog nell'attesa
            final ProgressDialog progressDialog = new ProgressDialog(this);

            progressDialog.setMessage("Registrazione in corso...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            progressDialog.show();

            //Creo il planner con l'email
            final Planner planner = Planner.Builder.create(email).build();

            try {

                //Inizializzo l'oggetto JSON per completare la richiesta
                JSONObject plannerJson = new JSONObject("{'email':'"+planner.email+"','password':'"+password+"'}");

                //Creo l'url della richiesta
                String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                        .appendPath("register").getUri();

                CustomRequest request = new CustomRequest(Request.Method.PUT, url, plannerJson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            progressDialog.dismiss();

                            next(planner);

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Snackbar snackbarError = Snackbar
                                    .make(view, error.toString(), Snackbar.LENGTH_LONG);

                            snackbarError.getView().setBackgroundColor(ContextCompat
                                    .getColor(getApplicationContext(), R.color.lightRed));
                            snackbarError.show();

                            progressDialog.dismiss();

                        }
                    });

                VolleyRequest.get(this).add(request);

            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
    }

    //region nuova activity

    public void goToLogin(final View view) {

        //Reindirizzamento al login quando si clicca il pulsante corrispondente
        final Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Prosegue nel completamento del profilo
     * @param planner utente appena creato
     */
    private void next(Planner planner){

        PlannerManager.get(this).save(planner);
        StepManager.get(this).setStep(StepManager.INCOMPLETE);

        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra(Planner.Keys.USER, planner);

        startActivity(intent);
        finish();
    }

    //Endregion

    //Region controllo dei campi

    /**
     * Listener della scrittura in un campo di testo
     */
    private final TextWatcher watcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {

            //Se le due password sono uguali e non vuote, compare l'icona di check
            if(passwordField.getText().toString().equals(passwordField2.getText().toString())
                    && !passwordField.getText().toString().equals("")){

                ic_check.setVisibility(View.VISIBLE);
                ic_close.setVisibility(View.INVISIBLE);

            }
            //Altrimenti compare l'icona di password errata
            else if(!passwordField.getText().toString().equals(passwordField2.getText().toString())){
                ic_close.setVisibility(View.VISIBLE);
                ic_check.setVisibility(View.INVISIBLE);

            }
        }

        public void afterTextChanged(Editable s) {

            //Si controlla la lunghezza del campo dell'email e le password
            if (emailField.getText().toString().length() != 0) {

                ic_warning_email.setVisibility(View.INVISIBLE);
            }
            if(passwordField.getText().toString().length() != 0){

                ic_warning_password.setVisibility(View.INVISIBLE);
            }
            if(passwordField.getText().toString().length() != 0
                    && passwordField.getText().toString().length() < 8){

                min_char_pass.setVisibility(View.VISIBLE);
            }
            if(passwordField.getText().toString().length() >= 8){

                min_char_pass.setVisibility(View.INVISIBLE);
            }
        }
    };

    /**
     * Controllo dell'user per la registrazione
     * @return un booleano, se i campi non sono vuoti ritorna true
     */
    public boolean controlUser() {

        email = emailField.getText().toString();
        password = passwordField.getText().toString();
        String password2 = passwordField2.getText().toString();

        //Se l'email e/o password sono vuote compare un warning
        if(email.equals("")) {
            ic_warning_email.setVisibility(View.VISIBLE);
            return false;
        }
        if(password.equals("")) {
            ic_warning_password.setVisibility(View.VISIBLE);
            return false;
        }
        return !(password2.equals("") || !password.equals(password2));
    }

    //endregion
}

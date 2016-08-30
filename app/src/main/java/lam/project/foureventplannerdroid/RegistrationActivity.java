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
import lam.project.foureventplannerdroid.utils.PlannerManager;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;


/**
 * Created by Vale on 30/07/2016.
 */

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
    private String password2;

    private final static String TAG = "RegistrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_registration);

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

        emailField.addTextChangedListener(watcher);
        passwordField.addTextChangedListener(watcher);
        passwordField2.addTextChangedListener(watcher);
    }

    public boolean controlUser() {
        email = emailField.getText().toString();
        password = passwordField.getText().toString();
        password2 = passwordField2.getText().toString();

        if(email.equals("")) {
            ic_warning_email.setVisibility(View.VISIBLE);
            return false;
        }
        if(password.equals("")) {
            ic_warning_password.setVisibility(View.VISIBLE);
            return false;
        }
        if(password2.equals("") || !password.equals(password2)) {
            return false;
        }
        else
            return true;
    }



    public void register(final View view){

        if(controlUser()) {


            final ProgressDialog progressDialog = new ProgressDialog(this);

            progressDialog.setMessage("Registrazione in corso...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            progressDialog.show();

            final Planner planner = Planner.Builder.create(email).build();

            try {

                JSONObject plannerJson = new JSONObject("{'email':'"+planner.email+"','password':'"+password+"'}");

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

                            View snackbarView = snackbarError.getView();

                            snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.lightRed));

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

    public void goToLogin(final View view) {

        final Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void next(Planner planner){

        PlannerManager.get(this).save(planner);
        StepManager.get(this).setStep(StepManager.INCOMPLETE);

        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra(Planner.Keys.USER, planner);

        startActivity(intent);
        finish();
    }

    private final TextWatcher watcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(passwordField.getText().toString().equals(passwordField2.getText().toString()) && !passwordField.getText().toString().equals("")){
                ic_check.setVisibility(View.VISIBLE);
                ic_close.setVisibility(View.INVISIBLE);

            }
            else if(!passwordField.getText().toString().equals(passwordField2.getText().toString())){
                ic_close.setVisibility(View.VISIBLE);
                ic_check.setVisibility(View.INVISIBLE);

            }
        }

        public void afterTextChanged(Editable s) {
            if (emailField.getText().toString().length() != 0) {
                ic_warning_email.setVisibility(View.INVISIBLE);
            }
            if(passwordField.getText().toString().length() != 0){
                ic_warning_password.setVisibility(View.INVISIBLE);
            }
            if(passwordField.getText().toString().length() != 0 && passwordField.getText().toString().length() < 8){
                min_char_pass.setVisibility(View.VISIBLE);
            }
            if(passwordField.getText().toString().length() >= 8){
                min_char_pass.setVisibility(View.INVISIBLE);
            }
        }
    };

}

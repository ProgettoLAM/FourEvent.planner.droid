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


/**
 * Created by Vale on 31/07/2016.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;

    private String email;
    private String password;

    private ImageView ic_warning_email;
    private ImageView ic_warning_password;

    private final static String TAG = ".LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        emailField = (EditText) findViewById(R.id.email_log);
        passwordField = (EditText) findViewById(R.id.pass_log);

        emailField.addTextChangedListener(watcher);
        passwordField.addTextChangedListener(watcher);

        ic_warning_email = (ImageView) findViewById(R.id.ic_alert_email);
        ic_warning_password = (ImageView) findViewById(R.id.ic_alert_pass);

    }

    public void goToRegistration(final View view) {

        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);

    }

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

    public void login(final View view) {
        if(controlUser()) {

            final ProgressDialog progressDialog = new ProgressDialog(this);

            progressDialog.setMessage("Login in corso...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            progressDialog.show();

                email = emailField.getText().toString();
                password = passwordField.getText().toString();

            try {

                String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                        .appendPath("authenticate").getUri();

                final JSONObject user = new JSONObject("{\"email\": \""+email+"\", \"password\" : \""+password+"\"}");

                CustomRequest request = new CustomRequest(Request.Method.POST, url, user,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {

                                    String email = response.getString("_id");

                                    response.remove("_id");
                                    response.put(Planner.Keys.EMAIL,email);

                                    next(Planner.fromJson(response));
                                }
                                catch (JSONException ex ) {
                                    ex.printStackTrace();
                                }

                                progressDialog.dismiss();

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                        Snackbar snackbarError = Snackbar.make(view, "Email / password sbagliata",
                                Snackbar.LENGTH_LONG);

                        View snackbarView = snackbarError.getView();

                        snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.lightRed));

                        snackbarError.show();

                        progressDialog.dismiss();
                    }
                });

                VolleyRequest.get(this).add(request);
            }
            catch (JSONException ex) {

            }
        }
    }

    private void next(Planner planner){

        PlannerManager.get(this).save(planner);

        //non visualizzo lo stepper
        StepManager.get(this).setStep(StepManager.COMPLETE);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private final TextWatcher watcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            if (emailField.getText().toString().length() != 0) {
                ic_warning_email.setVisibility(View.INVISIBLE);
            }
            if(passwordField.getText().toString().length() != 0){
                ic_warning_password.setVisibility(View.INVISIBLE);
            }
        }
    };

}

package lam.project.foureventplannerdroid.fragment;



import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Planner;
import lam.project.foureventplannerdroid.utils.PlannerManager;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentProfile extends Fragment {

    String oldPassword;
    String newPassword;
    Snackbar snackbar;

    public FragmentProfile() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profilo");

        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        final Planner planner = PlannerManager.get().getUser();

        ImageView editPass = (ImageView) view.findViewById(R.id.change_pass);

        TextView passProfile = (TextView) view.findViewById(R.id.pass_profile);
        TextView emailProfile = (TextView) view.findViewById(R.id.email_profile);
        TextView nameProfile = (TextView) view.findViewById(R.id.name_profile);
        TextView birthDateProfile = (TextView) view.findViewById(R.id.birth_date_profile);
        TextView locationProfile = (TextView) view.findViewById(R.id.location_profile);
        TextView genderProfile = (TextView) view.findViewById(R.id.gender_profile);

        passProfile.setText(planner.password);
        emailProfile.setText(planner.email);
        nameProfile.setText(planner.name);
        birthDateProfile.setText(planner.birthDate);
        locationProfile.setText(planner.location);
        if(planner.gender.equals("F")) {
            genderProfile.setText("Femmina");
        }
        else if(planner.gender.equals("M")) {
            genderProfile.setText("Maschio");
        }


        editPass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Cambia la password");

                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, (ViewGroup) getView(), false);

                final EditText oldPasswordField = (EditText) viewInflated.findViewById(R.id.old_password);
                final EditText newPasswordField = (EditText) viewInflated.findViewById(R.id.new_password);

                builder.setView(viewInflated);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        oldPassword = oldPasswordField.getText().toString();
                        newPassword = newPasswordField.getText().toString();

                        if(planner.password.equals(oldPassword) && newPassword.length() >= 8) {

                           try {

                               String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                                       .appendPath("changepassword").appendEncodedPath(planner.email)
                                       .getUri();

                                JSONObject obj = new JSONObject("{'oldPassword':'"+ planner.password+"', 'newPassword':'"+newPassword+"'}");

                                CustomRequest request = new CustomRequest(Request.Method.POST, url, obj,

                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                snackbar = Snackbar
                                                        .make(getView(), response.toString(), Snackbar.LENGTH_LONG);

                                                snackbar.show();

                                                planner.updatePassword(newPassword);

                                                PlannerManager.get().save(planner);

                                                dialog.cancel();

                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                        snackbar = Snackbar
                                                .make(getView(), error.toString(), Snackbar.LENGTH_LONG);

                                        snackbar.show();

                                    }
                                });

                                VolleyRequest.get(getContext()).add(request);

                            } catch (JSONException e) {

                                e.printStackTrace();
                            }
                        }
                        if(newPassword.length() < 8) {
                            snackbar = Snackbar
                                    .make(getView(), "La password deve essere almeno di 8 caratteri", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }

                        else {
                            snackbar = Snackbar
                                    .make(getView(), "Password errata, riprova!", Snackbar.LENGTH_LONG);
                            snackbar.show();

                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        return view;

    }

}


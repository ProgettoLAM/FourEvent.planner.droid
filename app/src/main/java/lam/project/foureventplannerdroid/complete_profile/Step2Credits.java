package lam.project.foureventplannerdroid.complete_profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.fcannizzaro.materialstepper.AbstractStep;

import org.json.JSONException;
import org.json.JSONObject;

import lam.project.foureventplannerdroid.MainActivity;
import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.User;
import lam.project.foureventplannerdroid.utils.UserManager;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;

/**
 * Created by Vale on 11/08/2016.
 */

public class Step2Credits extends AbstractStep {

    User createdUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.step2_credits, container, false);
    }


    @Override
    public String name() {
        return null;
    }

    @Override
    public void onNext() {

        createdUser = getStepDataFor(1).getParcelable(User.Keys.USER);

        String uri = FourEventUri.Builder.create(FourEventUri.Keys.USER)
                .appendPath(createdUser.email)
                .getUri();

        if(createdUser != null) {

            try {

                CustomRequest request = new CustomRequest(Request.Method.POST,
                        uri,
                    createdUser.toJson(),

                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            StepManager.get(getContext()).setStep(StepManager.COMPLETE);
                            UserManager.get().save(createdUser);

                            Intent intent = new Intent(getContext(), MainActivity.class);
                            startActivity(intent);

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            System.out.println(error.toString());
                        }
                    }
                );

                VolleyRequest.get(getContext()).add(request);

            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPrevious() {
        System.out.println("onPrevious");
    }

    @Override
    public String optional() {
        return null;
    }

    @Override
    public boolean nextIf() {

       return true;
    }

    @Override
    public String error() {

        return "Completa tutti i campi obbligatori";
    }
}

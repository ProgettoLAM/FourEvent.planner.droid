package lam.project.foureventplannerdroid.complete_profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import lam.project.foureventplannerdroid.model.Planner;
import lam.project.foureventplannerdroid.utils.shared_preferences.PlannerManager;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.HandlerManager;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;

public class Step2Credits extends AbstractStep {

    Planner mCreatedPlanner;

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

        //Si riprende l'utente precedentemente creato nello step 1
        mCreatedPlanner = getStepDataFor(1).getParcelable(Planner.Keys.USER);

        String uri = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                .appendEncodedPath(mCreatedPlanner.email)
                .getUri();

        //Se l'utente esiste si salva nel server
        if(mCreatedPlanner != null) {

            try {

                CustomRequest request = new CustomRequest(Request.Method.POST, uri,
                        mCreatedPlanner.toJson(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                //Completamento dei 2 step e salvataggio dell'utente ultimato
                                StepManager.get(getContext()).setStep(StepManager.COMPLETE);
                                PlannerManager.get().save(mCreatedPlanner);

                                //Richiamo della MainActivity
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                getActivity().finish();
                                startActivity(intent);

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Snackbar snackbar = Snackbar.make(getActivity()
                                        .findViewById(R.id.container), HandlerManager
                                        .getInstance().handleError(error), Snackbar.LENGTH_LONG);

                                snackbar.getView().setBackgroundColor(ContextCompat
                                        .getColor(getContext(), R.color.lightRed));
                                snackbar.show();
                            }
                        }
                );

                VolleyRequest.get(getContext()).add(request);

            } catch (JSONException e) { e.printStackTrace();}
        }
    }

    @Override
    public void onPrevious() {
        System.out.println("onPrevious");
    }
}

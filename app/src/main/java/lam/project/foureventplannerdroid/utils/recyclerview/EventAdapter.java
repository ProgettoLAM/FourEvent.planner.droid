package lam.project.foureventplannerdroid.utils.recyclerview;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import lam.project.foureventplannerdroid.MainActivity;
import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private final List<Event> mModel;

    private Activity mSenderActivity;
    private View divider;
    private Snackbar snackbar;

    public EventAdapter(final Activity senderActivity, final List<Event> model) {

        this.mSenderActivity = senderActivity;
        this.mModel = model;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View layout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_events_list,parent,false);

        divider = layout.findViewById(R.id.divider);

        return new EventViewHolder(mSenderActivity,mModel,layout);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {

        //Se l'elemento selezionato è l'ultimo della lista, non compare il divider al di sotto
        if( position == getItemCount() - 1 ){

            divider.setVisibility(View.INVISIBLE);
        }
        holder.bind(mModel.get(position));

    }

    /**
     * Metodo di rimozione di un evento
     * @param position posizione dell'elemento
     */
    void remove(final int position) {

        //Creo l'url della richiesta
        String url = FourEventUri.Builder.create(FourEventUri.Keys.EVENT)
                .appendEncodedPath(MainActivity.mCurrentPlanner.email)
                .appendPath(mModel.get(position).mId).getUri();

        CustomRequest request = new CustomRequest(Request.Method.DELETE, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        //Si elimina l'evento dalla lista
                        mModel.remove(position);
                        notifyItemRemoved(position);

                        snackbar = Snackbar
                                .make(mSenderActivity.findViewById(R.id.container),
                                        "Evento eliminato!", Snackbar.LENGTH_LONG);

                        snackbar.getView().setBackgroundColor(ContextCompat
                                .getColor(mSenderActivity.getApplicationContext(), R.color.lightGreen));
                        snackbar.show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        snackbar = Snackbar.make(mSenderActivity.findViewById(R.id.container),
                                        "Non è possibile eliminare l'evento", Snackbar.LENGTH_LONG);

                        snackbar.getView().setBackgroundColor(ContextCompat
                                .getColor(mSenderActivity.getApplicationContext(), R.color.lightRed));
                        snackbar.show();
                        }
                }
        );

        VolleyRequest.get(mSenderActivity.getApplicationContext()).add(request);


    }

    /**
     * Metodo di swap del modello
     * @param firstPosition dalla prima posizione
     * @param secondPosition alla seconda posizione
     */
    void swap(int firstPosition, int secondPosition){
        Collections.swap(mModel, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
    }

    @Override
    public int getItemCount() {
        return mModel.size();
    }
}


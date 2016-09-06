package lam.project.foureventplannerdroid.fragment;


import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lam.project.foureventplannerdroid.CreateEventActivity;
import lam.project.foureventplannerdroid.MainActivity;
import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.utils.connection.HandlerManager;
import lam.project.foureventplannerdroid.utils.recyclerview.EventAdapter;
import lam.project.foureventplannerdroid.utils.connection.EventListRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;
import lam.project.foureventplannerdroid.utils.recyclerview.EventTouchHelper;


public class EventFragment extends Fragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private EventAdapter mAdapter;
    private static final String NAME = "Eventi";

    public List<Event> mModel = new ArrayList<>();

    ProgressBar mProgressBar;
    ImageView mSadImageEmoticon;
    TextView mEventNotFound;

    public EventFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setModel();

        return initView(inflater.inflate(R.layout.fragment_event, container, false));

    }

    /**
     * Metodo per inizializzare gli elementi del fragment
     * @param view del fragment
     * @return la view completa di tutti i campi
     */
    private View initView(View view) {

        setTitle();

        mSadImageEmoticon = (ImageView) view.findViewById(R.id.events_sad_emoticon);
        mEventNotFound = (TextView) view.findViewById(R.id.events_not_found);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.events_swipe_refresh_layout);

        //Allo swipe refresh della recycler view, si setta la lista di eventi
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                setModel();
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.events_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton mEventFab = (FloatingActionButton) view.findViewById(R.id.events_fab);
        mEventFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(v.getContext(), CreateEventActivity.class));
            }
        });

        mAdapter = new EventAdapter(getActivity(), mModel);

        mRecyclerView.setAdapter(mAdapter);

        //Callback del touch di un item, attaccandolo ad una recycler view
        ItemTouchHelper.Callback callback = new EventTouchHelper(mAdapter, getActivity());
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecyclerView);

        //Animazione della progress bar, al caricamento della recycler view
        ObjectAnimator animation = ObjectAnimator.ofInt (mProgressBar, "progress", 0, 500);
        animation.setDuration (1000);
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();


        //Mostro la progress bar e nascondo tutto il resto
        mProgressBar.setVisibility(View.VISIBLE);

        mRecyclerView.setVisibility(View.INVISIBLE);
        mSadImageEmoticon.setVisibility(View.INVISIBLE);
        mEventNotFound.setVisibility(View.INVISIBLE);

        return view;

    }

    /**
     * Si setta la lista degli eventi, richiamandoli dal server
     */
    private void setModel(){

        //Creo l'url della richiesta
        String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER).appendPath(FourEventUri.Keys.EVENT)
                .appendEncodedPath(MainActivity.mCurrentPlanner.email).getUri();

        EventListRequest request = new EventListRequest(url,
                new Response.Listener<List<Event>>() {
                    @Override
                    public void onResponse(List<Event> response) {

                        //Rimpiazzo il modello con tutti gli eventi e li ordino dal più recente
                        mModel.clear();
                        mModel.addAll(response);
                        Collections.reverse(mModel);

                        mAdapter.notifyDataSetChanged();

                        //Se lo swipe refresh è attivo, si disattiva
                        if(mSwipeRefreshLayout.isRefreshing()) {

                            mSwipeRefreshLayout.setRefreshing(false);
                        }

                        showAndHideViews();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        mEventNotFound.setText(R.string.events_not_found);
                        showAndHideViews();

                    }
                });

        VolleyRequest.get(getContext()).add(request);
    }

    /**
     * Metodo per mostrare/nascondere gli elementi del fragment (progress bar e recycler view)
     */
    public final void showAndHideViews() {

        //Nascondo sempre la progress bar
        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressBar.clearAnimation();

        if(mModel != null && mModel.size() > 0) {

            //Mostro la recycler view
            mRecyclerView.setVisibility(View.VISIBLE);

            //Nascondo icone e testo degli eventi non trovati
            mSadImageEmoticon.setVisibility(View.INVISIBLE);
            mEventNotFound.setVisibility(View.INVISIBLE);

        } else {

            //Nascondo la recycler view
            mRecyclerView.setVisibility(View.INVISIBLE);

            //Mostro icone e testo degli eenti non trovati
            mSadImageEmoticon.setVisibility(View.VISIBLE);
            mEventNotFound.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Si setta il titolo del fragment
     */
    private void setTitle () {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(NAME);
    }
}

package lam.project.foureventplannerdroid.fragment;


import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lam.project.foureventplannerdroid.CreateEventActivity;
import lam.project.foureventplannerdroid.MainActivity;
import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.utils.EventAdapter;
import lam.project.foureventplannerdroid.utils.connection.EventListRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;

import static android.view.View.INVISIBLE;

/**
 * Created by Vale on 21/08/2016.
 */

public class EventFragment extends Fragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private EventAdapter mAdapter;
    private static final String NAME = "Eventi";

    public static List<Event> mModel = new ArrayList<>();

    ProgressBar mProgressBar;
    ImageView mSadImageEmoticon;
    TextView mEventNotFound;

    public EventFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setModel();

        return initView(inflater.inflate(R.layout.fragment_event, container, false));

    }

    private View initView(View view) {
        setTitle();

        mSadImageEmoticon = (ImageView) view.findViewById(R.id.events_sad_emoticon);
        mEventNotFound = (TextView) view.findViewById(R.id.events_not_found);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.events_swipe_refresh_layout);

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

        mAdapter = new EventAdapter(getActivity(), getContext(), mRecyclerView, mModel);

        mRecyclerView.setAdapter(mAdapter);

        ObjectAnimator animation = ObjectAnimator.ofInt (mProgressBar, "progress", 0, 500);
        animation.setDuration (1000);
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();


        //mostro progress bar e nascondo tutto il resto
        mProgressBar.setVisibility(View.VISIBLE);

        mRecyclerView.setVisibility(View.INVISIBLE);
        mSadImageEmoticon.setVisibility(View.INVISIBLE);
        mEventNotFound.setVisibility(View.INVISIBLE);

        return view;

    }

    private void setModel(){

        String url = FourEventUri.Builder.create(FourEventUri.Keys.EVENT).appendPath("planner")
                .appendEncodedPath(MainActivity.mCurrentPlanner.email).getUri();

        EventListRequest request = new EventListRequest(url,
                new Response.Listener<List<Event>>() {
                    @Override
                    public void onResponse(List<Event> response) {

                        mModel.clear();
                        mModel.addAll(response);
                        Collections.reverse(mModel);

                        mAdapter.notifyDataSetChanged();

                        mRecyclerView.setVisibility(View.VISIBLE);
                        mSadImageEmoticon.setVisibility(INVISIBLE);
                        mEventNotFound.setVisibility(INVISIBLE);

                        if(mSwipeRefreshLayout.isRefreshing()) {

                            mSwipeRefreshLayout.setRefreshing(false);
                        }

                        showAndHideViews();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String responseBody = null;

                        try {

                            responseBody = new String( error.networkResponse.data, "utf-8" );
                            JSONObject jsonObject = new JSONObject( responseBody );
                            String errorText = (String) jsonObject.get("message");
                            mEventNotFound.setText(errorText);
                            showAndHideViews();


                        } catch (NullPointerException | UnsupportedEncodingException  | JSONException e) {

                            if( e instanceof NullPointerException) {

                                Snackbar snackbar = Snackbar.make(mEventNotFound,"Impossibile raggiungere il server",Snackbar.LENGTH_INDEFINITE);
                                snackbar.getView().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightRed));
                                snackbar.show();
                            }

                            showAndHideViews();
                            e.printStackTrace();
                        }

                    }
                });

        VolleyRequest.get(getContext()).add(request);
    }

    public final void showAndHideViews() {

        //nascondo sempre la progress bar
        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressBar.clearAnimation();

        if(mModel != null && mModel.size() > 0) {

            //mostro la recyclerview
            mRecyclerView.setVisibility(View.VISIBLE);

            //nascondo icone e testo
            mSadImageEmoticon.setVisibility(View.INVISIBLE);
            mEventNotFound.setVisibility(View.INVISIBLE);

        } else {

            //nascondo la recyclerview
            mRecyclerView.setVisibility(View.INVISIBLE);

            //mostro icone e testo
            mSadImageEmoticon.setVisibility(View.VISIBLE);
            mEventNotFound.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResume() {

        setModel();
        super.onResume();
    }

    private void setTitle () {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(NAME);
    }
}
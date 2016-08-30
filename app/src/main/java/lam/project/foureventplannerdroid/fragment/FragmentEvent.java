package lam.project.foureventplannerdroid.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

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

public class FragmentEvent extends Fragment {

    private RecyclerView mRecyclerView;
    private EventAdapter mAdapter;

    public static List<Event> mModel = new LinkedList<>();

    private ImageView sadEmoticon;
    private TextView notEvents;

    private FloatingActionButton mEventFab;

    public FragmentEvent() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setModel();

        final View rootView = inflater.inflate(R.layout.fragment_event, container, false);

        sadEmoticon = (ImageView) rootView.findViewById(R.id.sad_emoticon);
        notEvents = (TextView) rootView.findViewById(R.id.not_events);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.events_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mEventFab = (FloatingActionButton) rootView.findViewById(R.id.events_fab);
        mEventFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(v.getContext(), CreateEventActivity.class));
            }
        });

        mAdapter = new EventAdapter(getActivity(), getContext(), mRecyclerView, mModel);

        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    private void setModel(){

        String url = FourEventUri.Builder.create(FourEventUri.Keys.EVENT)
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
                        sadEmoticon.setVisibility(INVISIBLE);
                        notEvents.setVisibility(INVISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        sadEmoticon.setVisibility(View.VISIBLE);
                        notEvents.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(INVISIBLE);

                    }
                });

        VolleyRequest.get(getContext()).add(request);
    }

    @Override
    public void onResume() {

        setModel();
        super.onResume();
    }
}

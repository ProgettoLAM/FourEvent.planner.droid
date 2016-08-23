package lam.project.foureventplannerdroid.fragment;


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
import java.util.List;

import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.utils.EventAdapter;
import lam.project.foureventplannerdroid.utils.EventListRequest;
import lam.project.foureventplannerdroid.utils.VolleyRequest;

import static android.view.View.INVISIBLE;

/**
 * Created by Vale on 21/08/2016.
 */

public class FragmentEvent extends Fragment {

    private RecyclerView mRecyclerView;
    private EventAdapter mAdapter;

    public static List<Event> mModel;

    private ImageView sadEmoticon;
    private TextView notEvents;
    private FloatingActionButton fab_event;


    public FragmentEvent() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            mModel = new ArrayList<>();
            setModel();

            final View rootView = inflater.inflate(R.layout.fragment_event, container, false);

            sadEmoticon = (ImageView) rootView.findViewById(R.id.sad_emoticon);
            notEvents = (TextView) rootView.findViewById(R.id.not_events);

            fab_event = (FloatingActionButton) rootView.findViewById(R.id.events_fab);

            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.events_recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


            mAdapter = new EventAdapter(getContext(), mRecyclerView, mModel);
            //LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

           // layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            //layoutManager.scrollToPosition(0);


            mRecyclerView.setAdapter(mAdapter);

            return rootView;
        }

        private void setModel(){

            EventListRequest request = new EventListRequest(getString(R.string.url_service),
                    new Response.Listener<List<Event>>() {
                        @Override
                        public void onResponse(List<Event> response) {

                            mModel.clear();
                            mModel.addAll(response);

                            mAdapter.notifyDataSetChanged();

                            mRecyclerView.setVisibility(View.VISIBLE);
                            sadEmoticon.setVisibility(INVISIBLE);
                            notEvents.setVisibility(INVISIBLE);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Snackbar.make(getView(), "Error: " + error.getLocalizedMessage(), Snackbar.LENGTH_SHORT)
                                    .setAction("action", null)
                                    .show();

                            sadEmoticon.setVisibility(View.VISIBLE);
                            notEvents.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(INVISIBLE);

                        }
                    });

            VolleyRequest.get(getContext()).add(request);
        }


}

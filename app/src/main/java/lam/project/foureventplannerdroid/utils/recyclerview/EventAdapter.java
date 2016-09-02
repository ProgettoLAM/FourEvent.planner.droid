/*
 * Copyright (C) 2015 Thomas Robert Altstidl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import lam.project.foureventplannerdroid.MainActivity;
import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.EventListRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.HandlerManager;
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
        if( position == getItemCount() - 1 ){

            divider.setVisibility(View.INVISIBLE);
        }
        holder.bind(mModel.get(position));

    }

    public void remove(final int position) {

        String url = FourEventUri.Builder.create(FourEventUri.Keys.EVENT).appendEncodedPath(MainActivity.mCurrentPlanner.email)
                    .appendPath(mModel.get(position).mId).getUri();

        CustomRequest request = new CustomRequest(Request.Method.DELETE, url, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    mModel.remove(position);
                    notifyItemRemoved(position);

                    snackbar = Snackbar
                            .make(mSenderActivity.findViewById(R.id.container),
                                    "Evento eliminato!", Snackbar.LENGTH_LONG);

                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(mSenderActivity.getApplicationContext(), R.color.lightGreen));

                    snackbar.show();


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                try {
                    String responseBody = new String( error.networkResponse.data, "utf-8" );
                    JSONObject jsonObject = new JSONObject( responseBody );

                    String errorText = (String) jsonObject.get("message");

                    snackbar = Snackbar
                            .make(mSenderActivity.findViewById(R.id.container),
                                    errorText, Snackbar.LENGTH_LONG);

                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(mSenderActivity.getApplicationContext(), R.color.lightRed));

                    snackbar.show();

                } catch (NullPointerException | UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        VolleyRequest.get(mSenderActivity.getApplicationContext()).add(request);


    }

    public void swap(int firstPosition, int secondPosition){
        Collections.swap(mModel, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
    }

    @Override
    public int getItemCount() {
        return mModel.size();
    }
}


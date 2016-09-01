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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Event;


public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {
    private final List<Event> mModel;

    private Activity mSenderActivity;
    private View divider;



    public EventAdapter(final Activity senderActivity, final List<Event> model) {

        this.mSenderActivity = senderActivity;

        this.mModel = model;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View layout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_events_list,parent,false);

        divider = (View) layout.findViewById(R.id.divider);


        return new EventViewHolder(mSenderActivity,mModel,layout);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        if( position == getItemCount() - 1 ){

            divider.setVisibility(View.INVISIBLE);
        }
        holder.bind(mModel.get(position));

    }

    @Override
    public int getItemCount() {
        return mModel.size();
    }
}

    /*@Override
    public RecyclerView.ViewHolder onCreateSwipeViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_events_list, parent, true);
        view =  v.findViewById(R.id.divider);

        return new EventViewHolder(v);
    }


    @Override
    public void onBindSwipeViewHolder(RecyclerView.ViewHolder swipeViewHolder, int i) {
        if( i == getItemCount() - 1 ){

            view.setVisibility(INVISIBLE);
        }
        ((EventViewHolder) swipeViewHolder).bind(mModel.get(i));
    }

    @Override
    public SwipeConfiguration onCreateSwipeConfiguration(Context context, int position) {
            return new SwipeConfiguration.Builder(context)
                    .setRightDrawableResource(R.drawable.ic_trash)
                    .setRightSwipeBehaviour(SwipeConfiguration.SwipeBehaviour.RESTRICTED_SWIPE)
                    .setLeftSwipeBehaviour(SwipeConfiguration.SwipeBehaviour.NO_SWIPE)
                    .build();

    }

    @Override
    public void onSwipe(final int position, int direction) {

       /* if(direction == SWIPE_RIGHT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage("Sei sicuro di voler eliminare l'evento?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mModel.remove(position);
                            notifyItemRemoved(position);
                            Toast toast = Toast.makeText(mContext, "Evento eliminato ", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    })
                    .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            new SwipeConfiguration.Builder(view.getContext())
                                    .setRightDrawableResource(R.drawable.ic_trash)
                                    .setRightSwipeBehaviour(SwipeConfiguration.SwipeBehaviour.RESTRICTED_SWIPE)
                                    .build();
                        }
                    });
            builder.show();
        }

    }



    /*@Override
    public int getItemCount() {
        return mModel.size();
    }*/


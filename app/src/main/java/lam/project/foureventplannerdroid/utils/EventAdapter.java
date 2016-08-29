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

package lam.project.foureventplannerdroid.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tr4android.recyclerviewslideitem.SwipeAdapter;
import com.tr4android.recyclerviewslideitem.SwipeConfiguration;

import java.util.Date;
import java.util.List;

import lam.project.foureventplannerdroid.EventDetailActivity;
import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;

import static android.view.View.INVISIBLE;

public class EventAdapter extends SwipeAdapter implements View.OnClickListener {
    private final List<Event> mModel;
    private View view;

    private Activity mSenderActivity;

    private Context mContext;
    private RecyclerView mRecyclerView;

    public EventAdapter(Activity senderActivity, Context context, RecyclerView recyclerView, final List<Event> model) {

        mSenderActivity = senderActivity;
        mContext = context;
        mRecyclerView = recyclerView;
        this.mModel = model;
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitleEvent;
        private TextView mDateEvent;
        private ImageView mImgEvent;


        public EventViewHolder(final View itemView) {
            super(itemView);

            mTitleEvent = (TextView) itemView.findViewById(R.id.title_event);
            mDateEvent = (TextView) itemView.findViewById(R.id.date_event);

            mImgEvent = (ImageView) itemView.findViewById(R.id.img_event);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mSenderActivity.startActivity(new Intent(itemView.getContext(),
                            EventDetailActivity.class));
                }
            });
        }

        public void bind(Event event){

            mTitleEvent.setText(event.mTitle);
            mDateEvent.setText(event.mStartDate);

            String url = FourEventUri.Builder.create(FourEventUri.Keys.EVENT)
                    .appendPath("img").appendPath(event.mId).getUri();

            Picasso.with(itemView.getContext()).load(url).resize(1200,600).into(mImgEvent);


        }
    }

    @Override
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

        if(direction == SWIPE_RIGHT) {
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

    @Override
    public void onClick(View view) {
        // We need to get the parent of the parent to actually have the proper view
        int position = mRecyclerView.getChildAdapterPosition((View) view.getParent().getParent());
        Toast toast = Toast.makeText(mContext, "Clicked item at position " + position, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public int getItemCount() {
        return mModel.size();
    }
}

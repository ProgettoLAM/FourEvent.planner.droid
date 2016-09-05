package lam.project.foureventplannerdroid.utils.recyclerview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import lam.project.foureventplannerdroid.EventDetailActivity;
import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Event;
import lam.project.foureventplannerdroid.utils.shared_preferences.ImageManager;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;


final class EventViewHolder extends RecyclerView.ViewHolder {

    private List<Event> mModel;
    private TextView mTitleEvent;
    private TextView mDateEvent;
    private ImageView mImgEvent;

    /**
     * Metodo per inizializzare i campi di un evento
     * @param activity activity dalla quale proviene
     * @param model lista di eventi
     * @param itemView la view del singolo item
     */
    EventViewHolder(final Activity activity, final List<Event> model, final View itemView) {

        super(itemView);

        mModel = model;
        mTitleEvent = (TextView) itemView.findViewById(R.id.title_event);
        mDateEvent = (TextView) itemView.findViewById(R.id.date_event);
        mImgEvent = (ImageView) itemView.findViewById(R.id.img_event);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(itemView.getContext(), EventDetailActivity.class);

                Event event = mModel.get(getAdapterPosition());
                intent.putExtra(Event.Keys.EVENT,event);

                activity.startActivity(intent);
            }
        });


    }

    /**
     * Bind degli elementi di un item con i dati raccolti dal server per quell'evento
     * @param event evento singolo della recycler view
     */
    void bind(Event event){

        mTitleEvent.setText(event.mTitle);
        mDateEvent.setText(event.mStartDate);

        String url = FourEventUri.Builder.create(FourEventUri.Keys.EVENT)
                .appendPath("img").appendPath(event.mId).getUri();

        Bitmap contentImage = ImageManager.get().readImage(event.mImage);

        //Se il contenuto dell'immagine salvato in locale è nullo,
        if(contentImage == null)

            //Si carica dal server
            Picasso.with(itemView.getContext()).load(url).into(mImgEvent);
        else
            mImgEvent.setImageBitmap(contentImage);

    }
}


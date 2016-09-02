package lam.project.foureventplannerdroid.utils.recyclerview;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Vale on 02/09/2016.
 */

public class MovieTouchHelper extends ItemTouchHelper.SimpleCallback {

    private EventAdapter mAdapter;
    private Activity mSenderActivity;

    public MovieTouchHelper(EventAdapter eventAdapter, Activity activity){
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.mAdapter = eventAdapter;
        this.mSenderActivity = activity;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder,final int direction) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mSenderActivity);
        builder.setTitle("Elimina evento");
        builder.setMessage("Vuoi davvero eliminare l'evento?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAdapter.remove(viewHolder.getAdapterPosition());
            }
        });
        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.show();
    }
}

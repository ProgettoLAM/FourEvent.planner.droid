package lam.project.foureventplannerdroid.utils.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Vale on 02/09/2016.
 */

public class MovieTouchHelper extends ItemTouchHelper.SimpleCallback {

    private EventAdapter mAdapter;

    public MovieTouchHelper(EventAdapter eventAdapter){
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.mAdapter = eventAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.remove(viewHolder.getAdapterPosition());
    }
}

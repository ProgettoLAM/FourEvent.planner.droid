package lam.project.foureventplannerdroid.fragment.TimeLine;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.vipul.hp_hp.timelineview.TimelineView;

import java.util.List;

import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Record;

/**
 * Created by spino on 22/08/16.
 */

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {

    private List<Record> mFeedList;

    public TimeLineAdapter(List<Record> feedList) {
        mFeedList = feedList;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position,getItemCount());
    }

    @Override
    public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = View.inflate(parent.getContext(), R.layout.item_timeline, null);

        return new TimeLineViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(TimeLineViewHolder holder, int position) {

        Record record = mFeedList.get(position);

        holder.mType.setText(record.mType);
        holder.mDate.setText(record.mDate);

        String amount = "";

        if(record.mType.equals(Record.Keys.RECHARGE)) {

            amount += "+"+record.mAmount;
            holder.mAmount.setTextColor(Color.parseColor("#4BAE4F"));

        } else {

            amount += record.mAmount;
            holder.mAmount.setTextColor(Color.parseColor("#F34235"));
        }

        amount += " €";

        holder.mAmount.setText(amount);
    }

    @Override
    public int getItemCount() {
        return (mFeedList!=null? mFeedList.size():0);
    }

}

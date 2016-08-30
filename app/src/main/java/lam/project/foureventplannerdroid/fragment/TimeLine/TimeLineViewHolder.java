package lam.project.foureventplannerdroid.fragment.TimeLine;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.vipul.hp_hp.timelineview.TimelineView;

import lam.project.foureventplannerdroid.R;

/**
 * Created by spino on 22/08/16.
 */

public class TimeLineViewHolder extends RecyclerView.ViewHolder {
    
    TextView mType;
    TextView mDate;
    TextView mAmount;

    TimeLineViewHolder(View itemView, int viewType) {
        super(itemView);

        mType = (TextView) itemView.findViewById(R.id.record_title);
        mDate = (TextView) itemView.findViewById(R.id.record_date);
        mAmount = (TextView) itemView.findViewById(R.id.record_amount);

        TimelineView mTimelineView = (TimelineView) itemView.findViewById(R.id.time_marker);
        mTimelineView.initLine(viewType);
    }
}
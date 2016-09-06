package lam.project.foureventplannerdroid.fragment.timeLine;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.vipul.hp_hp.timelineview.TimelineView;

import java.util.List;

import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Record;

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

        String title = record.mEvent == null ? record.mType : record.mType+" : "+record.mEvent;

        //Si setta il titolo e la data del record
        holder.mType.setText(title);
        holder.mDate.setText(record.mDate);

        String amount = "";

        //Se il tipo di record è una ricarica, l'importo sarà positivo e si cambia il colore
        if(record.mType.equals(Record.Keys.RECHARGE)) {

            amount += "+"+record.mAmount;
            holder.mAmount.setTextColor(Color.parseColor("#4BAE4F"));

            //Se il record indica un pagamento, l'importo sarà negativo e si cambia il colore
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

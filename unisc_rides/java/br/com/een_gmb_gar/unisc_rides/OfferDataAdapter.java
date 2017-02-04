package br.com.een_gmb_gar.unisc_rides;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eenagel on 10/18/15.
 */
public class OfferDataAdapter extends ArrayAdapter<MatchData> {
    private class ViewHolder {
        TextView name;
        TextView days;
        TextView shift;
        TextView promiscuous;
    }
    private Activity cntx;

    public void setActivity(Activity cntx){
        this.cntx = cntx;
    }

    public OfferDataAdapter(Context context, ArrayList<MatchData> offers) {
        super(context, R.layout.data_offer_item, offers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MatchData data = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder = new ViewHolder(); // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.data_offer_item, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.text_name_); // convertView.findViewById(R.id.text_name_)
            viewHolder.promiscuous = (TextView) convertView.findViewById(R.id.text_promiscuous); // convertView.findViewById(R.id.text_promiscuous);
            viewHolder.shift = (TextView) convertView.findViewById(R.id.text_shift); // convertView.findViewById(R.id.text_shift);
            viewHolder.days = (TextView) convertView.findViewById(R.id.text_days); //convertView.findViewById(R.id.text_days);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        Log.d("yadayada", "Offer: "+(viewHolder.days==null) +" Person: "+(viewHolder.name==null)+ " OfferData "+(viewHolder.shift==null));

        viewHolder.name.setText(data.person.name);
        viewHolder.days.setText(data.offer.weekDays.toString());
        viewHolder.shift.setText(data.offer.shift.toString());
        viewHolder.promiscuous.setText(data.origin.nickname);

        // Return the completed view to render on screen
        return convertView;
    }


}

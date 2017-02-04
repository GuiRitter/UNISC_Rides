package br.com.een_gmb_gar.unisc_rides;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nuts on 10/7/15.
 */
public class DataAdapter extends ArrayAdapter<MySeatData> {
    private class ViewHolder {
        TextView name;
        TextView days;
        TextView driver;
        ImageView img;
    }

    public DataAdapter(Context context, ArrayList<MySeatData> users) {
        super(context, R.layout.data_item, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MySeatData data = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.data_item, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.text_name);
            viewHolder.days = (TextView) convertView.findViewById(R.id.text_sub);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.img_status);
            viewHolder.driver = (TextView) convertView.findViewById(R.id.text_driver);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.name.setText(data.offer.shift.toString());
        viewHolder.days.setText(data.offer.weekDays.toString());
        viewHolder.driver.setText(data.person.name.toString());
        if (data.offer.enabled) {
            viewHolder.img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.green_ball));
        } else {
            viewHolder.img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.red_ball));
        }
        // Return the completed view to render on screen
        return convertView;
    }
}

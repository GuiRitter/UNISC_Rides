package br.com.een_gmb_gar.unisc_rides;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eenagel on 11/15/15.
 */
public class ChatAdapter extends ArrayAdapter<Chat> {
    private class ChatBanner {
        TextView peer_name;
        TextView last_msg;
    }

    public ChatAdapter(Context context, ArrayList<Chat> users) {
        super(context, R.layout.chat_view, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Person person = getItem(position).person;
        // Check if an existing view is being reused, otherwise inflate the view
        ChatBanner chat_banner; // view lookup cache stored in tag
        if (convertView == null) {
            chat_banner = new ChatBanner();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.chat_view, parent, false);
            chat_banner.peer_name = (TextView) convertView.findViewById(R.id.chat_view_peer_name);
            chat_banner.last_msg = (TextView) convertView.findViewById(R.id.chat_view_last_msg);
            convertView.setTag(chat_banner);
        } else {
            chat_banner = (ChatBanner) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        chat_banner.peer_name.setText(person.name);
        chat_banner.last_msg.setText(getItem(position).last_msg);

        // Return the completed view to render on screen
        return convertView;
    }
}

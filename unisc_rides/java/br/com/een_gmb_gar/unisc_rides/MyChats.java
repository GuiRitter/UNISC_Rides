package br.com.een_gmb_gar.unisc_rides;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MyChats extends Activity {
    private int usr_id;
    private ListView chats_view;
    private ArrayList<Chat> my_chat_peers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_chats);

        // get user ID
        Bundle bundle = getIntent().getExtras();
        this.usr_id = (int) bundle.get("user_id");

        // get list view reference
        chats_view = (ListView) findViewById(R.id.listView_my_chats);

        chats_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MyChats.this, ChatActivity.class);
                intent.putExtra("user_id", MyChats.this.usr_id);
                UserMain.transfer_person = MyChats.this.my_chat_peers.get(i).person;
                intent.putExtra("peer",  MyChats.this.my_chat_peers.get(i).person.getKeyValuePairs());
                startActivity(intent);
            }
        });

        my_chat_peers = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load_chats_view();
    }

    private void load_chats_view() {
        ArrayList <Person> db_person_list = new ArrayList<>();
        ArrayList <Message> db_msg_list = new ArrayList<>();
        Message msg = new Message();
        msg.sender = usr_id;

        // to get all persons from database we have to pass a null object
        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_PERSON, new Object[]{null, db_person_list});
        if(!Client.socket.getRc()){
            Toast.makeText(this, "Error reading persons from database.", Toast.LENGTH_LONG).show();
            return;
        }

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_MSG, new Object[]{msg, db_msg_list});
        if(!Client.socket.getRc()){
            Toast.makeText(this, "Error reading messages from database.", Toast.LENGTH_LONG).show();
            return;
        }
        msg.sender = -1;
        msg.receiver = usr_id;
        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_MSG, new Object[]{msg, db_msg_list});
        if(!Client.socket.getRc()){
            Toast.makeText(this, "Error reading messages from database.", Toast.LENGTH_LONG).show();
            return;
        }
        Collections.sort(db_msg_list, new Comparator<Message>() {
            @Override
            public int compare(Message message, Message t1) {
                if(message.date.after(t1.date)){
                    return -1;
                }
                else if(message.date.before(t1.date)){
                    return 1;
                }
                else {
                    if(message.time.after(t1.time))
                        return -1;
                    else if(message.time.before(t1.time))
                        return 1;
                    else
                        return 0;
                }
            }
        });

        my_chat_peers.clear();
        for(Person p : db_person_list){
            Chat chat;
            boolean found = false;

            if(p.id != this.usr_id) {
                for (Message m : db_msg_list) {
                    if (m.sender == p.id || m.receiver == p.id) {
                        chat = new Chat(p, m.message);
                        my_chat_peers.add(chat);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    chat = new Chat(p, "");
                    my_chat_peers.add(chat);
                }
            }
        }

        // update the list of chats
        final ChatAdapter adapter = new ChatAdapter(this, my_chat_peers);
        chats_view.setAdapter(adapter);
    }
}

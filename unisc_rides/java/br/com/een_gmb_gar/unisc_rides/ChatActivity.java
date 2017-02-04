package br.com.een_gmb_gar.unisc_rides;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChatActivity extends Activity {
    private int usr_id;
    private Person peer;
    private ListView lv_messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Bundle bundle = getIntent().getExtras();
        Log.d("ChatActivity", "is bundle null? " + (bundle == null));
        Log.d("ChatActivity", "bundle contains int? " + bundle.get("user_id"));
//        this.usr_id = (int) bundle.get("user_id");
        this.usr_id = UserMain.usr_id;
        try {
            this.peer = new Person((String) bundle.get("peer"));
            Log.d("ChatActivity", (peer == null) + "");
            Log.d("ChatActivity", peer.getKeyValuePairs());
        }catch(Exception e){
            e.printStackTrace();
            Log.d("chat on create","Erro passando person");
        }

//        this.peer = UserMain.transfer_person;

        this.lv_messages = (ListView) findViewById(R.id.actChat_chat_view);

        this.lv_messages.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean up_scroll = false;
            int cnt_up=0;

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (up_scroll) {
                        cnt_up++;
                        if(cnt_up==2){
                            load_chat_view();
                            cnt_up=1;
                        }
                    }
                    else {
                        cnt_up=0;
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCnt, int totalItemCnt) {
                up_scroll = firstVisibleItem == 0;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load_chat_view();
    }

    private ArrayList<Message> get_peer_msgs(){
        Message msg = new Message();
        ArrayList <Message> db_msg_list = new ArrayList<>();

        msg.sender = this.peer.id;
        msg.receiver =  this.usr_id;

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_MSG, new Object[]{msg, db_msg_list});
        if(!Client.socket.getRc()){
            Toast.makeText(this, "Error reading messages from database.", Toast.LENGTH_LONG).show();
            return null;
        }
        return db_msg_list;
    }

    private ArrayList<Message> get_my_msgs(){
        Message msg = new Message();
        ArrayList <Message> db_msg_list = new ArrayList<>();

        msg.sender = this.usr_id;
        msg.receiver = this.peer.id;

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_MSG, new Object[]{msg, db_msg_list});
        if(!Client.socket.getRc()){
            Toast.makeText(this, "Error reading messages from database.", Toast.LENGTH_LONG).show();
            return null;
        }
        return db_msg_list;
    }

    private Person get_my_person(){
        Person p = new Person();
        p.id = usr_id;

        ArrayList<Person> list = new ArrayList<>();

        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_PERSON, new Object[]{p, list});
        if(!Client.socket.getRc()){
            Toast.makeText(this, "Error reading messages from database.", Toast.LENGTH_LONG).show();
            return null;
        }
        if(list.size()>0)
            return list.get(0);
        else
            return null;
    }

    private void load_chat_view(){
        ArrayList<Message> my_msgs = get_my_msgs();
        ArrayList<Message> peer_msgs = get_peer_msgs();
        ArrayList<ChatMessage> chat_messages = new ArrayList<>();
        ArrayList<ChatMessage> my_chats = new ArrayList<>();
        ArrayList<ChatMessage> peer_chats = new ArrayList<>();

        Person my_self = get_my_person();

        if(my_self == null)
            return;


        for(Message m : my_msgs){
            ChatMessage chat = new ChatMessage(my_self,m);
            chat_messages.add(chat);
        }

        for(Message m : peer_msgs){
            ChatMessage chat = new ChatMessage(peer, m);
            chat_messages.add(chat);
        }


        Collections.sort(chat_messages, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage message, ChatMessage t1) {
                if (message.last_msg.date.after(t1.last_msg.date)) {

                    return -1;

                } else if (message.last_msg.date.before(t1.last_msg.date)) {
                    return 1;

                } else {
                    if(message.last_msg.time.after(t1.last_msg.time))
                        return -1;
                    else if(message.last_msg.time.before(t1.last_msg.time))
                        return 1;
                    else
                        return 0;
                }
            }
        });

        // update the list of chats
        final ChatMessageAdapter adapter = new ChatMessageAdapter(this, chat_messages);
        this.lv_messages.setAdapter(adapter);
    }

    public void bt_chat_send(View view) {
        EditText text = (EditText) findViewById(R.id.actChat_enterMsg);

        if (!text.getText().toString().isEmpty()) {
            Message msg = new Message(usr_id, peer.id, null, null, text.getText().toString());
            Log.d("rollet", "usr id " + usr_id);
            Client.socket.send(Client.Cmds.CMD_CREATE, Client.Tables.TB_MSG, new Object[]{msg});
            if(!Client.socket.getRc()){
                Toast.makeText(this, "Error reading messages from database.", Toast.LENGTH_LONG).show();
            }else{
                Client.socket.send(Client.Cmds.CMD_MSG_NEW_MSG, null, new Object[]{usr_id, peer.IP});

                load_chat_view();
                text.setText("");
            }
        }
    }
}

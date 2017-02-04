package br.com.een_gmb_gar.unisc_rides;

/**
 * Created by eenagel on 11/16/15.
 */
public class ChatMessage {
    public Person person;
    public Message last_msg;

    public ChatMessage(Person p, Message last_m){
        this.person = p;
        this.last_msg = last_m;
    }
}

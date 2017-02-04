package br.com.een_gmb_gar.unisc_rides;

/**
 * Created by eenagel on 10/25/15.
 */
public interface MessageInterface {

    public void receiveNewAllocation (int person, int offer);

    public void receiveNewMessage (int sender);
}
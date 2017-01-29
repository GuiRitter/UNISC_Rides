package webserviceinterface;

/**
 *
 * @author Guilherme Alan Ritter
 */
public interface MessageInterface {

    public void receiveNewAllocation (int person, int offer);

    public void receiveNewMessage (int sender);
}

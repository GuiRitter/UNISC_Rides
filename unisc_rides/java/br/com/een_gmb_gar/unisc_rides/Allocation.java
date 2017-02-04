package br.com.een_gmb_gar.unisc_rides;

import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.S_F;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.u;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.w;


/**
 * For when someone that wants a ride selects one available.
 * @author Guilherme Alan Ritter M72642
 */
public final class Allocation {

    final String COLUMN_OFFER  = "offer";
    final String COLUMN_ORIGIN = "origin";
    final String COLUMN_PERSON = "person";
    final String COLUMN_STATUS = "status_";

    /**
     * Location of person wanting a ride.
     */
    int origin = -1;

    /**
     * Selected ride offer's id.
     */
    int offer = -1;

    /**
     * Id of person wanting a ride.
     */
    int person = -1;

    Status status = null;

    public enum Status {

        APPROVED,

        PENDING,

        REJECTED
    }

    public Allocation () {
    }

    public Allocation (int person, int origin, int offer, Status status) {
        this.person = person;
        this.origin = origin;
        this.offer  = offer;
        this.status = status;
    }

    public Allocation (Allocation allocation) {
        this.person = allocation.person;
        this.origin = allocation.origin;
        this.offer  = allocation.offer;
        this.status = allocation.status;
    }

    public Allocation (String keyValuePairs) throws Exception {
        String fields[] = keyValuePairs.split(S_F);
        for (int i = 0; i < fields.length; i += 2) {
            switch (fields[i]) {
            case COLUMN_PERSON:
                person = Integer.parseInt(fields[i + 1]);
                break; case COLUMN_ORIGIN:
                    origin = Integer.parseInt(fields[i + 1]);
            break; case COLUMN_OFFER:
                offer = Integer.parseInt(fields[i + 1]);
            break; case COLUMN_STATUS:
                status = Status.valueOf(u(fields[i + 1]));
            break; default:
                throw new Exception();
            }
        }
    }

    final String getKeyValuePairs () {
        String returnString =
         (isPersonNull() ? "" : (COLUMN_PERSON + S_F + person           + S_F)) +
         (isOriginNull() ? "" : (COLUMN_ORIGIN + S_F + origin           + S_F)) +
         (isOfferNull()  ? "" : (COLUMN_OFFER  + S_F + offer            + S_F)) +
         (isStatusNull() ? "" : (COLUMN_STATUS + S_F + w(status.name()) + S_F));
        return (returnString.length() < 1) ? null
         : (returnString.substring(0, returnString.length() - 1));
    }

    final boolean isNull () {
        return isPersonNull() && isOriginNull() && isOfferNull() && isStatusNull();
    }

    final boolean isOriginNull () {
        return origin < 0;
    }

    final boolean isOfferNull () {
        return offer < 0;
    }

    final boolean isPersonNull () {
        return person < 0;
    }

    final boolean isStatusNull () {
        return status == null;
    }

    @Override
    public String toString () {
        return "Allocation\n" +
         "Person: " + person + "\n" +
         "Origin:"  + origin + "\n" +
         "Offer: "  + offer  + "\n" +
         "Status: " + status + "\n";
    }
}
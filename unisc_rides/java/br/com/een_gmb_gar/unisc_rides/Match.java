package br.com.een_gmb_gar.unisc_rides;

import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.S_F;

/**
 * For when someone that wants a ride selects one available.
 * @author Guilherme Alan Ritter M72642
 */
public final class Match {

    final String COLUMN_IN     = "in_";
    final String COLUMN_OFFER  = "offer";
    final String COLUMN_ORIGIN = "origin";

    /**
     * Whether it's a positive or negative match.
     */
    Boolean in = null;

    /**
     * Location of person wanting a ride.
     */
    int origin = -1;

    /**
     * Selected ride offer's id.
     */
    int offer = -1;

    public Match () {
    }

    public Match (int offer, int origin, boolean in) {
        this.offer  = offer;
        this.origin = origin;
        this.in     = in;
    }

    public Match (Match match) {
        this.offer  = match.offer;
        this.origin = match.origin;
        this.in     = match.in;
    }

    public Match (String keyValuePairs) throws Exception {
        String fields[] = keyValuePairs.split(S_F);
        for (int i = 0; i < fields.length; i += 2) {
            switch (fields[i]) {
            case COLUMN_OFFER:
                offer = Integer.parseInt(fields[i + 1]);
            break; case COLUMN_ORIGIN:
                origin = Integer.parseInt(fields[i + 1]);
            break; case COLUMN_IN:
                in = fields[i + 1].equals("1");
            break; default:
                throw new Exception();
            }
        }
    }

    final String getKeyValuePairs () {
        String returnString =
         (isOfferNull()  ? "" : (COLUMN_OFFER  + S_F + offer            + S_F)) +
         (isOriginNull() ? "" : (COLUMN_ORIGIN + S_F + origin           + S_F)) +
         (isInNull()     ? "" : (COLUMN_IN     + S_F + (in ? "1" : "0") + S_F));
        return (returnString.length() < 1) ? null
         : (returnString.substring(0, returnString.length() - 1));
    }

    final boolean isNull () {
        return isOfferNull() && isOriginNull() && isInNull();
    }

    final boolean isInNull () {
        return in == null;
    }

    final boolean isOriginNull () {
        return origin < 0;
    }

    final boolean isOfferNull () {
        return offer < 0;
    }

    @Override
    public String toString () {
        return "Match\n" +
         "Offer: "  + offer  + "\n" +
         "Origin:"  + origin + "\n" +
         "In: "     + in     + "\n";
    }
}

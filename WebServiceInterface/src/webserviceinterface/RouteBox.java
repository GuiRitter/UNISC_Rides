package webserviceinterface;

import static webserviceinterface.WebServiceInterface.S_F;

public final class RouteBox {

    final String COLUMN_NORTH_EAST_LATITUDE  = "northEastLatitude";
    final String COLUMN_NORTH_EAST_LONGITUDE = "northEastLongitude";
    final String COLUMN_OFFER                = "offer";
    final String COLUMN_SOUTH_WEST_LATITUDE  = "southWestLatitude";
    final String COLUMN_SOUTH_WEST_LONGITUDE = "southWestLongitude";

    Double northEastLatitude = null;

    Double northEastLongitude = null;

    int offer = -1;

    Double southWestLatitude = null;

    Double southWestLongitude = null;

    public RouteBox () {
    }

    public RouteBox (int offer,
     double northEastLatitude, double northEastLongitude,
     double southWestLatitude, double southWestLongitude) {
        this.offer              = offer;
        this.northEastLatitude  = northEastLatitude;
        this.northEastLongitude = northEastLongitude;
        this.southWestLatitude  = southWestLatitude;
        this.southWestLongitude = southWestLongitude;
    }

    public RouteBox (RouteBox routeBox) {
        this.offer              = routeBox.offer;
        this.northEastLatitude  = routeBox.northEastLatitude;
        this.northEastLongitude = routeBox.northEastLongitude;
        this.southWestLatitude  = routeBox.southWestLatitude;
        this.southWestLongitude = routeBox.southWestLongitude;
    }

    public RouteBox (String keyValuePairs) throws Exception {
        String fields[] = keyValuePairs.split(S_F);
        for (int i = 0; i < fields.length; i += 2) {
            switch (fields[i]) {
            case COLUMN_OFFER:
                offer = Integer.parseInt(fields[i + 1]);
            break; case COLUMN_NORTH_EAST_LATITUDE:
                try {
                    northEastLatitude = Double.parseDouble(fields[i + 1]);
                } catch (NumberFormatException ex) {
                    northEastLatitude = null;
                    throw new Exception(ex);
                }
            break; case COLUMN_NORTH_EAST_LONGITUDE:
                try {
                    northEastLongitude = Double.parseDouble(fields[i + 1]);
                } catch (NumberFormatException ex) {
                    northEastLongitude = null;
                    throw new Exception(ex);
                }
            break; case COLUMN_SOUTH_WEST_LATITUDE:
                try {
                    southWestLatitude = Double.parseDouble(fields[i + 1]);
                } catch (NumberFormatException ex) {
                    southWestLatitude = null;
                    throw new Exception(ex);
                }
            break; case COLUMN_SOUTH_WEST_LONGITUDE:
                try {
                    southWestLongitude = Double.parseDouble(fields[i + 1]);
                } catch (NumberFormatException ex) {
                    southWestLongitude = null;
                    throw new Exception(ex);
                }
            break; default:
                throw new Exception();
            }
        }
    }

    final String getKeyValuePairs () {
        String returnString =
         (isOfferNull()              ? "" : (COLUMN_OFFER                + S_F + offer              + S_F)) +
         (isNorthEastLatitudeNull()  ? "" : (COLUMN_NORTH_EAST_LATITUDE  + S_F + northEastLatitude  + S_F)) +
         (isNorthEastLongitudeNull() ? "" : (COLUMN_NORTH_EAST_LONGITUDE + S_F + northEastLongitude + S_F)) +
         (isSouthWestLatitudeNull()  ? "" : (COLUMN_SOUTH_WEST_LATITUDE  + S_F + southWestLatitude  + S_F)) +
         (isSouthWestLongitudeNull() ? "" : (COLUMN_SOUTH_WEST_LONGITUDE + S_F + southWestLongitude + S_F));
        return (returnString.length() < 1) ? null
         : (returnString.substring(0, returnString.length() - 1));
    }

    final boolean isNull () {
        return isOfferNull()
         && isNorthEastLatitudeNull() && isNorthEastLongitudeNull()
         && isSouthWestLatitudeNull() && isSouthWestLongitudeNull();
    }

    final boolean isNorthEastLatitudeNull () {
        return northEastLatitude == null;
    }

    final boolean isNorthEastLongitudeNull () {
        return northEastLongitude == null;
    }

    final boolean isOfferNull () {
        return offer < 0;
    }

    final boolean isSouthWestLatitudeNull () {
        return southWestLatitude == null;
    }

    final boolean isSouthWestLongitudeNull () {
        return southWestLongitude == null;
    }

    @Override
    public String toString () {
        return "RouteBox\n" +
         "Offer: "               + offer              + "\n" +
         "Northeast Latitude: " +  northEastLatitude  + "\n" +
         "Northeast Longitude: " + northEastLongitude + "\n" +
         "Southeast Latitude: " +  southWestLatitude  + "\n" +
         "Southeast Longitude: " + southWestLongitude + "\n";
    }
}

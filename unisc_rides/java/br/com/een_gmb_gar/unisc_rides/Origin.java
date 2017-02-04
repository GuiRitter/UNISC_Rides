package br.com.een_gmb_gar.unisc_rides;

import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.S_F;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.u;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.w;

public final class Origin {

    String city = null;

    final String COLUMN_CITY         = "city";
    final String COLUMN_ID           = "id";
    final String COLUMN_LATITUDE     = "latitude";
    final String COLUMN_LONGITUDE    = "longitude";
    final String COLUMN_NEIGHBORHOOD = "neighborhood";
    final String COLUMN_NICKNAME     = "nickname";
    final String COLUMN_NUMBER       = "number";
    final String COLUMN_PERSON       = "person";
    final String COLUMN_STREET       = "street";

    int id = -1;

    Double latitude = null;

    Double longitude = null;

    String neighborhood = null;

    String nickname = null;

    int number = -1;

    int person = -1;

    String street = null;

    public Origin () {
    }

    public Origin (int id, int person,
     double latitude, double longitude, String nickname,
     String street, int number, String neighborhood, String city) {
        this.id           = id;
        this.person       = person;
        this.latitude     = latitude;
        this.longitude    = longitude;
        this.nickname     = nickname;
        this.street       = street;
        this.number       = number;
        this.neighborhood = neighborhood;
        this.city         = city;
    }

    public Origin (Origin origin) {
        this.id           = origin.id;
        this.person       = origin.person;
        this.latitude     = origin.latitude;
        this.longitude    = origin.longitude;
        this.nickname     = origin.nickname;
        this.street       = origin.street;
        this.number       = origin.number;
        this.neighborhood = origin.neighborhood;
        this.city         = origin.city;
    }

    public Origin (String keyValuePairs) throws Exception {
        String fields[] = keyValuePairs.split(S_F);
        for (int i = 0; i < fields.length; i += 2) {
            switch (fields[i]) {
            case COLUMN_ID:
                id = Integer.parseInt(fields[i + 1]);
            break; case COLUMN_PERSON:
                person = Integer.parseInt(fields[i + 1]);
            break; case COLUMN_LATITUDE:
                try {
                    latitude = Double.parseDouble(fields[i + 1]);
                } catch (NumberFormatException ex) {
                    latitude = null;
                    throw new Exception(ex);
                }
            break; case COLUMN_LONGITUDE:
                try {
                    longitude = Double.parseDouble(fields[i + 1]);
                } catch (NumberFormatException ex) {
                    longitude = null;
                    throw new Exception(ex);
                }
            break; case COLUMN_NICKNAME:
                nickname = u(fields[i + 1]);
            break; case COLUMN_STREET:
                if (fields[i + 1].equals("'null'")) {
                    street = null;
                } else {
                    street = u(fields[i + 1]);
                }
            break; case COLUMN_NUMBER:
                if (fields[i + 1].equals("null")) {
                    number = -1;
                } else {
                    number = Integer.parseInt(fields[i + 1]);
                }
            break; case COLUMN_NEIGHBORHOOD:
                    if (fields[i + 1].equals("'null'")) {
                        neighborhood = null;
                    } else {
                        neighborhood = u(fields[i + 1]);
                    }
            break; case COLUMN_CITY:
                    if (fields[i + 1].equals("'null'")) {
                        city = null;
                    } else {
                        city = u(fields[i + 1]);
                    }
            break; default:
                throw new Exception();
            }
        }
    }

    final String getKeyValuePairs () {
        String returnString =
         (isIdNull()           ? "" : (COLUMN_ID           + S_F + id              + S_F)) +
         (isPersonNull()       ? "" : (COLUMN_PERSON       + S_F + person          + S_F)) +
         (isLatitudeNull()     ? "" : (COLUMN_LATITUDE     + S_F + latitude        + S_F)) +
         (isLongitudeNull()    ? "" : (COLUMN_LONGITUDE    + S_F + longitude       + S_F)) +
         (isNicknameNull()     ? "" : (COLUMN_NICKNAME     + S_F + w(nickname)     + S_F)) +
         (isStreetNull()       ? "" : (COLUMN_STREET       + S_F + w(street)       + S_F)) +
         (isNumberNull()       ? "" : (COLUMN_NUMBER       + S_F + number          + S_F)) +
         (isNeighborhoodNull() ? "" : (COLUMN_NEIGHBORHOOD + S_F + w(neighborhood) + S_F)) +
         (isCityNull()         ? "" : (COLUMN_CITY         + S_F + w(city)         + S_F));
        return (returnString.length() < 1) ? null
         : (returnString.substring(0, returnString.length() - 1));
    }

    final boolean isCityNull () {
        return city == null;
    }

    final boolean isIdNull () {
        return id < 0;
    }

    final boolean isLatitudeNull () {
        return latitude == null;
    }

    final boolean isLongitudeNull () {
        return longitude == null;
    }

    final boolean isNicknameNull () {
        return nickname == null;
    }

    final boolean isNeighborhoodNull () {
        return neighborhood == null;
    }

    final boolean isNull (boolean considerId) {
        return (considerId ? isIdNull(): true)
         && isPersonNull() && isStreetNull()
         && isLatitudeNull() && isLongitudeNull() && isNicknameNull()
         && isNumberNull() && isNeighborhoodNull() && isCityNull();
    }

    final boolean isNumberNull () {
        return number < 0;
    }

    final boolean isPersonNull () {
        return person < 0;
    }

    final boolean isStreetNull () {
        return street == null;
    }

    @Override
    public String toString () {
        return "Origin\n" +
         "id: "           + id           + "\n" +
         "Person: "       + person       + "\n" +
         "Latitude: "     + latitude     + "\n" +
         "Longitude: "    + longitude    + "\n" +
         "Nickname: "     + nickname     + "\n" +
         "Street: "       + street       + "\n" +
         "Number: "       + number       + "\n" +
         "Neighborhood: " + neighborhood + "\n" +
         "City: "         + city         + "\n";
    }
}

package webserviceinterface;

import java.util.ArrayList;
import static webserviceinterface.WebServiceInterface.SQLdateFormat;
import static webserviceinterface.WebServiceInterface.S_F;
import static webserviceinterface.WebServiceInterface.u;
import static webserviceinterface.WebServiceInterface.w;

public final class Offer {

    byte availableSeats = -1;

    int id = -1;

    final String COLUMN_AVAILABLE_SEATS = "availableSeats";
    final String COLUMN_DISTANCE        = "distance";
    final String COLUMN_ENABLED         = "enabled";
    final String COLUMN_ID              = "id";
    final String COLUMN_INSERTION_DATE  = "insertionDate";
    final String COLUMN_ORIGIN          = "origin";
    final String COLUMN_PERSON          = "person";
    final String COLUMN_PROMISCUOUS     = "promiscuous";
    final String COLUMN_REMAINING_SEATS = "remainingSeats";
    final String COLUMN_SHIFT           = "shift";
    final String COLUMN_WEEK_DAYS       = "weekDays";

    Boolean enabled = null;

    java.sql.Date insertionDate = null;

    double distance = -1;

    int origin = -1;

    int person = -1;

    Boolean promiscuous = null;

    byte remainingSeats = -1;

    Shift shift = null;

    final ArrayList<WeekDay> weekDays = new ArrayList<>(weekDaysInitialCapacity);

    private static final int weekDaysInitialCapacity = 6;

    public enum Shift {

        /**
         * Morning.
         */
        Morning,

        /**
         * Afternoon.
         */
        Afternoon,

        /**
         * Night.
         */
        Night
    }

    public enum WeekDay {

        MONDAY,

        TUESDAY,

        WEDNESDAY,

        THURSDAY,

        FRIDAY,

        SATURDAY
    }

    public Offer () {
    }

    public Offer (int id, int person, int origin, double distance,
     byte availableSeats, byte remainingSeats, ArrayList<WeekDay> weekDays,
     Shift shift, java.sql.Date insertionDate, Boolean promiscuous, Boolean enabled) {
        this.id             = id;
        this.person         = person;
        this.origin         = origin;
        this.distance       = distance;
        this.availableSeats = availableSeats;
        this.remainingSeats = remainingSeats;
        if (!(weekDays == null))
            this.weekDays.addAll(weekDays);
        this.shift = shift;
        this.insertionDate  = insertionDate;
        this.promiscuous    = promiscuous;
        this.enabled        = enabled;
    }

    public Offer (Offer offer) {
        this.id             = offer.id;
        this.person         = offer.person;
        this.origin         = offer.origin;
        this.distance       = offer.distance;
        this.availableSeats = offer.availableSeats;
        this.remainingSeats = offer.remainingSeats;
        this.weekDays.addAll(offer.weekDays);
        this.shift          = offer.shift;
        this.insertionDate  = offer.insertionDate;
        this.promiscuous    = offer.promiscuous;
        this.enabled        = offer.enabled;
    }

    public Offer (String keyValuePairs) throws Exception {
        String fields[] = keyValuePairs.split(S_F);
        for (int i = 0; i < fields.length; i += 2) {
            switch (fields[i]) {
            case COLUMN_ID:
                id = Integer.parseInt(fields[i + 1]);
            case COLUMN_PERSON:
                person = Integer.parseInt(fields[i + 1]);
            case COLUMN_ORIGIN:
                origin = Integer.parseInt(fields[i + 1]);
            case COLUMN_DISTANCE:
                distance = Double.parseDouble(fields[i + 1]);
            break; case COLUMN_AVAILABLE_SEATS:
                availableSeats = Byte.parseByte(fields[i + 1]);
            break; case COLUMN_REMAINING_SEATS:
                remainingSeats = Byte.parseByte(fields[i + 1]);
            break; case COLUMN_WEEK_DAYS:
                weekDaysByteToList(Byte.parseByte(fields[i + 1]), weekDays);
            break; case COLUMN_SHIFT:
                shift = Shift.valueOf(u(fields[i + 1]));
            break; case COLUMN_INSERTION_DATE:
                insertionDate = new java.sql.Date(SQLdateFormat.parse(u(fields[i +1])).getTime());
            break; case COLUMN_PROMISCUOUS:
                promiscuous = fields[i + 1].equals("1");
            break; case COLUMN_ENABLED:
                enabled = fields[i + 1].equals("1");
            break; default:
                throw new Exception();
            }
        }
    }

    final String getKeyValuePairs () {
        String returnString =
         (isIdNull()             ? "" : (COLUMN_ID              + S_F + id                           + S_F)) +
         (isPersonNull()         ? "" : (COLUMN_PERSON          + S_F + person                       + S_F)) +
         (isOriginNull()         ? "" : (COLUMN_ORIGIN          + S_F + origin                       + S_F)) +
         (isDistanceNull()       ? "" : (COLUMN_DISTANCE        + S_F + distance                     + S_F)) +
         (isAvailableSeatsNull() ? "" : (COLUMN_AVAILABLE_SEATS + S_F + availableSeats               + S_F)) +
         (isRemainingSeatsNull() ? "" : (COLUMN_REMAINING_SEATS + S_F + remainingSeats               + S_F)) +
         (isWeekDaysNull()       ? "" : (COLUMN_WEEK_DAYS       + S_F + weekDaysListToByte(weekDays) + S_F)) +
         (isShiftNull()          ? "" : (COLUMN_SHIFT           + S_F + w(shift.name())              + S_F)) +
         (isInsertionDateNull()  ? "" : (COLUMN_INSERTION_DATE  + S_F + w(insertionDate.toString())  + S_F)) +
         (isPromiscuousNull()    ? "" : (COLUMN_PROMISCUOUS     + S_F + (promiscuous ? "1" : "0")    + S_F)) +
         (isEnabledNull()        ? "" : (COLUMN_ENABLED         + S_F + (enabled ? "1" : "0")        + S_F));
        return (returnString.length() < 1) ? null
         : (returnString.substring(0, returnString.length() - 1));
    }

    final boolean isAvailableSeatsNull () {
        return availableSeats < 0;
    }

    final boolean isDistanceNull () {
        return distance < 0;
    }

    final boolean isEnabledNull () {
        return enabled == null;
    }

    final boolean isIdNull () {
        return id < 0;
    }

    final boolean isInsertionDateNull () {
        return insertionDate == null;
    }

    /**
     * Some parameters have the choice of not being considered
     * when determining whether the object is null.
     * For creation, all the not null fields have to be set,
     * except for the fields that are generated automatically.
     * For selection and update, at least one field must not be null,
     * so all have to be considered.
     * @param considerId
     * @param considerInsertionDate
     * @return
     */
    final boolean isNull (boolean considerId, boolean considerInsertionDate) {
        return (considerId ? isIdNull(): true)
         && isPersonNull() && isOriginNull() && isDistanceNull()
         && isAvailableSeatsNull() && isRemainingSeatsNull()
         && isWeekDaysNull() && isShiftNull()
         && (considerInsertionDate ? isInsertionDateNull() : true)
         && isPromiscuousNull() && isEnabledNull();
    }

    final boolean isOriginNull() {
        return origin < 0;
    }

    final boolean isPersonNull() {
        return person < 0;
    }

    final boolean isPromiscuousNull () {
        return promiscuous == null;
    }

    final boolean isRemainingSeatsNull () {
        return remainingSeats < 0;
    }

    final boolean isShiftNull () {
        return shift == null;
    }

    final boolean isWeekDaysNull () {
        return weekDays.isEmpty();
    }

    @Override
    public String toString () {
        return "Offer\n" +
         "ID: "              + id             + "\n" +
         "Person: "          + person         + "\n" +
         "Origin: "          + origin         + "\n" +
         "Distance: "        + distance       + "\n" +
         "Available Seats: " + availableSeats + "\n" +
         "Remaining Seats: " + remainingSeats + "\n" +
         "Week Days: "       + weekDays       + "\n" +
         "Shift: "           + shift          + "\n" +
         "Insertion Date: "  + insertionDate  + "\n" +
         "Promiscuous: "     + promiscuous    + "\n" +
         "Enabled: "         + enabled        + "\n";
    }

    static void weekDaysByteToList (byte value, ArrayList<WeekDay> list) {
        if (list == null) {
            list = new ArrayList<>(weekDaysInitialCapacity);
        }
        if ((value & 0b00100000) > 0) {
            list.add(WeekDay.MONDAY);
        }
        if ((value & 0b00010000) > 0) {
            list.add(WeekDay.TUESDAY);
        }
        if ((value & 0b00001000) > 0) {
            list.add(WeekDay.WEDNESDAY);
        }
        if ((value & 0b00000100) > 0) {
            list.add(WeekDay.THURSDAY);
        }
        if ((value & 0b00000010) > 0) {
            list.add(WeekDay.FRIDAY);
        }
        if ((value & 0b00000001) > 0) {
            list.add(WeekDay.SATURDAY);
        }
    }

    static byte weekDaysListToByte (ArrayList<WeekDay> list) {
        byte value = 0;
        if (list.contains(WeekDay.MONDAY)) {
            value |= 0b100000;
        }
        if (list.contains(WeekDay.TUESDAY)) {
            value |= 0b10000;
        }
        if (list.contains(WeekDay.WEDNESDAY)) {
            value |= 0b1000;
        }
        if (list.contains(WeekDay.THURSDAY)) {
            value |= 0b100;
        }
        if (list.contains(WeekDay.FRIDAY)) {
            value |= 0b10;
        }
        if (list.contains(WeekDay.SATURDAY)) {
            value |= 0b1;
        }
        return value;
    }
}

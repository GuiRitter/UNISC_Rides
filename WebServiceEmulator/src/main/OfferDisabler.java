package main;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Keeps the JavaScript server from dying.
 * @author Guilherme Alan Ritter M72642
 */
@SuppressWarnings({"CallToPrintStackTrace", "SleepWhileInLoop"})
public final class OfferDisabler implements Runnable {

    private final GregorianCalendar calendar = new GregorianCalendar();

    private final Connection connection;

    private final long countTime = 3600000;

    private long limitTime = 0;

    private long remainingTime = 0;

    public OfferDisabler (Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run () {
        while (true) {
            limitTime = System.currentTimeMillis() + countTime;
            while (true) {
                remainingTime = limitTime - System.currentTimeMillis();
                if (remainingTime > 0) {
                    try {
                        Thread.sleep(remainingTime);
                    } catch (InterruptedException ex) {}
                } else {
                    calendar.setTime(new Date());
                    if (calendar.get(GregorianCalendar.DAY_OF_WEEK)
                     != GregorianCalendar.SUNDAY) break;
                        System.out.println(
                         "OfferDisabler: disabling offers prior to "
                         + calendar.getTime() + "\n");
                    try {
                        connection.createStatement().execute(
                         "update Offer set enabled = false where insertionDate < '"
                          + calendar.get(GregorianCalendar.YEAR) + "-"
                          + (calendar.get(GregorianCalendar.MONTH) + 1) + "-"
                          + calendar.get(GregorianCalendar.DAY_OF_MONTH) + "';");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
}

package main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import main.Main.Operation;
import main.Main.Table;

/**
 * Communicates with the HTML/Javascript server
 * providing RouteBoxer functionality.
 * @author Guilherme Alan Ritter M72642
 */
@SuppressWarnings({"CallToPrintStackTrace", "Convert2Lambda"})
public final class RouteBoxer implements Runnable {

    private final Connection connection;

    private final StringBuilder builder = new StringBuilder();

    final Semaphore flowSemaphore = new Semaphore(0, true);

    Handler handler = null;

    private int i = 0;

    private boolean in = true;

    static RouteBoxer instance = null;

    private final Semaphore loopSemaphore = new Semaphore(0, true);

    private final LinkedList<Match> missingMatches = new LinkedList<>();

    private final LinkedList<Integer> offerIds = new LinkedList<>();

    private final LinkedList<Offer> offers = new LinkedList<>();

    private Origin origin = null;

    private final HashMap<Integer, Origin> originHashMap = new HashMap<>();

    private final LinkedList<Integer> originIds = new LinkedList<>();

    private RouteBoxerRequest request = null;

    private final List<RouteBoxerRequest> requests
     = Collections.synchronizedList(new LinkedList<RouteBoxerRequest>());

    String response = null;

    private ResultSet resultSet = null;

    private static int resultSetSize = 0;

    private final LinkedList<RouteBox> routeBoxes = new LinkedList<>();

    private final RouteBoxerServer routeBoxerServer = new RouteBoxerServer();

    /**
     * Field separator. Horizontal tab character.
     */
    public static final String S_F = "\t";

    /**
     * Message separator. Form feed character.
     */
    public static final String S_M = "\f";

    /**
     * Row separator. Vertical tab character.
     */
    public static final String S_R = "\u000b";

    private String values[] = null;

    private <T> void buildKeyValuePairs (String columnName, LinkedList<T> columnValues) {
        for (i = 0; i < columnValues.size(); i++) {
            builder.append(columnName).append(" = ")
             .append(columnValues.get(i));
            if (i != (columnValues.size() - 1)) {
                builder.append(" or ");
            }
        }
    }

    private void createMatch () {
        System.out.println("RouteBoxer: createMatch()\n");
        builder.setLength(0);
        builder.append("SELECT Offer.id AS offerId, Origin.id AS originId ")
         .append("FROM Offer, Origin WHERE NOT EXISTS ")
         .append("(SELECT * FROM Match_ ")
         .append("WHERE Origin.id = Match_.origin ")
         .append("AND Offer.id = Match_.offer);");
        missingMatches.clear();
        try {
            resultSet = connection.createStatement()
             .executeQuery(builder.toString());
            resultSet.last();
            resultSetSize = resultSet.getRow();
            resultSet.first();
            for (i = 0; i < resultSetSize; i++) {
                missingMatches.add(new Match(
                 resultSet.getInt("offerId"), resultSet.getInt("originId")));
                resultSet.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (missingMatches.isEmpty()) return;
        builder.setLength(0);
        builder.append("SELECT id, latitude, longitude from Origin");
        originHashMap.clear();
        try {
            resultSet = connection.createStatement()
             .executeQuery(builder.toString());
            resultSet.last();
            resultSetSize = resultSet.getRow();
            resultSet.first();
            for (i = 0; i < resultSetSize; i++) {
                originHashMap.put(resultSet.getInt("id"),
                 new Origin(resultSet.getInt("id"),
                 resultSet.getDouble("latitude"),
                 resultSet.getDouble("longitude")));
                resultSet.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (originHashMap.isEmpty()) return;
        builder.setLength(0);
        builder.append("SELECT * from RouteBox");
        routeBoxes.clear();
        try {
            resultSet = connection.createStatement()
             .executeQuery(builder.toString());
            resultSet.last();
            resultSetSize = resultSet.getRow();
            resultSet.first();
            for (i = 0; i < resultSetSize; i++) {
                routeBoxes.add(new RouteBox(resultSet.getInt("offer"),
                 resultSet.getDouble("northEastLatitude"),
                 resultSet.getDouble("northEastLongitude"),
                 resultSet.getDouble("southWestLatitude"),
                 resultSet.getDouble("southWestLongitude")));
                resultSet.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (routeBoxes.isEmpty()) return;
        System.out.println("RouteBoxer: beginning tests of missing matches");
        for (Match missingMatch : missingMatches) {
            origin = originHashMap.get(missingMatch.origin);
            System.out.format("RouteBoxer: origin at %.6f %.6f\n", origin.latitude, origin.longitude);
            in = false;
            for (RouteBox rBI : routeBoxes) {
                if (missingMatch.offer != rBI.offer) continue;
                System.out.format("RouteBoxer: routeBox at %.6f %.6f and %.6f %.6f\n",
                 rBI.northEastLatitude, rBI.northEastLongitude,
                 rBI.southWestLatitude, rBI.southWestLongitude);
                if ((origin.latitude  <= rBI.northEastLatitude)
                 && (origin.latitude  >= rBI.southWestLatitude)
                 && (origin.longitude <= rBI.northEastLongitude)
                 && (origin.longitude >= rBI.southWestLongitude)) {
                    System.out.println("RouteBoxer: in");
                    in = true;
                    break;
                } else {
                    System.out.println("RouteBoxer: out");
                }
            }
            System.out.println();
            builder.setLength(0);
            builder.append("insert into Match_ (offer, origin, in_) values (")
             .append(missingMatch.offer).append(", ")
             .append(missingMatch.origin).append(", ")
             .append(in ? "true" : "false").append(");");
            try {
                connection.createStatement().execute(builder.toString());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void createRouteBox () {
        System.out.println("RouteBoxer: createRouteBox()\n");
        builder.setLength(0);
        builder.append("select id, origin, distance from Offer where id not in")
         .append("(select distinct offer from RouteBox);");
        offers.clear();
        try {
            resultSet = connection.createStatement()
             .executeQuery(builder.toString());
            resultSet.last();
            resultSetSize = resultSet.getRow();
            resultSet.first();
            for (i = 0; i < resultSetSize; i++) {
                offers.add(new Offer(resultSet.getInt("id"),
                 resultSet.getInt("origin"), resultSet.getDouble("distance")));
                resultSet.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        for (Offer offerI : offers) {
            builder.setLength(0);
            builder
             .append("select latitude, longitude from Origin where id = ")
             .append(offerI.origin).append(";");
            try {
                resultSet = connection.createStatement()
                 .executeQuery(builder.toString());
                resultSet.first();
                origin = new Origin(-1, resultSet.getDouble("latitude"),
                 resultSet.getDouble("longitude"));
            } catch (SQLException ex) {
                ex.printStackTrace();
                origin = null;
            }
            if (origin == null) continue;
            handler.send(origin.latitude + " " + origin.longitude + " "
             + offerI.distance);
            flowSemaphore.acquireUninterruptibly();
            values = response.split(" ");
            for (i = 0; i < values.length; i += 4) {
                builder.setLength(0);
                builder.append("insert into RouteBox (offer, ")
                 .append("northEastLatitude, northEastLongitude, ")
                 .append("southWestLatitude, southWestLongitude) values (")
                 .append(offerI.id).append(", ")
                 .append(values[i + 0]).append(", ")
                 .append(values[i + 1]).append(", ")
                 .append(values[i + 2]).append(", ")
                 .append(values[i + 3]).append(")");
                try {
                    connection.createStatement().execute(builder.toString());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void deleteMatch () {
        System.out.println("RouteBoxer: deleteMatch()\n");
        offerIds.clear();
        originIds.clear();
        if (request.table == Table.Offer) {
            offerIds.addAll(request.offerIds);
        } else {
            builder.setLength(0);
            builder.append("select id from Offer where ");
            buildKeyValuePairs("origin", request.originIds);
            builder.append(";");
            try {
                resultSet = connection.createStatement()
                 .executeQuery(builder.toString());
                resultSet.last();
                resultSetSize = resultSet.getRow();
                resultSet.first();
                for (i = 0; i < resultSetSize; i++) {
                    offerIds.add(resultSet.getInt(1));
                    resultSet.next();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            originIds.addAll(request.originIds);
        }
        if (!offerIds.isEmpty()) {
            builder.setLength(0);
            builder.append("delete from Match_ where ");
            buildKeyValuePairs("offer", offerIds);
            builder.append(";");
            try {
                connection.createStatement().execute(builder.toString());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        if (!originIds.isEmpty()) {
            builder.setLength(0);
            builder.append("delete from Match_ where ");
            buildKeyValuePairs("origin", originIds);
            builder.append(";");
            try {
                connection.createStatement().execute(builder.toString());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void deleteRouteBox () {
        System.out.println("RouteBoxer: deleteRouteBox()\n");
        offerIds.clear();
        if (request.table == Table.Offer) {
            offerIds.addAll(request.offerIds);
        } else {
            builder.setLength(0);
            builder.append("select id from Offer where ");
            buildKeyValuePairs("origin", request.originIds);
            builder.append(";");
            try {
                resultSet = connection.createStatement()
                 .executeQuery(builder.toString());
                resultSet.last();
                resultSetSize = resultSet.getRow();
                resultSet.first();
                for (i = 0; i < resultSetSize; i++) {
                    offerIds.add(resultSet.getInt(1));
                    resultSet.next();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        if (offerIds.isEmpty()) return;
        builder.setLength(0);
        builder.append("delete from RouteBox where ");
        buildKeyValuePairs("offer", offerIds);
        builder.append(";");
        try {
            connection.createStatement().execute(builder.toString());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    final void request (RouteBoxerRequest request) {
        System.out.println("RouteBoxer: received request on table"
         + request.table + " with operation " + request.operation + "\n");
        requests.add(request);
        loopSemaphore.release();
    }

    @Override
    public void run () {
        System.out.println("RouteBoxer: running\n");
        RouteBoxer.instance = this;
        new Thread(routeBoxerServer).start();
        while (true) {
            if (requests.isEmpty()) {
                loopSemaphore.acquireUninterruptibly();
            } else {
                System.out.println("RouteBoxer: processing request\n");
                request = requests.remove(0);
                if (request.operation == Operation.POST) {
                    if (request.table == Table.Offer) {
                        createRouteBox();
                        createMatch();
                    } else {
                        createMatch();
                    }
                } else {
                    if (request.table == Table.Offer) {
                        deleteRouteBox();
                        deleteMatch();
                        createRouteBox();
                        createMatch();
                    } else {
                        deleteRouteBox();
                        deleteMatch();
                        createRouteBox();
                        createMatch();
                    }
                }
            }
        }
    }

    final void setHandler (Handler handler) {
        this.handler = handler;
    }

    public RouteBoxer (Connection connection) {
        this.connection = connection;
    }
}

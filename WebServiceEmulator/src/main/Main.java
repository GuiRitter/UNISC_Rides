package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/*
post = create
get = read
put = update
delete

MO
TU
WE
TH
FR
SA

M
A
N
 */

/**
 * Web service emulator.
 * @author Guilherme Alan Ritter M72642
 */
@SuppressWarnings({"CallToThreadDumpStack", "CallToPrintStackTrace",
    "ConvertToStringSwitch"})
public class Main {

    private static final LinkedList<String> columnNames = new LinkedList<>();

    private static int columnType = 0;

    private static final LinkedList<String> columnValues = new LinkedList<>();

    private static Connection connection;

    private static String dataString = null;

    private static String fields[];

    private static int i = 0;

    private static final String idFlag[] = {"id"};

    private static int j = 0;

    private static final String MESSAGE_POST = "POST";
    private static final String MESSAGE_GET = "GET";
    private static final String MESSAGE_PUT = "PUT";
    private static final String MESSAGE_DELETE = "DELETE";

    private static final String MESSAGE_ACK = "ack";
    private static final String MESSAGE_NACK = "nack";
    private static final String MESSAGE_UNKNOWN_PACKET_TYPE = "unknown_message_type";
    private static final String MESSAGE_WRONG_FIELD_AMOUNT = "wrong_field_amount";
    private static final String MESSAGE_WRONG_ROW_AMOUNT = "wrong_row_amount";

    private static int messageI = 0;

    private static String messageType = null;

    private static String messages[] = null;

    private static Operation operation = Operation.OTHER;

    private static PrintWriter out;

    static int port = 0;

    private static final StringBuilder queryBuilder = new StringBuilder();

    private static ServerRequest request = null;

    private static final List<ServerRequest> requests
     = Collections.synchronizedList(new LinkedList<ServerRequest>());

    private static final StringBuilder responseBuilder = new StringBuilder();

    private static ResultSet resultSet = null;

    private static ResultSetMetaData resultSetMetaData = null;

    private static int resultSetSize = 0;

    private static int rowI = 0;

    private static RouteBoxer routeBoxer;

    private static RouteBoxerRequest routeBoxerRequest = null;

    private static String rows[] = null;

    /**
     * Separator: field.
     */
    private static final String S_F = "\t";

    /**
     * Separator: message.
     */
    private static final String S_M = "\f";

    /**
     * Separator: row.
     */
    private static final String S_R = "\u000b";// \v

    private static final StringBuilder selectKeyValuePairs = new StringBuilder();

    private static final Semaphore semaphore = new Semaphore(0, true);

    private static Statement statement = null;

    private static String table = null;

    private static Table table_ = Table.OTHER;

    private static boolean updatedOffer = false;

    private static boolean updatedOrigin = false;

    private static int x = 0;

    private static double xD = 0;

    enum Operation {
        OTHER,
        POST,
        PUT
    }

    enum Table {
        Offer,
        Origin,
        OTHER
    }

    private static void buildKeyValuePairs (String separator) {
        selectKeyValuePairs.setLength(0);
        for (i = 0; i < columnNames.size(); i++) {
            selectKeyValuePairs.append(columnNames.get(i))
             .append(" = ").append(columnValues.get(i));
            if (i != (columnNames.size() - 1)) {
                selectKeyValuePairs.append(separator);
            }
        }
        queryBuilder.append(selectKeyValuePairs);
    }

    /**
     * TO DO only SELECT Offer and Origin updates
     * that change origin, distance, latitude or longitude
     * @param query
     */
    private static void executeSQL (boolean query) {
        try {
            System.out.println("executing");
            System.out.println(queryBuilder.toString());
            System.out.println();
            if (query) {
                resultSet = connection.createStatement()
                 .executeQuery(queryBuilder.toString());
            } else {
                if ((operation == Operation.OTHER) || (table_ == Table.OTHER)) {
                    connection.createStatement().execute(queryBuilder.toString());
                } else {
                    if (operation == Operation.POST) {
                        statement = connection.createStatement();
                        statement.execute(queryBuilder.toString(), idFlag);
                        resultSet = statement.getGeneratedKeys();
                    } else if (updatedOffer || updatedOrigin) {
                        connection.createStatement()
                         .execute(queryBuilder.toString());
                        queryBuilder.setLength(0);
                        queryBuilder.append("select id from ").append(table_)
                         .append(" where ")
                         .append(selectKeyValuePairs.toString()).append(";");
                        resultSet = connection.createStatement()
                         .executeQuery(queryBuilder.toString());
                    } else {
                        connection.createStatement()
                         .execute(queryBuilder.toString());
                    }
                    if ((operation == Operation.POST)
                     || updatedOffer || updatedOrigin) {
                        routeBoxerRequest =
                         new RouteBoxerRequest(operation, table_);
                        resultSet.last();
                        resultSetSize = resultSet.getRow();
                        resultSet.first();
                        for (i = 0; i < resultSetSize; i++) { // row
                                j = resultSet.getInt(1);
                            if (table_ == Table.Offer) {
                                routeBoxerRequest.offerIds.add(j);
                            } else {
                                routeBoxerRequest.originIds.add(j);
                            }
                            resultSet.next();
                        }
                        routeBoxer.request(routeBoxerRequest);
                    }
                }
            }
            System.out.println(MESSAGE_ACK + "\n");
            if (!query) {
                responseBuilder.append(MESSAGE_ACK);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(MESSAGE_NACK + "\n");
            responseBuilder.append(MESSAGE_NACK);
        }
    }

    private static void readPairs () {
        columnNames.clear();
        columnValues.clear();
        for (i = 0; i < fields.length; i += 2) {
            columnNames.add(fields[i]);
            columnValues.add(fields[i + 1]);
        }
    }

    static void request (ServerRequest request) {
        requests.add(request);
        semaphore.release();
    }

    private static void sendResponse (String message) {
        System.out.println(showControl(message) + "\n");
        out.println(message);
    }

    private static void sendResponse (Exception ex, String message) {
        ex.printStackTrace();
        sendResponse(message);
    }

    static String showControl (String input) {
        return input.replace(S_M, "\u240C")
         .replace(S_R, "\u240B").replace(S_F, "\u2409");
    }

    public static void main (String args[]) throws IOException, SQLException {
        if (args.length < 4) {
            System.err.println("Usage: port database SQL_user SQL_password");
            System.exit(0);
        }
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        System.out.println("preparing\n");
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/"
         + args[1] + "?user=" + args[2] + "&password=" + args[3]);
        new Thread(new OfferDisabler(DriverManager.getConnection("jdbc:mysql://localhost:3306/"
         + args[1] + "?user=" + args[2] + "&password=" + args[3]))).start();
        new Thread(routeBoxer = new RouteBoxer(DriverManager.getConnection("jdbc:mysql://localhost:3306/"
         + args[1] + "?user=" + args[2] + "&password=" + args[3]))).start();
        new Thread(new Server()).start();
        while (true) {
            if (requests.isEmpty()) {
                semaphore.acquireUninterruptibly();
            } else {
                request = requests.remove(0);
                dataString = request.data;
                out = request.out;
                responseBuilder.setLength(0);
                messages = dataString.split(S_M);
                messagesLoop:
                for (messageI = 0; messageI < messages.length; messageI++) {
                    if (messageI != 0) {
                        responseBuilder.append(S_M);
                    }
                    rows = messages[messageI].split(S_R);
                    if ((rows.length < 2) || (rows.length > 3)) {
                        responseBuilder.append(MESSAGE_WRONG_ROW_AMOUNT);
                        if (messageI != (messages.length - 1)) {
                            responseBuilder.append(S_M);
                        }
                        continue;
                    }
                    for (rowI = 0; rowI < rows.length; rowI++) {
                        fields = rows[rowI].split(S_F);
                        if ((fields.length < 2) || ((fields.length % 2) != 0)) {
                            responseBuilder.append(MESSAGE_WRONG_FIELD_AMOUNT);
                            if (messageI != (messages.length - 1)) {
                                responseBuilder.append(S_M);
                            }
                            continue messagesLoop;
                        }
                        if (rowI == 0) {
                            messageType = fields[0];
                            table = fields[1];
                            if (table.compareToIgnoreCase(
                             Table.Offer.name()) == 0) {
                                table_ = Table.Offer;
                            } else if (table.compareToIgnoreCase(
                             Table.Origin.name()) == 0){
                                table_ = Table.Origin;
                            } else {
                                table_ = Table.OTHER;
                            }
                        } else {
                            readPairs();
                            if (rowI == 1) {
                                if (messageType.equals(MESSAGE_POST)) {
                                    operation = Operation.POST;
                                    queryBuilder.setLength(0);
                                    queryBuilder.append("insert into ")
                                     .append(table).append(" (");
                                    for (i = 0; i < columnNames.size(); i++) {
                                        queryBuilder.append(columnNames.get(i));
                                        if (i != (columnNames.size() - 1)) {
                                            queryBuilder.append(", ");
                                        }
                                    }
                                    queryBuilder.append(") values (");
                                    for (i = 0; i < columnValues.size(); i++) {
                                        queryBuilder.append(columnValues.get(i));
                                        if (i != (columnValues.size() - 1)) {
                                            queryBuilder.append(", ");
                                        }
                                    }
                                    queryBuilder.append(");");
                                    executeSQL(false);
                                } else if (messageType.equals(MESSAGE_GET)) {
                                    operation = Operation.OTHER;
                                    queryBuilder.setLength(0);
                                    queryBuilder.append("select * from ")
                                     .append(table).append(" where ");
                                    buildKeyValuePairs(" AND ");
                                    queryBuilder.append(";");
                                    executeSQL(true);
                                    resultSet.last();
                                    resultSetSize = resultSet.getRow();
                                    responseBuilder.append(MESSAGE_GET)
                                     .append(S_F).append(table);
                                    resultSet.first();
                                    for (i = 0; i < resultSetSize; i++) { // rows
                                        responseBuilder.append(S_R);
                                        resultSetMetaData = resultSet.getMetaData();
                                        for (j = 1; j <= resultSetMetaData.getColumnCount(); j++) { // columns
                                            responseBuilder
                                             .append(resultSetMetaData.getColumnName(j))
                                             .append(S_F);
                                            columnType = resultSetMetaData.getColumnType(j);
                                            switch (columnType) {
                                            case Types.DATE:
                                                responseBuilder.append("'").append(resultSet.getDate(j)).append("'");
                                            break; case Types.TIME:
                                                responseBuilder.append("'").append(resultSet.getTime(j)).append("'");
                                            break; case Types.LONGVARCHAR:
                                                responseBuilder.append("'").append(resultSet.getString(j)).append("'");
                                            break; case Types.DOUBLE:
                                                xD = resultSet.getDouble(j);
                                                if (resultSet.wasNull()) {
                                                    responseBuilder.append("null");
                                                } else {
                                                    responseBuilder.append(xD);
                                                }
                                            break; /* case Types.INTEGER */ default:
                                                x = resultSet.getInt(j);
                                                if (resultSet.wasNull()) {
                                                    responseBuilder.append("null");
                                                } else {
                                                    responseBuilder.append(x);
                                                }   break;
                                            }
                                            if (j != resultSetMetaData.getColumnCount()) {
                                                responseBuilder.append(S_F);
                                            }
                                        }
                                        resultSet.next();
                                    }
                                } else if (messageType.equals(MESSAGE_PUT)) {
                                    operation = Operation.PUT;
                                    if (rows.length == 2) {
                                        responseBuilder.append(MESSAGE_WRONG_ROW_AMOUNT);
                                        if (messageI != (messages.length - 1)) {
                                            responseBuilder.append(S_M);
                                        }
                                        continue messagesLoop;
                                    }
                                    queryBuilder.setLength(0);
                                    queryBuilder.append("update ")
                                     .append(table).append(" set ");
                                    buildKeyValuePairs(", ");
                                    updatedOffer = (table_ == Table.Offer)
                                     && (columnNames.contains("origin")
                                     || (columnNames.contains("distance")));
                                    updatedOrigin = (table_ == Table.Origin)
                                     && (columnNames.contains("latitude")
                                     || (columnNames.contains("longitude")));
                                } else if (messageType.equals(MESSAGE_DELETE)) {
                                    operation = Operation.OTHER;
                                    queryBuilder.setLength(0);
                                    queryBuilder.append("delete from ")
                                     .append(table).append(" where ");
                                    buildKeyValuePairs(" AND ");
                                    queryBuilder.append(";");
                                    executeSQL(false);
                                } else {
                                    operation = Operation.OTHER;
                                    responseBuilder
                                     .append(MESSAGE_UNKNOWN_PACKET_TYPE);
                                    if (messageI != (messages.length - 1)) {
                                        responseBuilder.append(S_M);
                                    }
                                    continue messagesLoop;
                                }
                            } else {
                                if (messageType.equals(MESSAGE_PUT)) {
                                    queryBuilder.append(" where ");
                                    buildKeyValuePairs(" AND ");
                                    queryBuilder.append(";");
                                    executeSQL(false);
                                } else {
                                    responseBuilder
                                     .append(MESSAGE_UNKNOWN_PACKET_TYPE);
                                    if (messageI != (messages.length - 1)) {
                                        responseBuilder.append(S_M);
                                    }
                                    continue messagesLoop;
                                }
                            }
                        }
                    }
                }
                sendResponse(responseBuilder.toString());
            }
        }
    }
}//https://wportal.unisc.br:6082/php/uid.php?vsys=1&url=http://www.google.com.br%2f
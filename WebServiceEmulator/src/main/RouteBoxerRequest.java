package main;

import java.util.LinkedList;

/**
create offer
	create routebox
	create match
create origin
	create match
update offer
	delete routebox
	delete match
	create routebox
	create match
update origin
	delete routebox depends on offer
	delete match both depends on offer and don't
	create routebox
	create match
 * @author Guilherme Alan Ritter M72642
 */
final class RouteBoxerRequest {

    final LinkedList<Integer> offerIds = new LinkedList<>();

    final Main.Operation operation;

    final LinkedList<Integer> originIds = new LinkedList<>();

    final Main.Table table;

    public RouteBoxerRequest(Main.Operation operation, Main.Table table) {
        this.operation = operation;
        this.table = table;
    }
}

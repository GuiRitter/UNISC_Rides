package main;

/**
 * @author Guilherme Alan Ritter
 */
public final class RouteBox {

    final double northEastLatitude;

    final double northEastLongitude;

    final int offer;

    final double southWestLatitude;

    final double southWestLongitude;

    public RouteBox(int offer,
     double northEastLatitude, double northEastLongitude,
     double southWestLatitude, double southWestLongitude) {
        this.northEastLatitude  = northEastLatitude ;
        this.northEastLongitude = northEastLongitude;
        this.offer              = offer             ;
        this.southWestLatitude  = southWestLatitude ;
        this.southWestLongitude = southWestLongitude;
    }
}

package br.com.een_gmb_gar.unisc_rides;

/**
 * Created by eenagel on 10/18/15.
 */
public class MatchData {
    Match match;
    Offer offer;
    Person person;
    Origin origin;

    MatchData(){

    }

    MatchData(Offer offer, Match match, Person person, Origin origin) {
        this.match = match;
        this.offer = offer;
        this.person = person;
        this.origin = origin;
    }
}
package br.com.een_gmb_gar.unisc_rides;

/**
 * Created by nuts on 10/7/15.
 */
public class Data {
    public int reg_id;
    public String user_name; // shifts
    public String full_status; //

    public Data(int id, String name, String status){
        this.reg_id = id;
        this.user_name = name;
        this.full_status = status;
    }
}

package br.com.een_gmb_gar.unisc_rides;

import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.S_F;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.u;
import static br.com.een_gmb_gar.unisc_rides.WebServiceInterface.w;

public final class Login {

    final String COLUMN_PASSWORD = "password_";
    final String COLUMN_PERSON   = "person";
    final String COLUMN_USERNAME = "username";

    String password = null;

    int person = -1;

    String username = null;

    public Login () {
    }

    public Login (int person, String username, String password) {
        this.person   = person;
        this.username = username;
        this.password = password;
    }

    public Login (Login login) {
        this.person   = login.person;
        this.username = login.username;
        this.password = login.password;
    }

    public Login (String keyValuePairs) throws Exception {
        String fields[] = keyValuePairs.split(S_F);
        for (int i = 0; i < fields.length; i += 2) {
            switch (fields[i]) {
            case COLUMN_PERSON:
                person = Integer.parseInt(fields[i + 1]);
            break; case COLUMN_USERNAME:
                username = u(fields[i + 1]);
            break; case COLUMN_PASSWORD:
                password = u(fields[i + 1]);
            break; default:
                throw new Exception();
            }
        }
    }

    final String getKeyValuePairs () {
        String returnString =
         (isPersonNull()   ? "" : (COLUMN_PERSON   + S_F + person      + S_F)) +
         (isUsernameNull() ? "" : (COLUMN_USERNAME + S_F + w(username) + S_F)) +
         (isPasswordNull() ? "" : (COLUMN_PASSWORD + S_F + w(password) + S_F));
        return (returnString.length() < 1) ? null
         : (returnString.substring(0, returnString.length() - 1));
    }

    final boolean isNull () {
        return isPersonNull() && isUsernameNull() && isPasswordNull();
    }

    final boolean isPasswordNull () {
        return password == null;
    }

    final boolean isPersonNull () {
        return person < 0;
    }

    final boolean isUsernameNull () {
        return username == null;
    }

    @Override
    public String toString () {
        return "Login\n" + 
         "Person: "   + person   + "\n" +
         "Username: " + username + "\n" +
         "Password: " + password + "\n";
    }
}

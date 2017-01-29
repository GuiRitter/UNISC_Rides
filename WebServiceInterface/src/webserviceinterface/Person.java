package webserviceinterface;

import static webserviceinterface.WebServiceInterface.S_F;
import static webserviceinterface.WebServiceInterface.u;
import static webserviceinterface.WebServiceInterface.w;

public final class Person {

    final String COLUMN_EMAIL = "email";
    final String COLUMN_ID    = "id";
    final String COLUMN_IP    = "IP";
    final String COLUMN_NAME  = "name_";
    final String COLUMN_PHONE = "phone";

    String email = null;

    int id = -1;

    String IP = null;

    String name = null;

    String phone = null;

    public Person () {
    }

    public Person (int id, String name, String phone, String email, String IP) {
        this.id    = id;
        this.name  = name;
        this.phone = phone;
        this.email = email;
        this.IP    = IP;
    }

    public Person (Person person) {
        this.id    = person.id;
        this.name  = person.name;
        this.phone = person.phone;
        this.email = person.email;
        this.IP    = person.IP;
    }

    public Person  (String keyValuePairs) throws Exception {
        String fields[] = keyValuePairs.split(S_F);
        for (int i = 0; i < fields.length; i += 2) {
            switch (fields[i]) {
            case COLUMN_ID:
                id = Integer.parseInt(fields[i + 1]);
            break; case COLUMN_NAME:
                name = u(fields[i + 1]);
            break; case COLUMN_PHONE:
                if (fields[i + 1].equals("'null'")) {
                    phone = null;
                } else {
                    phone = u(fields[i + 1]);
                }
            break; case COLUMN_EMAIL:
                if (fields[i + 1].equals("'null'")) {
                    email = null;
                } else {
                    email = u(fields[i + 1]);
                }
            break; case COLUMN_IP:
                if (fields[i + 1].equals("'null'")) {
                    IP = null;
                } else {
                    IP = u(fields[i + 1]);
                }
            break; default:
                throw new Exception();
            }
        }
    }

    final String getKeyValuePairs () {
        String returnString =
         (isIdNull()    ? "" : (COLUMN_ID    + S_F + id       + S_F)) +
         (isNameNull()  ? "" : (COLUMN_NAME  + S_F + w(name)  + S_F)) +
         (isPhoneNull() ? "" : (COLUMN_PHONE + S_F + w(phone)    + S_F)) +
         (isEmailNull() ? "" : (COLUMN_EMAIL + S_F + w(email) + S_F)) +
         (isIPNull()    ? "" : (COLUMN_IP    + S_F + w(IP)    + S_F));
        return (returnString.length() < 1) ? null
         : (returnString.substring(0, returnString.length() - 1));
    }

    final boolean isEmailNull () {
        return email == null;
    }

    final boolean isIdNull () {
        return id < 0;
    }

    final boolean isIPNull () {
        return IP == null;
    }

    final boolean isNameNull () {
        return name == null;
    }

    final boolean isNull (boolean considerId) {
        return (considerId ? isIdNull(): true) && isNameNull()
         && isPhoneNull() && isEmailNull() && isIPNull();
    }

    final boolean isPhoneNull () {
        return phone == null;
    }

    @Override
    public String toString () {
        return "Person\n" +
         "ID: "    + id    + "\n" +
         "Name: "  + name  + "\n" +
         "Phone: " + phone + "\n" +
         "Email: " + email + "\n" +
         "IP: "    + IP    + "\n";
    }
}

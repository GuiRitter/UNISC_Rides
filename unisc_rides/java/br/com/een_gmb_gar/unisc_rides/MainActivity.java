package br.com.een_gmb_gar.unisc_rides;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Client.initialize();
        setContentView(R.layout.activity_main);
    }

    public void bt_login(View view) {
        EditText username = (EditText) findViewById(R.id.insert_user);
        EditText password = (EditText) findViewById(R.id.insert_pw);

        ArrayList<Login> array_login = new ArrayList<>();
        Login login = new Login(-1, username.getText().toString(), password.getText().toString());
        //Search for the login
        //If it returns correctly

        if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            Log.d("bt_login", "login data fault");
            Toast.makeText(this, "Invalid username/password!", Toast.LENGTH_SHORT).show();
        }else{
            Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_LOGIN, new Object[]{login, array_login});

            if (Client.socket.getRc() && array_login.size() == 1) {
                Log.d("bt_login", "user found");
                Intent intent = new Intent(this, UserMain.class);
                intent.putExtra("user_id", array_login.get(0).person);
                startActivity(intent);
            } else {
                Log.d("bt_login", "user not found");
                Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void bt_register(View view) {
        EditText username = (EditText) findViewById(R.id.insert_user);
        EditText password = (EditText) findViewById(R.id.insert_pw);

        if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            Log.d("bt_register", "invalid fields");
            Toast.makeText(this, "Username or password are empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        String usernameString = username.getText().toString();
        String passwordString = password.getText().toString();
        Login login = new Login(-1, usernameString, passwordString);
        ArrayList<Login> logins = new ArrayList<>();
        Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_LOGIN, new Object[]{login, logins});
        if (Client.socket.getRc()) {
            if (logins.isEmpty()) {
                Intent intent = new Intent(this, RegisterUser.class);
                intent.putExtra("user_login", usernameString);
                intent.putExtra("user_password", passwordString);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Username and password already exist.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Couldn't connect to database.", Toast.LENGTH_LONG).show();
        }
    }
}
package br.com.een_gmb_gar.unisc_rides;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class RegisterUser extends Activity {

    LoginData login = new LoginData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        Bundle bundle = getIntent().getExtras();
        login.user_login = bundle.get("user_login").toString();
        login.user_password = bundle.get("user_password").toString();
    }

    public void bt_register_user(View view) {

        Log.d("bt_register_user", "enter");

        //Pick the EditText fields
        EditText name = (EditText) findViewById(R.id.insert_name);
        EditText phone = (EditText) findViewById(R.id.insert_phone);
        EditText email = (EditText) findViewById(R.id.insert_email);

        //Pick up the data
        if(!name.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !phone.getText().toString().isEmpty()) {

            login.user_name = name.getText().toString();
            login.user_email = email.getText().toString();
            login.user_phone = phone.getText().toString();

            Person personDb = new Person(-1, login.user_name, login.user_phone, login.user_email, Client.IP);

            Client.socket.send(Client.Cmds.CMD_CREATE, Client.Tables.TB_PERSON, new Object[]{personDb});

            if(Client.socket.getRc()){
                ArrayList<Person> persons = new ArrayList<>();

                Client.socket.send(Client.Cmds.CMD_READ, Client.Tables.TB_PERSON, new Object[]{personDb, persons});

                Log.d("bt_login", "RC2: " + Client.socket.getRc()+" size "+persons.size());
                if(Client.socket.getRc() && persons.size()==1){
                    Login loginDb = new Login(persons.get(0).id, login.user_login, login.user_password);


                    Client.socket.send(Client.Cmds.CMD_CREATE, Client.Tables.TB_LOGIN, new Object[]{loginDb});
                    Log.d("bt_login", "RC2: "+Client.socket.getRc());
                    if(Client.socket.getRc()){
                        Log.d("bt_login", "create login");
                        Intent intent = new Intent(this, UserMain.class);
                        intent.putExtra("user_id", persons.get(0).id);
                        startActivity(intent);
                        Log.d("bt_login", "exit");
                    }else{
                        Toast.makeText(this,"Connection error 3.",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this,"Connection error 2.",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this,"Connection error 1.",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
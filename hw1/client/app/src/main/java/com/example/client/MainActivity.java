package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    Socket socket=null;
    DataOutputStream sender=null;
    String name,ip;
    int port;

    public byte[] getJsonString(String name, String message){
        Map<String,String> map = new HashMap<>();
        map.put("name",name);
        map.put("message",message);
        JSONObject json = new JSONObject(map);
        return (json.toString()+"\n").getBytes();
    }

    public void sendOnclick(View view) throws IOException {
        EditText chatText=(EditText)findViewById(R.id.chatText);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sender.write(getJsonString(name,chatText.getText().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        chatText.setText("");
    }

    public void leaveOnclick(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sender.write(getJsonString("",name+" has left."));
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        setContentView(R.layout.activity_main);
    }

    public void onConnectButtonClick(View view) {
        EditText nameInput=(EditText)findViewById(R.id.editTextName);
        name=nameInput.getText().toString();
        EditText ipInput=(EditText)findViewById(R.id.editTextIP);
        ip=ipInput.getText().toString();
        EditText portInput=(EditText)findViewById(R.id.editTextPort);
        port=Integer.parseInt(portInput.getText().toString());

        setContentView(R.layout.page2);

        TextView nameView=(TextView)findViewById(R.id.nameView);
        nameView.setText("Hi! "+name+"\n");

        TextView chatTextView=(TextView)findViewById(R.id.chatTextView);
        chatTextView.setText("Hi! "+name+"\n");

        Runnable handlesocket=new Runnable() {

            @Override
            public void run() {


                try {
                    socket = new Socket(ip, port);


                    if (socket.isConnected()) {


                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            sender = new DataOutputStream(socket.getOutputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            sender.write(getJsonString(name, "(" + socket.getLocalAddress() + ":" + socket.getLocalPort() + ") Connected"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            sender.write(getJsonString("", "Welcome " + name + " join us!"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String tmp = "";
                        while (true) {
                            try {
                                if (!((tmp = reader.readLine()) != null)) break;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            JSONObject obj = null;

                            try {
                                obj = new JSONObject(tmp);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            JSONObject finalObj = obj;
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                chatTextView.append(finalObj.getString("name") + ": " + finalObj.getString("message") + "\n");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                            );
                        }
                    } else {
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        setContentView(R.layout.activity_main);
                                    }
                                });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(
                handlesocket
        ).start();

    }
}
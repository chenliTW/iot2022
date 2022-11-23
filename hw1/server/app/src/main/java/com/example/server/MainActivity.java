package com.example.server;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private String name="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static String getLocalIpAddress(){
        try{
            for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();en.hasMoreElements();){
                NetworkInterface intf = en.nextElement();
                for(Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();enumIpAddr.hasMoreElements();){
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if(!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.getHostAddress().indexOf(':')<0){
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex){

        }
        return null;
    }


    ServerSocket server;
    Socket socket;

    Vector<DataOutputStream> connections_send=new Vector();

    public byte[] getJsonString(String name, String message){
        Map<String,String> map = new HashMap<>();
        map.put("name",name);
        map.put("message",message);
        JSONObject json = new JSONObject(map);
        return (json.toString()+"\n").getBytes();
    }

    public void broadcast(String name,String message) throws IOException {
        for(int i=0;i<connections_send.size();i++){
            try {
                connections_send.get(i).write(getJsonString(name,message));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    public void startButtonOnClick(View view) throws IOException, JSONException {
        EditText nameInput=(EditText)findViewById(R.id.nameInput);
        name=nameInput.getText().toString();
        setContentView(R.layout.page2);
        TextView nameView=(TextView)findViewById(R.id.nameView);
        nameView.setText("Hi! "+name);
        TextView chatTextView=(TextView)findViewById(R.id.chatTextView);

        chatTextView.setText("");
        chatTextView.append("Server start:("+getLocalIpAddress()+":7100)\n");

        server = new ServerSocket(7100);

        Runnable handlesocket=new Runnable() {

            @Override
            public void run() {

                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    connections_send.add(out);
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
                    JSONObject obj=null;

                    try {
                        obj = new JSONObject(tmp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSONObject finalObj = obj;
                    try {
                        if(finalObj.getString("name").length()==0){
                            finalObj.put("name",name);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(
                        new Runnable() {
                          @Override
                          public void run() {
                              try {
                                  chatTextView.append(finalObj.getString("name")+": " + finalObj.getString("message") + "\n");
                              } catch (JSONException e) {
                                  e.printStackTrace();
                              }
                          }
                        }
                    );
                    try {
                        if(!finalObj.getString("message").contains(") Connected")) {
                            broadcast(finalObj.getString("name"), finalObj.getString("message"));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        socket = server.accept();
                        new Thread(
                                handlesocket
                        ).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public void sendOnClick(View view) throws IOException {
        EditText chatText=(EditText)findViewById(R.id.chatText);

        TextView chatTextView=(TextView)findViewById(R.id.chatTextView);
        chatTextView.append(name+": "+ chatText.getText().toString()+"\n");


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    broadcast(name, chatText.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        chatText.setText("");

    }

    public void onLeaveClicked(View view) throws IOException {
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    broadcast(name,"server closed. Please press Leave button.");
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
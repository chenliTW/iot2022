package com.example.iot06;

import androidx.appcompat.app.AppCompatActivity;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.WheelLights;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class MainActivity extends RobotActivity{

    public MainActivity(RobotCallback robotCallback,RobotCallback.Listen robotListenCallback){
        super(robotCallback,robotListenCallback);
    }

    ServerSocket server;
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            server = new ServerSocket(7200);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                        if(finalObj.getString("action").equals("look")){
                            robotAPI.utility.lookAtUser(0);
                        }
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
    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {

        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);

            Log.d("RobotDevSample", "onResult:"
                    + RobotCommand.getRobotCommand(cmd).name()
                    + ", serial:" + serial + ", err_code:" + err_code
                    + ", result:" + result.getString("RESULT"));
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };


    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public MainActivity(){
        super(robotCallback, robotListenCallback);
    }
}
package com.tonysmarthome.lightcontrol;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private final int SHOW_RESPONSE=0;
    private final int SHOW_JSON=1;
    private EditText editIP=null;
    public TextView tvReturnData=null;
    public EditText editText=null;
    public String myURL="http://172.16.20.241:5000/light";
    public String sLightPath="";
    //public String sLightPath="http://172.16.20.241:5000/light";

    public final String NETWORK_OK="Network is OK";
    public final String NETWORK_UNAVAILABLE="Network is unavailable, please check!";
    public final String DATA_OR_PATH_ERROR="Web server error or return data error, please check web server ip address!";

    public boolean isRedOn=false;
    public boolean isGreenOn=false;
    public boolean isYellowOn=false;

    public Button btnRed;
    public Button btnGreen;
    public Button btnYellow;

    //broadcast to detect network status change
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private int broadCastCounter=0;

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SHOW_RESPONSE:
                    String response=(String)msg.obj;
                    tvReturnData.setText(response);

                    break;
                case SHOW_JSON:
                    response=(String)msg.obj;
                    String jsonTxt=parseJsonWithJsonObject(response);
                    tvReturnData.setText(jsonTxt);
                    btnRed.setEnabled(true);
                    btnGreen.setEnabled(true);
                    btnYellow.setEnabled(true);

                    if(isRedOn){
                        btnRed.setText("Red Light Off");
                        btnRed.setBackgroundColor(Color.RED);
                    }
                    else{
                        btnRed.setText("Red Light On");
                        btnRed.setBackgroundColor(Color.parseColor("#d6d7d7"));
                    }
                    if(isGreenOn){
                        btnGreen.setText("Green Light Off");
                        btnGreen.setBackgroundColor(Color.GREEN);
                    }
                    else{
                        btnGreen.setText("Green Light On");
                        btnGreen.setBackgroundColor(Color.parseColor("#d6d7d7"));
                    }
                    if(isYellowOn){
                        btnYellow.setText("Yellow Light Off");
                        btnYellow.setBackgroundColor(Color.YELLOW);
                    }
                    else{
                        btnYellow.setText("Yellow Light On");
                        btnYellow.setBackgroundColor(Color.parseColor("#d6d7d7"));
                    }

                default:
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        btnRed=(Button)findViewById(R.id.btnRed);
        btnGreen=(Button)findViewById(R.id.btnGreen);
        btnYellow=(Button)findViewById(R.id.btnYellow);


        Button btnStart=(Button)findViewById(R.id.btnStart);
        editIP = (EditText)findViewById(R.id.editIP);
        editIP.setText(myURL);
        tvReturnData=(TextView)findViewById(R.id.tvReturn);

        //register broadcast
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver=new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver,intentFilter);

        btnRed.setEnabled(false);
        btnGreen.setEnabled(false);
        btnYellow.setEnabled(false);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()) {
                    sendRequestWithHttpURLConnectionGet(sLightPath);
                 }
                else{
                    tvReturnData.setText(NETWORK_UNAVAILABLE);
                }
            }
        });

        btnRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRedOn=!isRedOn;//first time off
                if(isRedOn){
                    sendRequestWithHttpURLConnectionPost(sLightPath, "light=red");
                    btnRed.setText("Red Light Off");
                    btnRed.setBackgroundColor(Color.RED);
                }
                else{
                    sendRequestWithHttpURLConnectionPost(sLightPath, "light=nored");
                    btnRed.setText("Red Light On");
                    btnRed.setBackgroundColor(Color.parseColor("#d6d7d7"));
                }

            }
        });

        btnGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGreenOn=!isGreenOn;//first time off
                if(isGreenOn){
                    sendRequestWithHttpURLConnectionPost(sLightPath, "light=green");
                    btnGreen.setText("Green Light Off");
                    btnGreen.setBackgroundColor(Color.GREEN);
                }
                else{
                    sendRequestWithHttpURLConnectionPost(sLightPath, "light=nogreen");
                    btnGreen.setText("Green Light On");
                    btnGreen.setBackgroundColor(Color.parseColor("#d6d7d7"));
                }

            }
        });


        btnYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYellowOn=!isYellowOn;//first time off
                if(isYellowOn){
                    sendRequestWithHttpURLConnectionPost(sLightPath, "light=yellow");
                    btnYellow.setText("Yellow Light Off");
                    btnYellow.setBackgroundColor(Color.YELLOW);
                }
                else{
                    sendRequestWithHttpURLConnectionPost(sLightPath, "light=noyellow");
                    btnYellow.setText("Yellow Light On");
                    btnYellow.setBackgroundColor(Color.parseColor("#d6d7d7"));
                }

            }
        });

    }



    private  void sendRequestWithHttpURLConnectionPost(final  String sUrl,final String strPost){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                try {

                    if(sUrl.equals("")){
                        String str=editIP.getText().toString();
                        if(!str.isEmpty()) {
                            myURL = str;
                        }
                    }
                    else{
                        myURL=sUrl;
                    }


                    URL url=new URL(myURL);
                    connection=(HttpURLConnection)url.openConnection();
                    //begin post
                    connection.setRequestMethod("POST");

                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                    connection.setRequestProperty("Charset", "UTF-8");

                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());

                    out.writeBytes(strPost);
                    out.flush();
                    out.close();

                    int returnCode=connection.getResponseCode();
                    if(HttpURLConnection.HTTP_OK==returnCode){
                        StringBuffer sb=new StringBuffer();
                        String readLine=new String();
                        BufferedReader responseReader=new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
                        while((readLine=responseReader.readLine())!=null){
                            sb.append(readLine).append("\n");
                        }
                        responseReader.close();
                        Message message=new Message();
                        message.what=SHOW_RESPONSE;
                        //message.what=SHOW_JSON;//test json
                        message.obj=sb.toString();
                        handler.sendMessage(message);

                    }

                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }



    private  void sendRequestWithHttpURLConnectionGet(final String strUlr){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                try {

                    if(strUlr.equals("")){
                        String str=editIP.getText().toString();
                        if(!str.isEmpty()){
                            myURL=str;
                        }
                    }
                    else{
                        myURL=strUlr;
                    }



                    URL url=new URL(myURL);
                    connection=(HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(8000);
                    connection.setReadTimeout(8000);

                    InputStream in=connection.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    StringBuilder response=new StringBuilder();
                    String line;

                    while((line=reader.readLine())!=null){
                        response.append(line);
                    }

                    Message message=new Message();
                    //message.what=SHOW_RESPONSE;
                    message.what=SHOW_JSON;//test json
                    message.obj=response.toString();
                    handler.sendMessage(message);

                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }



    private String parseJsonWithJsonObject(String jsonData){

        String result="";
        try {

            JSONArray jsonArray=new JSONArray(jsonData);
            String red="",green="",yellow="";
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                red=jsonObject.getString("red");
                green=jsonObject.getString("green");
                yellow=jsonObject.getString("yellow");
            }

            if(red.equals("on"))
                isRedOn=true;
            else
                isRedOn=false;

            if(green.equals("on"))
                isGreenOn=true;
            else
                isGreenOn=false;

            if(yellow.equals("on"))
                isYellowOn=true;
            else
                isYellowOn=false;

            result="Red light is "+red+"\n"+
                    "Green light is "+green+"\n"+
                    "Yellow light is "+yellow+"\n";
        }catch (JSONException e){
            e.printStackTrace();
            result =DATA_OR_PATH_ERROR;
        }
        return result;
    }

    //broadcast receiver
    class NetworkChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            if(broadCastCounter==0){
                broadCastCounter++;
                return;
            }
            if(isNetworkAvailable()){
                btnGreen.setEnabled(true);
                btnRed.setEnabled(true);
                btnYellow.setEnabled(true);
                tvReturnData.setText(NETWORK_OK);
            }
            else {
                btnGreen.setEnabled(false);
                btnRed.setEnabled(false);
                btnYellow.setEnabled(false);
                tvReturnData.setText(NETWORK_UNAVAILABLE);
            }
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(MainActivity.this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo !=null && (networkInfo.isAvailable())) {
            return true;
        }
        else{
            return  false;
        }

    }

}

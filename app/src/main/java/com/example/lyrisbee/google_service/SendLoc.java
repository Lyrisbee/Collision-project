package com.example.lyrisbee.google_service;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

/**
 * Created by Lyrisbee on 2016/8/1.
 */
class MyTaskParams {
    LatLng location;
    String Mac;
    int Speed;
    double Angle;
    MyTaskParams(LatLng location,String Mac,int Speed, double Angle) {
        this.location = location;
        this.Mac = Mac;
        this.Speed = Speed;
        this.Angle = Angle;
    }
}
public class SendLoc extends AsyncTask<MyTaskParams,Void,JSONObject> {

    String tmp;
    private Socket clientSocket;
    private PrintWriter mOutput;
    LatLng latLng;
    String mac;
    int Speed;
    double Angle;


    @Override
    protected JSONObject doInBackground(MyTaskParams... params) {
        // IP為Server端

        latLng = params[0].location;
        mac = params[0].Mac;
        Speed = params[0].Speed;
        Angle = params[0].Angle;


        try{
        Log.e("text","try");
        clientSocket = new Socket("140.123.101.222",10002);
        JSONObject message = new JSONObject();
            message.put("mac",mac);
            message.put("latitude",latLng.latitude);
            message.put("longitude",latLng.longitude);
            message.put("speed",Speed);
            message.put("orientation",Angle);
            /*if(clientSocket.isConnected()) {
                Log.e("text","connect");
                mOutput = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream())), true);
                mOutput.write(message.toString());
                Log.e("text",message.toString());
            }*/

                    //取得網路輸出串流
        // 取得網路輸入串流

        if(clientSocket.isConnected()) {
            // 當連線後
            Log.e("text", "connect");
            // 取得網路訊息
            mOutput = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream())), true);
            mOutput.println(message.toString());
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            tmp = br.readLine();    //宣告一個緩衝,從br串流讀取值
            if (tmp != null) {
                //將取到的String抓取{}範圍資料
                Log.e("tmp", tmp);
            }

            JSONObject output = new JSONObject(tmp);
            Log.e("output->tmp", "OK");
            // 如果不是空訊息
            return output;
        }
        else{

        }
        clientSocket.close();
        } catch (Exception e) {
        //當斷線時會跳到catch,可以在這裡寫上斷開連線後的處理
             e.printStackTrace();
             Log.e("text", "Socket連線=" + e.toString());
            return null;
        //當斷線時自動關閉房間
        }
        return null;
    }

}

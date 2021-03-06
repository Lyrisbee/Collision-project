package com.example.lyrisbee.google_service;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.Service;


import android.location.Location;
import android.location.LocationManager;

import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;


public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback {
    private LatLng[] latlngbuffer = new LatLng[5];
    private Context context;
    private LatLng latlng;
    private LatLng CameraPos;
    private GoogleMap mMap;
    double Speed, Angle;
    LocationRequest mLocationRequest;
    Marker mCurrLocation;
    GoogleApiClient mGoogleApiClient;
    TextView location_txt;
    TelephonyManager tm;
    String deviceId;
    String ip = "140.123.101.222";
    int port = 10002, min=0, seconds=0;
    Vibrator myVibrator;
    int socketconnectrefuse = 0;
    int warning = 0;
    Bitmap icon;
    int RoundCount = 0;
    int ConnectFail = 0;
    ImageButton setting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if (!status.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//開啟設定頁面
        }
        //Log.e("text","123");
        PhoneSetting();

        context = MapsActivity.this;
        location_txt = (TextView) findViewById(R.id.location);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        getDeviceId();

        mMap.setMyLocationEnabled(true);


        buildGoogleApiClient();
        mGoogleApiClient.connect();
        icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.exclamation);
        setting = (ImageButton) findViewById(R.id.settingbutton);
        setting.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                v = LayoutInflater.from(MapsActivity.this).inflate(R.layout.activity_alert,null);
                final EditText iptext = (EditText) v.findViewById(R.id.ip);
                final EditText porttext = (EditText) v.findViewById(R.id.port);
                iptext.setText(ip);
                porttext.setText(String.valueOf(port));
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Ip Setting")
                        .setMessage("Ip/Port")
                        .setView(v)
                        .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                ip = iptext.getText().toString();
                                port = Integer.valueOf(porttext.getText().toString());
                            }
                         })
                        .setNegativeButton("Cancel",null)
                        .setCancelable(false);

                AlertDialog alert = builder.create();
                alert.show();
            }

        });

    }

    public void PhoneSetting(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);

    }
    public void uiSetting(){

        //mMap.getUiSettings().setAllGesturesEnabled(false);
        //mMap.getUiSettings().setMapToolbarEnabled(false);

    }
    public void getDeviceId(){
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = tm.getDeviceId();
    }
    protected synchronized void buildGoogleApiClient() {
        //Toast.makeText(this,"buildGoogleApiClient",Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
    @Override
    protected void onRestart(){
        super.onRestart();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public Bitmap resizeMapIcons(int iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(context.getResources(), iconName);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) this);

        getScreenInches();
       //Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (mLastLocation != null) {

        }
        mLocationRequest = new LocationRequest();
        // 設定讀取位置資訊的間隔時間為一秒（1000ms）
        mLocationRequest.setInterval(1000);
        // 設定讀取位置資訊最快的間隔時間為一秒（1000ms）
        mLocationRequest.setFastestInterval(1000);
        // 設定優先讀取高精確度的位置資訊（GPS）
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //Log.e("text","123");

    }
    public void getScreenInches(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int hi = dm.heightPixels ;
       // Toast.makeText(this, "Height : " + hi, Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onConnectionSuspended(int i) {
       // Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLocationChanged(Location location) {

        int top;
        if (mCurrLocation != null) {
            mCurrLocation.remove();
        }

        latlng = new LatLng(location.getLatitude(), location.getLongitude());


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18));
       // Log.e("text","Camera OK");

        // Save max - 5  latlng value
        top = RoundCount;

        if(RoundCount >= 5) {
            SwapLatLng();
            top =  4 ;
        }

        latlngbuffer[top] = latlng;

        //if(CircleCount == ) {
        /*    mMap.addCircle(new CircleOptions() // Draw red dot
                    .center(latlng)
                    .radius(0.5)
                    .strokeColor(Color.rgb(255, 102, 122))
                    .fillColor(Color.rgb(255, 102, 122)));*/
            //CircleCount = 0;
        //}
        if(socketconnectrefuse<5) {
            SocketConnect();
        }
       // Log.e("text","Speed OK");
        Speed = CalCulateSpeed();

       // Log.e("text","Angle OK");
        if(Speed != 0) {
            Angle = AverageAngle();
        }else{
            Angle = 864;
        }

       // Log.e("text","Connect Start");
        //deviceId = "2";

        RoundCount++;
    }
    public void SocketConnect(){
        LinearLayout connect_mes = (LinearLayout) findViewById(R.id.Connect_message);
        LatLng intrasection ;
        Marker exclamation = null;
        MyTaskParams params = new MyTaskParams(ip, port, latlng, deviceId, (int) Speed, Angle);
        try {

            JSONObject output = new SendLoc().execute(params).get();
            if(output !=null) {
                LinearLayout warning_mes = (LinearLayout) findViewById(R.id.warning_message);
                if(connect_mes.getVisibility() == View.VISIBLE){
                    connect_mes.setVisibility(View.GONE);
                }
                Log.e("text", "output");
                Log.d("test", output.getString("yorn"));
                Log.d("test", output.getString("latitude"));
                Log.d("test", output.getString("longitude"));

                if(output.getString("yorn")=="true"){
                    intrasection = new LatLng(Double.valueOf(output.getString("latitude")),Double.valueOf(output.getString("longitude")));
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.exclamation, 100, 100)))

                            .position(new LatLng(intrasection.latitude-0.00008, intrasection.longitude)));
                    myVibrator.vibrate(3000);
                    Calendar c = Calendar.getInstance();
                    seconds = c.get(Calendar.SECOND);
                    min = c.get(Calendar.MINUTE);
                    warning_mes.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "警告 !! ", Toast.LENGTH_LONG).show();
                }else{

                    Calendar c = Calendar.getInstance();
                    if(60*c.get(Calendar.MINUTE)+c.get(Calendar.SECOND)-60*min+seconds >2){
                        mMap.clear();
                    }
                    seconds = 5000;
                    min = 0;

                    warning_mes.setVisibility(View.GONE);
                }
                output = null;
            }
            else{
                socketconnectrefuse ++;
                if(socketconnectrefuse >= 5){

                    TextView mes = (TextView) findViewById(R.id.connect_mes);
                    mes.setText(R.string.connect_message);
                    connect_mes.setVisibility(View.VISIBLE);
                }
            }
            /*if(output == -1){
                ConnectFail ++;
                if(ConnectFail > 10 ){
                    Toast.makeText(this, "無法連接伺服器，請確認網路狀態", Toast.LENGTH_LONG).show();
                }
            }*/
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //Delete old value (5sec ago)
    public void SwapLatLng(){

        for(int i = 0 ;i < 4; i++){
                latlngbuffer[i] = latlngbuffer[i+1];
        }
    }
    public double CalculateAngle(LatLng A1, LatLng A2){
        double X, Y;
        double Angle;
        X = Math.cos(A2.latitude) * Math.sin(A2.longitude-A1.longitude);
        Y = Math.cos(A1.latitude) * Math.sin(A2.latitude) - Math.sin(A1.latitude) * Math.cos(A2.latitude) * Math.cos(A2.longitude-A1.longitude);

        Log.e("text",X + " ; " + Y);
        Angle = Math.atan2(X,Y);
        Angle = Math.toDegrees(Angle);

        return Angle;

    }
    public double CalCulateSpeed(){
        int top, down;
        double Speed;
        top = RoundCount < 4 ? RoundCount : 4;
        down = RoundCount < 1 ? RoundCount : top - 1;
        Location me = new Location("me");
        Location dest = new Location("dest");

        me.setLatitude(latlngbuffer[down].latitude);
        me.setLongitude(latlngbuffer[down].longitude);
        dest.setLatitude(latlngbuffer[top].latitude);
        dest.setLongitude(latlngbuffer[top].longitude);

        double dist =  me.distanceTo(dest);
        Speed =  dist ;

        return Speed;
    }
    public double AverageAngle(){
        double angle;

        int top, down;
        top = RoundCount < 4 ? RoundCount : 4;
        down = RoundCount < 2 ? 0 : top - 2;
        angle = CalculateAngle(latlngbuffer[down],latlngbuffer[top]);

        return angle;
    }
    public void connectAction(View view){

        socketconnectrefuse = 0;
        TextView mes = (TextView) findViewById(R.id.connect_mes);
        mes.setText(R.string.reconnect);
    }


}


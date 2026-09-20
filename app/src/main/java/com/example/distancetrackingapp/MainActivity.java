package com.example.distancetrackingapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    String provider = LocationManager.GPS_PROVIDER;
    long minTime = 1000; // 1000 ms, updates every second
    float minDistance = 1; // 1 meter updates
    float totalDis = 0;
    LocationListener listener;
    //Geocoder geocoder;
    TextView lat, lon;
    TextView currA, textDis, ADD, TIME;
    ListView listView;
    ArrayList<AddressAttributes> addresses = new ArrayList<>();
    private LocationManager locationManager;
    private int lastPos = -1;
    CustomAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //geocoder = new Geocoder(this, Locale.US);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            lon = findViewById(R.id.textLong);
            lat = findViewById(R.id.textLat);
            currA = findViewById(R.id.textCurrAdd);
            textDis = findViewById(R.id.textDistance);
            listView = findViewById(R.id.listView);
            ADD = findViewById(R.id.textView);
            TIME = findViewById(R.id.textView2);
        }else{
            lon = findViewById(R.id.textLongitude);
            lat = findViewById(R.id.textLatitude);
            currA = findViewById(R.id.textAdd);
            textDis = findViewById(R.id.textDis);
            listView = findViewById(R.id.listView);
            ADD = findViewById(R.id.textView5);
            TIME = findViewById(R.id.textView6);
        }

        if(savedInstanceState != null){
            addresses = (ArrayList<AddressAttributes>) savedInstanceState.getSerializable("ADDYS");
            lastPos = savedInstanceState.getInt("lastPos", -1);
        }else{
            addresses = new ArrayList<>();
            Log.d("CR","HELP");
        }

        adapter = new CustomAdapter(this, R.layout.adapter_layout, addresses);
        listView.setAdapter(adapter);

        if(lastPos != -1){
            listView.setSelection(lastPos);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        listener = new MyLocationListener();


        // checkinng & requesting permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        locationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("ADDYS", addresses);
        outState.putInt("listPos", listView.getFirstVisiblePosition());
        outState.putFloat("totalDis", totalDis);
    }
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        int pos = savedInstanceState.getInt("listPos", 0);
        totalDis = savedInstanceState.getFloat("totalDis", 0);
        listView.setSelection(pos);
    }

    public static class CustomAdapter extends ArrayAdapter<AddressAttributes>{
        List<AddressAttributes> addresses;
        Context context;
       // int xmlResource;

        public CustomAdapter(@NonNull Context context, int adapter_layout, List<AddressAttributes> addresses) {
            super(context, 0, addresses);
            //xmlResource = resource;
            this.addresses = addresses;
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.adapter_layout, parent, false);
            }

            AddressAttributes eachaddy = addresses.get(position);
            TextView addy = convertView.findViewById(R.id.textAddy);
            TextView timey = convertView.findViewById(R.id.textTime);

            if(eachaddy != null){
                addy.setText(eachaddy.getAddy());
                timey.setText(eachaddy.getTime());
            }
            return convertView;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
                }
            }
        }
    }

    public class MyLocationListener implements LocationListener {
        private static final float LOCATION_THREDSHOLD = 2;
        private long lastTime = -1;
        float distance = 0;
        Location lastLoc = null;



        // In MyLocationListener class
        @Override
        public void onLocationChanged(@NonNull Location location) {
            long currTime = SystemClock.elapsedRealtime();
            Log.d("LONG LOLO", lastTime+" ");

            double doubleLon = location.getLongitude();
            double doubleLat = location.getLatitude();
            String longitude = String.format("%.2f", location.getLongitude());
            String latitude = String.format("%.2f", location.getLatitude());

            lon.setText("Longitude: " + longitude);
            lat.setText("Laitude: " + latitude);
            textDis.setText("Total Distance Traveled: " + totalDis + " meters");

            try {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.US);
                List<Address> addess = null;
                addess = geocoder.getFromLocation(doubleLat, doubleLon, 5);

                String a1 = addess.get(0).getAddressLine(0);
                Log.d("ADDDDDD", a1 + "          2nd!");
                currA.setText("Current Address: "+a1 + "");
                //textDis.setText("Total Distance Traveled: " + totalDis + " meters");

                if (lastTime == -1) {
                    lastTime = currTime;
                    lastLoc = location;
                    return;
                }

                if (lastLoc != null) {
                    distance = lastLoc.distanceTo(location);
                    //totalDis += distance;
                    Log.d("TOTAL DIS", totalDis + "");

                    boolean isDuppy = lastLoc.getLatitude() == location.getLatitude() && lastLoc.getLongitude() == location.getLongitude();
                    /*boolean moving = distance <= LOCATION_THREDSHOLD;
                    for(AddressAttributes addys : addresses){
                        if(addys.getAddy().equals(a1)){
                            notDuppy = true;
                        }
                    }*/

                    if(distance > LOCATION_THREDSHOLD && !isDuppy){
                        totalDis += distance;
                        long elaspedTime = (currTime-lastTime)/1000;
                        Log.d("TIMTIMTIM", elaspedTime+" seconds");


                            List<Address> addes = geocoder.getFromLocation(doubleLat, doubleLon, 1);
                            String a2 = addes.get(0).getAddressLine(0);
                            // String a2 = addresses.get(1)+"";
                            Log.d("ADDDDDD", a1 + "          2nd!");
                            int cutoff = a2.indexOf(",");
                            int cutoff2 = a2.indexOf("USA");
                            currA.setText("Current Address: "+a2 + "");
                            Log.d("SSSSSSS",a2.substring(0, cutoff)+"\n"+a2.substring(cutoff+1));
                            textDis.setText("Total Distance Traveled: " + totalDis + " meters");

                            AddressAttributes newAddy = new AddressAttributes(a2.substring(0, cutoff+1)+"\n"+a2.substring(cutoff+2, cutoff2-3), elaspedTime+" secs");
                            addresses.add(newAddy);

                            runOnUiThread(() -> adapter.notifyDataSetChanged());

                    }
                    lastTime = currTime;
                }
                lastLoc = location;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
package com.example.hackupc2016f.authlandapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;

import static java.util.logging.Logger.global;

public class MainActivity extends AppCompatActivity {

    double act_lat;
    double act_lon;

    public void computeDistance (double lat, double lon, double flat, double flon){
        double radius = 6371.0; //km
        double dlat = flat - lat;
        double dlon = flon - lon;

        double a = Math.sin(dlat/2)*Math.sin(dlat/2) + Math.cos(lat) * Math.cos(flat) * Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = radius * c;

        if (d >= 0.1)
        {
            Toast.makeText(getApplicationContext(), "Rare authentication pattern detected.", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Authorization successful.", Toast.LENGTH_LONG).show();
        }
    }

    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    }

    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                String mes = remoteMessage.getNotification().getBody();
                String[] coords = mes.split(" ");
                Toast.makeText(getApplicationContext(), "I received a Push notification.", Toast.LENGTH_LONG).show();
                computeDistance(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), act_lat, act_lon);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Elements of the graphic interface
        final TextView message = (TextView) findViewById(R.id.textView);
        final EditText input = (EditText) findViewById(R.id.editText);
        final Button submit = (Button) findViewById(R.id.button);

        // The SharedPreferences, where we save the mobile phone number
        final SharedPreferences settings = getSharedPreferences("AuthlandAPPConf", 0);
        String settings_NumTelf = settings.getString("AppUserPhone", "").toString();

        if (settings_NumTelf == "") {
            // Ask the user for his/her mobile phone number
            submit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (input.getText().toString() != null && TextUtils.isDigitsOnly(input.getText().toString()) == true) {
                        // Saving the user data into app's Shared Preferences
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("AppUserPhone", message.getText().toString());
                        editor.commit();
                        input.setVisibility(View.INVISIBLE);
                        submit.setVisibility(View.INVISIBLE);

                        finish();
                        startActivity(getIntent());
                    }
                }
            });
        } else {
            input.setVisibility(View.INVISIBLE);
            submit.setVisibility(View.INVISIBLE);
            message.setText("The application is now configured to listen to Push notifications. " +
                    "These notifications are sent by the Authland API when a sensor detects the access of a client, " +
                    "so that a two-layer authentication based on geolocalization can be applied.");

            LocationManager locMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            LocationListener locLis = (LocationListener) new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    act_lat = location.getLatitude();
                    act_lon = location.getLongitude();
                    //Toast.makeText(getApplicationContext(), location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            onTokenRefresh();

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
            locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locLis);

        }
    }
}

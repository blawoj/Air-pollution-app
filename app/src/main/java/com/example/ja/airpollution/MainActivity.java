package com.example.ja.airpollution;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Button b, next, back;
    private TextView t;
    private LocationManager locationManager;
    private LocationListener listener;
    private double tempLongitude, tempLatitude;

    /*Do zrobienia możliwość sprawdzania zanieczyszczenia w największych miastach, miasto z najmniejszym oraz miasto z największym zanieczyszczeniem.
     Możliwość robienia zdjęć podejrzanych miejsc, które mogą potencjalnie zanieczyszczać środowisko. Zdjęcie wraz z współrzędnymi
     oraz danymi o zanieczyszczeniu powietrza przychodzą na maila do późniejszej weryfikacji.
     Bardziej przyszłościowo, użyć API google maps i pokazywać w czasie rzeczywistym zanieczyszczenie w całej Polsce.

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        t = (TextView) findViewById(R.id.textView_Location);
        b = (Button) findViewById(R.id.button_getLocation);
        next = (Button) findViewById(R.id.button_next);




        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                tempLongitude = location.getLongitude();
                tempLatitude = location.getLatitude();
                t.setText("\n " + tempLongitude + " " + tempLatitude);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configure_button();
        nextButton();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }

        /*
        Do zmiany, docelowo ma automatycznie robić pomiar co 5 minut albo częściej i jeżeli wykryje
        duże zanieczyszczenie informuje użytkownika przez powiadomeinie push. Użytkownik ma możliwość
        ręcznego zaktualizowania.
         */
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //locationManager.requestLocationUpdates("gps", 5000, 0, listener);
                locationManager.requestSingleUpdate("gps", listener, Looper.getMainLooper());
                t.setText("\n " + "Please wait, getting coordinates,\n move around or go outside if it takes too long");
            }
        });
    }

    public void nextButton(){
        next = (Button) findViewById(R.id.button_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.airpollution);
                backButton();
            }
        });
    }

    public void backButton(){
        back = (Button) findViewById(R.id.button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_main);
                configure_button();
                nextButton();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


}

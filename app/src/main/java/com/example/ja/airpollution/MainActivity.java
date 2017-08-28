package com.example.ja.airpollution;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Button b;
    private Button next;
    private TextView t;
    private LocationManager locationManager;
    private LocationListener listener;
    private Button apiinfo_button;
    private TextView apiinfo_textview;
    private ProgressDialog pd;
    private double tempLongitude;
    private double tempLatitude;

    public double getTempLongitude() {
        return tempLongitude;
    }

    public void setTempLongitude(double tempLongitude) {
        this.tempLongitude = tempLongitude;
    }

    public double getTempLatitude() {
        return tempLatitude;
    }

    public void setTempLatitude(double tempLatitude) {
        this.tempLatitude = tempLatitude;
    }

    /*Do zrobienia możliwość sprawdzania zanieczyszczenia w największych miastach, miasto z najmniejszym oraz miasto z największym zanieczyszczeniem.
     Możliwość robienia zdjęć podejrzanych miejsc, które mogą potencjalnie zanieczyszczać środowisko. Zdjęcie wraz z współrzędnymi
     oraz danymi o zanieczyszczeniu powietrza przychodzą na maila do późniejszej weryfikacji.
     Bardziej przyszłościowo, użyć API google maps i pokazywać w czasie rzeczywistym zanieczyszczenie w całej Polsce.

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTempLatitude(0);
        setTempLongitude(0);
        main_menu();
    }

    public void main_menu() {
        setContentView(R.layout.activity_main);
        final String token = "ec16a64a4ddbb03a0ece79d6e3ce996d2a2a7ed5";

        apiinfo_button = (Button) findViewById(R.id.button_apiinfo);
        apiinfo_textview = (TextView) findViewById(R.id.textView_apiinfo);

        apiinfo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getTempLongitude() == 0 || getTempLatitude() == 0) {
                    String s = "Get your coordinates first";
                    t.setText(s);
                } else {
                    new JsonTask().execute("https://api.waqi.info/feed/geo:" + getTempLatitude() + ";" + getTempLongitude() + "/?token=" + token);
                }
            }
        });


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
                setTempLongitude(location.getLongitude());
                setTempLatitude(location.getLatitude());
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

        getlocation_button();
        nextButton();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                getlocation_button();
                break;
            default:
                break;
        }
    }

    public void getlocation_button() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
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
                String s = "Please wait, getting coordinates,\n if it takes too long move around or go outside";

                //locationManager.requestLocationUpdates("gps", 5000, 0, listener);
                //Nie potrzebne sprawdzanie uprawnień, zostały sprawdzone wcześniej
                try {
                    locationManager.requestSingleUpdate("gps", listener, Looper.getMainLooper());
                } catch (SecurityException e) {
                    Context context = getApplicationContext();
                    CharSequence text = "Access denied, can't acquire permission for gps";
                    int duration = Toast.LENGTH_LONG;
                    Toast.makeText(context, text, duration).show();

                }
                t.setText(s);
            }
        });
    }

    public void nextButton() {
        next = (Button) findViewById(R.id.button_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.airpollution);
                backButton();
            }
        });
    }

    public void backButton() {
        Button back = (Button) findViewById(R.id.button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_main);
                main_menu();
            }
        });
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing application")
                .setMessage("Do you want to close the application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
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

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            apiinfo_textview.setText(result);
        }
    }
}






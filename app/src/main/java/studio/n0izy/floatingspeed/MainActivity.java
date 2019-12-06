package studio.n0izy.floatingspeed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int SYSTEM_ALERT_WINDOW_PERMISSION = 7;

    private FusedLocationProviderClient mFusedLocationClient;
    private int speed = 0;
    private double unit = 1000;
    private double lat;
    private double lng;
    private TextView velocity;
    private TextView units;
    private Switch unitsSwitch;
    private Button buttonCREATE;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        velocity = findViewById(R.id.velocity);
        units = findViewById(R.id.units);
        unitsSwitch = findViewById(R.id.unitsSwitch);
        buttonCREATE = findViewById(R.id.buttonCREATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            RuntimePermissionForUser();
        }

        buttonCREATE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent start = new Intent(MainActivity.this, FloatingWidgetShowService.class);

                if(unitsSwitch.isChecked()) {
                    start.putExtra("MPH", 1609.3);
                    //Toast.makeText(MainActivity.this, "dziala", Toast.LENGTH_LONG).show();
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

                        startService(start);

                    //finish();

                } else if (Settings.canDrawOverlays(MainActivity.this)) {

                        startService(start);

                   //finish();

                } else {
                    RuntimePermissionForUser();

                    Toast.makeText(MainActivity.this, "System Alert Window Permission Is Required For Floating Widget.", Toast.LENGTH_LONG).show();
                }

            }
        });


        String TMP = ""+speed;
        velocity.setText(TMP);

        unitsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    units.setText(R.string.unitMPH);
                    unit = 1609.3;
                        if(isMyServiceRunning(FloatingWidgetShowService.class)){
                            buttonCREATE.performClick();
                        }
                }else{
                    units.setText(R.string.unitKMH);
                    unit = 1000;
                        if(isMyServiceRunning(FloatingWidgetShowService.class)){
                            buttonCREATE.performClick();
                        }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!runtime_permissions()) {
            requestLocations();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLocations() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            List<Location> locationList = locationResult.getLocations();


            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                lat = location.getLatitude();
                lng = location.getLongitude();

                //speed in km/h or mph (unit)
                speed = (int) ((location.getSpeed() * 3600) / unit);
                String TMP = "" + speed;
                velocity.setText(TMP);
            }
        }
    };

    //permission dla lokalizacji
    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onResume();
            } else {
                runtime_permissions();
            }
        }
    }

    //permission dla widgeta
    public void RuntimePermissionForUser() {

        Intent PermissionIntent = null;
        PermissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));

        startActivityForResult(PermissionIntent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }




}

package studio.n0izy.floatingspeed;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.graphics.PixelFormat;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;


public class FloatingWidgetShowService extends Service{


    WindowManager windowManager;
    View floatingView, collapsedView, expandedView;
    WindowManager.LayoutParams params;


    TextView floatSpeed;
    TextView floatUnit;
    private FusedLocationProviderClient mFusedLocationClient;
    private int speed = 0;
    private double unit = 1000;
    private double lat;
    private double lng;


    public FloatingWidgetShowService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.hasExtra("MPH")) {
            unit = (double) intent.getExtras().get("MPH");
            floatUnit.setText("MPH");
            //Toast.makeText(this,""+unit,Toast.LENGTH_SHORT).show();
        }else {
            unit = 1000;
            floatUnit.setText("KM/H");
            //Toast.makeText(this,""+unit,Toast.LENGTH_SHORT).show();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

        }else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        windowManager.addView(floatingView, params);

        expandedView = floatingView.findViewById(R.id.Layout_Expended);

        collapsedView = floatingView.findViewById(R.id.Layout_Collapsed);

        floatingView.findViewById(R.id.Widget_Close_Icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopSelf();

            }
        });

        floatSpeed = floatingView.findViewById(R.id.floatSpeed);
        floatUnit = floatingView.findViewById(R.id.floatingUnits);

        String TMP = ""+speed;
        floatSpeed.setText(TMP);
        requestLocations();

        expandedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);

            }
        });

        floatingView.findViewById(R.id.MainParentRelativeLayout).setOnTouchListener(new View.OnTouchListener() {
            int X_Axis, Y_Axis;
            float TouchX, TouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        X_Axis = params.x;
                        Y_Axis = params.y;
                        TouchX = event.getRawX();
                        TouchY = event.getRawY();
                        return true;

                    /*case MotionEvent.ACTION_UP:

                        collapsedView.setVisibility(View.GONE);
                        expandedView.setVisibility(View.VISIBLE);
                        return true;
*/
                    case MotionEvent.ACTION_MOVE:

                        params.x = X_Axis + (int) (event.getRawX() - TouchX);
                        params.y = Y_Axis + (int) (event.getRawY() - TouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
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

                //speed in km/h
                speed = (int) ((location.getSpeed() * 3600) / unit);
                String TMP = "" + speed;
                floatSpeed.setText(TMP);

            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
    }
}
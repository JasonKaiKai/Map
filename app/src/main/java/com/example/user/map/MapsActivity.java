package com.example.user.map;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.KeyEvent.*;

public class MapsActivity extends FragmentActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks
    , GoogleApiClient.OnConnectionFailedListener,Callback
{

       private GoogleMap mMap; // Might be null if Google Play services APK is not available.
       private LocationManager mLocationMgr;
       private Location location;
       private StreetViewPanoramaFragment mstreet;
       private static double lat, lng;
       static private double lat2, lng2;
       private static String localname;
       private static LatLng latlng = new LatLng(lat, lng);
       private static LatLng latlng2 = new LatLng(lat2, lng2);
       private static Marker Mmarker;
       private Button btn_forward;
       private Button btn_back;
       private Button btn_right;
       private Button btn_add;
       private Button btn_show;
       private Button btn_place;
        private Button btn_search;
       private static int angle = 0;
       private static MapDB mapDB;
       private static SQLiteDatabase db;
       private static EditText locationName;
       private static EditText mapSearch;
       static int rownum;
       private GoogleApiClient mgoogleapiclient;
       private int PLACE_PICKER_REQUEST = 1;
       private static final LatLngBounds viewbound = new LatLngBounds(new LatLng(22.000000, 121.639356), new LatLng(25.167292, 120.000000));

     Dpad mDpad = new Dpad();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initView();
        setUpMapIfNeeded();
        setStreetView(latlng2);

        mLocationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        mapDB = new MapDB(this);
        db=mapDB.getWritableDatabase();

        mgoogleapiclient = new GoogleApiClient.Builder(this)
            .addApi(Places.GEO_DATA_API)
            .addApi(Places.PLACE_DETECTION_API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();

        getJoystickIds();
        if(getJoystickIds()!=null)
        {
            Toast.makeText(MapsActivity.this,"已連接",Toast.LENGTH_SHORT).show();
        }


        try {
            setupPlaceApi();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void onStart(){
        super.onStart();
        mgoogleapiclient.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        getJoystickIds();
        if(getJoystickIds()!=null)
        {
            Toast.makeText(MapsActivity.this,"已連接",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onStop() {

        mgoogleapiclient.disconnect();
        super.onStop();
    }
    @Override
    protected void onDestroy()
    {
      super.onDestroy();
        db.close();
    }

    private void initView()
    {
        mapSearch = (EditText)findViewById(R.id.mapSearch);
        btn_forward = (Button)findViewById(R.id.btn_forward);
        btn_forward.setOnClickListener(forward);
        btn_back = (Button)findViewById(R.id.btn_back);
        btn_back.setOnClickListener(back);
        btn_right = (Button)findViewById(R.id.btn_right);
        btn_right.setOnClickListener(right);
        btn_add = (Button)findViewById(R.id.btn_add);
        btn_add.setOnClickListener(add);
        btn_show = (Button)findViewById(R.id.btn_showall);
        btn_show.setOnClickListener(showall);
        btn_place = (Button)findViewById(R.id.btn_place);
        btn_place.setOnClickListener(setplaceapi);
        btn_search = (Button)findViewById(R.id.btn_search);
        btn_search.setOnClickListener(searchplace);
        locationName = (EditText)findViewById(R.id.edit_locationName);

    }

    private OnClickListener searchplace =new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("","click");
            placeSearch();
        }
    };

    private void placeSearch(){
        Geocoder geocoder = new Geocoder(getBaseContext());
        List<Address> gotAddress =null;
        try{
            gotAddress = geocoder.getFromLocationName(mapSearch.getText().toString(),5);
            Log.d("",gotAddress.get(0).toString());
        }
        catch(IOException e) {
            Log.d("error",e.toString());
        }
        Address address = gotAddress.get(0);
        Log.d("",address.toString());
        LatLng searchLatLng = new LatLng(address.getLatitude(), address.getLongitude());
        Mmarker = mMap.addMarker(new MarkerOptions().position(searchLatLng).visible(true));

    }

    private void setupPlaceApi() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException { //intentBuilder.build(context)需要這些例外
        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
        intentBuilder.setLatLngBounds(viewbound);

        Context context = getApplicationContext();
        startActivityForResult(intentBuilder.build(context),PLACE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST)
        {
          if(resultCode == RESULT_OK)
            {
                Place place = PlacePicker.getPlace(data,this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();

            }
        }
       // super.onActivityResult(requestCode, resultCode, data);
    }

    private OnClickListener setplaceapi = new OnClickListener() {
        @Override
        public void onClick(View v) {

                 Intent intent = new Intent();
            intent.setClass(MapsActivity.this,PlaceApiActivity.class);
         //   Bundle bundle = new Bundle();

          //  intent.putExtras(bundle);
            startActivity(intent);
        }
    };

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
            setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */


    protected void setUpMap() {

         mMap.setMyLocationEnabled(true);
         mMap.getUiSettings().setZoomControlsEnabled(true);

        final MarkerOptions markerOptions = new MarkerOptions();


        mMap.setOnMapClickListener(
                new OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng point) {

                        double nlat = point.latitude;
                        double nlng = point.longitude;
                        LatLng nlatlng = new LatLng(nlat,nlng);

                        latlng2 = nlatlng;
                        markerOptions.position(nlatlng);
                        markerOptions.draggable(true);
                        markerOptions.isVisible();
                        markerOptions.title("marker" );

                        if(Mmarker!=null)
                        {
                            Mmarker.remove();
                        }

                       Mmarker = mMap.addMarker(markerOptions);
                        Mmarker.isVisible();

                        setStreetView(latlng2);

                        Toast.makeText(MapsActivity.this,  ",", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }




    private void setStreetView(LatLng laln)
    {
        mstreet = (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.street);//上面宣告過mstreet了
        mstreet.getStreetViewPanoramaAsync(callback);

        StreetViewPanoramaOptions stoptions=new StreetViewPanoramaOptions();
        stoptions.position(laln);
        stoptions.getStreetViewPanoramaCamera();

        mstreet.newInstance(stoptions);//將option加入mstreet
    }




    OnStreetViewPanoramaReadyCallback callback = new OnStreetViewPanoramaReadyCallback() //要用此方法才能設定街景
    {
        @Override
        public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
            streetViewPanorama.setUserNavigationEnabled(true);
            streetViewPanorama.setPosition(latlng2);
        }
    };



    private ArrayList<Integer> getJoystickIds(){

        ArrayList<Integer> gameControllerDevicesIds = new ArrayList();
        int[] deviceIds = InputDevice.getDeviceIds();

        for(int deviceId:deviceIds){
            InputDevice device = InputDevice.getDevice(deviceId);
            int source = device.getSources();

            if(((source & InputDevice.SOURCE_GAMEPAD)==InputDevice.SOURCE_GAMEPAD)||
                    ((source & InputDevice.SOURCE_JOYSTICK)==InputDevice.SOURCE_JOYSTICK))
            {
                if (!gameControllerDevicesIds.contains(deviceId))
                {
                    gameControllerDevicesIds.add(deviceId);
                }
            }
        }
        return gameControllerDevicesIds;
    }


    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mDpad.isDpadDevice(event)) {

            int press = mDpad.getDirectionPressed(event);
            switch (press) {
                case Dpad.UP:
                 //  Toast.makeText(MapsActivity.this,"up",Toast.LENGTH_SHORT).show();
                    markerMoveUp();
                    return true;
                case Dpad.RIGHT:
                    // Do something for RIGHT direction press
                   // Toast.makeText(MapsActivity.this,"right",Toast.LENGTH_SHORT).show();
                    markerMoveRight();
                    return true;
                case Dpad.DOWN:
                    // Do something for UP direction press
                 //   Toast.makeText(MapsActivity.this,"down",Toast.LENGTH_SHORT).show();
                    markerMoveDown();
                    return true;
                case Dpad.LEFT:
                 //   Toast.makeText(MapsActivity.this,"Left",Toast.LENGTH_SHORT).show();
                    markerMoveLeft();
                    return true;
            }
        }

        return super.onGenericMotionEvent(event);
    }

    public class Dpad {
        final static int UP       = 0;
        final static int LEFT     = 1;
        final static int RIGHT    = 2;
        final static int DOWN     = 3;
        final static int CENTER   = 4;

        int directionPressed = -1; // initialized to -1

        public int getDirectionPressed(InputEvent event) {
            if (!isDpadDevice(event)) {
                return -1;
            }
            // If the input event is a MotionEvent, check its hat axis values.
            if (event instanceof MotionEvent) {
                // Use the hat axis value to find the D-pad direction
                MotionEvent motionEvent = (MotionEvent) event;
                float xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_X);
                float yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_Y);

                // Check if the AXIS_HAT_X value is -1 or 1, and set the D-pad
                // LEFT and RIGHT direction accordingly.
                if (Float.compare(xaxis, -1.0f) == 0) {
                    directionPressed =  Dpad.LEFT;
                } else if (Float.compare(xaxis, 1.0f) == 0) {
                    directionPressed =  Dpad.RIGHT;
                }
                // Check if the AXIS_HAT_Y value is -1 or 1, and set the D-pad
                // UP and DOWN direction accordingly.
                else if (Float.compare(yaxis, -1.0f) == 0) {
                    directionPressed =  Dpad.UP;
                } else if (Float.compare(yaxis, 1.0f) == 0) {
                    directionPressed =  Dpad.DOWN;
                }
            }

            // If the input event is a KeyEvent, check its key code.
            else if (event instanceof KeyEvent) {
                Toast.makeText(MapsActivity.this,"keyevent",Toast.LENGTH_SHORT).show();
                // Use the key code to find the D-pad direction.
                KeyEvent keyEvent = (KeyEvent) event;
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    directionPressed = Dpad.LEFT;
                } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    directionPressed = Dpad.RIGHT;
                } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                    directionPressed = Dpad.UP;
                } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    directionPressed = Dpad.DOWN;
                } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                    directionPressed = Dpad.CENTER;
                }
            }
            return directionPressed;
        }

       public boolean  isDpadDevice(InputEvent event) {
            // Check that input comes from a device with directional pads.
            if ((event.getSource() & InputDevice.SOURCE_DPAD)
                    != InputDevice.SOURCE_DPAD) {
                return true;
            } else {
                return false;
            }
        }
    }


    private void markerMoveUp(){
        Mmarker.remove();
        double nlat2,nlng2;
        nlat2 = latlng2.latitude;
        nlng2 = latlng2.longitude;

        if (angle == 0) {
            nlat2 += 0.0001;
        } else if (angle == 1) {
            nlng2 += 0.0001;
        } else if (angle == 2) {
            nlat2 -= 0.0001;
        } else if (angle == 3) {
            nlng2 -= 0.0001;
        }
        LatLng nlatlng2 = new LatLng(nlat2, nlng2);
        latlng2 = nlatlng2;
        Mmarker = mMap.addMarker(new MarkerOptions().position(nlatlng2)
                .visible(true));
        Mmarker.isVisible();

        setStreetView(nlatlng2);
    }
    private void markerMoveDown(){
        Mmarker.remove();

        double nlat3,nlng3;
        nlat3 = latlng2.latitude;
        nlng3 = latlng2.longitude;

        if (angle == 0) {
            nlat3 -= 0.0001;
        } else if (angle == 1) {
            nlng3 -= 0.0001;
        } else if (angle == 2) {
            nlat3 += 0.0001;
        } else if (angle == 3) {
            nlng3 += 0.0001;
        }
        LatLng nlatlng3 = new LatLng(nlat3, nlng3);
        latlng2 = nlatlng3;
        Mmarker = mMap.addMarker(new MarkerOptions().position(nlatlng3)
                .visible(true));
        Mmarker.isVisible();

        setStreetView(nlatlng3);
    }

    private void markerMoveRight(){
        Mmarker.remove();

        double nlat4,nlng4;
        nlat4 = latlng2.latitude;
        nlng4 = latlng2.longitude;

        if (angle == 0) {
            nlng4 += 0.0001;
        } else if (angle == 1) {
            nlat4 -= 0.0001;
        } else if (angle == 2) {
            nlng4 -= 0.0001;
        } else if (angle == 3) {
            nlat4 += 0.0001;
        }
        LatLng nlatlng4 = new LatLng(nlat4, nlng4);
        latlng2 = nlatlng4;
        Mmarker = mMap.addMarker(new MarkerOptions().position(nlatlng4)
                .visible(true));
        Mmarker.isVisible();

        setStreetView(nlatlng4);
    }

    private void markerMoveLeft(){
        Mmarker.remove();

        double nlat5,nlng5;
        nlat5 = latlng2.latitude;
        nlng5 = latlng2.longitude;

        if (angle == 0) {
            nlng5 -= 0.0001;
        } else if (angle == 1) {
            nlat5 += 0.0001;
        } else if (angle == 2) {
            nlng5 += 0.0001;
        } else if (angle == 3) {
            nlat5 -= 0.0001;
        }
        LatLng nlatlng5 = new LatLng(nlat5, nlng5);
        latlng2 = nlatlng5;
        Mmarker = mMap.addMarker(new MarkerOptions().position(nlatlng5)
                .visible(true));
        Mmarker.isVisible();

        setStreetView(nlatlng5);
    }

    private View.OnClickListener add = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            ContentValues values = new ContentValues();

            values.put("_lat",latlng2.latitude);
            values.put("_lng",latlng2.longitude);
            values.put("_localName",locationName.getText().toString());

            db.insert("MapInfo",null,values);
            Toast.makeText(MapsActivity.this,"加入成功",Toast.LENGTH_SHORT).show();
        }
    };



     private View.OnClickListener showall = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             if(db == null)
             {
                 db = mapDB.getReadableDatabase();
             }
             Cursor latcursor = db.rawQuery("select _lat from MapInfo order by _id DESC",null);
             Cursor lngcursor = db.rawQuery("select _lng from MapInfo order by _id DESC",null);
             Cursor localnamecursor = db.rawQuery("select _localName from MapInfo order by _id DESC",null);

             rownum = latcursor.getCount();

             if(rownum != 0) {
                 latcursor.moveToFirst();
                 lngcursor.moveToFirst();
                 localnamecursor.moveToFirst();//將指標移至第一筆資料
                 for(int i=0; i<rownum; i++) {

                     lat = Double.parseDouble(latcursor.getString(0));
                     lng = Double.parseDouble(lngcursor.getString(0));
                     localname = localnamecursor.getString(0);

                     latlng=new LatLng(lat,lng);
                     Mmarker = mMap.addMarker(new MarkerOptions().position(latlng)
                             .title(localname).visible(true)
                             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                     Mmarker.isVisible();

                     latcursor.moveToNext();
                     lngcursor.moveToNext();
                     localnamecursor.moveToNext();//將指標移至下一筆資料
                 }
             }

         }
     };


    private View.OnClickListener forward = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Mmarker.remove();
            double nlat2,nlng2;
            nlat2 = latlng2.latitude;
            nlng2 = latlng2.longitude;

                if (angle == 0) {
                    nlat2 += 0.0001;
                } else if (angle == 1) {
                    nlng2 += 0.0001;
                } else if (angle == 2) {
                    nlat2 -= 0.0001;
                } else if (angle == 3) {
                    nlng2 -= 0.0001;
                }
                LatLng nlatlng2 = new LatLng(nlat2, nlng2);
                latlng2 = nlatlng2;
                Mmarker = mMap.addMarker(new MarkerOptions().position(nlatlng2)
                        .visible(true));
                Mmarker.isVisible();

                setStreetView(nlatlng2);
        }
    };



              private View.OnClickListener back = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Mmarker.remove();

                        double nlat3,nlng3;
                        nlat3 = latlng2.latitude;
                        nlng3 = latlng2.longitude;

                           if (angle == 0) {
                                nlat3 -= 0.0001;
                            } else if (angle == 1) {
                                nlng3 -= 0.0001;
                            } else if (angle == 2) {
                                nlat3 += 0.0001;
                            } else if (angle == 3) {
                                nlng3 += 0.0001;
                            }
                            LatLng nlatlng2 = new LatLng(nlat3, nlng3);
                            latlng2 = nlatlng2;
                            Mmarker = mMap.addMarker(new MarkerOptions().position(nlatlng2)
                                    .visible(true));
                            Mmarker.isVisible();

                            setStreetView(nlatlng2);

                    }
                };


            private View.OnClickListener right = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(angle>3)
                    {
                        angle = -1;
                    }
                    angle++;
                }
            };


    public void onLocationChanged(Location location) {

    }

    public void onProviderDisabled(String provider) {

    }

    public void onProviderEnabled(String provider)
    {

    }

    public void onStatusChanged(String provider,int status,Bundle extras)
    {

    }

    @Override
    public void onConnected(Bundle bundle) {    //ConnectionCallBack的實作

    }

    @Override
    public void onConnectionSuspended(int i) {  //ConnectionCallBack的實作

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {     //OnConnectionFailedListener的實作

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */

}
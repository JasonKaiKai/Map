package com.example.user.map;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


public class PlaceApiActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mgoogleapiclient;
    private int PLACE_PICKER_REQUEST = 1;
    private static final LatLngBounds viewbound = new LatLngBounds(new LatLng(23.181767,121.639356), new LatLng(25.167292,120.000000));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_api);

        mgoogleapiclient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks( this)
                .addOnConnectionFailedListener( this)
                .build();
        Log.i("googleservice","yes");

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
            Log.i("requestcode","yes");
            if(resultCode == RESULT_OK)
            {
                Log.i("resultcode","yes");
                Place place = PlacePicker.getPlace(data,this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();


            }
        }
        Log.i("z","");

            super.onActivityResult(requestCode, resultCode, data);

    }











    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place_api, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}



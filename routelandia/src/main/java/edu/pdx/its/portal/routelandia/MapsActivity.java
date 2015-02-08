/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package edu.pdx.its.portal.routelandia;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    protected ArrayList<LatLng> mMarkerPoints = new ArrayList<>();
    protected ArrayList<LatLng> startEnd = new ArrayList<>();
    protected PolylineOptions globalPoly = new PolylineOptions();
    protected MarkerOptions marker = new MarkerOptions();
    protected List<Highway> highwayList = new ArrayList<>();
    protected HashMap<Integer, List<Station>> listOfStationsBaseOnHighwayid = new HashMap<>();
    public static LatLng start_point;
    public static LatLng end_ponit;

    /**
     * Perform initialization of all fragments and loaders.
     *
     * @param savedInstanceState Bundle from Google SDK
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else { // Google Play Services are available
            // Getting reference to SupportMapFragment of the activity_maps
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            // Getting Map for the SupportMapFragment
            mMap = fm.getMap();

//            // Enable MyLocation Button in the Map
//            mMap.setMyLocationEnabled(true);

            //The URL to download all highway data from the back end
            String url = "http://capstoneaa.cs.pdx.edu/api/highways.json";          
            try {
                //Create downloadtask to do the http connect and download json from API
                DownloadListofHighway downloadListofHighway = new DownloadListofHighway();
                
                ParserListofHighway parserListofHighway = new ParserListofHighway();
                
                highwayList =  parserListofHighway.execute(downloadListofHighway.execute(url).get()).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            for(int i =0; i<highwayList.size(); i++) {

                String urlStations = urlForAllStationsInEachHighWay(highwayList.get(i).getHighwayid());

                try {
                    DownloadTask downloadTask = new DownloadTask();

                    ParserTask parserTask = new ParserTask();

                    List<Station> stationList = parserTask.execute(downloadTask.execute(urlStations).get()).get();

                    listOfStationsBaseOnHighwayid.put(highwayList.get(i).getHighwayid(), stationList);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            
            for (int i =0; i<highwayList.size(); i++){
                List<Station> stations = listOfStationsBaseOnHighwayid.get(highwayList.get(i).getHighwayid());
                drawHighway(stations);
            }
            
//            drawHighway(stationList);
        }
        //overwrite onMapClickListener to let users drag marker in the map
        mMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                drawMarker(point);
            }
        });

        //Create a time and data button so users can go to the next page
        //which allow them to choose the time when they want to commute
        Button timeAndDateButton = (Button) findViewById(R.id.button3);
        timeAndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("clicks", "you clicked date pick up");
                if(mMarkerPoints.size()==2) {
                    Intent i = new Intent(MapsActivity.this, DatePickUp.class);
                    startActivity(i);
                }
            }
        });

        //Create a clear button so users can re-drag the markers
        Button clearButton = (Button) findViewById(R.id.button2);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMarkerPoints.size() == 1 || mMarkerPoints.size() == 2) {
                    Intent i = new Intent(MapsActivity.this, MapsActivity.class);
                    startActivity(i);
                }
            }
        });

    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link # setUpMap()} once when {@link #mMap} is not null.
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
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {

            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            //Enable location services
            //mMap.setMyLocationEnabled(true);

            //Enable zoom control and location
            //Disable map toolbar
            //mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.509534, -122.681081), 10.0f));
        }
    }

    /**
     * The function check if users tap a point close 200m to the freeway
     * then drag a marker
     * @param point which is users tap on the map
     */
    private void drawMarker(LatLng point) {
        List<LatLng> drawnPoints = globalPoly.getPoints();
        if (PolyUtil.isLocationOnPath(point, drawnPoints, true, 200.0)) {

            // Setting the position of the marker
            marker.position(point);
            mMarkerPoints.add(point);
            if (mMarkerPoints.size() == 1) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                marker.draggable(true);
                marker.title("Start");
                LatLng startPoint = marker.getPosition();
                start_point = startPoint;
                startEnd.add(startPoint);
                mMap.addMarker(marker);

            } else if (mMarkerPoints.size() == 2) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                marker.draggable(true);
                marker.title("End");
                LatLng endPoint = marker.getPosition();
                end_ponit = endPoint;
                startEnd.add(endPoint);
                mMap.addMarker(marker);
            }
        }
    }

    /**
     * * 
     * @param highwayid
     * @return
     */
    private String urlForAllStationsInEachHighWay(int highwayid){
        String url = "http://capstoneaa.cs.pdx.edu/api/highways.json/";
        String station = "/stations";
        
        return url + highwayid + station;
    }
    
    public void drawHighway(List<Station> stations){
       
        for (int i=0; i<stations.size(); i++){
            List<LatLng> points = stations.get(i).getLatLngList();
            
            if(points!= null){
                globalPoly.addAll(points);
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.addAll(points).width(10).color(Color.GREEN).geodesic(true);
                
                mMap.addPolyline(polylineOptions);
            }
        }
    }
}
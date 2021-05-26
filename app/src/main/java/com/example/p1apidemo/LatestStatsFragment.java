package com.example.p1apidemo;

import android.annotation.SuppressLint;
import android.graphics.Camera;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LatestStatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LatestStatsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    GoogleMap googleMap;
    List<Marker> markers;
    private final double defaultLat = 49.172;
    private final double defaultLon = -123.071;
    Marker initialm;


    public LatestStatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static LatestStatsFragment newInstance() {
        LatestStatsFragment fragment = new LatestStatsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_latest_stats, container, false);

        markers = new ArrayList<>();

        Log.i("LATESTFRAG", "setting up map");

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button refreshBtn = (Button) rootView.findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new latestStatsOperation().execute();
            }
        });

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
        CameraUpdate defaultPos = CameraUpdateFactory.newLatLng(new LatLng(defaultLat, defaultLon));
        googleMap.moveCamera(defaultPos);
        googleMap.setOnMarkerClickListener(this);

    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        String gpsAccuracy = (String) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (gpsAccuracy != "") {
            Toast.makeText(getActivity(),marker.getTitle() +
                    " GPS FixDimension : " + gpsAccuracy, Toast.LENGTH_LONG).show();
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    private void getLatestStats(Marker m){
        List<String> uids = ((HomeActivity)getActivity()).getSelectedGateways();
        RequestQueue queue = ((HomeActivity)getActivity()).queue;
        AmmRequestConstructor requestConstructor = ((HomeActivity)getActivity()).ammRequestConstructor;

        if(uids == null) {
            return;
        }

        int numRequests = 0;
        for(String uid : uids) {
            numRequests++;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, requestConstructor.getLatestMapStats(uid),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("LATESTFRAG", response.toString());
                            Gson gson = new Gson();
                            JsonObject stats = gson.fromJson(response, JsonObject.class);
                            addStatsToMap(uid, stats);
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("LATESTFRAG", error.getMessage());

                }
            });

            // Add the request to the RequestQueue.
            queue.add(stringRequest);

            //safeguard
            if(numRequests > 20) {
                break;
            }
        }
    }

    /*
    checks selected gateways and updates markers shown
     */
    private void removeMarkers() {
        for(Marker marker :markers){
            try{
                if(marker == null){
                    continue;
                }
                    //for some reason remove markers leads to error, so settle with making invisible
                markers.remove(marker);
                marker.remove();
//                    marker.setVisible(false);
            } catch (Exception e) {
                Log.i("LATESTFRAG", "Could not update marker: "+ marker.getTitle());
                e.printStackTrace();
            }
        }

    }

    private void addStatsToMap(String uid, JsonObject stats) {
        String gpsFixDimension = "";
        String idleTime = "not reported";
        try{
            gpsFixDimension = stats.get("GPS FixDimension").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
        } catch (Exception e){
            Log.i("LATESTFRAG", "no latest gps accuracy stats available for "+uid);
        }

        try{
            idleTime = stats.get("ReportIdleTime").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
        } catch (Exception e){
            Log.i("LATESTFRAG", "no idle time stats available for "+uid);
        }

        try{
            String gpsLatitude = stats.get("GPS Location-latitude").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
            String gpsLongitude = stats.get("GPS Location-longitude").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();

            LatLng gwLocation = new LatLng(Double.parseDouble(gpsLatitude), Double.parseDouble(gpsLongitude));
            Boolean markerExists;

            markerExists = false;
            for(Marker marker : markers){
                if(marker.getTitle()==uid){
                    marker.setVisible(true);
                    markerExists = true;
                    break;
                }
            }

            if(!markerExists){
                Marker m = googleMap.addMarker(new MarkerOptions()
                        .position(gwLocation)
                        .title(uid)
                        .snippet("Idle Time: "+idleTime));

                m.setTag(gpsFixDimension);
                markers.add(m);
                Log.i("LATESTFRAG", "title: "+ m.getTitle());
                Log.i("LATESTFRAG", markers.get(0).getTitle());

            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("LATESTFRAG", "Could not add uid: " + uid + " to map");
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class latestStatsOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            getLatestStats(initialm);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
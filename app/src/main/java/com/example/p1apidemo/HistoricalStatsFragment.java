package com.example.p1apidemo;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoricalStatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoricalStatsFragment extends Fragment {

    //timeout specific to this demo App. AMM API timeout can be configured in AMM
    private static final int RESPONSE_TIMEOUT_MS = 110000;
    View rootview;
    boolean[] dataReceived;
    String[] dataId;
    Semaphore sem;
    List<Pair<Float, String>> timeLatitudeMap;
    List<Pair<Float, String>> timeLongitudeMap;
    TableLayout tableLayout;
    Spinner gatewaysSpin;


    public HistoricalStatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.* @return A new instance of fragment HistoricalStatsFragment.
     */
    public static HistoricalStatsFragment newInstance(String param1, String param2) {
        HistoricalStatsFragment fragment = new HistoricalStatsFragment();
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
        rootview = inflater.inflate(R.layout.fragment_historical_stats, container, false);

        sem = new Semaphore(1);
        dataId = new String[]{"GPS Location-latitude", "GPS Location-longitude"};
        dataReceived = new boolean[]{false, false};

        gatewaysSpin = (Spinner) rootview.findViewById(R.id.gwSpin);

        tableLayout = rootview.findViewById(R.id.histStatsTable);

        timeLongitudeMap = new ArrayList<>();
        timeLatitudeMap = new ArrayList<>();

        TableRow header = new TableRow(getActivity());
        TableLayout.LayoutParams trParamsSep = new
                TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        trParamsSep.setMargins(16,8,16,4);
        header.setLayoutParams(trParamsSep);


        //first header column
        TextView tv = new TextView(getActivity());
        tv.setBackgroundColor(Color.parseColor("#f0f0f0"));
        tv.setTextSize(20);
        TableRow.LayoutParams tvSepLay = new
                TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(tvSepLay);
        tv.setText("Timestamp");

        //second header column
        TextView tv2 = new TextView(getActivity());
        tv2.setBackgroundColor(Color.parseColor("#f7f7f7"));
        tv2.setTextSize(20);
        TableRow.LayoutParams tvSepLay2 = new
                TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        tv2.setLayoutParams(tvSepLay2);
        tv2.setText("Latitude");


        //third column
        TextView tv3 = new TextView(getActivity());
        tv3.setBackgroundColor(Color.parseColor("#f0f0f0"));
        tv3.setTextSize(20);
        TableRow.LayoutParams tvSepLay3 = new
                TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        tv3.setLayoutParams(tvSepLay3);
        tv3.setText("Longitude");

        header.addView(tv);
        header.addView(tv2);
        header.addView(tv3);

        tableLayout.addView(header, trParamsSep);

        List<String> gws = ((HomeActivity)getActivity()).getSelectedGateways();

        // Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, gws);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        gatewaysSpin.setAdapter(spinnerArrayAdapter);

        Button searchBtn = (Button) rootview.findViewById(R.id.searchBtn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new historicalStatOperation().execute();
            }
        });

        return rootview;
    }

    private void showHistoricalStats() {

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String gw = gatewaysSpin.getSelectedItem().toString();

        AmmRequestConstructor requestConstructor = ((HomeActivity)getActivity()).ammRequestConstructor;

        /*
        We need to make a seperate request for each historical stat (longitude and latitude), as the Historical Stats endpoint can only give
        information on one stat per request.
         */
        StringRequest[] stringRequest = new StringRequest[2];

        for(int i = 0; i < 2; i++){
            int j = i;
            stringRequest[i] = new StringRequest(Request.Method.GET, requestConstructor.getHistoricalStats(gw, dataId[i]),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("FRAG", response.toString());
                            Gson gson = new Gson();
                            JsonObject results = gson.fromJson(response, JsonObject.class);
                            parseStats(results, gw, dataId[j]);

                            try {
                                sem.acquire();
                                dataReceived[j] = true;
                                if(dataReceived[0] && dataReceived[1]){
                                    createTables();
                                    dataReceived[0] = false;
                                    dataReceived[1] = false;
                                }
                                sem.release();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("FRAG", error.getMessage());
                }
            });

            stringRequest[i].setRetryPolicy(new DefaultRetryPolicy(
                    RESPONSE_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }



        // Add the request to the RequestQueue.
        queue.add(stringRequest[0]);
        queue.add(stringRequest[1]);

    }

    private void createTables() {
        if(timeLatitudeMap == null || timeLongitudeMap == null){
            return;
        }

        Log.i("HISTFRAG", "Creating Tables");
        System.out.println("Creating Tables..");
        int count = 0;
        for(Pair<Float, String> elem : timeLongitudeMap) {
            count++;
            if(count > 100){
                //for demo sake
                break;
            }

            float timestampF = elem.first;
            Date date = new Date((long) timestampF);
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("America/Vancouver"));
            String timestamp = format.format(date);

            String lon = elem.second.replaceAll("\"", "");
            String lat = timeLatitudeMap.get(timeLongitudeMap.indexOf(elem)).second.replaceAll("\"", "");

            TableRow row = new TableRow(getActivity());
            TableLayout.LayoutParams trParamsSep = new
                    TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParamsSep.setMargins(16,2,16,2);
            row.setLayoutParams(trParamsSep);

            //first column
            TextView tv = new TextView(getActivity());
            tv.setBackgroundColor(Color.parseColor("#f8f8f8"));
            TableRow.LayoutParams tvSepLay = new
                    TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(tvSepLay);
            tv.setText(timestamp);

            //second column
            TextView tv2 = new TextView(getActivity());
            tv2.setBackgroundColor(Color.parseColor("#ffffff"));
            TableRow.LayoutParams tvSepLay2 = new
                    TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            tv2.setLayoutParams(tvSepLay2);
            tv2.setText(lat);

            //3rd column
            TextView tv3 = new TextView(getActivity());
            tv3.setBackgroundColor(Color.parseColor("#f8f8f8"));
            TableRow.LayoutParams tvSepLay3 = new
                    TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            tv3.setLayoutParams(tvSepLay3);
            tv3.setText(lon);

            row.addView(tv);
            row.addView(tv2);
            row.addView(tv3);

            tableLayout.addView(row, trParamsSep);
        }


    }

    private void parseStats(JsonObject results, String uid, String dataName) {
        Log.i("HISTFRAG", "Parsing " + dataName);
        System.out.println("Parsing: "+dataName);
        JsonArray values = results.getAsJsonObject(uid).getAsJsonArray(dataName);
        int count = 0;
        for(JsonElement val : values) {
            count++;
            //List maximum 100 stats for the sake of Application performance, not
            //due to any limitations from AMM
            if(count > 100){
                break;
            }
            JsonObject data = val.getAsJsonObject();
            float timestamp = Float.parseFloat(data.get("timestamp").toString());
            String locationData = data.get("value").toString();

            Pair<Float, String> pair = new Pair<>(timestamp, locationData);

            if(dataName.equals(dataId[0])){
                timeLatitudeMap.add(pair);
            } else {
                timeLongitudeMap.add(pair);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class historicalStatOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            showHistoricalStats();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }



}
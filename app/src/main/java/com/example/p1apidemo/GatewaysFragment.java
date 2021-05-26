package com.example.p1apidemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class GatewaysFragment extends Fragment {

    List<String> gatewayAdapterList;
    List<String> tempGws;
    GatewayAdapter gatewayAdapter;
    ListView listView;

    View rootView;

    public GatewaysFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_gateways, container, false);
        if(gatewayAdapterList == null) {
            gatewayAdapterList = new ArrayList<>();
        }

        if(gatewayAdapter == null){
            gatewayAdapter = new GatewayAdapter(this.getActivity(), gatewayAdapterList);
            listView = (ListView) rootView.findViewById(R.id.gwList);
            listView.setAdapter(gatewayAdapter);
        }


        Button searchBtn = (Button) rootView.findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new gatewayOperation().execute();
            }
        });

        if(gatewayAdapterList.isEmpty()){
            new gatewayOperation().execute();
        }

        listView = (ListView) rootView.findViewById(R.id.gwList);
        listView.setAdapter(gatewayAdapter);

        return rootView;
    }

    TextView textView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void showGateways() {

        RequestQueue queue = ((HomeActivity)getActivity()).queue;

        Spinner platSpin = (Spinner) rootView.findViewById(R.id.platformSpin);
        String heartbeat = getHeartbeat();
        List<String> platforms = new ArrayList<>();

        if(platSpin.getSelectedItemPosition() != 0){
            platforms.add(platSpin.getSelectedItem().toString());
        }


        AmmRequestConstructor requestConstructor = ((HomeActivity)getActivity()).ammRequestConstructor;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestConstructor.getGateways(null, platforms, heartbeat, ""),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("FRAG", response.toString());
                        Gson gson = new Gson();
                        JsonObject gateways = gson.fromJson(response, JsonObject.class);
                        parseGateways(gateways);
                        ((HomeActivity)getActivity()).setAllGateways(tempGws);
                        ((HomeActivity)getActivity()).setSelectedGateways(new LinkedList<>());
                        gatewayAdapter.clear();
                        gatewayAdapter.addAll(tempGws);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("FRAG", "Error Response");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private String getHeartbeat() {
        Spinner spinner = rootView.findViewById(R.id.heartbeatSpin);
        String text = spinner.getSelectedItem().toString();
        String options[] = getResources().getStringArray(R.array.heartbeat_array);
        String result;

        //Endpoint requires heartbeat in seconds.
        switch (text) {
            case "ALL":
                result = "";
                break;
            case "10 min":
                result = "10";
                break;
            case "2 hours":
                result = "120";
                break;
            case "12 hours":
                result = "240";
                break;
            case "3 days":
                result = "4320";
                break;
            case "1 month":
                result = "43200";
                break;
            case "6 month":
                result = "259200";
                break;
            default:
                result = "";
                break;
        }

        return result;
    }


    private void parseGateways(JsonObject gateways) {
        tempGws = new ArrayList<>();

        //base level, should be only "All Gateways"
        for(String groupName : gateways.keySet()){
            JsonArray subgroups = (JsonArray) gateways.get(groupName);

            addGateways(subgroups);
        }
    }

    private void addGateways(JsonArray parentSubgroups) {
        for(JsonElement group : parentSubgroups) {
            //item is either a subgroup (array) or gateway (object)
            JsonObject jo = group.getAsJsonObject();
            if(jo.keySet().contains("uid")){
                String uid = jo.get("uid").getAsString();
                tempGws.add(uid);
            } else{
                String groupName = (jo.keySet().toArray())[0].toString();
                JsonArray subgroups = jo.get(groupName).getAsJsonArray();
                addGateways(subgroups);
            }
        }
    }

    /*
    Array adapter used to organize gateway list and actions associated with each row
     */
    public class GatewayAdapter extends ArrayAdapter<String> {

        Map<Integer, Boolean> mapper;
        public GatewayAdapter(Context context, List<String> users) {

            super(context, 0, users);
            mapper = new HashMap<>();
        }

        // ...
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Get the data item for this position
            String uri = getItem(position);
            RecyclerView.ViewHolder viewHolder; // view lookup cache stored in tag

            List<String> selectedGws = ((HomeActivity)getActivity()).selectedGateways;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.gatewaylist_item, parent, false);
            }

            TextView gwName = (TextView) convertView.findViewById(R.id.gwname);
            gwName.setText(uri);

            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                   @Override
                   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                       if(buttonView.isPressed()) {
                           if(isChecked && !selectedGws.contains(uri)){
                               selectedGws.add(uri);
                               mapper.put(position, true);
                           } else if (!isChecked && selectedGws.contains(uri)){
                               selectedGws.remove(uri);
                               mapper.put(position, false);
                           }
                       }

                   }
               }
            );

            checkBox.setChecked(selectedGws.contains(uri));
            return convertView;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class gatewayOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            showGateways();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


}
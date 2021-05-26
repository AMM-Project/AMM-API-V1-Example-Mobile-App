package com.example.p1apidemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class GroupsFragment extends Fragment {

    ListView listView;
    JsonObject groups;
    List<Group> groupList;
    GroupAdapter groupAdapter;
    List<Group> groupAdapterList;
    SearchView searchView;
    View rootView;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        groupList = new ArrayList<>();
        groupAdapterList = new ArrayList<>();

        groupAdapter = new GroupAdapter(this.getActivity(), groupAdapterList);
        listView = (ListView) rootView.findViewById(R.id.groupList);
        listView.setAdapter(groupAdapter);

        new groupOperation().execute();
        return rootView;

    }

    private void showGroups() {
        RequestQueue queue = ((HomeActivity)getActivity()).queue;

        AmmRequestConstructor requestConstructor = ((HomeActivity)getActivity()).ammRequestConstructor;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestConstructor.getAllGroups(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("GROUPFRAG", response.toString());
                        Gson gson = new Gson();
                        groups = gson.fromJson(response, JsonObject.class);
                        parseGroups();
                        groupAdapter.addAll(groupList);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("FRAG", error.getMessage());

            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void parseGroups() {

        //base level, should be only "All Gateways"
        for(String groupName : groups.keySet()){
            String id = ((JsonObject) groups.get(groupName)).get("id").getAsString();

            Group group = new Group(id, groupName, new ArrayList<String>());
            JsonArray subgroups = (JsonArray) ((JsonObject) groups.get(groupName)).get("subgroups");

            if(subgroups != null){
                addSubgroups(group, subgroups);
            }
        }

    }

    private List<Group> addSubgroups(Group parentGroup, JsonArray parentsubgroups) {
        for(JsonElement element: parentsubgroups){
            JsonObject jo = element.getAsJsonObject();
            String name = jo.keySet().toArray()[0].toString();
            String id = ((JsonObject) ((JsonObject) element).get(name)).get("id").getAsString();

            List<String> parentsList = parentGroup.getParents();
            parentsList.add(parentGroup.name);
            Group group = new Group(id, name, parentsList );
            JsonArray subgroups = (JsonArray) ((JsonObject) ((JsonObject) element).get(name)).get("subgroups");
            groupList.add(group);

            if(subgroups != null && subgroups.size() > 0){
                addSubgroups(group, subgroups);
            }
        }

        return groupList;
    }

    public class GroupAdapter extends ArrayAdapter<Group> implements Filterable {
        List<Group> filteredList;
        List<Group> fullList;

        public GroupAdapter(Context context, List<Group> users) {
            super(context, 0, users);
            filteredList = new ArrayList<>();
            fullList = users;
        }


        // ...
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // ...

            // Get the data item for this position
            Group group = getItem(position);
            RecyclerView.ViewHolder viewHolder; // view lookup cache stored in tag

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.grouplist_item, parent, false);
            }

            // Lookup view for data population
            TextView groupName = (TextView) convertView.findViewById(R.id.groupname);
            //Button infoBtn = (Button) convertView.findViewById(R.id.bInfo);

            ImageView imageView = convertView.findViewById(R.id.testimg);
            // Populate the data into the template view using the Group object
            groupName.setText(group.name);

            Pair<Group, TextView> tuple = new Pair<>(group, groupName);
            //infoBtn.setTag(tuple);
            imageView.setTag(tuple);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Group group = (Group) ((Pair) view.getTag()).first;
                    TextView text = (TextView) ((Pair) view.getTag()).second;

                    if(text.getText().toString().equals(group.name)){
                        String newText = TextUtils.join(" -> ", group.parents) + "->" + group.name;
                        text.setText(newText);
                    } else {
                        text.setText(group.name);
                    }
                }
            });

            return convertView;
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class groupOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            showGroups();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
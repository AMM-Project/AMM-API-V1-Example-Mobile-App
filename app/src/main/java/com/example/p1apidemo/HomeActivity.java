package com.example.p1apidemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    public List<String> getSelectedGateways() {
        return selectedGateways;
    }
    List<String> allGateways;
    AmmRequestConstructor ammRequestConstructor;
    RequestQueue queue;

    //global list of selected gateways, accessible form all fragments
    List<String> selectedGateways;

    public void setSelectedGateways(List<String> selectedGateways) {
        this.selectedGateways = selectedGateways;
    }

    public List<String> getAllGateways() {
        return new LinkedList<>(allGateways);
    }

    public void setAllGateways(List<String> allGateways) {
        this.allGateways = new LinkedList<>(allGateways);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        selectedGateways = new ArrayList<>();
        allGateways = new LinkedList<>();

        queue = Volley.newRequestQueue(getApplicationContext());
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPref.getString(getString(R.string.token_param), "");
        Token token_obj = gson.fromJson(json, Token.class);
        String target = sharedPref.getString(getString(R.string.AMM_HOST_ADDRESS_PARAM), "");
        ammRequestConstructor = new AmmRequestConstructor(target, token_obj);

        TabLayout tabLayout = findViewById(R.id.tablayout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_gateways));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_latestStats));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_historicalStats));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_groups));

        ViewPager viewPager = findViewById(R.id.viewpage);
        tabLayout.setupWithViewPager(viewPager);
        setupViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /*
    Provides action to menuitem option selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            //logout
            case R.id.one:
                StringRequest stringRequest = new StringRequest(Request.Method.POST, ammRequestConstructor.getLogout(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("HOMEACT", response.toString());
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("HOMEFRAG", "Error Response");
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        i.putExtra("action", "logout");
                        startActivity(i);
                    }
                });
                // Add the request to the RequestQueue.
                queue.add(stringRequest);
                break;

                //refresh
            case R.id.two:
                StringRequest refreshRequest = new StringRequest(Request.Method.POST, ammRequestConstructor.getLogout(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("HOMEACT", response.toString());
                                Intent i = new Intent(getApplicationContext(), AuthenticateActivity.class);
                                i.putExtra("action", "refresh");
                                startActivity(i);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("HOMEFRAG", "Error Response");
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(refreshRequest);
                break;
        }
        return(super.onOptionsItemSelected(item));
    }

    //sets up frgments
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new GatewaysFragment(), "Gateways");
        adapter.addFrag(new LatestStatsFragment(), "Latest Stats");
        adapter.addFrag(new HistoricalStatsFragment(), "Historical Stats");
        adapter.addFrag(new GroupsFragment(), "Groups");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
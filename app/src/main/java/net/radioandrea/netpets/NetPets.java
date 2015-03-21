package net.radioandrea.netpets;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class NetPets extends ActionBarActivity {
    SpinnerAdapter adapter;
    String[] list;
    android.support.v7.app.ActionBar actionBar;
    TextView mySmallText;
    SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_net_pets);
        addCustomSpinnerToActionBar();
        mySmallText = (TextView) findViewById(R.id.textView);
        //textViewNetworking();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_net_pets, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, NetPetSettings.class));
                return true;
            case R.id.action_about:
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setMessage(getString(R.string.about_message));
                dlgAlert.setTitle(getString(R.string.about_title));
                dlgAlert.setPositiveButton(getString(R.string.ok), null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
                return true;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    Spinner spinner;

    private void addCustomSpinnerToActionBar() {
        actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.spinner);
        spinner = (Spinner) actionBar.getCustomView().findViewById(R.id.spinner);
        new NetTask().execute(prefs.getString(getString(R.string.site_key), ""));
    }


    private class NetTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            final int httpOK = 200;
            StringBuilder sBuilder = new StringBuilder();
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(params[0] + getString(R.string.pets_json));

            try {
                HttpResponse httpStatus = httpClient.execute(httpGet);
                if (httpStatus.getStatusLine().getStatusCode() == httpOK) {
                    HttpEntity entity = httpStatus.getEntity();
                    BufferedReader bReader =
                            new BufferedReader(new InputStreamReader(entity.getContent()));
                    String line;
                    while ((line = bReader.readLine()) != null) {
                        sBuilder.append(line);
                    }
                } else {
                    sBuilder.append("Shit Broked");
                }

                return sBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "Some shit broke hardcore-like";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray pets = new JSONObject(result).getJSONArray("pets");
                ArrayList<NetPetJSON> list = new ArrayList<>();
                for (int i = 0; i < pets.length(); i++) {
                    list.add(new NetPetJSON(pets.getJSONObject(i)));
                }

                ArrayAdapter<NetPetJSON> adapter = new ArrayAdapter<>(actionBar.getThemedContext(), android.R.layout.simple_list_item_1, list);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public static final int SELECTED_ITEM = 0;

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long rowid) {
                        if (arg0.getChildAt(SELECTED_ITEM) != null) {
                            //todo make this do useful things :P
                            ((TextView) arg0.getChildAt(SELECTED_ITEM)).setTextColor(Color.WHITE);
                            NetPetJSON pet = (NetPetJSON) arg0.getItemAtPosition(pos);
                            Toast.makeText(NetPets.this, pet.getFileURL(), Toast.LENGTH_SHORT).show();
//                            Toast.makeText(NetPets.this, "Fucking test", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private class NetPetJSON {
            protected JSONObject pet;
            protected String site;

            protected NetPetJSON(JSONObject pet) {
                this.pet = pet;
                this.site = prefs.getString(getString(R.string.site_key), "");
            }

            public String getFileURL() {
                try {
                    return site + pet.getString("file");
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }

            @Override
            public String toString() {
                try {
                    return pet.getString("name");
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
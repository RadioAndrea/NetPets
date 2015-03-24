package net.radioandrea.netpets;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class NetPets extends ActionBarActivity {
    SpinnerAdapter adapter;
    String[] list;
    android.support.v7.app.ActionBar actionBar;
    SharedPreferences prefs;
    WebImage mywebImage;

    private static final int SETTINGRESULT = 1337;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        addCustomSpinnerToActionBar();
        mywebImage = new WebImage(this);
        mywebImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mywebImage.setImageResource(R.drawable.world);
        setContentView(mywebImage);
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
                startActivityForResult(new Intent(this, NetPetSettings.class), SETTINGRESULT);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTINGRESULT) {
            new NetTask().execute(prefs.getString(getString(R.string.site_key), ""));
        }
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
                    Log.e("NetPets", "HTTP not OK!");
                }

                return sBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("NetPets", "NetTask Exception!");
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
                            if(arg0.getChildAt(SELECTED_ITEM).toString().contains("Shit Broked"))
                            {
                                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(NetPets.this);
                                dlgAlert.setMessage("but I can't load that!\nCheck your network settings!");
                                dlgAlert.setTitle("I'm Sorry Dave...");
                                dlgAlert.setPositiveButton("Try Again", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        new NetTask().execute(prefs.getString(getString(R.string.site_key), ""));
                                    }
                                });
                                dlgAlert.setNegativeButton("Give Up", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Toast.makeText(NetPets.this, "What's the point...", Toast.LENGTH_SHORT).show();
                                    }} );
                                dlgAlert.setCancelable(true);
                                dlgAlert.create().show();

                            }
                             else
                            {
                                //todo make this do useful things :P
                                ((TextView) arg0.getChildAt(SELECTED_ITEM)).setTextColor(Color.WHITE);
                                NetPetJSON pet = (NetPetJSON) arg0.getItemAtPosition(pos);
                                //Toast.makeText(NetPets.this, pet.getFileURL(), Toast.LENGTH_SHORT).show();
                                mywebImage.setImageUrl(pet.getFileURL());
                            }
//
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(NetPets.this);
                        dlgAlert.setMessage("but I can't load that!\nCheck your network settings!");
                        dlgAlert.setTitle("I'm Sorry Dave...");
                        dlgAlert.setPositiveButton(getString(R.string.ok), null);
                        dlgAlert.setCancelable(true);
                        dlgAlert.create().show();
                    }
                });
            } catch (Exception e) {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(NetPets.this);
                dlgAlert.setMessage("but I can't load that!\nCheck your network settings!");
                dlgAlert.setTitle("I'm Sorry Dave...");
                dlgAlert.setPositiveButton("Try Again", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        new NetTask().execute(prefs.getString(getString(R.string.site_key), ""));
                    }
                });
                dlgAlert.setNegativeButton("Give Up", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(NetPets.this, "What's the point...", Toast.LENGTH_SHORT).show();
                    }} );
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
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
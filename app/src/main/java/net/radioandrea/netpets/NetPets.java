package net.radioandrea.netpets;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class NetPets extends ActionBarActivity {

    private static final String location = "www.tetonsoftware.com/pets/pets.json";
    SpinnerAdapter adapter;
    String[] list;
    android.support.v7.app.ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_pets);
        addCustomSpinnerToActionBar();

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

        switch(id){
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
        actionBar =  getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.spinner);
        spinner = (Spinner) actionBar.getCustomView().findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.action_list,R.layout.spinner_layout);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public static final  int SELECTED_ITEM = 0;

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long rowid) {
                if (arg0.getChildAt(SELECTED_ITEM) != null )
                {
                    //todo make this do useful things :P
                    ((TextView) arg0.getChildAt(SELECTED_ITEM)).setTextColor(Color.WHITE);
                    Toast.makeText(NetPets.this, (String) arg0.getItemAtPosition(pos), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0){}
        });
    }

    private void textViewNetworking()
    {


    }

    private String grabJSON(String url)
    {
        final int httpOK = 200;
        StringBuilder sBuilder = new StringBuilder();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        try{
            HttpResponse httpStatus = httpClient.execute(httpGet);
            if(httpStatus.getStatusLine().getStatusCode() == httpOK)
            {
                HttpEntity entity = httpStatus.getEntity();
                BufferedReader bReader =
                        new BufferedReader(new InputStreamReader(entity.getContent()));
                String line;
                while ((line = bReader.readLine()) != null)
                {
                    sBuilder.append(line);
                }
            }
            else
            {
                sBuilder.append("Shit Broked");
            }

            return sBuilder.toString();
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return "Some shit broke hardcore-like";
    }
}
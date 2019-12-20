package com.iskmz.dogcatalogue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class BreedList extends AppCompatActivity {

    Context context;
    ListView lstBreeds;
    ImageButton btnBack, btnRefresh;

    final String breedsURL = "https://dog.ceo/api/breeds/list/all";
    ArrayList<String> dataList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breed_list);

        setPointers();
    }

    private void setPointers() {
        this.context=this;
        lstBreeds = findViewById(R.id.lstBreeds);
        btnBack = findViewById(R.id.btnBackToMain_fromList);
        btnRefresh = findViewById(R.id.btnRefreshList);


        loadList();


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadList();
            }
        });

        lstBreeds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(context,BreedImg.class);
                intent.putExtra("breed",dataList.get(position));
                startActivity(intent);
            }
        });
    }


    private void errorMsg()
    {
        Toast.makeText(context,"Error while loading list! \nCheck Internet & try again please.", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadList() {


        if(!Utils.isNetworkConnected(context))
        {
            errorMsg();
            return;
        }

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {

                HttpURLConnection connection = null;
                String jsonString="";
                try {
                    connection = (HttpURLConnection) new URL(breedsURL).openConnection();
                    BufferedReader buf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = buf.readLine()) != null) jsonString += line;

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    assert connection != null;
                    connection.disconnect();
                }

                return jsonString;
            }

            @Override
            protected void onPostExecute(String res) {
                super.onPostExecute(res);

                if(res.isEmpty() || !res.contains("status"))
                {
                    errorMsg();
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(res);
                    String status = jsonObject.getString("status");

                    if(status.equals("success"))
                    {
                        String breedsRAW = jsonObject.getString("message");
                        dataList = parseRAW(breedsRAW);
                    }
                    else
                    {
                        errorMsg();
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(dataList.isEmpty())
                {
                    errorMsg();
                    return;
                }

                ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, dataList);
                lstBreeds.setAdapter(adapter);

            }

            private ArrayList<String> parseRAW(String breedsRAW) {

                ArrayList<String> lst = new ArrayList<>();

                try
                {
                    JSONObject jObject= new JSONObject(breedsRAW);
                    Iterator<String> keys = jObject.keys();
                    while( keys.hasNext() )
                    {
                        String key = keys.next();
                        lst.add(key);
                    }
                }
                catch (JSONException e){
                    e.printStackTrace();
                }

                return lst;
            }

        }.execute();
    }
}

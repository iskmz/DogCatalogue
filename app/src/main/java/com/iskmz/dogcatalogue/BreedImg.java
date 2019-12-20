package com.iskmz.dogcatalogue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BreedImg extends AppCompatActivity {


    String breedURL = "https://dog.ceo/api/breed/XXXXX/images/random";

    Context context;
    ImageButton btnBack, btnRefresh, btnAddFav;
    ImageView imgDog;
    TextView txtBreed;

    FavoritesSharedPref favorites;
    String currentImgURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breed_img);

        setPointers();
        setURL();
        loadImage();
    }

    private void setPointers() {
        this.context=this;
        txtBreed = findViewById(R.id.txtBreedType_BreedImg);
        imgDog = findViewById(R.id.imgBreedImg);
        btnBack = findViewById(R.id.btnBackToMain_fromBreedImg);
        btnRefresh = findViewById(R.id.btnRandom_FromBreed);
        btnAddFav = findViewById(R.id.btnAddFav_AtBreedImg);
        favorites = new FavoritesSharedPref(context);

        // onClicks //
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage();
            }
        });

        btnAddFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!currentImgURL.isEmpty())
                {
                    if(favorites.isInFav(currentImgURL))
                    {
                        favorites.removeFromFav(currentImgURL);
                        btnAddFav.setBackgroundResource(R.drawable.ic_star_orange_empty);
                    }
                    else
                    {
                        favorites.addtoFav(currentImgURL);
                        btnAddFav.setBackgroundResource(R.drawable.ic_star_orange_filled);
                    }
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void loadImage() {

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
                    connection = (HttpURLConnection) new URL(breedURL).openConnection();
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
                        currentImgURL = jsonObject.getString("message");
                        txtBreed.setText(getBreedType(currentImgURL));
                        adjustFavStatus(currentImgURL);
                        loadImageFinal(currentImgURL);
                    }
                    else
                    {
                        errorMsg();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }.execute();

    }

    private void adjustFavStatus(String imageURL) {
        if(favorites.isInFav(imageURL))
        {
            btnAddFav.setBackgroundResource(R.drawable.ic_star_orange_filled);
        }
        else
        {
            btnAddFav.setBackgroundResource(R.drawable.ic_star_orange_empty);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void loadImageFinal(String imageURL) {

        if(!Utils.isNetworkConnected(context))
        {
            errorMsg();
            return;
        }

        new AsyncTask<String, Void, Bitmap>(){

            @Override
            protected Bitmap doInBackground(String... args) {

                try {
                    return BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }


            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                if (bitmap != null) {
                    imgDog.setImageBitmap(bitmap);
                } else {
                    errorMsg();
                }

            }
        }.execute(imageURL);
    }



    private void errorMsg(){
        txtBreed.setText("");
        imgDog.setImageResource(R.drawable.error);
    }

    private String getBreedType(String breedURL) {
        String st = breedURL.substring(breedURL.indexOf("breeds/")+7);
        return st.substring(0,st.indexOf("/")).replace("-"," ");
    }


    private void setURL() {

        Intent intent = getIntent();
        String tmp = intent.getStringExtra("breed");
        breedURL = breedURL.replace("XXXXX",tmp);
    }
}

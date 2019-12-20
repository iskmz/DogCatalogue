package com.iskmz.dogcatalogue;

import android.annotation.SuppressLint;
import android.content.Context;
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

public class RandomImg extends AppCompatActivity {

    ImageView imgDog;
    ImageButton btnBack,btnRefresh, btnAddFav;
    TextView txtBreed;
    Context context;
    FavoritesSharedPref favorites;

    final String urlRandom = "https://dog.ceo/api/breeds/image/random";
    String currentImgURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_img);

        setPointers();
        loadRandomImage();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadRandomImage() {

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
                            connection = (HttpURLConnection) new URL(urlRandom).openConnection();
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
                            loadImage(currentImgURL);
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
    private void loadImage(String imageURL) {

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



    private String getBreedType(String imageURL) {
        String st = imageURL.substring(imageURL.indexOf("breeds/")+7);
        return "Breed:  "+st.substring(0,st.indexOf("/")).replace("-"," ");
    }

    private void errorMsg(){
        txtBreed.setText("");
        imgDog.setImageResource(R.drawable.error);
        currentImgURL="";
        btnAddFav.setBackgroundResource(R.drawable.ic_star_orange_empty);
    }

    private void setPointers() {

        this.context=this;
        imgDog = findViewById(R.id.imgRandom);
        btnBack = findViewById(R.id.btnBackToMain_fromRandom);
        btnRefresh = findViewById(R.id.btnRandomAgain);
        txtBreed = findViewById(R.id.txtBreedType);
        btnAddFav = findViewById(R.id.btnAddFav_AtRandomImg);
        favorites = new FavoritesSharedPref(context);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRandomImage();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
}

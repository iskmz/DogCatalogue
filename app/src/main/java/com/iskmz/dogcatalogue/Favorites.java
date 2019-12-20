package com.iskmz.dogcatalogue;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.Duration;

public class Favorites extends AppCompatActivity {

    Context context;
    ArrayList<ImageView> imgs;
    GridView favsGridView;
    ImageView btnBack, btnRefresh, btnChangeView;
    ProgressBar progressFavs;
    FavoritesSharedPref favorites;
    ArrayList<String> urls;

    int currentUrlIndex=-1;
    ImageView imgSingleView;
    ImageButton btnPrev,btnNext;
    RelativeLayout layImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        setPointers();
        loadFavs();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadFavs() {

        if(!Utils.isNetworkConnected(context))
        {
            errorMsg();
            return;
        }

        imgs = new ArrayList<>();
        favorites = new FavoritesSharedPref(context);

        progressFavs.setVisibility(View.VISIBLE); // start loading

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                urls = favorites.getAllurls();
                int picIndex=1;
                for (String url: urls)
                {
                    ImageView myImageView = new ImageView(context);
                    BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
                    bmpOptions.inSampleSize = 3; // to handle memory well ! //
                    Bitmap resizedBmp;
                    try {
                        resizedBmp = BitmapFactory.decodeStream((InputStream)new URL(url).getContent(),null,bmpOptions);
                        if(resizedBmp==null) throw new IOException();
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                    myImageView.setImageBitmap(resizedBmp);
                    myImageView.setId(picIndex++);
                    imgs.add(myImageView);
                }

                return null;
            }


            @Override
            protected void onPostExecute(Void aVoid) {
                favsGridView.setAdapter(new FavGridAdapter());
                progressFavs.setVisibility(View.GONE); // done loading

            }
        }.execute();
    }

    private void setPointers() {
        this.context = this;
        btnRefresh = findViewById(R.id.btnRefreshFavs);
        btnBack = findViewById(R.id.btnBackToMain_fromFavs);
        btnChangeView = findViewById(R.id.btnChangeView);
        favsGridView = findViewById(R.id.lstFavImgs);
        progressFavs = findViewById(R.id.progressFavs);
        favsGridView.setVisibility(View.VISIBLE); // VISIBLE on activity start !

        layImg = findViewById(R.id.laySingleImgView);
        btnNext = findViewById(R.id.btnNextImg);
        btnPrev = findViewById(R.id.btnPreviousImg);
        imgSingleView = findViewById(R.id.imgSingleView);
        layImg.setVisibility(View.GONE); // not visible on start of activity !

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFavs();
            }
        });

        favsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { showImgDialog(position); }
        });
        favsGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) { showImgDeleteDialog(position); return true; }});

        btnChangeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(favsGridView.getVisibility()==View.VISIBLE)
                {
                    layImg.setVisibility(View.VISIBLE);
                    favsGridView.setVisibility(View.GONE);
                    loadSingleImg();
                }
                else
                {
                    favsGridView.setVisibility(View.VISIBLE);
                    layImg.setVisibility(View.GONE);
                    loadFavs();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = currentUrlIndex==urls.size()-1?0:currentUrlIndex+1;
                loadSingleImg(index);
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = currentUrlIndex==0?urls.size()-1:currentUrlIndex-1;
                loadSingleImg(index);
            }
        });

        imgSingleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set and show snackbar
                if(currentUrlIndex!=-1) {
                    String breedType = getBreedType(urls.get(currentUrlIndex));
                    Snackbar.make((View) favsGridView.getParent(),
                            breedType, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        //to avoid api26+ policy restrictions // for images  ... //
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private void loadSingleImg() {
        favorites = new FavoritesSharedPref(context);
        urls = favorites.getAllurls();
        if (urls.isEmpty()) return;

        loadSingleImg(0); // load first one
    }

    @SuppressLint("StaticFieldLeak")
    private void loadSingleImg(int selectedIndex) {

        currentUrlIndex=selectedIndex;
        if(!Utils.isNetworkConnected(context)) { errorMsg(); return; }

        progressFavs.setVisibility(View.VISIBLE); // start loading
        btnPrev.setVisibility(View.GONE); // hide before loading !
        btnNext.setVisibility(View.GONE); // hide before loading !
        imgSingleView.setVisibility(View.GONE); // hide before loading !

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new AsyncTask<Void,Void,Void>()
        {
            Bitmap bmp;

            @Override
            protected Void doInBackground(Void... voids) {


                try {
                    bmp = BitmapFactory.decodeStream((InputStream)new URL(urls.get(currentUrlIndex)).getContent());
                    if(bmp==null) throw new IOException();
                } catch (IOException e) { e.printStackTrace(); return null; }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                imgSingleView.setImageBitmap(bmp);
                imgSingleView.setVisibility(View.VISIBLE); // done loading // can show
                progressFavs.setVisibility(View.GONE); // done loading
                btnPrev.setVisibility(View.VISIBLE); // done loading // can show
                btnNext.setVisibility(View.VISIBLE); // done loading // can show
            }
        }.execute();
    }

    private void showImgDeleteDialog(int position) {
        final String imgUrl = urls.get(position);

        new AlertDialog.Builder(context)
                .setTitle("Delete!")
                .setIcon(R.drawable.ic_warning)
                .setMessage("Delete \""+getBreedType(imgUrl)+"\" !\nAre you sure ?!\n")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        favorites.removeFromFav(imgUrl);
                        dialog.dismiss();
                        loadFavs();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showImgDialog(int position) {
        String imgUrl = urls.get(position);

        // load large bitmap // in a "blocking" way // NOT async !
        if(!Utils.isNetworkConnected(context)) { errorMsg(); return; }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Bitmap bmp;
        try {
            bmp = BitmapFactory.decodeStream((InputStream)new URL(imgUrl).getContent());
            if(bmp==null) throw new IOException();
        } catch (IOException e) { e.printStackTrace(); return; }

        // set Image view
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bmp);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setPadding(3, 3, 3, 3);

        // set and show dialog
        final Dialog dialogImg = new Dialog(context);
        dialogImg.setContentView(imageView);
        dialogImg.setCanceledOnTouchOutside(false);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dialogImg.dismiss(); }
        });
        dialogImg.show();

        // set and show snackbar
        String breedType = getBreedType(imgUrl);
        Snackbar.make((View)favsGridView.getParent(),breedType,Snackbar.LENGTH_LONG).show();
    }

    private String getBreedType(String imageURL) {
        String st = imageURL.substring(imageURL.indexOf("breeds/")+7);
        return st.substring(0,st.indexOf("/")).replace("-"," ");
    }

    private void errorMsg(){
        progressFavs.setVisibility(View.GONE);
        Toast.makeText(context,"Error loading favorites! check internet connection & try again!",
                Toast.LENGTH_LONG).show();
    }

    private class FavGridAdapter extends BaseAdapter {

        public FavGridAdapter() { }

        @Override public int getCount() { return imgs.size(); }
        @Override public Object getItem(int position) { return null; }
        @Override public long getItemId(int position) { return 0; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;
            imageView = imgs.get(position);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(3, 3, 3, 3);

            return imageView;
        }
    }
}

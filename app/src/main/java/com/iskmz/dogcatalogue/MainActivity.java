package com.iskmz.dogcatalogue;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Context context;
    TextView txtTitle, txtBtnFav, txtBtnBreeds, txtBtnRandom;
    ImageButton btnRandom, btnBreed, btnAbout, btnFav;
    Animation moveRandom, moveBreed, moveAbout, moveFav, moveTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPointers();
        setOnClicks();
    }

    private void setOnClicks() {

        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,RandomImg.class);
                startActivity(intent);
            }
        });

        btnBreed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,BreedList.class);
                startActivity(intent);
            }
        });

        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,Favorites.class);
                startActivity(intent);
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg="Developer: Iskandar Mazzawi\n\n";
                msg+="Powered By:\n\t\tDog API\n\t\thttps://dog.ceo/dog-api/\n";

                AlertDialog about = new AlertDialog.Builder(context)
                        .setMessage(msg)
                        .setTitle("About ...")
                        .setIcon(R.drawable.ic_info)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                about.setCanceledOnTouchOutside(false);
                about.show();
            }
        });
    }

    private void setPointers() {
        this.context=this;
        txtTitle = findViewById(R.id.txtTitle);
        btnAbout = findViewById(R.id.btnAboutMe);
        btnBreed = findViewById(R.id.btnBreed);
        btnRandom = findViewById(R.id.btnRandom);
        btnFav = findViewById(R.id.btnFav);
        txtBtnBreeds = findViewById(R.id.txtBtnBreeds);
        txtBtnFav = findViewById(R.id.txtBtnFav);
        txtBtnRandom = findViewById(R.id.txtBtnRandom);

        moveTitle = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.move_1_from_top);
        moveBreed = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.move_2_from_right);
        moveRandom = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.move_3_from_left);
        moveFav = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.move_4_from_right);
        moveAbout = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.move_5_from_left);

        txtTitle.startAnimation(moveTitle);
        btnBreed.startAnimation(moveBreed);
        txtBtnBreeds.startAnimation(moveBreed);
        btnRandom.startAnimation(moveRandom);
        txtBtnRandom.startAnimation(moveRandom);
        btnFav.startAnimation(moveFav);
        txtBtnFav.startAnimation(moveFav);
        btnAbout.startAnimation(moveAbout);


    }
}

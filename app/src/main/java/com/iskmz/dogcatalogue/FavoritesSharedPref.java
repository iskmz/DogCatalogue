package com.iskmz.dogcatalogue;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

public class FavoritesSharedPref {

    Context context;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    public FavoritesSharedPref(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
    }

    public void addtoFav(String URL) {
        String id = getIDFrom(URL);
        editor.putString(id, URL);
        editor.commit();
    }

    private String getIDFrom(String url)
    {
        String tmp = url.substring(url.indexOf("breeds/")+7);
        tmp = tmp.replace('/','_');
        tmp = tmp.substring(0,tmp.indexOf("."));
        return tmp;
    }

    public void removeFromFav(String URL) {
        editor.remove(getIDFrom(URL));
        editor.commit();
    }

    public boolean isInFav(String URL) {
        return !(preferences.getString(getIDFrom(URL), "X").equals("X"));
    }

    public ArrayList<String> getAllurls()
    {
        Map<String,?> keys = preferences.getAll();
        ArrayList<String> res = new ArrayList<>();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            res.add(entry.getValue().toString());
        }

        return res;
    }
}
package net.evendanan.chewbacca.model;

import android.net.Uri;

import com.google.gson.Gson;

public class GsonFactory {
    public static Gson build() {
        return new com.google.gson.GsonBuilder().serializeNulls().registerTypeAdapter(Uri.class, new UriInOut()).create();
    }
}

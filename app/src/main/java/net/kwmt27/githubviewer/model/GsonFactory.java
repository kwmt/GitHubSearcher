package net.kwmt27.githubviewer.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonFactory {
    public static Gson create() {
        return new GsonBuilder()
                .create();
    }
}

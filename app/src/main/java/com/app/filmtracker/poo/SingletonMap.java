package com.app.filmtracker.poo;

import java.util.HashMap;

public class SingletonMap extends HashMap<String, Object> {

    //Constants
    public static final String FIREBASE_AUTH_INSTANCE = "FIREBASE_AUTH_INSTANCE";
    public static final String FIREBASE_USER_INSTANCE = "FIREBASE_USER_INSTANCE";
    public static final String REQUEST_QUEUE = "REQUEST_QUEUE";
    public static final String CURRENT_FILM_DETAILS = "CURRENT_FILM_DETAILS";
    public static final String GENRES = "GENRES";
    public static final String CURRENT_FILMS_RECYCLER_VIEW = "CURRENT_FILMS_RECYCLER_VIEW";
    public static final String CURRENT_FILMS_HOLDER = "CURRENT_FILMS_HOLDER";
    public static final String CURRENT_FILMS_POSITION = "CURRENT_FILMS_POSITION";

    //Singleton Holder
    private static class SingletonHolder {
        private static final SingletonMap ourInstance = new SingletonMap();
    }

    public static SingletonMap getInstance() {
        return SingletonHolder.ourInstance;
    }

    private SingletonMap() {}
}

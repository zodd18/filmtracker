package com.app.filmtracker.poo;

import java.util.HashMap;

public class SingletonMap extends HashMap<String, Object> {

    //Constants
    public static final String FIREBASE_AUTH_INSTANCE = "FIREBASE_AUTH_INSTANCE";


    //Singleton Holder
    private static class SingletonHolder {
        private static final SingletonMap ourInstance = new SingletonMap();
    }

    public static SingletonMap getInstance() {
        return SingletonHolder.ourInstance;
    }

    private SingletonMap() {}
}

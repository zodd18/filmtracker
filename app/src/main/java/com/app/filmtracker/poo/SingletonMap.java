package com.app.filmtracker.poo;

import java.util.HashMap;

public class SingletonMap extends HashMap<String, Object> {

    //Constants
    public static final String FIREBASE_AUTH_INSTANCE = "FIREBASE_AUTH_INSTANCE";
    public static final String FIREBASE_USER_INSTANCE = "FIREBASE_USER_INSTANCE";
    public static final String HTTP_CLIENT = "HTTP_CLIENT";

    //Singleton Holder
    private static class SingletonHolder {
        private static final SingletonMap ourInstance = new SingletonMap();
    }

    public static SingletonMap getInstance() {
        return SingletonHolder.ourInstance;
    }

    private SingletonMap() {}
}

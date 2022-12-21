package me.rohank05;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();
    private static boolean endatabase = Boolean.parseBoolean(get("ENABLE_DB"));

    public static String get(String key) {
        return dotenv.get(key);
    }

    public static boolean getDB(){return endatabase;}

    public static void updateDB(boolean bool){endatabase = bool;}
}

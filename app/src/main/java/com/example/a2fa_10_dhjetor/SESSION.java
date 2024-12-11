package com.example.a2fa_10_dhjetor;

public class SESSION {
    private static String loggedEmail = "";

    public static String getLoggedEmail() {
        return loggedEmail;
    }

    public static void setLoggedEmail(String loggedEmail) {
        SESSION.loggedEmail = loggedEmail;
    }
}

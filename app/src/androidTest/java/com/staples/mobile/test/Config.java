package com.staples.mobile.test;

import java.util.Map;

public class Config {
    public static boolean doLiveCalls = true;

    static {
        Map<String, String> env = System.getenv();
        for(Map.Entry<String, String> entry : env.entrySet()) {
            String name = entry.getKey();
            if (name.equals("NOLIVECALLS")) {
                String value = entry.getValue().trim().toLowerCase();
                if (value.equals("1") || value.equals("true")) {
                    System.out.println("*** Disabling live tests ***");
                    doLiveCalls = false;
                }
            }
        }
    }
}

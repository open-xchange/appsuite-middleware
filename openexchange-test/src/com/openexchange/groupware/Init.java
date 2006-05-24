package com.openexchange.groupware;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.openexchange.server.ComfireConfig;
import com.openexchange.server.DBPool;
import com.openexchange.tools.conf.GlobalConfig;

public final class Init {

    private static boolean systemPropertiesLoaded = false;

    private static boolean serverConfLoaded = false;
    
    private static boolean dbInitialized = false;

    public static void loadSystemProperties() {
        if (!systemPropertiesLoaded) {
            Properties p = new Properties();
            try {
                p.load(new FileInputStream("openexchange-test/conf/test.properties"));
                GlobalConfig.loadConf(p.getProperty("openexchange.propfile"));
                systemPropertiesLoaded = true;
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    
    public static void loadServerConf() {
        if (!serverConfLoaded) {
            ComfireConfig cf = new ComfireConfig();
            cf.loadServerConf((String) ComfireConfig.properties.get("SERVERCONF"));
            serverConfLoaded = true;
        }
    }
    
    public static void initDB() {
        if (!dbInitialized) {
            loadSystemProperties();
            loadServerConf();
            new DBPool(0, 0);
            dbInitialized = true;
        }
    }
}

package com.openexchange.groupware;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.openexchange.server.ComfireConfig;
import com.openexchange.server.DBPool;
import com.openexchange.tools.conf.GlobalConfig;

/**
 * This class contains methods for initialising tests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Init {

    private static boolean testPropertiesLoaded = false;
    
    private static boolean ajaxPropertiesLoaded = false;

    private static boolean systemPropertiesLoaded = false;

    private static boolean serverConfLoaded = false;
    
    private static boolean dbInitialized = false;

    private static Properties testProps = null;

    private static Properties ajaxProps = null;

    public static void loadTestProperties() {
        if (!testPropertiesLoaded) {
            testProps = new Properties();
            try {
                testProps.load(new FileInputStream("openexchange-test/conf/test.properties"));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            testPropertiesLoaded = true;
        }
    }

    private static void loadAJAXProperties() {
        loadTestProperties();
        ajaxProps  = new Properties();
        try {
            ajaxProps.load(new FileInputStream(testProps.getProperty("ajaxPropertiesFile")));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        ajaxPropertiesLoaded = true;
    }

    public static Properties getAJAXProperties() {
        if (!ajaxPropertiesLoaded) {
            loadAJAXProperties();
        }
        return ajaxProps;
    }
    
    public static void loadSystemProperties() {
        if (!systemPropertiesLoaded) {
            loadTestProperties();
            GlobalConfig.loadConf(testProps.getProperty("openexchange.propfile"));
            systemPropertiesLoaded = true;
        }
    }
    
    public static void loadServerConf() {
        if (!serverConfLoaded) {
            loadSystemProperties();
            ComfireConfig cf = new ComfireConfig();
            cf.loadServerConf((String) ComfireConfig.properties.get("SERVERCONF"));
            serverConfLoaded = true;
        }
    }
    
    public static void initDB() {
        if (!dbInitialized) {
            loadServerConf();
            new DBPool(0, 0);
            dbInitialized = true;
        }
    }
}

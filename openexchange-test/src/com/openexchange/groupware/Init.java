package com.openexchange.groupware;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.openexchange.server.ComfireConfig;
import com.openexchange.server.DBPool;
import com.openexchange.sessiond.Sessiond;
import com.openexchange.sessiond.SessiondConfigWrapper;
import com.openexchange.sessiond.SessiondConnector;
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
	
	private static boolean sessiondInit = false;
	
	private static Properties testProps = null;
	
	private static Properties ajaxProps = null;
	
	public static void loadTestProperties() {
		if (!testPropertiesLoaded) {
			testProps = new Properties();
			try {
				testProps.load(new FileInputStream(System.getProperty("test.propfile")));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			testPropertiesLoaded = true;
		}
	}
	
    public static Properties getTestProperties() {
        if (!testPropertiesLoaded) {
            loadTestProperties();
        }
        return testProps;       
    }
    
    public static String getTestProperty(final String key) {
        return getTestProperties().getProperty(key);
    }

	private static void loadAJAXProperties() {
		loadTestProperties();
		ajaxProps  = new Properties();
		try {
			ajaxProps.load(new FileInputStream(testProps.getProperty("ajaxPropertiesFile")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ajaxPropertiesLoaded = true;
	}
	
	public static Properties getAJAXProperties() {
		if (!ajaxPropertiesLoaded) {
			loadAJAXProperties();
		}
		return ajaxProps;
	}

    public static String getAJAXProperty(final String key) {
        return getAJAXProperties().getProperty(key);
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
	
	public static void initSessiond() throws Exception {
		if (!sessiondInit) {
			String propfile = ComfireConfig.properties.getProperty("SESSIONDPROPERTIES");
			
			SessiondConfigWrapper config = new SessiondConfigWrapper(propfile);
			SessiondConnector.setConfig(config);
			new Sessiond(config);
			
			sessiondInit = true;
		}
	}

}

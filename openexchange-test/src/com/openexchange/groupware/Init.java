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

	private static boolean webdavPropertiesLoaded = false;

	private static boolean infostorePropertiesLoaded = false;
	
	private static boolean systemPropertiesLoaded = false;
	
	private static boolean serverConfLoaded = false;
	
	private static boolean dbInitialized = false;
	
	private static boolean sessiondInit = false;
	
	private static Properties testProps = null;
	
	private static Properties ajaxProps = null;
	
	private static Properties webdavProps = null;
	
	private static Properties infostoreProps = null;
	
	public static void loadTestProperties() {
		if (!testPropertiesLoaded) {
			testProps = new Properties();
			try {
                final String propfile = System.getProperty("test.propfile");
                if (null == propfile) {
                    throw new RuntimeException("Test properties file "
                        + "test.propfile is not defined as a JVM "
                        + "system property.");
                }
				testProps.load(new FileInputStream(propfile));
			} catch (IOException e) {
				throw new RuntimeException(e);
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
	
	private static void loadWebdavProperties() {
		loadTestProperties();
		webdavProps  = new Properties();
		try {
			webdavProps.load(new FileInputStream(testProps.getProperty("webdavPropertiesFile")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		webdavPropertiesLoaded = true;
	}
	
	public static Properties getWebdavProperties() {
		if (!webdavPropertiesLoaded) {
			loadWebdavProperties();
		}
		return webdavProps;
	}
	
	public static String getAJAXProperty(final String key) {
        return getAJAXProperties().getProperty(key);
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
	
	private static void loadInfostoreProperties() {
		if(infostorePropertiesLoaded)
			return;
		loadTestProperties();
		infostoreProps  = new Properties();
		try {
			infostoreProps.load(new FileInputStream(testProps.getProperty("infostorePropertiesFile")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		infostorePropertiesLoaded = true;
	}

	public static Properties getInfostoreProperties() {
		loadTestProperties();
		if (!infostorePropertiesLoaded) {
			loadInfostoreProperties();
		}
		return infostoreProps;
	}
}

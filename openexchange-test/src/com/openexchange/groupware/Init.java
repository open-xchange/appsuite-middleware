package com.openexchange.groupware;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.openexchange.configuration.ConfigDB;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.database.DatabaseInit;
import com.openexchange.groupware.contexts.ContextInit;
import com.openexchange.sessiond.Sessiond;
import com.openexchange.sessiond.SessiondConfigWrapper;
import com.openexchange.sessiond.SessiondConnector;

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

	private static boolean dbInitialized = false;

    private static boolean contextInitialized = false;
    
	private static boolean sessiondInit = false;

	private static Properties testProps = null;

	private static Properties ajaxProps = null;

	private static Properties webdavProps = null;

	private static Properties infostoreProps = null;

	private static boolean isAjaxDirInitialized = false;

	private static String[] ajaxPropFiles;

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
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			testPropertiesLoaded = true;
		}
	}

	private static void loadAJAXProperties() {
		loadTestProperties();
		ajaxProps = new Properties();
		try {
			ajaxProps.load(new FileInputStream(getFileName()));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		ajaxPropertiesLoaded = true;
	}

	private static String getFileName() {

		String retval = null;
		if (testProps.getProperty("ajaxPropertiesDir") != null) {
			if (!isAjaxDirInitialized) {
				synchronized (Init.class) {
					if (!isAjaxDirInitialized) {
						initAjaxProperties();
					}
				}
			}
			retval = ajaxPropFiles[(int) (Math.random() * ajaxPropFiles.length)];
		}
		else {
			retval = testProps.getProperty("ajaxPropertiesFile");
		}

		return retval;
	}

	private static void initAjaxProperties() {
		// Welches Verzeichnis soll gelesen werden?
		String ajaxPropertiesDir = testProps.getProperty("ajaxPropertiesDir");
		// ist der Pfad mit abschliessendem "/" ? Wenn nicht, packe den dazu:
		if (!ajaxPropertiesDir.endsWith(System.getProperty("file.separator"))) {
			ajaxPropertiesDir = new StringBuilder().append(ajaxPropertiesDir)
					.append(System.getProperty("file.separator")).toString();
		}
		File dir = new File(ajaxPropertiesDir);
		File myFile;
		// Lese das Verzeichnis und packe alle Files in das Array ajaxPropFiles
		// Aber nur dann, wenn es ein File ist (kein Directory) und es auch
		// gelesen werden kann.
		if (dir.isDirectory()) {
			// Hilfsliste:
			List<String> fileList = new ArrayList<String>();
			// Pruefe jeden im Verzeichnis vorhandenen Namen:
			for (String fileName : dir.list()) {
				myFile = new File(new StringBuilder().append(ajaxPropertiesDir)
						.append(fileName).toString());
				if (!myFile.isDirectory() && myFile.canRead()
						&& !myFile.getName().startsWith(".")) {
					fileList.add(myFile.getAbsolutePath());
				}
			}
			// Umladen:
			ajaxPropFiles = new String[fileList.size()];
			System.arraycopy(fileList.toArray(), 0, ajaxPropFiles, 0, fileList
					.size());
		}

		isAjaxDirInitialized = true;
	}

	public static Properties getAJAXProperties() {

		if (!ajaxPropertiesLoaded
				|| testProps.getProperty("ajaxPropertiesDir") != null) {
			loadAJAXProperties();
		}
		return ajaxProps;
	}

	private static void loadWebdavProperties() {
		loadTestProperties();
		webdavProps = new Properties();
		try {
			webdavProps.load(new FileInputStream(testProps
					.getProperty("webdavPropertiesFile")));
		}
		catch (IOException e) {
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

	public static void loadSystemProperties() throws AbstractOXException {
		if (!systemPropertiesLoaded) {
			loadTestProperties();
            final String propFileName = testProps.getProperty(
                "openexchange.propfile");
            System.setProperty("openexchange.propfile", propFileName); //FIXME
            SystemConfig.getInstance().start();
			systemPropertiesLoaded = true;
		}
	}

	public synchronized static void initDB() throws AbstractOXException {
		if (!dbInitialized) {
			loadSystemProperties();
	        ConfigDB.getInstance().start();
	        DatabaseInit.getInstance().start();
			dbInitialized = true;
		}
	}

	public synchronized static void stopDB() throws Exception {
		if (dbInitialized) {
			dbInitialized = false;
			DatabaseInit.getInstance().stop();
			ConfigDB.getInstance().stop();
		}
	}

    public synchronized static void initContext() throws AbstractOXException {
        if (!contextInitialized) {
            loadSystemProperties();
            ContextInit.init();
            contextInitialized = true;
        }
    }
    
	public static void initSessiond() throws Exception {
		if (!sessiondInit) {
			String propfile = SystemConfig.getProperty("SESSIONDPROPERTIES");

			SessiondConfigWrapper config = new SessiondConfigWrapper(propfile);
			SessiondConnector.setConfig(config);
			new Sessiond(config);

			sessiondInit = true;
		}
	}

	private static void loadInfostoreProperties() {
		if (infostorePropertiesLoaded)
			return;
		loadTestProperties();
		infostoreProps = new Properties();
		try {
			infostoreProps.load(new FileInputStream(testProps
					.getProperty("infostorePropertiesFile")));
		}
		catch (IOException e) {
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

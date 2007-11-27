package com.openexchange.groupware;

import com.openexchange.config.Configuration;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.groupware.impl.GroupwareInit;
import com.openexchange.monitoring.services.MonitoringConfiguration;
import com.openexchange.server.Initialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

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

    private static final List<Initialization> started = new ArrayList<Initialization>();
    private static boolean running;


    private static final Map<Class, Object> services = new HashMap<Class,Object>();


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


    private static final Initialization[] inits = new Initialization[] {
	/**
	 * Reads system.properties.
	 */
	com.openexchange.configuration.SystemConfig.getInstance(),
	/**
	 * Reads configdb.properties.
	 */
	com.openexchange.configuration.ConfigDB.getInstance(),
	/**
	 * Read in update tasks
	 */
	com.openexchange.groupware.update.UpdateTaskCollectionInit.getInstance(),
	/**
	 * Reads the calendar.properties.
	 */
	com.openexchange.groupware.calendar.CalendarConfig.getInstance(),
	/**
	 * Reads the participant.properties.
	 */
	com.openexchange.groupware.configuration.ParticipantConfig.getInstance(),
	/**
	 * Sets the caching system JCS up.
	 */
	com.openexchange.cache.impl.Configuration.getInstance(),
	/**
	 * Connection pools for ConfigDB and database assignments for contexts.
	 */
	com.openexchange.database.DatabaseInit.getInstance(),
	/**
	 * Starts HTTP serlvet manager
	 */
	com.openexchange.tools.servlet.http.HttpServletManagerInit.getInstance(),
	/**
	 * Setup of ContextStorage and LoginInfo.
	 */
	com.openexchange.groupware.contexts.impl.ContextInit.getInstance(),
	/**
	 * Folder initialization
	 */
	com.openexchange.tools.oxfolder.OXFolderProperties.getInstance(),
	/**
	 * Mail initialization
	 */
	com.openexchange.mail.MailInitialization.getInstance(),
	/**
	 * Infostore Configuration
	 */
	com.openexchange.groupware.infostore.InfostoreConfig.getInstance(),
	/**
	 * Contact Configuration
	 */
	com.openexchange.groupware.contact.ContactConfig.getInstance(),
	/**
	 * Attachment Configuration
	 */
	com.openexchange.groupware.attach.AttachmentConfig.getInstance(),
	/**
	 * User configuration init
	 */
	com.openexchange.groupware.userconfiguration.UserConfigurationStorageInit.getInstance(),
	/**
	 * Setup of SetupLink for Config Jump.
	 */
	com.openexchange.groupware.integration.SetupLinkInit.getInstance(),
	/**
	 * Notification Configuration
	 */
	com.openexchange.groupware.notify.NotificationConfig.getInstance(),
	/**
	 * Sets up the configuration tree.
	 */
	com.openexchange.groupware.settings.ConfigTreeInit.getInstance(),
	/**
	 * Responsible for starting and stopping the EventQueue
	 */
	new com.openexchange.event.EventInit()};

    public static void startServer() throws Exception {
        if(running)
            return;
        running = true;
        loadTestProperties();
        injectTestServices();
        final String propFileName = testProps.getProperty(
                "openexchange.propfile");
        System.setProperty("openexchange.propfile", propFileName); 
        for(Initialization init : inits) {
            init.start();
            started.add(init);
        }
        GroupwareInit.init();
    }

    private static void injectTestServices() throws Exception {
        // Since unit tests are running outside the OSGi container
        // we'll have to do the service wiring differently.
        // This method duplicates statically what the OSGi container
        // handles dynamically

        startAndInjectConfigBundle();
        startAndInjectMonitoringBundle();
    }

    private static void startAndInjectConfigBundle() {
        Configuration config = new ConfigurationImpl(getTestProperty("openexchange.propdir"));
        services.put(Configuration.class, config);
    }

    private static void startAndInjectMonitoringBundle() throws Exception {
        // First lookup services monitoring depends on and inject them
        MonitoringConfiguration.getInstance().setService((Configuration)services.get(Configuration.class));
    }



    public static void stopServer() throws AbstractOXException {
        //for(Initialization init: started) { init.stop(); }
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

package com.openexchange.groupware;

import com.openexchange.config.Configuration;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.i18n.impl.ResourceBundleDiscoverer;
import com.openexchange.i18n.impl.I18nImpl;
import com.openexchange.i18n.tools.I18nServices;
import com.openexchange.monitoring.services.MonitoringConfiguration;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.SessiondService;
import com.openexchange.sessiond.impl.ConfigurationService;
import com.openexchange.sessiond.impl.SessiondConnectorImpl;
import com.openexchange.sessiond.impl.SessiondInit;
import com.openexchange.test.TestInit;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * This class contains methods for initialising tests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Init {

    private static boolean infostorePropertiesLoaded = false;

	private static boolean systemPropertiesLoaded = false;

	private static boolean dbInitialized = false;

    private static boolean contextInitialized = false;
    
	private static boolean sessiondInit = false;

    //private static Properties infostoreProps = null;

    private static final List<Initialization> started = new ArrayList<Initialization>();
    private static boolean running;


    private static final Map<Class, Object> services = new HashMap<Class,Object>();


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
	new com.openexchange.event.EventInit(),

    SessiondInit.getInstance()};

    public static void startServer() throws Exception {
        if(running)
            return;
        running = true;
        System.setProperty("openexchange.propdir", TestInit.getTestProperty("openexchange.propdir"));

        injectTestServices();
        for(Initialization init : inits) {
            init.start();
            started.add(init);
        }
    }

    private static void injectTestServices() throws Exception {
        // Since unit tests are running outside the OSGi container
        // we'll have to do the service wiring differently.
        // This method duplicates statically what the OSGi container
        // handles dynamically

        startAndInjectConfigBundle();
        startAndInjectI18NBundle();
        startAndInjectMonitoringBundle();
        startAndInjectSessiondBundle();
    }

    private static void startAndInjectI18NBundle() throws FileNotFoundException {
        Configuration config = (Configuration)services.get(Configuration.class);
        String directory_name = config.getProperty("i18n.language.path");
		File dir = new File(directory_name);
        I18nServices i18nServices = I18nServices.getInstance();
        for (ResourceBundle rc : new ResourceBundleDiscoverer(dir).getResourceBundles()) {
            i18nServices.addService(rc.getLocale(), new I18nImpl(rc));    
        }
    }

    private static void startAndInjectConfigBundle() {
        Configuration config = new ConfigurationImpl();
        services.put(Configuration.class, config);
    }

    private static void startAndInjectMonitoringBundle() throws Exception {
        // First lookup services monitoring depends on and inject them
        MonitoringConfiguration.getInstance().setService((Configuration)services.get(Configuration.class));
    }

    private static void startAndInjectSessiondBundle() throws Exception {
        ConfigurationService.getInstance().setService((Configuration)services.get(Configuration.class));
        SessiondService.getInstance().setService(new SessiondConnectorImpl());
    }



    public static void stopServer() throws AbstractOXException {
        //for(Initialization init: started) { init.stop(); }
    }

}

package com.openexchange.groupware;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.i18n.impl.I18nImpl;
import com.openexchange.i18n.impl.ResourceBundleDiscoverer;
import com.openexchange.i18n.tools.I18nServices;
import com.openexchange.imap.IMAPProvider;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;
//import com.openexchange.server.services.SessiondService;
import com.openexchange.sessiond.impl.SessiondConnectorImpl;
import com.openexchange.sessiond.impl.SessiondInit;
import com.openexchange.test.TestInit;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.mail.MailInitialization;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.push.udp.EventAdminService;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.internal.JCSCacheService;
import com.openexchange.caching.internal.JCSCacheServiceInit;
import org.osgi.service.event.EventAdmin;

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
	//com.openexchange.cache.impl.Configuration.getInstance(),
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
	 * Notification Configuration
	 */
	com.openexchange.groupware.notify.NotificationConfig.getInstance(),
	/**
	 * Sets up the configuration tree.
	 */
	com.openexchange.groupware.settings.impl.ConfigTreeInit.getInstance(),
	/**
	 * Responsible for starting and stopping the EventQueue
	 */
 
	new com.openexchange.event.impl.EventInit(),
            
    SessiondInit.getInstance()};
    private static ConfigurationServiceHolder configur;

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
        startAndInjectMailBundle();
        startAndInjectI18NBundle();
        startAndInjectMonitoringBundle();
        startAndInjectSessiondBundle();
        startAndInjectPushUDPBundle();
        startAndInjectCache();
    }

    private static void startAndInjectI18NBundle() throws FileNotFoundException {
        ConfigurationService config = (ConfigurationService)services.get(ConfigurationService.class);
        String directory_name = config.getProperty("i18n.language.path");
		File dir = new File(directory_name);
        I18nServices i18nServices = I18nServices.getInstance();
        try {
            for (ResourceBundle rc : new ResourceBundleDiscoverer(dir).getResourceBundles()) {
                i18nServices.addService(rc.getLocale(), new I18nImpl(rc));    
            }
        } catch (final NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static void startAndInjectConfigBundle() {
        ConfigurationService config = new ConfigurationImpl();
        services.put(ConfigurationService.class, config);
        ServerServiceRegistry.getInstance().addService(ConfigurationService.class, config);
    }

    private static void startAndInjectMonitoringBundle() throws Exception {
        // First lookup services monitoring depends on and inject them
    }

    private static void startAndInjectMailBundle() throws Exception {
        //MailInitialization.getInstance().setConfigurationServiceHolder(getConfigurationServiceHolder());
    	/*
    	 * Init config
    	 */
    	MailProperties.getInstance().loadProperties();
    	
    	IMAPProperties.getInstance().setConfigurationServiceHolder(getConfigurationServiceHolder());
    	/*
		 * Register IMAP bundle
		 */
		MailProviderRegistry.registerMailProvider("imap_imaps", new IMAPProvider());
    }


    private static void startAndInjectSessiondBundle() throws Exception {
        //ConfigurationService.getInstance().setService((Configuration)services.get(Configuration.class));
        //SessiondService.getInstance().setService(new SessiondConnectorImpl());
        SessiondInit.getInstance().setConfigurationServiceHolder(getConfigurationServiceHolder());
    }

    private static void startAndInjectPushUDPBundle() throws Exception {
        ServerServiceRegistry.getInstance().addService(EventAdmin.class,new TestEventAdmin());
        EventAdminService.getInstance().setService(new TestEventAdmin());
        // SessiondService.getInstance().setService(new SessiondConnectorImpl());
    }


    public static void startAndInjectCache() throws CacheException {
        JCSCacheServiceInit.getInstance().start((ConfigurationService) services.get(ConfigurationService.class));
        ServerServiceRegistry.getInstance().addService(CacheService.class,JCSCacheService.getInstance());
    }

    public static void stopServer() throws AbstractOXException {
        //for(Initialization init: started) { init.stop(); }
    }



    public static ConfigurationServiceHolder getConfigurationServiceHolder() throws Exception {
        ConfigurationServiceHolder csh = ConfigurationServiceHolder.newInstance();
        csh.setService((ConfigurationService) services.get(ConfigurationService.class));
        return csh;
    }
}

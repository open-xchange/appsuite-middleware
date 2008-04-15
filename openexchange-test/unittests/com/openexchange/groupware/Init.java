package com.openexchange.groupware;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.service.event.EventAdmin;

import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.internal.JCSCacheService;
import com.openexchange.caching.internal.JCSCacheServiceInit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.i18n.impl.I18nImpl;
import com.openexchange.i18n.impl.ResourceBundleDiscoverer;
import com.openexchange.i18n.tools.I18nServices;
import com.openexchange.imap.IMAPProvider;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.push.udp.EventAdminService;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.SessiondConnectorImpl;
import com.openexchange.sessiond.impl.SessiondInit;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.spamhandler.defaultspamhandler.DefaultSpamHandler;
import com.openexchange.spamhandler.spamassassin.SpamAssassinSpamHandler;
import com.openexchange.test.TestInit;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.servlet.ServletConfigLoader;
import com.openexchange.tools.servlet.http.HttpManagersInit;

/**
 * This class contains methods for initialising tests.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Init {

	// private static Properties infostoreProps = null;

	private static final List<Initialization> started = new ArrayList<Initialization>();

	private static boolean running;

	private static final Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

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
	// com.openexchange.cache.impl.Configuration.getInstance(),
	/**
	 * Connection pools for ConfigDB and database assignments for
	 * contexts.
	 */
	com.openexchange.database.DatabaseInit.getInstance(),
	/**
	 * Starts HTTP servlet manager
	 */
	new Initialization() {
        public void start() throws AbstractOXException {
            AJPv13Config.getInstance().start();
            ServletConfigLoader.initDefaultInstance(AJPv13Config.getServletConfigs());
            HttpManagersInit.getInstance().start();
        }
        public void stop() throws AbstractOXException {
            HttpManagersInit.getInstance().stop();
            ServletConfigLoader.resetDefaultInstance();
            AJPv13Config.getInstance().stop();
        }
	},
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

	SessiondInit.getInstance() };

	public static void startServer() throws Exception {
		if (running)
			return;
		running = true;
		System.setProperty("openexchange.propdir", TestInit.getTestProperty("openexchange.propdir"));

		injectTestServices();
		for (Initialization init : inits) {
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
		startAndInjectCache();
		startAndInjectMailBundle();
		startAndInjectSpamHandler();
		startAndInjectI18NBundle();
		startAndInjectMonitoringBundle();
		startAndInjectSessiondBundle();
		startAndInjectPushUDPBundle();
	}

	private static void startAndInjectI18NBundle() throws FileNotFoundException {
		ConfigurationService config = (ConfigurationService) services.get(ConfigurationService.class);
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
		// MailInitialization.getInstance().setConfigurationServiceHolder(getConfigurationServiceHolder());
		/*
		 * Init config
		 */
		MailProperties.getInstance().loadProperties();
		
		IMAPServiceRegistry.getServiceRegistry().addService(ConfigurationService.class, services.get(ConfigurationService.class));
		IMAPServiceRegistry.getServiceRegistry().addService(CacheService.class, services.get(CacheService.class));
		
		/*
		 * Register IMAP bundle
		 */
		MailProviderRegistry.registerMailProvider("imap_imaps", IMAPProvider.getInstance());
	}

	private static void startAndInjectSpamHandler() {
		SpamHandlerRegistry.registerSpamHandler(DefaultSpamHandler.getInstance().getSpamHandlerName(),
				DefaultSpamHandler.getInstance());
		SpamHandlerRegistry.registerSpamHandler(SpamAssassinSpamHandler.getInstance().getSpamHandlerName(),
				SpamAssassinSpamHandler.getInstance());
	}

	private static void startAndInjectSessiondBundle() throws Exception {
		// ConfigurationService.getInstance().setService((Configuration)services.get(Configuration.class));
		// SessiondService.getInstance().setService(new
		// SessiondConnectorImpl());
		SessiondInit.getInstance().setConfigurationServiceHolder(getConfigurationServiceHolder());
		ServerServiceRegistry.getInstance().addService(SessiondService.class, new SessiondConnectorImpl());
	}

	private static void startAndInjectPushUDPBundle() throws Exception {
		ServerServiceRegistry.getInstance().addService(EventAdmin.class, new TestEventAdmin());
		EventAdminService.getInstance().setService(new TestEventAdmin());

		// SessiondService.getInstance().setService(new
		// SessiondConnectorImpl());
	}

	public static void startAndInjectCache() throws CacheException {
		JCSCacheServiceInit.getInstance().start((ConfigurationService) services.get(ConfigurationService.class));
		final CacheService cache = JCSCacheService.getInstance();
		services.put(CacheService.class, cache);
		ServerServiceRegistry.getInstance().addService(CacheService.class, cache);
	}

	public static void stopServer() {
		// for(Initialization init: started) { init.stop(); }
	}

	public static ConfigurationServiceHolder getConfigurationServiceHolder() throws Exception {
		ConfigurationServiceHolder csh = ConfigurationServiceHolder.newInstance();
		csh.setService((ConfigurationService) services.get(ConfigurationService.class));
		return csh;
	}
}

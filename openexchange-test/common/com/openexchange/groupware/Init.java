
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
import com.openexchange.calendar.api.AppointmentSqlFactory;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.context.ContextService;
import com.openexchange.context.internal.ContextServiceImpl;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.engine.internal.ConversionEngineRegistry;
import com.openexchange.conversion.engine.internal.ConversionServiceImpl;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ical4j.ICal4JEmitter;
import com.openexchange.data.conversion.ical.ical4j.ICal4JParser;
import com.openexchange.data.conversion.ical.ical4j.internal.OXResourceResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.OXUserResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.event.impl.AppointmentEventInterface;
import com.openexchange.event.impl.EventDispatcher;
import com.openexchange.event.impl.EventQueue;
import com.openexchange.event.impl.TaskEventInterface;
import com.openexchange.group.internal.GroupInit;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.configuration.ParticipantConfig;
import com.openexchange.groupware.contact.datahandler.ContactInsertDataHandler;
import com.openexchange.i18n.impl.I18nImpl;
import com.openexchange.i18n.impl.POTranslationsDiscoverer;
import com.openexchange.i18n.impl.ResourceBundleDiscoverer;
import com.openexchange.i18n.impl.TranslationsI18N;
import com.openexchange.i18n.parsing.Translations;
import com.openexchange.i18n.tools.I18nServices;
import com.openexchange.imap.IMAPProvider;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.conversion.VCardMailPartDataSource;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.mailaccount.internal.MailAccountStorageInit;
import com.openexchange.push.udp.EventAdminService;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.internal.ResourceServiceImpl;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.SessiondInit;
import com.openexchange.sessiond.impl.SessiondServiceImpl;
import com.openexchange.sessiond.services.SessiondServiceRegistry;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.spamhandler.defaultspamhandler.DefaultSpamHandler;
import com.openexchange.spamhandler.spamassassin.SpamAssassinSpamHandler;
import com.openexchange.test.TestInit;
import com.openexchange.timer.Timer;
import com.openexchange.timer.internal.TimerImpl;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.servlet.ServletConfigLoader;
import com.openexchange.tools.servlet.http.HttpManagersInit;
import com.openexchange.user.UserService;
import com.openexchange.user.internal.UserServiceImpl;
import com.openexchange.xml.jdom.JDOMParser;
import com.openexchange.xml.jdom.impl.JDOMParserImpl;
import com.openexchange.xml.spring.SpringParser;
import com.openexchange.xml.spring.impl.DefaultSpringParser;

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
         * Sets the caching system JCS up.
         */
        // com.openexchange.cache.impl.Configuration.getInstance(),
        /**
         * Connection pools for ConfigDB and database assignments for contexts.
         */
        com.openexchange.database.DatabaseInit.getInstance(),
        /**
         * Initialization for alias charset provider
         */
        new com.openexchange.charset.AliasCharsetProviderInit(),
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
         * Setup of user storage.
         */
        com.openexchange.groupware.ldap.UserStorageInit.getInstance(),
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
         * Attachment Configuration
         */
        com.openexchange.groupware.attach.AttachmentConfig.getInstance(),
        /**
         * User configuration init
         */
        com.openexchange.groupware.userconfiguration.UserConfigurationStorageInit.getInstance(),
        com.openexchange.groupware.ldap.UserStorageInit.getInstance(),
        /**
         * Resource storage init
         */
        com.openexchange.resource.internal.ResourceStorageInit.getInstance(),
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

        SessiondInit.getInstance(),

        new GroupInit() };

    public static void injectProperty() {
        System.setProperty("openexchange.propdir", TestInit.getTestProperty("openexchange.propdir"));
    }

    public static void startServer() throws Exception {
        if (running) {
            return;
        }
        running = true;
        injectProperty();

        injectTestServices();
        for (final Initialization init : inits) {
            init.start();
            started.add(init);
        }
    }

    private static void injectTestServices() throws Exception {
        // Since unit tests are running outside the OSGi container
        // we'll have to do the service wiring differently.
        // This method duplicates statically what the OSGi container
        // handles dynamically
        startAndInjectCalendarServices();
        startAndInjectTimerBundle();
        startAndInjectConfigBundle();
        startAndInjectConfiguration();
        startAndInjectCache();
        startAndInjectI18NBundle();
        startAndInjectMonitoringBundle();
        startAndInjectSessiondBundle();
        startAndInjectEventBundle();
        startAndInjectContextService();
        startAndInjectUserService();
        startAndInjectResourceService();
        startAndInjectMailAccountStorageService();
        startAndInjectMailBundle();
        startAndInjectSpamHandler();
        startAndInjectICalServices();
        startAndInjectConverterService();
        startAndInjectXMLServices();
    }

    /**
     * 
     */
    private static void startAndInjectCalendarServices() {
        ServerServiceRegistry.getInstance().addService(CalendarCollectionService.class, new CalendarCollection());
        ServerServiceRegistry.getInstance().addService(AppointmentSqlFactoryService.class, new AppointmentSqlFactory());
    }

    private static void startAndInjectXMLServices() {
        final SpringParser springParser = new DefaultSpringParser();
        ServerServiceRegistry.getInstance().addService(SpringParser.class, springParser);

        final JDOMParser jdomParser = new JDOMParserImpl();
        ServerServiceRegistry.getInstance().addService(JDOMParser.class, jdomParser);
    }

    public static void startAndInjectI18NBundle() throws FileNotFoundException {
        final ConfigurationService config = (ConfigurationService) services.get(ConfigurationService.class);
        final String directory_name = config.getProperty("i18n.language.path");
        final File dir = new File(directory_name);
        final I18nServices i18nServices = I18nServices.getInstance();
        try {
            for (final ResourceBundle rc : new ResourceBundleDiscoverer(dir).getResourceBundles()) {
                i18nServices.addService(rc.getLocale(), new I18nImpl(rc));
            }
            for (final Translations tr : new POTranslationsDiscoverer(dir).getTranslations()) {
                i18nServices.addService(tr.getLocale(), new TranslationsI18N(tr));
            }
        } catch (final NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static void startAndInjectTimerBundle() {
        final TimerImpl timer = new TimerImpl();
        timer.start();
        services.put(Timer.class, timer);
        ServerServiceRegistry.getInstance().addService(Timer.class, timer);
    }

    public static void startAndInjectConfigBundle() {
        final ConfigurationService config = new ConfigurationImpl();
        services.put(ConfigurationService.class, config);
        ServerServiceRegistry.getInstance().addService(ConfigurationService.class, config);
    }

    public static void startAndInjectConfiguration() {
        final ConfigurationService config = (ConfigurationService) services.get(ConfigurationService.class);
        ParticipantConfig.getInstance().initialize(config);
    }

    private static void startAndInjectMonitoringBundle() {
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
        IMAPServiceRegistry.getServiceRegistry().addService(UserService.class, services.get(UserService.class));
        IMAPServiceRegistry.getServiceRegistry().addService(MailAccountStorageService.class, services.get(MailAccountStorageService.class));
        IMAPServiceRegistry.getServiceRegistry().addService(UnifiedINBOXManagement.class, services.get(UnifiedINBOXManagement.class));

        /*
         * Register IMAP bundle
         */
        MailProviderRegistry.registerMailProvider("imap_imaps", IMAPProvider.getInstance());
    }

    private static void startAndInjectMailAccountStorageService() throws Exception {
        // Initialize mail account storage
        new MailAccountStorageInit().start();
        services.put(MailAccountStorageService.class, MailAccountStorageInit.newMailAccountStorageService());
        services.put(UnifiedINBOXManagement.class, MailAccountStorageInit.newUnifiedINBOXManagement());
    }

    private static void startAndInjectSpamHandler() {
        SpamHandlerRegistry.registerSpamHandler(DefaultSpamHandler.getInstance().getSpamHandlerName(), DefaultSpamHandler.getInstance());
        SpamHandlerRegistry.registerSpamHandler(
            SpamAssassinSpamHandler.getInstance().getSpamHandlerName(),
            SpamAssassinSpamHandler.getInstance());
    }

    private static void startAndInjectResourceService() {
        final ResourceService resources = ResourceServiceImpl.getInstance();
        services.put(ResourceService.class, resources);
        ServerServiceRegistry.getInstance().addService(ResourceService.class, resources);
    }

    private static void startAndInjectUserService() {
        final UserService us = new UserServiceImpl();
        services.put(UserService.class, us);
        ServerServiceRegistry.getInstance().addService(UserService.class, us);
    }

    private static void startAndInjectContextService() {
        final ContextService cs = new ContextServiceImpl();
        services.put(ContextService.class, cs);
        ServerServiceRegistry.getInstance().addService(ContextService.class, cs);
    }

    private static void startAndInjectSessiondBundle() {
        SessiondServiceRegistry.getServiceRegistry().addService(ConfigurationService.class, services.get(ConfigurationService.class));
        SessiondServiceRegistry.getServiceRegistry().addService(Timer.class, services.get(Timer.class));
        ServerServiceRegistry.getInstance().addService(SessiondService.class, new SessiondServiceImpl());
    }

    private static void startAndInjectEventBundle() throws Exception {
        EventQueue.setNewEventDispatcher(new EventDispatcher() {

            public void addListener(final AppointmentEventInterface listener) {

            }

            public void addListener(final TaskEventInterface listener) {

            }
        });
        ServerServiceRegistry.getInstance().addService(EventAdmin.class, TestEventAdmin.getInstance());
        EventAdminService.getInstance().setService(TestEventAdmin.getInstance());

        // SessiondService.getInstance().setService(new
        // SessiondConnectorImpl());
    }

    public static void startAndInjectCache() throws CacheException {
        JCSCacheServiceInit.getInstance().start((ConfigurationService) services.get(ConfigurationService.class));
        final CacheService cache = JCSCacheService.getInstance();
        services.put(CacheService.class, cache);
        ServerServiceRegistry.getInstance().addService(CacheService.class, cache);
    }

    public static void startAndInjectICalServices() {
        final ICal4JParser parser = new ICal4JParser();
        final ICal4JEmitter emitter = new ICal4JEmitter();

        final OXUserResolver userResolver = new OXUserResolver();
        userResolver.setUserService((UserService) services.get(UserService.class));
        Participants.userResolver = userResolver;

        final OXResourceResolver resourceResolver = new OXResourceResolver();
        resourceResolver.setResourceService((ResourceService) services.get(ResourceService.class));
        Participants.resourceResolver = resourceResolver;

        services.put(ICalParser.class, parser);
        services.put(ICalEmitter.class, emitter);

        ServerServiceRegistry.getInstance().addService(ICalParser.class, parser);
        ServerServiceRegistry.getInstance().addService(ICalEmitter.class, emitter);
    }

    public static void startAndInjectConverterService() {
        ConversionEngineRegistry.getInstance().putDataHandler("com.openexchange.contact", new ContactInsertDataHandler());
        ConversionEngineRegistry.getInstance().putDataSource("com.openexchange.mail.vcard", new VCardMailPartDataSource());

        final ConversionService conversionService = new ConversionServiceImpl();
        services.put(ConversionService.class, conversionService);
        ServerServiceRegistry.getInstance().addService(ConversionService.class, conversionService);
    }

    public static void stopServer() {
        // This causes NPEs everywhere in the tests.
        // for (final Initialization init: started) {
        // init.stop();
        // }
    }

    public static ConfigurationServiceHolder getConfigurationServiceHolder() throws Exception {
        final ConfigurationServiceHolder csh = ConfigurationServiceHolder.newInstance();
        csh.setService((ConfigurationService) services.get(ConfigurationService.class));
        return csh;
    }
}

/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.EventAdmin;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13Server;
import com.openexchange.ajp13.AJPv13ServiceRegistry;
import com.openexchange.ajp13.servlet.ServletConfigLoader;
import com.openexchange.ajp13.servlet.http.HttpManagersInit;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.internal.CacheEventServiceImpl;
import com.openexchange.caching.internal.JCSCacheService;
import com.openexchange.caching.internal.JCSCacheServiceInit;
import com.openexchange.calendar.CalendarReminderDelete;
import com.openexchange.calendar.api.AppointmentSqlFactory;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.calendar.cache.CalendarVolatileCache;
import com.openexchange.charset.CollectionCharsetProvider;
import com.openexchange.charset.CustomCharsetProvider;
import com.openexchange.charset.CustomCharsetProviderInit;
import com.openexchange.charset.ModifyCharsetExtendedProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.config.internal.filewatcher.FileWatcher;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.internal.ContactServiceImpl;
import com.openexchange.contact.internal.ContactServiceLookup;
import com.openexchange.contact.storage.internal.DefaultContactStorageRegistry;
import com.openexchange.contact.storage.rdb.internal.RdbContactStorage;
import com.openexchange.contact.storage.registry.ContactStorageRegistry;
import com.openexchange.contactcollector.osgi.CCServiceRegistry;
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
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.CreatedBy;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.database.DatabaseService;
import com.openexchange.databaseold.Database;
import com.openexchange.event.impl.AppointmentEventInterface;
import com.openexchange.event.impl.EventDispatcher;
import com.openexchange.event.impl.EventQueue;
import com.openexchange.event.impl.TaskEventInterface;
import com.openexchange.exception.OXException;
import com.openexchange.folder.FolderService;
import com.openexchange.folder.internal.FolderInitialization;
import com.openexchange.folder.internal.FolderServiceImpl;
import com.openexchange.folderstorage.mail.MailServiceRegistry;
import com.openexchange.group.GroupService;
import com.openexchange.group.internal.GroupInit;
import com.openexchange.group.internal.GroupServiceImpl;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.configuration.ParticipantConfig;
import com.openexchange.groupware.contact.datahandler.ContactInsertDataHandler;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.groupware.impl.id.IDGeneratorServiceImpl;
import com.openexchange.groupware.reminder.internal.TargetRegistry;
import com.openexchange.groupware.update.internal.InternalList;
import com.openexchange.html.HtmlService;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.osgi.HTMLServiceActivator;
import com.openexchange.i18n.impl.I18nImpl;
import com.openexchange.i18n.impl.POTranslationsDiscoverer;
import com.openexchange.i18n.impl.ResourceBundleDiscoverer;
import com.openexchange.i18n.impl.TranslationsI18N;
import com.openexchange.i18n.parsing.Translations;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.imap.IMAPProvider;
import com.openexchange.imap.IMAPStoreCache;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.conversion.VCardMailPartDataSource;
import com.openexchange.mail.transport.config.TransportPropertiesInit;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.internal.MailAccountStorageInit;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.push.udp.registry.PushServiceRegistry;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaService;
import com.openexchange.quota.Resource;
import com.openexchange.quota.ResourceDescription;
import com.openexchange.quota.UnlimitedQuota;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.internal.ResourceServiceImpl;
import com.openexchange.server.Initialization;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.I18nServices;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.SessiondInit;
import com.openexchange.sessiond.impl.SessiondServiceImpl;
import com.openexchange.sessiond.services.SessiondServiceRegistry;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.spamhandler.defaultspamhandler.DefaultSpamHandler;
import com.openexchange.spamhandler.spamassassin.SpamAssassinSpamHandler;
import com.openexchange.subscribe.SimSubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.internal.ContactFolderMultipleUpdaterStrategy;
import com.openexchange.subscribe.internal.ContactFolderUpdaterStrategy;
import com.openexchange.subscribe.internal.StrategyFolderUpdaterService;
import com.openexchange.subscribe.internal.SubscriptionExecutionServiceImpl;
import com.openexchange.subscribe.osgi.SubscriptionServiceRegistry;
import com.openexchange.test.TestInit;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.internal.ThreadPoolProperties;
import com.openexchange.threadpool.internal.ThreadPoolServiceImpl;
import com.openexchange.threadpool.osgi.ThreadPoolActivator;
import com.openexchange.timer.TimerService;
import com.openexchange.timer.internal.CustomThreadPoolExecutorTimerService;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.external.FileStorageFactory;
import com.openexchange.tools.file.internal.CompositeFileStorageFactory;
import com.openexchange.tools.file.internal.DBQuotaFileStorageFactory;
import com.openexchange.user.UserService;
import com.openexchange.user.internal.UserServiceImpl;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.internal.UserConfigurationServiceImpl;
import com.openexchange.version.Version;
import com.openexchange.version.internal.Numbers;
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

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Init.class));

    // private static Properties infostoreProps = null;

    private static final List<Initialization> started = new ArrayList<Initialization>();

    private static final AtomicBoolean running = new AtomicBoolean();

    private static final Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

    private static final Initialization[] inits = new Initialization[] {
        /**
         * Reads system.properties.
         */
        com.openexchange.configuration.SystemConfig.getInstance(),
        /**
         * Reads the calendar.properties.
         */
        com.openexchange.groupware.calendar.CalendarConfig.getInstance(),
        /**
         * Initialization for alias charset provider
         */
        new com.openexchange.charset.CustomCharsetProviderInit(),
        /**
         * Starts HTTP servlet manager
         */
        new Initialization() {

            @Override
            public void start() throws OXException {
                AJPv13Config.getInstance().start();
                ServletConfigLoader.initDefaultInstance(AJPv13Config.getServletConfigs());
                if (null == AJPv13Server.getInstance()) {
                    AJPv13Server.setInstance(new com.openexchange.ajp13.AJPv13ServerImpl());
                }
                try {
                    AJPv13Server.startAJPServer();
                    HttpManagersInit.getInstance().start();
                } catch (OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            @Override
            public void stop() throws OXException {
                HttpManagersInit.getInstance().stop();
                AJPv13Server.stopAJPServer();
                AJPv13Server.releaseInstrance();
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
        new FolderInitialization(), com.openexchange.tools.oxfolder.OXFolderProperties.getInstance(),
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
        /**
         * Init transport properties
         */
        TransportPropertiesInit.getInstance(),

        SessiondInit.getInstance(),

        new GroupInit() };

	private static boolean databaseUpdateinitialized = false;

    public static void injectProperty() {
        final String propDir1 = TestInit.getTestProperty("openexchange.propdir");
        if (null != propDir1) {
            System.setProperty("openexchange.propdir", propDir1);
        }
    }

    public static void startServer() throws Exception {
        if (!running.compareAndSet(false, true)) {
            /*
             * Already running
             */
            return;
        }
        /*
         * Start-up
         */
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
        startVersionBundle();
        startAndInjectIDGeneratorService();
        startAndInjectConfigBundle();
        startAndInjectThreadPoolBundle();
        startAndInjectBasicServices();
        startAndInjectHTMLService();
        startAndInjectServerConfiguration();
        startAndInjectNotification();
        startAndInjectQuotaService();
        startAndInjectCache();
        startAndInjectCalendarServices();
        startAndInjectDatabaseBundle();
        startAndInjectFileStorage();
        startAndInjectDatabaseUpdate();
        startAndInjectI18NBundle();
        startAndInjectMonitoringBundle();
        startAndInjectSessiondBundle();
        startAndInjectEventBundle();
        startAndInjectContextService();
        startAndInjectUserService();
        startAndInjectGroupService();
        startAndInjectFolderService();
        startAndInjectResourceService();
        startAndInjectMailAccountStorageService();
        startAndInjectMailBundle();
        startAndInjectSpamHandler();
        startAndInjectICalServices();
        startAndInjectConverterService();
        startAndInjectXMLServices();
        startAndInjectSubscribeServices();
        startAndInjectContactStorageServices();
        startAndInjectContactServices();        
        startAndInjectContactCollector();
        startAndInjectImportExportServices();

    }

    private static void startVersionBundle() throws Exception {
        // Using some static version because access to c.o.version bundle manifest is not possible currently.
        Version.getInstance().setNumbers(new Numbers("0.0.0", "0"));
    }

    public static void startAndInjectConfigBundle() {
        /*
         * This one is properly dropped in stopServer() method
         */
        final ConfigurationService config = new ConfigurationImpl();
        services.put(ConfigurationService.class, config);
        TestServiceRegistry.getInstance().addService(ConfigurationService.class, config);
        AJPv13ServiceRegistry.SERVICE_REGISTRY.set(new ServiceRegistry());
        AJPv13ServiceRegistry.getInstance().addService(ConfigurationService.class, config);
        
    }

    private static void startAndInjectThreadPoolBundle() {
        if (null == TestServiceRegistry.getInstance().getService(ThreadPoolService.class)) {
            final ConfigurationService config = (ConfigurationService) services.get(ConfigurationService.class);
            final ThreadPoolProperties props = new ThreadPoolProperties().init(config);
            final ThreadPoolServiceImpl threadPool =
                ThreadPoolServiceImpl.newInstance(
                    props.getCorePoolSize(),
                    props.getMaximumPoolSize(),
                    props.getKeepAliveTime(),
                    props.getWorkQueue(),
                    props.getWorkQueueSize(),
                    props.isBlocking(),
                    props.getRefusedExecutionBehavior());
            services.put(ThreadPoolService.class, threadPool);
            TestServiceRegistry.getInstance().addService(ThreadPoolService.class, threadPool);
            ThreadPoolActivator.REF_THREAD_POOL.set(threadPool);
            final TimerService timer = new CustomThreadPoolExecutorTimerService(threadPool.getThreadPoolExecutor());
            services.put(TimerService.class, timer);
            TestServiceRegistry.getInstance().addService(TimerService.class, timer);
        }
    }

    private static void startAndInjectBasicServices() throws OXException {
        /*
         * Check for one service which is initialized below
         */
        if (null == TestServiceRegistry.getInstance().getService(UserConfigurationService.class)) {
            try {
                // Add charset providers
                new CustomCharsetProviderInit().start();
                final CharsetProvider[] results = ModifyCharsetExtendedProvider.modifyCharsetExtendedProvider();
                final CollectionCharsetProvider collectionCharsetProvider = (CollectionCharsetProvider) results[1];
                collectionCharsetProvider.addCharsetProvider(new net.freeutils.charset.CharsetProvider());
                collectionCharsetProvider.addCharsetProvider(new CustomCharsetProvider());
            } catch (final NoSuchFieldException e) {
                throw getWrappingOXException(e);
            } catch (final IllegalAccessException e) {
                throw getWrappingOXException(e);
            }
            services.put(UserConfigurationService.class, new UserConfigurationServiceImpl());
            TestServiceRegistry.getInstance().addService(UserConfigurationService.class, services.get(UserConfigurationService.class));
        }
    }

    private static void startAndInjectHTMLService() {
        if (null == TestServiceRegistry.getInstance().getService(HtmlService.class)) {
            final ConfigurationService configService = (ConfigurationService) services.get(ConfigurationService.class);
            com.openexchange.html.services.ServiceRegistry.getInstance().addService(ConfigurationService.class, configService);
            final Object[] maps = HTMLServiceActivator.getHTMLEntityMaps(configService.getFileByName("HTMLEntities.properties"));
            @SuppressWarnings("unchecked")
            final Map<String, Character> htmlEntityMap = (Map<String, Character>) maps[1];
            htmlEntityMap.put("apos", Character.valueOf('\''));
            @SuppressWarnings("unchecked")
            final Map<Character, String> htmlCharMap = (Map<Character, String>) maps[0];
            htmlCharMap.put(Character.valueOf('\''), "apos");
            final HtmlService service = new HtmlServiceImpl(htmlCharMap, htmlEntityMap);
            services.put(HtmlService.class, service);
            TestServiceRegistry.getInstance().addService(HtmlService.class, service);
        }
    }

    private static final OXException getWrappingOXException(final Exception cause) {
        final String message = cause.getMessage();
        final Component c = new Component() {
            private static final long serialVersionUID = 2411378382745647554L;
            @Override
            public String getAbbreviation() {
                return "TEST";
            }
        };
        return new OXException(
        		9999,
        		null == message ? "[Not available]" : message,
        		cause);
    }

    private static void startAndInjectCalendarServices() {
        if (null == TestServiceRegistry.getInstance().getService(CalendarCollectionService.class)) {
            TestServiceRegistry.getInstance().addService(CalendarCollectionService.class, new CalendarCollection());
            TestServiceRegistry.getInstance().addService(AppointmentSqlFactoryService.class, new AppointmentSqlFactory());
            TargetRegistry.getInstance().addService(Types.APPOINTMENT, new CalendarReminderDelete());

            if (null == CalendarVolatileCache.getInstance()) {
                try {
                    /*
                     * Important cache configuration constants
                     */
                    final String regionName = CalendarVolatileCache.REGION;
                    final int maxObjects = 10000000;
                    final int maxLifeSeconds = 300;
                    final int idleTimeSeconds = 180;
                    final int shrinkerIntervalSeconds = 60;
                    /*
                     * Compose cache configuration
                     */
                    final byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
                            "jcs.region."+regionName+".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
                            "jcs.region."+regionName+".cacheattributes.MaxObjects="+maxObjects+"\n" +
                            "jcs.region."+regionName+".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
                            "jcs.region."+regionName+".cacheattributes.UseMemoryShrinker=true\n" +
                            "jcs.region."+regionName+".cacheattributes.MaxMemoryIdleTimeSeconds="+idleTimeSeconds+"\n" +
                            "jcs.region."+regionName+".cacheattributes.ShrinkerIntervalSeconds="+shrinkerIntervalSeconds+"\n" +
                            "jcs.region."+regionName+".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
                            "jcs.region."+regionName+".elementattributes.IsEternal=false\n" +
                            "jcs.region."+regionName+".elementattributes.MaxLifeSeconds="+maxLifeSeconds+"\n" +
                            "jcs.region."+regionName+".elementattributes.IdleTime="+idleTimeSeconds+"\n" +
                            "jcs.region."+regionName+".elementattributes.IsSpool=false\n" +
                            "jcs.region."+regionName+".elementattributes.IsRemote=false\n" +
                            "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes();
                    final CacheService cacheService = TestServiceRegistry.getInstance().getService(CacheService.class);
                    cacheService.loadConfiguration(new ByteArrayInputStream(ccf));
                    CalendarVolatileCache.initInstance(cacheService.getCache(regionName));
                } catch (final OXException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        }
    }

    private static void startAndInjectXMLServices() {
        if (null == TestServiceRegistry.getInstance().getService(SpringParser.class)) {
            final SpringParser springParser = new DefaultSpringParser();
            TestServiceRegistry.getInstance().addService(SpringParser.class, springParser);
        }

        if (null == TestServiceRegistry.getInstance().getService(JDOMParser.class)) {
            final JDOMParser jdomParser = new JDOMParserImpl();
            TestServiceRegistry.getInstance().addService(JDOMParser.class, jdomParser);
        }
    }

    private static void startAndInjectImportExportServices() throws OXException {
        if (null == com.openexchange.importexport.osgi.ImportExportServices.LOOKUP.get()) {
            com.openexchange.importexport.osgi.ImportExportServices.LOOKUP.set(new ServiceLookup() {
                @Override
                public <S> S getService(final Class<? extends S> clazz) {
                    return TestServiceRegistry.getInstance().getService(clazz);
                }
                @Override
                public <S> S getOptionalService(final Class<? extends S> clazz) {
                    return null;
                }
            });
            SubscriptionServiceRegistry.getInstance().addService(
                ContactService.class, services.get(ContactService.class));
        }
    }
    
    private static void startAndInjectIDGeneratorService() {
        final IDGeneratorService idService = new IDGeneratorServiceImpl();
        TestServiceRegistry.getInstance().addService(IDGeneratorService.class, idService);
    }

    private static void startAndInjectSubscribeServices() {
        final List<FolderUpdaterService<?>> folderUpdaters = new ArrayList<FolderUpdaterService<?>>(2);
        folderUpdaters.add(new StrategyFolderUpdaterService<Contact>(new ContactFolderUpdaterStrategy()));
        folderUpdaters.add(new StrategyFolderUpdaterService<Contact>(new ContactFolderMultipleUpdaterStrategy(), true));
        final ContextService contextService = (ContextService) services.get(ContextService.class);
        final FolderUpdaterRegistry registry = new SubscriptionExecutionServiceImpl(new SimSubscriptionSourceDiscoveryService(), folderUpdaters, contextService);
        TestServiceRegistry.getInstance().addService(FolderUpdaterRegistry.class, registry);
    }

    private static void startAndInjectContactStorageServices() {
        if (null == TestServiceRegistry.getInstance().getService(ContactStorageRegistry.class)) {
            final DefaultContactStorageRegistry registry = new DefaultContactStorageRegistry();
            registry.addStorage(new RdbContactStorage());
            TestServiceRegistry.getInstance().addService(ContactStorageRegistry.class, registry);
            com.openexchange.contact.storage.rdb.internal.RdbServiceLookup.set(new ServiceLookup() {
                @Override
                public <S> S getService(final Class<? extends S> clazz) {
                    return TestServiceRegistry.getInstance().getService(clazz);
                }
                @Override
                public <S> S getOptionalService(final Class<? extends S> clazz) {
                    return null;
                }
            });                
        }
    }

    private static void startAndInjectContactServices() {
        if (null == TestServiceRegistry.getInstance().getService(ContactService.class)) {
            final ContactService contactService = new ContactServiceImpl();
            ContactServiceLookup.set(new ServiceLookup() {
                @Override
                public <S> S getService(final Class<? extends S> clazz) {
                    return TestServiceRegistry.getInstance().getService(clazz);
                }
                @Override
                public <S> S getOptionalService(final Class<? extends S> clazz) {
                    return null;
                }
            });
            TestServiceRegistry.getInstance().addService(ContactService.class, contactService);
            services.put(ContactService.class, contactService);
        }
    }

    public static void startAndInjectI18NBundle() throws FileNotFoundException {
        /*
         * This one properly dropped by stopServer() method
         */
        final ConfigurationService config = (ConfigurationService) services.get(ConfigurationService.class);
        final String directory_name = config.getProperty("i18n.language.path");
        if (directory_name == null) {
        	LOG.error("Tried to load i18n files and did not find a property");
        	return;
        }
        final File dir = new File(directory_name);
        final I18nServices i18nServices = I18nServices.getInstance();
        try {
            for (final ResourceBundle rc : new ResourceBundleDiscoverer(dir).getResourceBundles()) {
                i18nServices.addService(new I18nImpl(rc));
            }
            for (final Translations tr : new POTranslationsDiscoverer(dir).getTranslations()) {
                i18nServices.addService(new TranslationsI18N(tr));
            }
        } catch (final NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void startAndInjectServerConfiguration() {
        final ConfigurationService config = (ConfigurationService) services.get(ConfigurationService.class);
        /*
         * May be invoked multiple times
         */
        ServerConfig.getInstance().initialize(config);
    }

    public static void startAndInjectNotification() {
        final ConfigurationService config = (ConfigurationService) services.get(ConfigurationService.class);
        /*
         * May be invoked multiple times
         */
        ParticipantConfig.getInstance().initialize(config);
    }

    public static void startAndInjectDatabaseBundle() throws OXException, OXException {
        if (null == services.get(DatabaseService.class)) {
            final ConfigurationService configurationService = (ConfigurationService) services.get(ConfigurationService.class);
            final TimerService timerService = (TimerService) services.get(TimerService.class);
            final CacheService cacheService = (CacheService) services.get(CacheService.class);
            com.openexchange.database.internal.Initialization.getInstance().getTimer().setTimerService(timerService);
            final DatabaseService dbService = com.openexchange.database.internal.Initialization.getInstance().start(configurationService);
            services.put(DatabaseService.class, dbService);
            com.openexchange.database.internal.Initialization.getInstance().setCacheService(cacheService);
            Database.setDatabaseService(dbService);
            TestServiceRegistry.getInstance().addService(DatabaseService.class, dbService);
        }
    }

    public static void startAndInjectFileStorage() {
        /*
         * May be invoked multiple times
         */
        final FileStorageFactory fileStorageStarter = new CompositeFileStorageFactory();
        FileStorage.setFileStorageStarter(fileStorageStarter);
        final DatabaseService dbService = (DatabaseService) services.get(DatabaseService.class);
        QuotaFileStorage.setQuotaFileStorageStarter(new DBQuotaFileStorageFactory(dbService, fileStorageStarter));
    }

    public static void startAndInjectDatabaseUpdate() throws OXException {
    	if(databaseUpdateinitialized ) {
            return;
        }
    	InternalList.getInstance().start();
    	databaseUpdateinitialized = true;
    }

    private static void startAndInjectMonitoringBundle() {
        // First lookup services monitoring depends on and inject them
    }

    private static void startAndInjectMailBundle() throws Exception {
        // MailInitialization.getInstance().setConfigurationServiceHolder(getConfigurationServiceHolder());
        /*
         * Init config. Works only once.
         */
        MailProperties.getInstance().loadProperties();

        final com.openexchange.osgi.ServiceRegistry imapServiceRegistry = IMAPServiceRegistry.getServiceRegistry();
        if (null == MailProviderRegistry.getMailProvider("imap_imaps")) {
            imapServiceRegistry.addService(ConfigurationService.class, services.get(ConfigurationService.class));
            imapServiceRegistry.addService(CacheService.class, services.get(CacheService.class));
            imapServiceRegistry.addService(UserService.class, services.get(UserService.class));
            imapServiceRegistry.addService(MailAccountStorageService.class, services.get(MailAccountStorageService.class));
            imapServiceRegistry.addService(UnifiedInboxManagement.class, services.get(UnifiedInboxManagement.class));
            imapServiceRegistry.addService(ThreadPoolService.class, services.get(ThreadPoolService.class));
            imapServiceRegistry.addService(TimerService.class, services.get(TimerService.class));
            imapServiceRegistry.addService(DatabaseService.class, services.get(DatabaseService.class));
            IMAPStoreCache.initInstance();
            /*
             * Register IMAP bundle
             */
            MailProviderRegistry.registerMailProvider("imap_imaps", IMAPProvider.getInstance());
        }
    }

    private static void startAndInjectContactCollector() {
        CCServiceRegistry.SERVICE_REGISTRY.set(new ServiceRegistry());
        final ServiceRegistry reg = CCServiceRegistry.getInstance();
        if (null == reg.getService(TimerService.class)) {
            reg.addService(TimerService.class, services.get(TimerService.class));
            reg.addService(ThreadPoolService.class, services.get(ThreadPoolService.class));
            reg.addService(ContextService.class, services.get(ContextService.class));
            reg.addService(UserConfigurationService.class, services.get(UserConfigurationService.class));
            reg.addService(UserService.class, services.get(UserService.class));
//            reg.addService(ContactInterfaceDiscoveryService.class, services.get(ContactInterfaceDiscoveryService.class));
            reg.addService(ContactService.class, services.get(ContactService.class));
        }
    }

    private static void startAndInjectMailAccountStorageService() throws Exception {
        if (null == TestServiceRegistry.getInstance().getService(MailAccountStorageService.class)) {
            // Initialize mail account storage
            new MailAccountStorageInit().start();
            final MailAccountStorageService storageService = MailAccountStorageInit.newMailAccountStorageService();
            services.put(MailAccountStorageService.class, storageService);
            TestServiceRegistry.getInstance().addService(MailAccountStorageService.class, storageService);
            MailServiceRegistry.getServiceRegistry().addService(MailAccountStorageService.class, storageService);

            final UnifiedInboxManagement unifiedINBOXManagement = MailAccountStorageInit.newUnifiedINBOXManagement();
            services.put(UnifiedInboxManagement.class, unifiedINBOXManagement);
            TestServiceRegistry.getInstance().addService(UnifiedInboxManagement.class, unifiedINBOXManagement);
        }
    }

    private static void startAndInjectSpamHandler() {
        if (null == SpamHandlerRegistry.getSpamHandler(DefaultSpamHandler.getInstance().getSpamHandlerName())) {
            SpamHandlerRegistry.registerSpamHandler(DefaultSpamHandler.getInstance().getSpamHandlerName(), DefaultSpamHandler.getInstance());
        }
        if (null == SpamHandlerRegistry.getSpamHandler(SpamAssassinSpamHandler.getInstance().getSpamHandlerName())) {
            SpamHandlerRegistry.registerSpamHandler(
                SpamAssassinSpamHandler.getInstance().getSpamHandlerName(),
                SpamAssassinSpamHandler.getInstance());
        }
    }

    private static void startAndInjectResourceService() {
        if (null == TestServiceRegistry.getInstance().getService(ResourceService.class)) {
            final ResourceService resources = ResourceServiceImpl.getInstance();
            services.put(ResourceService.class, resources);
            TestServiceRegistry.getInstance().addService(ResourceService.class, resources);
        }
    }

    private static void startAndInjectUserService() {
        if (null == TestServiceRegistry.getInstance().getService(UserService.class)) {
            final UserService us = new UserServiceImpl();
            services.put(UserService.class, us);
            TestServiceRegistry.getInstance().addService(UserService.class, us);
        }
    }

    private static void startAndInjectGroupService() {
        if (null == TestServiceRegistry.getInstance().getService(GroupService.class)) {
            final GroupService us = new GroupServiceImpl();
            services.put(GroupService.class, us);
            TestServiceRegistry.getInstance().addService(GroupService.class, us);
        }
    }

    private static void startAndInjectFolderService() {
        if (null == TestServiceRegistry.getInstance().getService(FolderService.class)) {
            final FolderService fs = new FolderServiceImpl();
            services.put(FolderService.class, fs);
            TestServiceRegistry.getInstance().addService(FolderService.class, fs);
        }
    }

    private static void startAndInjectContextService() {
        if (null == TestServiceRegistry.getInstance().getService(ContextService.class)) {
            final ContextService cs = new ContextServiceImpl();
            services.put(ContextService.class, cs);
            TestServiceRegistry.getInstance().addService(ContextService.class, cs);
        }
    }

    private static void startAndInjectSessiondBundle() {
        if (null == TestServiceRegistry.getInstance().getService(SessiondService.class)) {
            SessiondServiceRegistry.getServiceRegistry().addService(ConfigurationService.class, services.get(ConfigurationService.class));
            SessiondServiceRegistry.getServiceRegistry().addService(TimerService.class, services.get(TimerService.class));
            final SessiondServiceImpl serviceImpl = new SessiondServiceImpl();
            SessiondService.SERVICE_REFERENCE.set(serviceImpl);
            TestServiceRegistry.getInstance().addService(SessiondService.class, serviceImpl);
        }
    }

    private static void startAndInjectEventBundle() throws Exception {
        if (null == TestServiceRegistry.getInstance().getService(EventAdmin.class)) {
            EventQueue.setNewEventDispatcher(new EventDispatcher() {

                @Override
                public void addListener(final AppointmentEventInterface listener) {
                    // Do nothing.
                }

                @Override
                public void addListener(final TaskEventInterface listener) {
                    // Do nothing.
                }
            });
            TestServiceRegistry.getInstance().addService(EventAdmin.class, TestEventAdmin.getInstance());
            PushServiceRegistry.getServiceRegistry().addService(EventAdmin.class, TestEventAdmin.getInstance());
        }
    }

    public static void startAndInjectQuotaService() {
        if (null == TestServiceRegistry.getInstance().getService(QuotaService.class)) {
            QuotaService quotaService = new QuotaService() {
                
                @Override
                public Quota getQuotaFor(Resource resource, ResourceDescription desc, Session session) throws OXException {
                    return UnlimitedQuota.getInstance();
                }
                
                @Override
                public Quota getQuotaFor(Resource resource, Session session) throws OXException {
                    return UnlimitedQuota.getInstance();
                }
            };
            services.put(QuotaService.class, quotaService);
            TestServiceRegistry.getInstance().addService(QuotaService.class, quotaService);
            ServerServiceRegistry.getInstance().addService(QuotaService.class, quotaService);
        }
    }

    public static void startAndInjectCache() throws OXException {
        if (null == TestServiceRegistry.getInstance().getService(CacheService.class)) {
            CacheEventService cacheEventService = new CacheEventServiceImpl();
            services.put(CacheEventService.class, cacheEventService);
            TestServiceRegistry.getInstance().addService(CacheEventService.class, cacheEventService);
            JCSCacheServiceInit.initInstance();
            JCSCacheServiceInit.getInstance().setCacheEventService((CacheEventService)services.get(CacheEventService.class));
            JCSCacheServiceInit.getInstance().start((ConfigurationService) services.get(ConfigurationService.class));
            final CacheService cache = JCSCacheService.getInstance();
            services.put(CacheService.class, cache);
            TestServiceRegistry.getInstance().addService(CacheService.class, cache);
        }
    }

    public static void startAndInjectICalServices() {
        if (null == TestServiceRegistry.getInstance().getService(ICalParser.class)) {
            final ICal4JParser parser = new ICal4JParser();
            final ICal4JEmitter emitter = new ICal4JEmitter();

            final OXUserResolver userResolver = new OXUserResolver();
            userResolver.setUserService((UserService) services.get(UserService.class));
            Participants.userResolver = userResolver;
            CreatedBy.userResolver = userResolver;

            final OXResourceResolver resourceResolver = new OXResourceResolver();
            resourceResolver.setResourceService((ResourceService) services.get(ResourceService.class));
            Participants.resourceResolver = resourceResolver;

            services.put(ICalParser.class, parser);
            services.put(ICalEmitter.class, emitter);

            TestServiceRegistry.getInstance().addService(ICalParser.class, parser);
            TestServiceRegistry.getInstance().addService(ICalEmitter.class, emitter);
        }
    }

    public static void startAndInjectConverterService() {
        if (null == TestServiceRegistry.getInstance().getService(ConversionService.class)) {
            ConversionEngineRegistry.getInstance().putDataHandler("com.openexchange.contact", new ContactInsertDataHandler());
            ConversionEngineRegistry.getInstance().putDataSource("com.openexchange.mail.vcard", new VCardMailPartDataSource());

            final ConversionService conversionService = new ConversionServiceImpl();
            services.put(ConversionService.class, conversionService);
            TestServiceRegistry.getInstance().addService(ConversionService.class, conversionService);
        }
    }

    public static void stopServer() throws Exception {
        // This causes NPEs everywhere in the tests.
        // for (final Initialization init: started) {
        // init.stop();
        // }
//        stopMailBundle();
//        stopDatabaseBundle();
//        stopThreadPoolBundle();
        if (!running.compareAndSet(true, false)) {
            /*
             * Already stopped
             */
            return;
        }
        /*
         * Shut-down
         */
        dropI18NBundle();
        dropConfigBundle();
        dropProperty();
    }

    public static void dropProperty() {
        final Properties sysProps = System.getProperties();
        sysProps.remove("openexchange.propdir");
    }

    public static void dropConfigBundle() {
        services.remove(ConfigurationService.class);
        TestServiceRegistry.getInstance().removeService(ConfigurationService.class);
        FileWatcher.dropTimer();
    }

    public static void dropI18NBundle() {
        final I18nServices i18nServices = I18nServices.getInstance();
        i18nServices.clear();
    }

    public static void stopMailBundle() {
        IMAPServiceRegistry.getServiceRegistry().removeService(TimerService.class);
        IMAPStoreCache.shutDownInstance();
    }

    public static void stopDatabaseBundle() {
        Database.setDatabaseService(null);
        com.openexchange.database.internal.Initialization.getInstance().stop();
        databaseUpdateinitialized = false;
    }

    public static void stopThreadPoolBundle() throws Exception {
        services.remove(TimerService.class);
        final ThreadPoolServiceImpl threadPool = (ThreadPoolServiceImpl) services.remove(ThreadPoolService.class);
        threadPool.shutdownNow();
        threadPool.awaitTermination(10000);
    }

    public static ConfigurationServiceHolder getConfigurationServiceHolder() throws Exception {
        final ConfigurationServiceHolder csh = ConfigurationServiceHolder.newInstance();
        csh.setService((ConfigurationService) services.get(ConfigurationService.class));
        return csh;
    }
}

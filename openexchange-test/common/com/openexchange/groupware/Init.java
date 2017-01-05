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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.service.event.EventAdmin;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEventConfiguration;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.internal.CacheEventServiceImpl;
import com.openexchange.caching.internal.JCSCacheService;
import com.openexchange.caching.internal.JCSCacheServiceInit;
import com.openexchange.calendar.CalendarAdministration;
import com.openexchange.calendar.CalendarReminderDelete;
import com.openexchange.calendar.api.AppointmentSqlFactory;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.calendar.cache.CalendarVolatileCache;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.internal.AbstractCapabilityService;
import com.openexchange.charset.CollectionCharsetProvider;
import com.openexchange.charset.CustomCharsetProvider;
import com.openexchange.charset.CustomCharsetProviderInit;
import com.openexchange.charset.ModifyCharsetExtendedProvider;
import com.openexchange.cluster.timer.ClusterTimerService;
import com.openexchange.cluster.timer.internal.ClusterTimerServiceImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ReinitializableConfigProviderService;
import com.openexchange.config.cascade.impl.ConfigCascade;
import com.openexchange.config.cascade.impl.InMemoryConfigProvider;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.config.internal.filewatcher.FileWatcher;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.internal.ContactServiceImpl;
import com.openexchange.contact.internal.ContactServiceLookup;
import com.openexchange.contact.storage.internal.DefaultContactStorageRegistry;
import com.openexchange.contact.storage.rdb.internal.RdbContactStorage;
import com.openexchange.contact.storage.registry.ContactStorageRegistry;
import com.openexchange.contacts.json.converters.ContactInsertDataHandler;
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
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.filestore.impl.CompositeFileStorageService;
import com.openexchange.filestore.impl.DBQuotaFileStorageService;
import com.openexchange.folder.FolderService;
import com.openexchange.folder.internal.FolderInitialization;
import com.openexchange.folder.internal.FolderServiceImpl;
import com.openexchange.group.GroupService;
import com.openexchange.group.internal.GroupInit;
import com.openexchange.group.internal.GroupServiceImpl;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.alias.impl.RdbAliasStorage;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarAdministrationService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.configuration.ParticipantConfig;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.groupware.impl.id.IDGeneratorServiceImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
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
import com.openexchange.imap.services.Services;
import com.openexchange.imap.storecache.IMAPStoreCache;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.autoconfig.AutoconfigService;
import com.openexchange.mail.autoconfig.internal.AutoconfigServiceImpl;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.conversion.VCardMailPartDataSource;
import com.openexchange.mail.transport.config.TransportPropertiesInit;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.internal.MailAccountStorageInit;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.TrustedSSLSocketFactory;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.config.impl.internal.SSLConfigurationServiceImpl;
import com.openexchange.net.ssl.config.impl.internal.SSLProperties;
import com.openexchange.net.ssl.internal.DefaultSSLSocketFactoryProvider;
import com.openexchange.osgi.util.ServiceCallWrapperModifier;
import com.openexchange.passwordmechs.PasswordMechFactoryImpl;
import com.openexchange.push.udp.registry.PushServiceRegistry;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.internal.ResourceServiceImpl;
import com.openexchange.server.Initialization;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.server.services.I18nServices;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.SessiondInit;
import com.openexchange.sessiond.impl.SessiondServiceImpl;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.sessionstorage.TestSessionStorageService;
import com.openexchange.share.ShareService;
import com.openexchange.share.impl.DefaultShareService;
import com.openexchange.share.impl.cleanup.GuestCleaner;
import com.openexchange.sms.PhoneNumberParserService;
import com.openexchange.sms.impl.PhoneNumberParserServiceImpl;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.spamhandler.defaultspamhandler.DefaultSpamHandler;
import com.openexchange.subscribe.SimSubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.internal.ContactFolderMultipleUpdaterStrategy;
import com.openexchange.subscribe.internal.ContactFolderUpdaterStrategy;
import com.openexchange.subscribe.internal.StrategyFolderUpdaterService;
import com.openexchange.subscribe.internal.SubscriptionExecutionServiceImpl;
import com.openexchange.subscribe.osgi.SubscriptionServiceRegistry;
import com.openexchange.test.TestInit;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.internal.DelegateExecutorService;
import com.openexchange.threadpool.internal.ThreadPoolProperties;
import com.openexchange.threadpool.internal.ThreadPoolServiceImpl;
import com.openexchange.threadpool.osgi.ThreadPoolActivator;
import com.openexchange.timer.TimerService;
import com.openexchange.timer.internal.CustomThreadPoolExecutorTimerService;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.strings.BasicTypesStringParser;
import com.openexchange.user.UserService;
import com.openexchange.user.UserServiceInterceptor;
import com.openexchange.user.UserServiceInterceptorRegistry;
import com.openexchange.user.internal.UserServiceImpl;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;
import com.openexchange.userconf.internal.UserConfigurationServiceImpl;
import com.openexchange.userconf.internal.UserPermissionServiceImpl;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Init.class);

    // private static Properties infostoreProps = null;

    private static final List<Initialization> started = new ArrayList<Initialization>();

    private static final AtomicBoolean running = new AtomicBoolean();

    private static final Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

    public static final ServiceLookup LOOKUP = new ServiceLookup() {

        @Override
        public <S> S getService(Class<? extends S> clazz) {
            return (S) services.get(clazz);
        }

        @Override
        public <S> S getOptionalService(Class<? extends S> clazz) {
            return (S) services.get(clazz);
        }
    };

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
         * Setup of ContextStorage and LoginInfo.
         */
        com.openexchange.groupware.contexts.impl.ContextInit.getInstance(),
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
        long start = System.currentTimeMillis();
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
        long startTestServices = System.currentTimeMillis();
        injectTestServices();
        System.out.println("Injecting the test services took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        for (final Initialization init : inits) {
            long startInit = System.currentTimeMillis();
            init.start();
            System.out.println("Starting init for " + init.toString() + " took " + (System.currentTimeMillis() - startInit) + "ms.");
            started.add(init);
        }

        ServiceCallWrapperModifier.initTestRun(services);
        System.out.println("Initializing the test setup took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private static void injectTestServices() throws Exception {
        // Since unit tests are running outside the OSGi container
        // we'll have to do the service wiring differently.
        // This method duplicates statically what the OSGi container
        // handles dynamically
        long startTestServices = System.currentTimeMillis();
        startVersionBundle();
        System.out.println("startVersionBundle took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectIDGeneratorService();
        System.out.println("startAndInjectIDGeneratorService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectConfigBundle();
        System.out.println("startAndInjectConfigBundle took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startAndInjectNetSSLBundle();

        startTestServices = System.currentTimeMillis();
        startAndInjectConfigViewFactory();
        System.out.println("startAndInjectConfigViewFactory took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectThreadPoolBundle();
        System.out.println("startAndInjectThreadPoolBundle took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectBasicServices();
        System.out.println("startAndInjectBasicServices took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectHTMLService();
        System.out.println("startAndInjectHTMLService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectServerConfiguration();
        System.out.println("startAndInjectServerConfiguration took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectNotification();
        System.out.println("startAndInjectNotification took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectCache();
        System.out.println("startAndInjectCache took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectCalendarServices();
        System.out.println("startAndInjectCalendarServices took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectDatabaseBundle();
        System.out.println("startAndInjectDatabaseBundle took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectDatabaseUpdate();
        System.out.println("startAndInjectDatabaseUpdate took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        //        startAndInjectI18NBundle();
        System.out.println("startAndInjectI18NBundle took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectMonitoringBundle();
        System.out.println("startAndInjectMonitoringBundle took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectEventBundle();
        System.out.println("startAndInjectEventBundle took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectSessiondBundle();
        System.out.println("startAndInjectSessiondBundle took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectContextService();
        System.out.println("startAndInjectContextService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectFileStorage();
        System.out.println("startAndInjectFileStorage took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectGroupService();
        System.out.println("startAndInjectGroupService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectFolderService();
        System.out.println("startAndInjectFolderService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectResourceService();
        System.out.println("startAndInjectResourceService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectMailAccountStorageService();
        System.out.println("startAndInjectMailAccountStorageService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectMailAutoconfigService();
        System.out.println("startAndInjectMailAutoconfigService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectMailBundle();
        System.out.println("startAndInjectMailBundle took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectSpamHandler();
        System.out.println("startAndInjectSpamHandler took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectICalServices();
        System.out.println("startAndInjectICalServices took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectConverterService();
        System.out.println("startAndInjectConverterService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectXMLServices();
        System.out.println("startAndInjectXMLServices took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectSubscribeServices();
        System.out.println("startAndInjectSubscribeServices took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectContactStorageServices();
        System.out.println("startAndInjectContactStorageServices took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectContactServices();
        System.out.println("startAndInjectContactServices took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectContactCollector();
        System.out.println("startAndInjectContactCollector took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectImportExportServices();
        System.out.println("startAndInjectImportExportServices took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectCapabilitiesServices();
        System.out.println("startAndInjectCapabilitiesServices took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectClusterTimerService();
        System.out.println("startAndInjectClusterTimerService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectDefaultShareService();
        System.out.println("startAndInjectDefaultShareService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectPhoneNumberParserService();
        System.out.println("startAndInjectPhoneNumberParserService took " + (System.currentTimeMillis() - startTestServices) + "ms.");

        startTestServices = System.currentTimeMillis();
        startAndInjectAliasService();
        System.out.println("startAndInjectAliasService took " + (System.currentTimeMillis() - startTestServices) + "ms.");
    }

    /**
     *
     */
    private static void startAndInjectCapabilitiesServices() {
        AbstractCapabilityService c = new AbstractCapabilityService(new ServiceLookup() {

            @Override
            public <S> S getService(final Class<? extends S> clazz) {
                return TestServiceRegistry.getInstance().getService(clazz);
            }

            @Override
            public <S> S getOptionalService(final Class<? extends S> clazz) {
                return null;
            }
        }, null) {

            @Override
            protected Map<String, List<CapabilityChecker>> getCheckers() {
                return Collections.emptyMap();
            }
        };

        services.put(CapabilityService.class, c);
        TestServiceRegistry.getInstance().addService(CapabilityService.class, c);
    }

    private static void startAndInjectConfigViewFactory() {
        ConfigCascade cascade = new ConfigCascade();
        cascade.setProvider("server", new InMemoryConfigProvider());
        cascade.setProvider("context", new InMemoryConfigProvider());
        cascade.setProvider("user", new InMemoryConfigProvider());
        cascade.setSearchPath("user", "context", "server");
        cascade.setStringParser(new BasicTypesStringParser());
        services.put(ConfigViewFactory.class, cascade);
        TestServiceRegistry.getInstance().addService(ConfigViewFactory.class, cascade);
    }

    private static void startVersionBundle() throws Exception {
        // Using some static version because access to c.o.version bundle manifest is not possible currently.
        Version.getInstance().setNumbers(new Numbers("0.0.0", "0"));
    }

    public static void startAndInjectConfigBundle() {
        /*
         * This one is properly dropped in stopServer() method
         */
        final ConfigurationService config = new ConfigurationImpl(Collections.<ReinitializableConfigProviderService> emptyList());
        services.put(ConfigurationService.class, config);
        TestServiceRegistry.getInstance().addService(ConfigurationService.class, config);
        com.openexchange.http.grizzly.osgi.Services.setServiceLookup(LOOKUP);
    }

    private static void startAndInjectNetSSLBundle() {
        SimpleServiceLookup myServices = new SimpleServiceLookup();
        Object configurationService = services.get(ConfigurationService.class);
        myServices.add(ConfigurationService.class, configurationService);
        myServices.add(SSLConfigurationService.class, new SSLConfigurationServiceImpl((ConfigurationService) configurationService));
        com.openexchange.net.ssl.osgi.Services.setServiceLookup(myServices);

        services.put(SSLSocketFactoryProvider.class, DefaultSSLSocketFactoryProvider.getInstance());
        services.put(SSLConfigurationService.class, new SSLConfigurationServiceImpl((ConfigurationService) configurationService));

        TestServiceRegistry.getInstance().addService(SSLSocketFactoryProvider.class, DefaultSSLSocketFactoryProvider.getInstance());
        TestServiceRegistry.getInstance().addService(SSLConfigurationService.class, new SSLConfigurationServiceImpl((ConfigurationService) configurationService));

        TrustedSSLSocketFactory.init();
        SSLProperties.initJvmDefaultCipherSuites();
    }

    private static void startAndInjectThreadPoolBundle() {
        if (null == TestServiceRegistry.getInstance().getService(ThreadPoolService.class)) {
            final ConfigurationService config = (ConfigurationService) services.get(ConfigurationService.class);
            final ThreadPoolProperties props = new ThreadPoolProperties().init(config);
            final ThreadPoolServiceImpl threadPool = ThreadPoolServiceImpl.newInstance(props.getCorePoolSize(), props.getMaximumPoolSize(), props.getKeepAliveTime(), props.getWorkQueue(), props.getWorkQueueSize(), props.isBlocking(), props.getRefusedExecutionBehavior());
            services.put(ThreadPoolService.class, threadPool);
            TestServiceRegistry.getInstance().addService(ThreadPoolService.class, threadPool);
            ThreadPoolActivator.REF_THREAD_POOL.set(threadPool);

            DelegateExecutorService delegateExecutorService = new DelegateExecutorService(threadPool.getThreadPoolExecutor());
            services.put(ExecutorService.class, delegateExecutorService);
            TestServiceRegistry.getInstance().addService(ExecutorService.class, delegateExecutorService);

            final TimerService timer = new CustomThreadPoolExecutorTimerService(threadPool.getThreadPoolExecutor());
            services.put(TimerService.class, timer);
            TestServiceRegistry.getInstance().addService(TimerService.class, timer);
        }
    }

    private static void startAndInjectBasicServices() throws OXException {
        if (null == TestServiceRegistry.getInstance().getService(UserService.class)) {
            final UserService us = new UserServiceImpl(new UserServiceInterceptorRegistry(null) {

                @Override
                public synchronized List<UserServiceInterceptor> getInterceptors() {
                    return Collections.emptyList();
                }
            }, new PasswordMechFactoryImpl());
            services.put(UserService.class, us);
            TestServiceRegistry.getInstance().addService(UserService.class, us);
        }
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
            services.put(UserPermissionService.class, new UserPermissionServiceImpl());
            TestServiceRegistry.getInstance().addService(UserConfigurationService.class, services.get(UserConfigurationService.class));
            TestServiceRegistry.getInstance().addService(UserPermissionService.class, services.get(UserPermissionService.class));
        }
    }

    private static void startAndInjectHTMLService() {
        if (null == TestServiceRegistry.getInstance().getService(HtmlService.class)) {
            final ConfigurationService configService = (ConfigurationService) services.get(ConfigurationService.class);
            com.openexchange.html.services.ServiceRegistry.getInstance().addService(ConfigurationService.class, configService);
            final Object[] maps = HTMLServiceActivator.getHTMLEntityMaps(configService.getFileByName("HTMLEntities.properties"));
            @SuppressWarnings("unchecked") final Map<String, Character> htmlEntityMap = (Map<String, Character>) maps[1];
            htmlEntityMap.put("apos", Character.valueOf('\''));
            @SuppressWarnings("unchecked") final Map<Character, String> htmlCharMap = (Map<Character, String>) maps[0];
            htmlCharMap.put(Character.valueOf('\''), "apos");
            final HtmlService service = new HtmlServiceImpl(htmlCharMap, htmlEntityMap);
            services.put(HtmlService.class, service);
            TestServiceRegistry.getInstance().addService(HtmlService.class, service);
        }
    }

    private static final OXException getWrappingOXException(final Exception cause) {
        final String message = cause.getMessage();
        new Component() {

            private static final long serialVersionUID = 2411378382745647554L;

            @Override
            public String getAbbreviation() {
                return "TEST";
            }
        };
        return new OXException(9999, null == message ? "[Not available]" : message, cause);
    }

    private static void startAndInjectCalendarServices() {
        if (null == TestServiceRegistry.getInstance().getService(CalendarCollectionService.class)) {
            TestServiceRegistry.getInstance().addService(CalendarCollectionService.class, new CalendarCollection());
            TestServiceRegistry.getInstance().addService(AppointmentSqlFactoryService.class, new AppointmentSqlFactory());
            TargetRegistry.getInstance().addService(Types.APPOINTMENT, new CalendarReminderDelete());
            TestServiceRegistry.getInstance().addService(CalendarAdministrationService.class, new CalendarAdministration());

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
                    final byte[] ccf = ("jcs.region." + regionName + "=LTCP\n" + "jcs.region." + regionName + ".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" + "jcs.region." + regionName + ".cacheattributes.MaxObjects=" + maxObjects + "\n" + "jcs.region." + regionName + ".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" + "jcs.region." + regionName + ".cacheattributes.UseMemoryShrinker=true\n" + "jcs.region." + regionName + ".cacheattributes.MaxMemoryIdleTimeSeconds=" + idleTimeSeconds + "\n" + "jcs.region." + regionName + ".cacheattributes.ShrinkerIntervalSeconds=" + shrinkerIntervalSeconds + "\n" + "jcs.region." + regionName + ".elementattributes=org.apache.jcs.engine.ElementAttributes\n" + "jcs.region." + regionName + ".elementattributes.IsEternal=false\n" + "jcs.region." + regionName + ".elementattributes.MaxLifeSeconds=" + maxLifeSeconds + "\n" + "jcs.region." + regionName + ".elementattributes.IdleTime=" + idleTimeSeconds + "\n" + "jcs.region." + regionName + ".elementattributes.IsSpool=false\n" + "jcs.region." + regionName + ".elementattributes.IsRemote=false\n" + "jcs.region." + regionName + ".elementattributes.IsLateral=false\n").getBytes();
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
            SubscriptionServiceRegistry.getInstance().addService(ContactService.class, services.get(ContactService.class));
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
            final ContactService contactService = new ContactServiceImpl(new UserServiceInterceptorRegistry(null));
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
            ConfigViewFactory configViewFactory = (ConfigViewFactory) services.get(ConfigViewFactory.class);
            com.openexchange.database.internal.Initialization.getInstance().getTimer().setTimerService(timerService);
            final DatabaseService dbService = com.openexchange.database.internal.Initialization.getInstance().start(configurationService, configViewFactory, null);
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
        final FileStorageService fileStorageStarter = new CompositeFileStorageService(null);
        FileStorage.setFileStorageStarter(fileStorageStarter);
        final DatabaseService dbService = (DatabaseService) services.get(DatabaseService.class);
        FileStorages.setFileStorageService(fileStorageStarter);

        SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
        serviceLookup.add(DatabaseService.class, dbService);
        ContextService contextService = (ContextService) services.get(ContextService.class);
        serviceLookup.add(ContextService.class, contextService);
        UserService userService = (UserService) services.get(UserService.class);
        serviceLookup.add(UserService.class, userService);
        com.openexchange.filestore.impl.osgi.Services.setServiceLookup(serviceLookup);

        DBQuotaFileStorageService qfss = new DBQuotaFileStorageService(null, null, fileStorageStarter);
        QuotaFileStorage.setQuotaFileStorageStarter(qfss);
        InfostoreFacadeImpl.setQuotaFileStorageService(qfss);
        services.put(QuotaFileStorageService.class, qfss);
        TestServiceRegistry.getInstance().addService(QuotaFileStorageService.class, qfss);
        FileStorages.setQuotaFileStorageService(qfss);
    }

    public static void startAndInjectDatabaseUpdate() throws OXException {
        if (databaseUpdateinitialized) {
            return;
        }
        // ConfigurationService config = TestServiceRegistry.getInstance().getService(ConfigurationService.class);
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

        if (null == MailProviderRegistry.getMailProvider("imap_imaps")) {
            Services.setServiceLookup(LOOKUP);
            IMAPStoreCache.initInstance();
            /*
             * Register IMAP bundle
             */
            MailProviderRegistry.registerMailProvider("imap_imaps", IMAPProvider.getInstance());
        }
    }

    private static void startAndInjectContactCollector() {
        // Nothing to do
    }

    private static void startAndInjectMailAccountStorageService() throws Exception {
        if (null == TestServiceRegistry.getInstance().getService(MailAccountStorageService.class)) {
            // Initialize mail account storage
            new MailAccountStorageInit().start();
            final MailAccountStorageService storageService = MailAccountStorageInit.newMailAccountStorageService();
            services.put(MailAccountStorageService.class, storageService);
            TestServiceRegistry.getInstance().addService(MailAccountStorageService.class, storageService);

            {
                SimpleServiceLookup services = new SimpleServiceLookup();
                services.add(MailAccountStorageService.class, storageService);
                com.openexchange.folderstorage.mail.osgi.Services.setServiceLookup(services);
            }

            final UnifiedInboxManagement unifiedINBOXManagement = MailAccountStorageInit.newUnifiedINBOXManagement();
            services.put(UnifiedInboxManagement.class, unifiedINBOXManagement);
            TestServiceRegistry.getInstance().addService(UnifiedInboxManagement.class, unifiedINBOXManagement);
        }
    }

    private static void startAndInjectMailAutoconfigService() throws Exception {
        if (null == TestServiceRegistry.getInstance().getService(AutoconfigService.class)) {
            AutoconfigService service = new AutoconfigServiceImpl(LOOKUP);
            services.put(AutoconfigService.class, service);
            TestServiceRegistry.getInstance().addService(AutoconfigService.class, service);
        }
    }

    private static void startAndInjectSpamHandler() {
        if (null == SpamHandlerRegistry.getSpamHandler(DefaultSpamHandler.getInstance().getSpamHandlerName())) {
            SpamHandlerRegistry.registerSpamHandler(DefaultSpamHandler.getInstance().getSpamHandlerName(), DefaultSpamHandler.getInstance());
        }
    }

    private static void startAndInjectResourceService() {
        if (null == TestServiceRegistry.getInstance().getService(ResourceService.class)) {
            final ResourceService resources = ResourceServiceImpl.getInstance();
            services.put(ResourceService.class, resources);
            TestServiceRegistry.getInstance().addService(ResourceService.class, resources);
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
            SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
            serviceLookup.add(ConfigurationService.class, services.get(ConfigurationService.class));
            serviceLookup.add(TimerService.class, services.get(TimerService.class));
            serviceLookup.add(EventAdmin.class, TestServiceRegistry.getInstance().getService(EventAdmin.class));
            SessionStorageService sessionStorageService = TestSessionStorageService.getInstance();
            serviceLookup.add(SessionStorageService.class, sessionStorageService);
            com.openexchange.sessiond.osgi.Services.setServiceLookup(serviceLookup);

            TestServiceRegistry.getInstance().addService(SessionStorageService.class, sessionStorageService);
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

    public static void startAndInjectCache() throws OXException {
        if (null == TestServiceRegistry.getInstance().getService(CacheService.class)) {
            CacheEventConfiguration config = new CacheEventConfiguration() {

                @Override
                public boolean remoteInvalidationForPersonalFolders() {
                    return false;
                }
            };
            CacheEventService cacheEventService = new CacheEventServiceImpl(config);
            services.put(CacheEventService.class, cacheEventService);
            TestServiceRegistry.getInstance().addService(CacheEventService.class, cacheEventService);
            JCSCacheServiceInit.initInstance();
            JCSCacheServiceInit.getInstance().setCacheEventService((CacheEventService) services.get(CacheEventService.class));
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
            ConversionEngineRegistry.getInstance().putDataHandler("com.openexchange.contact", new ContactInsertDataHandler(TestServiceRegistry.getInstance()));
            ConversionEngineRegistry.getInstance().putDataSource("com.openexchange.mail.vcard", new VCardMailPartDataSource());

            final ConversionService conversionService = new ConversionServiceImpl();
            services.put(ConversionService.class, conversionService);
            TestServiceRegistry.getInstance().addService(ConversionService.class, conversionService);
        }
    }

    public static void startAndInjectClusterTimerService() throws OXException {
        if (null == TestServiceRegistry.getInstance().getService(ClusterTimerService.class)) {
            ClusterTimerService clusterTimerService = new ClusterTimerServiceImpl(LOOKUP, null);
            services.put(ClusterTimerService.class, clusterTimerService);
            TestServiceRegistry.getInstance().addService(ClusterTimerService.class, clusterTimerService);
        }
    }

    public static void startAndInjectAliasService() {
        if (null == TestServiceRegistry.getInstance().getService(UserAliasStorage.class)) {
            TestServiceRegistry.getInstance().addService(UserAliasStorage.class, new RdbAliasStorage());
        }
    }

    public static void startAndInjectDefaultShareService() throws OXException {
        if (null == TestServiceRegistry.getInstance().getService(ShareService.class)) {
            DefaultShareService service = new DefaultShareService(LOOKUP, new GuestCleaner(LOOKUP));
            services.put(ShareService.class, service);
            TestServiceRegistry.getInstance().addService(ShareService.class, service);
        }
    }

    public static void startAndInjectPhoneNumberParserService() throws OXException {
        if (null == TestServiceRegistry.getInstance().getService(PhoneNumberParserService.class)) {
            PhoneNumberParserService service = new PhoneNumberParserServiceImpl();
            services.put(PhoneNumberParserService.class, service);
            TestServiceRegistry.getInstance().addService(PhoneNumberParserService.class, service);
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
        Services.setServiceLookup(null);
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

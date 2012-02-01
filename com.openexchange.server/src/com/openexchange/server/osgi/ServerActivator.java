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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.server.osgi;

import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.Infostore;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.customizer.folder.osgi.FolderFieldCollector;
import com.openexchange.ajax.requesthandler.AJAXRequestHandler;
import com.openexchange.api2.ContactInterfaceFactory;
import com.openexchange.api2.RdbContactInterfaceFactory;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.caching.CacheService;
import com.openexchange.charset.CustomCharsetProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configjump.ConfigJumpService;
import com.openexchange.configjump.client.ConfigJump;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataSource;
import com.openexchange.counter.MailCounter;
import com.openexchange.counter.MailIdleCounter;
import com.openexchange.crypto.CryptoService;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.osgiservice.WhiteboardDBProvider;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.dataretention.DataRetentionService;
import com.openexchange.event.EventFactoryService;
import com.openexchange.event.impl.EventFactoryServiceImpl;
import com.openexchange.event.impl.EventQueue;
import com.openexchange.event.impl.osgi.EventHandlerRegistration;
import com.openexchange.event.impl.osgi.OSGiEventDispatcher;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.parse.FileMetadataParserService;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.filemanagement.internal.ManagedFileImageDataSource;
import com.openexchange.folder.FolderDeleteListenerService;
import com.openexchange.folder.FolderService;
import com.openexchange.folder.internal.FolderDeleteListenerServiceTrackerCustomizer;
import com.openexchange.folder.internal.FolderServiceImpl;
import com.openexchange.folderstorage.osgi.FolderStorageActivator;
import com.openexchange.group.GroupService;
import com.openexchange.group.internal.GroupServiceImpl;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarAdministrationService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.contact.ContactInterfaceProvider;
import com.openexchange.groupware.contact.datahandler.ContactInsertDataHandler;
import com.openexchange.groupware.contact.datahandler.ContactJSONDataHandler;
import com.openexchange.groupware.contact.datasource.ContactDataSource;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.contact.internal.ContactInterfaceDiscoveryServiceImpl;
import com.openexchange.groupware.datahandler.ICalInsertDataHandler;
import com.openexchange.groupware.datahandler.ICalJSONDataHandler;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.impl.id.CreateIDSequenceTable;
import com.openexchange.groupware.importexport.importers.ExtraneousSeriesMasterRecoveryParser;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.reminder.TargetService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.html.HTMLService;
import com.openexchange.i18n.I18nService;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.image.servlet.ImageServlet;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.mail.MailCounterImpl;
import com.openexchange.mail.MailIdleCounterImpl;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.cache.MailAccessCacheEventListener;
import com.openexchange.mail.cache.MailSessionEventHandler;
import com.openexchange.mail.conversion.ICalMailPartDataSource;
import com.openexchange.mail.conversion.InlineImageDataSource;
import com.openexchange.mail.conversion.VCardAttachMailDataHandler;
import com.openexchange.mail.conversion.VCardMailPartDataSource;
import com.openexchange.mail.loginhandler.MailLoginHandler;
import com.openexchange.mail.loginhandler.TransportLoginHandler;
import com.openexchange.mail.osgi.MailProviderServiceTracker;
import com.openexchange.mail.osgi.TransportProviderServiceTracker;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.service.impl.MailServiceImpl;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.mailaccount.internal.CreateMailAccountTables;
import com.openexchange.management.ManagementService;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.multiple.internal.MultipleHandlerServiceTracker;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.preview.PreviewService;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.report.internal.LastLoginRecorder;
import com.openexchange.resource.ResourceService;
import com.openexchange.search.SearchService;
import com.openexchange.search.internal.SearchServiceImpl;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.server.impl.Starter;
import com.openexchange.server.impl.Version;
import com.openexchange.server.osgiservice.BundleServiceTracker;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.WhiteboardFactoryService;
import com.openexchange.server.services.ServerRequestHandlerRegistry;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.ThreadLocalSessionHolder;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.osgi.SpamHandlerServiceTracker;
import com.openexchange.systemname.SystemNameService;
import com.openexchange.textxtraction.TextXtractService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.tools.strings.StringParser;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.internal.UserConfigurationServiceImpl;
import com.openexchange.xml.jdom.JDOMParser;
import com.openexchange.xml.spring.SpringParser;

/**
 * {@link ServerActivator} - The activator for server bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ServerActivator extends DeferredActivator {

	private static final class ServiceAdderTrackerCustomizer implements ServiceTrackerCustomizer<FileMetadataParserService,FileMetadataParserService> {

        private final BundleContext context;

        public ServiceAdderTrackerCustomizer(final BundleContext context) {
            super();
            this.context = context;
        }

        @Override
        public void removedService(final ServiceReference<FileMetadataParserService> reference, final FileMetadataParserService service) {
            ServerServiceRegistry.getInstance().removeService(FileMetadataParserService.class);
            context.ungetService(reference);
        }

        @Override
        public void modifiedService(final ServiceReference<FileMetadataParserService> reference, final FileMetadataParserService service) {
            // Nope
        }

        @Override
        public FileMetadataParserService addingService(final ServiceReference<FileMetadataParserService> reference) {
            final FileMetadataParserService service = context.getService(reference);
            ServerServiceRegistry.getInstance().addService(FileMetadataParserService.class, service);
            return service;
        }
    }


    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ServerActivator.class));

    /**
     * Bundle ID of admin.<br>
     * TODO: Maybe this should be read by config.ini
     */
    private static final String BUNDLE_ID_ADMIN = "com.openexchange.admin";

    /**
     * Constant for string: "identifier"
     */
    private static final String STR_IDENTIFIER = "identifier";

    private static final Class<?>[] NEEDED_SERVICES_ADMIN =
        {
            ConfigurationService.class, CacheService.class, EventAdmin.class, TimerService.class, ThreadPoolService.class,
            CalendarAdministrationService.class, CalendarCollectionService.class, AppointmentSqlFactoryService.class
        };

    private static final Class<?>[] NEEDED_SERVICES_SERVER =
        {
            ConfigurationService.class, CacheService.class, EventAdmin.class, SessiondService.class, SpringParser.class, JDOMParser.class,
            TimerService.class, ThreadPoolService.class, CalendarAdministrationService.class, AppointmentSqlFactoryService.class,
            CalendarCollectionService.class, TargetService.class, MessagingServiceRegistry.class, HTMLService.class, IDBasedFileAccessFactory.class,
            FileStorageServiceRegistry.class, CryptoService.class, HttpService.class, SystemNameService.class, FolderUpdaterRegistry.class,
            ConfigViewFactory.class, StringParser.class, PreviewService.class, TextXtractService.class, SecretEncryptionFactoryService.class
        };

    private static volatile BundleContext CONTEXT;

    /**
     * Gets the bundle context.
     *
     * @return The bundle context or <code>null</code> if not started, yet
     */
    public static BundleContext getContext() {
        return CONTEXT;
    }

    private final List<ServiceRegistration<?>> registrationList;

    private final List<ServiceTracker<?,?>> serviceTrackerList;

    private final List<EventHandlerRegistration> eventHandlerList;

    private final List<BundleActivator> activators;

    private final Starter starter;

    private final AtomicBoolean started;

    private Boolean adminBundleInstalled;

    private WhiteboardSecretService secretService;

    /**
     * Initializes a new {@link ServerActivator}
     */
    public ServerActivator() {
        super();
        this.started = new AtomicBoolean();
        this.starter = new Starter();
        registrationList = new ArrayList<ServiceRegistration<?>>();
        serviceTrackerList = new ArrayList<ServiceTracker<?,?>>();
        eventHandlerList = new ArrayList<EventHandlerRegistration>();
        activators = new ArrayList<BundleActivator>(8);
    }

    /**
     * The server bundle will not start unless these services are available.
     */
    @Override
    protected Class<?>[] getNeededServices() {
        if (null == adminBundleInstalled) {
            this.adminBundleInstalled = Boolean.valueOf(isAdminBundleInstalled(context));
        }
        return this.adminBundleInstalled.booleanValue() ? NEEDED_SERVICES_ADMIN : NEEDED_SERVICES_SERVER;
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        /*
         * Never stop the server even if a needed service is absent
         */
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        if (CacheService.class.equals(clazz)) {
            final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
            if (null != reg) {
                try {
                    reg.notifyAbsence();
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        ServerServiceRegistry.getInstance().removeService(clazz);
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        ServerServiceRegistry.getInstance().addService(clazz, getService(clazz));
        if (CacheService.class.equals(clazz)) {
            final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
            if (null != reg) {
                try {
                    reg.notifyAvailability();
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    protected void startBundle() throws Exception {
        CONTEXT = context;
        JSONObject.setMaxSize(getService(ConfigurationService.class).getIntProperty("com.openexchange.json.maxSize", 1000));
        // get version information from MANIFEST file
        final Dictionary<?, ?> headers = context.getBundle().getHeaders();
        Version.buildnumber = "Rev" + (String) headers.get("OXRevision");
        Version.version = (String) headers.get("OXVersion");
        // (Re-)Initialize server service registry with available services
        {
            final ServerServiceRegistry registry = ServerServiceRegistry.getInstance();
            registry.clearRegistry();
            final Class<?>[] classes = getNeededServices();
            for (int i = 0; i < classes.length; i++) {
                final Object service = getService(classes[i]);
                if (null != service) {
                    registry.addService(classes[i], service);
                }
            }
        }
        if (!started.compareAndSet(false, true)) {
            /*
             * Don't start the server again. A duplicate call to startBundle() is probably caused by temporary absent service(s) whose
             * re-availability causes to trigger this method again.
             */
            LOG.info("A temporary absent service is available again");
            return;
        }
        LOG.info("starting bundle: com.openexchange.server");
        /*
         * Add service trackers
         */
        // Configuration service load
        final ServiceTracker<ConfigurationService,ConfigurationService> confTracker = new ServiceTracker<ConfigurationService,ConfigurationService>(context, ConfigurationService.class, new ConfigurationCustomizer(context));
        confTracker.open(); // We need this for {@link Starter#start()}
        serviceTrackerList.add(confTracker);
        // move this to the required services once the database component gets into its own bundle.
        serviceTrackerList.add(new ServiceTracker<DatabaseService,DatabaseService>(context, DatabaseService.class, new DatabaseCustomizer(context)));
        // I18n service load
        serviceTrackerList.add(new ServiceTracker<I18nService,I18nService>(context, I18nService.class, new I18nServiceListener(context)));

        // Mail account delete listener
        serviceTrackerList.add(new ServiceTracker<MailAccountDeleteListener,MailAccountDeleteListener>(
            context,
            MailAccountDeleteListener.class,
            new com.openexchange.mailaccount.internal.DeleteListenerServiceTracker(context)));

        // Mail provider service tracker
        serviceTrackerList.add(new ServiceTracker<MailProvider,MailProvider>(context, MailProvider.class, new MailProviderServiceTracker(context)));

        // Transport provider service tracker
        serviceTrackerList.add(new ServiceTracker<TransportProvider,TransportProvider>(context, TransportProvider.class, new TransportProviderServiceTracker(context)));

        // Spam handler provider service tracker
        serviceTrackerList.add(new ServiceTracker<SpamHandler,SpamHandler>(context, SpamHandler.class, new SpamHandlerServiceTracker(context)));

        // AJAX request handler
        serviceTrackerList.add(new ServiceTracker<AJAXRequestHandler,AJAXRequestHandler>(context, AJAXRequestHandler.class, new AJAXRequestHandlerCustomizer(context)));

        // contacts
        serviceTrackerList.add(new ServiceTracker<ContactInterfaceProvider,ContactInterfaceProvider>(context, ContactInterfaceProvider.class, new ContactServiceListener(context)));
        // ICal Parser
        serviceTrackerList.add(new ServiceTracker<ICalParser,ICalParser>(context, ICalParser.class, new RegistryCustomizer<ICalParser>(
            context,
            ICalParser.class){

            @Override
            protected ICalParser customize(final ICalParser service) {
                return new ExtraneousSeriesMasterRecoveryParser(service, ServerServiceRegistry.getInstance());
            }

        }));

        // ICal Emitter
        serviceTrackerList.add(new ServiceTracker<ICalEmitter,ICalEmitter>(context, ICalEmitter.class, new RegistryCustomizer<ICalEmitter>(
            context,
            ICalEmitter.class)));

        // Data Retention Service
        serviceTrackerList.add(new ServiceTracker<DataRetentionService,DataRetentionService>(
            context,
            DataRetentionService.class,
            new RegistryCustomizer<DataRetentionService>(context, DataRetentionService.class)));

        // Delete Listener Service Tracker
        serviceTrackerList.add(new ServiceTracker<DeleteListener,DeleteListener>(context, DeleteListener.class, new DeleteListenerServiceTrackerCustomizer(
            context)));

        // Folder Delete Listener Service Tracker
        serviceTrackerList.add(new ServiceTracker<FolderDeleteListenerService,FolderDeleteListenerService>(
            context,
            FolderDeleteListenerService.class,
            new FolderDeleteListenerServiceTrackerCustomizer(context)));

        /*
         * Register EventHandler
         */
        final OSGiEventDispatcher dispatcher = new OSGiEventDispatcher();
        EventQueue.setNewEventDispatcher(dispatcher);
        eventHandlerList.add(dispatcher);
        eventHandlerList.add(new MailAccessCacheEventListener());
        for (final EventHandlerRegistration ehr : eventHandlerList) {
            ehr.registerService(context);
        }

        /*
         * Start server dependent on whether admin bundle is available or not
         */
        if (adminBundleInstalled.booleanValue()) {
            // Start up server to only fit admin needs.
            starter.adminStart();
        } else {
            // Management is only needed for groupware.
            serviceTrackerList.add(new ServiceTracker<ManagementService,ManagementService>(context, ManagementService.class, new ManagementServiceTracker(context)));
            // TODO:
            /*-
             * serviceTrackerList.add(new ServiceTracker(context, MonitorService.class.getName(),
             *     new BundleServiceTracker&lt;MonitorService&gt;(context, MonitorService.getInstance(), MonitorService.class)));
             */

            // Search for AuthenticationService
            serviceTrackerList.add(new ServiceTracker<AuthenticationService,AuthenticationService>(context, AuthenticationService.class, new AuthenticationCustomizer(context)));
            // Search for ConfigJumpService
            serviceTrackerList.add(new ServiceTracker<ConfigJumpService,ConfigJumpService>(
                context,
                ConfigJumpService.class,
                new BundleServiceTracker<ConfigJumpService>(context, ConfigJump.getHolder(), ConfigJumpService.class)));
            // Search for extensions of the preferences tree interface
            serviceTrackerList.add(new ServiceTracker<PreferencesItemService,PreferencesItemService>(context, PreferencesItemService.class, new PreferencesCustomizer(context)));
            // Search for UserPasswordChange service
            serviceTrackerList.add(new ServiceTracker<PasswordChangeService,PasswordChangeService>(context, PasswordChangeService.class, new PasswordChangeCustomizer(context)));
            // Search for host name service
            serviceTrackerList.add(new ServiceTracker<HostnameService,HostnameService>(context, HostnameService.class, new HostnameServiceCustomizer(context)));
            // Conversion service
            serviceTrackerList.add(new ServiceTracker<ConversionService,ConversionService>(
                context,
                ConversionService.class,
                new RegistryCustomizer<ConversionService>(context, ConversionService.class)));
            // Contact collector
            serviceTrackerList.add(new ServiceTracker<ContactCollectorService,ContactCollectorService>(
                context,
                ContactCollectorService.class,
                new RegistryCustomizer<ContactCollectorService>(context, ContactCollectorService.class)));
            // Search Service
            serviceTrackerList.add(new ServiceTracker<SearchService,SearchService>(context, SearchService.class, new RegistryCustomizer<SearchService>(
                context,
                SearchService.class)));
            // Login handler
            serviceTrackerList.add(new ServiceTracker<LoginHandlerService,LoginHandlerService>(context, LoginHandlerService.class, new LoginHandlerCustomizer(context)));
            // Multiple handler factory services
            serviceTrackerList.add(new ServiceTracker<MultipleHandlerFactoryService,MultipleHandlerFactoryService>(
                context,
                MultipleHandlerFactoryService.class,
                new MultipleHandlerServiceTracker(context)));

            // Attachment Plugins
            serviceTrackerList.add(new AttachmentAuthorizationTracker(context));
            serviceTrackerList.add(new AttachmentListenerTracker(context));

           // PublicationTargetDiscoveryService
            serviceTrackerList.add(new ServiceTracker<PublicationTargetDiscoveryService,PublicationTargetDiscoveryService>(
                context,
                PublicationTargetDiscoveryService.class,
                new PublicationTargetDiscoveryServiceTrackerCustomizer(context)));

            // Folder Fields

            serviceTrackerList.add(new ServiceTracker<AdditionalFolderField,AdditionalFolderField>(context, AdditionalFolderField.class, new FolderFieldCollector(context, Folder.getAdditionalFields())));

            /*
             * The FileMetadataParserService needs to be tracked by a separate service tracker instead of just adding the service to
             * getNeededServices(), because publishing bundle needs the HttpService which is in turn provided by server
             */
            serviceTrackerList.add(new ServiceTracker<FileMetadataParserService,FileMetadataParserService>(
                context,
                FileMetadataParserService.class,
                new ServiceAdderTrackerCustomizer(context)));

            // Start up server the usual way
            starter.start();
        }
        // Open service trackers
        for (final ServiceTracker<?,?> tracker : serviceTrackerList) {
            tracker.open();
        }
        // Register server's services
        registrationList.add(context.registerService(CharsetProvider.class.getName(), new CustomCharsetProvider(), null));
        final GroupService groupService = new GroupServiceImpl();
        registrationList.add(context.registerService(GroupService.class.getName(), groupService, null));
        ServerServiceRegistry.getInstance().addService(GroupService.class, groupService);
        registrationList.add(context.registerService(
            ResourceService.class.getName(),
            ServerServiceRegistry.getInstance().getService(ResourceService.class, true),
            null));
        registrationList.add(context.registerService(
            UserService.class.getName(),
            ServerServiceRegistry.getInstance().getService(UserService.class, true),
            null));
        ServerServiceRegistry.getInstance().addService(UserConfigurationService.class, new UserConfigurationServiceImpl());
        registrationList.add(context.registerService(
            UserConfigurationService.class.getName(),
            ServerServiceRegistry.getInstance().getService(UserConfigurationService.class),
            null));
        registrationList.add(context.registerService(ContextService.class.getName(), ServerServiceRegistry.getInstance().getService(
            ContextService.class,
            true), null));
        // Register mail stuff
        {
            registrationList.add(context.registerService(MailService.class.getName(), new MailServiceImpl(), null));
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, MailSessionEventHandler.getTopics());
            registrationList.add(context.registerService(EventHandler.class.getName(), new MailSessionEventHandler(), serviceProperties));
            registrationList.add(context.registerService(MailCounter.class.getName(), new MailCounterImpl(), null));
            registrationList.add(context.registerService(MailIdleCounter.class.getName(), new MailIdleCounterImpl(), null));
        }
        // TODO: Register search service here until its encapsulated in an own bundle
        registrationList.add(context.registerService(SearchService.class.getName(), new SearchServiceImpl(), null));
        // TODO: Register server's login handler here until its encapsulated in an own bundle
        registrationList.add(context.registerService(LoginHandlerService.class.getName(), new MailLoginHandler(), null));
        registrationList.add(context.registerService(LoginHandlerService.class.getName(), new TransportLoginHandler(), null));
        registrationList.add(context.registerService(LoginHandlerService.class.getName(), new LastLoginRecorder(), null));
//        registrationList.add(context.registerService(LoginHandlerService.class.getName(), new PasswordCrypter(), null));
        // Register table creation for mail account storage.
        registrationList.add(context.registerService(CreateTableService.class.getName(), new CreateMailAccountTables(), null));
        registrationList.add(context.registerService(CreateTableService.class.getName(), new CreateIDSequenceTable(), null));
        // TODO: Register server's mail account storage here until its encapsulated in an own bundle
        registrationList.add(context.registerService(
            MailAccountStorageService.class.getName(),
            ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class),
            null));
        // TODO: Register server's Unified INBOX management here until its encapsulated in an own bundle
        registrationList.add(context.registerService(
            UnifiedINBOXManagement.class.getName(),
            ServerServiceRegistry.getInstance().getService(UnifiedINBOXManagement.class),
            null));
        // Register ID generator
        registrationList.add(context.registerService(
            IDGeneratorService.class.getName(),
            ServerServiceRegistry.getInstance().getService(IDGeneratorService.class),
            null));
        /*
         * Register data sources
         */
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.mail.vcard");
            registrationList.add(context.registerService(DataSource.class.getName(), new VCardMailPartDataSource(), props));
        }
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.mail.ical");
            registrationList.add(context.registerService(DataSource.class.getName(), new ICalMailPartDataSource(), props));
        }
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.contact");
            registrationList.add(context.registerService(DataSource.class.getName(), new ContactDataSource(), props));
        }
        {
            final InlineImageDataSource dataSource = InlineImageDataSource.getInstance();
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, dataSource.getRegistrationName());
            ImageServlet.addMapping(dataSource.getRegistrationName(), dataSource.getAlias());
            registrationList.add(context.registerService(DataSource.class.getName(), dataSource, props));
        }
        {
            final ContactImageDataSource dataSource = ContactImageDataSource.getInstance();
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, dataSource.getRegistrationName());
            ImageServlet.addMapping(dataSource.getRegistrationName(), dataSource.getAlias());
            registrationList.add(context.registerService(DataSource.class.getName(), dataSource, props));
        }
        {
            final ManagedFileImageDataSource dataSource = new ManagedFileImageDataSource();
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, dataSource.getRegistrationName());
            ImageServlet.addMapping(dataSource.getRegistrationName(), dataSource.getAlias());
            registrationList.add(context.registerService(DataSource.class.getName(), dataSource, props));
        }
        /*
         * Register data handlers
         */
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.contact");
            registrationList.add(context.registerService(DataHandler.class.getName(), new ContactInsertDataHandler(), props));
        }
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.contact.json");
            registrationList.add(context.registerService(DataHandler.class.getName(), new ContactJSONDataHandler(), props));
        }
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.ical");
            registrationList.add(context.registerService(DataHandler.class.getName(), new ICalInsertDataHandler(), props));
        }
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.ical.json");
            registrationList.add(context.registerService(DataHandler.class.getName(), new ICalJSONDataHandler(), props));
        }
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.mail.vcard");
            registrationList.add(context.registerService(DataHandler.class.getName(), new VCardAttachMailDataHandler(), props));
        }

        // Register DBProvider
        registrationList.add(context.registerService(DBProvider.class.getName(), new DBPoolProvider(), null));
        registrationList.add(context.registerService(WhiteboardFactoryService.class.getName(), new WhiteboardDBProvider.Factory(), null));

        // Register Infostore
        registrationList.add(context.registerService(InfostoreFacade.class.getName(), Infostore.FACADE, null));
        registrationList.add(context.registerService(InfostoreSearchEngine.class.getName(), Infostore.SEARCH_ENGINE, null));

        // Register AttachmentBase
        registrationList.add(context.registerService(AttachmentBase.class.getName(), Attachment.ATTACHMENT_BASE, null));

        // Register ContactSQL
        registrationList.add(context.registerService(ContactInterfaceFactory.class.getName(), new RdbContactInterfaceFactory(), null));

        // Register event factory service
        registrationList.add(context.registerService(EventFactoryService.class.getName(), new EventFactoryServiceImpl(), null));

        // Register folder service
        final FolderService folderService = new FolderServiceImpl();
        registrationList.add(context.registerService(FolderService.class.getName(), folderService, null));
        ServerServiceRegistry.getInstance().addService(FolderService.class, folderService);

        // Register contact interface discovery service
        final ContactInterfaceDiscoveryService cids = ContactInterfaceDiscoveryServiceImpl.getInstance();
        registrationList.add(context.registerService(ContactInterfaceDiscoveryService.class.getName(), cids, null));
        ServerServiceRegistry.getInstance().addService(ContactInterfaceDiscoveryService.class, cids);

        // Register SessionHolder
        registrationList.add(context.registerService(SessionHolder.class.getName(), ThreadLocalSessionHolder.getInstance(), null));
        ServerServiceRegistry.getInstance().addService(SessionHolder.class, ThreadLocalSessionHolder.getInstance());

        // Fake bundle start
        activators.add(new FolderStorageActivator());
        for (final BundleActivator activator : activators) {
            activator.start(context);
        }

        ServerServiceRegistry.getInstance().addService(SecretService.class, secretService = new WhiteboardSecretService(context));
        secretService.open();

        /*
         * Register servlets
         */
        if (!adminBundleInstalled.booleanValue()) {
            registerServlets(getService(HttpService.class));
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.server");
        try {
            /*
             * Fake bundle stop
             */
            for (final BundleActivator activator : activators) {
                activator.stop(context);
            }
            activators.clear();
            /*
             * Unregister server's services
             */
            for (final ServiceRegistration<?> registration : registrationList) {
                registration.unregister();
            }
            registrationList.clear();
            /*
             * Close service trackers
             */
            for (final ServiceTracker<?,?> tracker : serviceTrackerList) {
                tracker.close();
            }
            serviceTrackerList.clear();
            ServerRequestHandlerRegistry.getInstance().clearRegistry();
            /*
             * Unregister EventHandler
             */
            for (final EventHandlerRegistration ehr : eventHandlerList) {
                ehr.unregisterService();
            }
            eventHandlerList.clear();
            // Stop all inside the server.
            starter.stop();
            /*
             * Clear service registry
             */
            ServerServiceRegistry.getInstance().clearRegistry();
            if (null != secretService) {
                secretService.close();
                secretService = null;
            }
        } finally {
            started.set(false);
            adminBundleInstalled = null;
            CONTEXT = null;
        }
    }

    /**
     * Determines if admin bundle is installed by iterating context's bundles whose status is set to {@link Bundle#INSTALLED} or
     * {@link Bundle#ACTIVE} and whose symbolic name equals {@value #BUNDLE_ID_ADMIN}.
     *
     * @param context The bundle context
     * @return <code>true</code> if admin bundle is installed; otherwise <code>false</code>
     */
    private static boolean isAdminBundleInstalled(final BundleContext context) {
        for (final Bundle bundle : context.getBundles()) {
            if (BUNDLE_ID_ADMIN.equals(bundle.getSymbolicName())) {
                return true;
            }
        }
        return false;
    }

    private void registerServlets(final HttpService http) throws ServletException, NamespaceException {
        http.registerServlet("/infostore", new com.openexchange.webdav.Infostore(), null, null);
        http.registerServlet("/servlet/webdav.ical", new com.openexchange.webdav.ical(), null, null);
        http.registerServlet("/servlet/webdav.vcard", new com.openexchange.webdav.vcard(), null, null);
        http.registerServlet("/servlet/webdav.version", new com.openexchange.webdav.version(), null, null);
        http.registerServlet("/servlet/webdav.folders", new com.openexchange.webdav.folders(), null, null);
        http.registerServlet("/servlet/webdav.calendar", new com.openexchange.webdav.calendar(), null, null);
        http.registerServlet("/servlet/webdav.contacts", new com.openexchange.webdav.contacts(), null, null);
        http.registerServlet("/servlet/webdav.tasks", new com.openexchange.webdav.tasks(), null, null);
        http.registerServlet("/servlet/webdav.groupuser", new com.openexchange.webdav.groupuser(), null, null);
        http.registerServlet("/servlet/webdav.attachments", new com.openexchange.webdav.attachments(), null, null);
        http.registerServlet("/servlet/webdav.infostore", new com.openexchange.webdav.Infostore(), null, null);
        http.registerServlet("/servlet/webdav.freebusy", new com.openexchange.webdav.freebusy(), null, null);
//        http.registerServlet("/ajax/tasks", new com.openexchange.ajax.Tasks(), null, null);
//        http.registerServlet("/ajax/contacts", new com.openexchange.ajax.Contact(), null, null);
//        http.registerServlet("/ajax/mail", new com.openexchange.ajax.Mail(), null, null);

        http.registerServlet("/ajax/mail.attachment", new com.openexchange.ajax.MailAttachment(), null, null);
        // http.registerServlet("/ajax/calendar", new com.openexchange.ajax.Appointment(), null, null);
        // http.registerServlet("/ajax/config", new com.openexchange.ajax.ConfigMenu(), null, null);
        // http.registerServlet("/ajax/attachment", new com.openexchange.ajax.Attachment(), null, null);
        // http.registerServlet("/ajax/reminder", new com.openexchange.ajax.Reminder(), null, null);
        // http.registerServlet("/ajax/group", new com.openexchange.ajax.Group(), null, null);
        // http.registerServlet("/ajax/resource", new com.openexchange.ajax.Resource(), null, null);
        http.registerServlet("/ajax/link", new com.openexchange.ajax.Link(), null, null);
        http.registerServlet("/ajax/multiple", new com.openexchange.ajax.Multiple(), null, null);
        // http.registerServlet("/ajax/quota", new com.openexchange.ajax.Quota(), null, null);
        http.registerServlet("/ajax/control", new com.openexchange.ajax.ConfigJump(), null, null);
        // http.registerServlet("/ajax/file", new com.openexchange.ajax.AJAXFile(), null, null);
        // http.registerServlet("/ajax/import", new com.openexchange.ajax.ImportServlet(), null, null);
        // http.registerServlet("/ajax/export", new com.openexchange.ajax.ExportServlet(), null, null);
        http.registerServlet("/ajax/image", new com.openexchange.image.servlet.ImageServlet(), null, null);
        http.registerServlet("/ajax/sync", new com.openexchange.ajax.SyncServlet(), null, null);
    }

}

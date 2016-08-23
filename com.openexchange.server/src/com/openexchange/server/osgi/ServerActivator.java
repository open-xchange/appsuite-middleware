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

package com.openexchange.server.osgi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.spi.CharsetProvider;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import javax.activation.MailcapCommandMap;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.customizer.folder.osgi.FolderFieldCollector;
import com.openexchange.ajax.requesthandler.AJAXRequestHandler;
import com.openexchange.auth.Authenticator;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.auth.mbean.impl.AuthenticatorMBeanImpl;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.auth.rmi.impl.RemoteAuthenticatorImpl;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.charset.CustomCharsetProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configjump.ConfigJumpService;
import com.openexchange.configjump.client.ConfigJump;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
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
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DatabaseServiceDBProvider;
import com.openexchange.databaseold.Database;
import com.openexchange.dataretention.DataRetentionService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.event.EventFactoryService;
import com.openexchange.event.impl.EventFactoryServiceImpl;
import com.openexchange.event.impl.EventQueue;
import com.openexchange.event.impl.osgi.EventHandlerRegistration;
import com.openexchange.event.impl.osgi.OSGiEventDispatcher;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.parse.FileMetadataParserService;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.filemanagement.DistributedFileManagement;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.folder.FolderDeleteListenerService;
import com.openexchange.folder.FolderService;
import com.openexchange.folder.internal.FolderDeleteListenerServiceTrackerCustomizer;
import com.openexchange.folder.internal.FolderServiceImpl;
import com.openexchange.folderstorage.FolderI18nNamesService;
import com.openexchange.folderstorage.internal.FolderI18nNamesServiceImpl;
import com.openexchange.folderstorage.osgi.FolderStorageActivator;
import com.openexchange.group.GroupService;
import com.openexchange.group.internal.GroupServiceImpl;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.alias.impl.CachingAliasStorage;
import com.openexchange.groupware.alias.impl.RdbAliasStorage;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarAdministrationService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.contact.datasource.ContactDataSource;
import com.openexchange.groupware.datahandler.ICalInsertDataHandler;
import com.openexchange.groupware.datahandler.ICalJSONDataHandler;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.delete.contextgroup.DeleteContextGroupListener;
import com.openexchange.groupware.impl.id.CreateIDSequenceTable;
import com.openexchange.groupware.importexport.importers.ExtraneousSeriesMasterRecoveryParser;
import com.openexchange.groupware.infostore.EventFiringInfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.infostore.facade.impl.EventFiringInfostoreFacadeImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.groupware.userconfiguration.osgi.CapabilityRegistrationListener;
import com.openexchange.guest.GuestService;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.I18nService;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.lock.LockService;
import com.openexchange.lock.impl.LockServiceImpl;
import com.openexchange.log.Slf4jLogger;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.login.BlockingLoginHandlerService;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.internal.LoginNameRecorder;
import com.openexchange.login.listener.LoginListener;
import com.openexchange.mail.MailCounterImpl;
import com.openexchange.mail.MailIdleCounterImpl;
import com.openexchange.mail.MailQuotaProvider;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.api.unified.UnifiedViewService;
import com.openexchange.mail.attachment.AttachmentTokenService;
import com.openexchange.mail.cache.MailAccessCacheEventListener;
import com.openexchange.mail.cache.MailSessionEventHandler;
import com.openexchange.mail.conversion.ICalMailPartDataSource;
import com.openexchange.mail.conversion.VCardAttachMailDataHandler;
import com.openexchange.mail.conversion.VCardMailPartDataSource;
import com.openexchange.mail.json.compose.ComposeHandlerRegistry;
import com.openexchange.mail.json.compose.share.internal.AttachmentStorageRegistry;
import com.openexchange.mail.json.compose.share.internal.EnabledCheckerRegistry;
import com.openexchange.mail.json.compose.share.internal.MessageGeneratorRegistry;
import com.openexchange.mail.json.compose.share.internal.ShareLinkGeneratorRegistry;
import com.openexchange.mail.loginhandler.MailLoginHandler;
import com.openexchange.mail.loginhandler.TransportLoginHandler;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.osgi.MailCapabilityServiceTracker;
import com.openexchange.mail.osgi.MailProviderServiceTracker;
import com.openexchange.mail.osgi.MailSessionCacheInvalidator;
import com.openexchange.mail.osgi.MailcapServiceTracker;
import com.openexchange.mail.osgi.TransportProviderServiceTracker;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.service.impl.MailServiceImpl;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.config.NoReplyConfigFactory;
import com.openexchange.mail.transport.config.impl.DefaultNoReplyConfigFactory;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.internal.CreateMailAccountTables;
import com.openexchange.mailaccount.internal.DeleteListenerServiceTracker;
import com.openexchange.management.ManagementService;
import com.openexchange.management.Managements;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.multiple.internal.MultipleHandlerServiceTracker;
import com.openexchange.notification.service.FullNameBuilderService;
import com.openexchange.oauth.provider.resourceserver.OAuthResourceService;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.objectusecount.service.ObjectUseCountServiceTracker;
import com.openexchange.osgi.BundleServiceTracker;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.passwordmechs.PasswordMechFactory;
import com.openexchange.preview.PreviewService;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.resource.ResourceService;
import com.openexchange.search.SearchService;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.server.impl.Starter;
import com.openexchange.server.reloadable.GenericReloadable;
import com.openexchange.server.services.ServerRequestHandlerRegistry;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.serverconfig.ServerConfigService;
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
import com.openexchange.uadetector.UserAgentParser;
import com.openexchange.user.UserService;
import com.openexchange.user.UserServiceInterceptor;
import com.openexchange.user.UserServiceInterceptorRegistry;
import com.openexchange.user.internal.UserServiceImpl;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;
import com.openexchange.userconf.internal.UserConfigurationServiceImpl;
import com.openexchange.userconf.internal.UserPermissionServiceImpl;
import com.openexchange.xml.jdom.JDOMParser;
import com.openexchange.xml.spring.SpringParser;
import net.htmlparser.jericho.Config;
import net.htmlparser.jericho.LoggerProvider;

/**
 * {@link ServerActivator} - The activator for server bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ServerActivator extends HousekeepingActivator {

    private static final class ServiceAdderTrackerCustomizer implements ServiceTrackerCustomizer<FileMetadataParserService, FileMetadataParserService> {

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

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServerActivator.class);

    /**
     * Constant for string: "identifier"
     */
    private static final String STR_IDENTIFIER = "identifier";

    private static final Class<?>[] NEEDED_SERVICES_SERVER = {
        ConfigurationService.class, DatabaseService.class, CacheService.class, EventAdmin.class, SessiondService.class, SpringParser.class,
        JDOMParser.class, TimerService.class, ThreadPoolService.class, CalendarAdministrationService.class,
        AppointmentSqlFactoryService.class, CalendarCollectionService.class, MessagingServiceRegistry.class, HtmlService.class,
        IDBasedFolderAccessFactory.class, IDBasedFileAccessFactory.class, FileStorageServiceRegistry.class, FileStorageAccountManagerLookupService.class,
        CryptoService.class, HttpService.class, SystemNameService.class, ConfigViewFactory.class, StringParser.class, PreviewService.class,
        TextXtractService.class, SecretEncryptionFactoryService.class, SearchService.class, DispatcherPrefixService.class,
        UserAgentParser.class, PasswordMechFactory.class };

    private static volatile BundleContext CONTEXT;

    /**
     * Gets the bundle context.
     *
     * @return The bundle context or <code>null</code> if not started, yet
     */
    public static BundleContext getContext() {
        return CONTEXT;
    }

    private final List<ServiceTracker<?, ?>> serviceTrackerList;
    private final List<EventHandlerRegistration> eventHandlerList;
    private final List<BundleActivator> activators;
    private final Starter starter;
    private volatile WhiteboardSecretService secretService;
    private volatile LockServiceImpl lockService;

    /**
     * Initializes a new {@link ServerActivator}
     */
    public ServerActivator() {
        super();
        this.starter = new Starter();
        serviceTrackerList = new ArrayList<ServiceTracker<?, ?>>();
        eventHandlerList = new ArrayList<EventHandlerRegistration>();
        activators = new ArrayList<BundleActivator>(8);
    }

    /**
     * The server bundle will not start unless these services are available.
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES_SERVER;
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
        if (CacheService.class.equals(clazz)) {
            final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
            if (null != reg) {
                try {
                    reg.notifyAbsence();
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
        }
        ServerServiceRegistry.getInstance().removeService(clazz);
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        ServerServiceRegistry.getInstance().addService(clazz, getService(clazz));
        if (CacheService.class.equals(clazz)) {
            final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
            if (null != reg) {
                try {
                    reg.notifyAvailability();
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        CONTEXT = context;
        {
            // Set logger
            JSONObject.setLogger(new Slf4jLogger(JSONValue.class));
            // JSON configuration
            final ConfigurationService service = getService(ConfigurationService.class);
            JSONObject.setMaxSize(service.getIntProperty("com.openexchange.json.maxSize", 2500));
        }
        Config.LoggerProvider = LoggerProvider.DISABLED;
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

            Database.setDatabaseService(getService(DatabaseService.class));
        }
        LOG.info("starting bundle: com.openexchange.server");
        /*
         * Add service trackers
         */
        // Configuration service load
        final ServiceTracker<ConfigurationService, ConfigurationService> confTracker = new ServiceTracker<ConfigurationService, ConfigurationService>(context, ConfigurationService.class, new ConfigurationCustomizer(context));
        confTracker.open(); // We need this for {@link Starter#start()}
        serviceTrackerList.add(confTracker);

        // I18n service load
        track(I18nService.class, new I18nServiceListener(context));

        // Audit logger
        track(AuditLogService.class, new RegistryCustomizer<AuditLogService>(context, AuditLogService.class));

        // Full-name builder
        track(ServerConfigService.class, new RegistryCustomizer<ServerConfigService>(context, ServerConfigService.class));
        track(FullNameBuilderService.class, new RegistryCustomizer<FullNameBuilderService>(context, FullNameBuilderService.class));

        // Mail account delete listener
        track(MailAccountDeleteListener.class, new DeleteListenerServiceTracker(context));

        // Mail provider service tracker
        track(CacheEventService.class, new MailSessionCacheInvalidator(context));
        track(MailProvider.class, new MailProviderServiceTracker(context));
        track(MailcapCommandMap.class, new MailcapServiceTracker(context));
        track(CapabilityService.class, new MailCapabilityServiceTracker(context));
        track(AttachmentTokenService.class, new RegistryCustomizer<AttachmentTokenService>(context, AttachmentTokenService.class));
        // Mail compose stuff
        track(ComposeHandlerRegistry.class, new RegistryCustomizer<ComposeHandlerRegistry>(context, ComposeHandlerRegistry.class));
        track(ShareLinkGeneratorRegistry.class, new RegistryCustomizer<ShareLinkGeneratorRegistry>(context, ShareLinkGeneratorRegistry.class));
        track(MessageGeneratorRegistry.class, new RegistryCustomizer<MessageGeneratorRegistry>(context, MessageGeneratorRegistry.class));
        track(AttachmentStorageRegistry.class, new RegistryCustomizer<AttachmentStorageRegistry>(context, AttachmentStorageRegistry.class));
        track(EnabledCheckerRegistry.class, new RegistryCustomizer<EnabledCheckerRegistry>(context, EnabledCheckerRegistry.class));

        // Image transformation service
        track(ImageTransformationService.class, new RegistryCustomizer<ImageTransformationService>(context, ImageTransformationService.class));

        // Transport provider service tracker
        track(TransportProvider.class, new TransportProviderServiceTracker(context));

        // Spam handler provider service tracker
        track(SpamHandler.class, new SpamHandlerServiceTracker(context));

        // CacheEventService
        track(CacheEventService.class, new RegistryCustomizer<CacheEventService>(context, CacheEventService.class));

        // AJAX request handler
        track(AJAXRequestHandler.class, new AJAXRequestHandlerCustomizer(context));

        // ICal Parser
        track(ICalParser.class, new RegistryCustomizer<ICalParser>(context, ICalParser.class) {

            @Override
            protected ICalParser customize(final ICalParser service) {
                return new ExtraneousSeriesMasterRecoveryParser(service, ServerServiceRegistry.getInstance());
            }

        });

        // ICal Emitter
        track(ICalEmitter.class, new RegistryCustomizer<ICalEmitter>(context, ICalEmitter.class));

        // vCard service & storage
        track(VCardService.class, new RegistryCustomizer<VCardService>(context, VCardService.class));
        track(VCardStorageFactory.class, new RegistryCustomizer<VCardStorageFactory>(context, VCardStorageFactory.class));

        // Data Retention Service
        track(DataRetentionService.class, new RegistryCustomizer<DataRetentionService>(context, DataRetentionService.class));

        // Delete Listener Service Tracker
        track(DeleteListener.class, new DeleteListenerServiceTrackerCustomizer(context));

        // Folder Delete Listener Service Tracker
        track(FolderDeleteListenerService.class, new FolderDeleteListenerServiceTrackerCustomizer(context));

        // Delete Context Group Listener Service Tracker
        track(DeleteContextGroupListener.class, new DeleteContextGroupListenerServiceTracker(context));

        // Distributed files
        track(DistributedFileManagement.class, new DistributedFilesListener());

        // CapabilityService
        track(CapabilityService.class, new CapabilityRegistrationListener(context));

        // Authenticator
        track(Authenticator.class, new RegistryCustomizer<Authenticator>(context, Authenticator.class));
        track(ManagementService.class, new SimpleRegistryListener<ManagementService>() {

            @Override
            public void added(ServiceReference<ManagementService> ref, ManagementService management) {
                try {
                    ObjectName objectName = Managements.getObjectName(AuthenticatorMBean.class.getName(), AuthenticatorMBean.DOMAIN);
                    management.registerMBean(objectName, new AuthenticatorMBeanImpl());
                } catch (Exception e) {
                    LOG.warn("Could not register MBean {}", AuthenticatorMBean.class.getName());
                }
            }

            @Override
            public void removed(ServiceReference<ManagementService> ref, ManagementService management) {
                try {
                    management.unregisterMBean(Managements.getObjectName(AuthenticatorMBean.class.getName(), AuthenticatorMBean.DOMAIN));
                } catch (Exception e) {
                    LOG.warn("Could not un-register MBean {}", AuthenticatorMBean.class.getName());
                }
            }
        });
        {
            Dictionary<String, Object> props = new Hashtable<String, Object>(2);
            props.put("RMIName", RemoteAuthenticator.RMI_NAME);
            registerService(Remote.class, new RemoteAuthenticatorImpl(), props);
        }

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

        track(ManagementService.class, new ManagementServiceTracker(context));
        // TODO:
        /*-
         * serviceTrackerList.add(new ServiceTracker(context, MonitorService.class.getName(),
         *     new BundleServiceTracker&lt;MonitorService&gt;(context, MonitorService.getInstance(), MonitorService.class)));
         */

        // Search for ConfigJumpService
        track(ConfigJumpService.class, new BundleServiceTracker<ConfigJumpService>(context, ConfigJump.getHolder(), ConfigJumpService.class));
        // Search for extensions of the preferences tree interface
        track(PreferencesItemService.class, new PreferencesCustomizer(context));
        // Search for UserPasswordChange service
        track(PasswordChangeService.class, new PasswordChangeCustomizer(context));
        // Search for host name service
        track(HostnameService.class, new HostnameServiceCustomizer(context));
        // Conversion service
        track(ConversionService.class, new RegistryCustomizer<ConversionService>(context, ConversionService.class));
        // Contact collector
        track(ContactCollectorService.class, new RegistryCustomizer<ContactCollectorService>(context, ContactCollectorService.class));
        // Login handler
        track(LoginHandlerService.class, new LoginHandlerCustomizer(context));
        track(BlockingLoginHandlerService.class, new BlockingLoginHandlerCustomizer(context));
        // Login listener
        track(LoginListener.class, new LoginListenerCustomizer(context));
        // Multiple handler factory services
        track(MultipleHandlerFactoryService.class, new MultipleHandlerServiceTracker(context));

        track(GuestService.class, new RegistryCustomizer<GuestService>(context, GuestService.class));

        // Attachment Plugins
        serviceTrackerList.add(new AttachmentAuthorizationTracker(context));
        serviceTrackerList.add(new AttachmentListenerTracker(context));

        // PublicationTargetDiscoveryService
        track(PublicationTargetDiscoveryService.class, new PublicationTargetDiscoveryServiceTrackerCustomizer(context));

        // Folder Fields
        track(AdditionalFolderField.class, new FolderFieldCollector(context, Folder.getAdditionalFields()));

        /*
         * The FileMetadataParserService needs to be tracked by a separate service tracker instead of just adding the service to
         * getNeededServices(), because publishing bundle needs the HttpService which is in turn provided by server
         */
        track(FileMetadataParserService.class, new ServiceAdderTrackerCustomizer(context));

        /*
         * Track ManagedFileManagement
         */
        track(ManagedFileManagement.class, new RegistryCustomizer<ManagedFileManagement>(context, ManagedFileManagement.class));

        /*
         * Track UnifiedViewService
         */
        track(UnifiedViewService.class, new RegistryCustomizer<UnifiedViewService>(context, UnifiedViewService.class));

        /*
         * Track OAuth provider services
         */
        track(OAuthResourceService.class, new RegistryCustomizer<OAuthResourceService>(context, OAuthResourceService.class));

        /*
         * Track QuotaFileStorageService
         */
        track(QuotaFileStorageService.class, new RegistryCustomizer<QuotaFileStorageService>(context, QuotaFileStorageService.class));

        /*
         * User Alias Service
         */
        UserAliasStorage aliasStorage;
        {
            String regionName = "UserAlias";
            byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
                "jcs.region."+regionName+".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
                "jcs.region."+regionName+".cacheattributes.MaxObjects=1000000\n" +
                "jcs.region."+regionName+".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
                "jcs.region."+regionName+".cacheattributes.UseMemoryShrinker=true\n" +
                "jcs.region."+regionName+".cacheattributes.MaxMemoryIdleTimeSeconds=360\n" +
                "jcs.region."+regionName+".cacheattributes.ShrinkerIntervalSeconds=60\n" +
                "jcs.region."+regionName+".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
                "jcs.region."+regionName+".elementattributes.IsEternal=false\n" +
                "jcs.region."+regionName+".elementattributes.MaxLifeSeconds=-1\n" +
                "jcs.region."+regionName+".elementattributes.IdleTime=360\n" +
                "jcs.region."+regionName+".elementattributes.IsSpool=false\n" +
                "jcs.region."+regionName+".elementattributes.IsRemote=false\n" +
                "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes();
            getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf));

            aliasStorage = new CachingAliasStorage(new RdbAliasStorage());
            ServerServiceRegistry.getInstance().addService(UserAliasStorage.class, aliasStorage);
        }

        /*
         * User Service
         */
        UserServiceInterceptorRegistry interceptorRegistry = new UserServiceInterceptorRegistry(context);
        track(UserServiceInterceptor.class, interceptorRegistry);

        UserService userService = new UserServiceImpl(interceptorRegistry, getService(PasswordMechFactory.class));
        ServerServiceRegistry.getInstance().addService(UserService.class, userService);

        track(ObjectUseCountService.class, new ObjectUseCountServiceTracker(context));

        // Start up server the usual way
        starter.start();
        // Open service trackers
        for (final ServiceTracker<?, ?> tracker : serviceTrackerList) {
            tracker.open();
        }
        openTrackers();
        // Register server's services
        registerService(UserService.class, userService);
        registerService(UserAliasStorage.class, aliasStorage);
        registerService(Reloadable.class, ServerConfig.getInstance());
        registerService(Reloadable.class, SystemConfig.getInstance());
        registerService(Reloadable.class, GenericReloadable.getInstance());
        registerService(CharsetProvider.class, new CustomCharsetProvider());
        final GroupService groupService = new GroupServiceImpl();
        registerService(GroupService.class, groupService);
        ServerServiceRegistry.getInstance().addService(GroupService.class, groupService);
        registerService(ResourceService.class, ServerServiceRegistry.getInstance().getService(ResourceService.class, true));
        ServerServiceRegistry.getInstance().addService(UserConfigurationService.class, new UserConfigurationServiceImpl());
        registerService(UserConfigurationService.class, ServerServiceRegistry.getInstance().getService(UserConfigurationService.class, true));

        ServerServiceRegistry.getInstance().addService(UserPermissionService.class, new UserPermissionServiceImpl());
        registerService(UserPermissionService.class, ServerServiceRegistry.getInstance().getService(UserPermissionService.class, true));

        ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class, true);
        registerService(ContextService.class, contextService);
        // Register mail stuff
        MailServiceImpl mailService = new MailServiceImpl();
        {
            registerService(MailService.class, mailService);
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, MailSessionEventHandler.getTopics());
            registerService(EventHandler.class, new MailSessionEventHandler(), serviceProperties);
            registerService(MailCounter.class, new MailCounterImpl());
            registerService(MailIdleCounter.class, new MailIdleCounterImpl());
            registerService(MimeTypeMap.class, new MimeTypeMap() {

                @Override
                public String getContentType(final File file) {
                    return MimeType2ExtMap.getContentType(file);
                }

                @Override
                public String getContentType(final String fileName) {
                    return MimeType2ExtMap.getContentType(fileName);
                }

                @Override
                public String getContentTypeByExtension(final String extension) {
                    return MimeType2ExtMap.getContentTypeByExtension(extension);
                }

                @Override
                public List<String> getFileExtensions(final String mime) {
                    return MimeType2ExtMap.getFileExtensions(mime);
                }
            });
        }
        registerService(NoReplyConfigFactory.class, new DefaultNoReplyConfigFactory(contextService, getService(ConfigViewFactory.class)));
        // TODO: Register server's login handler here until its encapsulated in an own bundle
        registerService(LoginHandlerService.class, new MailLoginHandler());
        registerService(LoginHandlerService.class, new TransportLoginHandler());
        registerService(LoginHandlerService.class, new LoginNameRecorder(userService));
        // registrationList.add(context.registerService(LoginHandlerService.class.getName(), new PasswordCrypter(), null));
        // Register table creation for mail account storage.
        registerService(CreateTableService.class, new CreateMailAccountTables());
        registerService(CreateTableService.class, new CreateIDSequenceTable());
        // TODO: Register server's mail account storage here until its encapsulated in an own bundle
        MailAccountStorageService mailAccountStorageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
        registerService(MailAccountStorageService.class, mailAccountStorageService);
        // TODO: Register server's Unified Mail management here until its encapsulated in an own bundle
        registerService(UnifiedInboxManagement.class, ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class));
        // TODO: Register server's Unified Mail management here until its encapsulated in an own bundle
        registerService(QuotaProvider.class, new MailQuotaProvider(mailAccountStorageService, mailService));
        // Register ID generator
        registerService(IDGeneratorService.class, ServerServiceRegistry.getInstance().getService(IDGeneratorService.class));
        /*
         * Register data sources
         */
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.mail.vcard");
            registerService(DataSource.class, new VCardMailPartDataSource(), props);
        }
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.mail.ical");
            registerService(DataSource.class, new ICalMailPartDataSource(), props);
        }
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.contact");
            registerService(DataSource.class, new ContactDataSource(), props);
        }
        // {
        // final InlineImageDataSource dataSource = InlineImageDataSource.getInstance();
        // final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
        // props.put(STR_IDENTIFIER, dataSource.getRegistrationName());
        // registerService(DataSource.class, dataSource, props);
        // ImageServlet.addMapping(dataSource.getRegistrationName(), dataSource.getAlias());
        // }
        // {
        // final ContactImageDataSource dataSource = ContactImageDataSource.getInstance();
        // final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
        // props.put(STR_IDENTIFIER, dataSource.getRegistrationName());
        // registerService(DataSource.class, dataSource, props);
        // ImageServlet.addMapping(dataSource.getRegistrationName(), dataSource.getAlias());
        // }
        // {
        // final ManagedFileImageDataSource dataSource = new ManagedFileImageDataSource();
        // final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
        // props.put(STR_IDENTIFIER, dataSource.getRegistrationName());
        // registerService(DataSource.class, dataSource, props);
        // ImageServlet.addMapping(dataSource.getRegistrationName(), dataSource.getAlias());
        // }
        /*
         * Register data handlers
         */
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.ical");
            registerService(DataHandler.class, new ICalInsertDataHandler(), props);
        }
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.ical.json");
            registerService(DataHandler.class, new ICalJSONDataHandler(), props);
        }
        {
            final Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put(STR_IDENTIFIER, "com.openexchange.mail.vcard");
            registerService(DataHandler.class, new VCardAttachMailDataHandler(), props);
        }

        // Register DBProvider
        registerService(DBProvider.class, new DBPoolProvider());

        // Register Infostore
        registerInfostore();

        // Register AttachmentBase
        registerService(AttachmentBase.class, Attachment.ATTACHMENT_BASE);

        // Register event factory service
        {
            final EventFactoryServiceImpl eventFactoryServiceImpl = new EventFactoryServiceImpl();
            registerService(EventFactoryService.class, eventFactoryServiceImpl);
            ServerServiceRegistry.getInstance().addService(EventFactoryService.class, eventFactoryServiceImpl);
        }

        // Register folder service
        final FolderService folderService = new FolderServiceImpl();
        registerService(FolderService.class, folderService);
        ServerServiceRegistry.getInstance().addService(FolderService.class, folderService);

        // Register folder i18n name service
        FolderI18nNamesServiceImpl folderI18nNamesService = FolderI18nNamesServiceImpl.getInstance();
        registerService(FolderI18nNamesService.class, folderI18nNamesService);
        ServerServiceRegistry.getInstance().addService(FolderI18nNamesService.class, folderI18nNamesService);

        // Register SessionHolder
        registerService(SessionHolder.class, ThreadLocalSessionHolder.getInstance());
        ServerServiceRegistry.getInstance().addService(SessionHolder.class, ThreadLocalSessionHolder.getInstance());

        // Fake bundle start
        activators.add(new FolderStorageActivator());
        for (final BundleActivator activator : activators) {
            activator.start(context);
        }

        WhiteboardSecretService secretService = new WhiteboardSecretService(context);
        this.secretService = secretService;
        ServerServiceRegistry.getInstance().addService(SecretService.class, secretService);
        secretService.open();

        // Cache for generic volatile locks
        {
            LockServiceImpl lockService = new LockServiceImpl();
            this.lockService = lockService;
            ServerServiceRegistry.getInstance().addService(LockService.class, lockService);
            registerService(LockService.class, lockService);
        }

        /*
         * Register servlets
         */
        registerServlets(getService(HttpService.class));
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
            unregisterServices();
            /*
             * Close service trackers
             */
            for (final ServiceTracker<?, ?> tracker : serviceTrackerList) {
                tracker.close();
            }
            closeTrackers();
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
            WhiteboardSecretService secretService = this.secretService;
            if (null != secretService) {
                this.secretService = null;
                secretService.close();
            }
            LockServiceImpl lockService = this.lockService;
            if (null != lockService) {
                this.lockService = null;
                lockService.dispose();
            }
            LoginServlet.setRampUpServices(null);
            UploadUtility.shutDown();
        } finally {
            started.set(false);
            CONTEXT = null;
        }
    }

    private void registerInfostore() {
        DBProvider dbProvider = new DatabaseServiceDBProvider(getService(DatabaseService.class));
        InfostoreFacadeImpl infostoreFacade = new InfostoreFacadeImpl(dbProvider);
        infostoreFacade.setTransactional(true);
        infostoreFacade.setSessionHolder(ThreadLocalSessionHolder.getInstance());
        EventFiringInfostoreFacade eventFiringInfostoreFacade = new EventFiringInfostoreFacadeImpl(dbProvider);
        eventFiringInfostoreFacade.setTransactional(true);
        eventFiringInfostoreFacade.setSessionHolder(ThreadLocalSessionHolder.getInstance());
        registerService(InfostoreFacade.class, infostoreFacade);
        registerService(EventFiringInfostoreFacade.class, eventFiringInfostoreFacade);
        registerService(InfostoreSearchEngine.class, infostoreFacade);
    }

    private void registerServlets(final HttpService http) throws ServletException, NamespaceException {
        http.registerServlet("/infostore", new com.openexchange.webdav.Infostore(), null, null);
        http.registerServlet("/files", new com.openexchange.webdav.Infostore(), null, null);
        http.registerServlet("/drive", new com.openexchange.webdav.Infostore(), null, null);
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
        http.registerServlet("/servlet/webdav.drive", new com.openexchange.webdav.Infostore(), null, null);
        http.registerServlet("/servlet/webdav.freebusy", new com.openexchange.webdav.freebusy(), null, null);
        // http.registerServlet(prefix+"tasks", new com.openexchange.ajax.Tasks(), null, null);
        // http.registerServlet(prefix+"contacts", new com.openexchange.ajax.Contact(), null, null);
        // http.registerServlet(prefix+"mail", new com.openexchange.ajax.Mail(), null, null);

        final String prefix = getService(DispatcherPrefixService.class).getPrefix();
        http.registerServlet(prefix + "mail.attachment", new com.openexchange.ajax.MailAttachment(), null, null);
        // http.registerServlet(prefix+"calendar", new com.openexchange.ajax.Appointment(), null, null);
        // http.registerServlet(prefix+"config", new com.openexchange.ajax.ConfigMenu(), null, null);
        // http.registerServlet(prefix+"attachment", new com.openexchange.ajax.Attachment(), null, null);
        // http.registerServlet(prefix+"reminder", new com.openexchange.ajax.Reminder(), null, null);
        // http.registerServlet(prefix+"group", new com.openexchange.ajax.Group(), null, null);
        // http.registerServlet(prefix+"resource", new com.openexchange.ajax.Resource(), null, null);
        http.registerServlet(prefix + "multiple", new com.openexchange.ajax.Multiple(), null, null);
        // http.registerServlet(prefix+"quota", new com.openexchange.ajax.Quota(), null, null);
        http.registerServlet(prefix + "control", new com.openexchange.ajax.ConfigJump(), null, null);
        // http.registerServlet(prefix+"file", new com.openexchange.ajax.AJAXFile(), null, null);
        // http.registerServlet(prefix+"import", new com.openexchange.ajax.ImportServlet(), null, null);
        // http.registerServlet(prefix+"export", new com.openexchange.ajax.ExportServlet(), null, null);
        // http.registerServlet(prefix+"image", new com.openexchange.image.servlet.ImageServlet(), null, null);
        http.registerServlet(prefix + "sync", new com.openexchange.ajax.SyncServlet(), null, null);
    }

}

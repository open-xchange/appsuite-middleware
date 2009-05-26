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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.Infostore;
import com.openexchange.ajax.requesthandler.AJAXRequestHandler;
import com.openexchange.api2.ContactSQLFactory;
import com.openexchange.api2.RdbContactSQLFactory;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.caching.CacheService;
import com.openexchange.charset.CustomCharsetProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configjump.ConfigJumpService;
import com.openexchange.configjump.client.ConfigJump;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataSource;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.osgi.Activator;
import com.openexchange.dataretention.DataRetentionService;
import com.openexchange.event.EventFactoryService;
import com.openexchange.event.impl.EventFactoryServiceImpl;
import com.openexchange.event.impl.EventQueue;
import com.openexchange.event.impl.osgi.EventHandlerRegistration;
import com.openexchange.event.impl.osgi.OSGiEventDispatcher;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.filemanagement.ManagedFileException;
import com.openexchange.filemanagement.ManagedFileExceptionFactory;
import com.openexchange.folder.FolderService;
import com.openexchange.folder.internal.FolderServiceImpl;
import com.openexchange.group.GroupService;
import com.openexchange.group.internal.GroupServiceImpl;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarAdministrationService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.datahandler.ContactInsertDataHandler;
import com.openexchange.groupware.contact.datasource.ContactDataSource;
import com.openexchange.groupware.datahandler.ICalInsertDataHandler;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.reminder.ReminderDeleteInterface;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.osgi.WhiteboardDBProvider;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskServiceTrackerCustomizer;
import com.openexchange.i18n.I18nTools;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.mail.MailLoginHandler;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.cache.MailAccessCacheEventListener;
import com.openexchange.mail.conversion.ICalMailPartDataSource;
import com.openexchange.mail.conversion.VCardAttachMailDataHandler;
import com.openexchange.mail.conversion.VCardMailPartDataSource;
import com.openexchange.mail.osgi.MailProviderServiceTracker;
import com.openexchange.mail.osgi.TransportProviderServiceTracker;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.service.impl.MailServiceImpl;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountExceptionFactory;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.mailaccount.internal.CreateMailAccountTables;
import com.openexchange.management.ManagementService;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.multiple.internal.MultipleHandlerServiceTracker;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.internal.ResourceServiceImpl;
import com.openexchange.search.SearchException;
import com.openexchange.search.SearchExceptionFactory;
import com.openexchange.search.SearchService;
import com.openexchange.search.internal.SearchServiceImpl;
import com.openexchange.server.impl.Starter;
import com.openexchange.server.impl.Version;
import com.openexchange.server.osgiservice.BundleServiceTracker;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.WhiteboardFactoryService;
import com.openexchange.server.services.ServerRequestHandlerRegistry;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.osgi.SpamHandlerServiceTracker;
import com.openexchange.systemname.SystemNameService;
import com.openexchange.systemname.internal.JVMRouteSystemNameImpl;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.servlet.http.osgi.HttpServiceImpl;
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

    private static final Log LOG = LogFactory.getLog(ServerActivator.class);

    /**
     * Bundle ID of admin.<br>
     * TODO: Maybe this should be read by config.ini
     */
    private static final String BUNDLE_ID_ADMIN = "com.openexchange.admin";

    /**
     * Constant for string: "identifier"
     */
    private static final String STR_IDENTIFIER = "identifier";

    private static final Class<?>[] NEEDED_SERVICES_ADMIN = {
        ConfigurationService.class, CacheService.class, EventAdmin.class, TimerService.class, CalendarAdministrationService.class };

    private static final Class<?>[] NEEDED_SERVICES_SERVER = {
        ConfigurationService.class, CacheService.class, EventAdmin.class, SessiondService.class, SpringParser.class, JDOMParser.class,
        TimerService.class, CalendarAdministrationService.class, AppointmentSqlFactoryService.class, CalendarCollectionService.class,
        ReminderDeleteInterface.class };

    private final List<ServiceRegistration> registrationList;

    private final List<ServiceTracker> serviceTrackerList;

    private final List<ComponentRegistration> componentRegistrationList;

    private final List<EventHandlerRegistration> eventHandlerList;

    private final Starter starter;

    private final AtomicBoolean started;

    private Boolean adminBundleInstalled;

    /**
     * Initializes a new {@link ServerActivator}
     */
    public ServerActivator() {
        super();
        this.started = new AtomicBoolean();
        this.starter = new Starter();
        registrationList = new ArrayList<ServiceRegistration>();
        serviceTrackerList = new ArrayList<ServiceTracker>();
        componentRegistrationList = new ArrayList<ComponentRegistration>();
        eventHandlerList = new ArrayList<EventHandlerRegistration>();
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
                } catch (final AbstractOXException e) {
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
                } catch (final AbstractOXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private BundleActivator databaseActivator;

    @Override
    protected void startBundle() throws Exception {
        // TODO remove the following line if database bundle is finished.
        (databaseActivator = new Activator()).start(context);
        // get version information from MANIFEST file
        final Dictionary<?, ?> headers = context.getBundle().getHeaders();
        Version.buildnumber = (String) headers.get("OXVersion") + " Rev" + (String) headers.get("OXRevision");
        Version.version = (String) headers.get("Bundle-Version");
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
        serviceTrackerList.add(new ServiceTracker(context, ConfigurationService.class.getName(), new ConfigurationCustomizer(context)));
        // move this to the required services once the database component gets into its own bundle.
        serviceTrackerList.add(new ServiceTracker(context, DatabaseService.class.getName(), new DatabaseCustomizer(context)));
        // I18n service load
        serviceTrackerList.add(new ServiceTracker(context, I18nTools.class.getName(), new I18nServiceListener(context)));
        // Update task service tracker
        serviceTrackerList.add(new ServiceTracker(context, UpdateTaskProviderService.class.getName(), new UpdateTaskServiceTrackerCustomizer(context)));

        // Mail account delete listener
        serviceTrackerList.add(new ServiceTracker(
            context,
            com.openexchange.mailaccount.MailAccountDeleteListener.class.getName(),
            new com.openexchange.mailaccount.internal.DeleteListenerServiceTracker(context)));

        // Mail provider service tracker
        serviceTrackerList.add(new ServiceTracker(context, MailProvider.class.getName(), new MailProviderServiceTracker(context)));

        // Transport provider service tracker
        serviceTrackerList.add(new ServiceTracker(context, TransportProvider.class.getName(), new TransportProviderServiceTracker(context)));

        // Spam handler provider service tracker
        serviceTrackerList.add(new ServiceTracker(context, SpamHandler.class.getName(), new SpamHandlerServiceTracker(context)));

        // AJAX request handler
        serviceTrackerList.add(new ServiceTracker(context, AJAXRequestHandler.class.getName(), new AJAXRequestHandlerCustomizer(context)));

        // contacts
        serviceTrackerList.add(new ServiceTracker(context, ContactInterface.class.getName(), new ContactServiceListener(context)));
        // ICal Parser
        serviceTrackerList.add(new ServiceTracker(context, ICalParser.class.getName(), new RegistryCustomizer<ICalParser>(
            context,
            ICalParser.class)));

        // ICal Emitter
        serviceTrackerList.add(new ServiceTracker(context, ICalEmitter.class.getName(), new RegistryCustomizer<ICalEmitter>(
            context,
            ICalEmitter.class)));

        // Data Retention Service
        serviceTrackerList.add(new ServiceTracker(
            context,
            DataRetentionService.class.getName(),
            new RegistryCustomizer<DataRetentionService>(context, DataRetentionService.class)));

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
            serviceTrackerList.add(new ServiceTracker(context, ManagementService.class.getName(), new ManagementServiceTracker(context)));
            // TODO:
            /*-
             * serviceTrackerList.add(new ServiceTracker(context, MonitorService.class.getName(),
             *     new BundleServiceTracker&lt;MonitorService&gt;(context, MonitorService.getInstance(), MonitorService.class)));
             */

            // Search for AuthenticationService
            serviceTrackerList.add(new ServiceTracker(context, AuthenticationService.class.getName(), new AuthenticationCustomizer(context)));
            // Search for ConfigJumpService
            serviceTrackerList.add(new ServiceTracker(
                context,
                ConfigJumpService.class.getName(),
                new BundleServiceTracker<ConfigJumpService>(context, ConfigJump.getHolder(), ConfigJumpService.class)));
            // Search for extensions of the preferences tree interface
            serviceTrackerList.add(new ServiceTracker(context, PreferencesItemService.class.getName(), new PreferencesCustomizer(context)));
            // Search for UserPasswordChange service
            serviceTrackerList.add(new ServiceTracker(context, PasswordChangeService.class.getName(), new PasswordChangeCustomizer(context)));
            // Search for host name service
            serviceTrackerList.add(new ServiceTracker(context, HostnameService.class.getName(), new HostnameServiceCustomizer(context)));
            // Conversion service
            serviceTrackerList.add(new ServiceTracker(
                context,
                ConversionService.class.getName(),
                new RegistryCustomizer<ConversionService>(context, ConversionService.class)));
            // Contact collector
            serviceTrackerList.add(new ServiceTracker(
                context,
                ContactCollectorService.class.getName(),
                new RegistryCustomizer<ContactCollectorService>(context, ContactCollectorService.class)));
            // Search Service
            serviceTrackerList.add(new ServiceTracker(context, SearchService.class.getName(), new RegistryCustomizer<SearchService>(
                context,
                SearchService.class)));
            // Login handler
            serviceTrackerList.add(new ServiceTracker(context, LoginHandlerService.class.getName(), new LoginHandlerCustomizer(context)));
            // Multiple handler factory services
            serviceTrackerList.add(new ServiceTracker(
                context,
                MultipleHandlerFactoryService.class.getName(),
                new MultipleHandlerServiceTracker(context)));

            // Start up server the usual way
            starter.start();
        }
        // Open service trackers
        for (final ServiceTracker tracker : serviceTrackerList) {
            tracker.open();
        }
        // Register server's services
        registrationList.add(context.registerService(CharsetProvider.class.getName(), new CustomCharsetProvider(), null));
        registrationList.add(context.registerService(HttpService.class.getName(), new HttpServiceImpl(), null));
        registrationList.add(context.registerService(GroupService.class.getName(), new GroupServiceImpl(), null));
        registrationList.add(context.registerService(ResourceService.class.getName(), ResourceServiceImpl.getInstance(), null));
        registrationList.add(context.registerService(UserService.class.getName(), ServerServiceRegistry.getInstance().getService(
            UserService.class,
            true), null));
        registrationList.add(context.registerService(UserConfigurationService.class.getName(), new UserConfigurationServiceImpl(), null));
        registrationList.add(context.registerService(ContextService.class.getName(), ServerServiceRegistry.getInstance().getService(
            ContextService.class,
            true), null));
        registrationList.add(context.registerService(SystemNameService.class.getName(), new JVMRouteSystemNameImpl(), null));
        registrationList.add(context.registerService(MailService.class.getName(), new MailServiceImpl(), null));
        // TODO: Register search service here until its encapsulated in an own bundle
        registrationList.add(context.registerService(SearchService.class.getName(), new SearchServiceImpl(), null));
        // TODO: Register server's login handler here until its encapsulated in an own bundle
        registrationList.add(context.registerService(LoginHandlerService.class.getName(), new MailLoginHandler(), null));
        // Register table creation for mail account storage.
        registrationList.add(context.registerService(CreateTableService.class.getName(), new CreateMailAccountTables(), null));
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
        /*
         * Register data sources
         */
        {
            final Dictionary<Object, Object> props = new Hashtable<Object, Object>();
            props.put(STR_IDENTIFIER, "com.openexchange.mail.vcard");
            registrationList.add(context.registerService(DataSource.class.getName(), new VCardMailPartDataSource(), props));
        }
        {
            final Dictionary<Object, Object> props = new Hashtable<Object, Object>();
            props.put(STR_IDENTIFIER, "com.openexchange.mail.ical");
            registrationList.add(context.registerService(DataSource.class.getName(), new ICalMailPartDataSource(), props));
        }
        {
            final Dictionary<Object, Object> props = new Hashtable<Object, Object>();
            props.put(STR_IDENTIFIER, "com.openexchange.contact");
            registrationList.add(context.registerService(DataSource.class.getName(), new ContactDataSource(), props));
        }
        /*
         * Register data handlers
         */
        {
            final Dictionary<Object, Object> props = new Hashtable<Object, Object>();
            props.put(STR_IDENTIFIER, "com.openexchange.contact");
            registrationList.add(context.registerService(DataHandler.class.getName(), new ContactInsertDataHandler(), props));
        }
        {
            final Dictionary<Object, Object> props = new Hashtable<Object, Object>();
            props.put(STR_IDENTIFIER, "com.openexchange.ical");
            registrationList.add(context.registerService(DataHandler.class.getName(), new ICalInsertDataHandler(), props));
        }
        {
            final Dictionary<Object, Object> props = new Hashtable<Object, Object>();
            props.put(STR_IDENTIFIER, "com.openexchange.mail.vcard");
            registrationList.add(context.registerService(DataHandler.class.getName(), new VCardAttachMailDataHandler(), props));
        }

        // Register DBProvider

        registrationList.add(context.registerService(DBProvider.class.getName(), new DBPoolProvider(), null));
        registrationList.add(context.registerService(WhiteboardFactoryService.class.getName(), new WhiteboardDBProvider.Factory(), null));

        // Register Infostore

        registrationList.add(context.registerService(InfostoreFacade.class.getName(), Infostore.FACADE, null));

        // Register ContactSQL

        registrationList.add(context.registerService(ContactSQLFactory.class.getName(), new RdbContactSQLFactory(), null));

        // Register event factory service
        registrationList.add(context.registerService(EventFactoryService.class.getName(), new EventFactoryServiceImpl(), null));

        // Register folder service
        registrationList.add(context.registerService(FolderService.class.getName(), new FolderServiceImpl(), null));

        /*
         * Register components
         */
        // TODO: Decide what to register dependent on admin/groupware start
        componentRegistrationList.add(new ComponentRegistration(
            context,
            SearchException.SEARCH_COMPONENT,
            "com.openexchange.search",
            SearchExceptionFactory.getInstance()));
        componentRegistrationList.add(new ComponentRegistration(
            context,
            ManagedFileException.MANAGED_FILE_COMPONENT,
            "com.openexchange.filemanagement",
            ManagedFileExceptionFactory.getInstance()));
        componentRegistrationList.add(new ComponentRegistration(
            context,
            MailAccountException.MAIL_ACCOUNT_COMPONENT,
            "com.openexchange.mailaccount",
            MailAccountExceptionFactory.getInstance()));
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.server");
        try {
            /*
             * Unregister components
             */
            for (final ComponentRegistration componentRegistration : componentRegistrationList) {
                componentRegistration.unregister();
            }
            componentRegistrationList.clear();
            /*
             * Unregister server's services
             */
            for (final ServiceRegistration registration : registrationList) {
                registration.unregister();
            }
            registrationList.clear();
            /*
             * Close service trackers
             */
            for (final ServiceTracker tracker : serviceTrackerList) {
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
        } finally {
            started.set(false);
            adminBundleInstalled = null;
        }
        // TODO remove this lines if database bundle is finished.
        databaseActivator.stop(context);
        databaseActivator = null;
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

}

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

package com.openexchange.folderstorage.outlook.osgi;

import static com.openexchange.folderstorage.outlook.OutlookServiceRegistry.getServiceRegistry;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.FolderEventConstants;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.folderstorage.outlook.OutlookServiceRegistry;
import com.openexchange.folderstorage.outlook.memory.MemoryTable;
import com.openexchange.folderstorage.outlook.sql.Update;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.push.PushEventConstants;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link OutlookFolderStorageActivator} - {@link BundleActivator Activator} for MS outlook folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OutlookFolderStorageActivator extends DeferredActivator {

    static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(OutlookFolderStorageActivator.class));

    private List<ServiceRegistration<?>> serviceRegistrations;

    private List<ServiceTracker<?,?>> serviceTrackers;

    /**
     * Initializes a new {@link OutlookFolderStorageActivator}.
     */
    public OutlookFolderStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            DatabaseService.class, MailAccountStorageService.class, ThreadPoolService.class, MessagingServiceRegistry.class,
            UnifiedInboxManagement.class, ConfigurationService.class, FileStorageServiceRegistry.class, SessiondService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        getServiceRegistry().addService(clazz, getService(clazz));

    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (final Class<?> classe : classes) {
                    final Object service = getService(classe);
                    if (null != service) {
                        registry.addService(classe, service);
                    }
                }
            }
            // Trackers
            serviceTrackers = new ArrayList<ServiceTracker<?,?>>(1);
            serviceTrackers.add(new ServiceTracker<FolderStorage,FolderStorage>(context, FolderStorage.class, new OutlookFolderStorageServiceTracker(context)));
            for (final ServiceTracker<?,?> serviceTracker : serviceTrackers) {
                serviceTracker.open();
            }

            // Register services
            serviceRegistrations = new ArrayList<ServiceRegistration<?>>(2);
            // DeleteListener was added statically
            // serviceRegistrations.add(context.registerService(DeleteListener.class.getName(), new OutlookFolderDeleteListener(), null));

            serviceRegistrations.add(context.registerService(MailAccountDeleteListener.class, new MailAccountDeleteListener() {

                @Override
                public void onBeforeMailAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
                    OutlookFolderStorage.clearTCM();
                }

                @Override
                public void onAfterMailAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
                    // Nothing todo
                }
            }, null));

            final Dictionary<String, String> dictionary = new Hashtable<String, String>(1);
            dictionary.put("tree", OutlookFolderStorage.OUTLOOK_TREE_ID);
            serviceRegistrations.add(context.registerService(FolderStorage.class, OutlookFolderStorage.getInstance(), dictionary));
            {
                final EventHandler pushMailEventHandler = new EventHandler() {

                    @Override
                    public void handleEvent(final Event event) {
                        // final Session session = ((Session) event.getProperty(PushEventConstants.PROPERTY_SESSION));
                        // final String folderId = (String) event.getProperty(PushEventConstants.PROPERTY_FOLDER);
                        // final Boolean contentRelated = (Boolean) event.getProperty(PushEventConstants.PROPERTY_CONTENT_RELATED);
                        OutlookFolderStorage.clearTCM();
                    }
                };
                final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
                dict.put(EventConstants.EVENT_TOPIC, PushEventConstants.getAllTopics());
                serviceRegistrations.add(context.registerService(EventHandler.class, pushMailEventHandler, dict));
            }
            {
                final EventHandler folderEventHandler = new EventHandler() {

                    private final int tree = Integer.parseInt(OutlookFolderStorage.OUTLOOK_TREE_ID);

                    @Override
                    public void handleEvent(final Event event) {
                        OutlookFolderStorage.clearTCM();
                        if (FolderEventConstants.TOPIC_IDENTIFIERS.equals(event.getTopic())) {
                            final Session session = ((Session) event.getProperty(FolderEventConstants.PROPERTY_SESSION));
                            final String newId = (String) event.getProperty(FolderEventConstants.PROPERTY_NEW_IDENTIFIER);
                            final String oldId = (String) event.getProperty(FolderEventConstants.PROPERTY_OLD_IDENTIFIER);
                            final String delim = (String) event.getProperty(FolderEventConstants.PROPERTY_DELIMITER);

                            try {
                                Update.updateIds(session.getContextId(), tree, session.getUserId(), newId, oldId, delim);
                            } catch (final Exception e) {
                                LOG.error(e.getMessage(), e);
                            }

                            final MemoryTable memoryTable = MemoryTable.optMemoryTableFor(session);
                            if (null != memoryTable) {
                                try {
                                    memoryTable.initializeTree(tree, session.getUserId(), session.getContextId());
                                } catch (final Exception e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                        }
                    }
                };
                final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
                dict.put(EventConstants.EVENT_TOPIC, FolderEventConstants.getAllTopics());
                serviceRegistrations.add(context.registerService(EventHandler.class.getName(), folderEventHandler, dict));
            }
            {
                final EventHandler sessionEventHandler = new EventHandler() {

                    @Override
                    public void handleEvent(final Event event) {
                        final String topic = event.getTopic();
                        if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic)) {
                            @SuppressWarnings("unchecked") final Map<String, Session> container = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                            for (final Session session : container.values()) {
                                dropMemoryTable(session);
                            }
                        } else if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                            dropMemoryTable((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
                        } else if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                            @SuppressWarnings("unchecked") final Map<String, Session> container = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                            for (final Session session : container.values()) {
                                dropMemoryTable(session);
                            }
                        }
                    }

                    private void dropMemoryTable(final Session session) {
                        if (session.isTransient()) {
                            return;
                        }
                        /*
                         * Any active session left?
                         */
                        final SessiondService service = OutlookServiceRegistry.getServiceRegistry().getService(SessiondService.class);
                        if (null == service.getAnyActiveSessionForUser(session.getUserId(), session.getContextId())) {
                            MemoryTable.dropMemoryTableFrom(session);
                        }
                    }

                };
                final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
                dict.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
                serviceRegistrations.add(context.registerService(EventHandler.class.getName(), sessionEventHandler, dict));
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            /*
             * Drop service registrations
             */
            if (null != serviceRegistrations) {
                for (final ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
                    serviceRegistration.unregister();
                }
                serviceRegistrations.clear();
                serviceRegistrations = null;
            }
            /*
             * Drop/close service trackers
             */
            if (null != serviceTrackers) {
                for (final ServiceTracker<?,?> serviceTracker : serviceTrackers) {
                    serviceTracker.close();
                }
                serviceTrackers.clear();
                serviceTrackers = null;
            }
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

}

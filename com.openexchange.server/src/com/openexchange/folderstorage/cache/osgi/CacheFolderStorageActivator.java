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

package com.openexchange.folderstorage.cache.osgi;

import static com.openexchange.folderstorage.cache.CacheServiceRegistry.getServiceRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.folderstorage.cache.lock.UserLockManagement;
import com.openexchange.folderstorage.cache.memory.FolderMapManagement;
import com.openexchange.folderstorage.cache.service.FolderCacheInvalidationService;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CacheFolderStorageActivator} - {@link BundleActivator Activator} for cache folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheFolderStorageActivator extends DeferredActivator {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CacheFolderStorageActivator.class);

    private volatile CacheFolderStorage cacheFolderStorage;
    private volatile List<ServiceRegistration<?>> registrations;
    private volatile List<ServiceTracker<?,?>> serviceTrackers;

    /**
     * Initializes a new {@link CacheFolderStorageActivator}.
     */
    public CacheFolderStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { CacheService.class, ThreadPoolService.class, ConfigurationService.class, SessiondService.class,
            MailAccountStorageService.class, CacheEventService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        getServiceRegistry().addService(clazz, getService(clazz));
        if (CacheService.class.equals(clazz)) {
            try {
                initCacheFolderStorage();
            } catch (final OXException e) {
                LOG.error("", e);
                unregisterCacheFolderStorage();
            }
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
        if (CacheService.class.equals(clazz)) {
            try {
                disposeCacheFolderStorage();
            } catch (final OXException e) {
                LOG.error("", e);
                unregisterCacheFolderStorage();
            }
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected synchronized void startBundle() throws Exception {
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
            initCacheFolderStorage();
            CacheFolderStorageInvalidator invalidator = new CacheFolderStorageInvalidator(context);
            registrations.add(context.registerService(MailAccountDeleteListener.class, invalidator, null));
            // Register service trackers
            List<ServiceTracker<?,?>> serviceTrackers = new ArrayList<ServiceTracker<?,?>>(4);
            this.serviceTrackers = serviceTrackers;
            serviceTrackers.add(new ServiceTracker<FolderStorage,FolderStorage>(context, FolderStorage.class, new CacheFolderStorageServiceTracker(context)));
            serviceTrackers.add(new ServiceTracker<CacheEventService, CacheEventService>(context, CacheEventService.class, invalidator));
            serviceTrackers.add(new ServiceTracker<CacheEventService, CacheEventService>(context, CacheEventService.class, new FolderMapInvalidator(context)));
            for (final ServiceTracker<?,?> serviceTracker : serviceTrackers) {
                serviceTracker.open();
            }
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        try {
            // Drop service trackers
            List<ServiceTracker<?, ?>> serviceTrackers = this.serviceTrackers;
            if (null != serviceTrackers) {
                for (final ServiceTracker<?,?> serviceTracker : serviceTrackers) {
                    serviceTracker.close();
                }
                serviceTrackers.clear();
                this.serviceTrackers = null;
            }
            disposeCacheFolderStorage();
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    private void disposeCacheFolderStorage() throws OXException {
        // Unregister folder storage
        unregisterCacheFolderStorage();
        // Shut-down folder storage
        CacheFolderStorage cacheFolderStorage = this.cacheFolderStorage;
        if (null != cacheFolderStorage) {
            cacheFolderStorage.onCacheAbsent();
            this.cacheFolderStorage = null;
        }
    }

    private void initCacheFolderStorage() throws OXException {
        // Start-up folder storage
        final CacheFolderStorage cache = CacheFolderStorage.getInstance();
        cacheFolderStorage = cache;
        cache.onCacheAvailable();
        // Register folder storage
        List<ServiceRegistration<?>> registrations = new ArrayList<ServiceRegistration<?>>(4);
        this.registrations = registrations;
        {
            final Dictionary<String, String> dictionary = new Hashtable<String, String>(1);
            dictionary.put("tree", FolderStorage.ALL_TREE_ID);
            registrations.add(context.registerService(FolderStorage.class, cache, dictionary));
            registrations.add(context.registerService(FolderCacheInvalidationService.class, cache, null));
        }
        {
            final EventHandler eventHandler = new EventHandler() {

                @Override
                public void handleEvent(final Event event) {
                    final ThreadPoolService threadPool = getService(ThreadPoolService.class);
                    if (null == threadPool) {
                        doHandleEvent(event);
                    } else {
                        final AbstractTask<Void> t = new AbstractTask<Void>() {

                            @Override
                            public Void call() throws Exception {
                                try {
                                    doHandleEvent(event);
                                } catch (final Exception e) {
                                    LOG.warn("Handling event {} failed.", event.getTopic(), e);
                                }
                                return null;
                            }
                        };
                        threadPool.submit(t, CallerRunsBehavior.<Void> getInstance());
                    }
                }

                /**
                 * Handles given event.
                 *
                 * @param event The event
                 */
                protected void doHandleEvent(final Event event) {
                    final String topic = event.getTopic();
                    if (SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
                        Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                        if (null != contextId) {
                            Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
                            if (null != userId) {
                                FolderMapManagement.getInstance().dropFor(userId.intValue(), contextId.intValue());
                                UserLockManagement.getInstance().dropFor(userId, contextId);
                            }
                        }
                    } else if (SessiondEventConstants.TOPIC_LAST_SESSION_CONTEXT.equals(topic)) {
                        Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                        if (null != contextId) {
                            FolderMapManagement.getInstance().dropFor(contextId.intValue());
                            UserLockManagement.getInstance().dropFor(contextId.intValue());
                        }
                    }
                }
            };
            final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
            dict.put(EventConstants.EVENT_TOPIC, new String[] { SessiondEventConstants.TOPIC_LAST_SESSION, SessiondEventConstants.TOPIC_LAST_SESSION_CONTEXT });
            registrations.add(context.registerService(EventHandler.class, eventHandler, dict));
        }
        {
            /*
             * Attach handler for file storage folder events
             */
            final EventHandler eventHandler = new EventHandler() {

                @Override
                public void handleEvent(final Event event) {
                    final ThreadPoolService threadPool = getService(ThreadPoolService.class);
                    if (null == threadPool) {
                        doHandleEvent(cache, event);
                    } else {
                        final AbstractTask<Void> t = new AbstractTask<Void>() {

                            @Override
                            public Void call() throws Exception {
                                try {
                                    doHandleEvent(cache, event);
                                } catch (final Exception e) {
                                    LOG.warn("Handling event {} failed.", event.getTopic(), e);
                                }
                                return null;
                            }
                        };
                        threadPool.submit(t, CallerRunsBehavior.<Void> getInstance());
                    }
                }

                /**
                 * Handles given event.
                 *
                 * @param event The event
                 */
                protected void doHandleEvent(final CacheFolderStorage tmp, final Event event) {
                    try {
                        final String folderID = (String)event.getProperty(FileStorageEventConstants.FOLDER_ID);
                        final ServerSession session = ServerSessionAdapter.valueOf((Session)event.getProperty(FileStorageEventConstants.SESSION));
                        final String[] folderPath = (String[]) event.getProperty(FileStorageEventConstants.FOLDER_PATH);
                        tmp.removeFromGlobalCache(folderID, FolderStorage.REAL_TREE_ID, session.getContextId());
                        if (null != folderPath) {
                            tmp.removeFromCache(folderID, FolderStorage.REAL_TREE_ID, false, session.getUser(), session.getContext(), session, Arrays.asList(folderPath));
                        } else {
                            tmp.removeFromCache(folderID, FolderStorage.REAL_TREE_ID, false, session.getUser(), session.getContext(), session);
                        }
                    } catch (final OXException e) {
                        LOG.error("", e);
                    }
                }
            };
            final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
            dict.put(EventConstants.EVENT_TOPIC, FileStorageEventConstants.ALL_FOLDER_TOPICS);
            registrations.add(context.registerService(EventHandler.class, eventHandler, dict));
        }
    }

    private static final String DEFAULT = MailFolder.DEFAULT_FOLDER_ID;

    private static final Pattern PAT_FIX = Pattern.compile(Pattern.quote(DEFAULT) + "([0-9]+)" + Pattern.quote(DEFAULT));

    protected static String sanitizeFolderId(final String id) {
        String fid = id;
        if (fid.startsWith(DEFAULT)) {
            try {
                final Matcher matcher = PAT_FIX.matcher(fid);
                if (matcher.matches()) {
                    fid = DEFAULT + matcher.group(1);
                }
            } catch (final Exception e) {
                LOG.warn("Couldn't sanitize folder identifier: {}. Returning unchanged.", id, e);
                return id;
            }
        }
        return fid;
    }

    private void unregisterCacheFolderStorage() {
        // Unregister
        List<ServiceRegistration<?>> registrations = this.registrations;
        if (null != registrations) {
            while (!registrations.isEmpty()) {
                registrations.remove(0).unregister();
            }
            this.registrations = null;
        }
    }

}

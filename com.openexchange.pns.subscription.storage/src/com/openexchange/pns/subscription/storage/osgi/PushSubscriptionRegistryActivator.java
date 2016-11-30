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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns.subscription.storage.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.subscription.storage.CompositePushSubscriptionRegistry;
import com.openexchange.pns.subscription.storage.groupware.CreatePnsSubscriptionTable;
import com.openexchange.pns.subscription.storage.groupware.PnsCreateTableTask;
import com.openexchange.pns.subscription.storage.groupware.PnsDeleteListener;
import com.openexchange.pns.subscription.storage.inmemory.InMemoryPushSubscriptionRegistry;
import com.openexchange.pns.subscription.storage.rdb.RdbPushSubscriptionRegistry;
import com.openexchange.pns.subscription.storage.rdb.cache.RdbPushSubscriptionRegistryCache;
import com.openexchange.pns.subscription.storage.rdb.cache.RdbPushSubscriptionRegistryInvalidator;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;


/**
 * {@link PushSubscriptionRegistryActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PushSubscriptionRegistryActivator extends HousekeepingActivator {

    private RdbPushSubscriptionRegistryCache cache;

    /**
     * Initializes a new {@link PushSubscriptionRegistryActivator}.
     */
    public PushSubscriptionRegistryActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ContextService.class, CacheService.class, CacheEventService.class, ThreadPoolService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(PushSubscriptionRegistryActivator.class);

        // Create database-backed registry
        RdbPushSubscriptionRegistry persistentRegistry = new RdbPushSubscriptionRegistry(getService(DatabaseService.class), getService(ContextService.class));

        // Create cache instance
        final RdbPushSubscriptionRegistryCache cache = new RdbPushSubscriptionRegistryCache(persistentRegistry, getService(CacheEventService.class), getService(CacheService.class));
        this.cache = cache;
        persistentRegistry.setCache(cache);

        // Track subscription listeners
        PushSubscriptionListenerTracker listenerTracker = new PushSubscriptionListenerTracker(context);
        rememberTracker(listenerTracker);

        // Track subscription providers
        PushSubscriptionProviderTracker providerTracker = new PushSubscriptionProviderTracker(listenerTracker, context);
        rememberTracker(providerTracker);
        track(CacheEventService.class, new RdbPushSubscriptionRegistryInvalidator(cache, context));
        openTrackers();

        // Register update task, create table job and delete listener
        boolean registerGroupwareStuff = true;
        if (registerGroupwareStuff) {
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new PnsCreateTableTask(this)));
            registerService(CreateTableService.class, new CreatePnsSubscriptionTable());
            registerService(DeleteListener.class, new PnsDeleteListener(persistentRegistry));
        }

        // Register service
        PushSubscriptionRegistry volatileRegistry = new InMemoryPushSubscriptionRegistry();
        registerService(PushSubscriptionRegistry.class, new CompositePushSubscriptionRegistry(persistentRegistry, volatileRegistry, providerTracker, listenerTracker, false));

        // Register event handler
        {
            EventHandler eventHandler = new EventHandler() {

                @Override
                public void handleEvent(final Event event) {
                    if (false == SessiondEventConstants.TOPIC_LAST_SESSION.equals(event.getTopic())) {
                        return;
                    }

                    ThreadPoolService threadPool = getService(ThreadPoolService.class);
                    if (null == threadPool) {
                        doHandleEvent(event);
                    } else {
                        AbstractTask<Void> t = new AbstractTask<Void>() {

                            @Override
                            public Void call() throws Exception {
                                try {
                                    doHandleEvent(event);
                                } catch (Exception e) {
                                    logger.warn("Handling event {} failed.", event.getTopic(), e);
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
                 * @param lastSessionEvent The event
                 */
                protected void doHandleEvent(Event lastSessionEvent) {
                    Integer contextId = (Integer) lastSessionEvent.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                    if (null != contextId) {
                        Integer userId = (Integer) lastSessionEvent.getProperty(SessiondEventConstants.PROP_USER_ID);
                        if (null != userId) {
                            cache.dropFor(userId.intValue(), contextId.intValue(), false);
                        }
                    }
                }
            };

            Dictionary<String, Object> serviceProperties = new Hashtable<>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
            registerService(EventHandler.class, eventHandler, serviceProperties);
        }

        logger.info("Bundle {} successfully started", context.getBundle().getSymbolicName());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PushSubscriptionRegistryActivator.class);

        RdbPushSubscriptionRegistryCache cache = this.cache;
        if (null != cache) {
            this.cache = null;
            cache.clear(false);
        }

        super.stopBundle();

        logger.info("Bundle {} successfully stopped", context.getBundle().getSymbolicName());
    }

}

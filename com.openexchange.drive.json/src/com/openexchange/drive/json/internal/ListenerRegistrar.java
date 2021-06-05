/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.drive.json.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.DriveUtility;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventImpl;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.drive.json.LongPollingListener;
import com.openexchange.drive.json.LongPollingListenerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link ListenerRegistrar}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ListenerRegistrar implements DriveEventPublisher, EventHandler {

    /**
     * Gets the {@link ListenerRegistrar} instance.
     *
     * @return The instance.
     */
    public static ListenerRegistrar getInstance() {
        return INSTANCE;
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ListenerRegistrar.class);
    private static final int EXPIRY_TIME = 300;
    private static final ListenerRegistrar INSTANCE = new ListenerRegistrar();

    /** Maps contextID:rootFolderID to listenerID[]  */
    private final ListMultimap<String, String> listenersPerFolder;

    /** Maps sessionID to listenerID[] */
    private final ListMultimap<String, String> listenersPerSession;

    /** Maps listenerID to listener */
    private final Cache<String, LongPollingListener> listeners;

    private final SortedSet<LongPollingListenerFactory> listenerFactories;

    private ListenerRegistrar() {
        super();
        this.listenersPerFolder = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, String> create(1024, 4));
        this.listenersPerSession = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, String> create(1024, 2));
        this.listeners = CacheBuilder.newBuilder().expireAfterAccess(EXPIRY_TIME, TimeUnit.SECONDS)
            .removalListener(notification -> {
                /*
                 * remove from secondary mappings, too
                 */
                LongPollingListener listener = LongPollingListener.class.cast(notification.getValue());
                listenersPerSession.removeAll(listener.getSession().getServerSession().getSessionID());
                int contextID = listener.getSession().getServerSession().getContextId();
                for (String rootFolderID : listener.getRootFolderIDs()) {
                    listenersPerFolder.remove(getFolderKey(rootFolderID, contextID), notification.getKey());
                }
                LOG.debug("Unregistered listener: {}", listener);
            })
            .build();
        this.listenerFactories = new TreeSet<LongPollingListenerFactory>(LongPollingListenerFactory.PRIORITY_COMPARATOR);
    }

    /**
     * Gets the long polling listener for the supplied session, creating one if not yet present.
     *
     * @param session The session
     * @param rootFolderIDs The root folder IDs to listen for changes in
     * @param mode The subscription mode
     * @return The listener
     */
    public LongPollingListener getOrCreate(final DriveSession session, final List<String> rootFolderIDs, SubscriptionMode mode) throws ExecutionException {
        final String listenerID = getListenerID(session, rootFolderIDs);
        final int contextID = session.getServerSession().getContextId();
        return listeners.get(listenerID, () -> {
            /*
             * create listener & remember in secondary mappings
             */
            LongPollingListener listener = createListener(session, rootFolderIDs, mode);
            for (String rootFolderID : rootFolderIDs) {
                listenersPerFolder.put(getFolderKey(rootFolderID, contextID), listenerID);
            }
            listenersPerSession.put(session.getServerSession().getSessionID(), listenerID);
            LOG.debug("Registered new listener: {}", listener);
            return listener;
        });
    }

    /**
     * Adds a listener factory.
     *
     * @param listenerFactory The listener factory to add
     * @return <code>true</code> if the factory was not yet known, <code>false</code>, otherwise
     */
    public boolean addFactory(LongPollingListenerFactory listenerFactory) {
        return listenerFactories.add(listenerFactory);
    }

    /**
     * Removes a listener factory.
     *
     * @param listenerFactory The listener factory to remove
     * @return <code>true</code> if a factory was removed, <code>false</code>, otherwise
     */
    public boolean removeFactory(LongPollingListenerFactory listenerFactory) {
        return listenerFactories.remove(listenerFactory);
    }

    @Override
    public boolean isLocalOnly() {
        return false;
    }

    @Override
    public void publish(DriveEvent event) {
        Set<String> listenerIDs = new HashSet<String>();
        for (String folderID : event.getFolderIDs()) {
            listenerIDs.addAll(listenersPerFolder.get(getFolderKey(folderID, event.getContextID())));
        }
        publish(event, listenerIDs);
    }

    @Override
    public void handleEvent(Event event) {
        try {
            DriveUtility driveUtility = Services.getService(DriveService.class, true).getUtility();
            List<String> driveSessionIds = new ArrayList<String>();
            if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(event.getTopic())) {
                Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                if (driveUtility.isDriveSession(session)) {
                    driveSessionIds.add(session.getSessionID());
                }
            } else if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(event.getTopic())) {
                for (Session session : ((Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER)).values()) {
                    if (driveUtility.isDriveSession(session)) {
                        driveSessionIds.add(session.getSessionID());
                    }
                }
            }
            if (0 < driveSessionIds.size()) {
                triggerSync(driveSessionIds);
            }
        } catch (Exception e) {
            LOG.warn("Unexpected error handling {}", event, e);
        }
    }

    /**
     * Creates and sends a synthetic drive event for the root folders of each registered listener associated with the supplied session
     * identifiers, usually provoking a synchronization of the client.
     *
     * @param sessionIds The identifiers of the sessions to trigger the sync for
     */
    public void triggerSync(List<String> sessionIds) {
        List<String> listenerIDs = new ArrayList<String>();
        if (null != sessionIds) {
            for (String sessionId : sessionIds) {
                listenerIDs.addAll(listenersPerSession.get(sessionId));
            }
        }
        if (0 < listenerIDs.size()) {
            for (LongPollingListener listener : listeners.getAllPresent(listenerIDs).values()) {
                LOG.trace("Triggering sync for long polling listener {}", listener);
                int contextId = listener.getSession().getServerSession().getContextId();
                listener.onEvent(new DriveEventImpl(contextId, new HashSet<String>(listener.getRootFolderIDs()), null, false, false, null));
            }
        }
    }

    private void publish(DriveEvent event, Collection<String> listenerIDs) {
        if (null == listenerIDs || listenerIDs.isEmpty()) {
            return;
        }
        String pushTokenReference = event.getPushTokenReference();
        for (LongPollingListener listener : listeners.getAllPresent(listenerIDs).values()) {
            if (null != pushTokenReference && listener.matches(pushTokenReference)) {
                // don't send back to originator
                LOG.trace("Skipping push notification for listener: {}", listener);
                continue;
            }
            listener.onEvent(event);
        }
    }

    private LongPollingListener createListener(DriveSession session, List<String> rootFolderIDs, SubscriptionMode mode) throws OXException {
        if (false == listenerFactories.isEmpty()) {
            LongPollingListenerFactory listenerFactory = listenerFactories.first();
            if (null != listenerFactory) {
                return listenerFactory.create(session, rootFolderIDs, mode);
            }
        }
        throw DriveExceptionCodes.LONG_POLLING_NOT_AVAILABLE.create(
            ServiceExceptionCode.SERVICE_UNAVAILABLE.create(LongPollingListenerFactory.class.getName()));
    }

    private static String getFolderKey(String folderID, int contextID) {
        return String.valueOf(contextID) + ':' + folderID;
    }

    private static String getListenerID(DriveSession session, List<String> rootFolderIDs) {
        StringBuilder stringBuilder = new StringBuilder()
            .append(session.getServerSession().getContextId()).append(':').append(session.getServerSession().getSessionID());
        for (String rootFolderID : rootFolderIDs) {
            stringBuilder.append(':').append(rootFolderID);
        }
        return stringBuilder.toString();
    }

}

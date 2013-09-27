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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.json.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.json.LongPollingListener;
import com.openexchange.drive.json.LongPollingListenerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link ListenerRegistrar}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ListenerRegistrar implements DriveEventPublisher  {

    /**
     * Gets the {@link ListenerRegistrar} instance.
     *
     * @return The instance.
     */
    public static ListenerRegistrar getInstance() {
        return INSTANCE;
    }

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ListenerRegistrar.class);
    private static final int EXPIRY_TIME = 300;
    private static final ListenerRegistrar INSTANCE = new ListenerRegistrar();

    /** Maps contextID:rootFolderID to sessionID[]  */
    private final ListMultimap<String, String> listenersPerFolder;

    /** Maps sessionID to listener */
    private final Cache<String, LongPollingListener> listeners;

    private final SortedSet<LongPollingListenerFactory> listenerFactories;

    private ListenerRegistrar() {
        super();
        ArrayListMultimap<String, String> multimap = ArrayListMultimap.create(1024, 4);
        this.listenersPerFolder = Multimaps.synchronizedListMultimap(multimap);
        this.listeners = CacheBuilder.newBuilder().expireAfterAccess(EXPIRY_TIME, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, LongPollingListener>() {

                @Override
                public void onRemoval(RemovalNotification<String, LongPollingListener> notification) {
                    /*
                     * remove from session id <=> root folder mapping, too
                     */
                    LongPollingListener listener = notification.getValue();
                    listenersPerFolder.remove(getFolderKey(listener.getSession()), notification.getKey());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unregistered listener: " + listener);
                    }
                }
            })
            .build();
        this.listenerFactories = new TreeSet<LongPollingListenerFactory>(LongPollingListenerFactory.PRIORITY_COMPARATOR);
    }

    /**
     * Gets the long polling listener for the supplied session, creating one if not yet present.
     *
     * @param session The session
     * @param rootFolderID The root folder ID that should be monitored
     * @return The listener
     * @throws ExecutionException
     */
    public LongPollingListener getOrCreate(final DriveSession session) throws ExecutionException {
        final String sessionID = session.getServerSession().getSessionID();
        return listeners.get(sessionID, new Callable<LongPollingListener>() {

            @Override
            public LongPollingListener call() throws Exception {
                LongPollingListener listener = createListener(session);
                listenersPerFolder.put(getFolderKey(session), sessionID);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Registered new listener: " + listener);
                }
                return listener;
            }
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
        Set<String> listenerSessionIDs = new HashSet<String>();
        for (String folderID : event.getFolderIDs()) {
            listenerSessionIDs.addAll(listenersPerFolder.get(getFolderKey(folderID, event.getContextID())));
        }
        for (LongPollingListener listener : listeners.getAllPresent(listenerSessionIDs).values()) {
            listener.onEvent(event);
        }
    }

    private LongPollingListener createListener(DriveSession session) throws OXException {
        if (false == listenerFactories.isEmpty()) {
            LongPollingListenerFactory listenerFactory = listenerFactories.first();
            if (null != listenerFactory) {
                return listenerFactory.create(session);
            }
        }
        throw DriveExceptionCodes.LONG_POLLING_NOT_AVAILABLE.create(
            ServiceExceptionCode.SERVICE_UNAVAILABLE.create(LongPollingListenerFactory.class.getName()));
    }

    private static String getFolderKey(String folderID, int contextID) {
        return String.valueOf(contextID) + ':' + folderID;
    }

    private static String getFolderKey(DriveSession session) {
        return getFolderKey(session.getRootFolderID(), session.getServerSession().getContextId());
    }

}

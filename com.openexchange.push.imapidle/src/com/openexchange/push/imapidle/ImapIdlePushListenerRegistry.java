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

package com.openexchange.push.imapidle;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushUtility;
import com.openexchange.push.imapidle.services.ImapIdleServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.Collections;

/**
 * {@link ImapIdlePushListenerRegistry} - The registry for IMAP IDLE {@link PushListener}s.
 */
public final class ImapIdlePushListenerRegistry {

    private static volatile Boolean DEBUG_ENABLED;
    
    /**
     * @param debugEnabled the debugEnabled to set
     */
    public static final void setDebugEnabled(final boolean debugEnabled) {
        DEBUG_ENABLED = Boolean.valueOf(debugEnabled);
    }

    private static boolean isDebugEnabled(final Log logger) {
        final Boolean debug = DEBUG_ENABLED;
        return null == debug ? logger.isDebugEnabled() : debug.booleanValue();
    }

    private static final ImapIdlePushListenerRegistry instance = new ImapIdlePushListenerRegistry();

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static ImapIdlePushListenerRegistry getInstance() {
        return instance;
    }

    private final ConcurrentMap<SimpleKey, ImapIdlePushListener> map;

    private final AtomicBoolean enabled;

    /**
     * Initializes a new {@link ImapIdlePushListenerRegistry}.
     */
    private ImapIdlePushListenerRegistry() {
        super();
        enabled = new AtomicBoolean(true);
        map = new ConcurrentHashMap<SimpleKey, ImapIdlePushListener>();
    }

    /**
     * Sets the enabled flag to specified <code>newEnabledFlag</code> if current value equals <code>expectedEnabledFlag</code>.
     *
     * @param expectedEnabledFlag The expected enabled flag
     * @param newEnabledFlag The new enabled flags
     * @return <code>true</code> if compare-and-set was successful; otherwise <code>false</code>
     */
    public final boolean compareAndSetEnabled(final boolean expectedEnabledFlag, final boolean newEnabledFlag) {
        return this.enabled.compareAndSet(expectedEnabledFlag, newEnabledFlag);
    }

    /**
     * Sets the enabled flag.
     *
     * @param enabled The flag
     */
    public final void setEnabled(final boolean enabled) {
        this.enabled.set(enabled);
    }

    /**
     * Clears this registry. <br>
     * <b>Note</b>: {@link ImapIdlePushListener#close()} is called for each instance.
     */
    public void clear() {
        for (final Iterator<ImapIdlePushListener> i = map.values().iterator(); i.hasNext();) {
            i.next().close();
        }
        map.clear();
    }

    /**
     * Closes all listeners contained in this registry.
     */
    public void closeAll() {
        for (final Iterator<ImapIdlePushListener> i = map.values().iterator(); i.hasNext();) {
            i.next().close();
        }
    }

    /**
     * Opens all listeners contained in this registry.
     */
    public void openAll() {
        if (!enabled.get()) {
            return;
        }
        for (final Iterator<ImapIdlePushListener> i = map.values().iterator(); i.hasNext();) {
            final ImapIdlePushListener l = i.next();
            try {
                l.open();
            } catch (final OXException e) {
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ImapIdlePushListenerRegistry.class)).error(
                    MessageFormat.format("Opening IMAP IDLE listener failed. Removing listener from registry: {0}", l.toString()),
                    e);
                i.remove();
            }
        }
    }

    /**
     * Adds specified push listener.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param pushListener The push listener to add
     * @return <code>true</code> if push listener service could be successfully added; otherwise <code>false</code>
     */
    public boolean addPushListener(final int contextId, final int userId, final ImapIdlePushListener pushListener) {
        if (!enabled.get()) {
            return false;
        }
        final SimpleKey key = SimpleKey.valueOf(contextId, userId);
        return (null == map.putIfAbsent(key, pushListener));
    }

    /**
     * Removes specified session identifier associated with given user-context-pair and the push listener as well, if no more
     * user-associated session identifiers are present.
     * 
     * @param session The session
     * @return <code>true</code> if a push listener for given user-context-pair was found and removed; otherwise <code>false</code>
     * @throws OXException 
     */
    public boolean removePushListener(final Session session) throws OXException {
        final int contextId = session.getContextId();
        final int userId = session.getUserId();
        final SimpleKey key = SimpleKey.valueOf(contextId, userId);
        final ImapIdlePushListener currentListener = map.get(key);
        if (null != currentListener) {
            final Session ses = currentListener.getSession();
            final SessiondService sessiondService = ImapIdleServiceRegistry.getServiceRegistry().getService(SessiondService.class);
            if ((session.equals(ses)) || (null != ses) && ((null != sessiondService.getSession(ses.getSessionID())) || session.getSessionID().equals(
                ses.getSessionID()))) {
                /*
                 * Listener's session is still valid; ignore this request for stopping listener
                 */
                return false;
            }
        }
        return removePushListener(contextId, userId);
    }

    /**
     * Removes specified session identifier associated with given user-context-pair and the push listener as well, if no more
     * user-associated session identifiers are present.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a push listener for given user-context-pair was found and removed; otherwise <code>false</code>
     * @throws PushException
     */
    public boolean removePushListener(final int contextId, final int userId) throws OXException {
        final SessiondService sessiondService = ImapIdleServiceRegistry.getServiceRegistry().getService(SessiondService.class);
        // Bind ImapIdlePushListener to another session because of password change issues.
        final Session session = sessiondService.findFirstMatchingSessionForUser(userId, contextId, new SessionMatcher() {

            @Override
            public boolean accepts(final Session tmp) {
                return PushUtility.allowedClient(tmp.getClient());
            }

        });
        final SimpleKey key = SimpleKey.valueOf(contextId, userId);
        final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(ImapIdlePushListenerRegistry.class));
        if (null == session) {
            final String message = "Found no other valid & active session for user " + userId + " in context " + contextId  + ". Therefore shutting down associated IMAP IDLE push listener.";
            if (isDebugEnabled(logger)) {
                logger.info(message, new Throwable());
            } else {
                logger.info(message);
            }
            return removeListener(key);
        }
        /*
         * Check for equality and/or validity of looked-up session and listener's session
         */
        {
            final ImapIdlePushListener currentListener = map.get(key);
            if (null != currentListener) {
                final Session ses = currentListener.getSession();
                if ((session.equals(ses)) || (null != ses) && ((null != sessiondService.getSession(ses.getSessionID())) || session.getSessionID().equals(
                    ses.getSessionID()))) {
                    /*
                     * Listener's session is still valid; ignore this request for stopping listener
                     */
                    return false;
                }
            }
        }
        /*
         * Proceed with newly found session
         */
        logger.info("Found another valid & active session for user " + userId + " in context " + contextId  + ". Reactivating IMAP IDLE push listener.");
        removeListener(key);
        final ImapIdlePushListener pushListener = ImapIdlePushListener.newInstance(session);
        if (null == map.putIfAbsent(key, pushListener)) {
            pushListener.open();
            logger.info("Reactivated IMAP IDLE push listener for user " + userId + " in context " + contextId);
            return false;
        }
        return true;
    }

    /**
     * Purges specified user's push listener.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a push listener for given user-context-pair was found and purged; otherwise <code>false</code>
     */
    public boolean purgeUserPushListener(final int contextId, final int userId) {
        return removeListener(SimpleKey.valueOf(contextId, userId));
    }

   /**
    * Purges specified user's push listener.
    *
    * @param listener The push listener to purge from registry
    * @return <code>true</code> if exactly that push listener was found and purged; otherwise <code>false</code>
    */
   public boolean purgeUserPushListener(final ImapIdlePushListener listener) {
       final int userId = listener.getUserId();
       final int contextId = listener.getContextId();
       if (map.remove(SimpleKey.valueOf(contextId, userId), listener)) {
           listener.close();
           return true;
       }
       return false;
   }

    /**
     * Purges all listeners and their data.
     *
     * @return <code>true</code> on success; otherwise <code>false</code>
     */
    public boolean purgeAllPushListener() {
        for (final Iterator<ImapIdlePushListener> i = map.values().iterator(); i.hasNext();) {
            i.next().close();
            i.remove();
        }
        return true;
    }

    private boolean removeListener(final SimpleKey key) {
        final ImapIdlePushListener listener = map.remove(key);
        if (null != listener) {
            listener.close();
        }
        return true;
    }

    /**
     * Gets a read-only {@link Iterator iterator} over the push listeners in this registry.
     * <p>
     * Invoking {@link Iterator#remove() remove} will throw an {@link UnsupportedOperationException}.
     *
     * @return A read-only {@link Iterator iterator} over the push listeners in this registry.
     */
    public Iterator<ImapIdlePushListener> getPushListeners() {
        final List<ImapIdlePushListener> list = new LinkedList<ImapIdlePushListener>();
        for (final Iterator<ImapIdlePushListener> i = map.values().iterator(); i.hasNext();) {
            list.add(i.next());
        }
        return Collections.unmodifiableIterator(list.iterator());
    }

    private static final class SimpleKey {

        public static SimpleKey valueOf(final int cid, final int user) {
            return new SimpleKey(cid, user);
        }

        final int cid;

        final int user;

        private final int hash;

        private SimpleKey(final int cid, final int user) {
            super();
            this.cid = cid;
            this.user = user;
            // hash code
            final int prime = 31;
            int result = 1;
            result = prime * result + cid;
            result = prime * result + user;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SimpleKey)) {
                return false;
            }
            final SimpleKey other = (SimpleKey) obj;
            if (cid != other.cid) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }
    } // End of SimpleKey
}

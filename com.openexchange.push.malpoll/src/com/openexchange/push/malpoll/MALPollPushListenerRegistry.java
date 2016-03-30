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

package com.openexchange.push.malpoll;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushListener;
import com.openexchange.push.malpoll.services.MALPollServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.Collections;

/**
 * {@link MALPollPushListenerRegistry} - The registry for MAL poll {@link PushListener}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollPushListenerRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MALPollPushListenerRegistry.class);

    private static final MALPollPushListenerRegistry instance = new MALPollPushListenerRegistry();

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static MALPollPushListenerRegistry getInstance() {
        return instance;
    }

    private final ConcurrentMap<SimpleKey, MALPollPushListener> map;

    private final AtomicBoolean enabled;

    /**
     * Initializes a new {@link MALPollPushListenerRegistry}.
     */
    private MALPollPushListenerRegistry() {
        super();
        enabled = new AtomicBoolean(true);
        map = new ConcurrentHashMap<SimpleKey, MALPollPushListener>();
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
     * <b>Note</b>: {@link MALPollPushListener#close()} is called for each instance.
     */
    public void clear() {
        for (final Iterator<MALPollPushListener> i = map.values().iterator(); i.hasNext();) {
            i.next().close();
            i.remove();
        }
        map.clear();
    }

    /**
     * Closes all listeners contained in this registry.
     */
    public void closeAll() {
        for (final Iterator<MALPollPushListener> i = map.values().iterator(); i.hasNext();) {
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
        for (final Iterator<MALPollPushListener> i = map.values().iterator(); i.hasNext();) {
            final MALPollPushListener l = i.next();
            try {
                l.open();
            } catch (final OXException e) {
                org.slf4j.LoggerFactory.getLogger(MALPollPushListenerRegistry.class).error(
                    MessageFormat.format("Opening MAL Poll listener failed. Removing listener from registry: {0}", l.toString()),
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
    public boolean addPushListener(final int contextId, final int userId, final MALPollPushListener pushListener) {
        return (enabled.get() && null == map.putIfAbsent(SimpleKey.valueOf(contextId, userId), pushListener));
    }

    /**
     * Removes specified session identifier associated with given user-context-pair and the push listener as well, if no more
     * user-associated session identifiers are present.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a push listener for given user-context-pair was found and removed; otherwise <code>false</code>
     */
    public boolean removePushListener(final int contextId, final int userId) {
        final SessiondService sessiondService = MALPollServiceRegistry.getServiceRegistry().getService(SessiondService.class);
        if (null == sessiondService || null == sessiondService.getAnyActiveSessionForUser(userId, contextId)) {
            return removeListener(SimpleKey.valueOf(contextId, userId));
        }
        return false;
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
     * Purges all listeners and their data.
     *
     * @return <code>true</code> on success; otherwise <code>false</code>
     */
    public boolean purgeAllPushListener() {
        for (final Iterator<Entry<MALPollPushListenerRegistry.SimpleKey, MALPollPushListener>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            final Entry<MALPollPushListenerRegistry.SimpleKey, MALPollPushListener> entry = iterator.next();
            final MALPollPushListener listener = entry.getValue();
            if (null != listener) {
                listener.close();
            }
            final SimpleKey key = entry.getKey();
            try {
                MALPollDBUtility.dropMailIDs(key.cid, key.user);
            } catch (final OXException e) {
                LOG.error("DB tables could not be cleansed for removed push listener. User={}, context={}", key.user, key.cid, e);
            }
        }
        return true;
    }

    private boolean removeListener(final SimpleKey key) {
        final MALPollPushListener listener = map.remove(key);
        if (null != listener) {
            listener.close();
        }
        try {
            MALPollDBUtility.dropMailIDs(key.cid, key.user);
        } catch (final OXException e) {
            LOG.error("DB tables could not be cleansed for removed push listener. User={}, context={}", key.user, key.cid, e);
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
    public Iterator<MALPollPushListener> getPushListeners() {
        return Collections.unmodifiableIterator(map.values().iterator());
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

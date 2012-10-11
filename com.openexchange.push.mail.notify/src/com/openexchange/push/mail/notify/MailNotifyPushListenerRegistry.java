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

package com.openexchange.push.mail.notify;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.push.PushListener;
import com.openexchange.push.mail.notify.services.PushServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.iterator.ReadOnlyIterator;

/**
 * {@link MailNotifyPushListenerRegistry} - The registry for {@link PushListener}s.
 *
 */
public final class MailNotifyPushListenerRegistry {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailNotifyPushListenerRegistry.class));


    /**
     * @return the useOXLogin
     */
    public final boolean isUseOXLogin() {
        return useOXLogin;
    }


    /**
     * @param useOXLogin the useOXLogin to set
     */
    public final void setUseOXLogin(final boolean useOXLogin) {
        this.useOXLogin = useOXLogin;
    }


    /**
     * @return the useEmailAddress
     */
    public final boolean isUseEmailAddress() {
        return useEmailAddress;
    }


    /**
     * @param useEmailAddress the useEmailAddress to set
     */
    public final void setUseEmailAddress(final boolean useEmailAddress) {
        this.useEmailAddress = useEmailAddress;
    }

    private static final MailNotifyPushListenerRegistry instance = new MailNotifyPushListenerRegistry();

    private final ConcurrentMap<String, MailNotifyPushListener> map;

    private boolean useOXLogin;

    private boolean useEmailAddress;

    /**
     * Initializes a new {@link MailNotifyPushListenerRegistry}.
     */
    private MailNotifyPushListenerRegistry() {
        super();
        map = new ConcurrentHashMap<String, MailNotifyPushListener>();
    }

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static MailNotifyPushListenerRegistry getInstance() {
        return instance;
    }

    /**
     * If the given mboxid is registered for receiving of events, fire event...
     *
     * @param mboxid
     * @throws OXException
     */
    public void fireEvent(final String mboxid) throws OXException {
        final PushListener listener;
        LOG.debug("checking whether to fire event for " + mboxid);
        if (null != (listener = map.get(mboxid))) {
            LOG.debug("fireEvent, mboxid=" + mboxid);
            listener.notifyNewMail();
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
    public boolean addPushListener(final int contextId, final int userId, final MailNotifyPushListener pushListener) throws OXException {
        boolean notYetPushed = true;
        for(final String id : getMboxIds(contextId, userId)) {
            LOG.debug("adding alias " + id + " to map");
            final boolean pushFailed = (null == map.putIfAbsent(id, pushListener) ? false : true);
            if( notYetPushed && pushFailed ) {
                notYetPushed = false;
            }
        }
        return notYetPushed;
    }

    /**
     * return all the users aliases with domain stripped off and localpart to lowercase
     *
     * @param contextId
     * @param userId
     * @return String Array
     * @throws OXException
     */
    private final String[] getMboxIds(final int contextId, final int userId) throws OXException {
        Context storageContext;
        {
            storageContext = ContextStorage.getStorageContext(contextId);
            final User user = UserStorage.getInstance().getUser(userId, storageContext);
            int alength = user.getAliases().length;
            if( useOXLogin ) {
                alength++;
            }
            final String[] ret = new String[alength];
            int i=0;
            for(final String alias : user.getAliases()) {
                if( useEmailAddress ) {
                    ret[i] = alias.toLowerCase();
                } else {
                    final int idx = alias.indexOf("@");
                    if( idx != -1) {
                        ret[i] = alias.substring(0, idx).toLowerCase();
                    } else {
                        ret[i] = alias.toLowerCase();
                    }
                }
                i++;
            }
            if( useOXLogin ) {
                ret[i] = user.getLoginInfo().toLowerCase();
            }
            return ret;
        }
    }
    /**
     * Clears this registry. <br>
     * <b>Note</b>: {@link MailNotifyPushListener#close()} is called for each instance.
     */
    public void clear() {
        for (final Iterator<MailNotifyPushListener> i = map.values().iterator(); i.hasNext();) {
            i.next().close();
            i.remove();
        }
        map.clear();
    }

    /**
     * Closes all listeners contained in this registry.
     */
    public void closeAll() {
        for (final Iterator<MailNotifyPushListener> i = map.values().iterator(); i.hasNext();) {
            i.next().close();
        }
    }

    /**
     * Gets a read-only {@link Iterator iterator} over the push listeners in this registry.
     * <p>
     * Invoking {@link Iterator#remove() remove} will throw an {@link UnsupportedOperationException}.
     *
     * @return A read-only {@link Iterator iterator} over the push listeners in this registry.
     */
    public Iterator<MailNotifyPushListener> getPushListeners() {
        return new ReadOnlyIterator<MailNotifyPushListener>(map.values().iterator());
    }

    /**
     * Opens all listeners contained in this registry.
     */
    public void openAll() {
        for (final Iterator<MailNotifyPushListener> i = map.values().iterator(); i.hasNext();) {
            final MailNotifyPushListener l = i.next();
            try {
                l.open();
            } catch (final OXException e) {
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailNotifyPushListenerRegistry.class)).error(
                    MessageFormat.format("Opening mail push UDP listener failed. Removing listener from registry: {0}", l.toString()),
                    e);
                i.remove();
            }
        }
    }

    /**
     * Purges specified user's push listener and all of user-associated session identifiers from this registry.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a push listener for given user-context-pair was found and purged; otherwise <code>false</code>
     * @throws OXException
     */
    public boolean purgeUserPushListener(final int contextId, final int userId) throws OXException {
        return removeListener(getMboxIds(contextId, userId));
    }

    /**
     * Removes specified session identifier associated with given user-context-pair and the push listener as well, if no more
     * user-associated session identifiers are present.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a push listener for given user-context-pair was found and removed; otherwise <code>false</code>
     * @throws OXException
     */
    public boolean removePushListener(final int contextId, final int userId) throws OXException {
        final SessiondService sessiondService = PushServiceRegistry.getServiceRegistry().getService(SessiondService.class);
        if (null == sessiondService || null == sessiondService.getAnyActiveSessionForUser(userId, contextId)) {
            return removeListener(getMboxIds(contextId, userId));
        }
        return false;
    }

    private boolean removeListener(final String[] mboxIds) {
        for(final String id : mboxIds) {
            LOG.debug("removing alias" + id + " from map");
            final MailNotifyPushListener listener = map.remove(id);
            if (null != listener) {
                listener.close();
            }
        }
        return true;
    }
}

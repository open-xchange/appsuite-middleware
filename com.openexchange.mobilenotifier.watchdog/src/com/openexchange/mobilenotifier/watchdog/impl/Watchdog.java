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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mobilenotifier.watchdog.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.mobilenotifier.MobileNotifierProviders;
import com.openexchange.mobilenotifier.events.MobileNotifyEventService;
import com.openexchange.mobilenotifier.events.storage.ContextUsers;
import com.openexchange.mobilenotifier.events.storage.MobileNotifierStorageService;
import com.openexchange.mobilenotifier.events.storage.UserToken;
import com.openexchange.mobilenotifier.watchdog.osgi.Services;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushListenerServiceExtended;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link Watchdog}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Watchdog  {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Watchdog.class);

    public static void sessionLookup() throws OXException {
        MobileNotifierStorageService rdms = Services.getService(MobileNotifierStorageService.class);

        //The time to wait for sending a new notification
        List<ContextUsers> contextUsers = rdms.getSubscriptions(MobileNotifierProviders.MAIL, true);

        if(false == contextUsers.isEmpty()) {
            PushListenerService pls = Services.getService(PushListenerService.class);
            if(pls instanceof PushListenerServiceExtended) {
                PushListenerServiceExtended plse = (PushListenerServiceExtended) pls;
                List<ContextUsers> contextUsersWithoutPush = new LinkedList<ContextUsers>();
                for(ContextUsers cu : contextUsers) {
                    boolean[] hasListeners = plse.hasListenerFor(cu.getContextId(), Autoboxing.I2i(getUserIds(cu)));
                    cu = getUserIdsWithoutPushListener(cu, hasListeners);
                    if(cu != null) {
                        contextUsersWithoutPush.add(cu);
                    }
                }

                if(false == contextUsersWithoutPush.isEmpty()) {
                    SessiondService sessiond = Services.getService(SessiondService.class);

                    List<ContextUsers> contextUsersWithoutSessions = new ArrayList<ContextUsers>();

                    for(ContextUsers cu : contextUsersWithoutPush) {
                        List<UserToken> userIdsWithoutSessions = new LinkedList<UserToken>();
                        int ctxId = cu.getContextId();

                        for(UserToken userToken : cu.getUserTokens()) {
                            Session session = sessiond.getAnyActiveSessionForUser(ctxId, userToken.getUserId());
                            if(session == null) {
                                // user has no active push listener and no active session found
                                userIdsWithoutSessions.add(userToken);
                            } else {
                                //TODO client? / starts for multiple users
                                plse.startListenerFor(session);
                            }
                        }
                        if(false == userIdsWithoutSessions.isEmpty()) {
                            contextUsersWithoutSessions.add(new ContextUsers(ctxId, userIdsWithoutSessions));
                        }
                    }

                    if(false == contextUsersWithoutSessions.isEmpty()) {
                        //TODO: async call
                        MobileNotifyEventService mns = Services.getService(MobileNotifyEventService.class);
                        LOG.trace("Notifying new login");
                        mns.notifyLogin(contextUsersWithoutSessions);
                    }
                }
            }
        }
    }

    private static List<Integer> getUserIds(ContextUsers contextUsers) {
        List<UserToken> userTokens = contextUsers.getUserTokens();
        List<Integer> userIds = new LinkedList<Integer>();
        if(userTokens != null && false == userTokens.isEmpty()) {
            for(UserToken ut : userTokens) {
                userIds.add(ut.getUserId());
            }
        }
        return userIds;
    }

    /**
     * Returns the ContextUsers without any push listeners.
     *
     * @param contextUser
     * @param hasListeners
     * @return
     */
    private static ContextUsers getUserIdsWithoutPushListener(ContextUsers contextUser, boolean[] hasListeners) {
        int i=0;
        for(Iterator<UserToken> iter = contextUser.getUserTokens().iterator(); iter.hasNext();) {
            if(hasListeners[i++]) {
                iter.remove();
            }
        }
        if(contextUser.getUserTokens().isEmpty()) {
            return null;
        }
        return new ContextUsers(contextUser.getContextId(), contextUser.getUserTokens());
    }
}

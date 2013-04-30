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

package com.openexchange.sessiond.impl;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.log.ForceLog;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.session.SessionHolderExtended;


/**
 * {@link ThreadLocalSessionHolder} - The session holder using a {@link ThreadLocal} instance.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ThreadLocalSessionHolder implements SessionHolderExtended {

    private static final ThreadLocalSessionHolder INSTANCE = new ThreadLocalSessionHolder();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static ThreadLocalSessionHolder getInstance() {
        return INSTANCE;
    }

    private final ThreadLocal<ServerSession> session;

    /**
     * Initializes a new {@link ThreadLocalSessionHolder}.
     */
    private ThreadLocalSessionHolder() {
        super();
        session = new ThreadLocal<ServerSession>();
    }

    /**
     * Sets the specified <tt>ServerSession</tt> instance.
     *
     * @param serverSession The <tt>ServerSession</tt> instance
     */
    public void setSession(final ServerSession serverSession) {
        session.set(serverSession);
        if (LogProperties.isEnabled() && serverSession != null) {
            final Props properties = LogProperties.getLogProperties();
            properties.put(LogProperties.Name.SESSION_SESSION_ID, serverSession.getSessionID());
            properties.put(LogProperties.Name.SESSION_USER_ID, Integer.valueOf(serverSession.getUserId()));
            properties.put(LogProperties.Name.SESSION_CONTEXT_ID, Integer.valueOf(serverSession.getContextId()));
            final String client = serverSession.getClient();
            properties.put(LogProperties.Name.SESSION_CLIENT_ID, client == null ? "unknown" : ForceLog.valueOf(client));
            properties.put(LogProperties.Name.SESSION_SESSION, serverSession);
        }
    }

    public void clear() {
        session.remove();
    }

    @Override
    public Context getContext() {
        return getSessionObject().getContext();
    }

    @Override
    public Session optSessionObject() {
        final ServerSession serverSession = session.get();
        if (serverSession == null && LogProperties.isEnabled()) {
            return LogProperties.getLogProperty(LogProperties.Name.SESSION_SESSION);
        }
        return serverSession;
    }

    @Override
    public ServerSession getSessionObject() {
        final ServerSession serverSession = session.get();
        if (serverSession == null) {
        	if (LogProperties.isEnabled()) {
        		final Session session = LogProperties.getLogProperty(LogProperties.Name.SESSION_SESSION);
        		try {
					return ServerSessionAdapter.valueOf(session);
				} catch (final OXException e) {
					return null;
				}
        	}
        }
        return serverSession;
    }

    @Override
    public User getUser() {
        return getSessionObject().getUser();
    }

}

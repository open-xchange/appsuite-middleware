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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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


package com.openexchange.tools.session;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;

/**
 * {@link ServerSessionAdapter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ServerSessionAdapter implements ServerSession {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.exception.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ServerSessionAdapter.class));

    /**
     * Gets the server session for specified session.
     * 
     * @param session The session
     * @return The appropriate server session
     * @throws OXException If context cannot be resolved
     */
    public static ServerSession valueOf(final Session session) throws OXException {
        if (ServerSession.class.isInstance(session)) {
            return (ServerSession) session;
        }
        return new ServerSessionAdapter(session);
    }

    private Session session;

    private Context ctx;

    private volatile User user;

    private volatile UserConfiguration userConfiguration;

    private volatile UserSettingMail userSettingMail;

    private ServerSession serverSession;

    /**
     * Initializes a new {@link ServerSessionAdapter}.
     *
     * @param session The delegate session
     * @throws OXException If context look-up fails
     */
    public ServerSessionAdapter(final Session session) throws OXException {
        super();
        if (ServerSession.class.isInstance(session)) {
            this.serverSession = (ServerSession) session;
        } else {
            this.session = session;
            ctx = ContextStorage.getStorageContext(session.getContextId());
        }
    }

    /**
     * Initializes a new {@link ServerSessionAdapter}.
     *
     * @param session The delegate session
     * @param ctx The session's context object
     */
    public ServerSessionAdapter(final Session session, final Context ctx) {
        super();
        if (ServerSession.class.isInstance(session)) {
            this.serverSession = (ServerSession) session;
        } else {
            this.session = session;
            this.ctx = ctx;
        }
    }

    /**
     * Initializes a new {@link ServerSessionAdapter}.
     *
     * @param session The delegate session
     * @param ctx The session's context object
     * @param user The session's user object
     */
    public ServerSessionAdapter(final Session session, final Context ctx, final User user) {
        super();
        if (ServerSession.class.isInstance(session)) {
            this.serverSession = (ServerSession) session;
        } else {
            this.session = session;
            this.ctx = ctx;
            this.user = user;
        }
    }

    /**
     * Initializes a new {@link ServerSessionAdapter}.
     *
     * @param session The delegate session
     * @param ctx The session's context object
     * @param user The session's user object
     */
    public ServerSessionAdapter(final Session session, final Context ctx, final User user, final UserConfiguration userConfiguration) {
        super();
        if (ServerSession.class.isInstance(session)) {
            this.serverSession = (ServerSession) session;
        } else {
            this.session = session;
            this.ctx = ctx;
            this.user = user;
            this.userConfiguration = userConfiguration;
        }
    }

    public int getContextId() {
        return session().getContextId();
    }

    public String getLocalIp() {
        return session().getLocalIp();
    }

    public void setLocalIp(final String ip) {
        session().setLocalIp(ip);
    }

    public String getLoginName() {
        return session().getLoginName();
    }

    public boolean containsParameter(final String name) {
        return session().containsParameter(name);
    }

    public Object getParameter(final String name) {
        return session().getParameter(name);
    }

    public String getPassword() {
        return session().getPassword();
    }

    public String getRandomToken() {
        return session().getRandomToken();
    }

    public String getSecret() {
        return session().getSecret();
    }

    public String getSessionID() {
        return session().getSessionID();
    }

    public int getUserId() {
        return session().getUserId();
    }

    public String getUserlogin() {
        return session().getUserlogin();
    }

    public void setParameter(final String name, final Object value) {
        session().setParameter(name, value);
    }

    public void removeRandomToken() {
        session().removeRandomToken();
    }

    public String getAuthId() {
        return session().getAuthId();
    }

    public Context getContext() {
        if (serverSession != null) {
            return serverSession.getContext();
        }
        return ctx;
    }

    public String getLogin() {
        return session().getLogin();
    }

    public String getHash() {
        return session().getHash();
    }

    public String getClient() {
        return session.getClient();
    }

    public void setClient(final String client) {
        session.setClient(client);
    }

    public User getUser() {
        if (serverSession != null) {
            return serverSession.getUser();
        }
        User tmp = user;
        if (null == tmp) {
            synchronized (this) {
                tmp = user;
                if (null == tmp) {
                    user = tmp = UserStorage.getStorageUser(getUserId(), ctx);
                }
            }
        }
        return tmp;
    }

    public UserConfiguration getUserConfiguration() {
        if (serverSession != null) {
            return serverSession.getUserConfiguration();
        }
        UserConfiguration tmp = userConfiguration;
        if (null == tmp) {
            synchronized (this) {
                tmp = userConfiguration;
                if (null == tmp) {
                    try {
                        userConfiguration = tmp = UserConfigurationStorage.getInstance().getUserConfiguration(getUserId(), ctx);
                    } catch (final OXException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
        return tmp;
    }

    public UserSettingMail getUserSettingMail() {
        if (serverSession != null) {
            return serverSession.getUserSettingMail();
        }
        UserSettingMail tmp = userSettingMail;
        if (null == tmp) {
            synchronized (this) {
                tmp = userSettingMail;
                if (null == tmp) {
                    userSettingMail = tmp = UserSettingMailStorage.getInstance().getUserSettingMail(getUserId(), ctx);
                }
            }
        }
        return tmp;
    }

    private Session session() {
        if (serverSession != null) {
            return serverSession;
        }
        return session;
    }

    public void setHash(final String hash) {
        session().setHash(hash);
    }

}

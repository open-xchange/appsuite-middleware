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

package com.openexchange.tools.session;

import static com.openexchange.osgi.util.ServiceCallWrapper.doServiceCall;
import java.util.Set;
import org.apache.commons.lang.Validate;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceException;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceUser;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link ServerSessionAdapter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ServerSessionAdapter implements ServerSession, PutIfAbsent {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServerSessionAdapter.class);

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
        return null == session ? null : new ServerSessionAdapter(session);
    }

    /**
     * Gets the server session for specified session.
     *
     * @param session The session
     * @param context The associated context
     * @return The appropriate server session
     */
    public static ServerSession valueOf(final Session session, final Context context) {
        if (ServerSession.class.isInstance(session)) {
            return (ServerSession) session;
        }
        return null == session ? null : new ServerSessionAdapter(session, context);
    }

    /**
     * Gets the server session for specified session.
     *
     * @param session The session
     * @param context The associated context
     * @param user The user
     * @return The appropriate server session
     */
    public static ServerSession valueOf(final Session session, final Context context, final User user) {
        if (ServerSession.class.isInstance(session)) {
            return (ServerSession) session;
        }
        return null == session ? null : new ServerSessionAdapter(session, context, user);
    }


    /**
     * Gets the server session for specified session.
     *
     * @param session The session
     * @param context The associated context
     * @param user The user
     * @param userConfiguration The user configuration
     * @return The appropriate server session
     */
    public static ServerSession valueOf(final Session session, final Context context, final User user, final UserConfiguration userConfiguration) {
        if (ServerSession.class.isInstance(session)) {
            return (ServerSession) session;
        }
        return null == session ? null : new ServerSessionAdapter(session, context, user, userConfiguration);
    }

    /**
     * Creates a synthetic server session for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The synthetic server session
     * @throws OXException If creation of server session fails
     */
    public static ServerSession valueOf(int userId, int contextId) throws OXException {
        return new ServerSessionAdapter(userId, contextId);
    }

    // ---------------------------------------------------------------------------------------------------------------------- //

    private final Session session;
    private final ServerSession serverSession;
    private final Context context;
    private final User overwriteUser;
    private final UserConfiguration overwriteUserConfiguration;
    private final UserPermissionBits overwritePermissionBits;

    /**
     * Initializes a new {@link ServerSessionAdapter}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If initialization fails
     */
    public ServerSessionAdapter(final int userId, final int contextId) throws OXException {
        super();
        if (contextId > 0) {
            context = loadContext(contextId);
        } else {
            context = null;
        }

        session = new SessionObject("synthetic") {
            @Override
            public int getUserId() { return userId; }
            @Override
            public int getContextId() {return contextId; }
        };

        serverSession = null;
        overwriteUserConfiguration = null;
        overwritePermissionBits = null;
        overwriteUser = null;
    }

    /**
     * Initializes a new {@link ServerSessionAdapter}.
     *
     * @param session The delegate session
     * @throws OXException If initialization fails
     */
    public ServerSessionAdapter(final Session session) throws OXException {
        this(session, loadContext(session.getContextId()), null, null, null);
    }

    /**
     * Initializes a new {@link ServerSessionAdapter}.
     *
     * @param session The delegate session
     * @param ctx The session's context object
     * @throws IllegalArgumentException If session argument is <code>null</code>
     */
    public ServerSessionAdapter(@NonNull final Session session, @NonNull final Context ctx) {
        this(session, ctx, null, null, null);
    }

    /**
     * Initializes a new {@link ServerSessionAdapter}.
     *
     * @param session The delegate session
     * @param ctx The session's context object
     * @param user The session's user object
     * @throws IllegalArgumentException If session argument is <code>null</code>
     */
    public ServerSessionAdapter(@NonNull final Session session, @NonNull final Context ctx, @Nullable final User user) {
        this(session, ctx, user, null, null);

    }

    /**
     * Initializes a new {@link ServerSessionAdapter}.
     *
     * @param session The delegate session
     * @param ctx The session's context object
     * @param user The session's user object
     * @throws IllegalArgumentException If session argument is <code>null</code>
     */
    public ServerSessionAdapter(@NonNull final Session session, @NonNull final Context ctx, @Nullable final User user, @Nullable final UserConfiguration userConfiguration) {
        this(session, ctx, user, userConfiguration, null);
    }

    /**
     * Initializes a new {@link ServerSessionAdapter}.
     *
     * @param session The delegate session
     * @param ctx The session's context object
     * @param user The session's user object
     * @throws IllegalArgumentException If session argument is <code>null</code>
     */
    public ServerSessionAdapter(@NonNull final Session session, @NonNull final Context ctx, @Nullable final User user, @Nullable final UserConfiguration userConfiguration, @Nullable final UserPermissionBits permissionBits) {
        super();
        Validate.notNull(session, "Session is null.");
        Validate.notNull(ctx, "Context is null.");

        context = ctx;
        overwriteUser = user;
        overwriteUserConfiguration = userConfiguration;
        overwritePermissionBits = permissionBits;
        if (ServerSession.class.isInstance(session)) {
            this.serverSession = (ServerSession) session;
            this.session = null;
        } else {
            this.serverSession = null;
            this.session = session;
        }
    }

    @Override
    public int getContextId() {
        return session().getContextId();
    }

    @Override
    public String getLocalIp() {
        return session().getLocalIp();
    }

    @Override
    public void setLocalIp(final String ip) {
        session().setLocalIp(ip);
    }

    @Override
    public String getLoginName() {
        return session().getLoginName();
    }

    @Override
    public boolean containsParameter(final String name) {
        return session().containsParameter(name);
    }

    @Override
    public Object getParameter(final String name) {
        return session().getParameter(name);
    }

    @Override
    public String getPassword() {
        return session().getPassword();
    }

    @Override
    public String getRandomToken() {
        return session().getRandomToken();
    }

    @Override
    public String getSecret() {
        return session().getSecret();
    }

    @Override
    public String getSessionID() {
        return session().getSessionID();
    }

    @Override
    public int getUserId() {
        return session().getUserId();
    }

    @Override
    public String getUserlogin() {
        return session().getUserlogin();
    }

    @Override
    public void setParameter(final String name, final Object value) {
        session().setParameter(name, value);
    }

    @Override
    public Object setParameterIfAbsent(String name, Object value) {
        final Session session = session();
        if (session instanceof PutIfAbsent) {
            return ((PutIfAbsent) session).setParameterIfAbsent(name, value);
        }
        final Object prev = session.getParameter(name);
        if (null == prev) {
            session.setParameter(name, value);
            return null;
        }
        return prev;
    }

    @Override
    public String getAuthId() {
        return session().getAuthId();
    }

    @Override
    public String getLogin() {
        return session().getLogin();
    }

    @Override
    public String getHash() {
        return session().getHash();
    }

    @Override
    public String getClient() {
        return session().getClient();
    }

    @Override
    public void setClient(final String client) {
        session().setClient(client);
    }

    @Override
    public Context getContext() {
        if (serverSession != null) {
            return serverSession.getContext();
        }

        return context;
    }

    @Override
    public User getUser() {
        if (serverSession != null) {
            return serverSession.getUser();
        }
        if (null != overwriteUser) {
            return overwriteUser;
        }

        // Do not cache fetched instance
        final int userId = session.getUserId();
        if (userId <= 0) {
            return null;
        }
        try {
            return loadUser();
        } catch (final Exception e) {
            LOG.error("", e);
        }
        return null;
    }

    @Override
    public UserPermissionBits getUserPermissionBits() {
        if (serverSession != null) {
            return serverSession.getUserPermissionBits();
        }
        if (null != overwritePermissionBits) {
            return overwritePermissionBits;
        }

        // Do not cache fetched instance
        final int userId = null == overwriteUser ? session.getUserId() : overwriteUser.getId();
        if (userId <= 0) {
            return null;
        }
        try {
            return loadUserPermissionBits();
        } catch (final Exception e) {
            LOG.error("", e);
        }
        return null;
    }

    @Override
    public UserConfiguration getUserConfiguration() {
        if (serverSession != null) {
            return serverSession.getUserConfiguration();
        }
        if (null != overwriteUserConfiguration) {
            return overwriteUserConfiguration;
        }

        // Do not cache fetched instance
        final int userId = null == overwriteUser ? session.getUserId() : overwriteUser.getId();
        if (userId <= 0) {
            return null;
        }
        try {
            return loadUserConfiguration();
        } catch (final Exception e) {
            LOG.error("", e);
        }
        return null;
    }

    @Override
    public UserSettingMail getUserSettingMail() {
        if (serverSession != null) {
            return serverSession.getUserSettingMail();
        }
        // Do not cache fetched instance
        final int userId = null == overwriteUser ? session.getUserId() : overwriteUser.getId();
        if (userId <= 0) {
            return null;
        }

        if (getUser().isGuest()) {
            return null;
        }

        return UserSettingMailStorage.getInstance().getUserSettingMail(userId, context);
    }

    private Session session() {
        return serverSession == null ? session : serverSession;
    }

    @Override
    public void setHash(final String hash) {
        session().setHash(hash);
    }

    @Override
    public boolean isAnonymous() {
        return session().getUserId() <= 0;
    }

    @Override
    public String toString() {
        return session().toString();
    }

    @Override
    public boolean isTransient() {
        return session().isTransient();
    }

    @Override
    public int hashCode() {
        return session().hashCode();
    }

    @Override
    public Set<String> getParameterNames() {
        return session().getParameterNames();
    }

    private static Context loadContext(final int contextId) throws OXException {
        try {
            return doServiceCall(ServerSessionAdapter.class, ContextService.class,
                new ServiceUser<ContextService, Context>() {
                    @Override
                    public Context call(ContextService service) throws OXException {
                        return service.getContext(contextId);
                    }
                });
        } catch (ServiceException e) {
            throw e.toOXException();
        }
    }

    private User loadUser() throws Exception {
        return doServiceCall(getClass(), UserService.class,
            new ServiceUser<UserService, User>() {
                @Override
                public User call(UserService service) throws OXException {
                    return service.getUser(getUserId(), getContextId());
                }
            });
    }

    private UserPermissionBits loadUserPermissionBits() throws Exception {
        return doServiceCall(getClass(), UserPermissionService.class,
            new ServiceUser<UserPermissionService, UserPermissionBits>() {
                @Override
                public UserPermissionBits call(UserPermissionService service) throws OXException {
                    return service.getUserPermissionBits(getUserId(), getContext());
                }
            });
    }

    private UserConfiguration loadUserConfiguration() throws Exception {
        return doServiceCall(getClass(), UserConfigurationService.class,
            new ServiceUser<UserConfigurationService, UserConfiguration>() {
                @Override
                public UserConfiguration call(UserConfigurationService service) throws OXException {
                    return service.getUserConfiguration(getUserId(), getContext());
                }
            });
    }

}

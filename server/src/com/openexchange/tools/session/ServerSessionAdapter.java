
package com.openexchange.tools.session;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
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

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ServerSessionAdapter.class);

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
     * @throws ContextException If context look-up fails
     */
    public ServerSessionAdapter(final Session session) throws ContextException {
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

    public int getContextId() {
        return session().getContextId();
    }

    public String getLocalIp() {
        return session().getLocalIp();
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

    public Context getContext() {
        if (serverSession != null) {
            return serverSession.getContext();
        }
        return ctx;
    }

    public String getLogin() {
        return session().getLogin();
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
                    tmp = user = UserStorage.getStorageUser(getUserId(), ctx);
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
                        tmp = userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(getUserId(), ctx);
                    } catch (final UserConfigurationException e) {
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
                    tmp = userSettingMail = UserSettingMailStorage.getInstance().getUserSettingMail(getUserId(), ctx);
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

}

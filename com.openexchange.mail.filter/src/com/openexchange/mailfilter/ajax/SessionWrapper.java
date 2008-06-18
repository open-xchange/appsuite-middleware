package com.openexchange.mailfilter.ajax;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.openexchange.mailfilter.ajax.exceptions.OXMailfilterException;
import com.openexchange.mailfilter.services.MailFilterServletServiceRegistry;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.exception.SessiondException;


/**
 * This class is used to deal with the fact that the mailfilter takes requests
 * from the admin-gui and the groupware gui. So it has to deal with different
 * session and session type. To handle this unique this class is introduced
 * 
 * @author d7
 * 
 */
public class SessionWrapper {

    private static final String USERNAME_PARAMETER = "username";

    public static final String USERNAME = USERNAME_PARAMETER;

    public static final String PASSWORD = "password";

    /**
     * The name where the username is stored in the session object, must be
     * different because "username" is already used in admin session
     */
    public static final String USERNAME_SESSION = "username_auth";

    public class Credentials {
        
        private String username;
        
        private String authname;
        
        private String password;
        
        private int userid;
        
        private int contextid;

        /**
         * @param username
         * @param password
         */
        public Credentials(final String authname, final String password, final int userid, final int contextid) {
            super();
            this.authname = authname;
            this.password = password;
            this.userid = userid;
            this.contextid = contextid;
        }

        /**
         * @param username
         * @param authname
         * @param password
         */
        public Credentials(final String username, final String authname, final String password) {
            super();
            this.username = username;
            this.authname = authname;
            this.password = password;
        }

        /**
         * @return the username
         */
        public final String getUsername() {
            return username;
        }

        /**
         * @param username the username to set
         */
        public final void setUsername(final String username) {
            this.username = username;
        }

        /**
         * @return the password
         */
        public final String getPassword() {
            return password;
        }

        /**
         * @param password the password to set
         */
        public final void setPassword(final String password) {
            this.password = password;
        }

        /**
         * @return the authname
         */
        public final String getAuthname() {
            return authname;
        }

        /**
         * @param authname the authname to set
         */
        public final void setAuthname(final String authname) {
            this.authname = authname;
        }

        /**
         * @return the userid
         */
        public final int getUserid() {
            return userid;
        }

        /**
         * @return the contextid
         */
        public final int getContextid() {
            return contextid;
        }

        /**
         * This method returns the right username. If username is null this is the authname otherwise the username
         */
        public final String getRightUsername() {
            if (null != this.username) {
                return this.username;
            } else {
                return this.authname;
            }
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Username: " + this.username;
        }
        
        
    }
    
    /**
     * Takes the httpSession from the admin
     */
    private HttpSession httpSession;
    
    /**
     * Takes the session from the groupware
     */
    private Session session;

    /**
     * @param req
     * @throws SessiondException 
     * @throws OXMailfilterException 
     */
    public SessionWrapper(final HttpServletRequest req) throws SessiondException, OXMailfilterException {
        super();
        // First determine which mode to choose from the parameters
        final String username = req.getParameter(USERNAME_PARAMETER);
        final Cookie[] cookies = req.getCookies();
        for (final Cookie cookie : cookies) {
            if (null != username && cookie.getName().startsWith("JSESSION")) {
                // admin mode
                this.httpSession = req.getSession(false);
                if (null == this.httpSession) {
                    throw new OXMailfilterException(OXMailfilterException.Code.SESSION_EXPIRED, null, "Can't find session.");
                }
                this.httpSession.setAttribute(USERNAME_SESSION, username);
            } else if (null == username && cookie.getName().startsWith("open-xchange")) {
                // groupware mode
                final SessiondService service = MailFilterServletServiceRegistry.getServiceRegistry().getService(SessiondService.class);
                if (null == service) {
                    throw new SessiondException(new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE));
                }
                this.session = service.getSession(cookie.getValue());
                if (null == this.session) {
                    throw new OXMailfilterException(OXMailfilterException.Code.SESSION_EXPIRED, null, "Can't find session.");
                }
                return;
            }
        }
    }

    public Credentials getCredentials() {
        if (null != this.httpSession) {
            // Admin mode
            final String authname = (String) httpSession.getAttribute(USERNAME);
            final String password = (String) httpSession.getAttribute(PASSWORD);
            final String username = (String) httpSession.getAttribute(USERNAME_SESSION);
            return new Credentials(username, authname, password);
        } else if (null != this.session) {
            // Groupware mode
            final String loginName = this.session.getLoginName();
            final String password2 = this.session.getPassword();
            final int userId = this.session.getUserId();
            final int contextId = this.session.getContextId();
            return new Credentials(loginName, password2, userId, contextId);
        }
        return null;
    }
    
    public void setParameter(final String name, final Object obj) {
        if (null != this.httpSession) {
            // Admin mode
            this.httpSession.setAttribute(name, obj);
        } else if (null != this.session) {
            // Groupware mode
            this.session.setParameter(name, obj);
        }
    }
    
    public Object getParameter(final String name) {
        if (null != this.httpSession) {
            // Admin mode
            return this.httpSession.getAttribute(name);
        } else if (null != this.session) {
            // Groupware mode
            return this.session.getParameter(name);
        }
        return null;
    }

    public void removeParameter(final String name) {
        if (null != this.httpSession) {
            // Admin mode
            this.httpSession.removeAttribute(name);
        } else if (null != this.session) {
            // Groupware mode
            // TODO: how to remove a Parameter in Groupware session

        }
    }

}

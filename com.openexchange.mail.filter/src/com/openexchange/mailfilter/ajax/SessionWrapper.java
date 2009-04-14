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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mailfilter.ajax;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Login;
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
 */
public class SessionWrapper {

    private static final Log LOG = LogFactory.getLog(SessionWrapper.class);

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
        
        private final int contextid;

        private final boolean b_contextid;

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
            b_contextid = true;
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
            contextid = -1;
            b_contextid = false;
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
            if (null == this.username) {
                return this.authname;
            }
            return this.username;
        }

        /**
         * Gets the string value of context ID if a context ID is present; otherwise "unknown" is returned
         * 
         * @return The string value of context ID if a context ID is present; otherwise "unknown" is returned
         */
        public final String getContextString() {
            if (!b_contextid) {
                return "unknown";
            }
            return String.valueOf(contextid);
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
        final String cookieId = req.getParameter(AJAXServlet.PARAMETER_SESSION);
        final Cookie[] cookies = req.getCookies();
        for (final Cookie cookie : cookies) {
            if (null != username && cookie.getName().startsWith("JSESSION")) {
                // admin mode
                this.httpSession = req.getSession(false);
                if (null != this.httpSession) {
                    this.httpSession.setAttribute(USERNAME_SESSION, username);
                    return;
                }
            } else if (null == username && null != cookieId && new StringBuilder(Login.COOKIE_PREFIX).append(cookieId).toString().equals(cookie.getName())) {
                // groupware mode
                final SessiondService service = MailFilterServletServiceRegistry.getServiceRegistry().getService(SessiondService.class);
                if (null == service) {
                    throw new SessiondException(new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE));
                }
                this.session = service.getSession(cookie.getValue());
                if (null != this.session) {
                    return;
                } else {
                    LOG.warn("Found cookie but not matching session. " + cookie.getName() + ':' + cookie.getValue());
                }
            } else if (cookie.getName().startsWith(Login.COOKIE_PREFIX)) {
                LOG.warn("Found cookie with matching prefix but invalid secret. " + cookie.getName() + ':' + cookie.getValue());
            }
        }
        throw new OXMailfilterException(OXMailfilterException.Code.SESSION_EXPIRED, "Can't find session.");
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

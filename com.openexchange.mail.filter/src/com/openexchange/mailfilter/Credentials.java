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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mailfilter;

import javax.security.auth.Subject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mailfilter.services.Services;
import com.openexchange.session.Session;

/**
 * This class holds the credentials to login into the imap server.
 */
public class Credentials {

    private String username;

    private String authname;

    private String password;

    private final int userid;

    private final int contextid;

    private final boolean b_contextid;

    private final Subject subject;
    
    /**
     * Initializes a new {@link Credentials} out of a {@link ServerSession}
     * 
     * @param session ServerSession
     */
    public Credentials(final Session session) {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        final String credsrc = config.getProperty(MailFilterProperties.Values.SIEVE_CREDSRC.property);
        if (MailFilterProperties.CredSrc.SESSION_FULL_LOGIN.name.equals(credsrc)) {
            authname = session.getLogin();
        } else {
            authname = session.getLoginName();
        }
        password = session.getPassword();
        userid = session.getUserId();
        contextid = session.getContextId();
        subject = (Subject) session.getParameter("kerberosSubject");
        username = null;
        b_contextid = true;
    }

    /**
     * @param authname The user name for authentication.
     * @param password The password.
     * @param userid The session users user id.
     * @param contextid The session users context id.
     */
    public Credentials(final String authname, final String password, final int userid, final int contextid) {
        this(authname, password, userid, contextid, null);
    }

    /**
     * @param authname The user name for authentication.
     * @param password The password.
     * @param userid The session users user id.
     * @param contextid The session users context id.
     * @param username The user name of the effected user which configuration is beeing touched.
     */
    public Credentials(final String authname, final String password, final int userid, final int contextid, final String username) {
        this(authname, password, userid, contextid, username, null);
    }

    public Credentials(final String authname, final String password, final int userid, final int contextid, final String username, final Subject subject) {
        super();
        this.authname = authname;
        this.password = password;
        this.userid = userid;
        this.contextid = contextid;
        this.username = username;
        b_contextid = true;
        this.subject = subject;
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

    /**
     * Gets the subject
     * 
     * @return The subject
     */
    public Subject getSubject() {
        return subject;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Username: " + this.username;
    }

}

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
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.mailfilter.properties.CredentialSource;
import com.openexchange.mailfilter.properties.MailFilterProperty;
import com.openexchange.mailfilter.services.Services;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * This class holds the credentials to login into the SIEVE server.
 */
public class Credentials {

    private static final String SESSION_FULL_LOGIN = CredentialSource.SESSION_FULL_LOGIN.name;

    private String username;
    private String authname;
    private String password;
    private final int userid;
    private final int contextid;
    private final boolean b_contextid;
    private final Subject kerberosSubject;
    private final String oauthToken;

    /**
     * Initializes a new {@link Credentials} out of a {@link ServerSession}
     *
     * @param session ServerSession
     */
    public Credentials(Session session) {
        super();
        LeanConfigurationService config = Services.getService(LeanConfigurationService.class);
        String credsrc = config.getProperty(session.getUserId(), session.getContextId(), MailFilterProperty.credentialSource);
        authname = SESSION_FULL_LOGIN.equals(credsrc) ? session.getLogin() : session.getLoginName();

        password = session.getPassword();
        userid = session.getUserId();
        contextid = session.getContextId();
        kerberosSubject = (Subject) session.getParameter("kerberosSubject");
        oauthToken = (String) session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN);
        username = null;
        b_contextid = true;
    }

    /**
     * @param authname The user name for authentication.
     * @param password The password.
     * @param userid The session users user id.
     * @param contextid The session users context id.
     */
    public Credentials(String authname, String password, int userid, int contextid) {
        this(authname, password, userid, contextid, null);
    }

    /**
     * @param authname The user name for authentication.
     * @param password The password.
     * @param userid The session users user id.
     * @param contextid The session users context id.
     * @param username The user name of the effected user which configuration is being touched.
     */
    public Credentials(String authname, String password, int userid, int contextid, String username) {
        this(authname, password, userid, contextid, username, null, null);
    }

    /**
     *
     * Initializes a new {@link Credentials}.
     *
     * @param authname The user name for authentication.
     * @param password The password.
     * @param userid The session users user id.
     * @param contextid The session users context id.
     * @param username The user name of the effected user which configuration is being touched.
     * @param kerberosSubject The Kerberos subject
     * @param oauthToken The oauth token
     */
    public Credentials(String authname, String password, int userid, int contextid, String username, Subject kerberosSubject, String oauthToken) {
        super();
        this.authname = authname;
        this.password = password;
        this.userid = userid;
        this.contextid = contextid;
        this.username = username;
        b_contextid = true;
        this.kerberosSubject = kerberosSubject;
        this.oauthToken = oauthToken;
    }

    /**
     * @return the user name
     */
    public final String getUsername() {
        return username;
    }

    /**
     * @param username the user name to set
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
     * @return the auth-name
     */
    public final String getAuthname() {
        return authname;
    }

    /**
     * @param authname the auth-name to set
     */
    public final void setAuthname(final String authname) {
        this.authname = authname;
    }

    /**
     * @return the user identifier
     */
    public final int getUserid() {
        return userid;
    }

    /**
     * @return the context identifier
     */
    public final int getContextid() {
        return contextid;
    }

    /**
     * This method returns the right user name.
     * <p>
     * If user name is <code>null</code> the {@link #getAuthname() auth-name} is returned; otherwise the user name
     */
    public final String getRightUsername() {
        String username = this.username;
        return null == username ? authname : username;
    }

    /**
     * Gets the string value of context ID if a context ID is present; otherwise "unknown" is returned
     *
     * @return The string value of context ID if a context ID is present; otherwise "unknown" is returned
     */
    public final String getContextString() {
        return b_contextid ? String.valueOf(contextid) : "unknown";
    }

    /**
     * Gets the optional Kerberos subject
     *
     * @return The Kerberos subject or <code>null</code> if absent
     */
    public Subject getSubject() {
        return kerberosSubject;
    }

    /**
     * Gets the OAuth token
     *
     * @return The OAuth token or <code>null</code> if absent
     */
    public String getOauthToken() {
        return oauthToken;
    }

    @Override
    public String toString() {
        return "Username: " + getRightUsername();
    }

}

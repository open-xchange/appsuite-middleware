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

package com.openexchange.login.internal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.authentication.Cookie;
import com.openexchange.authentication.Header;
import com.openexchange.authentication.ResultCode;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;

/**
 * {@link LoginResultImpl} - The {@link LoginResult} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LoginResultImpl implements LoginResult {

    private LoginRequest request;
    private Context context;
    private User user;
    private Session session;
    private final List<OXException> warnings;
    private ResultCode code;
    private String redirect;
    private Cookie[] cookies;
    private Header[] headers;
    private String serverToken;

    /**
     * Initializes a new empty {@link LoginResultImpl}.
     */
    public LoginResultImpl() {
        super();
        warnings = new LinkedList<OXException>();
    }

    /**
     * Initializes a new {@link LoginResultImpl}.
     *
     * @param session The session
     * @param context The resolved context
     * @param user The resolved user
     */
    public LoginResultImpl(final Session session, final Context context, final User user) {
        super();
        this.session = session;
        this.context = context;
        this.user = user;
        warnings = new LinkedList<OXException>();
    }

    @Override
    public LoginRequest getRequest() {
        return request;
    }

    /**
     * Sets the login request.
     *
     * @param request The login request
     */
    public void setRequest(final LoginRequest request) {
        this.request = request;
    }

    @Override
    public Context getContext() {
        return context;
    }

    /**
     * Sets the context.
     *
     * @param context The context
     */
    public void setContext(final Context context) {
        this.context = context;
    }

    @Override
    public User getUser() {
        return user;
    }

    /**
     * Sets the user
     *
     * @param user The user
     */
    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public Session getSession() {
        return session;
    }

    /**
     * Sets the session.
     *
     * @param session The session
     */
    public void setSession(final Session session) {
        this.session = session;
    }

    /**
     * Sets the cookies.
     *
     * @param cookies The cookies
     */
    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    /**
     * Sets the headers
     *
     * @param headers The headers
     */
    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    @Override
    public Header[] getHeaders() {
        return this.headers;
    }

    @Override
    public String getRedirect() {
        return redirect;
    }

    @Override
    public ResultCode getCode() {
        return code;
    }

    /**
     * Sets the redirect URI.
     *
     * @param redirect The redirect URI
     */
    public void setRedirect(final String redirect) {
        this.redirect = redirect;
    }

    /**
     * Sets the result code.
     *
     * @param code The result code
     */
    public void setCode(final ResultCode code) {
        this.code = code;
    }

    @Override
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    @Override
    public Collection<OXException> warnings() {
        return java.util.Collections.unmodifiableList(warnings);
    }

    @Override
    public void addWarning(final OXException warning) {
        warning.setCategory(Category.CATEGORY_WARNING);
        warnings.add(warning);
    }

    @Override
    public void addWarnings(final Collection<? extends OXException> warnings) {
        for (final OXException warning : warnings) {
            warning.setCategory(Category.CATEGORY_WARNING);
        }
        this.warnings.addAll(warnings);
    }

    @Override
    public String getServerToken() {
        return serverToken;
    }

    public void setServerToken(String serverToken) {
        this.serverToken = serverToken;
    }
}

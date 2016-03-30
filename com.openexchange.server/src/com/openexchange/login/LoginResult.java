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

package com.openexchange.login;

import java.util.Collection;
import com.openexchange.authentication.Cookie;
import com.openexchange.authentication.Header;
import com.openexchange.authentication.ResultCode;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;

/**
 * {@link LoginResult} - Offers information about a performed login.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface LoginResult {

    /**
     * Get the cookies which should be set on the response
     *
     * @return A {@link Cookie} object or null if the underlying implementation did not provide any cookies to set
     */
    Cookie[] getCookies();

    /**
     * Get the headers which should be set on the response
     *
     * @return A {@link Header} object of null if the underlying implementation did not provide any headers to set
     */
    Header[] getHeaders();

    /**
     * If the {@link ResultCode} indicates a {@link ResultCode#REDIRECT} this value will be used to redirect the browser to.
     *
     * @return the URL to redirect the browser to in case of {@link ResultCode#REDIRECT}.
     */
    String getRedirect();

    /**
     * A code indicating the result of the {@link AuthenticationService} or the {@link AutoLoginAuthenticationService} when the returned
     * object implements the {@link ResponseEnhancement}.
     *
     * @return the {@link ResultCode} provided from the {@link AuthenticationService} or the {@link AutoLoginAuthenticationService}.
     */
    ResultCode getCode();

    /**
     * Remembers the according {@link LoginRequest login request}
     */
    LoginRequest getRequest();

    /**
     * Gets the {@link Session session} associated with this login.
     *
     * @return The session associated with this login.
     */
    Session getSession();

    /**
     * Gets the resolved {@link Context context}.
     *
     * @return The resolved context.
     */
    Context getContext();

    /**
     * Gets the resolved {@link User user}.
     *
     * @return The resolved user.
     */
    User getUser();

    /**
     * Checks if this result has warnings.
     *
     * @return <code>true</code> if this result has warnings; otherwise <code>false</code>
     */
    boolean hasWarnings();

    /**
     * Gets the warnings contained in this result.
     * <p>
     * Modifying methods are <b>NOT</b> supported by returned {@link Collection}!
     *
     * @return The (possibly empty) warnings
     */
    Collection<OXException> warnings();

    /**
     * Adds specified warning to this result.
     *
     * @param warning The warning to add
     */
    void addWarning(OXException warning);

    /**
     * Adds specified warnings to this result.
     *
     * @param warnings The warnings to add
     */
    void addWarnings(Collection<? extends OXException> warnings);

    /**
     * This method only returns not <code>null</code> if the token login request was used. Otherwise there will be no server side token.
     *
     * @return the server side token of the token login mechanism.
     */
    String getServerToken();
}

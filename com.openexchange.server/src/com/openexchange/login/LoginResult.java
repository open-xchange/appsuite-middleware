/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.login;

import java.util.Collection;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.Cookie;
import com.openexchange.authentication.Header;
import com.openexchange.authentication.ResponseEnhancement;
import com.openexchange.authentication.ResultCode;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.user.User;

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

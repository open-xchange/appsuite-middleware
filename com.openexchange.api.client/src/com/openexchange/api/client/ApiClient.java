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

package com.openexchange.api.client;

import java.net.URL;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;

/**
 * {@link ApiClient} - A client for communicating to another Open Xchange server.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public interface ApiClient {

    /**
     * Gets the context identifier that belongs to the local server
     *
     * @return The local context ID
     */
    int getContextId();

    /**
     * Gets the user identifier that belongs to the local server
     *
     * @return The local user ID
     */
    int getUserId();

    /**
     * Gets the session identifier that belongs to the local server
     *
     * @return The local user session ID
     */
    String getSession();

    /**
     * Gets the link to the login or rather the URL for the remote App Suite instance
     *
     * @return The initial link, e.g. <code>share.appsuite.example.org/share/1123</code>
     */
    URL getLoginLink();

    /**
     * Gets the {@link Credentials}
     *
     * @return The credentials. If not specified by the caller {@link Credentials#EMPTY} is returned
     */
    @NonNull
    Credentials getCredentials();

    /**
     * Gets information that were acquired during login
     *
     * @return The information or <code>null</code> if the client is not yet logged in
     */
    @Nullable
    LoginInformation getLoginInformation();

    /**
     * Performs a login on the remote system
     *
     * @throws OXException in case of error, specifically a {@link ApiClientExceptions#NO_ACCESS} if the login fails
     */
    void login() throws OXException;

    /**
     * Performs a logout on the remote system.
     * <p>
     * <b>Note:</b> Any error during the logout process will be logged
     */
    void logout();

    /**
     * Gets a value indicating whether this client is closed, meaning if it can serve requests or not.
     * <p>
     * If the client is closed, any call of {@link #execute(ApiCall)} will fail
     * with an exception.
     *
     * @return <code>true</code> if the client has been closed, <code>false</code> otherwise
     */
    boolean isClosed();

    /**
     * Executes the given call using the parser provided with it.
     *
     * @param <T> The type of the return value defined by the call's parser
     * @param call The {@link ApiCall} to execute
     * @return The parsed object
     * @throws OXException In case request can't be executed, parsing fails or client is closed
     */
    <T> T execute(ApiCall<T> call) throws OXException;
}

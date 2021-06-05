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

package com.openexchange.login.listener;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;

/**
 * {@link LoginListener} - A login listener, which receives call-backs for different phases of the login operation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface LoginListener {

    /**
     * Invoked during login operation right before actual authentication is performed.
     *
     * @param request The associated login request
     * @param properties The arbitrary properties; e.g. <code>"headers"</code> or <code>{@link com.openexchange.authentication.Cookie "cookies"}</code>
     * @throws OXException If this call-back renders the login attempt as invalid
     */
    void onBeforeAuthentication(LoginRequest request, Map<String, Object> properties) throws OXException;

    /**
     * Invoked during login operation in case authentication succeeded.
     *
     * @param result The login result
     * @throws OXException If this call-back renders the login attempt as invalid
     */
    void onSucceededAuthentication(LoginResult result) throws OXException;

    /**
     * Invoked during login operation in case authentication failed.
     *
     * @param request The associated login request
     * @param properties The arbitrary properties; e.g. <code>"headers"</code> or <code>{@link com.openexchange.authentication.Cookie "cookies"}</code>
     * @param e The optional exception rendering the login attempt as invalid; e.g. <code>null</code> in case authentication service signaled <code>ResultCode.FAILED</code>
     * @throws OXException If this call-back is supposed to signal a different error
     */
    void onFailedAuthentication(LoginRequest request, Map<String, Object> properties, OXException e) throws OXException;

    /**
     * Invoked during login operation in case authentication gets redirected to another end-point.
     *
     * @param request The associated login request
     * @param properties The arbitrary properties; e.g. <code>"headers"</code> or <code>{@link com.openexchange.authentication.Cookie "cookies"}</code>
     * @param e The optional exception rendering the login attempt as redirected; e.g. <code>null</code> in case authentication service signaled <code>ResultCode.REDIRECT</code>
     * @throws OXException If this call-back is supposed to signal a different error
     */
    void onRedirectedAuthentication(LoginRequest request, Map<String, Object> properties, OXException e) throws OXException;

}

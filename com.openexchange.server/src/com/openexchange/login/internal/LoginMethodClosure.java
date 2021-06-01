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

package com.openexchange.login.internal;

import com.openexchange.authentication.Authenticated;
import com.openexchange.exception.OXException;

/**
 * Closure interface for the different login methods.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface LoginMethodClosure {

    /**
     * Performs a login using currently available authentication service.
     *
     * @param loginResult The current login result used to pass login request information; such as headers
     * @return The resolved login information for the context as well as for the user; even <code>null</code> for auto-login if <code>LoginExceptionCodes.NOT_SUPPORTED</code> happens
     * @throws OXException If login attempt fails
     */
    Authenticated doAuthentication(LoginResultImpl loginResult) throws OXException;

}

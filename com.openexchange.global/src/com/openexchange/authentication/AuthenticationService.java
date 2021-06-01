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

package com.openexchange.authentication;

import javax.security.auth.login.LoginException;
import com.openexchange.exception.OXException;

/**
 * This interface defines the methods for handling the login information. E.g. the login information <code>user@domain.tld</code> is split
 * into <code>user</code> and <code>domain.tld</code> and the context part will be used to resolve the context while the user part will be
 * used to authenticate the user.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface AuthenticationService {

    /**
     * This method maps the login information from the login screen to the both parts needed to resolve the context and the user of that
     * context.
     *
     * @param loginInfo the complete login information from the login screen.
     * @return an {@link Authenticated} containing context information to resolve the context and user information to resolve the user.
     * This return type can be enhanced with {@link SessionEnhancement} and/or {@link ResponseEnhancement}.
     * @throws OXException If something with the login info is wrong.
     */
    Authenticated handleLoginInfo(LoginInfo loginInfo) throws OXException;

    /**
     * This method authenticates a user using a global web services session which is useful in single sign on scenarios. If no such global
     * web services session exists either throw a {@link LoginException} or redirect the browser to some global login site with
     * {@link ResultCode#REDIRECT}. This method should never return <code>null</code>.
     *
     * If the implementing authentication bundle does not support some global web services single sign on this method has to throw
     * {@link LoginExceptionCodes#NOT_SUPPORTED}.
     *
     * @param loginInfo the complete login information from the autologin request. It does never contain login and password.
     * @return an {@link Authenticated} containing context information to resolve the context and user information to resolve the user.
     * This return type can be enhanced with {@link SessionEnhancement} and/or {@link ResponseEnhancement}.
     * @throws OXException if something with the login info is wrong and no {@link Authenticated} can be returned.
     */
    Authenticated handleAutoLoginInfo(LoginInfo loginInfo) throws OXException;

}

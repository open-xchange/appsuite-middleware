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

package com.openexchange.authentication.driver.basic.database;

import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationDriver;
import com.openexchange.authentication.BasicAuthenticationService;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.exception.OXException;

/**
 * This implementation authenticates the user against the database.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DatabaseAuthenticationDriver implements AuthenticationDriver {

    private final BasicAuthenticationService basicAuthService;

    /**
     * Default constructor.
     */
    public DatabaseAuthenticationDriver(BasicAuthenticationService basicAuthService) {
        super();
        this.basicAuthService = basicAuthService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticated handleLoginInfo(final LoginInfo loginInfo) throws OXException {
        return basicAuthService.handleLoginInfo(loginInfo);
    }

    @Override
    public Authenticated handleAutoLoginInfo(LoginInfo loginInfo) throws OXException {
        return basicAuthService.handleAutoLoginInfo(loginInfo);
    }

    @Override
    public String getId() {
        return "database";
    }

}

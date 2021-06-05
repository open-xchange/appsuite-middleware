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

package com.openexchange.login.multifactor;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.listener.AutoLoginAwareLoginListener;
import com.openexchange.session.Session;

/**
 * {@link MultifactorAutoLoginAwareListener}
 * Handle any session cleanup required when autologin called and user has Multifactor authentication
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.1
 */
public class MultifactorAutoLoginAwareListener implements AutoLoginAwareLoginListener {

    @Override
    public void onBeforeAuthentication(LoginRequest request, Map<String, Object> properties) throws OXException {
        // nothing to do

    }

    @Override
    public void onSucceededAuthentication(LoginResult result) throws OXException {
        // nothing to do

    }

    @Override
    public void onFailedAuthentication(LoginRequest request, Map<String, Object> properties, OXException e) throws OXException {
        // nothing to do

    }

    @Override
    public void onRedirectedAuthentication(LoginRequest request, Map<String, Object> properties, OXException e) throws OXException {
        // nothing to do

    }

    @Override
    public void onSucceededAutoLogin(LoginResult result) throws OXException {
        // Remove the session parameter stating that the user authorized this session
        result.getSession().setParameter(Session.MULTIFACTOR_LAST_VERIFIED, null);
    }
}

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

package com.openexchange.api.client.impl;

import java.net.URL;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.Credentials;
import com.openexchange.api.client.LoginInformation;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link NoSessionClient}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class NoSessionClient extends AbstractApiClient {

    /**
     * Initializes a new {@link NoSessionClient}.
     *
     * @param services The services
     * @param contextId The context identifier of this local OX node
     * @param userId The user identifier of this local OX node
     * @param sessionId The user identifier of this local OX node
     * @param loginLink The link to the target to log in into
     */
    public NoSessionClient(ServiceLookup services, int contextId, int userId, String sessionId, URL loginLink) {
        super(services, contextId, userId, sessionId, loginLink);
    }

    @Override
    @NonNull
    public Credentials getCredentials() {
        return Credentials.EMPTY;
    }

    @Override
    @Nullable
    public LoginInformation getLoginInformation() {
        return null;
    }

    @Override
    protected void doLogin() throws OXException {
        // No-op
    }

}

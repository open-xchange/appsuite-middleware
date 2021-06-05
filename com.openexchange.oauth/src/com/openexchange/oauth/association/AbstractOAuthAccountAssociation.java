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

package com.openexchange.oauth.association;

import com.openexchange.exception.OXException;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link AbstractOAuthAccountAssociation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractOAuthAccountAssociation implements OAuthAccountAssociation {

    private final int accountId;
    private final int userId;
    private final int contextId;

    /**
     * Initializes a new {@link AbstractOAuthAccountAssociation}.
     *
     * @param accountId The identifier of the OAuth account
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    protected AbstractOAuthAccountAssociation(int accountId, int userId, int contextId) {
        super();
        this.accountId = accountId;
        this.userId = userId;
        this.contextId = contextId;
    }

    /**
     * Initializes and returns a new {@link AbstractOAuthAccess}
     *
     * @param session The session
     * @return The new OAuth access
     * @throws OXException If the OAuth access cannot be initialized or any other error occurs
     */
    protected abstract AbstractOAuthAccess newAccess(Session session) throws OXException;

    @Override
    public Status getStatus(Session session) throws OXException {
        AbstractOAuthAccess access = newAccess(session);
        try {
            access.initialize();
        } catch (@SuppressWarnings("unused") OXException e) {
            return Status.RECREATION_NEEDED;
        }
        boolean success = access.ping();
        if (success) {
            return Status.OK;
        }
        return Status.INVALID_GRANT;
    }

    @Override
    public int getOAuthAccountId() {
        return accountId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int getContextId() {
        return contextId;
    }
}

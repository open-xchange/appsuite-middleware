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

package com.openexchange.external.account;

/**
 * {@link DefaultExternalAccount}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class DefaultExternalAccount implements ExternalAccount {

    private static final long serialVersionUID = -3140622819766792644L;

    private final int id;
    private final int contextId;
    private final int userId;
    private final String providerId;
    private final ExternalAccountModule module;

    /**
     * Initializes a new {@link DefaultExternalAccount}.
     * 
     * @param id The external account's identifier
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param providerId The provider's identifier
     * @param module The {@link ExternalAccountModule}
     */
    public DefaultExternalAccount(int id, int contextId, int userId, String providerId, ExternalAccountModule module) {
        super();
        this.id = id;
        this.contextId = contextId;
        this.userId = userId;
        this.providerId = providerId;
        this.module = module;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public ExternalAccountModule getModule() {
        return module;
    }
}

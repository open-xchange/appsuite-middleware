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

package com.openexchange.file.storage;

/**
 * {@link DefaultAdministrativeFileStorageAccount}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class DefaultAdministrativeFileStorageAccount implements AdministrativeFileStorageAccount {

    private final int contextId;
    private final int userId;
    private final int id;
    private final String serviceId;

    /**
     * Initializes a new {@link DefaultAdministrativeFileStorageAccount}.
     * 
     * @param contextId the context identifier
     * @param userId The user identifier
     * @param id The account identifier
     * @param serviceId The service identifier
     */
    public DefaultAdministrativeFileStorageAccount(int contextId, int userId, int id, String serviceId) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        this.id = id;
        this.serviceId = serviceId;
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
    public int getId() {
        return id;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }
}

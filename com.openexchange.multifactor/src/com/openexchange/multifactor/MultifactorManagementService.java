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

package com.openexchange.multifactor;

import java.util.Collection;
import com.openexchange.exception.OXException;

/**
 * {@link MultifactorManagementService} - a management service for configuring multifactor devices
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public interface MultifactorManagementService {

    /**
     * Returns a set of devices for a given user
     *
     * @param contextId The context-ID of the user
     * @param userId The ID of the user
     * @return A set of multifactor devices for the given user or an empty array if there is no device for the user
     * @throws OXException
     */
    public Collection<MultifactorDevice> getMultifactorDevices(int contextId, int userId) throws OXException;

    /**
     * Removes a specific multifactor device for a given user
     *
     * @param contextId The context-ID of the user
     * @param userId The ID of the user
     * @param providerName The name of the provider to delete the device for
     * @param deviceId The ID of the device to remove
     * @throws OXException due an error
     */
    public void removeDevice(int contextId, int userId, String providerName, String deviceId) throws OXException;

    /**
     * Removes all multifactor devices for a given user and provider
     *
     * @param contextId The context-ID of the user
     * @param userId The ID of the user
     * @throws OXException due an error
     */
    public void removeAllDevices(int contextId, int userId) throws OXException;
}

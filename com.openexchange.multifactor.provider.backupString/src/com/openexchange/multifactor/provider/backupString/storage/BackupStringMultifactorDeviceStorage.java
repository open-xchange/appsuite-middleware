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

package com.openexchange.multifactor.provider.backupString.storage;

import java.util.Collection;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.provider.backupString.BackupStringMultifactorDevice;

/**
 * {@link BackupStringMultifactorDeviceStorage}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public interface BackupStringMultifactorDeviceStorage {

    /**
     * Stores a new {@link MultifactorDevice} for the given user
     *
     * @param contextId the context ID
     * @param userId the user ID
     * @param device The device to store
     * @throws OXException
     */
    public void registerDevice(int contextId, int userId, BackupStringMultifactorDevice device) throws OXException;

    /**
     * Removes a device from the storage
     *
     * @param contextId the context ID
     * @param userId the user ID
     * @param deviceId The ID of the device to remove
     * @return <code>true</code>, if the device was removed, <code>false</code> if there was no device with the given ID
     * @throws OXException
     */
    public boolean unregisterDevice(int contextId, int userId, String deviceId) throws OXException;

    /**
     * Gets a collection of devices owned by the given user
     *
     * @param contextId The request to get all devices for
     * @param userId The request to get all devices for
     * @return A collection of devices for the given session, or an empty Collection
     * @throws OXException
     */
    public Collection<BackupStringMultifactorDevice> getDevices(int contextId, int userId) throws OXException;

    /**
     * Gets a specific device for the given user
     *
     * @param contextId The request to get all devices for
     * @param userId The request to get all devices for
     * @param deviceId The ID of the device to get
     * @return The device with the given ID or an empty Optional
     * @throws OXException
     */
    public Optional<BackupStringMultifactorDevice> getDevice(int userId, int cotextId, String deviceId) throws OXException;

    /**
     * Store a new name for a user's device
     *
     * @param contextId The request to get all devices for
     * @param userId The request to get all devices for
     * @param name New name for the device
     * @return <code>true</code>, if the device was renamed, <code>false</code> otherwise (not found)
     * @throws OXException
     */
    public boolean renameDevice(int userId, int cotextId, String deviceId, String name) throws OXException;

    /**
     * Deletes all BackupString devices for a user
     *
     * @param userId The id of the user
     * @param contextId The id of the context
     * @return <code>true</code> if devices have been deleted, <code>false</code> otherwise
     * @throws OXException
     */
    public boolean deleteAllForUser(int userId, int contextId) throws OXException;

    /**
     * Deletes all BackupStrings for a context
     *
     * @param contextId The id of the context
     * @return <code>true</code> if devices have been deleted, <code>false</code> otherwise
     * @throws OXException
     */
    public boolean deleteAllForContext(int contextId) throws OXException;

}

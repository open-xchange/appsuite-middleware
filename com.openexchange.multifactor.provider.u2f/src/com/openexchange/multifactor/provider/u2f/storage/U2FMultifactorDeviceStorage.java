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

package com.openexchange.multifactor.provider.u2f.storage;

import java.util.Collection;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.provider.u2f.impl.U2FMultifactorDevice;

/**
 * {@link U2FMultifactorDeviceStorage}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public interface U2FMultifactorDeviceStorage {

    /**
     * Stores a new {@link MultifactorDevice} for the given session
     *
     * @param contextId The context ID to register the device for
     * @param userId The user ID to register the device for
     * @param device The device to store
     * @throws OXException
     */
    public void registerDevice(int contextId, int userId, U2FMultifactorDevice device) throws OXException;

    /**
     * Removes a device from the storage
     *
     * @param contextId The context ID to remove the device for
     * @param userId The user ID to remove the device for
     * @param deviceId The ID of the device to remove
     * @return <code>true</code>, if the device was removed, <code>false</code> if there was no device with the given ID
     * @throws OXException
     */
    public boolean unregisterDevice(int contextId, int userId, String deviceId) throws OXException;

    /**
     * Gets a collection of devices
     *
     * @param contextId The context ID to get the devices for
     * @param userId The user ID to get the devices for
     * @return A collection of devices for the given session, or an empty Collection
     * @throws OXException
     */
    public Collection<U2FMultifactorDevice> getDevices(int contextId, int userId) throws OXException;

    /**
     * Gets a specific device
     *
     * @param contextId The context ID to get the device for
     * @param userId The user ID to get the device for
     * @return The device with the given ID or an empty Optional
     * @throws OXException
     */
    public Optional<U2FMultifactorDevice> getDevice(int contextId, int userId, String deviceId) throws OXException;

    /**
     * Store a new name for a device
     *
     * @param contextId The context ID to rename the device for
     * @param userId The user ID to rename the device for
     * @param deviceId The device to be renamed
     * @param name  New name for the device
     * @return <code>true</code>, if the device was renamed, <code>false</code> otherwise (not found)
     * @throws OXException
     */
    public boolean renameDevice(int contextId, int userId, String deviceId, String name) throws OXException;

    /**
     * Increment authentication counter. Used for signature based devices
     *
     * @param contextId The context ID to increment the counter for
     * @param userId The user ID to increment the counter for
     * @param deviceId The id of the device
     * @param current the current counter value
     * @return <code>true</code> if the counter was successfully incremented, <code>false</code> otherwise
     * @throws OXException
     */
    boolean incrementCounter(int contextId, int userId, String deviceId, long current) throws OXException;

    /**
     * Get count of U2FDevices devices registered to the user
     *
     * @param contextId The context ID to get the count for
     * @param userId The user ID to get the count for
     * @return The count of U2Devices
     * @throws OXException
     */
    int getCount(int contextId, int userId) throws OXException;

    /**
     * Deletes all U2F registrations for a user
     *
     * @param userId The id of the user
     * @param contextId The id of the context
     * @return <code>true</code> if devices have been deleted, <code>false</code> otherwise
     * @throws OXException
     */
    boolean deleteAllForUser(int userId, int contextId) throws OXException;

    /**
     * Deletes all U2F registrations for a context
     *
     * @param contextId The id of the context
     * @return <code>true</code> if devices have been deleted, <code>false</code> otherwise
     * @throws OXException
     */
    boolean deleteAllForContext(int contextId) throws OXException;
}

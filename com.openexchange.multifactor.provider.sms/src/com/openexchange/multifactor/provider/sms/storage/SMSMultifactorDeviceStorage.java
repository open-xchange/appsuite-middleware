/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.multifactor.provider.sms.storage;

import java.util.Collection;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.provider.sms.SMSMultifactorDevice;

/**
 * {@link SMSMultifactorDeviceStorage}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public interface SMSMultifactorDeviceStorage {

    /**
     * Stores a new {@link MultifactorDevice} for the given session
     *
     * @param contextId The context ID to register the device for
     * @param userId The user ID to register the device for
     * @param device The device to store
     * @throws OXException
     */
    public void registerDevice(int contextId, int userId, SMSMultifactorDevice device) throws OXException;

    /**
     * Removes a device from the storage
     *
     * @param contextId The context ID to remove the device for
     * @param userId The user ID to remove the device for
     * @param deviceId The ID of the device to remove
     * @return true, if the device was removed, false if there was no device with the given ID
     * @throws OXException
     */
    public boolean unregisterDevice(int contextId, int userId, String deviceId) throws OXException;

    /**
     * Gets a collection of devices
     *
     * @param contextId The context ID to get the device for
     * @param userId The user ID to get the device for
     * @return A collection of devices for the given session, or an empty Collection
     * @throws OXException
     */
    public Collection<SMSMultifactorDevice> getDevices(int contextId, int userId) throws OXException;

    /**
     * Gets a specific device
     *
     * @param contextId The context ID to get the device for
     * @param userId The user ID to get the device for
     * @param deviceId The ID of the device to get
     * @return The device with the given ID or an empty Optional
     * @throws OXException
     */
    public Optional<SMSMultifactorDevice> getDevice(int contextId, int userId, String deviceId) throws OXException;

    /**
     * Store a new name for a device
     *
     * @param contextId The context ID to rename the device for
     * @param userId The user ID to rename the device for
     * @param deviceId The device to be renamed
     * @param name  New name for the device
     * @return <code>true</code> if the device was renamed, <code>false</code> otherwise (not found)
     * @throws OXException
     */
    public boolean renameDevice(int contextId, int userId, String deviceId, String name) throws OXException;

    /**
     * Gets a device by it's registered phone number
     *
     * @param contextId The context ID to get the device for
     * @param userId The user ID to get the device for
     * @param phoneNumber The phone number
     * @return The device with the given number or an empty Optional
     * @throws OXException
     */
    public Optional<SMSMultifactorDevice> getByPhoneNumber(int contextId, int userId, String phoneNumber) throws OXException;

    /**
     * Deletes all SMS device registrations for a user
     *
     * @param userId The id of the user
     * @param contextId The id of the context
     * @return <code>true</code> if devices have been deleted, <code>false</code> otherwise
     * @throws OXException
     */
    boolean deleteAllForUser(int userId, int contextId) throws OXException;

    /**
     * Deletes all SMS registrations for a context
     *
     * @param contextId The id of the context
     * @return <code>true</code> if devices have been deleted, <code>false</code> otherwise
     * @throws OXException
     */
    boolean deleteAllForContext(int contextId) throws OXException;
}

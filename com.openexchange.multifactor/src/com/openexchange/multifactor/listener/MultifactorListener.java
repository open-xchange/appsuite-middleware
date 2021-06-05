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

package com.openexchange.multifactor.listener;

import com.openexchange.exception.OXException;

/**
 * {@link MultifactorListener}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public interface MultifactorListener {

    /**
     * Event called after a multifactor authentication event
     *
     * @param userId The user id
     * @param contextId The context id
     * @param success True if the authentication was successful, false otherwise
     * @throws OXException
     */
    public void onAfterAuthentication(int userId, int contextId, boolean success) throws OXException;

    /**
     * Event called after a multifactor device has been removed
     *
     * @param userId The user id
     * @param contextId The context id
     * @param enabledDevices the current amount of enabled devices
     * @throws OXException
     */
    public void onAfterDelete(int userId, int contextId, int enabledDevices) throws OXException;

    /**
     * Event called after multifactor device added
     *
     * @param userId The user id
     * @param contextId The context id
     * @param enabledDevices the current amount of enabled devices
     * @throws OXException
     */
    public void onAfterAdd(int userId, int contextId, int enabledDevices) throws OXException;

}

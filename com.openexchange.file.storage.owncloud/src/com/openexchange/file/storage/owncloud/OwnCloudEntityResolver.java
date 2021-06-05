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

package com.openexchange.file.storage.owncloud;

import com.openexchange.exception.OXException;

/**
 * {@link OwnCloudEntityResolver} - resolves entity ids from owncloud to ox and vice versa
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public interface OwnCloudEntityResolver {

    /**
     * Gets the ox entity id for the given user or group 
     *
     * @param ocEntityId The owncloud user or group id
     * @param isGroup Whether the given id is a group id or not
     * @return The ox entity id
     * @throws OXException in case the owncloud entity couldn't be resolved
     */
    public int ocEntity2OXEntity(String ocEntityId, boolean isGroup) throws OXException;

    /**
     * Gets the oc entity id for the given user or group
     *
     * @param oxEntityId The ox entity id
     * @param isGroup Whether the given id is a group or not
     * @return The oc entity id
     * @throws OXException in case the ox entity couldn't be resolved
     */
    public String oxEntity2OCEntity(int oxEntityId, boolean isGroup) throws OXException;
    
}

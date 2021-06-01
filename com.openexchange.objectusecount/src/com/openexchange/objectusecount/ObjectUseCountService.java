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

package com.openexchange.objectusecount;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ObjectUseCountService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public interface ObjectUseCountService {

    /**
     * Get use count for object
     *
     * @param session The associated session
     * @param folder The identifier of the folder in which the object resides
     * @param objectId The identifier of the object
     * @return The object's use count
     * @throws OXException If use count cannot be returned
     */
    int getObjectUseCount(Session session, int folder, int objectId) throws OXException;

    /**
     * Get use count for object
     *
     * @param session The associated session
     * @param folder The identifier of the folder in which the object resides
     * @param objectId The identifier of the object
     * @param con An existing connection to database or <code>null</code> to fetch a new one
     * @return The object's use count
     * @throws OXException If use count cannot be returned
     */
    int getObjectUseCount(Session session, int folder, int objectId, Connection con) throws OXException;

    /**
     * Sets use count according to specified arguments
     *
     * @param session The associated session
     * @param arguments The arguments determining how/what to set
     * @throws OXException If setting user count(s) fails and arguments signal to throw an error
     */
    void setObjectUseCount(Session session, SetArguments arguments) throws OXException;

    /**
     * Increments the use count(s) according to specified arguments
     *
     * @param session The associated session
     * @param arguments The arguments determining how/what to update
     * @throws OXException If incrementing user count(s) fails and arguments signal to throw an error
     */
    void incrementObjectUseCount(Session session, IncrementArguments arguments) throws OXException;

    /**
     * Reset use count for object
     *
     * @param session The associated session
     * @param folder The identifier of the folder in which the object resides
     * @param objectId The identifier of the object
     * @throws OXException If reset operation fails
     */
    void resetObjectUseCount(Session session, int folder, int objectId) throws OXException;

    /**
     * Reset use count for object
     *
     * @param session The associated session
     * @param folder The identifier of the folder in which the object resides
     * @param objectId The identifier of the object
     * @param con A writable connection to database or <code>null</code> to fetch a new one
     * @throws OXException If reset operation fails
     */
    void resetObjectUseCount(Session session, int folder, int objectId, Connection con) throws OXException;

}

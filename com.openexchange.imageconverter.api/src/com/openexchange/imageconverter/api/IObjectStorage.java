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

package com.openexchange.imageconverter.api;

import java.io.InputStream;
import java.util.Set;

/**
 * {@link IObjectStorage}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v8.0.0
 */
public interface IObjectStorage {

    /**
     * @return
     */
    default int getId() {
        return 0;
    }

    /**
     * @param inputStm
     * @return
     * @throws Exception
     */
    String createObject(InputStream inputStm) throws Exception;

    /**
     * @param objectId
     * @throws Exception
     */
    void deleteObject(String objectId) throws Exception;

    /**
     * @param objectIds
     * @return
     * @throws Exception
     */
    Set<String> deleteObjects(String[] objectIds) throws Exception;

    /**
     * @param objectId
     * @return
     * @throws Exception
     */
    InputStream getObject(String objectId) throws Exception;

    /**
     * @param objectId
     * @param inputStm
     * @param offset
     * @return
     * @throws Exception
     */
    long appendToObject(String objectId, InputStream inputStm, long offset) throws Exception;

    /**
     * @param objectId
     * @return
     * @throws Exception
     */
    long getObjectSize(String objectId) throws Exception;

    /**
     * @param objectId
     * @param size
     * @throws Exception
     */
    void setObjectSize(String objectId, long size) throws Exception;
}

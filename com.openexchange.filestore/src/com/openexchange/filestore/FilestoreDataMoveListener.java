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

package com.openexchange.filestore;

import java.net.URI;
import com.openexchange.exception.OXException;

/**
 * {@link FilestoreDataMoveListener} - Receives call-backs for various file storage move operations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface FilestoreDataMoveListener {

    /**
     * Invoked before data is about being moved from context-associated file storage to another one.
     *
     * @param contextId The context identifier
     * @param srcUri The URI for the source file storage
     * @param dstUri The URI for the destination file storage
     * @throws OXException If listener signals that the move should not occur
     */
    void onBeforeContextDataMove(int contextId, URI srcUri, URI dstUri) throws OXException;

    /**
     * Invoked in case data was moved from context-associated file storage to a user-associated one.
     *
     * @param contextId The context identifier
     * @param srcUri The URI for the source file storage
     * @param dstUri The URI for the destination file storage
     */
    void onAfterContextDataMoved(int contextId, URI srcUri, URI dstUri);



    /**
     * Invoked before data is about being moved from user-associated file storage to another one.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param srcUri The URI for the source file storage
     * @param dstUri The URI for the destination file storage
     * @throws OXException If listener signals that the move should not occur
     */
    void onBeforeUserDataMove(int contextId, int userId, URI srcUri, URI dstUri) throws OXException;

    /**
     * Invoked in case data was moved from user-associated file storage to a user-associated one.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param srcUri The URI for the source file storage
     * @param dstUri The URI for the destination file storage
     */
    void onAfterUserDataMoved(int contextId, int userId, URI srcUri, URI dstUri);



    /**
     * Invoked before data is about being moved from context-associated file storage to a user-associated one.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param srcContextStorage The source file storage associated with given context
     * @param dstUserStorage The destination file storage associated with given user
     * @throws OXException If listener signals that the move should not occur
     */
    void onBeforeContextToUserDataMove(int contextId, int userId, QuotaFileStorage srcContextStorage, QuotaFileStorage dstUserStorage) throws OXException;

    /**
     * Invoked in case data was moved from context-associated file storage to a user-associated one.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param srcContextStorage The source file storage associated with given context
     * @param dstUserStorage The destination file storage associated with given user
     */
    void onAfterContextToUserDataMoved(int contextId, int userId, QuotaFileStorage srcContextStorage, QuotaFileStorage dstUserStorage);



    /**
     * Invoked before data is about being moved from user-associated file storage to a context-associated one.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param srcUserStorage The source file storage associated with given user
     * @param dstContextStorage The destination file storage associated with given context
     * @throws OXException If listener signals that the move should not occur
     */
    void onBeforeUserToContextDataMove(int contextId, int userId, QuotaFileStorage srcUserStorage, QuotaFileStorage dstContextStorage) throws OXException;

    /**
     * Invoked in case data was moved from user-associated file storage to a context-associated one.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param srcUserStorage The source file storage associated with given user
     * @param dstContextStorage The destination file storage associated with given context
     */
    void onAfterUserToContextDataMoved(int contextId, int userId, QuotaFileStorage srcUserStorage, QuotaFileStorage dstContextStorage);



    /**
     * Invoked before data is about being moved from user-associated file storage to a master-associated one.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param masterId The master identifier
     * @param srcUserStorage The source file storage associated with given user
     * @param dstMasterStorage The destination file storage associated with given master
     * @throws OXException If listener signals that the move should not occur
     */
    void onBeforeUserToMasterDataMove(int contextId, int userId, int masterId, QuotaFileStorage srcUserStorage, QuotaFileStorage dstMasterStorage) throws OXException;

    /**
     * Invoked in case data was moved from user-associated file storage to a master-associated one.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param masterId The master identifier
     * @param srcUserStorage The source file storage associated with given user
     * @param dstMasterStorage The destination file storage associated with given master
     */
    void onAfterUserToMasterDataMoved(int contextId, int userId, int masterId, QuotaFileStorage srcUserStorage, QuotaFileStorage dstMasterStorage);



    /**
     * Invoked before data is about being moved from master-associated file storage to a user-associated one.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param masterId The master identifier
     * @param srcMasterStorage The source file storage associated with given master
     * @param dstUserStorage The destination file storage associated with given user
     * @throws OXException If listener signals that the move should not occur
     */
    void onBeforeMasterToUserDataMove(int contextId, int userId, int masterId, QuotaFileStorage srcMasterStorage, QuotaFileStorage dstUserStorage) throws OXException;

    /**
     * Invoked in case data was moved from master-associated file storage to a user-associated one.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param masterId The master identifier
     * @param srcMasterStorage The source file storage associated with given master
     * @param dstUserStorage The destination file storage associated with given user
     */
    void onAfterMasterToUserDataMoved(int contextId, int userId, int masterId, QuotaFileStorage srcMasterStorage, QuotaFileStorage dstUserStorage);

}

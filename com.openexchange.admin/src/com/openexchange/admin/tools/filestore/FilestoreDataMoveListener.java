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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.admin.tools.filestore;

import java.net.URI;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.QuotaFileStorage;

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
     */
    void onBeforeMasterToUserDataMove(int contextId, int userId, int masterId, QuotaFileStorage srcMasterStorage, QuotaFileStorage dstUserStorage);

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

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

package com.openexchange.tools.oxfolder.property;

import java.sql.Connection;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UsedForSync;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * The {@link FolderSubscriptionHelper} is able to check and set subscription and usedForSync status of pim folders.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
@SingletonService
public interface FolderSubscriptionHelper {

    /**
     * Checks if the folder has any subscriptions.
     *
     * @param optCon The optional {@link Connection} to use
     * @param ctxId The context identifier
     * @param userId The user identifier
     * @param folderId The folder identifier
     * @param module The module identifier
     * @return The optional subscription value
     * @throws OXException In case the subscription couldn't be loaded
     */
    Optional<Boolean> isSubscribed(Optional<Connection> optCon, int ctxId, int userId, int folderId, int module) throws OXException;

    /**
     * Checks if the folder has any subscriptions.
     *
     * @param optCon The optional {@link Connection} to use
     * @param ctxId The context identifier
     * @param userId The user identifier
     * @param folderId The folder identifier
     * @param folderModule The folder module
     * @return The optional usedForSync value
     * @throws OXException In case the subscription couldn't be loaded
     */
    Optional<Boolean> isUsedForSync(Optional<Connection> optCon, int ctxId, int userId, int folderId, int folderModule) throws OXException;

    /**
     * Inserts, updates or removes the usedForSync property for the given user and folder.
     *
     * @param optCon The optional {@link Connection} to use
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param folderId The folder identifier
     * @param module The module identifier
     * @param usedForSync Whether the folder is used for sync or not
     * @throws OXException In case the subscription couldn't be set
     */
    void setUsedForSync(Optional<Connection> optCon, int contextId, int userId, int folderId, int module, UsedForSync usedForSync) throws OXException;

    /**
     * Inserts, updates or removes the subscription property for the given user and folder.
     *
     * @param optCon The optional {@link Connection} to use
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param folderId The folder identifier
     * @param module The module identifier
     * @param subscribed Whether the folder is subscribed or not
     * @throws OXException In case the subscription couldn't be set
     */
    void setSubscribed(Optional<Connection> optCon, int contextId, int userId, int folderId, int module, boolean subscribed) throws OXException;

    /**
     * Checks if the given module is subscription aware.
     *
     * @param module The module id
     * @return <code>true</code> if the module is subscription aware, <code>false</code> otherwise
     * @throws OXException If check fails
     */
    boolean isSubscribableModule(final int module) throws OXException;

    /**
     * Removes the <i>subscribed</i> user property for a specific folder.
     * 
     * @param optCon The optional database connection to use
     * @param contextId The context identifier
     * @param userIds The identifiers of the users to clear the <i>subscribed</i> flag for
     * @param folderId The folder identifier
     * @param module The module identifier
     */
    void clearSubscribed(Optional<Connection> optCon, int contextId, int[] userIds, int folderId, int module) throws OXException;

    /**
     * Removes the <i>usedForSync</i> user property for a specific folder.
     * 
     * @param optCon The optional database connection to use
     * @param contextId The context identifier
     * @param userIds The identifiers of the users to clear the <i>usedForSync</i> flag for
     * @param folderId The folder identifier
     * @param module The module identifier
     */
    void clearUsedForSync(Optional<Connection> optCon, int contextId, int[] userIds, int folderId, int module) throws OXException;

}

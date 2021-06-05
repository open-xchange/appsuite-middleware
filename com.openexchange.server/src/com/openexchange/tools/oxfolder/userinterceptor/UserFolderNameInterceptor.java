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

package com.openexchange.tools.oxfolder.userinterceptor;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.user.User;
import com.openexchange.user.interceptor.AbstractUserServiceInterceptor;
import com.openexchange.user.interceptor.UserServiceInterceptor;

/**
 * {@link UserFolderNameInterceptor} - Ensures unique folder name under {@link FolderObject#SYSTEM_USER_INFOSTORE_FOLDER_ID}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
public class UserFolderNameInterceptor extends AbstractUserServiceInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFolderNameInterceptor.class);

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link UserFolderNameInterceptor}.
     *
     * @param serviceLookup The {@link ServiceLookup}
     */
    public UserFolderNameInterceptor(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public void afterCreate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        handle(context, user, contactData, properties);
        super.afterCreate(context, user, contactData, properties);
    }

    @Override
    public void afterUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        handle(context, user, contactData, properties);
        super.afterUpdate(context, user, contactData, properties);
    }

    /**
     * Ensures unique folder name under {@link FolderObject#SYSTEM_USER_INFOSTORE_FOLDER_ID}
     *
     * @param context The {@link Context} to check the uniqueness in
     * @param user The {@link User} that was created or updated
     * @param contactData The {@link Contact} that was created or updated
     * @throws OXException
     */
    private void handle(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        String displayName = null;
        int userId = -1;

        /*
         * Check if display name was updated
         */
        if (null != contactData && contactData.containsDisplayName() && Strings.isNotEmpty(contactData.getDisplayName())) {
            displayName = contactData.getDisplayName();
            userId = contactData.getInternalUserId();
        }
        if (Strings.isEmpty(displayName) && null != user && Strings.isNotEmpty(user.getDisplayName())) {
            displayName = user.getDisplayName();
            userId = user.getId();
        }

        /*
         * Check if values are set
         */
        if (Strings.isEmpty(displayName)) {
            LOGGER.debug("Display name wasn't updated. Skip updating folder cache.");
            return;
        }
        if (userId <= 0) {
            LOGGER.debug("Display for external contact was updated. Skip updating folder cache.");
            return;
        }
        checkValidName(displayName);

        /*
         * Acquire connection and update folder name
         */
        Context loadedContext = serviceLookup.getServiceSafe(ContextService.class).getContext(context.getContextId());
        Optional<Connection> optionalConnection = getConnection(properties);
        if (optionalConnection.isPresent()) {
            propagateDisplayNameModification(loadedContext, userId, displayName, optionalConnection.get());
        } else {
            propagateDisplayNameModification(loadedContext, userId, displayName);
        }
    }

    /**
     * Propagates modifications <b>already</b> performed on an existing user throughout folder module.
     *
     * @param context The {@link Context}
     * @param userId The user identifier
     * @param displayName The new display name of the user
     * @throws OXException If user's modifications could not be successfully propagated
     */
    // Partly copied from OXFolderAdminCache, written by Thorben
    private void propagateDisplayNameModification(Context context, int userId, String displayName) throws OXException {
        DatabaseService databaseService = serviceLookup.getServiceSafe(DatabaseService.class);
        Connection connection = databaseService.getWritable(context);
        int rollback = 0;
        try {
            Databases.startTransaction(connection);
            rollback = 1;

            propagateDisplayNameModification(context, userId, displayName, connection);

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
            databaseService.backWritable(context, connection);
        }
    }

    /**
     * Propagates modifications <b>already</b> performed on an existing user throughout folder module.
     *
     * @param context The {@link Context}
     * @param userId The user identifier
     * @param displayName The new display name of the user
     * @param connection The write {@link Connection} to use
     * @throws OXException If user's modifications could not be successfully propagated
     */
    // Partly copied from OXFolderAdminCache, written by Thorben
    private void propagateDisplayNameModification(Context context, int userId, String displayName, Connection connection) throws OXException {
        long lastModified = System.currentTimeMillis();
        try {
            /*
             * Determine the users default folder identifier and name
             */
            int defaultInfostoreFolderId = OXFolderSQL.getUserDefaultFolder(userId, FolderObject.INFOSTORE, connection, context);
            if (-1 == defaultInfostoreFolderId) {
                LOGGER.debug("No default infostore folder available for user {}, skip propagation of changed display name.", I(userId));
                return;
            }
            FolderObject folder = FolderObject.loadFolderObjectFromDB(defaultInfostoreFolderId, context, connection);
            if (displayName.equals(folder.getFolderName())) {
                //Nothing changed, nothing to do (MWB-329)
                return;
            }
            /*
             * Update folder name
             */
            String folderName;
            if (displayName.equalsIgnoreCase(folder.getFolderName())) {
                folderName = displayName;
            } else {
                folderName = OXFolderAdminHelper.determineUserstoreFolderName(context, displayName, connection);
            }
            OXFolderSQL.updateName(folder, folderName, lastModified, context.getMailadmin(), context, connection);
            /*
             * Clear caches
             */
            updateLastModified(context, connection, lastModified);
            clearFolderCache(context, defaultInfostoreFolderId);
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Updates the last modified timestamp of the folder {@link FolderObject#SYSTEM_SHARED_FOLDER_ID} and clears the
     * corresponding cache
     *
     * @param context The {@link Context}
     * @param connection The {@link Connection}
     * @param lastModified The timespamp to set the last modified value too
     * @throws OXException If updating fails
     */
    // Extracted from OXFolderAdminCache, written by Thorben
    private void updateLastModified(Context context, Connection connection, long lastModified) throws OXException {
        /*
         * Update shared folder's last modified time stamp
         */
        try {
            OXFolderSQL.updateLastModified(FolderObject.SYSTEM_SHARED_FOLDER_ID, lastModified, context.getMailadmin(), connection, context);
            clearFolderCache(context, FolderObject.SYSTEM_SHARED_FOLDER_ID);
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private void clearFolderCache(Context context, int folderId) throws OXException {
        /*
         * Reload cache entry
         */
        if (FolderCacheManager.isInitialized()) {
            /*
             * Distribute remove among remote caches
             */
            FolderCacheManager.getInstance().removeFolderObject(folderId, context);
        }
    }

    /**
     * Checks a string if it contains invalid characters
     *
     * @param folderName The folder name to check
     * @throws OXException {@link OXFolderExceptionCode#INVALID_DATA} if string contains invalid characters
     */
    private void checkValidName(String folderName) throws OXException {
        String result = Check.containsInvalidChars(folderName);
        if (null != result) {
            throw OXFolderExceptionCode.INVALID_DATA.create(result);
        }
    }

    /**
     * Does a look-up if the caller provided a connection
     *
     * @param properties A map with properties
     * @return The optional {@link Connection}
     */
    private Optional<Connection> getConnection(Map<String, Object> properties) {
        Object object = properties == null ? null : properties.get(UserServiceInterceptor.PROP_CONNECTION);
        return object instanceof Connection ? Optional.of((Connection) object) : Optional.empty();
    }

}

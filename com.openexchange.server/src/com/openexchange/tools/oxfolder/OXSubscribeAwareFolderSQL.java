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

package com.openexchange.tools.oxfolder;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.property.FolderSubscriptionHelper;

/**
 * {@link OXSubscribeAwareFolderSQL} - Utility class.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class OXSubscribeAwareFolderSQL {

    /**
     * Initializes a new {@link OXSubscribeAwareFolderSQL}.
     */
    private OXSubscribeAwareFolderSQL() {
        super();
    }

    /**
     * Gets all visible folders for the given module. Unsubscribed folders are ignored.
     *
     * @param userId The user identifier
     * @param memberInGroups The memberInGroups
     * @param accessibleModules The accessible modules
     * @param module The module to retrieve
     * @param ctx The context
     * @return A list of available folders in the given module
     * @throws OXException If query fails
     */
    public static List<FolderObject> getAllVisibleFoldersOfModule(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int module, final Context ctx) throws OXException {
        Connection con = DBPool.pickup(ctx);
        try {
            return getAllVisibleFoldersOfModule(userId, memberInGroups, accessibleModules, module, ctx, con);
        } finally {
            Database.back(ctx.getContextId(), false, con);
        }
    }

    private static List<FolderObject> getAllVisibleFoldersOfModule(int userId, int[] memberInGroups, int[] accessibleModules, int module, Context ctx, Connection con) throws OXException {
        SearchIterator<FolderObject> iter = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(userId, memberInGroups, accessibleModules, module, ctx, con);
        try {
            if (!iter.hasNext()) {
                return Collections.emptyList();
            }

            FolderSubscriptionHelper subscriptionHelper = ServerServiceRegistry.getInstance().getService(FolderSubscriptionHelper.class);
            if (subscriptionHelper == null) {
                throw ServiceExceptionCode.absentService(FolderSubscriptionHelper.class);
            }

            Optional<Connection> optCon = Optional.of(con);
            List<FolderObject> result = null;
            do {
                FolderObject folder = iter.next();
                if (folder != null && subscriptionHelper.isSubscribed(optCon, ctx.getContextId(), userId, folder.getObjectID(), folder.getModule()).orElse(Boolean.TRUE).booleanValue()) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(folder);
                }
            } while (iter.hasNext());
            return result == null ? Collections.emptyList() : result;
        } finally {
            SearchIterators.close(iter);
        }
    }

}

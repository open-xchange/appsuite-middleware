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

package com.openexchange.groupware.tasks;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.SearchObject;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.oxfolder.OXSubscribeAwareFolderSQL;
import com.openexchange.tools.sql.SearchStrings;
import com.openexchange.user.User;

/**
 * Implements the search operation logic.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Search {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Search.class);

    protected final Context ctx;

    protected final User user;

    protected final UserPermissionBits permissionBits;

    protected final TaskSearchObject search;

    protected final int orderBy;

    protected final Order order;

    protected final int[] columns;

    protected final List<Integer> all = new ArrayList<Integer>(), own = new ArrayList<Integer>(), shared = new ArrayList<Integer>();

    /**
     * Initializes a new {@link Search}.
     *
     * @param ctx
     * @param user
     * @param permissionBits
     * @param search
     * @param orderBy
     * @param order
     * @param columns
     */
    public Search(final Context ctx, final User user, final UserPermissionBits permissionBits, final TaskSearchObject search, final int orderBy, final Order order, final int[] columns) {
        super();
        this.permissionBits = permissionBits;
        this.ctx = ctx;
        this.user = user;
        this.search = search;
        this.orderBy = orderBy;
        this.order = order;
        this.columns = columns;
    }

    /**
     * Performs the search
     *
     * @return The search results
     * @throws OXException
     */
    public SearchIterator<Task> perform() throws OXException {
        checkConditions();
        prepareFolder();
        if (all.size() + own.size() + shared.size() == 0) {
            return SearchIteratorAdapter.emptyIterator();
        }
        return TaskStorage.getInstance().search(ctx, getUserId(), search, orderBy, order, columns, all, own, shared);
    }

    /**
     * Checks conditions on the search pattern
     *
     * @throws OXException
     */
    protected void checkConditions() throws OXException {
        if (SearchObject.NO_PATTERN == search.getPattern()) {
            return;
        }
        final int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (0 == minimumSearchCharacters) {
            return;
        }
        if (SearchStrings.lengthWithoutWildcards(search.getPattern()) < minimumSearchCharacters) {
            throw TaskExceptionCode.PATTERN_TOO_SHORT.create(I(minimumSearchCharacters));
        }
    }

    /**
     * Prepares the folder
     *
     * @throws OXException
     */
    protected void prepareFolder() throws OXException {
        List<FolderObject> folders;
        if (search.hasFolders()) {
            folders = loadFolder(ctx, search.getFolders());
        } else {
            try {
                folders = OXSubscribeAwareFolderSQL.getAllVisibleFoldersOfModule(
                    getUserId(),
                    user.getGroups(),
                    permissionBits.getAccessibleModules(),
                    FolderObject.TASK,
                    ctx);
            } catch (OXException e) {
                throw e;
            }
        }
        try {
            for(FolderObject folder: folders) {
                if (!Permission.isFolderVisible(ctx, user, permissionBits, folder) || Permission.canOnlySeeFolder(ctx, user, permissionBits, folder)) {
                    continue;
                }
                Permission.checkReadInFolder(ctx, user, permissionBits, folder);
                if (folder.isShared(getUserId()) && !Permission.canReadInFolder(ctx, user, permissionBits, folder)) {
                    shared.add(Integer.valueOf(folder.getObjectID()));
                } else if (Permission.canReadInFolder(ctx, user, permissionBits, folder)) {
                    own.add(Integer.valueOf(folder.getObjectID()));
                } else {
                    all.add(Integer.valueOf(folder.getObjectID()));
                }
            }
        } catch (OXException e) {
            throw e;
        }
        LOG.trace("Search tasks, all: {}, own: {}, shared: {}", all, own, shared);
    }

    /**
     * Loads the given folders
     *
     * @param ctx The {@link Context}
     * @param folderIds The folder ids to load
     * @return The folders
     * @throws OXException
     */
    private static List<FolderObject> loadFolder(final Context ctx, final int[] folderIds) throws OXException {
        final List<FolderObject> retval = new ArrayList<FolderObject>(folderIds.length);
        for (final int folderId : folderIds) {
            retval.add(Tools.getFolder(ctx, folderId));
        }
        return retval;
    }

    /**
     * Gets the user id
     *
     * @return The user id
     */
    private int getUserId() {
        return user.getId();
    }
}

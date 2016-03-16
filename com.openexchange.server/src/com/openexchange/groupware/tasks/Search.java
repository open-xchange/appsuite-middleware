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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.groupware.tasks;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.SearchObject;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.sql.SearchStrings;

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

    public SearchIterator<Task> perform() throws OXException, OXException {
        checkConditions();
        prepareFolder();
        if (all.size() + own.size() + shared.size() == 0) {
            return SearchIteratorAdapter.emptyIterator();
        }
        return TaskStorage.getInstance().search(ctx, getUserId(), search, orderBy, order, columns, all, own, shared);
    }

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

    protected void prepareFolder() throws OXException, OXException {
        SearchIterator<FolderObject> folders;
        if (search.hasFolders()) {
            folders = loadFolder(ctx, search.getFolders());
        } else {
            try {
                folders = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(
                    getUserId(),
                    user.getGroups(),
                    permissionBits.getAccessibleModules(),
                    FolderObject.TASK,
                    ctx);
            } catch (final OXException e) {
                throw e;
            }
        }
        try {
            while (folders.hasNext()) {
                final FolderObject folder = folders.next();
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
        } catch (final OXException e) {
            throw e;
        }
        LOG.trace("Search tasks, all: {}, own: {}, shared: {}", all, own, shared);
    }

    private static SearchIterator<FolderObject> loadFolder(final Context ctx, final int[] folderIds) throws OXException {
        final List<FolderObject> retval = new ArrayList<FolderObject>(folderIds.length);
        for (final int folderId : folderIds) {
            retval.add(Tools.getFolder(ctx, folderId));
        }
        return new SearchIteratorAdapter<FolderObject>(retval.iterator());
    }

    private int getUserId() {
        return user.getId();
    }
}

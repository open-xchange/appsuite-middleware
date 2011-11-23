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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.favfolder.internal;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CreateFavFolderTreePerformer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CreateFavFolderTreePerformer extends AbstractFavFolderPerformer {

    private static final int MIN_ID = 100;

    /**
     * Initializes a new {@link CreateFavFolderTreePerformer}.
     */
    public CreateFavFolderTreePerformer(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    private static final Pattern PATTERN_NUM = Pattern.compile(Pattern.quote("#NUM#"));

    /**
     * Start a new favorite folder tree by inserting its root element.
     *
     * @param name The favorite folder list name or <code>null</code> to select default name {@link FavFolderStrings#MY_FAV_FOLDERS
     *            "My favorite folders #NUM#"}
     * @param id The tree/list identifier or <code>-1</code> to generate a new unique identifier
     * @param session The session providing user data
     * @return The identifier of the newly created tree
     * @throws OXException If creation fails
     */
    public int createNewFavFolderTree(final String name, final int id, final ServerSession session) throws OXException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        final int contextId = session.getContextId();
        final int treeId;
        if (id <= 0) {
            /*
             * Acquire new identifier
             */
            final IDGeneratorService generatorService = getService(IDGeneratorService.class);
            treeId = generatorService.getId("com.openexchange.favfolder", contextId, MIN_ID);
        } else {
            treeId = id;
        }
        /*
         * Insert root folder for new tree
         */
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            con.setAutoCommit(false); // BEGIN
            stmt =
                con.prepareStatement("INSERT INTO virtualTree (cid, tree, user, folderId, parentId, name, lastModified, modifiedBy, shadow, sortNum) VALUES (?,?,?,?,?,?,?,?,?,?)");
            final int userId = session.getUserId();
            int pos = 1;
            stmt.setInt(pos++, contextId); // cid
            stmt.setInt(pos++, treeId); // tree
            stmt.setInt(pos++, userId); // user
            stmt.setString(pos++, FolderStorage.ROOT_ID); // folderId
            stmt.setString(pos++, ""); // parentId
            if (null == name) {
                final String i18nName = StringHelper.valueOf(session.getUser().getLocale()).getString(FavFolderStrings.MY_FAV_FOLDERS);
                stmt.setString(pos++, PATTERN_NUM.matcher(i18nName).replaceFirst(String.valueOf(treeId))); // name
            } else {
                stmt.setString(pos++, name); // name
            }
            stmt.setLong(pos++, System.currentTimeMillis()); // lastModified
            stmt.setLong(pos++, userId); // modifiedBy
            stmt.setString(pos++, ""); // shadow
            stmt.setInt(pos++, 0); // sortNum
            try {
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.DUPLICATE_TREE.create(e, Integer.valueOf(treeId));
            }
            con.commit(); // COMMIT
        } catch (final SQLException e) {
            rollback(con);
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
        /*
         * Return its identifier
         */
        return treeId;
    }

}

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

package com.openexchange.folderstorage.virtual.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.virtual.VirtualFolder;
import com.openexchange.folderstorage.virtual.VirtualPermission;
import com.openexchange.folderstorage.virtual.osgi.Services;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Collators;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Select} - SQL to load a virtual folder or its subfolder identifiers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Select {

    /**
     * Initializes a new {@link Select}.
     */
    private Select() {
        super();
    }

    private static final String SQL_SELECT =
        "SELECT parentId, name, modifiedBy, lastModified FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_SELECT_BCK =
        "SELECT parentId, name, modifiedBy, lastModified FROM virtualBackupTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_SELECT2 = "SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_SELECT2_BCK =
        "SELECT folderId, name FROM virtualBackupTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_SELECT_SUBF =
        "SELECT folderId, name FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND parentId = ?";

    private static final String SQL_SELECT_SUBF_BCK =
        "SELECT folderId, name FROM virtualBackupTree WHERE cid = ? AND tree = ? AND user = ? AND parentId = ?";

    private static final String SQL_SELECT_PERMS =
        "SELECT entity, groupFlag, fp, orp, owp, odp, adminFlag, system FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_SELECT_PERMS_BCK =
        "SELECT entity, groupFlag, fp, orp, owp, odp, adminFlag, system FROM virtualBackupPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_SELECT_SUBSCRIPTION =
        "SELECT subscribed FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_SELECT_SUBSCRIPTION_BCK =
        "SELECT subscribed FROM virtualBackupSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_SELECT2_SUBF =
        "SELECT folderId, name FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND parentId = ?";

    private static final String SQL_SELECT2_SUBF_BCK =
        "SELECT folderId, name FROM virtualBackupTree WHERE cid = ? AND tree = ? AND user = ? AND parentId = ?";

    /**
     * Checks if the specified virtual tree contains a folder denoted by given folder identifier.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folderId The folder identifier
     * @param storageType The storage type to use
     * @return <code>true</code> if the specified virtual tree contains a folder denoted by given folder identifier; otherwise
     *         <code>false</code>
     * @throws OXException If checking folder's presence fails
     */
    public static boolean containsFolder(final int cid, final int tree, final int user, final String folderId, final StorageType storageType) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        // Get a connection
        final Connection con = databaseService.getReadOnly(cid);
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement(StorageType.WORKING.equals(storageType) ? SQL_SELECT2 : SQL_SELECT2_BCK);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                return rs.next();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
        } finally {
            databaseService.backReadOnly(cid, con);
        }
    }

    /**
     * Fills specified folder.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param locale The user's locale (needed for proper sorting of possible subfolders)
     * @param virtualFolder The folder to fill
     * @param storageType The storage type to use
     * @throws OXException If filling the folder fails
     */
    public static void fillFolder(final int cid, final int tree, final int user, final Locale locale, final VirtualFolder virtualFolder, final StorageType storageType) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        // Get a connection
        final Connection con = databaseService.getReadOnly(cid);
        final String folderId = virtualFolder.getID();
        try {
            // Select folder data
            PreparedStatement stmt = null;
            ResultSet rs = null;
            final boolean working = StorageType.WORKING.equals(storageType);
            try {
                stmt = con.prepareStatement(working ? SQL_SELECT : SQL_SELECT_BCK);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw FolderExceptionErrorMessage.NOT_FOUND.create(folderId, Integer.valueOf(tree));
                }
                pos = 1;
                virtualFolder.setParentID(rs.getString(pos++));
                virtualFolder.setName(rs.getString(pos++));
                // Set optional modified-by
                {
                    final int modifiedBy = rs.getInt(pos++);
                    if (rs.wasNull()) {
                        virtualFolder.setModifiedBy(-1);
                    } else {
                        virtualFolder.setModifiedBy(modifiedBy);
                    }
                }
                // Set optional last-modified time stamp
                {
                    final long date = rs.getLong(pos);
                    if (rs.wasNull()) {
                        virtualFolder.setLastModified(null);
                    } else {
                        virtualFolder.setLastModified(new Date(date));
                    }
                }
            } catch (final SQLException e) {
                if (null != stmt) {
                    final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Select.class);
                    if (LOG.isDebugEnabled()) {
                        final String sql = getSQLString(stmt);
                        LOG.debug("Failed SQL:\n\t{}", sql);
                    }
                }
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
            stmt = null;
            // Select subfolders
            {
                final String[] subfolderIds = getSubfolderIds(cid, tree, user, locale, folderId, storageType, con);
                virtualFolder.setSubfolderIDs(subfolderIds);
                virtualFolder.setSubscribedSubfolders(subfolderIds != null && subfolderIds.length > 0);
            }
            // Select permissions
            try {
                stmt = con.prepareStatement(working ? SQL_SELECT_PERMS : SQL_SELECT_PERMS_BCK);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                final List<Permission> permissions = new ArrayList<Permission>();
                while (rs.next()) {
                    final Permission p = new VirtualPermission();
                    pos = 1;
                    p.setEntity(rs.getInt(pos++));
                    p.setGroup(rs.getInt(pos++) > 0);
                    p.setFolderPermission(rs.getInt(pos++));
                    p.setReadPermission(rs.getInt(pos++));
                    p.setWritePermission(rs.getInt(pos++));
                    p.setDeletePermission(rs.getInt(pos++));
                    p.setAdmin(rs.getInt(pos++) > 0);
                    p.setSystem(rs.getInt(pos++));
                    permissions.add(p);
                }
                if (permissions.isEmpty()) {
                    virtualFolder.setPermissions(null);
                } else {
                    virtualFolder.setPermissions(permissions.toArray(new Permission[permissions.size()]));
                }
            } catch (final SQLException e) {
                if (null != stmt) {
                    final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Select.class);
                    if (LOG.isDebugEnabled()) {
                        final String sql = getSQLString(stmt);
                        LOG.debug("Failed SQL:\n\t{}", sql);
                    }
                }
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
            stmt = null;
            // Select subscription
            try {
                stmt = con.prepareStatement(working ? SQL_SELECT_SUBSCRIPTION : SQL_SELECT_SUBSCRIPTION_BCK);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                pos = 1;
                boolean subscribed = true;
                if (rs.next()) {
                    subscribed = rs.getInt(pos) > 0;
                }
                virtualFolder.setSubscribed(subscribed);
            } catch (final SQLException e) {
                if (null != stmt) {
                    final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Select.class);
                    if (LOG.isDebugEnabled()) {
                        final String sql = getSQLString(stmt);
                        LOG.debug("Failed SQL:\n\t{}", sql);
                    }
                }
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
        } finally {
            databaseService.backReadOnly(cid, con);
        }
    }

    /**
     * Gets the sorted identifiers of the subfolders located below specified parent.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param locale The user's locale
     * @param parentId The parent identifier
     * @param storageType The storage type
     * @return The sorted identifiers of the subfolders located below specified parent
     * @throws OXException If subfolders cannot be detected
     */
    public static String[] getSubfolderIds(final int cid, final int tree, final int user, final Locale locale, final String parentId, final StorageType storageType) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        // Get a connection
        final Connection con = databaseService.getReadOnly(cid);
        try {
            return getSubfolderIds(cid, tree, user, locale, parentId, storageType, con);
        } finally {
            databaseService.backReadOnly(cid, con);
        }
    }

    /**
     * Gets the sorted identifiers of the subfolders located below specified parent.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param locale The user's locale
     * @param parentId The parent identifier
     * @param storageType The storage type
     * @param con The connection to use
     * @return The sorted identifiers of the subfolders located below specified parent
     * @throws OXException If subfolders cannot be detected
     */
    public static String[] getSubfolderIds(final int cid, final int tree, final int user, final Locale locale, final String parentId, final StorageType storageType, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final boolean working = StorageType.WORKING.equals(storageType);
            stmt = con.prepareStatement(working ? SQL_SELECT_SUBF : SQL_SELECT_SUBF_BCK);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            stmt.setInt(pos++, user);
            stmt.setString(pos, parentId);
            rs = stmt.executeQuery();
            pos = 1;
            final List<String> subfolderIds;
            if (FolderStorage.ROOT_ID.equals(parentId)) {
                /*
                 * Proper sort of top level folders 1. Private 2. Public 3. Shared . . . n Sorted external email accounts
                 */
                final List<String[]> fns = new ArrayList<String[]>();
                while (rs.next()) {
                    final String[] sa = new String[2];
                    sa[0] = rs.getString(pos); // ID
                    sa[1] = rs.getString(2); // Name
                    fns.add(sa);
                }
                Collections.sort(fns, new PrivateSubfolderIDComparator(locale));
                subfolderIds = new ArrayList<String>(fns.size());
                for (final String[] fn : fns) {
                    subfolderIds.add(fn[0]);
                }
            } else {
                final TreeMap<String, String> treeMap = new TreeMap<String, String>(new FolderNameComparator(locale));
                final StringHelper stringHelper = StringHelper.valueOf(locale);
                while (rs.next()) {
                    treeMap.put(stringHelper.getString(rs.getString(2)), rs.getString(pos));
                }
                final Set<Entry<String, String>> entrySet = treeMap.entrySet();
                subfolderIds = new ArrayList<String>(entrySet.size());
                for (final Entry<String, String> entry : entrySet) {
                    subfolderIds.add(entry.getValue());
                }
            }
            return subfolderIds.toArray(new String[subfolderIds.size()]);
        } catch (final SQLException e) {
            if (null != stmt) {
                final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Select.class);
                if (LOG.isDebugEnabled()) {
                    final String sql = getSQLString(stmt);
                    LOG.debug("Failed SQL:\n\t{}", sql);
                }
            }
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private static String getSQLString(final PreparedStatement stmt) {
        final String toString = stmt.toString();
        return toString.substring(toString.indexOf(": ") + 2);
    }

    private static final class FolderNameComparator implements Comparator<String> {

        private final Collator collator;

        public FolderNameComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final String o1, final String o2) {
            return collator.compare(o1, o2);
        }

    } // End of FolderNameComparator

    private static final class PrivateSubfolderIDComparator implements Comparator<String[]> {

        private final Collator collator;

        public PrivateSubfolderIDComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final String[] o1, final String[] o2) {
            {
                final String privateId = "1";
                final Integer privateComp = conditionalCompare(privateId.equals(o1[0]), privateId.equals(o2[0]));
                if (null != privateComp) {
                    return privateComp.intValue();
                }
            }
            {
                final String publicId = "2";
                final Integer publicComp = conditionalCompare(publicId.equals(o1[0]), publicId.equals(o2[0]));
                if (null != publicComp) {
                    return publicComp.intValue();
                }
            }
            {
                final String sharedId = "3";
                final Integer sharedComp = conditionalCompare(sharedId.equals(o1[0]), sharedId.equals(o2[0]));
                if (null != sharedComp) {
                    return sharedComp.intValue();
                }
            }
            {
                final String uiName = "Unified Inbox";
                final Integer unifiedInboxComp = conditionalCompare(uiName.equalsIgnoreCase(o1[1]), uiName.equalsIgnoreCase(o2[1]));
                if (null != unifiedInboxComp) {
                    return unifiedInboxComp.intValue();
                }
            }
            return collator.compare(o1[1], o2[1]);
        }

        private Integer conditionalCompare(final boolean b1, final boolean b2) {
            if (b1) {
                if (!b2) {
                    return Integer.valueOf(-1);
                }
                return Integer.valueOf(0);
            } else if (b2) {
                return Integer.valueOf(1);
            }
            return null;
        }

    } // End of FolderNameComparator

}

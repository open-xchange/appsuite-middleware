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

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.internal.Tools;
import com.openexchange.folderstorage.mail.MailFolderType;
import com.openexchange.folderstorage.virtual.osgi.Services;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.event.EventPool;
import com.openexchange.mail.event.PooledEvent;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Insert} - SQL for inserting a virtual folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Insert {

    /**
     * Initializes a new {@link Insert}.
     */
    private Insert() {
        super();
    }

    private static final String SQL_INSERT =
        "INSERT INTO virtualTree (cid, tree, user, folderId, parentId, name, modifiedBy, lastModified, shadow) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_PERM =
        "INSERT INTO virtualPermission (cid, tree, user, folderId, entity, groupFlag, fp, orp, owp, odp, adminFlag, system) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_SUBS =
        "INSERT INTO virtualSubscription (cid, tree, user, folderId, subscribed) VALUES (?, ?, ?, ?, ?)";

    /**
     * Inserts specified folder.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folder The folder
     * @param shadow The optional shadow string
     * @throws FolderException If insertion fails
     */
    public static void insertFolder(final int cid, final int tree, final int user, final Folder folder, final String shadow, final Session session) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        // Get a connection
        final Connection con = databaseService.getWritable(cid);
        try {
            con.setAutoCommit(false); // BEGIN
            insertFolder(cid, tree, user, folder, shadow, session, con);
            con.commit(); // COMMIT
        } catch (final SQLException e) {
            DBUtils.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            DBUtils.rollback(con); // ROLLBACK
            throw e;
        } catch (final Exception e) {
            DBUtils.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            databaseService.backWritable(cid, con);
        }
    }

    /**
     * Inserts specified folder.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folder The folder
     * @param shadow The optional shadow string
     * @param con The connection
     * @throws FolderException If insertion fails
     */
    public static void insertFolder(final int cid, final int tree, final int user, final Folder folder, final String shadow, final Session session, final Connection con) throws OXException {
        if (null == con) {
            insertFolder(cid, tree, user, folder, shadow, session);
            return;
        }
        final String folderId = folder.getID();
        // Insert folder data
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_INSERT);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            stmt.setInt(pos++, user);
            stmt.setString(pos++, folderId);
            stmt.setString(pos++, folder.getParentID());
            stmt.setString(pos++, folder.getName());
            final int modifiedBy = folder.getModifiedBy();
            if (modifiedBy == -1) {
                stmt.setNull(pos++, Types.INTEGER);
            } else {
                stmt.setInt(pos++, modifiedBy);
            }
            final Date lastModified = folder.getLastModified();
            if (lastModified == null) {
                stmt.setNull(pos++, Types.BIGINT);
            } else {
                stmt.setLong(pos++, lastModified.getTime());
            }
            stmt.setString(pos, null == shadow ? "" : shadow);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        if (false) {
            // Insert permission data if non-null and not empty
            final Permission[] permissions = folder.getPermissions();
            if (null != permissions && permissions.length > 0) {
                try {
                    stmt = con.prepareStatement(SQL_INSERT_PERM);
                    for (final Permission p : permissions) {
                        int pos = 1;
                        stmt.setInt(pos++, cid);
                        stmt.setInt(pos++, tree);
                        stmt.setInt(pos++, user);
                        stmt.setString(pos++, folderId);
                        stmt.setInt(pos++, p.getEntity());
                        stmt.setInt(pos++, p.isGroup() ? 1 : 0);
                        stmt.setInt(pos++, p.getFolderPermission());
                        stmt.setInt(pos++, p.getReadPermission());
                        stmt.setInt(pos++, p.getWritePermission());
                        stmt.setInt(pos++, p.getDeletePermission());
                        stmt.setInt(pos++, p.isAdmin() ? 1 : 0);
                        stmt.setInt(pos++, p.getSystem());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                } catch (final SQLException e) {
                    throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    DBUtils.closeSQLStuff(stmt);
                }
            }
        }
        if (false) {
            // Insert subscription data
            try {
                stmt = con.prepareStatement(SQL_INSERT_SUBS);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos++, folderId);
                stmt.setInt(pos, folder.isSubscribed() ? 1 : 0);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
        }
        /*
         * Post event
         */
        if (null != session) {
            final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
            final boolean postEvent =
                null == service ? true : service.getBoolProperty("com.openexchange.folderstorage.postEASFolderEvents", true);
            if (postEvent) {
                try {
                    if (MailFolderType.getInstance().servesFolderId(folderId)) {
                        final FullnameArgument argument = prepareMailFolderParam(folder.getParentID());
                        postEvent(argument.getAccountId(), argument.getFullname(), false, false, session);
                    } else {
                        final ServerSession serverSession = ServerSessionAdapter.valueOf(session);
                        new EventClient(serverSession).create(new OXFolderAccess(con, serverSession.getContext()).getFolderObject(unsignedInt(folderId)));
                    }
                } catch (final Exception e) {
                    // Ignore
                    final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Delete.class);
                    log.error("", e);
                }
            }
        }
    }

    private static void postEvent(final int accountId, final String fullname, final boolean contentRelated, final boolean immediateDelivery, final Session session) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(new PooledEvent(session.getContextId(), session.getUserId(), accountId, prepareFullname(accountId, fullname), contentRelated, immediateDelivery, true, session));
    }

    private static int unsignedInt(final String sInteger) {
        return Tools.getUnsignedInteger(sInteger);
    }

}

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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.importexport;

import static com.openexchange.groupware.contact.Contacts.performContactReadCheck;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import org.apache.commons.logging.Log;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.ContactMySql;
import com.openexchange.groupware.contact.ContactSql;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbContactSQLImpl}
 *
 * Stripped down version of original com.openexchange.api2.RdbContactSQLImpl class for import/export tests.
 */
public class RdbContactSQLImpl {

    private final int userId;

    private final int[] memberInGroups;

    private final Context ctx;

    private final Session session;

    private final UserConfiguration userConfiguration;

    private static final Log LOG = com.openexchange.log.Log.loggerFor(RdbContactSQLImpl.class);

    public RdbContactSQLImpl(final Session session) throws OXException {
        super();
        this.ctx = ContextStorage.getStorageContext(session);
        this.userId = session.getUserId();
        this.memberInGroups = UserStorage.getStorageUser(session.getUserId(), ctx).getGroups();
        this.session = session;
        userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx);
    }

    public Contact getObjectById(final int objectId, final int fid) throws OXException {
        if (objectId <= 0) {
            throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(I(objectId), I(ctx.getContextId()));
        }
        final FolderObject contactFolder = new OXFolderAccess(ctx).getFolderObject(fid);
        if (contactFolder.getModule() != FolderObject.CONTACT) {
            throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(fid), I(ctx.getContextId()), I(userId));
        }
        final Connection con = DBPool.pickup(ctx);
        final Contact co;
        try {
            co = Contacts.getContactById(objectId, userId, memberInGroups, ctx, userConfiguration, con);
            if (!performContactReadCheck(contactFolder, userId, co.getCreatedBy(), userConfiguration, con)) {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(fid), I(ctx.getContextId()), I(userId));
            }
            final Date creationDate = Attachments.getInstance(new SimpleDBProvider(con, null)).getNewestCreationDate(
                ctx,
                Types.CONTACT,
                objectId);
            if (null != creationDate) {
                co.setLastModifiedOfNewestAttachment(creationDate);
            }
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
        return co;
    }

    public void deleteContactObject(final int oid, final int fuid, final Date client_date) throws OXException {
        if (FolderObject.SYSTEM_LDAP_FOLDER_ID == fuid) {
            throw ContactExceptionCodes.NO_USER_CONTACT_DELETE.create();
        }
        Connection readcon = null;
        EffectivePermission oclPerm = null;
        int created_from = 0;
        final Contact co = new Contact();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            readcon = DBPool.pickup(ctx);

            boolean pflag = false;
            Date changing_date = null;
            final ContactSql cs = new ContactMySql(session, ctx);
            stmt = readcon.createStatement();
            rs = stmt.executeQuery(cs.iFdeleteContactObject(oid, ctx.getContextId()));
            if (rs.next()) {
                created_from = rs.getInt(2);

                co.setCreatedBy(created_from);
                co.setParentFolderID(fuid);
                co.setObjectID(oid);

                final long xx = rs.getLong(3);
                changing_date = new java.util.Date(xx);
                final int pf = rs.getInt(4);
                if (!rs.wasNull() && pf > 0) {
                    pflag = true;
                }
            } else {
                throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(I(oid), I(ctx.getContextId()));
            }

            if ((client_date != null && client_date.getTime() >= 0) && (client_date.before(changing_date))) {
                throw ContactExceptionCodes.OBJECT_HAS_CHANGED.create(I(ctx.getContextId()), I(fuid), I(userId), I(oid));
            }
            final OXFolderAccess folderAccess = new OXFolderAccess(readcon, ctx);
            final FolderObject contactFolder = folderAccess.getFolderObject(fuid);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(fuid), I(ctx.getContextId()), I(userId));
            }
            if ((contactFolder.getType() != FolderObject.PRIVATE) && pflag) {
                LOG.debug(new com.openexchange.java.StringAllocator("Here is a contact in a non PRIVATE folder with a set private flag -> (cid=").append(
                    ctx.getContextId()).append(" fid=").append(fuid).append(" oid=").append(oid).append(')'));
            } else if ((contactFolder.getType() == FolderObject.PRIVATE) && pflag && created_from != userId) {
                throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(I(fuid), I(ctx.getContextId()), I(userId));
            }

            oclPerm = folderAccess.getFolderPermission(fuid, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(I(fuid), I(ctx.getContextId()), I(userId));
            }
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            try {
                if (readcon != null) {
                    DBPool.closeReaderSilent(ctx, readcon);
                }
            } catch (final Exception ex) {
                LOG.error("Unable to return Connection", ex);
            }
        }
        Connection writecon = null;
        try {
            writecon = DBPool.pickupWriteable(ctx);

            final int deletePermission = oclPerm.getDeletePermission();
            if (deletePermission >= OCLPermission.DELETE_ALL_OBJECTS) {
                // May delete any contact
                Contacts.deleteContact(oid, ctx.getContextId(), writecon);
            } else {
                if ((deletePermission < OCLPermission.DELETE_OWN_OBJECTS) || created_from != userId) {
                    throw ContactExceptionCodes.NO_DELETE_PERMISSION.create(I(fuid), I(ctx.getContextId()), I(userId));
                }
                // May delete own contact
                Contacts.deleteContact(oid, ctx.getContextId(), writecon);
            }
            final EventClient ec = new EventClient(session);
            ec.delete(co);
        } catch (final OXException e) {
            throw e;
        } finally {
            if (writecon != null) {
                DBPool.closeWriterSilent(ctx, writecon);
            }
        }
    }

    public int getNumberOfContacts(final int folderId) throws OXException {
        final Connection readCon;
        try {
            readCon = DBPool.pickup(ctx);
        } catch (final OXException e) {
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
        }
        try {
            final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
            final ContactSql contactSQL = new ContactMySql(session, ctx);
            final EffectivePermission oclPerm = new OXFolderAccess(readCon, ctx).getFolderPermission(folderId, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
            }
            if (!oclPerm.canReadAllObjects()) {
                if (oclPerm.canReadOwnObjects()) {
                    contactSQL.setReadOnlyOwnFolder(userId);
                } else {
                    throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(userId));
                }
            }
            contactSQL.setSelect(contactSQL.iFgetNumberOfContactsString());
            contactSQL.setFolder(folderId);
            int retval = 0;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = contactSQL.getSqlStatement(readCon);
                rs = ((PreparedStatement) stmt).executeQuery();

                if (rs.next()) {
                    retval = rs.getInt(1);
                }
            } catch (final SQLException e) {
                throw ContactExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                closeSQLStuff(rs, stmt);
            }
            return retval;
        } finally {
            DBPool.closeReaderSilent(ctx, readCon);
        }
    }

}

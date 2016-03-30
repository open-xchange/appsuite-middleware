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

package com.openexchange.contact.storage.ldap.folder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderSQL;


public class LdapGlobalFolderCreator {

    public static class FolderIDAndAdminID {

        /**
         * Initializes a new {@link FolderIDAndAdminID}.
         * @param folderid
         * @param adminid
         */
        FolderIDAndAdminID(final int folderid, final int adminid) {
            this.folderid = folderid;
            this.adminid = adminid;
        }

        private final int folderid;

        private final int adminid;


        public final int getFolderid() {
            return folderid;
        }


        public final int getAdminid() {
            return adminid;
        }

    }

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(LdapGlobalFolderCreator.class);

    public static FolderIDAndAdminID createGlobalFolder(int contextID, String foldername) throws OXException, SQLException {
        // First search for a folder with the name if is doesn't exist create it
        Context ctx = new ContextImpl(contextID);
        int ldapFolderID;
        final int admin_user_id;
        {
            final Connection readCon = DBPool.pickup(ctx);
            try {
                admin_user_id = OXFolderSQL.getContextAdminID(ctx, readCon);
                ldapFolderID = getLdapFolderID(foldername, ctx, readCon);
            } finally {
                DBPool.closeReaderSilent(ctx, readCon);
            }
        }
        if (-1 == ldapFolderID) {
            final FolderObject fo = createFolderObject(admin_user_id, foldername);
            /*
             * As we have no possibility right now to access OXFolderManager without a session, we have to create
             * a dummy session object here, which provides the needed information
             */
            final OXFolderManager instance = OXFolderManager.getInstance(getDummySessionObj(admin_user_id, ctx.getContextId()));
            ldapFolderID = instance.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
            LOG.info("LDAP folder successfully created");
        }
        return new FolderIDAndAdminID(ldapFolderID, admin_user_id);
    }

    /**
     * @param globalLdapFolderName2
     * @param ctx
     * @param writeCon
     * @return the id or -1 if not found
     * @throws SQLException
     */
    private static int getLdapFolderID(final String globalLdapFolderName2, final Context ctx, final Connection readCon) throws SQLException {
        PreparedStatement ps = null;
        ResultSet executeQuery = null;
        try {
            ps = readCon.prepareStatement("SELECT fuid from oxfolder_tree WHERE cid=? AND fname=?");
            ps.setInt(1, ctx.getContextId());
            ps.setString(2, globalLdapFolderName2);
            executeQuery = ps.executeQuery();
            while (executeQuery.next()) {
                return executeQuery.getInt(1);
            }
            return -1;
        } catch (final SQLException e) {
            throw e;
        } finally {
            if (null != executeQuery) {
                try {
                    executeQuery.close();
                } catch (final SQLException e) {
                    LOG.error("", e);
                }
            }
            if (null != ps) {
                try {
                    ps.close();
                } catch (final SQLException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    private static FolderObject createFolderObject(final int admin_user_id, final String globalLdapFolderName) {
        final FolderObject fo = new FolderObject();
        final OCLPermission defaultPerm = new OCLPermission();
        defaultPerm.setEntity(admin_user_id);
        defaultPerm.setGroupPermission(false);
        defaultPerm.setAllPermission(
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION);
        defaultPerm.setFolderAdmin(true);

        final OCLPermission allPerm = new OCLPermission();
        allPerm.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
        allPerm.setGroupPermission(true);
        allPerm.setAllPermission(
                OCLPermission.READ_FOLDER,
                OCLPermission.READ_ALL_OBJECTS,
                OCLPermission.NO_PERMISSIONS,
                OCLPermission.NO_PERMISSIONS);
        allPerm.setFolderAdmin(false);
        fo.setPermissionsAsArray(new OCLPermission[] { defaultPerm, allPerm });
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setType(FolderObject.PUBLIC);
        fo.setFolderName(globalLdapFolderName);
        fo.setModule(FolderObject.CONTACT);
        return fo;
    }

    private static Session getDummySessionObj(final int admin_user_id, final int contextid) {
        return new Session(){

            @Override
            public int getContextId() {
                return contextid;
            }

            @Override
            public String getLocalIp() {
                return null;
            }

            @Override
            public String getLogin() {
                return null;
            }

            @Override
            public String getLoginName() {
                return null;
            }

            @Override
            public Object getParameter(final String name) {
                return null;
            }

            @Override
            public boolean containsParameter(final String name) {
                return false;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getRandomToken() {
                return null;
            }

            @Override
            public String getSecret() {
                return null;
            }

            @Override
            public String getSessionID() {
                return null;
            }

            @Override
            public int getUserId() {
                return admin_user_id;
            }

            @Override
            public String getUserlogin() {
                return null;
            }

            @Override
            public void setParameter(final String name, final Object value) {
                // Nothing to do
            }
            @Override
            public String getAuthId() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getHash() {
                return null;
            }

            @Override
            public String getClient() {
                return null;
            }

            @Override
            public void setClient(final String client) {
                // Nothing to do.
            }

            @Override
            public void setLocalIp(final String ip) {
                // Nothing to do

            }

            @Override
            public void setHash(final String hash) {
                // Nothing to do

            }

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public Set<String> getParameterNames() {
                return Collections.emptySet();
            }
        };
    }

}

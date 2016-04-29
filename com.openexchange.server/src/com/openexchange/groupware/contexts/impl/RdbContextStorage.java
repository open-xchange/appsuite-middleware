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

package com.openexchange.groupware.contexts.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.update.Tools;

/**
 * This class implements a storage for contexts in a relational database.
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbContextStorage extends ContextStorage {

    /**
     * SQL select statement for loading a context.
     */
    private static final String SELECT_CONTEXT = "SELECT name,enabled,filestore_id,filestore_name,filestore_login,filestore_passwd,quota_max FROM context WHERE cid=?";

    /**
     * SQL select statement for resolving the login info to the context
     * identifier.
     */
    private static final String RESOLVE_CONTEXT =
        "SELECT cid FROM login2context WHERE login_info=?";

    /**
     * SQL select statement for resolving the identifier of the contexts
     * mailadmin.
     */
    private static final String GET_MAILADMIN =
        "SELECT user FROM user_setting_admin WHERE cid=?";

    /**
     * SQL select statement for reading the login information of a context.
     */
    private static final String GET_LOGININFOS =
        "SELECT login_info FROM login2context WHERE cid=?";

    /**
     * Default constructor.
     */
    public RdbContextStorage() {
        super();
    }

    @Override
    public int getContextId(final String loginInfo) throws OXException {
        final Connection con;
        try {
            con = DBPool.pickup();
        } catch (final OXException e) {
            throw ContextExceptionCodes.NO_CONNECTION.create(e);
        }
        int contextId = NOT_FOUND;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(RESOLVE_CONTEXT);
            stmt.setString(1, loginInfo);
            result = stmt.executeQuery();
            if (result.next()) {
                contextId = result.getInt(1);
            }
        } catch (final SQLException e) {
            throw ContextExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(con);
        }
        return contextId;
    }

    private static int getAdmin(final Context ctx) throws OXException {
        final Connection con = DBPool.pickup(ctx);
        try {
            return getAdmin(con, ctx.getContextId());
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    public static final int getAdmin(final Connection con, final int ctxId) throws OXException {
        int identifier = -1;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(GET_MAILADMIN);
            stmt.setInt(1, ctxId);
            result = stmt.executeQuery();
            if (result.next()) {
                identifier = result.getInt(1);
            } else {
                throw ContextExceptionCodes.NO_MAILADMIN.create(Integer.valueOf(ctxId));
            }
        } catch (final SQLException e) {
            throw ContextExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return identifier;
    }

    private String[] getLoginInfos(final Context ctx) throws OXException {
        final Connection con = DBPool.pickup();
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<String> loginInfo = new ArrayList<String>();
        try {
            stmt = con.prepareStatement(GET_LOGININFOS);
            stmt.setInt(1, ctx.getContextId());
            result = stmt.executeQuery();
            while (result.next()) {
                loginInfo.add(result.getString(1));
            }
        } catch (final SQLException e) {
            throw ContextExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(con);
        }
        return loginInfo.toArray(new String[loginInfo.size()]);
    }

    @Override
    public ContextExtended loadContext(final int contextId) throws OXException {
        final ContextImpl context = loadContextData(contextId);
        context.setLoginInfo(getLoginInfos(context));
        context.setMailadmin(getAdmin(context));
        loadAttributes(context);
        return context;
    }

    private void loadAttributes(final ContextImpl ctx) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = DBPool.pickup(ctx);

            stmt = con.prepareStatement("SELECT name, value FROM contextAttribute WHERE cid = ?");
            stmt.setInt(1, ctx.getContextId());
            result = stmt.executeQuery();
            while(result.next()) {
                final String name = result.getString(1);
                final String value = result.getString(2);
                ctx.addAttribute(name, value);
            }
        } catch (final SQLException e) {
            try {
                if(!Tools.tableExists(con, "contextAttribute")) {
                    // This would be an explanation for the exception. Will
                    // happen once for every schema.
                    return;
                }
            } catch (final SQLException e1) {
                // IGNORE
            }
            throw ContextExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            if (null != con) {
                DBPool.closeReaderSilent(ctx, con);
            }
        }
    }

    public ContextImpl loadContextData(final int contextId) throws OXException {
        final Connection con = DBPool.pickup();
        try {
            return loadContextData(con, contextId);
        } finally {
            DBPool.closeReaderSilent(con);
        }
    }

    public ContextImpl loadContextData(final Connection con, final int contextId) throws OXException {
        ContextImpl context = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_CONTEXT);
            stmt.setInt(1, contextId);
            result = stmt.executeQuery();
            if (result.next()) {
                context = new ContextImpl(contextId);
                int pos = 1;
                context.setName(result.getString(pos++));
                context.setEnabled(result.getBoolean(pos++));
                context.setFilestoreId(result.getInt(pos++));
                context.setFilestoreName(result.getString(pos++));
                final String[] auth = new String[2];
                auth[0] = result.getString(pos++);
                auth[1] = result.getString(pos++);
                context.setFilestoreAuth(auth);
                context.setFileStorageQuota(result.getLong(pos++));
            } else {
                throw ContextExceptionCodes.NOT_FOUND.create(I(contextId));
            }
        } catch (final SQLException e) {
            throw ContextExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getAllContextIds() throws OXException {
        Connection con = DBPool.pickup();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT cid FROM context");
            result = stmt.executeQuery();
            List<Integer> retval = new ArrayList<Integer>();
            while (result.next()) {
                retval.add(Integer.valueOf(result.getInt(1)));
            }
            return retval;
        } catch (final SQLException e) {
            throw ContextExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(con);
        }
    }

    @Override
    public List<Integer> getAllContextIdsForFilestore(int filestoreId) throws OXException {
        Connection con = DBPool.pickup();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT cid FROM context WHERE filestore_id = ?");
            stmt.setInt(1, filestoreId);
            result = stmt.executeQuery();
            List<Integer> retval = new ArrayList<Integer>();
            while (result.next()) {
                retval.add(Integer.valueOf(result.getInt(1)));
            }
            return retval;
        } catch (final SQLException e) {
            throw ContextExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(con);
        }
    }

    @Override
    protected void shutDown() {
        // Nothing to do.
    }

    @Override
    protected void startUp() {
        // Nothing to do.
    }

    /**
     * Stores a internal user attribute. Internal user attributes must not be exposed to clients through the HTTP/JSON API.
     * <p>
     * This method might throw a {@link ContextExceptionCodes#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY} error in case a concurrent modification occurred. The
     * caller can decide to treat as an error or to simply ignore it.
     *
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param contextId Identifier of the context that attribute should be set.
     * @throws OXException if writing the attribute fails.
     * @see ContextExceptionCodes#CONCURRENT_ATTRIBUTES_UPDATE
     */
    @Override
    public void setAttribute(String name, String value, int contextId) throws OXException {
        if (null == name) {
            throw LdapExceptionCode.UNEXPECTED_ERROR.create("Attribute name is null.").setPrefix("CTX");
        }
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = dbService.getWritable(contextId);
            if (value == null) {
                stmt = con.prepareStatement("DELETE FROM contextAttribute WHERE cid = ? AND name = ?");
                stmt.setInt(1, contextId);
                stmt.setString(2, name);
                stmt.executeUpdate();
            } else {
                insertOrUpdateAttribute(name, value, contextId, con);
            }
        } catch (SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("CTX");
        } finally {
            Databases.closeSQLStuff(stmt);
            if (con != null) {
                dbService.backWritable(contextId, con);
            }
        }
    }

    private static void insertOrUpdateAttribute(String name, String newValue, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;
            stmt = con.prepareStatement("SELECT value FROM contextAttribute WHERE cid=? AND name=?");
            stmt.setInt(1, contextId);
            stmt.setString(2, name);
            rs = stmt.executeQuery();
            List<String> toUpdate = new LinkedList<String>();
            while (rs.next()) {
                toUpdate.add(rs.getString(1));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            if (toUpdate.isEmpty()) {
                stmt = con.prepareStatement("INSERT INTO contextAttribute (cid,name,value) VALUES (?,?,?)");
                stmt.setInt(1, contextId);
                stmt.setString(2, name);
                stmt.setString(3, newValue);
                stmt.executeUpdate();
            } else {
                stmt = con.prepareStatement("UPDATE contextAttribute SET value=? WHERE cid=? AND name=? AND value=?");
                for (String oldValue : toUpdate) {
                    stmt.setString(1, newValue);
                    stmt.setInt(2, contextId);
                    stmt.setString(3, name);
                    stmt.setString(4, oldValue);
                    stmt.addBatch();
                }
                int[] updateCounts = stmt.executeBatch();
                for (int updateCount : updateCounts) {
                    // Concurrent modification of at least one attribute. We lost the race...
                    if (updateCount != 1) {
                        Logger logger = org.slf4j.LoggerFactory.getLogger(RdbContextStorage.class);
                        logger.error("Concurrent modification of attribute '{}' for context {}. New value '{}' could not be set.", name, I(contextId), newValue);
                        throw ContextExceptionCodes.CONCURRENT_ATTRIBUTES_UPDATE.create(I(contextId));
                    }
                }
            }
            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(stmt);
            Databases.autocommit(con);
        }
    }

}

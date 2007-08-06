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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.contexts;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import com.openexchange.groupware.contexts.ContextException.Code;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a storage for contexts in a relational database.
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbContextStorage extends ContextStorage {

    /**
     * SQL select statement for loading a context.
     */
    private static final String SELECT_CONTEXT =
        "SELECT enabled,filestore_id,filestore_name,filestore_login,"
        + "filestore_passwd,quota_max FROM context WHERE cid=?";

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
     * SQL select statement for reading the login informations of a context.
     */
    private static final String GET_LOGININFOS =
        "SELECT login_info FROM login2context WHERE cid=?";
    
    /**
     * Default constructor.
     */
    public RdbContextStorage() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public void close() {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public int getContextId(final String loginInfo) throws ContextException {
        Connection con = null;
        try {
            con = DBPool.pickup();
        } catch (DBPoolingException e) {
            throw new ContextException(Code.NO_CONNECTION, e);
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
        } catch (SQLException e) {
            throw new ContextException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(con);
        }
        return contextId;
    }

    /**
     * Reads the identifier of the mailadmin.
     * @param ctx Context
     * @return the unique identifier of mailadmin.
     * @throws ContextException if an error occurs.
     */
    private int getMailadmin(final Context ctx) throws ContextException {
        Connection con = null;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new ContextException(Code.NO_CONNECTION, e);
        }
        int identifier = -1;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(GET_MAILADMIN);
            final int contextId = ctx.getContextId();
            stmt.setInt(1, contextId);
            result = stmt.executeQuery();
            if (result.next()) {
                identifier = result.getInt(1);
            } else {
                throw new ContextException(Code.NO_MAILADMIN, Integer.valueOf(contextId));
            }
        } catch (SQLException e) {
            throw new ContextException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(ctx, con);
        }
        return identifier;
    }

    /**
     * Reads the login informations of a context.
     * @param ctx Context.
     * @return a string array with all login informations of a context.
     * @throws ContextException if loading the login informations fails.
     */
    private String[] getLoginInfos(final Context ctx) throws ContextException {
        Connection con = null;
        try {
            con = DBPool.pickup();
        } catch (DBPoolingException e) {
            throw new ContextException(Code.NO_CONNECTION, e);
        }
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
        } catch (SQLException e) {
            throw new ContextException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(con);
        }
        return loginInfo.toArray(new String[loginInfo.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
	protected ContextExtended loadContext(final int contextId) throws ContextException {
        Connection con = null;
        try {
            con = DBPool.pickup();
        } catch (DBPoolingException e) {
            throw new ContextException(Code.NO_CONNECTION, e);
        }
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
                context.setEnabled(result.getBoolean(pos++));
                context.setFilestoreId(result.getInt(pos++));
                context.setFilestoreName(result.getString(pos++));
                final String[] auth = new String[2];
                auth[0] = result.getString(pos++);
                auth[1] = result.getString(pos++);
                context.setFilestoreAuth(auth);
                context.setFileStorageQuota(result.getLong(pos++));
            }
        } catch (SQLException e) {
            throw new ContextException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(con);
        }
        if (null != context) {
            context.setMailadmin(getMailadmin(context));
            context.setLoginInfo(getLoginInfos(context));
        }
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public List<Integer> getAllContextIds() throws ContextException {
        final List<Integer> retval = new ArrayList<Integer>();
        Connection con = null;
        try {
            con = DBPool.pickup();
        } catch (DBPoolingException e) {
            throw new ContextException(Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT cid FROM context");
            result = stmt.executeQuery();
            while (result.next()) {
                retval.add(result.getInt(1));
            }
        } catch (SQLException e) {
            throw new ContextException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(con);
        }
        return retval;
    }
}

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

package com.openexchange.datatypes.genericonf.storage.impl;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.datatypes.genericonf.storage.GenericConfigStorageExceptionCode;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;

/**
 * {@link MySQLGenericConfigurationStorage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MySQLGenericConfigurationStorage implements GenericConfigurationStorageService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MySQLGenericConfigurationStorage.class);

    private DBProvider provider;

    public void setDBProvider(final DBProvider provider) {
        this.provider = provider;
    }

    @Override
    public int save(final Context ctx, final Map<String, Object> content) throws OXException {
        return save(null, ctx, content);
    }

    @Override
    public int save(final Connection con, final Context ctx, final Map<String, Object> content) throws OXException {
        return ((Integer) write(con, ctx, new TX() {

            @Override
            public Object perform() throws SQLException {
                final Connection con = getConnection();

                final InsertIterator insertIterator = new InsertIterator();
                insertIterator.prepareStatements(this);

                final int id = IDGenerator.getId(ctx, Types.GENERIC_CONFIGURATION, con);
                final int cid = ctx.getContextId();
                insertIterator.setIds(cid, id);

                Tools.iterate(content, insertIterator);
                insertIterator.close();
                insertIterator.throwException();
                return I(id);
            }

        })).intValue();
    }

    private Object write(final Connection con, final Context ctx, final TX tx) throws OXException {
        Connection writeCon = con;
        final boolean connectionHandling = con == null;
        try {
            if(connectionHandling) {
                writeCon = provider.getWriteConnection(ctx);
                writeCon.setAutoCommit(false);
            }
            tx.setConnection(writeCon);
            final Object retval = tx.perform();
            if(connectionHandling) {
                writeCon.commit();
            }
            return retval;
        } catch (final SQLException x) {
            if (connectionHandling) {
                try {
                    writeCon.rollback();
                } catch (final SQLException e) { /**/ }
            }
            LOG.error("", x);
            throw GenericConfigStorageExceptionCode.SQLException.create(x, x.getMessage());
        } finally {
            tx.close();
            if(connectionHandling) {
                try {
                    writeCon.setAutoCommit(true);
                } catch (final SQLException e) { /**/ }
                provider.releaseWriteConnection(ctx, writeCon);
            }
        }
    }

    @Override
    public void fill(final Context ctx, final int id, final Map<String, Object> content) throws OXException {
        fill(null, ctx, id, content);
    }

    @Override
    public void fill(final Connection con, final Context ctx, final int id, final Map<String, Object> content) throws OXException {
        Connection readCon = con;
        final boolean connectionHandling = con == null;
        try {
            if(connectionHandling) {
                readCon = provider.getReadConnection(ctx);
            }
            loadValues(readCon, ctx, id, content, "genconf_attributes_strings");
            loadValues(readCon, ctx, id, content, "genconf_attributes_bools");
        } finally {
            if(connectionHandling) {
                provider.releaseReadConnection(ctx, readCon);
            }
        }
    }

    private void loadValues(final Connection readCon, final Context ctx, final int id, final Map<String, Object> content, final String tablename) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            stmt = readCon.prepareStatement("SELECT name, value FROM "+tablename+" WHERE cid = ? AND id = ?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("name");
                final Object value = rs.getObject("value");

                content.put(name, value);
            }
        } catch (final SQLException x) {
            throw GenericConfigStorageExceptionCode.SQLException.create(x, null == stmt ? x.getMessage() : stmt.toString());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException x) {
                }

            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException x) {
                }
            }
        }
    }


    @Override
    public void update(final Context ctx, final int id, final Map<String, Object> content) throws OXException {
        update(null, ctx, id, content);
    }

    @Override
    public void update(final Connection con, final Context ctx, final int id, final Map<String, Object> content) throws OXException {
        final Map<String, Object> original = new HashMap<String, Object>();
        fill(con, ctx, id, original);

        write(con, ctx, new TX() {

            @Override
            public Object perform() throws SQLException {
                final UpdateIterator updateIterator = new UpdateIterator();
                try {
                    updateIterator.prepareStatements(this);
                    updateIterator.setIds(ctx.getContextId(), id);
                    updateIterator.setOriginal(original);

                    Tools.iterate(content, updateIterator);
                } finally {
                    updateIterator.close();
                }
                updateIterator.throwException();
                return null;
            }

        });

    }

    @Override
    public void delete(final Context ctx, final int id) throws OXException {
        delete(null, ctx, id);
    }

    @Override
    public void delete(final Connection con, final Context ctx, final int id) throws OXException {

        write(con, ctx, new TX() {

            @Override
            public Object perform() throws SQLException {
                clearTable("genconf_attributes_strings");
                clearTable("genconf_attributes_bools");
                return null;
            }

            private void clearTable(final String tablename) throws SQLException {
                PreparedStatement delete = null;
                delete = prepare("DELETE FROM "+tablename+" WHERE cid = ? AND id = ?");
                delete.setInt(1, ctx.getContextId());
                delete.setInt(2, id);
                delete.executeUpdate();

            }

        });

    }

    @Override
    public void delete(final Connection con, final Context ctx) throws OXException {

        write(con, ctx, new TX() {

            @Override
            public Object perform() throws SQLException {
                clearTable("genconf_attributes_strings");
                clearTable("genconf_attributes_bools");
                return null;
            }

            private void clearTable(final String tablename) throws SQLException {
                PreparedStatement delete = null;
                delete = prepare("DELETE FROM "+tablename+" WHERE cid = ?");
                delete.setInt(1, ctx.getContextId());
                delete.executeUpdate();
            }

        });

    }

    @Override
    public List<Integer> search(final Context ctx, final Map<String, Object> query) throws OXException {
        return search(null, ctx, query);
    }

    @Override
    public List<Integer> search(Connection con, final Context ctx, final Map<String, Object> query) throws OXException {
        final boolean handleOwnConnections = con == null;

        final LinkedList<Integer> list = new LinkedList<Integer>();
        final StringBuilder builder = new StringBuilder("SELECT DISTINCT p.id FROM ");
        final SearchIterator whereIterator = new SearchIterator();
        Tools.iterate(query, whereIterator);


        builder.append(whereIterator.getFrom());
        builder.append(" WHERE ");
        builder.append('(');
        builder.append(whereIterator.getWhere());
        builder.append(") AND p.cid = ?");
        whereIterator.addReplacement(I(ctx.getContextId()));
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if(handleOwnConnections) {
                con = provider.getReadConnection(ctx);
            }
            stmt = con.prepareStatement(builder.toString());
            whereIterator.setReplacements(stmt);
            rs = stmt.executeQuery();

            while(rs.next()) {
                list.add(I(rs.getInt(1)));
            }

        } catch (final SQLException e) {
            throw GenericConfigStorageExceptionCode.SQLException.create(e, null == stmt ? e.getMessage() : stmt.toString());
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException x) {
                    // Ignore
                }
            }
            if(rs != null) {
                try {
                    rs.close();
                } catch (final SQLException x) {
                    // Ignore
                }
            }
            if(handleOwnConnections) {
                provider.releaseReadConnection(ctx, con);
            }
        }

        return list;
    }

}

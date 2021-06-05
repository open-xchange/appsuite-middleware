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

package com.openexchange.groupware.infostore.webdav;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.getStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.webdav.protocol.WebdavProperty;

public class PropertyStoreImpl extends DBService implements PropertyStore {

    private String INSERT = "INSERT INTO %%tablename%% (cid, id, name, namespace, value, language, xml) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private String tablename;

    public PropertyStoreImpl(final String tablename){
        this(null,tablename);
    }

    public PropertyStoreImpl(final DBProvider provider, final String tablename) {
        setProvider(provider);
        initTable(tablename);
    }

    private void initTable(final String tablename) {
        INSERT = INSERT.replaceAll("%%tablename%%",tablename);
        this.tablename = tablename;
    }

    @Override
    public Map<Integer, List<WebdavProperty>> loadProperties(final List<Integer> entities, final List<WebdavProperty> properties, final Context ctx) throws OXException {
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final StringBuilder builder = new StringBuilder("SELECT id, name, namespace, value, language, xml FROM ");
        builder.append(tablename);
        builder.append(" WHERE CID = ? AND id IN (");
        join(entities, builder);
        builder.append(") AND (");
        addOr(builder,properties);
        builder.append(')');
        try {
            readCon = getReadConnection(ctx);
            stmt = readCon.prepareStatement(builder.toString());
            stmt.setInt(1, ctx.getContextId());
            addOr(stmt, properties, 1);

            rs = stmt.executeQuery();
            final Map<Integer, List<WebdavProperty>> retVal = new HashMap<Integer, List<WebdavProperty>>();
            while (rs.next()) {
                final Integer id = I(rs.getInt(1));
                List<WebdavProperty> props = retVal.get(id);
                if (props == null) {
                    props = new ArrayList<WebdavProperty>();
                    retVal.put(id, props);
                }
                props.add(getProperty(rs));
            }

            return retVal;
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, builder.toString());
        } catch (OXException e) {
            throw e;
        } finally {
            close(stmt,rs);
            releaseReadConnection(ctx, readCon);
        }
    }

    private WebdavProperty getProperty(final ResultSet rs) throws SQLException {
        final WebdavProperty prop = new WebdavProperty();
        prop.setName(rs.getString("name"));
        prop.setNamespace(rs.getString("namespace"));
        prop.setLanguage(rs.getString("language"));
        prop.setXML(rs.getBoolean("xml"));
        prop.setValue(rs.getString("value"));
        return prop;
    }

    @Override
    public List<WebdavProperty> loadProperties(final int entity, final List<WebdavProperty> properties, final Context ctx) throws OXException {
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final StringBuilder builder = new StringBuilder("SELECT id, name, namespace, value, language, xml FROM ");
        builder.append(tablename);
        builder.append(" WHERE CID = ? AND id = ");
        builder.append(entity);
        builder.append(" AND (");
        addOr(builder,properties);
        builder.append(')');
        try {
            readCon = getReadConnection(ctx);
            stmt = readCon.prepareStatement(builder.toString());
            stmt.setInt(1, ctx.getContextId());
            addOr(stmt, properties, 1);

            rs = stmt.executeQuery();
            final List<WebdavProperty> props = new ArrayList<WebdavProperty>();
            while (rs.next()) {
                props.add(getProperty(rs));
            }

            return props;
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, builder.toString());
        } catch (OXException e) {
            throw e;
        } finally {
            close(stmt,rs);
            releaseReadConnection(ctx, readCon);
        }
    }

    @Override
    public void saveProperties(final int entity, final List<WebdavProperty> properties, final Context ctx) throws OXException {
        Connection writeCon = null;
        PreparedStatement stmt = null;
        try {
            writeCon = getWriteConnection(ctx);
            removeProperties(entity, properties, ctx, writeCon);
            stmt = writeCon.prepareStatement(INSERT);
            stmt.setInt(1,ctx.getContextId());
            stmt.setInt(2, entity);
            for(final WebdavProperty prop : properties) {
                setValues(stmt, prop);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (OXException e) {
            throw e;
        } finally {
            close(stmt, null);
            releaseWriteConnection(ctx, writeCon);
        }
    }

    private final void setValues(final PreparedStatement stmt, final WebdavProperty prop) throws SQLException {
        stmt.setString(3, prop.getName());
        stmt.setString(4, prop.getNamespace());
        stmt.setString(5, prop.getValue());
        stmt.setString(6, prop.getLanguage());
        stmt.setBoolean(7, prop.isXML());
    }

    @Override
    public List<WebdavProperty> loadAllProperties(final int entity, final Context ctx) throws OXException {
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final StringBuilder builder = new StringBuilder("SELECT id, name, namespace, value, language, xml FROM ");
            builder.append(tablename);
            builder.append(" WHERE CID = ? AND id = ");
            builder.append(entity);
            readCon = getReadConnection(ctx);
            stmt = readCon.prepareStatement(builder.toString());
            stmt.setInt(1, ctx.getContextId());

            rs = stmt.executeQuery();
            final List<WebdavProperty> props = new ArrayList<WebdavProperty>();
            while (rs.next()) {
                props.add(getProperty(rs));
            }

            return props;
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (OXException e) {
            throw e;
        } finally {
            close(stmt,rs);
            releaseReadConnection(ctx, readCon);
        }
    }

    @Override
    public Map<Integer, List<WebdavProperty>> loadAllProperties(final List<Integer> entities, final Context ctx) throws OXException {
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final StringBuilder builder = new StringBuilder("SELECT id, name, namespace, value, language, xml FROM ");
            builder.append(tablename);
            builder.append(" WHERE CID = ? AND id IN (");
            join(entities, builder);
            builder.append(')');

            readCon = getReadConnection(ctx);
            stmt = readCon.prepareStatement(builder.toString());
            stmt.setInt(1, ctx.getContextId());

            rs = stmt.executeQuery();
            final Map<Integer, List<WebdavProperty>> retVal = new HashMap<Integer, List<WebdavProperty>>();
            while (rs.next()) {
                final Integer id = I(rs.getInt(1));
                List<WebdavProperty> props = retVal.get(id);
                if (props == null) {
                    props = new ArrayList<WebdavProperty>();
                    retVal.put(id, props);
                }
                props.add(getProperty(rs));
            }

            return retVal;
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (OXException e) {
            throw e;
        } finally {
            close(stmt,rs);
            releaseReadConnection(ctx, readCon);
        }
    }

    @Override
    public void removeAll(final List<Integer> entities, final Context ctx) throws OXException {
        Connection writeCon = null;
        PreparedStatement stmt = null;
        try {
            final StringBuilder b = new StringBuilder("DELETE FROM ");
            b.append(tablename);
            b.append(" WHERE cid = ");
            b.append(ctx.getContextId());
            b.append(" AND id IN (");
            join(entities,b);
            b.append(')');
            writeCon = getWriteConnection(ctx);
            stmt = writeCon.prepareStatement(b.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (OXException e) {
            throw e;
        } finally {
            close(stmt, null);
            releaseWriteConnection(ctx, writeCon);
        }
    }

    private final void join(final List<Integer> entities, final StringBuilder b) {
        for(final int entity : entities) {
            b.append(entity);
            b.append(',');
        }
        b.setLength(b.length()-1);
    }

    @Override
    public void removeProperties(final int entity, final List<WebdavProperty> properties, final Context ctx) throws OXException {
        Connection writeCon = null;
        final PreparedStatement stmt = null;
        try {
            writeCon = getWriteConnection(ctx);
            removeProperties(entity, properties, ctx, writeCon);
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (OXException e) {
            throw e;
        } finally {
            close(stmt, null);
            releaseWriteConnection(ctx, writeCon);
        }
    }

    private void removeProperties(final int entity, final List<WebdavProperty> properties, final Context ctx, final Connection writeCon) throws SQLException {
        if (properties.isEmpty()) {
            return;
        }
        PreparedStatement stmt = null;
        final StringBuilder builder = new StringBuilder("DELETE FROM ");
        builder.append(tablename);
        builder.append(" WHERE cid = ? AND id = ? AND (");
        addOr(builder,properties);
        builder.append(')');
        try {
            stmt = writeCon.prepareStatement(builder.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, entity);
            addOr(stmt, properties, 2);
            stmt.executeUpdate();
        } finally {
            close(stmt, null);
        }

    }

    private final void addOr(final PreparedStatement stmt, final List<WebdavProperty> properties, int count) throws SQLException {
        for(final WebdavProperty property : properties) {
            stmt.setString(++count, property.getName());
            stmt.setString(++count, property.getNamespace());
        }
    }

    private final void addOr(final StringBuilder builder, final List<WebdavProperty> properties) {
        final String append = "(name = ? AND namespace = ?) OR ";
        final int size = properties.size();
        for(int i = 0; i < size; i++) {
            builder.append(append);
        }
        builder.setLength(builder.length()-3);
    }

    @Override
    public void removeAll(final int entity, final Context ctx) throws OXException {
        removeAll(Arrays.asList(entity), ctx);
    }

}

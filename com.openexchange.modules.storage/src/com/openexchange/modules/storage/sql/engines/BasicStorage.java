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

package com.openexchange.modules.storage.sql.engines;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.AttributeHandler;
import com.openexchange.modules.model.Metadata;
import com.openexchange.modules.model.Model;
import com.openexchange.modules.model.Tools;
import com.openexchange.modules.storage.sql.Builder;
import com.openexchange.modules.storage.sql.SQLTools;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.Constant;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.ModifyCommand;
import com.openexchange.sql.grammar.Predicate;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.UPDATE;


/**
 * {@link BasicStorage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class BasicStorage<T extends Model<T>> implements Storage<T> {

    protected Metadata<T> metadata;

    protected Builder<T> builder = null;
    protected DatabaseService dbService;
    protected int ctxId;

    protected AttributeHandler<T> overridesFromDB = AttributeHandler.DO_NOTHING;
    protected AttributeHandler<T> overridesToDB = AttributeHandler.DO_NOTHING;


    public BasicStorage(final Metadata<T> metadata, final DatabaseService dbService, final int ctxId) {
        this.metadata = metadata;
        this.dbService = dbService;
        this.ctxId = ctxId;
        builder = new Builder<T>(metadata);
    }

    public void setOverridesFromDB(final AttributeHandler<T> overrides) {
        this.overridesFromDB = overrides;
    }

    public void setOverridesToDB(final AttributeHandler<T> overrides) {
        this.overridesToDB = overrides;
    }

    @Override
    public void create(final T thing) throws SQLException, OXException {
        final List<Attribute<T>> attributes = getAttributes();
        final INSERT insert = builder.insert(attributes, getExtraFields());
        final List<Object> values = Tools.values(thing, attributes, overridesToDB);
        values.addAll(getExtraValues());
        executeUpdate(insert, values);
    }


    protected List<Attribute<T>> getAttributes() {
        return metadata.getPersistentFields();
    }

    @Override
    public T load(final Object id) throws SQLException, OXException {
        final List<Attribute<T>> attributes = getAttributes();
        final SELECT select = builder.select(attributes);
        final List<Object> primaryKey = primaryKey(id);

        return executeQuery(select, primaryKey, new ResultSetHandler<T>() {

            @Override
            public T handle(final ResultSet rs) throws SQLException {
                if(!rs.next()){
                    return null;
                }
                final T thing = metadata.create();
                SQLTools.fillObject(rs, thing, attributes, overridesFromDB);
                return thing;
            }

        });

    }


    @Override
    public List<T> load() throws SQLException, OXException {
        final List<Attribute<T>> attributes = getAttributes();
        final SELECT select = builder.selectWithoutWhere(attributes);
        final List<String> extraFields = getExtraFields();

        Predicate predicate = null;
        for (final String field : extraFields) {
            final Predicate old = predicate;
            predicate = new EQUALS(field, Constant.PLACEHOLDER);
            if(old != null) {
                predicate = old.AND(predicate);
            }
        }

        select.WHERE(predicate);

        final List<Object> values = getExtraValues();



        return executeQuery(select, values, new ResultSetHandler<List<T>>() {

            @Override
            public List<T> handle(final ResultSet rs) throws SQLException {
                final LinkedList<T> list = new LinkedList<T>();
                while(rs.next()) {
                    final T thing = metadata.create();
                    SQLTools.fillObject(rs, thing, attributes, overridesFromDB);
                    list.add(thing);
                }
                return list;
            }

        });
    }

    @Override
    public void update(final T thing, List<? extends Attribute<T>> updatedAttributes) throws SQLException, OXException {
        updatedAttributes = new ArrayList<Attribute<T>>(updatedAttributes);
        updatedAttributes.remove(metadata.getIdField());
        updatedAttributes.retainAll(getAttributes());
        if(updatedAttributes.isEmpty()) {
            return;
        }
        final UPDATE update = builder.update(updatedAttributes);

        final List<Object> values = Tools.values(thing, updatedAttributes, overridesToDB);
        values.addAll(primaryKey(thing.get(metadata.getIdField())));

        executeUpdate(update, values);

    }

    @Override
    public void delete(final Object id) throws SQLException, OXException {
        final DELETE delete = builder.delete();

        final List<Object> primaryKey = primaryKey(id);

        executeUpdate(delete, primaryKey);
    }

    public boolean exists(final Object id) throws SQLException, OXException {
        final SELECT select = new SELECT(metadata.getIdField().getName()).FROM(builder.getTableName());
        select.WHERE(builder.matchOne());

        final List<Object> primaryKey = primaryKey(id);

        return executeQuery(select, primaryKey, new ResultSetHandler<Boolean>() {

            @Override
            public Boolean handle(final ResultSet rs) throws SQLException {
                return rs.next();
            }

        });
    }

    protected List<Object> primaryKey(Object id) {
        final Object overridden = overridesToDB.handle(metadata.getIdField(), id);
        if(overridden != null) {
            id = overridden;
        }
        final LinkedList<Object> primaryKey = new LinkedList<Object>();
        primaryKey.add(id);
        primaryKey.add(ctxId);
        return primaryKey;
    }

    protected List<String> getExtraFields() {
        return new LinkedList<String>(Arrays.asList("cid"));
    }

    protected List<Object> getExtraValues() {
        return new LinkedList<Object>(Arrays.asList(ctxId));
    }

    protected <M> M executeQuery(final SELECT select, final List<Object> values, final ResultSetHandler<M> handler) throws SQLException, OXException {
        Connection con = null;
        ResultSet rs = null;
        final StatementBuilder sBuilder = new StatementBuilder();
        try {
            con = dbService.getReadOnly(ctxId);
            rs = sBuilder.executeQuery(con, select, values);
            return handler.handle(rs);

        } finally {
            sBuilder.closePreparedStatement(null, rs);
            if(con != null) {
                dbService.backReadOnly(ctxId, con);
            }
        }
    }

    protected void executeUpdate(final ModifyCommand command, final List<Object> values) throws OXException, SQLException {
        Connection con = null;

        try {
            con = dbService.getWritable(ctxId);
            final StatementBuilder sBuilder = new StatementBuilder();
            sBuilder.executeStatement(con, command, values);

        } finally {
            if(con != null) {
                try {
                    con.rollback();
                } catch (final SQLException x) {
                    // IGNORE
                }
                dbService.backWritable(ctxId, con);
            }
        }
    }

}

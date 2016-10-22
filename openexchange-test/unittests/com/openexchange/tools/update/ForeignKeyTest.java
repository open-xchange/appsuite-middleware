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
package com.openexchange.tools.update;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.setuptools.TestConfig;
import junit.framework.TestCase;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ForeignKeyTest extends TestCase {
    private Context ctx;
    private Connection con;

    @Override
	public void setUp() throws Exception {
		Init.startServer();
		final ContextStorage ctxstor = ContextStorage.getInstance();
        final TestConfig config = new TestConfig();
        final int contextId = ctxstor.getContextId(config.getContextName());
        ctx = ctxstor.getContext(contextId);
		con = DBPool.pickupWriteable(ctx);

		_sql_update("CREATE TABLE test_parent (id int, PRIMARY KEY (id)) ENGINE=InnoDB");
        _sql_update("CREATE TABLE test_child (parent_id int, FOREIGN KEY (parent_id) REFERENCES test_parent(id)) ENGINE=InnoDB");

    }

    @Override
    public void tearDown() throws Exception {
        _sql_update("DROP TABLE test_child");
        _sql_update("DROP TABLE test_parent");
        DBPool.closeWriterSilent(ctx, con);
        Init.stopServer();
    }


    public void testDiscoverForeignKeys() throws SQLException {
        List<ForeignKeyOld> foreignKeys = ForeignKeyOld.getForeignKeys(con, "test_child");

        assertNotNull(foreignKeys);
        assertEquals(1, foreignKeys.size());

        ForeignKeyOld key = foreignKeys.get(0);

        assertEquals("test_child", key.getSourceTable());
        assertEquals("test_parent", key.getTargetTable());
        assertEquals("parent_id", key.getSourceColumn());
        assertEquals("id", key.getTargetColumn());
        assertNotNull(key.getName());


    }

    public void testDropForeignKey() throws SQLException {
        new ForeignKeyOld("test_child", "parent_id", "test_parent", "id").drop(con);
        List<ForeignKeyOld> foreignKeys = ForeignKeyOld.getForeignKeys(con, "test_child");
        assertTrue(foreignKeys.isEmpty());
    }

    public void testCreateForeignKey() throws SQLException {
        ForeignKeyOld key = new ForeignKeyOld("test_child", "parent_id", "test_parent", "id");
        key.drop(con);
        List<ForeignKeyOld> foreignKeys = ForeignKeyOld.getForeignKeys(con, "test_child");
        assertTrue(foreignKeys.isEmpty());
        key.create(con);

        foreignKeys = ForeignKeyOld.getForeignKeys(con, "test_child");
        assertEquals(1, foreignKeys.size());
        assertEquals(key, foreignKeys.get(0));
    }

    public void testCreateIfNotExists() throws SQLException {
        ForeignKeyOld key = new ForeignKeyOld("test_child", "parent_id", "test_parent", "id");
        key.createIfNotExists(con);
        key.drop(con);
        key.createIfNotExists(con);
        List<ForeignKeyOld> foreignKeys = ForeignKeyOld.getForeignKeys(con, "test_child");
        assertEquals(1, foreignKeys.size());
        assertEquals(key, foreignKeys.get(0));
    }


    public void _sql_update(final String sql) throws Exception {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate(sql);
		} finally {
			if(stmt != null) {
				stmt.close();
			}
		}
	}
}

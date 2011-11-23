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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.index.internal;

import java.sql.Connection;
import java.sql.ResultSet;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexUrl;
import com.openexchange.tools.sql.SQLTestCase;


/**
 * {@link IndexMappingRoundtripTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexMappingRoundtripTest extends SQLTestCase {

    private Connection con;

    private DBProvider dbProvider;

    private IndexServerImpl indexServer;

    private int cid;

    private int uid;

    private int module;

    private String index;

    private int serverId;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        dbProvider = getDBProvider();
        con = dbProvider.getWriteConnection(null);
        con.createStatement().executeUpdate("DELETE FROM index_servers");
        con.createStatement().executeUpdate("DELETE FROM user_module2index");
        indexServer = createIndexServer();
        cid = 1;
        uid = 2;
        module = 3;
        index = "test";
        serverId = ConfigIndexMysql.getInstance().createIndexMapping(con, cid, uid, module, index);
        assertEquals("Server ids were not equal.", indexServer.getId(), serverId);
    }

    public void testIndexMappingModification() throws Exception {
        IndexServerImpl newServer = createIndexServer();
        String newIndex = "tset";
        ConfigIndexMysql.getInstance().modifiyIndexMapping(con, cid, uid, module, newServer.getId(), newIndex);
        ResultSet rs = con.createStatement().executeQuery("SELECT server, indexName FROM user_module2index WHERE cid = " + cid + " AND uid = " + uid + " AND module = " + module);
        try {
            if (rs.next()) {
                int newId = rs.getInt(1);
                String newIndex2 = rs.getString(2);
                assertEquals("Server Ids were not equal.", newServer.getId(), newId);
                assertEquals("Indices were not equal.", newIndex, newIndex2);
            } else {
                fail("Mapping not found.");
            }
        } finally {
            rs.close();
        }
    }

    public void testIndexMappingDeletion() throws Exception {
        ConfigIndexMysql.getInstance().removeIndexMapping(con, cid, uid, module);
        ResultSet rs = con.createStatement().executeQuery("SELECT server, indexName FROM user_module2index WHERE cid = " + cid + " AND uid = " + uid + " AND module = " + module);
        try {
            if (rs.next()) {
                fail("Index mapping was not deleted.");
            }
        } finally {
            rs.close();
        }
    }

    public void testGetIndexUrl() throws Exception {
        IndexUrl indexUrl = ConfigIndexMysql.getInstance().getIndexUrl(con, cid, uid, module);
        assertEquals("Server url was wrong.", indexServer.getUrl() + "/" + index, indexUrl.getUrl());
    }

    @Override
    protected void tearDown() throws Exception {
        con.createStatement().executeUpdate("DELETE FROM user_module2index");
        con.createStatement().executeUpdate("DELETE FROM index_servers");
        dbProvider.releaseWriteConnection(null, con);
        super.tearDown();
    }

    private IndexServerImpl createIndexServer() throws OXException {
        return IndexTestTool.createIndexServer(con);
    }

}

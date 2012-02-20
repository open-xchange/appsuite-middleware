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
import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexServer;
import com.openexchange.tools.sql.SQLTestCase;


/**
 * {@link IndexServerRoundtripTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexServerRoundtripTest extends SQLTestCase {

    private Connection con;

    private DBProvider dbProvider;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        dbProvider = getDBProvider();
        con = dbProvider.getWriteConnection(null);
        con.createStatement().executeUpdate("DELETE FROM solrServers");
    }

    public void testIndexServerCreation() throws OXException {
        final IndexServerImpl expected = createIndexServer();

        // List all servers and check the inserted one
        final List<IndexServer> servers = ConfigIndexMysql.getInstance().getAllIndexServers(con);
        boolean found = false;
        for (final IndexServer actual : servers) {
            if (expected.getId() == actual.getId()) {
                found = true;
                assertServers(expected, actual);
                break;
            }
        }

        if (!found) {
            fail("Did not find the inserted server.");
        }
    }

    public void testIndexServerModification() throws OXException {
        final IndexServerImpl expected = createIndexServer();
        expected.setConnectionTimeout(32);
        expected.setMaxConnectionsPerHost(45);
        expected.setMaxIndices(21);
        expected.setSoTimeout(64);
        expected.setUrl("http://4.3.2.1:5008");
        ConfigIndexMysql.getInstance().updateIndexServerEntry(con, expected);

        // List all servers and check the modified one
        final List<IndexServer> servers = ConfigIndexMysql.getInstance().getAllIndexServers(con);
        boolean found = false;
        for (final IndexServer actual : servers) {
            if (expected.getId() == actual.getId()) {
                found = true;
                assertServers(expected, actual);
                break;
            }
        }

        if (!found) {
            fail("Did not find the modified server.");
        }
    }

    public void testIndexServerDeletion() throws OXException {
        final IndexServerImpl expected = createIndexServer();

        // List all servers and check the inserted one
        List<IndexServer> servers = ConfigIndexMysql.getInstance().getAllIndexServers(con);
        boolean found = false;
        for (final IndexServer actual : servers) {
            if (expected.getId() == actual.getId()) {
                found = true;
                assertServers(expected, actual);
                break;
            }
        }

        if (!found) {
            fail("Did not find the modified server.");
        }

        ConfigIndexMysql.getInstance().removeIndexServerEntry(con, expected.getId());
        servers = ConfigIndexMysql.getInstance().getAllIndexServers(con);
        found = false;
        for (final IndexServer actual : servers) {
            if (expected.getId() == actual.getId()) {
                found = true;
                break;
            }
        }

        if (found) {
            fail("Server was not deleted");
        }
    }

    private IndexServerImpl createIndexServer() throws OXException {
        return IndexTestTool.createIndexServer(con);
    }

    private void assertServers(final IndexServer indexServer, final IndexServer actual) {
        assertEquals("Id and Url were not equal.", indexServer, actual);
        assertEquals("Connection timeout was not equal.", indexServer.getConnectionTimeout(), actual.getConnectionTimeout());
        assertEquals("Max connections per host was not equal.", indexServer.getMaxConnectionsPerHost(), actual.getMaxConnectionsPerHost());
        assertEquals("Max indices was not equal.", indexServer.getMaxIndices(), actual.getMaxIndices());
        assertEquals("Socket timeout was not equal.", indexServer.getSoTimeout(), actual.getSoTimeout());
    }

    @Override
    protected void tearDown() throws Exception {
        con.createStatement().executeUpdate("DELETE FROM solrServers");
        dbProvider.releaseWriteConnection(null, con);
        super.tearDown();
    }

}

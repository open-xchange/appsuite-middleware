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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexServer;
import com.openexchange.tools.sql.SQLTestCase;


/**
 * {@link MultithreadedMappingCreationTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MultithreadedMappingCreationTest extends SQLTestCase {

    private Connection con;

    private DBProvider dbProvider;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dbProvider = getDBProvider();
        con = dbProvider.getWriteConnection(null);
        con.createStatement().executeUpdate("DELETE FROM index_servers");
        con.createStatement().executeUpdate("DELETE FROM user_module2index");
    }

    public void testMultithreadedMappingCreation() throws Exception {
        for (int i = 0; i < 30; i++) {
            List<IndexServer> servers = createMultipleServers();
            int capacity = 0;
            for (IndexServer server : servers) {
                capacity += server.getMaxIndices();
            }
            int perThread = capacity / 3;
            MappingCreator creator1 = new MappingCreator(1, perThread, dbProvider);
            MappingCreator creator2 = new MappingCreator(2, perThread, dbProvider);
            MappingCreator creator3 = new MappingCreator(3, perThread, dbProvider);
            Thread thread1 = new Thread(creator1);
            Thread thread2 = new Thread(creator2);
            Thread thread3 = new Thread(creator3);
            thread1.start();
            thread2.start();
            thread3.start();
            thread1.join();
            thread2.join();
            thread3.join();
            if (creator1.hasError() || creator2.hasError() || creator3.hasError()) {
                fail("Errors occurred during mapping creation.");
            }
            ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM user_module2index");
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            if (count != capacity) {
                fail("Not all mappings were created.");
            }
            for (IndexServer server : servers) {
                int maxIndices = server.getMaxIndices();
                int sum = 0;
                Integer c1 = creator1.getCreated().get(server.getId());
                if (c1 != null) {
                    sum += c1.intValue();
                }
                Integer c2 = creator2.getCreated().get(server.getId());
                if (c2 != null) {
                    sum += c2.intValue();
                }
                Integer c3 = creator3.getCreated().get(server.getId());
                if (c3 != null) {
                    sum += c3.intValue();
                }

                assertEquals("Did not create the right amount of indices per server.", maxIndices, sum);
            }

            con.createStatement().executeUpdate("DELETE FROM index_servers");
            con.createStatement().executeUpdate("DELETE FROM user_module2index");
        }
    }

    @Override
    protected void tearDown() throws Exception {
        con.createStatement().executeUpdate("DELETE FROM user_module2index");
        con.createStatement().executeUpdate("DELETE FROM index_servers");
        dbProvider.releaseWriteConnection(null, con);
        super.tearDown();
    }

    private List<IndexServer> createMultipleServers() throws OXException {
        IndexServerImpl server1 = IndexTestTool.createIndexServer(con);
        IndexServerImpl server2 = IndexTestTool.createIndexServer(con);
        IndexServerImpl server3 = IndexTestTool.createIndexServer(con);

        List<IndexServer> servers = new ArrayList<IndexServer>();
        servers.add(server1);
        servers.add(server2);
        servers.add(server3);
        return servers;
    }

    private static final class MappingCreator implements Runnable {

        private final int cid;

        private final Connection con;

        private final DBProvider dbProvider;

        private final int amount;

        private boolean hasError;

        private final Map<Integer, Integer> created;

        public MappingCreator(int cid, int amount, DBProvider dbProvider) throws OXException {
            super();
            this.cid = cid;
            this.amount = amount;
            this.dbProvider = dbProvider;
            con = dbProvider.getWriteConnection(null);
            hasError = false;
            created = new HashMap<Integer, Integer>();
        }

        @Override
        public void run() {
            try {
                int uid = 1;
                String index = "index";
                for (int i = 0; i < amount; i++) {
                    try {
                        int serverId = ConfigIndexMysql.getInstance().createIndexMapping(con, cid, uid, i, index);
                        if (created.containsKey(serverId)) {
                            Integer count = created.get(serverId);
                            int newCount = count.intValue() + 1;
                            created.put(serverId, newCount);
                        } else {
                            created.put(serverId, 1);
                        }
                    } catch (OXException e) {
                        e.printStackTrace();
                        hasError = true;
                    }
                }
            } finally {
                dbProvider.releaseWriteConnection(null, con);
            }
        }

        public boolean hasError() {
            return hasError;
        }

        public Map<Integer, Integer> getCreated() {
            return created;
        }
    }

}

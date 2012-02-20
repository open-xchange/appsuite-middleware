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
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.tools.sql.SQLTestCase;


/**
 * {@link ConfigIndexMysqlTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ConfigIndexMysqlTest extends SQLTestCase {
    
    private Connection con;

    private DBProvider dbProvider;
    
    private ConfigIndexMysql indexMysql;
    
    private final int cid = 1;
    
    private final int uid = 2;
    
    private final int module = 3;
    
    private final String indexFile = "c1_u2_m3";
    
    private final String coreName = "c1_u2_m3_s1";


    @Override
    public void setUp() throws Exception {
        super.setUp();
        dbProvider = getDBProvider();        
        indexMysql = ConfigIndexMysql.getInstance();
        con = dbProvider.getWriteConnection(null);
        con.createStatement().executeUpdate("DELETE FROM solrCores");
        con.createStatement().executeUpdate("DELETE FROM solrIndexFiles");
    }
    
    public void testHasActiveCoreCreatesCoreEntry() throws Exception {
        assertFalse("There was an active core although it should not.", indexMysql.hasActiveCore(con, cid, uid, module));        
        final SolrCore core = indexMysql.getSolrCore(con, cid, uid, module);
        assertNotNull(core);
        assertNull(core.getCoreName());
        assertNull(core.getServer());
    }
    
    public void testGetIndexFile() throws Exception {
        try {
            indexMysql.getIndexFile(con, cid, uid, module);
        } catch (final OXException e) {
            assertTrue("Wrong exception thrown.", IndexExceptionCodes.INDEX_NOT_FOUND.equals(e));
        }
        
        indexMysql.createIndexFileEntry(con, cid, uid, module, indexFile);
        final String file = indexMysql.getIndexFile(con, cid, uid, module);
        assertEquals(indexFile, file);
    }
    
    public void testGetSolrCore() throws Exception {
        // Create and activate core
        final IndexServerImpl server = IndexTestTool.createIndexServer();
        final int serverId = indexMysql.createIndexServerEntry(con, server);
        server.setId(serverId);
        assertFalse("There was an active core although it should not.", indexMysql.hasActiveCore(con, cid, uid, module));
        assertTrue("Could not activate core.", indexMysql.activateCoreEntry(con, cid, uid, module, coreName, serverId));
        
        SolrCore core = indexMysql.getSolrCore(con, cid, uid, module);
        assertNotNull(core);
        assertEquals(coreName, core.getCoreName());
        assertEquals(server, core.getServer());       
        
        assertTrue("Could not deactivate core.", indexMysql.deactivateCoreEntry(con, cid, uid, module));
        core = indexMysql.getSolrCore(con, cid, uid, module);
        assertNotNull(core);
        assertNull(core.getCoreName());
        assertNull(core.getServer());
    }
    
    public void testConcurrentCoreActivation() throws Exception {
        final IndexServerImpl server = IndexTestTool.createIndexServer();
        final int serverId = indexMysql.createIndexServerEntry(con, server);
        indexMysql.createCoreEntry(con, cid, uid, module);
        
        final Thread t1 = new Thread(new ActivationRunner(dbProvider.getWriteConnection(null), cid, uid, module, indexMysql, serverId, "t1_"));
        final Thread t2 = new Thread(new ActivationRunner(dbProvider.getWriteConnection(null), cid, uid, module, indexMysql, serverId, "t2_"));
        final Thread t3 = new Thread(new ActivationRunner(dbProvider.getWriteConnection(null), cid, uid, module, indexMysql, serverId, "t3_"));
        
        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();
    }
    
    @Override
    protected void tearDown() throws Exception {
        con.createStatement().executeUpdate("DELETE FROM solrCores");
        con.createStatement().executeUpdate("DELETE FROM solrIndexFiles");
        dbProvider.releaseWriteConnection(null, con);
        super.tearDown();
    }
    
    private static final class ActivationRunner implements Runnable {
        
        private final int cid;
        
        private final int uid;
        
        private final int module;
        
        private final ConfigIndexMysql indexMysql;
        
        private final Connection con;
        
        private final String prefix;
        
        private final int serverId;
        
        
        public ActivationRunner(final Connection con, final int cid, final int uid, final int module, final ConfigIndexMysql indexMysql, final int serverId, final String prefix) {
            super();
            this.con = con;
            this.cid = cid;
            this.uid = uid;
            this.module = module;
            this.indexMysql = indexMysql;
            this.prefix = prefix;
            this.serverId = serverId;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    final String name = prefix + ++i;
                    System.out.println("Activating: " + name);
                    final boolean winner = indexMysql.activateCoreEntry(con, cid, uid, module, name, serverId);
                    final SolrCore solrCore = indexMysql.getSolrCore(con, cid, uid, module);
                    if (winner) {   
                        System.out.println("Winner: " + name);
                        assertEquals(name, solrCore.getCoreName());
                    } else {
                        assertNotNull(solrCore.getCoreName());
                    }
                    System.out.println("Deactivating: " + name);
                    indexMysql.deactivateCoreEntry(con, cid, uid, module);
                } catch (final OXException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Thread.yield();
            }
                        
        }        
    }

}

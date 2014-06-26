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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.index.solr.internal;

import java.net.URI;
import java.sql.Connection;
import java.util.List;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.solr.SolrCore;
import com.openexchange.solr.SolrCoreStore;
import com.openexchange.solr.SolrExceptionCodes;
import com.openexchange.solr.internal.SolrIndexMysql;
import com.openexchange.tools.sql.SQLTestCase;


/**
 * {@link ConfigIndexMysqlTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ConfigIndexMysqlTest extends SQLTestCase {

    private Connection con;

    private DBProvider dbProvider;

    private SolrIndexMysql indexMysql;

    private final int cid = 1;

    private final int uid = 2;

    private final int module = 3;

    private final String server = "http://localhost";


    @Override
    public void setUp() throws Exception {
        super.setUp();
        dbProvider = getDBProvider();
        indexMysql = SolrIndexMysql.getInstance();
        con = dbProvider.getWriteConnection(null);
        con.createStatement().executeUpdate("DELETE FROM solrCores");
        con.createStatement().executeUpdate("DELETE FROM solrCoreStores");
    }

    public void testHasActiveCoreCreatesCoreEntry() throws Exception {
        createStore();
        assertFalse("There was an active core although it should not.", indexMysql.hasActiveCore(con, cid, uid, module));
        final SolrCore core = indexMysql.getSolrCore(con, cid, uid, module);
        assertNotNull(core);
        assertNull(core.getServer());
    }

    public void testGetSolrCore() throws Exception {
        createStore();
        // Create and activate core
        assertFalse("There was an active core although it should not.", indexMysql.hasActiveCore(con, cid, uid, module));
        assertTrue("Could not activate core.", indexMysql.activateCoreEntry(con, cid, uid, module, server));

        SolrCore core = indexMysql.getSolrCore(con, cid, uid, module);
        assertNotNull(core);
        assertEquals(server, core.getServer());

        assertTrue("Could not deactivate core.", indexMysql.deactivateCoreEntry(con, cid, uid, module));
        core = indexMysql.getSolrCore(con, cid, uid, module);
        assertNotNull(core);
        assertNull(core.getServer());
    }

    public void testCoreStoreRoundtrip() throws Exception {
        final int sid1 = createStore();
        final int sid2 = createStore();

        final List<SolrCoreStore> stores = indexMysql.getCoreStores(con);
        assertEquals("Wrong number of stores.", 2, stores.size());
        for (final SolrCoreStore store : stores) {
            assertTrue(store.getId() == sid1 || store.getId() == sid2);
        }

        final SolrCoreStore store = stores.get(0);
        store.setMaxCores(99);
        store.setUri(new URI("file:/sonstwo"));
        indexMysql.updateCoreStoreEntry(con, store);
        final SolrCoreStore reloaded = indexMysql.getCoreStore(con, store.getId());
        assertEquals("MaxCores not equal", store.getMaxCores(), reloaded.getMaxCores());
        assertEquals("Uri not equal", store.getUri(), reloaded.getUri());

        indexMysql.removeCoreStoreEntry(con, store.getId());
        try {
            indexMysql.getCoreStore(con, store.getId());
        } catch (final OXException e) {
            assertTrue("Wrong exception.", e.similarTo(SolrExceptionCodes.CORE_STORE_ENTRY_NOT_FOUND));
            return;
        }

        fail("You should not get here.");

    }

    public void testChooseOfCoreStore() throws Exception {
        final SolrCoreStore store1 = new SolrCoreStore();
        final int places = 10;

        store1.setMaxCores(places);
        store1.setUri(new URI("file:/tmp/nowhere"));
        final int sid1 = indexMysql.createCoreStoreEntry(con, store1);
        store1.setId(sid1);

        final SolrCoreStore store2 = new SolrCoreStore();
        store2.setMaxCores(places);
        store2.setUri(new URI("file:/tmp/nowhere/else"));
        final int sid2 = indexMysql.createCoreStoreEntry(con, store2);
        store2.setId(sid2);

        int otherId = -1;
        for (int i = 0; i < (2 * places); i++) {
            indexMysql.createCoreEntry(con, cid, uid, i);
            final SolrCore core = indexMysql.getSolrCore(con, cid, uid, i);

            if (i == 0) {
                if (core.getStore() == sid1) {
                    otherId = sid2;
                } else {
                    otherId = sid1;
                }
            }

            final int coreId = core.getStore();
            if ((i % 2) != 0)  {
                assertEquals("Wrong store.", coreId, otherId);
            }
        }
    }

    private int createStore() throws Exception {
        final SolrCoreStore store = new SolrCoreStore();
        store.setMaxCores(10);
        store.setUri(new URI("file:/tmp/nowhere"));
        return indexMysql.createCoreStoreEntry(con, store);
    }

    @Override
    protected void tearDown() throws Exception {
        con.createStatement().executeUpdate("DELETE FROM solrCores");
        con.createStatement().executeUpdate("DELETE FROM solrCoreStores");
        dbProvider.releaseWriteConnection(null, con);
        super.tearDown();
    }

}

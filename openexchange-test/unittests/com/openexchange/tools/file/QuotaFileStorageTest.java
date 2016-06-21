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

package com.openexchange.tools.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.database.Assignment;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.impl.DBQuotaFileStorage;
import com.openexchange.filestore.impl.LocalFileStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.tools.RandomString;

public class QuotaFileStorageTest extends TestCase {

    private com.openexchange.filestore.FileStorage fs;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBasic() throws Exception{
        // Taken from FileStorageTest
        final File tempFile = File.createTempFile("filestorage", ".tmp");

        tempFile.deleteOnExit();

        tempFile.delete();

        fs = new LocalFileStorage(new URI("file:"+tempFile.getAbsolutePath()));

        SimpleServiceLookup slk = new SimpleServiceLookup();
        slk.add(DatabaseService.class, new DummyDatabaseService());
        com.openexchange.filestore.impl.osgi.Services.setServiceLookup(slk);
        final TestQuotaFileStorage quotaStorage = new TestQuotaFileStorage(new ContextImpl(1), fs);

        quotaStorage.setQuota(10000);
        // And again, some lines from the original test
        final String fileContent = RandomString.generateLetter(100);
        final ByteArrayInputStream bais = new ByteArrayInputStream(fileContent
            .getBytes(com.openexchange.java.Charsets.UTF_8));

        final String id = quotaStorage.saveNewFile(bais);

        assertEquals(fileContent.getBytes(com.openexchange.java.Charsets.UTF_8).length, quotaStorage.getUsage());
        assertEquals(fileContent.getBytes(com.openexchange.java.Charsets.UTF_8).length, quotaStorage.getFileSize(id));


        quotaStorage.deleteFile(id);

        assertEquals(0,quotaStorage.getUsage());
        rmdir(tempFile);
    }

    public void testFull() throws Exception{
        final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();

        tempFile.delete();

        fs = new LocalFileStorage(new URI("file://"+tempFile.getAbsolutePath()));
        SimpleServiceLookup slk = new SimpleServiceLookup();
        slk.add(DatabaseService.class, new DummyDatabaseService());
        com.openexchange.filestore.impl.osgi.Services.setServiceLookup(slk);
        final TestQuotaFileStorage quotaStorage = new TestQuotaFileStorage(new ContextImpl(1), fs);
        quotaStorage.setQuota(10000);

        final String fileContent = RandomString.generateLetter(100);

        quotaStorage.setQuota(fileContent.getBytes(com.openexchange.java.Charsets.UTF_8).length-2);

        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(fileContent.getBytes(com.openexchange.java.Charsets.UTF_8));
            quotaStorage.saveNewFile(bais);
            fail("Managed to exceed quota");
        } catch (final OXException x) {
            assertTrue(true);
        }
        rmdir(tempFile);
    }


    public static final class TestQuotaFileStorage extends DBQuotaFileStorage {

        public TestQuotaFileStorage(final Context ctx, final com.openexchange.filestore.FileStorage fs) throws OXException {
            super(ctx.getContextId(), -1, 0L, fs, null);
        }

        private long usage;
        private long quota;

        public void setQuota(final long quota){
            this.quota = quota;
        }

        @Override
        public long getQuota() {
            return quota;
        }

        @Override
        public long getUsage() {
            return usage;
        }

        protected void setUsage(final long usage) {
            this.usage = usage;
        }

        @Override
        protected boolean incUsage(final long added) {
            boolean full = false;
            if (this.usage + added <= this.quota) {
                this.usage += added;
            } else {
                full = true;
            }
            return full;
        }

        @Override
        protected void decUsage(final long removed) {
            this.usage -= removed;
        }
    }



    static final class DummyDatabaseService implements DatabaseService {

        @Override
        public void back(final int poolId, final Connection con) {
            // Nothing to do.
        }

        @Override
        public void backForUpdateTask(final int contextId, final Connection con) {
            // Nothing to do.
        }

        @Override
        public void backForUpdateTaskAfterReading(Connection con) {
            // Nothing to do.
        }

        @Override
        public void backReadOnly(final Context ctx, final Connection con) {
            // Nothing to do.
        }

        @Override
        public void backReadOnly(final int contextId, final Connection con) {
            // Nothing to do.
        }

        @Override
        public void backWritable(final Context ctx, final Connection con) {
            // Nothing to do.
        }

        @Override
        public void backWritable(final int contextId, final Connection con) {
            // Nothing to do.
        }

        @Override
        public void backWritableAfterReading(Context ctx, Connection con) {
            // Nothing to do.
        }

        @Override
        public void backWritableAfterReading(int contextId, Connection con) {
            // Nothing to do.
        }

        @Override
        public Connection get(final int poolId, final String schema) {
            return null;
        }

        @Override
        public int[] getContextsInSameSchema(final int contextId) {
            return null;
        }

        @Override
        public int[] getContextsInSameSchema(Connection con, int contextId) {
            return null;
        }

        @Override
        public int[] getContextsInSchema(Connection con, int poolId, String schema) {
            return null;
        }

        @Override
        public String[] getUnfilledSchemas(Connection con, int poolId, int maxContexts) {
            return null;
        }

        @Override
        public Map<String, Integer> getContextCountPerSchema(Connection con, int poolId, int maxContexts) throws OXException {
            return null;
        }

        @Override
        public void lock(Connection con, int poolId) {
            // Nothing to do
        }

        @Override
        public Connection getForUpdateTask(final int contextId) {
            return null;
        }

        @Override
        public Connection getReadOnly(final Context ctx) {
            return null;
        }

        @Override
        public Connection getReadOnly(final int contextId) {
            return null;
        }

        @Override
        public String getSchemaName(final int contextId) {
            return null;
        }

        @Override
        public Connection getWritable(final Context ctx) {
            return null;
        }

        @Override
        public Connection getWritable(final int contextId) {
            return null;
        }

        @Override
        public int getWritablePool(final int contextId) {
            return 0;
        }

        @Override
        public void invalidate(final int... contextIds) {
            // Nothing to do.
        }

        @Override
        public void backReadOnly(final Connection con) {
            // Nothing to do.
        }

        @Override
        public void backWritable(final Connection con) {
            // Nothing to do.
        }

        @Override
        public Connection getReadOnly() {
            return null;
        }

        @Override
        public Connection getWritable() {
            return null;
        }

        @Override
        public int[] listContexts(final int poolId) {
            return null;
        }

        @Override
        public Connection getNoTimeout(final int poolId, final String schema) {
            return null;
        }

        @Override
        public void backNoTimeoout(final int poolId, final Connection con) {
            // Nothing to do
        }

        @Override
        public int getServerId() {
            return 0;
        }

        @Override
        public String getServerName() {
            return null;
        }

        @Override
        public void writeAssignment(final Connection con, final Assignment assignment) throws OXException {
            // Nothing to do
        }

        @Override
        public void deleteAssignment(Connection con, int contextId) {
            // Nothing to do
        }

        @Override
        public void backForUpdateTaskAfterReading(int contextId, Connection con) {
            //nothing to do
        }

        @Override
        public Connection getReadOnlyMonitored(int readPoolId, int writePoolId, String schema, int partitionId) throws OXException {
            return null;
        }

        @Override
        public Connection getWritableMonitored(int readPoolId, int writePoolId, String schema, int partitionId) throws OXException {
            return null;
        }

        @Override
        public Connection getWritableMonitoredForUpdateTask(int readPoolId, int writePoolId, String schema, int partitionId) throws OXException {
            return null;
        }

        @Override
        public void backReadOnlyMonitored(int readPoolId, int writePoolId, String schema, int partitionId, Connection con) {
            //nothing to do
        }

        @Override
        public void backWritableMonitored(int readPoolId, int writePoolId, String schema, int partitionId, Connection con) {
            //nothing to do
        }

        @Override
        public void backWritableMonitoredForUpdateTask(int readPoolId, int writePoolId, String schema, int partitionId, Connection con) {
            //nothing to do
        }

        @Override
        public void initMonitoringTables(int writePoolId, String schema) throws OXException {
            //nothing to do
        }

        @Override
        public void initPartitions(int writePoolId, String schema, int... partitions) throws OXException {
            //nothing to do
        }

        @Override
        public Connection getForUpdateTask() throws OXException {
            return null;
        }

        @Override
        public void backForUpdateTask(Connection con) {

        }

        @Override
        public Connection getReadOnlyForGlobal(String group) throws OXException {
            return null;
        }

        @Override
        public Connection getReadOnlyForGlobal(int contextId) throws OXException {
            return null;
        }

        @Override
        public void backReadOnlyForGlobal(String group, Connection connection) {

        }

        @Override
        public void backReadOnlyForGlobal(int contextId, Connection connection) {

        }

        @Override
        public Connection getWritableForGlobal(String group) throws OXException {
            return null;
        }

        @Override
        public Connection getWritableForGlobal(int contextId) throws OXException {
            return null;
        }

        @Override
        public void backWritableForGlobal(String group, Connection connection) {

        }

        @Override
        public void backWritableForGlobal(int contextId, Connection connection) {

        }

        @Override
        public boolean isGlobalDatabaseAvailable(String group) throws OXException {
            return false;
        }

        @Override
        public boolean isGlobalDatabaseAvailable(int contextId) throws OXException {
            return false;
        }
    }

    private static void rmdir(final File tempFile) {
        if (tempFile.isDirectory()) {
            for (final File f : tempFile.listFiles()) {
                rmdir(f);
            }
        }
        tempFile.delete();
    }
}

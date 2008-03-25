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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.consistency;

import junit.framework.TestCase;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.InMemoryAttachmentBase;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.InMemoryInfostoreDatabase;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.file.InMemoryFileStorage;
import com.openexchange.tools.file.FileStorage;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ConsistencyTest extends TestCase {

    private InMemoryInfostoreDatabase database = new InMemoryInfostoreDatabase();
    private InMemoryFileStorage storage = null;
    private InMemoryAttachmentBase attachments = new InMemoryAttachmentBase();

    private ContextImpl ctx = null;
    private ContextImpl ctx2 = null;
    private ContextImpl ctx3 = null;

    private Map<Integer, Context> contexts = new HashMap<Integer, Context>();

    private int id = 20;

    private static final HashSet<String> MISSING = new HashSet<String>() {
        {
            add("00/01/01");
            add("00/01/03");
            add("00/02/02");
        }
    };

    private static final HashSet<String> UNASSIGNED = new HashSet<String>() {
        {
            add("00/04/04");
        }
    };

    protected void setUp() throws Exception {
        super.setUp();
        storage = new InMemoryFileStorage();
        ctx = new ContextImpl(1);
        ctx.setFilestoreId(1);

        ctx2 = new ContextImpl(2);
        ctx2.setFilestoreId(1);

        ctx3 = new ContextImpl(3);
        ctx3.setFilestoreId(2);

        contexts.put(ctx.getContextId(), ctx);
        contexts.put(ctx2.getContextId(), ctx2);
        contexts.put(ctx3.getContextId(), ctx3);


        simulateBrokenContext(ctx);
        simulateBrokenContext(ctx2);
        simulateBrokenContext(ctx3);


    }

    // Tests //

    public void testListMissingFilesInContext() throws AbstractOXException {
        ConsistencyMBean consistency = getConsistencyTool();
        List<String> missing = consistency.listMissingFilesInContext(ctx.getContextId());
        assertNotNull(missing);

        Set<String> expected = new HashSet<String>(MISSING);

        assertEquals(missing.toString(), expected.size() , missing.size());
        expected.removeAll(missing);
        assertTrue(missing.toString(), expected.isEmpty());
    }

    public void testListMissingFilesInFilestore() throws AbstractOXException {
        ConsistencyMBean consistency = getConsistencyTool();
        Map<Integer, List<String>>  missing = consistency.listMissingFilesInFilestore(1);
        assertContexts(missing, MISSING, ctx, ctx2);
    }

    public void testListMissingFilesInDatabase() throws AbstractOXException {
        ConsistencyMBean consistency = getConsistencyTool();
        Map<Integer, List<String>>  missing = consistency.listMissingFilesInDatabase(1);
        assertContexts(missing, MISSING, ctx, ctx3);
    }

    public void testListAllMissingFiles() throws AbstractOXException {
        ConsistencyMBean consistency = getConsistencyTool();
        Map<Integer, List<String>>  missing = consistency.listAllMissingFiles();
        assertContexts(missing, MISSING, ctx, ctx2, ctx3);
    }

    public void testListUnassignedFilesInContext() throws AbstractOXException {
        ConsistencyMBean consistency = getConsistencyTool();
        List<String> unassigned = consistency.listUnassignedFilesInContext(ctx.getContextId());
        assertNotNull(unassigned);

        Set<String> expected = new HashSet<String>(UNASSIGNED);

        assertEquals(unassigned.toString(), expected.size() , unassigned.size());
        expected.removeAll(unassigned);
        assertTrue(unassigned.toString(), expected.isEmpty());

    }

    public void testListUnassignedFilesInFilestore() throws AbstractOXException {
        ConsistencyMBean consistency = getConsistencyTool();
        Map<Integer, List<String>>  unassigned = consistency.listUnassignedFilesInFilestore(1);
        assertContexts(unassigned, UNASSIGNED, ctx, ctx2);
    }

    public void testListUnassignedFilesInDatabase() throws AbstractOXException {
        ConsistencyMBean consistency = getConsistencyTool();
        Map<Integer, List<String>>  unassigned = consistency.listUnassignedFilesInDatabase(1);
        assertContexts(unassigned, UNASSIGNED, ctx, ctx3);
    }

    public void testListAllUnassignedFiles() throws AbstractOXException {
        ConsistencyMBean consistency = getConsistencyTool();
        Map<Integer, List<String>>  unassigned = consistency.listAllUnassignedFiles();
        assertContexts(unassigned, UNASSIGNED, ctx, ctx2, ctx3);
    }

    protected void assertContexts(Map<Integer, List<String>> missing,Set<String> expect, Context...contexts) {
        assertNotNull(missing);
        Set<Integer> contextIds = new HashSet<Integer>(missing.keySet());
        assertEquals(contextIds.toString(), contexts.length, contextIds.size());
        for(Context context : contexts) {
            List<String> ids = missing.get(context.getContextId());
            assertNotNull(ids);

            Set<String> expected = new HashSet<String>(expect);

            assertEquals(ids.toString(), expected.size() , ids.size());
            expected.removeAll(ids);
            assertTrue(ids.toString(), expected.isEmpty());

            contextIds.remove(context.getContextId());
        }

        assertTrue(contextIds.toString(), contextIds.isEmpty());

    }


    private ConsistencyMBean getConsistencyTool() {
        return new TestConsistency(database,attachments, storage, contexts);
    }

    // Simulation //

    protected void simulateBrokenContext(Context ctx){

        simulateBrokenOlderVersionInInfostore(ctx);
        simulateBrokenCurrentVersionInInfostore(ctx);
        simulateWholeInfostoreEntry(ctx);
        simulateBrokenAttachment(ctx);
        simulateWholeAttachment(ctx);
        simulateFileStoreEntryWithoutDatabaseEntry(ctx);

    }

    private void simulateFileStoreEntryWithoutDatabaseEntry(Context ctx) {
        String unassignedEntry = "00/04/04";
        createFilestoreEntry(ctx, unassignedEntry, "unassigned");
    }



    private void simulateWholeAttachment(Context ctx) {
        String attachmentEntry = "00/00/01";
        createAttachment(ctx, attachmentEntry);
        createFilestoreEntry(ctx, attachmentEntry, "wholeAttachment");
    }

    private void simulateBrokenAttachment(Context ctx) {
        String brokenAttachment = "00/01/01";
        createAttachment(ctx, brokenAttachment);
    }

    private void simulateWholeInfostoreEntry(Context ctx) {
        String version1 = "00/00/02";
        String version2 = "00/00/03";
        int id = createInfostoreDocument(ctx);
        createVersion(ctx, id, version1);
        createVersion(ctx, id, version2);

        createFilestoreEntry(ctx, version1, "wholeInfoitemVersion1");
        createFilestoreEntry(ctx, version2, "wholeInfoitemVersion2");
        
    }

    private void simulateBrokenCurrentVersionInInfostore(Context ctx) {
        String version1 = "00/01/02";
        String version2 = "00/01/03";
        int id = createInfostoreDocument(ctx);
        createVersion(ctx, id, version1);
        createVersion(ctx, id, version2);

        createFilestoreEntry(ctx, version1, "brokenCurrentVersionV1");
                
    }

    private void simulateBrokenOlderVersionInInfostore(Context ctx) {
        String version1 = "00/02/02";
        String version2 = "00/02/03";
        int id = createInfostoreDocument(ctx);
        createVersion(ctx, id, version1);
        createVersion(ctx, id, version2);

        createFilestoreEntry(ctx, version2, "brokenOlderVersionV2");
    }

    private void createFilestoreEntry(Context ctx, String filestoreId, String content) {
        try {
            storage.put(ctx,filestoreId, content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            //IGNORE
        }
    }

    private void createAttachment(Context ctx, String filestorePath) {
        AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFileId(filestorePath);
        attachment.setId(id++);
        attachment.setFilename("attachment.bin");
        attachment.setFilesize(23);

        attachments.put(ctx, attachment);

    }

    private int createInfostoreDocument(Context ctx) {
        int istoreId  = id++;

        DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setId(istoreId);
        dm.setVersion(0);

        database.put(ctx,dm);


        return istoreId;
    }

    private void createVersion(Context ctx, int id, String filestoreLocation) {
        DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setId(id);
        dm.setFilestoreLocation(filestoreLocation);
        dm.setVersion(database.getNextVersionNumber(ctx,id));

        database.put(ctx,dm);        
    }

    private static final class TestConsistency extends Consistency{

        private InMemoryInfostoreDatabase database = new InMemoryInfostoreDatabase();
        private InMemoryFileStorage storage = null;
        private InMemoryAttachmentBase attachments = new InMemoryAttachmentBase();

        private Map<Integer, Context> contexts = null;

        private TestConsistency(InMemoryInfostoreDatabase database, InMemoryAttachmentBase attachments, InMemoryFileStorage storage, Map<Integer, Context> contexts) {
            this.database = database;
            this.storage = storage;
            this.attachments = attachments;
            this.contexts = contexts;
        }

        protected Context getContext(int contextId) {
            return contexts.get(contextId);
        }

        protected DatabaseImpl getDatabase() {
            return database;
        }

        protected AttachmentBase getAttachments() {
            return attachments;
        }

        protected FileStorage getFileStorage(Context ctx) {
            storage.setContext(ctx);
            return storage;
        }

        protected List<Context> getContextsForFilestore(int filestoreId) {
            List<Context> retval = new ArrayList<Context>();
            for(Context context : contexts.values()){
                if(context.getFilestoreId() == filestoreId) {
                    retval.add(context);
                }
            }
            return retval;
        }

        protected List<Context> getContextsForDatabase(int datbaseId) {
            return Arrays.asList(contexts.get(1), contexts.get(3));
        }

        protected List<Context> getAllContexts() {
            return new ArrayList<Context>(contexts.values());
        }
    }

}

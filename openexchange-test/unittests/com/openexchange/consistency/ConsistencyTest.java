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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.InMemoryAttachmentBase;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.InMemoryInfostoreDatabase;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;
import com.openexchange.tools.file.InMemoryFileStorage;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ConsistencyTest extends TestCase {

    private final InMemoryInfostoreDatabase database = new InMemoryInfostoreDatabase();
    private InMemoryFileStorage storage = null;
    private final InMemoryAttachmentBase attachments = new InMemoryAttachmentBase();

    private ContextImpl ctx = null;
    private ContextImpl ctx2 = null;
    private ContextImpl ctx3 = null;

    private final Map<Integer, Context> contexts = new HashMap<Integer, Context>();

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

    @Override
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
        final ConsistencyMBean consistency = getConsistencyTool();
        final List<String> missing = consistency.listMissingFilesInContext(ctx.getContextId());
        assertNotNull(missing);

        final Set<String> expected = new HashSet<String>(MISSING);

        assertEquals(missing.toString(), expected.size() , missing.size());
        expected.removeAll(missing);
        assertTrue(missing.toString(), expected.isEmpty());
    }

    public void testListMissingFilesInFilestore() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<Integer, List<String>>  missing = consistency.listMissingFilesInFilestore(1);
        assertContexts(missing, MISSING, ctx, ctx2);
    }

    public void testListMissingFilesInDatabase() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<Integer, List<String>>  missing = consistency.listMissingFilesInDatabase(1);
        assertContexts(missing, MISSING, ctx, ctx3);
    }

    public void testListAllMissingFiles() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<Integer, List<String>>  missing = consistency.listAllMissingFiles();
        assertContexts(missing, MISSING, ctx, ctx2, ctx3);
    }

    public void testListUnassignedFilesInContext() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final List<String> unassigned = consistency.listUnassignedFilesInContext(ctx.getContextId());
        assertNotNull(unassigned);

        final Set<String> expected = new HashSet<String>(UNASSIGNED);

        assertEquals(unassigned.toString(), expected.size() , unassigned.size());
        expected.removeAll(unassigned);
        assertTrue(unassigned.toString(), expected.isEmpty());

    }

    public void testListUnassignedFilesInFilestore() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<Integer, List<String>>  unassigned = consistency.listUnassignedFilesInFilestore(1);
        assertContexts(unassigned, UNASSIGNED, ctx, ctx2);
    }

    public void testListUnassignedFilesInDatabase() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<Integer, List<String>>  unassigned = consistency.listUnassignedFilesInDatabase(1);
        assertContexts(unassigned, UNASSIGNED, ctx, ctx3);
    }

    public void testListAllUnassignedFiles() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<Integer, List<String>>  unassigned = consistency.listAllUnassignedFiles();
        assertContexts(unassigned, UNASSIGNED, ctx, ctx2, ctx3);
    }

    public void testCreateDummyFilesForInfoitems() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        database.forgetChanges(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), "missing_file_for_infoitem : create_dummy");

        final List<DocumentMetadata> changes = database.getChanges(ctx);
        assertEquals(2, changes.size());
        storage.setContext(ctx);
        for(final DocumentMetadata version : changes) {
            assertEquals("\nCaution! The file has changed", version.getDescription());
            assertEquals("text/plain", version.getFileMIMEType());
            try {
                assertNotNull(storage.getFile(version.getFilestoreLocation()));
            } catch (final FileStorageException e) {
                fail(e.toString());
            }
        }
    }

    public void testCreateDummyFilesForAttachments() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        attachments.forgetChanges(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), "missing_file_for_attachment : create_dummy");

        final List<AttachmentMetadata> changes = attachments.getChanges(ctx);
        assertEquals(1, changes.size());

        for(final AttachmentMetadata attachment : changes) {
            assertEquals("\nCaution! The file has changed", attachment.getComment());
            assertEquals("text/plain", attachment.getFileMIMEType());
            try {
                assertNotNull(storage.getFile(attachment.getFileId()));
            } catch (final FileStorageException e) {
                fail(e.toString());
            }
        }

    }

    public void testDeleteStaleInfoitems() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        database.forgetDeletions(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), "missing_file_for_infoitem : delete");

        final List<DocumentMetadata> deletions = database.getDeletions(ctx);
        assertEquals(2, deletions.size());

        final Set<String> missing = new HashSet<String>(MISSING);
        for(final DocumentMetadata document : deletions) {
            assertTrue(missing.remove(document.getFilestoreLocation()));
        }
        assertEquals(1, missing.size());

    }

    public void testDeleteStaleAttachments() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        attachments.forgetDeletions(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), "missing_file_for_attachment : delete");

        final List<AttachmentMetadata> deletions = attachments.getDeletions(ctx);
        assertEquals(1, deletions.size());

        final Set<String> missing = new HashSet<String>(MISSING);
        for(final AttachmentMetadata document : deletions) {
            assertTrue(missing.remove(document.getFileId()));
        }
        assertEquals(2, missing.size());
    }

    public void testCreateInfoitemForUnassignedFile() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        database.forgetCreated(ctx);
        consistency.repairFilesInContext(1, "missing_entry_for_file : create_admin_infoitem");

        final List<DocumentMetadata> created = database.getCreated(ctx);
        assertEquals(UNASSIGNED.size(), created.size());

        final Set<String> unassigned = new HashSet<String>(UNASSIGNED);
        for(final DocumentMetadata document : created) {
            final String location = document.getFilestoreLocation();
            assertTrue(unassigned.remove(location));
            if(location != null) {
                final String description = "This file needs attention";
                final String title = "Restoredfile";
                final String fileName = "Restoredfile";

                assertEquals(description, document.getDescription());
                assertEquals(title, document.getTitle());
                assertEquals(fileName, document.getFileName());
            }
        }
    }

    public void testDeleteUnassignedFile() throws AbstractOXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        storage.forgetDeleted(ctx);
        consistency.repairFilesInContext(1, "missing_entry_for_file : delete");


        final List<String> deleted = storage.getDeleted(ctx);

        assertEquals(UNASSIGNED.size(), deleted.size());

        final Set<String> unassigned = new HashSet<String>(UNASSIGNED);
        unassigned.removeAll(deleted);
        assertTrue(unassigned.isEmpty());
    }

    protected void assertContexts(final Map<Integer, List<String>> missing,final Set<String> expect, final Context...contexts) {
        assertNotNull(missing);
        final Set<Integer> contextIds = new HashSet<Integer>(missing.keySet());
        assertEquals(contextIds.toString(), contexts.length, contextIds.size());
        for(final Context context : contexts) {
            final List<String> ids = missing.get(context.getContextId());
            assertNotNull(ids);

            final Set<String> expected = new HashSet<String>(expect);

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

    protected void simulateBrokenContext(final Context ctx){

        simulateBrokenOlderVersionInInfostore(ctx);
        simulateBrokenCurrentVersionInInfostore(ctx);
        simulateWholeInfostoreEntry(ctx);
        simulateBrokenAttachment(ctx);
        simulateWholeAttachment(ctx);
        simulateFileStoreEntryWithoutDatabaseEntry(ctx);

    }

    private void simulateFileStoreEntryWithoutDatabaseEntry(final Context ctx) {
        final String unassignedEntry = "00/04/04";
        createFilestoreEntry(ctx, unassignedEntry, "unassigned");
    }



    private void simulateWholeAttachment(final Context ctx) {
        final String attachmentEntry = "00/00/01";
        createAttachment(ctx, attachmentEntry);
        createFilestoreEntry(ctx, attachmentEntry, "wholeAttachment");
    }

    private void simulateBrokenAttachment(final Context ctx) {
        final String brokenAttachment = "00/01/01";
        createAttachment(ctx, brokenAttachment);
    }

    private void simulateWholeInfostoreEntry(final Context ctx) {
        final String version1 = "00/00/02";
        final String version2 = "00/00/03";
        final int id = createInfostoreDocument(ctx);
        createVersion(ctx, id, version1);
        createVersion(ctx, id, version2);

        createFilestoreEntry(ctx, version1, "wholeInfoitemVersion1");
        createFilestoreEntry(ctx, version2, "wholeInfoitemVersion2");
        
    }

    private void simulateBrokenCurrentVersionInInfostore(final Context ctx) {
        final String version1 = "00/01/02";
        final String version2 = "00/01/03";
        final int id = createInfostoreDocument(ctx);
        createVersion(ctx, id, version1);
        createVersion(ctx, id, version2);

        createFilestoreEntry(ctx, version1, "brokenCurrentVersionV1");
                
    }

    private void simulateBrokenOlderVersionInInfostore(final Context ctx) {
        final String version1 = "00/02/02";
        final String version2 = "00/02/03";
        final int id = createInfostoreDocument(ctx);
        createVersion(ctx, id, version1);
        createVersion(ctx, id, version2);

        createFilestoreEntry(ctx, version2, "brokenOlderVersionV2");
    }

    private void createFilestoreEntry(final Context ctx, final String filestoreId, final String content) {
        try {
            storage.put(ctx,filestoreId, content.getBytes("UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            //IGNORE
        }
    }

    private void createAttachment(final Context ctx, final String filestorePath) {
        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFileId(filestorePath);
        attachment.setId(id++);
        attachment.setFilename("attachment.bin");
        attachment.setFilesize(23);

        attachments.put(ctx, attachment);

    }

    private int createInfostoreDocument(final Context ctx) {
        final int istoreId  = id++;

        final DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setId(istoreId);
        dm.setVersion(0);

        database.put(ctx,dm);


        return istoreId;
    }

    private void createVersion(final Context ctx, final int id, final String filestoreLocation) {
        final DocumentMetadata dm = new DocumentMetadataImpl();
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

        private TestConsistency(final InMemoryInfostoreDatabase database, final InMemoryAttachmentBase attachments, final InMemoryFileStorage storage, final Map<Integer, Context> contexts) {
            this.database = database;
            this.storage = storage;
            this.attachments = attachments;
            this.contexts = contexts;
        }

        @Override
		protected Context getContext(final int contextId) {
            return contexts.get(contextId);
        }

        @Override
		protected DatabaseImpl getDatabase() {
            return database;
        }

        @Override
		protected AttachmentBase getAttachments() {
            return attachments;
        }

        @Override
		protected FileStorage getFileStorage(final Context ctx) {
            storage.setContext(ctx);
            return storage;
        }

        @Override
		protected List<Context> getContextsForFilestore(final int filestoreId) {
            final List<Context> retval = new ArrayList<Context>();
            for(final Context context : contexts.values()){
                if(context.getFilestoreId() == filestoreId) {
                    retval.add(context);
                }
            }
            return retval;
        }

        @Override
		protected List<Context> getContextsForDatabase(final int datbaseId) {
            return Arrays.asList(contexts.get(1), contexts.get(3));
        }

        @Override
		protected List<Context> getAllContexts() {
            return new ArrayList<Context>(contexts.values());
        }

        @Override
		protected User getAdmin(final Context ctx) throws LdapException {
            return null;
        }
    }

}

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

package com.openexchange.consistency;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.management.MBeanException;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.tools.file.InMemoryFileStorage;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ConsistencyTest extends TestCase {

    private final InMemoryInfostoreDatabase database = new InMemoryInfostoreDatabase();
    private InMemoryFileStorage storage = null;
    private final InMemoryAttachmentBase attachments = new InMemoryAttachmentBase();
    private SimQuotaFileStorageService quotaFileStorageService = null;

    private ContextImpl ctx = null;
    private ContextImpl ctx2 = null;
    private ContextImpl ctx3 = null;

    private final Map<Integer, Context> contexts = new HashMap<Integer, Context>();

    private int id = 20;

    private static final HashSet<String> MISSING = new HashSet<String>() {

        private static final long serialVersionUID = -795039863003179912L;
        {
            add("00/01/01");
            add("00/01/03");
            add("00/02/02");
        }
    };

    private static final HashSet<String> UNASSIGNED = new HashSet<String>() {

        private static final long serialVersionUID = 7050405041645511451L;
        {
            add("00/00/02"); //Broken whole entry
            add("00/00/03"); //Broken whole entry
            add("00/01/02"); //Broken current version in infostore
            add("00/02/03"); //Broken older version in inforstore
            add("00/04/04"); //Filestore entry with no DB entry
        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        storage = new InMemoryFileStorage();
        quotaFileStorageService = new SimQuotaFileStorageService(storage);
        FileStorages.setQuotaFileStorageService(quotaFileStorageService);

        ctx = new ContextImpl(1);
        ctx.setFilestoreId(1);

        ctx2 = new ContextImpl(2);
        ctx2.setFilestoreId(1);

        ctx3 = new ContextImpl(3);
        ctx3.setFilestoreId(2);

        contexts.put(I(ctx.getContextId()), ctx);
        contexts.put(I(ctx2.getContextId()), ctx2);
        contexts.put(I(ctx3.getContextId()), ctx3);

        simulateBrokenContext(ctx);
        simulateBrokenContext(ctx2);
        simulateBrokenContext(ctx3);
    }

    // Tests //

    public void testListMissingFilesInContext() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final List<String> missing = consistency.listMissingFilesInContext(ctx.getContextId());
        assertNotNull(missing);

        final Set<String> expected = new HashSet<String>(MISSING);

        assertEquals(missing.toString(), expected.size(), missing.size());
        expected.removeAll(missing);
        assertTrue(missing.toString(), expected.isEmpty());
    }

    public void testListMissingFilesInFilestore() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<MBeanEntity, List<String>> missing = consistency.listMissingFilesInFilestore(1);
        assertContextEntities(missing, MISSING, ctx, ctx2);
    }

    public void testListMissingFilesInDatabase() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<MBeanEntity, List<String>> missing = consistency.listMissingFilesInDatabase(1);
        assertContextEntities(missing, MISSING, ctx, ctx3);
    }

    public void testListAllMissingFiles() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<MBeanEntity, List<String>> missing = consistency.listAllMissingFiles();
        assertContextEntities(missing, MISSING, ctx, ctx2, ctx3);
    }

    public void testListUnassignedFilesInContext() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final List<String> unassigned = consistency.listUnassignedFilesInContext(ctx.getContextId());
        assertNotNull(unassigned);

        final Set<String> expected = new HashSet<String>(UNASSIGNED);

        assertEquals(unassigned.toString(), expected.size(), unassigned.size());
        expected.removeAll(unassigned);
        assertTrue(unassigned.toString(), expected.isEmpty());

    }

    public void testListUnassignedFilesInFilestore() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<MBeanEntity, List<String>> unassigned = consistency.listUnassignedFilesInFilestore(1);
        assertContextEntities(unassigned, UNASSIGNED, ctx, ctx2);
    }

    public void testListUnassignedFilesInDatabase() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<MBeanEntity, List<String>> unassigned = consistency.listUnassignedFilesInDatabase(1);
        assertContextEntities(unassigned, UNASSIGNED, ctx, ctx3);
    }

    public void testListAllUnassignedFiles() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        final Map<MBeanEntity, List<String>> unassigned = consistency.listAllUnassignedFiles();
        assertContextEntities(unassigned, UNASSIGNED, ctx, ctx2, ctx3);
    }

    public void testCreateDummyFilesForInfoitems() throws MBeanException, OXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        database.forgetChanges(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), "missing_file_for_infoitem : create_dummy");

        final List<DocumentMetadata> changes = database.getChanges(ctx);
        assertEquals(2, changes.size());
        storage.setContext(ctx);
        for (final DocumentMetadata version : changes) {
            assertEquals("\nCaution! The file has changed", version.getDescription());
            assertEquals("text/plain", version.getFileMIMEType());
            //try {
            assertNotNull(storage.getFile(version.getFilestoreLocation()));
            /*
             * } catch (final OXException e) {
             * fail(e.toString());
             */
            //}
        }
    }

    public void testCreateDummyFilesForAttachments() throws MBeanException, OXException {
        final ConsistencyMBean consistency = getConsistencyTool();
        attachments.forgetChanges(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), "missing_file_for_attachment : create_dummy");

        final List<AttachmentMetadata> changes = attachments.getChanges(ctx);
        assertEquals(1, changes.size());

        for (final AttachmentMetadata attachment : changes) {
            assertEquals("\nCaution! The file has changed", attachment.getComment());
            assertEquals("text/plain", attachment.getFileMIMEType());
            //try {
            assertNotNull(storage.getFile(attachment.getFileId()));
            //} catch (final OXException e) {
            //  fail(e.toString());
            //}
        }

    }

    public void testDeleteStaleInfoitems() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        database.forgetDeletions(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), "missing_file_for_infoitem : delete");

        final List<DocumentMetadata> deletions = database.getDeletions(ctx);
        assertEquals(2, deletions.size());

        final Set<String> missing = new HashSet<String>(MISSING);
        for (final DocumentMetadata document : deletions) {
            assertTrue(missing.remove(document.getFilestoreLocation()));
        }
        assertEquals(1, missing.size());

    }

    public void testDeleteStaleAttachments() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        attachments.forgetDeletions(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), "missing_file_for_attachment : delete");

        final List<AttachmentMetadata> deletions = attachments.getDeletions(ctx);
        assertEquals(1, deletions.size());

        final Set<String> missing = new HashSet<String>(MISSING);
        for (final AttachmentMetadata document : deletions) {
            assertTrue(missing.remove(document.getFileId()));
        }
        assertEquals(2, missing.size());
    }

    public void testCreateInfoitemForUnassignedFile() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        database.forgetCreated(ctx);
        consistency.repairFilesInContext(1, "missing_entry_for_file : create_admin_infoitem");

        final List<DocumentMetadata> created = database.getCreated(ctx);
        assertEquals(UNASSIGNED.size(), created.size());

        final Set<String> unassigned = new HashSet<String>(UNASSIGNED);
        for (final DocumentMetadata document : created) {
            final String location = document.getFilestoreLocation();
            assertTrue(unassigned.remove(location));
            if (location != null) {
                final String description = "This file needs attention";
                final String title = "Restoredfile";
                final String fileName = "Restoredfile";

                assertEquals(description, document.getDescription());
                assertEquals(title, document.getTitle());
                assertEquals(fileName, document.getFileName());
            }
        }
    }

    public void testDeleteUnassignedFile() throws MBeanException {
        final ConsistencyMBean consistency = getConsistencyTool();
        storage.forgetDeleted(ctx);
        consistency.repairFilesInContext(1, "missing_entry_for_file : delete");

        final List<String> deleted = storage.getDeleted(ctx);

        assertEquals(UNASSIGNED.size(), deleted.size());

        final Set<String> unassigned = new HashSet<String>(UNASSIGNED);
        unassigned.removeAll(deleted);
        assertTrue(unassigned.isEmpty());
    }

    protected void assertContextEntities(final Map<MBeanEntity, List<String>> missing, final Set<String> expect, final Context... testContexts) {
        assertNotNull(missing);
        final Set<MBeanEntity> entities = new HashSet<MBeanEntity>(missing.keySet());
        assertEquals(entities.toString(), testContexts.length, entities.size());
        for (final Context context : testContexts) {
            MBeanEntity entity = new MBeanEntity(context.getContextId());
            final List<String> ids = missing.get(entity);
            assertNotNull(ids);

            final Set<String> expected = new HashSet<String>(expect);

            assertEquals(ids.toString(), expected.size(), ids.size());
            expected.removeAll(ids);
            assertTrue(ids.toString(), expected.isEmpty());

            entities.remove(entity);
        }

        assertTrue(entities.toString(), entities.isEmpty());

    }

    private ConsistencyMBean getConsistencyTool() {
        return new TestConsistency(database, attachments, storage, contexts);
    }

    // Simulation //

    protected void simulateBrokenContext(final Context context) {
        simulateBrokenOlderVersionInInfostore(context);
        simulateBrokenCurrentVersionInInfostore(context);
        simulateWholeInfostoreEntry(context);
        simulateBrokenAttachment(context);
        simulateWholeAttachment(context);
        simulateFileStoreEntryWithoutDatabaseEntry(context);

    }

    private void simulateFileStoreEntryWithoutDatabaseEntry(final Context context) {
        final String unassignedEntry = "00/04/04";
        createFilestoreEntry(context, unassignedEntry, "unassigned");
    }

    private void simulateWholeAttachment(final Context context) {
        final String attachmentEntry = "00/00/01";
        createAttachment(context, attachmentEntry);
        createFilestoreEntry(context, attachmentEntry, "wholeAttachment");
    }

    private void simulateBrokenAttachment(final Context context) {
        final String brokenAttachment = "00/01/01";
        createAttachment(context, brokenAttachment);
    }

    private void simulateWholeInfostoreEntry(final Context context) {
        final String version1 = "00/00/02";
        final String version2 = "00/00/03";
        final int dmId = createInfostoreDocument(context);
        createVersion(context, dmId, version1);
        createVersion(context, dmId, version2);

        createFilestoreEntry(context, version1, "wholeInfoitemVersion1");
        createFilestoreEntry(context, version2, "wholeInfoitemVersion2");

    }

    private void simulateBrokenCurrentVersionInInfostore(final Context context) {
        final String version1 = "00/01/02";
        final String version2 = "00/01/03";
        final int dmId = createInfostoreDocument(context);
        createVersion(context, dmId, version1);
        createVersion(context, dmId, version2);

        createFilestoreEntry(context, version1, "brokenCurrentVersionV1");

    }

    private void simulateBrokenOlderVersionInInfostore(final Context context) {
        final String version1 = "00/02/02";
        final String version2 = "00/02/03";
        final int dmId = createInfostoreDocument(context);
        createVersion(context, dmId, version1);
        createVersion(context, dmId, version2);

        createFilestoreEntry(context, version2, "brokenOlderVersionV2");
    }

    private void createFilestoreEntry(final Context context, final String filestoreId, final String content) {
        storage.put(context, filestoreId, content.getBytes(com.openexchange.java.Charsets.UTF_8));
    }

    private void createAttachment(final Context context, final String filestorePath) {
        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFileId(filestorePath);
        attachment.setId(id++);
        attachment.setFilename("attachment.bin");
        attachment.setFilesize(23);

        attachments.put(context, attachment);

    }

    private int createInfostoreDocument(final Context context) {
        final int istoreId = id++;

        final DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setId(istoreId);
        dm.setVersion(0);
        dm.setCreatedBy(1);

        database.put(context, dm);

        return istoreId;
    }

    private void createVersion(final Context context, final int dmId, final String filestoreLocation) {
        final DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setId(dmId);
        dm.setFilestoreLocation(filestoreLocation);
        dm.setVersion(database.getNextVersionNumber(context, dmId));
        dm.setCreatedBy(1);

        database.put(context, dm);
    }

    private static final class TestConsistency extends Consistency {

        private InMemoryInfostoreDatabase database = new InMemoryInfostoreDatabase();
        private InMemoryFileStorage storage = null;
        private InMemoryAttachmentBase attachments = new InMemoryAttachmentBase();

        private Map<Integer, Context> contexts = null;

        TestConsistency(final InMemoryInfostoreDatabase database, final InMemoryAttachmentBase attachments, final InMemoryFileStorage storage, final Map<Integer, Context> contexts) {
            this.database = database;
            this.storage = storage;
            this.attachments = attachments;
            this.contexts = contexts;
        }

        @Override
        protected Context getContext(final int contextId) {
            return contexts.get(I(contextId));
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
        protected List<Context> getContextsForFilestore(final int filestoreId) {
            final List<Context> retval = new ArrayList<Context>();
            for (final Context context : contexts.values()) {
                if (context.getFilestoreId() == filestoreId) {
                    retval.add(context);
                }
            }
            return retval;
        }

        @Override
        protected List<Context> getContextsForDatabase(final int datbaseId) {
            return Arrays.asList(contexts.get(I(1)), contexts.get(I(3)));
        }

        @Override
        protected List<Context> getAllContexts() {
            return new ArrayList<Context>(contexts.values());
        }

        @Override
        protected User getAdmin(final Context ctx) {
            UserImpl usr = new UserImpl();
            usr.setId(1);
            return usr;
        }

        @Override
        protected FileStorage getFileStorage(final Context ctx) {
            storage.setContext(ctx);
            FileStorage retval = storage;
            return retval;
        }

        @Override
        protected FileStorage getFileStorage(Context ctx, User usr) throws OXException {
            storage.setContext(ctx);
            FileStorage retval = storage;
            return retval;
        }

        @Override
        protected FileStorage getFileStorage(Entity entity) throws OXException {
            storage.setContext(entity.getContext());
            FileStorage retval = storage;
            return retval;
        }

        @Override
        protected List<Entity> getEntitiesForFilestore(int filestoreId) throws OXException {
            final List<Entity> retval = new ArrayList<Entity>();
            for (final Context context : contexts.values()) {
                if (context.getFilestoreId() == filestoreId) {
                    retval.add(new EntityImpl(context));
                }
            }
            return retval;
        }

        @Override
        protected SortedSet<String> getSnippetFileStoreLocationsPerContext(Context ctx) throws OXException {
            return new TreeSet<String>();
        }

        @Override
        protected SortedSet<String> getVCardFileStoreLocationsPerContext(Context ctx) throws OXException {
            return new TreeSet<String>();
        }

        @Override
        protected SortedSet<String> getPreviewCacheFileStoreLocationsPerContext(Context ctx) throws OXException {
            return new TreeSet<String>();
        }
    }

}

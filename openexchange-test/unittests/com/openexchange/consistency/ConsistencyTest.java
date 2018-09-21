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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.consistency.internal.ConsistencyServiceImpl;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.InMemoryAttachmentBase;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.InMemoryInfostoreDatabase;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.tools.file.InMemoryFileStorage;

/**
 * {@link ConsistencyTest}
 * 
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ConsistencyServiceImpl.class)
public class ConsistencyTest {

    private final InMemoryInfostoreDatabase database = new InMemoryInfostoreDatabase();
    private final InMemoryAttachmentBase attachments = new InMemoryAttachmentBase();
    private InMemoryFileStorage storage = null;
    private InMemoryFileStorage storage2 = null;
    private SimQuotaFileStorageService quotaFileStorageService = null;

    private UserImpl admin = null;

    private Context ctx = null;
    private Context ctx2 = null;
    private Context ctx3 = null;

    private Entity entity = null;
    private Entity entity2 = null;
    private Entity entity3 = null;

    private ConsistencyServiceImpl consistency = null;

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

    @Before
    public void setUp() throws Exception {
        storage = new InMemoryFileStorage();
        storage2 = new InMemoryFileStorage();
        quotaFileStorageService = new SimQuotaFileStorageService(storage);
        FileStorages.setQuotaFileStorageService(quotaFileStorageService);

        admin = new UserImpl();
        admin.setId(1);

        ctx = new ContextImpl(1);
        ((ContextImpl) ctx).setFilestoreId(1);
        storage.setContext(ctx);
        entity = new EntityImpl(ctx);

        ctx2 = new ContextImpl(2);
        ((ContextImpl) ctx2).setFilestoreId(1);
        storage.setContext(ctx2);
        entity = new EntityImpl(ctx2);

        ctx3 = new ContextImpl(3);
        ((ContextImpl) ctx3).setFilestoreId(2);
        storage2.setContext(ctx3);
        entity = new EntityImpl(ctx3);

        contexts.put(I(ctx.getContextId()), ctx);
        contexts.put(I(ctx2.getContextId()), ctx2);
        contexts.put(I(ctx3.getContextId()), ctx3);

        simulateBrokenContext(ctx);
        simulateBrokenContext(ctx2);
        simulateBrokenContext(ctx3);

        consistency = PowerMockito.spy(new ConsistencyServiceImpl());
        PowerMockito.doReturn(ctx).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getContext", int.class)).withArguments(1);
        PowerMockito.doReturn(ctx2).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getContext", int.class)).withArguments(2);
        PowerMockito.doReturn(ctx3).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getContext", int.class)).withArguments(3);

        PowerMockito.doReturn(database).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getDatabase")).withNoArguments();
        PowerMockito.doReturn(attachments).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getAttachments")).withNoArguments();

        PowerMockito.doReturn(Arrays.asList(ctx, ctx2)).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getContextsForFilestore", int.class)).withArguments(1);
        PowerMockito.doReturn(Arrays.asList(ctx3)).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getContextsForFilestore", int.class)).withArguments(2);

        PowerMockito.doReturn(Arrays.asList(ctx, ctx3)).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getContextsForDatabase", int.class)).withArguments(Matchers.anyInt());
        PowerMockito.doReturn(Arrays.asList(ctx, ctx2, ctx3)).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getAllContexts")).withNoArguments();

        PowerMockito.doReturn(admin).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getAdmin", Context.class)).withArguments(Matchers.any(Context.class));

        PowerMockito.doReturn(storage).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getFileStorage", Context.class)).withArguments(ctx);
        PowerMockito.doReturn(storage).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getFileStorage", Context.class)).withArguments(ctx2);
        PowerMockito.doReturn(storage2).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getFileStorage", Context.class)).withArguments(ctx3);

        PowerMockito.doReturn(storage).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getFileStorage", Context.class, User.class)).withArguments(ctx, Matchers.any(User.class));
        PowerMockito.doReturn(storage).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getFileStorage", Context.class, User.class)).withArguments(ctx2, Matchers.any(User.class));
        PowerMockito.doReturn(storage2).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getFileStorage", Context.class, User.class)).withArguments(ctx3, Matchers.any(User.class));

        PowerMockito.doReturn(storage).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getFileStorage", Entity.class)).withArguments(entity);
        PowerMockito.doReturn(storage).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getFileStorage", Entity.class)).withArguments(entity2);
        PowerMockito.doReturn(storage2).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getFileStorage", Entity.class)).withArguments(entity3);

        PowerMockito.doReturn(Arrays.asList(entity, entity2)).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getEntitiesForFilestore", int.class)).withArguments(1);
        PowerMockito.doReturn(Arrays.asList(entity3)).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getEntitiesForFilestore", int.class)).withArguments(2);

        PowerMockito.doReturn(new TreeSet<String>()).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getSnippetFileStoreLocationsPerContext", Context.class)).withArguments(Matchers.any(Context.class));
        PowerMockito.doReturn(new TreeSet<String>()).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getVCardFileStoreLocationsPerContext", Context.class)).withArguments(Matchers.any(Context.class));
        PowerMockito.doReturn(new TreeSet<String>()).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getPreviewCacheFileStoreLocationsPerContext", Context.class)).withArguments(Matchers.any(Context.class));
        PowerMockito.doReturn(new TreeSet<String>()).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getSnippetFileStoreLocationsPerUser", Context.class, User.class)).withArguments(Matchers.any(Context.class), Matchers.any(User.class));
        PowerMockito.doReturn(new TreeSet<String>()).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getPreviewCacheFileStoreLocationsPerUser", Context.class, User.class)).withArguments(Matchers.any(Context.class), Matchers.any(User.class));
        PowerMockito.doReturn(new TreeSet<String>()).when(consistency, PowerMockito.method(ConsistencyServiceImpl.class, "getVCardFileStoreLocationsPerUser", Context.class, User.class)).withArguments(Matchers.any(Context.class), Matchers.any(User.class));
    }

    // Tests //

    @Test
    public void testListMissingFilesInContext() throws OXException {
        List<String> missing = consistency.listMissingFilesInContext(ctx.getContextId());
        assertNotNull(missing);

        Set<String> expected = new HashSet<String>(MISSING);

        assertEquals(missing.toString(), expected.size(), missing.size());
        expected.removeAll(missing);
        assertTrue(missing.toString(), expected.isEmpty());
    }

    @Test
    public void testListMissingFilesInFilestore() throws OXException {
        Map<Entity, List<String>> missing = consistency.listMissingFilesInFilestore(1);
        assertContextEntities(missing, MISSING, ctx, ctx2);
    }

    @Test
    public void testListMissingFilesInDatabase() throws OXException {

        Map<Entity, List<String>> missing = consistency.listMissingFilesInDatabase(1);
        assertContextEntities(missing, MISSING, ctx, ctx3);
    }

    @Test
    public void testListAllMissingFiles() throws OXException {

        Map<Entity, List<String>> missing = consistency.listAllMissingFiles();
        assertContextEntities(missing, MISSING, ctx, ctx2, ctx3);
    }

    @Test
    public void testListUnassignedFilesInContext() throws OXException {

        List<String> unassigned = consistency.listUnassignedFilesInContext(ctx.getContextId());
        assertNotNull(unassigned);

        Set<String> expected = new HashSet<String>(UNASSIGNED);

        assertEquals(unassigned.toString(), expected.size(), unassigned.size());
        expected.removeAll(unassigned);
        assertTrue(unassigned.toString(), expected.isEmpty());

    }

    @Test
    public void testListUnassignedFilesInFilestore() throws OXException {

        Map<Entity, List<String>> unassigned = consistency.listUnassignedFilesInFilestore(1);
        assertContextEntities(unassigned, UNASSIGNED, ctx, ctx2);
    }

    @Test
    public void testListUnassignedFilesInDatabase() throws OXException {

        Map<Entity, List<String>> unassigned = consistency.listUnassignedFilesInDatabase(1);
        assertContextEntities(unassigned, UNASSIGNED, ctx, ctx3);
    }

    @Test
    public void testListAllUnassignedFiles() throws OXException {

        Map<Entity, List<String>> unassigned = consistency.listAllUnassignedFiles();
        assertContextEntities(unassigned, UNASSIGNED, ctx, ctx2, ctx3);
    }

    @Test
    public void testCreateDummyFilesForInfoitems() throws OXException, OXException {

        database.forgetChanges(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), RepairPolicy.MISSING_FILE_FOR_INFOITEM, RepairAction.CREATE_DUMMY);

        List<DocumentMetadata> changes = database.getChanges(ctx);
        assertEquals(2, changes.size());
        storage.setContext(ctx);
        for (DocumentMetadata version : changes) {
            assertEquals("\nCaution! The file has changed", version.getDescription());
            assertEquals("text/plain", version.getFileMIMEType());
            //try {
            assertNotNull(storage.getFile(version.getFilestoreLocation()));
            /*
             * } catch (OXException e) {
             * fail(e.toString());
             */
            //}
        }
    }

    @Test
    public void testCreateDummyFilesForAttachments() throws OXException, OXException {

        attachments.forgetChanges(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), RepairPolicy.MISSING_FILE_FOR_ATTACHMENT, RepairAction.CREATE_DUMMY);

        List<AttachmentMetadata> changes = attachments.getChanges(ctx);
        assertEquals(1, changes.size());

        for (AttachmentMetadata attachment : changes) {
            assertEquals("\nCaution! The file has changed", attachment.getComment());
            assertEquals("text/plain", attachment.getFileMIMEType());
            //try {
            assertNotNull(storage.getFile(attachment.getFileId()));
            //} catch (OXException e) {
            //  fail(e.toString());
            //}
        }

    }

    @Test
    public void testDeleteStaleInfoitems() throws OXException {

        database.forgetDeletions(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), RepairPolicy.MISSING_FILE_FOR_INFOITEM, RepairAction.DELETE);

        List<DocumentMetadata> deletions = database.getDeletions(ctx);
        assertEquals(2, deletions.size());

        Set<String> missing = new HashSet<String>(MISSING);
        for (DocumentMetadata document : deletions) {
            assertTrue(missing.remove(document.getFilestoreLocation()));
        }
        assertEquals(1, missing.size());

    }

    @Test
    public void testDeleteStaleAttachments() throws OXException {

        attachments.forgetDeletions(ctx);
        consistency.repairFilesInContext(ctx.getContextId(), RepairPolicy.MISSING_FILE_FOR_ATTACHMENT, RepairAction.CREATE_DUMMY);

        List<AttachmentMetadata> deletions = attachments.getDeletions(ctx);
        assertEquals(1, deletions.size());

        Set<String> missing = new HashSet<String>(MISSING);
        for (AttachmentMetadata document : deletions) {
            assertTrue(missing.remove(document.getFileId()));
        }
        assertEquals(2, missing.size());
    }

    @Test
    public void testCreateInfoitemForUnassignedFile() throws OXException {

        database.forgetCreated(ctx);
        consistency.repairFilesInContext(1, RepairPolicy.MISSING_ENTRY_FOR_FILE, RepairAction.CREATE_ADMIN_INFOITEM);

        List<DocumentMetadata> created = database.getCreated(ctx);
        assertEquals(UNASSIGNED.size(), created.size());

        Set<String> unassigned = new HashSet<String>(UNASSIGNED);
        for (DocumentMetadata document : created) {
            String location = document.getFilestoreLocation();
            assertTrue(unassigned.remove(location));
            if (location != null) {
                String description = "This file needs attention";
                String title = "Restoredfile";
                String fileName = "Restoredfile";

                assertEquals(description, document.getDescription());
                assertEquals(title, document.getTitle());
                assertEquals(fileName, document.getFileName());
            }
        }
    }

    @Test
    public void testDeleteUnassignedFile() throws OXException {

        storage.forgetDeleted(ctx);
        consistency.repairFilesInContext(1, RepairPolicy.MISSING_ENTRY_FOR_FILE, RepairAction.DELETE);

        List<String> deleted = storage.getDeleted(ctx);

        assertEquals(UNASSIGNED.size(), deleted.size());

        Set<String> unassigned = new HashSet<String>(UNASSIGNED);
        unassigned.removeAll(deleted);
        assertTrue(unassigned.isEmpty());
    }

    protected void assertContextEntities(Map<Entity, List<String>> missing, Set<String> expect, Context... testContexts) {
        assertNotNull(missing);
        Set<Entity> entities = new HashSet<Entity>(missing.keySet());
        assertEquals(entities.toString(), testContexts.length, entities.size());
        for (Context context : testContexts) {
            Entity entity = new EntityImpl(context);
            List<String> ids = missing.get(entity);
            assertNotNull(ids);

            Set<String> expected = new HashSet<String>(expect);

            assertEquals(ids.toString(), expected.size(), ids.size());
            expected.removeAll(ids);
            assertTrue(ids.toString(), expected.isEmpty());

            entities.remove(entity);
        }

        assertTrue(entities.toString(), entities.isEmpty());

    }

    // Simulation //

    protected void simulateBrokenContext(Context context) {
        simulateBrokenOlderVersionInInfostore(context);
        simulateBrokenCurrentVersionInInfostore(context);
        simulateWholeInfostoreEntry(context);
        simulateBrokenAttachment(context);
        simulateWholeAttachment(context);
        simulateFileStoreEntryWithoutDatabaseEntry(context);

    }

    private void simulateFileStoreEntryWithoutDatabaseEntry(Context context) {
        String unassignedEntry = "00/04/04";
        createFilestoreEntry(context, unassignedEntry, "unassigned");
    }

    private void simulateWholeAttachment(Context context) {
        String attachmentEntry = "00/00/01";
        createAttachment(context, attachmentEntry);
        createFilestoreEntry(context, attachmentEntry, "wholeAttachment");
    }

    private void simulateBrokenAttachment(Context context) {
        String brokenAttachment = "00/01/01";
        createAttachment(context, brokenAttachment);
    }

    private void simulateWholeInfostoreEntry(Context context) {
        String version1 = "00/00/02";
        String version2 = "00/00/03";
        int dmId = createInfostoreDocument(context);
        createVersion(context, dmId, version1);
        createVersion(context, dmId, version2);

        createFilestoreEntry(context, version1, "wholeInfoitemVersion1");
        createFilestoreEntry(context, version2, "wholeInfoitemVersion2");

    }

    private void simulateBrokenCurrentVersionInInfostore(Context context) {
        String version1 = "00/01/02";
        String version2 = "00/01/03";
        int dmId = createInfostoreDocument(context);
        createVersion(context, dmId, version1);
        createVersion(context, dmId, version2);

        createFilestoreEntry(context, version1, "brokenCurrentVersionV1");

    }

    private void simulateBrokenOlderVersionInInfostore(Context context) {
        String version1 = "00/02/02";
        String version2 = "00/02/03";
        int dmId = createInfostoreDocument(context);
        createVersion(context, dmId, version1);
        createVersion(context, dmId, version2);

        createFilestoreEntry(context, version2, "brokenOlderVersionV2");
    }

    private void createFilestoreEntry(Context context, String filestoreId, String content) {
        storage.put(context, filestoreId, content.getBytes(com.openexchange.java.Charsets.UTF_8));
    }

    private void createAttachment(Context context, String filestorePath) {
        AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFileId(filestorePath);
        attachment.setId(id++);
        attachment.setFilename("attachment.bin");
        attachment.setFilesize(23);

        attachments.put(context, attachment);

    }

    private int createInfostoreDocument(Context context) {
        int istoreId = id++;

        DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setId(istoreId);
        dm.setVersion(0);
        dm.setCreatedBy(1);

        database.put(context, dm);

        return istoreId;
    }

    private void createVersion(Context context, int dmId, String filestoreLocation) {
        DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setId(dmId);
        dm.setFilestoreLocation(filestoreLocation);
        dm.setVersion(database.getNextVersionNumber(context, dmId));
        dm.setCreatedBy(1);

        database.put(context, dm);
    }
}

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

package com.openexchange.groupware.infostore;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.util.Collections;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.ServerSession;

public class InfostoreFacadeTest extends AbstractInfostoreTest {

    // Bug 7012
    public void testExists() throws OXException{
        final DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setFolderId(folderId);
        dm.setTitle("Exists Test");

        infostore.saveDocumentMetadata(dm, System.currentTimeMillis(), session);
        clean.add(dm);
        assertTrue("Should Exist", infostore.exists(dm.getId(), InfostoreFacade.CURRENT_VERSION, session));

        IDTuple idTuple = new IDTuple(Long.toString(dm.getFolderId()), Integer.toString(dm.getId()));
        infostore.removeDocument(Collections.singletonList(idTuple), System.currentTimeMillis(), session);
        clean.remove(dm);
        assertFalse("Should not exist", infostore.exists(dm.getId(), InfostoreFacade.CURRENT_VERSION, session));

    }

    // Bug 7012
    public void testNotExistsIfNoReadPermission() throws OXException {
        final DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setFolderId(folderId);
        dm.setTitle("Exists Test");

        infostore.saveDocumentMetadata(dm, System.currentTimeMillis(), session);
        clean.add(dm);

        assertFalse("No read permission so should not exist", infostore.exists(dm.getId(), InfostoreFacade.CURRENT_VERSION, session2));
    }

//     Bug 9555
    public void testMoveChecksDeletePermission() throws Exception {
        final int folderId = createFolderWithoutDeletePermissionForSecondUser();
        final DocumentMetadata document = createEntry(folderId);
        failMovingEntryAsOtherUser(document);
    }


    private int createFolderWithoutDeletePermissionForSecondUser() throws OXException {
        final FolderObject folder = new FolderObject();
        folder.setFolderName("bug9555");
        folder.setParentFolderID(folderId);
        folder.setType(FolderObject.PUBLIC);
        folder.setModule(FolderObject.INFOSTORE);

        final OCLPermission perm = new OCLPermission();
        perm.setEntity(user.getId());
        perm.setFolderAdmin(true);
        perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setReadObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setWriteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setDeleteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setGroupPermission(false);

        // All others may read and write, but not delete

        final OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
        perm2.setGroupPermission(true);
        perm2.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
        perm2.setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
        perm2.setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
        perm2.setDeleteObjectPermission(OCLPermission.NO_PERMISSIONS);

        folder.setPermissionsAsArray(new OCLPermission[]{perm, perm2});

        Connection writeCon = null;
        try {
            writeCon = provider.getWriteConnection(ctx);
        } finally {
            if (writeCon != null) {
                provider.releaseWriteConnection(ctx, writeCon);
                writeCon = null;
            }
        }
        final OXFolderManager oxma = OXFolderManager.getInstance(session, writeCon, writeCon);
        oxma.createFolder(folder, true, System.currentTimeMillis());
        cleanFolders.add(folder);
        return folder.getObjectID();
    }

    // Bug 11521

    public void testShouldRemoveMIMETypeWhenRemovingLastVersionOfAFile() throws OXException {
        DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setFolderId(folderId);
        dm.setTitle("Exists Test");

        try {
            infostore.startTransaction();
            infostore.saveDocumentMetadata(dm, System.currentTimeMillis(), session);
            dm.setFileMIMEType("text/plain");
            dm.setFileName("bla.txt");
            dm.setFileSize(12);
            infostore.saveDocument(dm, new ByteArrayInputStream("Hallo".getBytes(com.openexchange.java.Charsets.UTF_8)), Long.MAX_VALUE, session);
            infostore.removeVersion(dm.getId(), new int[]{1}, session);
            infostore.commit();
        } catch(final OXException x) {
            x.printStackTrace();
            infostore.rollback();
            throw x;
        } finally {
            infostore.finish();

        }

        dm = infostore.getDocumentMetadata(dm.getId(), InfostoreFacade.CURRENT_VERSION, session);

        assertEquals("", dm.getFileMIMEType());
        assertNull(dm.getFileName());
        assertEquals(0, dm.getFileSize());

        clean.add(dm);

    }

    public void testTouch() throws Exception{
        final DocumentMetadata document = createEntry(folderId);
        assertNotNull(document.getLastModified());
        assertTrue(document.getLastModified().getTime() != 0);
        Thread.sleep(100);
        infostore.touch(document.getId(), session);
        final DocumentMetadata reload = load(document.getId(), session);

        assertTrue("lastModified did not change", reload.getLastModified().getTime() > document.getLastModified().getTime());

    }

    private DocumentMetadata load(final int id, final ServerSession session) throws OXException {
        return infostore.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, session);
    }

    private DocumentMetadata createEntry(final int fid) throws OXException {
        final DocumentMetadata dm = new DocumentMetadataImpl();
        dm.setFolderId(fid);
        dm.setTitle("Exists Test");

        infostore.saveDocumentMetadata(dm, System.currentTimeMillis(), session);
        clean.add(dm);

        return dm;
    }

    private void failMovingEntryAsOtherUser(final DocumentMetadata document) {
        long originalFolderID = document.getFolderId();
        document.setFolderId(folderId2);
        try {
            infostore.saveDocumentMetadata(document, Long.MAX_VALUE, session2);
            fail("Shouldn't be able to move without delete permissions");
        } catch (final OXException x) {
            x.printStackTrace();
            assertTrue(true);
        } finally {
            document.setFolderId(originalFolderID);
        }
    }


}

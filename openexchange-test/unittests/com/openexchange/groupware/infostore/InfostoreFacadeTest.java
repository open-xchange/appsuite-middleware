package com.openexchange.groupware.infostore;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestInit;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionFactory;

public class InfostoreFacadeTest extends AbstractInfostoreTest {
	
	// Bug 7012
	public void testExists() throws OXException{
		final DocumentMetadata dm = new DocumentMetadataImpl();
		dm.setFolderId(folderId);
		dm.setTitle("Exists Test");
		
		infostore.saveDocumentMetadata(dm, System.currentTimeMillis(), session);
		clean.add(dm);
		assertTrue("Should Exist", infostore.exists(dm.getId(), InfostoreFacade.CURRENT_VERSION, ctx, user, userConfig));
		
		infostore.removeDocument(new int[]{dm.getId()}, System.currentTimeMillis(), session);
		clean.remove(dm);
		assertFalse("Should not exist", infostore.exists(dm.getId(), InfostoreFacade.CURRENT_VERSION, ctx, user, userConfig));
		
	}
	
	// Bug 7012
	public void testNotExistsIfNoReadPermission() throws OXException {
		final DocumentMetadata dm = new DocumentMetadataImpl();
		dm.setFolderId(folderId);
		dm.setTitle("Exists Test");
		
		infostore.saveDocumentMetadata(dm, System.currentTimeMillis(), session);
		clean.add(dm);
		
		assertFalse("No read permission so should not exist", infostore.exists(dm.getId(), InfostoreFacade.CURRENT_VERSION, ctx, user2, userConfig2));
	}
	
//	 Bug 9555
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
            infostore.saveDocument(dm, new ByteArrayInputStream("Hallo".getBytes("UTF-8")), Long.MAX_VALUE, session);
            infostore.removeVersion(dm.getId(), new int[]{1}, session);
            infostore.commit();
        } catch(final OXException x) {
            x.printStackTrace();
            infostore.rollback();
            throw x;
        } catch (final UnsupportedEncodingException e) {
            // Subba!
        } finally {
            infostore.finish();

        }

        dm = infostore.getDocumentMetadata(dm.getId(), InfostoreFacade.CURRENT_VERSION, ctx, user, userConfig);

        assertEquals("", dm.getFileMIMEType());
        assertNull(dm.getFileName());
        assertEquals(0, dm.getFileSize());

        clean.add(dm);

    }
    
    public void testTouch() throws Exception{
        DocumentMetadata document = createEntry(folderId);
        assertNotNull(document.getLastModified());
        assertTrue(document.getLastModified().getTime() != 0);
        Thread.sleep(100);
        infostore.touch(document.getId(), session);
        DocumentMetadata reload = load(document.getId(), session);

        assertTrue("lastModified did not change", reload.getLastModified().getTime() > document.getLastModified().getTime());
    
    }

    private DocumentMetadata load(int id, ServerSession session) throws OXException {
        return infostore.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, session.getContext(), session.getUser(), session.getUserConfiguration());
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
		document.setFolderId(folderId2);
		try {
			infostore.saveDocumentMetadata(document, Long.MAX_VALUE, session2);
			fail("Shouldn't be able to move without delete permissions");
		} catch (final OXException x) {
			x.printStackTrace();
			assertTrue(true);
		}
	}

	
}

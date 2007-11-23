package com.openexchange.groupware.infostore;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;

public class InfostoreFacadeTest extends TestCase {
	
	private InfostoreFacade infostore;

	private Context ctx = null;
	private User user = null;
	private User user2 = null;
	
	private UserConfiguration userConfig = null;
	private UserConfiguration userConfig2 = null;
	
	private int folderId;
	private int folderId2;

	private SessionObject session;
	private SessionObject session2;

	private List<DocumentMetadata> clean;
	private List<FolderObject> cleanFolders = null;
	
	private DBProvider provider = null;
	
	@Override
	public void setUp() throws Exception {
		clean = new ArrayList<DocumentMetadata>();
		cleanFolders = new ArrayList<FolderObject>();
		
		Init.loadTestProperties();
		Init.startServer();
		ContextStorage.init();
		
		final ContextStorage ctxstor = ContextStorage.getInstance();
        final int contextId = ctxstor.getContextId("defaultcontext");
        ctx = ctxstor.getContext(contextId);
		user = UserStorage.getInstance(ctx).getUser(UserStorage.getInstance(ctx).getUserId("thorben")); //FIXME
		user2 = UserStorage.getInstance(ctx).getUser(UserStorage.getInstance(ctx).getUserId("francisco")); //FIXME
		
		
		session = SessionObjectWrapper.createSessionObject(user.getId(), ctx, "blupp");
		session2 = SessionObjectWrapper.createSessionObject(user2.getId(), ctx, "blupp2");
		
		userConfig = session.getUserConfiguration();
		userConfig2 = session2.getUserConfiguration();
		
		folderId = _getPrivateInfostoreFolder(ctx,user,session);
		folderId2 = _getPrivateInfostoreFolder(ctx, user2, session2);
		
		provider = new DBPoolProvider();
		infostore = new InfostoreFacadeImpl(provider);
		
	}
	
	public int _getPrivateInfostoreFolder(final Context context, final User usr, final SessionObject sess) throws OXException {
		final OXFolderAccess oxfa = new OXFolderAccess(context);
		return oxfa.getDefaultFolder(usr.getId(), FolderObject.INFOSTORE).getObjectID();
	}

	@Override
	public void tearDown() throws Exception{
		for(DocumentMetadata dm : clean) {
			infostore.removeDocument(new int[]{dm.getId()}, System.currentTimeMillis(), session);
		}
		
		final OXFolderManager oxma = new OXFolderManagerImpl(session);
		for(FolderObject folder : cleanFolders) {
			oxma.deleteFolder(folder, false, System.currentTimeMillis());
		}
		
		Init.stopServer();
	}
	
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
		int folderId = createFolderWithoutDeletePermissionForSecondUser();
		DocumentMetadata document = createEntry(folderId);
		failMovingEntryAsOtherUser(document);
	}


	private int createFolderWithoutDeletePermissionForSecondUser() throws OXException {
		FolderObject folder = new FolderObject();
		folder.setFolderName("bug9555");
		folder.setParentFolderID(folderId);
		folder.setType(FolderObject.PUBLIC);
		folder.setModule(FolderObject.INFOSTORE);
		
		OCLPermission perm = new OCLPermission();
		perm.setEntity(user.getId());
		perm.setFolderAdmin(true);
		perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setReadObjectPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setWriteObjectPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setDeleteObjectPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setGroupPermission(false);
		
		// All others may read and write, but not delete
		
		OCLPermission perm2 = new OCLPermission();
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
			if (writeCon != null)
				provider.releaseWriteConnection(ctx, writeCon);
		}
		final OXFolderManager oxma = new OXFolderManagerImpl(session, writeCon, writeCon);
		oxma.createFolder(folder, true, System.currentTimeMillis());
		cleanFolders.add(folder);
		return folder.getObjectID();
	}
	
	private DocumentMetadata createEntry(int fid) throws OXException {
		final DocumentMetadata dm = new DocumentMetadataImpl();
		dm.setFolderId(fid);
		dm.setTitle("Exists Test");
		
		infostore.saveDocumentMetadata(dm, System.currentTimeMillis(), session);
		clean.add(dm);
		
		return dm;
	}

	private void failMovingEntryAsOtherUser(DocumentMetadata document) {
		document.setFolderId(folderId2);
		try {
			infostore.saveDocumentMetadata(document, Long.MAX_VALUE, session2);
			fail("Shouldn't be able to move without delete permissions");
		} catch (OXException x) {
			x.printStackTrace();
			assertTrue(true);
		}
	}

	
}

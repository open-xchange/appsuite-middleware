package com.openexchange.groupware.infostore;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.oxfolder.OXFolderAccess;

public class InfostoreFacadeTest extends TestCase {
	
	private InfostoreFacade infostore;

	private Context ctx = null;
	private User user = null;
	private User user2 = null;
	
	private UserConfiguration userConfig = null;
	private UserConfiguration userConfig2 = null;
	
	private int folderId;

	private SessionObject session;
	private SessionObject session2;

	private List<DocumentMetadata> clean;

	
	@Override
	public void setUp() throws Exception {
		clean = new ArrayList<DocumentMetadata>();
		Init.loadTestProperties();
		Init.loadSystemProperties();
		Init.initDB();
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
		
		infostore = new InfostoreFacadeImpl(new DBPoolProvider());
		
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
		Init.stopDB();
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
	
}

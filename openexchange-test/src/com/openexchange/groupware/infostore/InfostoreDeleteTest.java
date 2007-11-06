package com.openexchange.groupware.infostore;

import java.sql.Connection;

import junit.framework.TestCase;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.oxfolder.OXFolderAccess;

public class InfostoreDeleteTest extends TestCase {
	
	SessionObject session = null;
	DBProvider provider = new DBPoolProvider();
	InfostoreFacade database;
	int myFolder = 0;
	
	public void setUp() throws Exception {
		Init.startServer();
		Context ctx = ContextStorage.getInstance().getContext(1);
		session = SessionObjectWrapper.createSessionObject(UserStorage.getInstance(ctx).getUserId("francisco"), ctx, "Blubb");
		database = new InfostoreFacadeImpl(provider);
		database.setTransactional(true);
		
		final OXFolderAccess oxfa = new OXFolderAccess(ctx);
		myFolder = oxfa.getDefaultFolder(session.getUserObject().getId(), FolderObject.INFOSTORE).getObjectID();
	}
	
	public void tearDown() throws Exception {
		Init.stopServer();
	}
	
	public void testDeleteUser() throws Exception {
		DocumentMetadata metadata = createMetadata();
		DeleteEvent delEvent = new DeleteEvent(this, session.getUserObject().getId(), DeleteEvent.TYPE_USER,session.getContext());
		
		Connection con = null;
		try {
			con = provider.getWriteConnection(session.getContext());
			new InfostoreDelete().deletePerformed(delEvent, con, con);
		} finally {
			if(con != null)
				provider.releaseWriteConnection(session.getContext(), con);
		}
		assertFalse(database.exists(metadata.getId(), InfostoreFacade.CURRENT_VERSION, session.getContext(), session.getUserObject(), session.getUserConfiguration()));
	
	}

	private DocumentMetadataImpl createMetadata() throws Exception {
		DocumentMetadataImpl metadata = new DocumentMetadataImpl();
		metadata.setTitle("Nice Infoitem");
		metadata.setFolderId(myFolder); // FIXME
		database.startTransaction();
		try {
			database.saveDocumentMetadata(metadata, Long.MAX_VALUE, session);
			database.commit();
			return metadata;
		} catch (Exception x) {
			database.rollback();
			throw x;
		} finally {
			database.finish();
		}
	}
}

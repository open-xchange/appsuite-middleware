package com.openexchange.groupware.infostore;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import junit.framework.TestCase;

import java.sql.Connection;

public class InfostoreDeleteTest extends TestCase {
	
	SessionObject session = null;
	DBProvider provider = new DBPoolProvider();
	InfostoreFacade database;
	int myFolder = 0;
    private Context ctx;

    public void setUp() throws Exception {
		Init.startServer();
		ctx = ContextStorage.getInstance().getContext(1);
		session = SessionObjectWrapper.createSessionObject(UserStorage.getInstance().getUserId("francisco", ctx), ctx, "Blubb");
		database = new InfostoreFacadeImpl(provider);
		database.setTransactional(true);
		
		final OXFolderAccess oxfa = new OXFolderAccess(ctx);
		myFolder = oxfa.getDefaultFolder(session.getUserId(), FolderObject.INFOSTORE).getObjectID();
	}
	
	public void tearDown() throws Exception {
		Init.stopServer();
	}
	
	public void testDeleteUser() throws Exception {
		DocumentMetadata metadata = createMetadata();
		DeleteEvent delEvent = new DeleteEvent(this, session.getUserId(), DeleteEvent.TYPE_USER,ContextStorage.getInstance().getContext(session.getContextId()));
		
		Connection con = null;
		try {
			con = provider.getWriteConnection(ContextStorage.getInstance().getContext(session.getContextId()));
			new InfostoreDelete().deletePerformed(delEvent, con, con);
		} finally {
			if(con != null)
				provider.releaseWriteConnection(ContextStorage.getInstance().getContext(session.getContextId()), con);
		}
        UserStorage userStorage = UserStorage.getInstance();
        UserConfigurationStorage userConfigStorage = UserConfigurationStorage.getInstance();
        
        assertFalse(database.exists(metadata.getId(), InfostoreFacade.CURRENT_VERSION, ContextStorage.getInstance().getContext(session.getContextId()), userStorage.getUser(session.getUserId(), ctx), userConfigStorage.getUserConfiguration(session.getUserId(),ctx)));
	
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

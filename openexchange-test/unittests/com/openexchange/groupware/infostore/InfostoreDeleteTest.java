package com.openexchange.groupware.infostore;

import java.sql.Connection;
import junit.framework.TestCase;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionFactory;

public class InfostoreDeleteTest extends TestCase {

	ServerSession session = null;
	DBProvider provider = new DBPoolProvider();
	InfostoreFacade database;
	int myFolder = 0;
    private Context ctx;

    @Override
	public void setUp() throws Exception {
		Init.startServer();

		final TestConfig config = new TestConfig();
        final String userName = config.getUser();
        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);
        final int user = tools.resolveUser(userName, ctx);

		session = ServerSessionFactory.createServerSession(user, ctx, "Blubb");
		database = new InfostoreFacadeImpl(provider);
		database.setTransactional(true);

		final OXFolderAccess oxfa = new OXFolderAccess(ctx);
		myFolder = oxfa.getDefaultFolder(session.getUserId(), FolderObject.INFOSTORE).getObjectID();
	}

	@Override
	public void tearDown() throws Exception {
		Init.stopServer();
	}

	public void testDeleteUser() throws Exception {
		final DocumentMetadata metadata = createMetadata();
		final DeleteEvent delEvent = new DeleteEvent(this, session.getUserId(), DeleteEvent.TYPE_USER,ContextStorage.getInstance().getContext(session.getContextId()));

		Connection con = null;
		try {
			con = provider.getWriteConnection(ContextStorage.getInstance().getContext(session.getContextId()));
			new InfostoreDelete().deletePerformed(delEvent, con, con);
		} finally {
			if(con != null) {
				provider.releaseWriteConnection(ContextStorage.getInstance().getContext(session.getContextId()), con);
			}
		}
        assertFalse(database.exists(metadata.getId(), InfostoreFacade.CURRENT_VERSION, session));
	}

	private DocumentMetadataImpl createMetadata() throws Exception {
		final DocumentMetadataImpl metadata = new DocumentMetadataImpl();
		metadata.setTitle("Nice Infoitem");
		metadata.setFolderId(myFolder); // FIXME
		database.startTransaction();
		try {
			database.saveDocumentMetadata(metadata, Long.MAX_VALUE, session);
			database.commit();
			return metadata;
		} catch (final Exception x) {
			database.rollback();
			throw x;
		} finally {
			database.finish();
		}
	}
}

package com.openexchange.webdav.protocol;


import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.FolderLockManagerImpl;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.paths.impl.PathResolverImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.tx.AlwaysWriteConnectionProvider;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.webdav.protocol.impl.DummyResourceManager;

public class TestWebdavFactoryBuilder {

	public static final int DUMMY = 0;
	public static final int INFO = 1;
	
	private static final int mode = INFO;
	
	public static WebdavFactory buildFactory() throws Exception {
		switch(mode) {
		case DUMMY : return buildDummyFactory();
		case INFO : return buildInfoFactory();
		} 
		return null;
	}

	private static WebdavFactory buildDummyFactory() {
		return DummyResourceManager.getInstance();
	}

	private static WebdavFactory buildInfoFactory() throws Exception{
		
		InfostoreWebdavFactory factory = new InfostoreWebdavFactory();
		factory.setDatabase(new InfostoreFacadeImpl());
		factory.setFolderLockManager(new FolderLockManagerImpl());
		factory.setFolderProperties(new PropertyStoreImpl("oxfolder_property"));
		factory.setInfoLockManager(new EntityLockManagerImpl("infostore_lock"));
		factory.setLockNullLockManager(new EntityLockManagerImpl("lock_null_lock"));
		factory.setInfoProperties(new PropertyStoreImpl("infostore_property"));
		factory.setProvider(new DBPoolProvider());
		factory.setResolver(new PathResolverImpl(factory.getDatabase()));
		final ContextStorage ctxstor = ContextStorage.getInstance();
        final int contextId = ctxstor.getContextId("defaultcontext");
        final Context ctx = ctxstor.getContext(contextId);
		factory.setSessionHolder(new DummySessionHolder("thorben", ctx));
		return factory;
	}
	
	public static void setUp() throws Exception {
		if(mode == INFO) {
			Init.loadTestProperties();
			Init.startServer();
			ContextStorage.init();
		}
	}
	
	public static void tearDown() throws Exception {
		if(mode == INFO)
			Init.stopServer(); 
	}

}

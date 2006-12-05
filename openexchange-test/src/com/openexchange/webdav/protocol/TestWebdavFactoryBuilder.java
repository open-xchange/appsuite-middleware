package com.openexchange.webdav.protocol;

import java.sql.SQLException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.FolderLockManagerImpl;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.paths.impl.PathResolverImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionHolder;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.webdav.protocol.impl.DummyResourceManager;

public class TestWebdavFactoryBuilder {

	public static final int DUMMY = 0;
	public static final int INFO = 1;
	
	private static final int mode = INFO;
	
	public static WebdavFactory buildFactory() {
		switch(mode) {
		case DUMMY : return buildDummyFactory();
		case INFO : return buildInfoFactory();
		} 
		return null;
	}

	private static WebdavFactory buildDummyFactory() {
		return DummyResourceManager.getInstance();
	}

	private static WebdavFactory buildInfoFactory() {
		
		InfostoreWebdavFactory factory = new InfostoreWebdavFactory();
		factory.setDatabase(new InfostoreFacadeImpl());
		factory.setFolderLockManager(new FolderLockManagerImpl());
		factory.setFolderProperties(new PropertyStoreImpl("oxfolder_property"));
		factory.setInfoLockManager(new EntityLockManagerImpl("infostore_lock"));
		factory.setLockNullLockManager(new EntityLockManagerImpl("lock_null_lock"));
		factory.setInfoProperties(new PropertyStoreImpl("infostore_property"));
		factory.setProvider(new DBPoolProvider());
		factory.setResolver(new PathResolverImpl(factory.getDatabase()));
		try {
			factory.setSessionHolder(new DummySessionHolder("thorben", 1,5));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return factory;
	}
	
	private static final class DummySessionHolder implements SessionHolder{

		private SessionObject session = null;
		
		public DummySessionHolder(String username, int context, int filestoreId) throws LdapException, SQLException, DBPoolingException, OXException {
			ContextImpl ctx = new ContextImpl(context);
			ctx.setFilestoreId(filestoreId);
			ctx.setFileStorageQuota(Long.MAX_VALUE);
			session =  SessionObjectWrapper.createSessionObject(UserStorage.getInstance(ctx).getUserId(username)  , ctx,"12345");
		}
		
		public SessionObject getSessionObject() {
			return session;
		}
		
	}

	public static void setUp() throws Exception {
		if(mode == INFO) {
			Init.loadTestProperties();
			Init.loadSystemProperties();
			Init.initDB();
		}
	}
	
	public static void tearDown() throws Exception {
		if(mode == INFO)
			Init.stopDB(); 
	}

}

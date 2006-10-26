package com.openexchange.webdav.protocol;

import java.sql.SQLException;

import com.openexchange.groupware.FolderLockManagerImpl;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.paths.impl.PathResolverImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.sessiond.SessionHolder;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.webdav.protocol.impl.DummyResourceManager;

public class TestWebdavFactoryBuilder {

	public static final int DUMMY = 0;
	public static final int INFO = 1;
	
	private static final int mode = DUMMY;
	
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
		
		DBProvider provider = new DBPoolProvider();
		
		InfostoreWebdavFactory factory = new InfostoreWebdavFactory();
		factory.setDatabase(new DatabaseImpl(provider));
		factory.setFolderLockManager(new FolderLockManagerImpl(provider));
		factory.setFolderProperties(new PropertyStoreImpl(provider, "oxfolder_property"));
		factory.setInfoLockManager(new EntityLockManagerImpl(provider, "infostore_lock"));
		factory.setInfoProperties(new PropertyStoreImpl(provider,"infostore_property"));
		factory.setProvider(provider);
		factory.setResolver(new PathResolverImpl(provider, factory.getDatabase()));
		try {
			factory.setSessionHolder(new DummySessionHolder("thorben", 1));
		} catch (LdapException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return factory;
	}
	
	private static final class DummySessionHolder implements SessionHolder{

		private SessionObject session = null;
		
		public DummySessionHolder(String username, int context) throws LdapException, SQLException {
			Context ctx = new ContextImpl(context);
			session =  SessionObjectWrapper.createSessionObject(UserStorage.getInstance(ctx).getUserId(username)  , ctx,"12345");
		}
		
		public SessionObject getSessionObject() {
			return session;
		}
		
	}

}

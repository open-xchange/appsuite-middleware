package com.openexchange.webdav.protocol;


import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.tools.CalendarContextToolkit;
import com.openexchange.groupware.calendar.tools.CalendarTestConfig;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.FolderLockManagerImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.paths.impl.PathResolverImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.test.AjaxInit;
import com.openexchange.test.TestInit;
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
		final InfostoreWebdavFactory factory = new InfostoreWebdavFactory();
        final InfostoreFacadeImpl database = new InfostoreFacadeImpl();
        factory.setDatabase(database);
        factory.setSecurity(database.getSecurity());
        factory.setFolderLockManager(new FolderLockManagerImpl());
		factory.setFolderProperties(new PropertyStoreImpl("oxfolder_property"));
		factory.setInfoLockManager(new EntityLockManagerImpl("infostore_lock"));
		factory.setLockNullLockManager(new EntityLockManagerImpl("lock_null_lock"));
		factory.setInfoProperties(new PropertyStoreImpl("infostore_property"));
		factory.setProvider(new DBPoolProvider());
		factory.setResolver(new PathResolverImpl(factory.getDatabase()));
		
		final CalendarTestConfig config = new CalendarTestConfig();
        final CalendarContextToolkit tools = new CalendarContextToolkit();
        final String ctxName = config.getContextName();
        final Context ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);

		factory.setSessionHolder(new DummySessionHolder(getUsername(), ctx));
		return factory;
	}

	private static String getUsername() {
	    final String un = AjaxInit.getAJAXProperty("login");
	    final int pos = un.indexOf('@');
	    return pos == -1 ? un : un.substring(0, pos);
	}
	
	public static void setUp() throws Exception {
		if(mode == INFO) {
            TestInit.loadTestProperties();
			Init.startServer();
		}
	}
	
	public static void tearDown() throws Exception {
		if(mode == INFO) {
			Init.stopServer();
		} 
	}

}

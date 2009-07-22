package com.openexchange.groupware.folder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.oxfolder.OXFolderLogicException;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderPermissionException;

public class FolderTestCase extends TestCase {
	
	protected Context ctx = new ContextImpl(1);
	protected User user = null;
	protected UserConfiguration userConfig;
	protected SessionObject session;
	
	protected List<FolderObject> clean = new ArrayList<FolderObject>();
	
	
	
	@Override
	public void setUp() throws Exception {
		Init.startServer();

        final UserStorage userStorage = UserStorage.getInstance();
        final UserConfigurationStorage userConfigStorage = UserConfigurationStorage.getInstance();
        
        final TestConfig config = new TestConfig();
        final TestContextToolkit tools = new TestContextToolkit();
        
        final String ctxName = config.getContextName();
        ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);

        session = SessionObjectWrapper.createSessionObject(userStorage.getUserId(getUsername(), ctx), ctx, getClass().getName());
		user = userStorage.getUser(session.getUserId(), ctx);
		userConfig = userConfigStorage.getUserConfiguration(session.getUserId(),ctx);
	}
	
	private String getUsername() {
		final String userName = AjaxInit.getAJAXProperty("login");
		final int pos = userName.indexOf('@');
        return pos == -1 ? userName : userName.substring(0, pos);
	}

	@Override
	public void tearDown() throws Exception {
		for(final FolderObject folderobject : clean) {
			rm(folderobject.getObjectID());
		}
		Init.stopServer();
	}
	
	protected FolderObject mkdir(final int parent, final String name) throws SQLException, OXFolderPermissionException, Exception {
		Connection writecon = null;
        try {
        	writecon = DBPool.pickupWriteable(ctx);
	        //OXFolderAction ofa = new OXFolderAction(session);
	        final OXFolderManager oxma = OXFolderManager.getInstance(session, writecon, writecon);
	        final OCLPermission oclp = new OCLPermission();
	        oclp.setEntity(user.getId());
	        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
	        oclp.setFolderAdmin(true);
	        FolderObject fo = new FolderObject();
	        fo.setFolderName(name);
	        fo.setParentFolderID(parent);
	        fo.setModule(FolderObject.INFOSTORE);
	        fo.setType(FolderObject.PUBLIC);
	        fo.setPermissionsAsArray(new OCLPermission[] { oclp });
	        //ofa.createFolder(fo, session, true, writecon, writecon, false);
	        fo = oxma.createFolder(fo, true, System.currentTimeMillis());
	        return fo;
        } finally {
        	if(writecon != null) {
				DBPool.pushWrite(ctx, writecon);
			}
        }
    }
	
	protected void rm(final int objectID) throws SQLException, OXFolderPermissionException, OXFolderLogicException, Exception {
		//OXFolderAction ofa = new OXFolderAction(session);
		final OXFolderManager oxma = OXFolderManager.getInstance(session);
		//ofa.deleteFolder(objectID, session, true, System.currentTimeMillis());
		oxma.deleteFolder(new FolderObject(objectID), true, System.currentTimeMillis());
	}
}

package com.openexchange.groupware.folder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.DBPool;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.oxfolder.OXFolderLogicException;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.tools.oxfolder.OXFolderPermissionException;

public class FolderTestCase extends TestCase {
	
	protected Context ctx = new ContextImpl(1);
	protected User user = null;
	protected UserConfiguration userConfig;
	protected SessionObject session;
	
	protected List<FolderObject> clean = new ArrayList<FolderObject>();
	
	
	
	public void setUp() throws Exception {
		Init.startServer();
		
		session = SessionObjectWrapper.createSessionObject(UserStorage.getInstance(ctx).getUserId(getUsername()), ctx, getClass().getName());
		user = session.getUserObject();
		userConfig = session.getUserConfiguration();
	}
	
	private String getUsername() {
		return Init.getAJAXProperty("login");
	}

	public void tearDown() throws Exception {
		for(FolderObject folderobject : clean) {
			rm(folderobject.getObjectID());
		}
		Init.stopServer();
	}
	
	protected FolderObject mkdir(int parent, String name) throws SQLException, OXFolderPermissionException, Exception {
		Connection writecon = null;
        try {
        	writecon = DBPool.pickupWriteable(ctx);
	        //OXFolderAction ofa = new OXFolderAction(session);
	        final OXFolderManager oxma = new OXFolderManagerImpl(session, writecon, writecon);
	        OCLPermission oclp = new OCLPermission();
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
        	if(writecon != null)
        		DBPool.pushWrite(ctx, writecon);
        }
    }
	
	protected void rm(int objectID) throws SQLException, OXFolderPermissionException, OXFolderLogicException, Exception {
		//OXFolderAction ofa = new OXFolderAction(session);
		final OXFolderManager oxma = new OXFolderManagerImpl(session);
		//ofa.deleteFolder(objectID, session, true, System.currentTimeMillis());
		oxma.deleteFolder(new FolderObject(objectID), true, System.currentTimeMillis());
	}
}

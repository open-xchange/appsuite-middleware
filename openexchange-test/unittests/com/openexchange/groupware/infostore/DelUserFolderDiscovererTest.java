package com.openexchange.groupware.infostore;

import java.util.List;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.userconfiguration.RdbUserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.utils.DelUserFolderDiscoverer;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.tools.oxfolder.OXFolderTools;
import com.openexchange.test.AjaxInit;

import junit.framework.TestCase;

public class DelUserFolderDiscovererTest extends TestCase{
	
	private DelUserFolderDiscoverer discoverer = null;
	private Context ctx;
	private int userIdA;
	private int userIdB;
	
	private User userA;
	private User userB;
	
	private UserConfiguration userConfigA;
	private UserConfiguration userConfigB;
	
	private SessionObject session;
	
	private FolderObject privateInfostoreFolder;
	private FolderObject folderWithOtherEntity;
	
	public void setUp() throws Exception {
		Init.startServer();
		ctx = ContextStorage.getInstance().getContext(1); 
		
		String userNameA = AjaxInit.getAJAXProperty("login");
		String userNameB = AjaxInit.getAJAXProperty("seconduser");
		
		userIdA = UserStorage.getInstance().getUserId(userNameA, ctx);
		userIdB = UserStorage.getInstance().getUserId(userNameB, ctx);
		
		userA = UserStorage.getInstance().getUser(userIdA, ctx);
		userB = UserStorage.getInstance().getUser(userIdB, ctx);
		
		userConfigA = RdbUserConfigurationStorage.loadUserConfiguration(userIdA, ctx);
		userConfigB = RdbUserConfigurationStorage.loadUserConfiguration(userIdB, ctx);
		
		
		session = SessionObjectWrapper.createSessionObject(userIdA, ctx, "blupp");
		
		final SearchIterator iter = OXFolderTools
        .getAllVisibleFoldersIteratorOfModule(userIdA,
            userA.getGroups(), userConfigA.getAccessibleModules(),
            FolderObject.INFOSTORE, ctx);
		
		try {
			while(privateInfostoreFolder == null && iter.hasNext()) {
				FolderObject f = (FolderObject) iter.next();
				List<OCLPermission> perms = f.getPermissions();
				if(perms.size() == 1) {
					if(perms.get(0).getFolderPermission() >= OCLPermission.ADMIN_PERMISSION && perms.get(0).getEntity() == userIdA) {
						privateInfostoreFolder = f;
					}
				}
			}
		} finally {
			iter.close();
		}
		
		assertTrue("Can't find suitable infostore folder",null != privateInfostoreFolder);
		
		folderWithOtherEntity = new FolderObject();
		addDefaults(folderWithOtherEntity);
		folderWithOtherEntity.setFolderName("Folder with other entity");
		OCLPermission perm1 = buildReadAll(userIdA,true);
		OCLPermission perm2 = buildReadOwn(userIdB,false);
		folderWithOtherEntity.setPermissionsAsArray(new OCLPermission[]{perm1, perm2});
		
		// Create all folders
		//OXFolderAction oxfa = new OXFolderAction(session);
		OXFolderManager manager = new OXFolderManagerImpl(session);
		manager.createFolder(folderWithOtherEntity,true, System.currentTimeMillis());
		discoverer = new DelUserFolderDiscoverer(new DBPoolProvider());
	}

	public void tearDown() throws Exception{
//		Remove all folders
		OXFolderManager manager = new OXFolderManagerImpl(session);
		manager.deleteFolder(folderWithOtherEntity, true, System.currentTimeMillis());
		Init.stopServer();
	}
	
	public void testDiscoverFolders() throws Exception{
		List<FolderObject> folders = discoverer.discoverFolders(userIdA, ctx);
		assertEquals(1,folders.size());
		assertEquals(privateInfostoreFolder.getObjectID(), folders.get(0).getObjectID());
		
	}
	
	private void addDefaults(FolderObject folder) {
		folder.setType(FolderObject.PUBLIC);
		folder.setModule(FolderObject.INFOSTORE);
		folder.setParentFolderID(this.privateInfostoreFolder.getObjectID());
	}
	
	private OCLPermission buildReadAll(int userId,boolean admin) {
		OCLPermission perm = new OCLPermission();
		perm.setFolderAdmin(admin);
		if(admin)
			perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
		else
			perm.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
		
		perm.setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
		perm.setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
		perm.setDeleteObjectPermission(OCLPermission.DELETE_ALL_OBJECTS);
		perm.setGroupPermission(false);
		perm.setEntity(userId);
		return perm;
	}
	
	private OCLPermission buildReadOwn(int userId, boolean admin) {
		OCLPermission perm = new OCLPermission();
		
		perm.setFolderAdmin(admin);
		if(admin)
			perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
		else
			perm.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
		
		perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setReadObjectPermission(OCLPermission.READ_OWN_OBJECTS);
		perm.setWriteObjectPermission(OCLPermission.WRITE_OWN_OBJECTS);
		perm.setDeleteObjectPermission(OCLPermission.DELETE_OWN_OBJECTS);
		perm.setGroupPermission(false);
		perm.setEntity(userId);
		return perm;
	}
	
}

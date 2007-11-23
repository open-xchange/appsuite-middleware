package com.openexchange.groupware.infostore.webdav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.openexchange.webdav.protocol.TestWebdavFactoryBuilder;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;

import junit.framework.TestCase;

public class FolderCollectionPermissionHandlingTest extends TestCase {
	
	private static int count = 0;
	
	private Context ctx;
	private int userIdA;
	private int userIdB;
	
	private User userA;
	private User userB;
	
	private UserConfiguration userConfigA;
	private UserConfiguration userConfigB;
	
	private SessionObject session;
	private SessionObject sessionB;
	
	
	private FolderObject privateInfostoreFolder;
	private List<FolderObject> folders = new ArrayList<FolderObject>();
	private List<FolderObject> foldersB = new ArrayList<FolderObject>();
	
	private OXFolderManager manager = null;
	private OXFolderManager managerB = null;
	
	private WebdavFactory factory;
	
	
	public void setUp() throws Exception {
		Init.startServer();
		ctx = ContextStorage.getInstance().getContext(1); 
		
		String userNameA = Init.getAJAXProperty("login");
		String userNameB = Init.getAJAXProperty("seconduser");
		
		userIdA = UserStorage.getInstance(ctx).getUserId(userNameA);
		userIdB = UserStorage.getInstance(ctx).getUserId(userNameB);
		
		userA = UserStorage.getInstance(ctx).getUser(userIdA);
		userB = UserStorage.getInstance(ctx).getUser(userIdB);
		
		userConfigA = RdbUserConfigurationStorage.loadUserConfiguration(userIdA, ctx);
		userConfigB = RdbUserConfigurationStorage.loadUserConfiguration(userIdB, ctx);
		
		
		session = SessionObjectWrapper.createSessionObject(userIdA, ctx, "blupp");
		sessionB = SessionObjectWrapper.createSessionObject(userIdB, ctx, "blupp");
		
		
		manager = new OXFolderManagerImpl(session);
		managerB = new OXFolderManagerImpl(sessionB);
		
		folders.clear();
		foldersB.clear();
		
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
		
		TestWebdavFactoryBuilder.setUp();
		factory = TestWebdavFactoryBuilder.buildFactory();
		factory.beginRequest();
		
		
		assertTrue("Can't find suitable infostore folder",null != privateInfostoreFolder);
	}
	
	public void tearDown() throws Exception {
		
		Collections.reverse(folders);
		for(FolderObject fo : folders) {
			manager.deleteFolder(fo, true, System.currentTimeMillis());
		}
		
		Collections.reverse(foldersB);
		for(FolderObject fo : foldersB) {
			managerB.deleteFolder(fo, true, System.currentTimeMillis());
		}
		
		factory.endRequest(200);
		TestWebdavFactoryBuilder.tearDown();
		Init.stopServer();
	}
	
	public void testCreate() throws Exception {
		FolderObject folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin = new FolderObject();
		addDefaults(folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin);
		folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin.setFolderName("User B May Read All and User A may read all and is Admin");
		OCLPermission perm1 = buildReadAll(userIdA, true);
		OCLPermission perm2 = buildReadAll(userIdB, false);
		folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin.setPermissionsAsArray(new OCLPermission[]{perm1,perm2});
		
		manager.createFolder(folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin, true, System.currentTimeMillis());
		folders.add(folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin);
		
		String url = url(privateInfostoreFolder, folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin) + "/subfolder";
		
		factory.resolveCollection(url).create();
		FolderCollection coll = (FolderCollection) factory.resolveCollection(url);
		
		FolderObject newFolder = FolderObject.loadFolderObjectFromDB(coll.getId(), ctx);
		folders.add(newFolder);
		
		assertPermissions(newFolder.getPermissions(), perm1, perm2);
	}
	
	public void testAddAdminOnCreate() throws Exception {
		FolderObject folderWithUserBMayReadAllAndAdminAndUserAMayReadAll = new FolderObject();
		addDefaults(folderWithUserBMayReadAllAndAdminAndUserAMayReadAll);
		folderWithUserBMayReadAllAndAdminAndUserAMayReadAll.setFolderName("User B May Read All and is Admin and User A may read all");
		OCLPermission perm1 = buildReadAll(userIdA, false);
		perm1.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
		OCLPermission perm2 = buildReadAll(userIdB, true);
		folderWithUserBMayReadAllAndAdminAndUserAMayReadAll.setPermissionsAsArray(new OCLPermission[]{perm1,perm2});
		
		manager.createFolder(folderWithUserBMayReadAllAndAdminAndUserAMayReadAll, true, System.currentTimeMillis());
		foldersB.add(folderWithUserBMayReadAllAndAdminAndUserAMayReadAll);
		
		String url = url(privateInfostoreFolder, folderWithUserBMayReadAllAndAdminAndUserAMayReadAll)+"/subfolder";
		
		factory.resolveCollection(url).create();
		FolderCollection coll = (FolderCollection) factory.resolveCollection(url);
		
		FolderObject newFolder = FolderObject.loadFolderObjectFromDB(coll.getId(), ctx);
		folders.add(newFolder);
		
		assertPermissions(newFolder.getPermissions(), buildReadAll(userIdA, true), perm2);
	}
		
	public void testCopy() throws Exception {
		FolderObject copyMe = buildFolderToCopy();
		OCLPermission[] perms = buildFolderToCopyPermissions();
		
		manager.createFolder(copyMe, true, System.currentTimeMillis());
		folders.add(copyMe);
		
		String url = url(privateInfostoreFolder, copyMe);
		String copyUrl = "/"+privateInfostoreFolder.getFolderName()+"/copy";
		
		factory.resolveCollection(url).copy(copyUrl);
		
		FolderCollection theCopy = (FolderCollection) factory.resolveCollection(copyUrl);
		FolderObject theCopiedFolder = FolderObject.loadFolderObjectFromDB(theCopy.getId(), ctx);
		folders.add(theCopiedFolder);
		
		assertPermissions(theCopiedFolder.getPermissions(), perms);
	}

	public void testMove() throws Exception{
		FolderObject moveMe = buildFolderToCopy();
		OCLPermission[] perms = buildFolderToCopyPermissions();
		
		manager.createFolder(moveMe, true, System.currentTimeMillis());
		folders.add(moveMe);
		
		String url = url(privateInfostoreFolder, moveMe);
		String moveUrl = "/"+privateInfostoreFolder.getFolderName()+"/moved";
		
		factory.resolveCollection(url).move(moveUrl);
		
		FolderCollection theDestination = (FolderCollection) factory.resolveCollection(moveUrl);
		FolderObject theDestinationFolder = FolderObject.loadFolderObjectFromDB(theDestination.getId(), ctx);
		folders.remove(moveMe);
		folders.add(theDestinationFolder);
		
		assertPermissions(theDestinationFolder.getPermissions(), perms);
	}
	
	public void testCopyWithOverwrite() throws Exception {
		FolderObject copyMe = buildFolderToCopy();
		OCLPermission[] perms = buildFolderToCopyPermissions();
		
		manager.createFolder(copyMe, true, System.currentTimeMillis());
		folders.add(copyMe);
		
		FolderObject overwriteMe = buildFolderToOverwrite();
		manager.createFolder(overwriteMe, true, System.currentTimeMillis());	
		folders.add(overwriteMe);
		
		String url = url(privateInfostoreFolder, copyMe);
		String copyUrl = url(privateInfostoreFolder, overwriteMe);
		
		factory.resolveCollection(url).copy(copyUrl,false,true);
		
		FolderCollection theCopy = (FolderCollection) factory.resolveCollection(copyUrl);
		FolderObject theCopiedFolder = FolderObject.loadFolderObjectFromDB(theCopy.getId(), ctx);
		
		assertPermissions(theCopiedFolder.getPermissions(), perms);
	}
	
	public void testMoveWithOverwrite() throws Exception {
		FolderObject moveMe = buildFolderToCopy();
		OCLPermission[] perms = buildFolderToCopyPermissions();
		
		manager.createFolder(moveMe, true, System.currentTimeMillis());
		folders.add(moveMe);
		
		FolderObject overwriteMe = buildFolderToOverwrite();
		manager.createFolder(overwriteMe, true, System.currentTimeMillis());		
		folders.add(overwriteMe);
		
		String url = url(privateInfostoreFolder, moveMe);
		String moveUrl = url(privateInfostoreFolder, overwriteMe);
		
		factory.resolveCollection(url).move(moveUrl,false,true);
		
		FolderCollection theDestination = (FolderCollection) factory.resolveCollection(moveUrl);
		folders.remove(moveMe);
		FolderObject theDestinationFolder = FolderObject.loadFolderObjectFromDB(theDestination.getId(), ctx);
		assertPermissions(theDestinationFolder.getPermissions(), perms);
	}
	
	
	public void testCopyWithoutOverwrite() throws Exception {
		FolderObject copyMe = buildFolderToCopy();
		
		manager.createFolder(copyMe, true, System.currentTimeMillis());
		folders.add(copyMe);
		
		FolderObject overwriteMe = buildFolderToOverwrite();
		manager.createFolder(overwriteMe, true, System.currentTimeMillis());	
		folders.add(overwriteMe);
		
		String url = url(privateInfostoreFolder, copyMe);
		String copyUrl = url(privateInfostoreFolder, overwriteMe);
		
		factory.resolveCollection(url).copy(copyUrl,false,false);
		
		FolderCollection theCopy = (FolderCollection) factory.resolveCollection(copyUrl);
		FolderObject theCopiedFolder = FolderObject.loadFolderObjectFromDB(theCopy.getId(), ctx);
		
		assertPermissions(theCopiedFolder.getPermissions(), buildFolderToOverwritePermissions());
		
	}
	
	public void testMoveWithoutOverwrite() throws Exception {
		FolderObject moveMe = buildFolderToCopy();
		
		manager.createFolder(moveMe, true, System.currentTimeMillis());
		folders.add(moveMe);
		
		FolderObject overwriteMe = buildFolderToOverwrite();
		manager.createFolder(overwriteMe, true, System.currentTimeMillis());		
		folders.add(overwriteMe);
		
		String url = url(privateInfostoreFolder, moveMe);
		String moveUrl = url(privateInfostoreFolder, overwriteMe);
		
		factory.resolveCollection(url).move(moveUrl,false,false);
		
		FolderCollection theDestination = (FolderCollection) factory.resolveCollection(moveUrl);
		folders.remove(moveMe);
		FolderObject theDestinationFolder = FolderObject.loadFolderObjectFromDB(theDestination.getId(), ctx);
		
		assertPermissions(theDestinationFolder.getPermissions(), buildFolderToOverwritePermissions());
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

	private String url(FolderObject... folders) {
		StringBuilder b = new StringBuilder();
		for(FolderObject fo : folders) {
			b.append(fo.getFolderName()).append("/");
		}
		b.setLength(b.length()-1);
		return b.toString();
	}
	
	
	private OCLPermission[] buildFolderToCopyPermissions() {
		OCLPermission perm1 = buildReadAll(userIdA, true);
		OCLPermission perm2 = buildReadOwn(userIdB,false);
		return new OCLPermission[]{perm1, perm2};
	}

	private FolderObject buildFolderToCopy() {
		FolderObject folderToCopy = new FolderObject();
		addDefaults(folderToCopy);
		folderToCopy.setFolderName("CopyMe"+count++);
		folderToCopy.setPermissionsAsArray(buildFolderToCopyPermissions());
		return folderToCopy;
	}
	
	private OCLPermission[] buildFolderToOverwritePermissions() {
		OCLPermission perm1 = buildReadAll(userIdA, true);
		OCLPermission perm2 = buildReadAll(userIdB,false);
		return new OCLPermission[]{perm1, perm2};
	}

	private FolderObject buildFolderToOverwrite() {
		FolderObject folderToOverwrite = new FolderObject();
		addDefaults(folderToOverwrite);
		folderToOverwrite.setFolderName("OverwriteMe");
		folderToOverwrite.setPermissionsAsArray(buildFolderToOverwritePermissions());
		return folderToOverwrite;
	}

	
	public static void assertPermissions(Collection<OCLPermission> perms, OCLPermission...expected){
		Set<OCLPermission> expectSet = new HashSet<OCLPermission>(Arrays.asList(expected));
		
		for(OCLPermission perm : perms) {
			OCLPermission matches = null;
			for(OCLPermission permExpect : expectSet){
				if(
						permExpect.getReadPermission() == perm.getReadPermission() &&
						permExpect.getWritePermission() == perm.getWritePermission() &&
						permExpect.getDeletePermission() == perm.getDeletePermission() &&
						permExpect.getFolderPermission() == perm.getFolderPermission() &&
						permExpect.getEntity() == perm.getEntity()
				){
					matches = permExpect;
				}
			}
			assertNotNull("Permission not expected on folder "+stringify(perm),matches);
			assertTrue(expectSet.remove(matches));
		}
		assertTrue(expectSet.isEmpty());
	}

	private static String stringify(OCLPermission perm) {
		StringBuilder b = new StringBuilder();
		b.append("Read: ").append(perm.getReadPermission()).append(" Write: ")
		.append(perm.getWritePermission()).append(" Delete: ").append(perm.getDeletePermission())
		.append(" Folder: ").append(perm.getFolderPermission()).append(" Entity: ").append(perm.getEntity());
		return b.toString();
	}

}

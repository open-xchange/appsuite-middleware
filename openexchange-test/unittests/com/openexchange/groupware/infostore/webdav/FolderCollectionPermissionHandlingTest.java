package com.openexchange.groupware.infostore.webdav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.CapabilityUserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderTools;
import com.openexchange.webdav.protocol.TestWebdavFactoryBuilder;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;

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
	private final List<FolderObject> folders = new ArrayList<FolderObject>();
	private final List<FolderObject> foldersB = new ArrayList<FolderObject>();

	private OXFolderManager manager = null;
	private OXFolderManager managerB = null;

	private WebdavFactory factory;

	private static String getUsername(final String un) {
        final int pos = un.indexOf('@');
        return pos == -1 ? un : un.substring(0, pos);
    }

	@Override
	public void setUp() throws Exception {
		Init.startServer();
		final TestConfig config = new TestConfig();
        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);

		final String userNameA = AjaxInit.getAJAXProperty("login");
		final String userNameB = AjaxInit.getAJAXProperty("seconduser");

		userIdA = UserStorage.getInstance().getUserId(getUsername(userNameA), ctx);
		userIdB = UserStorage.getInstance().getUserId(getUsername(userNameB), ctx);

		userA = UserStorage.getInstance().getUser(userIdA, ctx);
		userB = UserStorage.getInstance().getUser(userIdB, ctx);

		userConfigA = CapabilityUserConfigurationStorage.loadUserConfiguration(userIdA, ctx);
		userConfigB = CapabilityUserConfigurationStorage.loadUserConfiguration(userIdB, ctx);


		session = SessionObjectWrapper.createSessionObject(userIdA, ctx, "blupp");
		sessionB = SessionObjectWrapper.createSessionObject(userIdB, ctx, "blupp");


		manager = OXFolderManager.getInstance(session);
		managerB = OXFolderManager.getInstance(sessionB);

		folders.clear();
		foldersB.clear();

		final SearchIterator iter = OXFolderTools
        .getAllVisibleFoldersIteratorOfModule(userIdA,
            userA.getGroups(), userConfigA.getAccessibleModules(),
            FolderObject.INFOSTORE, ctx);

		try {
			while(privateInfostoreFolder == null && iter.hasNext()) {
				final FolderObject f = (FolderObject) iter.next();
                final List<OCLPermission> perms = f.getPermissions();
                for(final OCLPermission perm : perms) {
                    if(perm.getFolderPermission() >= OCLPermission.ADMIN_PERMISSION && perm.getEntity() == userIdA) {
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

	@Override
	public void tearDown() throws Exception {

		Collections.reverse(folders);
		for(final FolderObject fo : folders) {
			manager.deleteFolder(fo, true, System.currentTimeMillis());
		}

		Collections.reverse(foldersB);
		for(final FolderObject fo : foldersB) {
			managerB.deleteFolder(fo, true, System.currentTimeMillis());
		}

		factory.endRequest(200);
		TestWebdavFactoryBuilder.tearDown();
		Init.stopServer();
	}

	public void testCreate() throws Exception {
		final FolderObject folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin = new FolderObject();
		addDefaults(folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin);
		folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin.setFolderName("User B May Read All and User A may read all and is Admin");
		final OCLPermission perm1 = buildReadAll(userIdA, true);
		final OCLPermission perm2 = buildReadAll(userIdB, false);
		folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin.setPermissionsAsArray(new OCLPermission[]{perm1,perm2});

		manager.createFolder(folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin, true, System.currentTimeMillis());
		folders.add(folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin);

		final String url = url(privateInfostoreFolder, folderWithUserBMayReadAllAndUserAMayReadAllAndAdmin) + "/subfolder";

		factory.resolveCollection(url).create();
		final FolderCollection coll = (FolderCollection) factory.resolveCollection(url);

		final FolderObject newFolder = FolderObject.loadFolderObjectFromDB(coll.getId(), ctx);
		folders.add(newFolder);

		assertPermissions(newFolder.getPermissions(), perm1, perm2);
	}

	public void testAddAdminOnCreate() throws Exception {
		final FolderObject folderWithUserBMayReadAllAndAdminAndUserAMayReadAll = new FolderObject();
		addDefaults(folderWithUserBMayReadAllAndAdminAndUserAMayReadAll);
		folderWithUserBMayReadAllAndAdminAndUserAMayReadAll.setFolderName("User B May Read All and is Admin and User A may read all "+System.currentTimeMillis());
		final OCLPermission perm1 = buildReadAll(userIdA, false);
		perm1.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
		final OCLPermission perm2 = buildReadAll(userIdB, true);
		folderWithUserBMayReadAllAndAdminAndUserAMayReadAll.setPermissionsAsArray(new OCLPermission[]{perm1,perm2});

		manager.createFolder(folderWithUserBMayReadAllAndAdminAndUserAMayReadAll, true, System.currentTimeMillis());
		foldersB.add(folderWithUserBMayReadAllAndAdminAndUserAMayReadAll);

		final String url = url(privateInfostoreFolder, folderWithUserBMayReadAllAndAdminAndUserAMayReadAll)+"/subfolder";

		factory.resolveCollection(url).create();
		final FolderCollection coll = (FolderCollection) factory.resolveCollection(url);

		final FolderObject newFolder = FolderObject.loadFolderObjectFromDB(coll.getId(), ctx);
		folders.add(newFolder);

		assertPermissions(newFolder.getPermissions(), buildReadAll(userIdA, true), perm2);
	}

	public void testAddAdminOnCreateAndDontInheritFromSystemFolders() throws Exception {

	    final FolderObject publicInfostore = FolderObject.loadFolderObjectFromDB(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, ctx);

        final String url = "/"+publicInfostore.getFolderName()+"/subfolder";

        factory.resolveCollection(url).create();
        final FolderCollection coll = (FolderCollection) factory.resolveCollection(url);

        final FolderObject newFolder = FolderObject.loadFolderObjectFromDB(coll.getId(), ctx);
        folders.add(newFolder);

        assertPermissions(newFolder.getPermissions(), buildReadAll(userIdA, true));
    }

	public void testCopy() throws Exception {
		final FolderObject copyMe = buildFolderToCopy();
		final OCLPermission[] perms = buildFolderToCopyPermissions();

		manager.createFolder(copyMe, true, System.currentTimeMillis());
		folders.add(copyMe);

		final WebdavPath url = url(privateInfostoreFolder, copyMe);
		final WebdavPath copyUrl = new WebdavPath("userstore", privateInfostoreFolder.getFolderName()).append("copy");

		factory.resolveCollection(url).copy(copyUrl);

		final FolderCollection theCopy = (FolderCollection) factory.resolveCollection(copyUrl);
		final FolderObject theCopiedFolder = FolderObject.loadFolderObjectFromDB(theCopy.getId(), ctx);
		folders.add(theCopiedFolder);

		assertPermissions(theCopiedFolder.getPermissions(), perms);
	}

	public void testMove() throws Exception{
		final FolderObject moveMe = buildFolderToCopy();
		final OCLPermission[] perms = buildFolderToCopyPermissions();

		manager.createFolder(moveMe, true, System.currentTimeMillis());
		folders.add(moveMe);

		final WebdavPath url = url(privateInfostoreFolder, moveMe);
		final WebdavPath moveUrl = new WebdavPath("userstore", privateInfostoreFolder.getFolderName()).append("moved");

		factory.resolveCollection(url).move(moveUrl);

		final FolderCollection theDestination = (FolderCollection) factory.resolveCollection(moveUrl);
		final FolderObject theDestinationFolder = FolderObject.loadFolderObjectFromDB(theDestination.getId(), ctx);
		folders.remove(moveMe);
		folders.add(theDestinationFolder);

		assertPermissions(theDestinationFolder.getPermissions(), perms);
	}

	public void testCopyWithOverwrite() throws Exception {
		final FolderObject copyMe = buildFolderToCopy();
		final OCLPermission[] perms = buildFolderToCopyPermissions();

		manager.createFolder(copyMe, true, System.currentTimeMillis());
		folders.add(copyMe);

		final FolderObject overwriteMe = buildFolderToOverwrite();
		manager.createFolder(overwriteMe, true, System.currentTimeMillis());
		folders.add(overwriteMe);

		final WebdavPath url = url(privateInfostoreFolder, copyMe);
		final WebdavPath copyUrl = url(privateInfostoreFolder, overwriteMe);

		factory.resolveCollection(url).copy(copyUrl,false,true);

		final FolderCollection theCopy = (FolderCollection) factory.resolveCollection(copyUrl);
		final FolderObject theCopiedFolder = FolderObject.loadFolderObjectFromDB(theCopy.getId(), ctx);

		assertPermissions(theCopiedFolder.getPermissions(), perms);
	}

	public void testMoveWithOverwrite() throws Exception {
		final FolderObject moveMe = buildFolderToCopy();
		final OCLPermission[] perms = buildFolderToCopyPermissions();

		manager.createFolder(moveMe, true, System.currentTimeMillis());
		folders.add(moveMe);

		final FolderObject overwriteMe = buildFolderToOverwrite();
		manager.createFolder(overwriteMe, true, System.currentTimeMillis());
		folders.add(overwriteMe);

		final WebdavPath url = url(privateInfostoreFolder, moveMe);
		final WebdavPath moveUrl = url(privateInfostoreFolder, overwriteMe);

		factory.resolveCollection(url).move(moveUrl,false,true);

		final FolderCollection theDestination = (FolderCollection) factory.resolveCollection(moveUrl);
		folders.remove(moveMe);
		final FolderObject theDestinationFolder = FolderObject.loadFolderObjectFromDB(theDestination.getId(), ctx);
		assertPermissions(theDestinationFolder.getPermissions(), perms);
	}


	public void testCopyWithoutOverwrite() throws Exception {
		final FolderObject copyMe = buildFolderToCopy();

		manager.createFolder(copyMe, true, System.currentTimeMillis());
		folders.add(copyMe);

		final FolderObject overwriteMe = buildFolderToOverwrite();
		manager.createFolder(overwriteMe, true, System.currentTimeMillis());
		folders.add(overwriteMe);

		final WebdavPath url = url(privateInfostoreFolder, copyMe);
		final WebdavPath copyUrl = url(privateInfostoreFolder, overwriteMe);

		factory.resolveCollection(url).copy(copyUrl,false,false);

		final FolderCollection theCopy = (FolderCollection) factory.resolveCollection(copyUrl);
		final FolderObject theCopiedFolder = FolderObject.loadFolderObjectFromDB(theCopy.getId(), ctx);

		assertPermissions(theCopiedFolder.getPermissions(), buildFolderToOverwritePermissions());

	}

	public void testMoveWithoutOverwrite() throws Exception {
		final FolderObject moveMe = buildFolderToCopy();

		manager.createFolder(moveMe, true, System.currentTimeMillis());
		folders.add(moveMe);

		final FolderObject overwriteMe = buildFolderToOverwrite();
		manager.createFolder(overwriteMe, true, System.currentTimeMillis());
		folders.add(overwriteMe);

		final WebdavPath url = url(privateInfostoreFolder, moveMe);
		final WebdavPath moveUrl = url(privateInfostoreFolder, overwriteMe);

		factory.resolveCollection(url).move(moveUrl,false,false);

		final FolderCollection theDestination = (FolderCollection) factory.resolveCollection(moveUrl);
		folders.remove(moveMe);
		final FolderObject theDestinationFolder = FolderObject.loadFolderObjectFromDB(theDestination.getId(), ctx);

		assertPermissions(theDestinationFolder.getPermissions(), buildFolderToOverwritePermissions());
	}

	private void addDefaults(final FolderObject folder) {
		folder.setType(FolderObject.PUBLIC);
		folder.setModule(FolderObject.INFOSTORE);
		folder.setParentFolderID(this.privateInfostoreFolder.getObjectID());
	}

	private OCLPermission buildReadAll(final int userId,final boolean admin) {
		final OCLPermission perm = new OCLPermission();
		perm.setFolderAdmin(admin);
		if(admin) {
			perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
		} else {
			perm.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
		}

		perm.setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
		perm.setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
		perm.setDeleteObjectPermission(OCLPermission.DELETE_ALL_OBJECTS);
		perm.setGroupPermission(false);
		perm.setEntity(userId);
		return perm;
	}

	private OCLPermission buildReadOwn(final int userId, final boolean admin) {
		final OCLPermission perm = new OCLPermission();

		perm.setFolderAdmin(admin);
		if(admin) {
			perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
		} else {
			perm.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
		}

		perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
		perm.setReadObjectPermission(OCLPermission.READ_OWN_OBJECTS);
		perm.setWriteObjectPermission(OCLPermission.WRITE_OWN_OBJECTS);
		perm.setDeleteObjectPermission(OCLPermission.DELETE_OWN_OBJECTS);
		perm.setGroupPermission(false);
		perm.setEntity(userId);
		return perm;
	}

	private WebdavPath url(final FolderObject... folders) {
		final WebdavPath path = new WebdavPath("userstore");
		for(final FolderObject fo : folders) {
			path.append(fo.getFolderName());
		}

		return path;
	}


	private OCLPermission[] buildFolderToCopyPermissions() {
		final OCLPermission perm1 = buildReadAll(userIdA, true);
		final OCLPermission perm2 = buildReadOwn(userIdB,false);
		return new OCLPermission[]{perm1, perm2};
	}

	private FolderObject buildFolderToCopy() {
		final FolderObject folderToCopy = new FolderObject();
		addDefaults(folderToCopy);
		folderToCopy.setFolderName("CopyMe"+count++);
		folderToCopy.setPermissionsAsArray(buildFolderToCopyPermissions());
		return folderToCopy;
	}

	private OCLPermission[] buildFolderToOverwritePermissions() {
		final OCLPermission perm1 = buildReadAll(userIdA, true);
		final OCLPermission perm2 = buildReadAll(userIdB,false);
		return new OCLPermission[]{perm1, perm2};
	}

	private FolderObject buildFolderToOverwrite() {
		final FolderObject folderToOverwrite = new FolderObject();
		addDefaults(folderToOverwrite);
		folderToOverwrite.setFolderName("OverwriteMe");
		folderToOverwrite.setPermissionsAsArray(buildFolderToOverwritePermissions());
		return folderToOverwrite;
	}


	public static void assertPermissions(final Collection<OCLPermission> perms, final OCLPermission...expected){
		final Set<OCLPermission> expectSet = new HashSet<OCLPermission>(Arrays.asList(expected));

		for(final OCLPermission perm : perms) {
			OCLPermission matches = null;
			for(final OCLPermission permExpect : expectSet){
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

	private static String stringify(final OCLPermission perm) {
		final StringBuilder b = new StringBuilder();
		b.append("Read: ").append(perm.getReadPermission()).append(" Write: ")
		.append(perm.getWritePermission()).append(" Delete: ").append(perm.getDeletePermission())
		.append(" Folder: ").append(perm.getFolderPermission()).append(" Entity: ").append(perm.getEntity());
		return b.toString();
	}

}

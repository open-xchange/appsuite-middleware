package com.openexchange.groupware.infostore;

import java.util.List;
import junit.framework.TestCase;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.utils.DelUserFolderDiscoverer;
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

		final SearchIterator iter = OXFolderTools
        .getAllVisibleFoldersIteratorOfModule(userIdA,
            userA.getGroups(), userConfigA.getAccessibleModules(),
            FolderObject.INFOSTORE, ctx);

		try {
			while(privateInfostoreFolder == null && iter.hasNext()) {
				final FolderObject f = (FolderObject) iter.next();
				final List<OCLPermission> perms = f.getPermissions();
				if(f.isDefaultFolder()) {
					for(final OCLPermission perm : perms) {
                        if(perm.getFolderPermission() >= OCLPermission.ADMIN_PERMISSION && perm.getEntity() == userIdA) {
						    privateInfostoreFolder = f;
					    }
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
		final OCLPermission perm1 = buildReadAll(userIdA,true);
		final OCLPermission perm2 = buildReadOwn(userIdB,false);
		folderWithOtherEntity.setPermissionsAsArray(new OCLPermission[]{perm1, perm2});

        // Create all folders
		//OXFolderAction oxfa = new OXFolderAction(session);
		final OXFolderManager manager = OXFolderManager.getInstance(session);
		manager.createFolder(folderWithOtherEntity,true, System.currentTimeMillis());

        discoverer = new DelUserFolderDiscoverer(new DBPoolProvider());
	}

	@Override
	public void tearDown() throws Exception{
//		Remove all folders
		final OXFolderManager manager = OXFolderManager.getInstance(session);
		manager.deleteFolder(folderWithOtherEntity, true, System.currentTimeMillis());
		Init.stopServer();
	}

	public void testDiscoverFolders() throws Exception{
        final List<FolderObject> folders = discoverer.discoverFolders(userIdA, ctx, false);
        boolean privateFolderFound = false;
        for(final FolderObject folder : folders) {
            assertFalse(folder.getObjectID() == this.folderWithOtherEntity.getObjectID());
            assertOnlyUserCanRead(folder, userIdA);
            privateFolderFound = privateFolderFound || privateInfostoreFolder.getObjectID() == folder.getObjectID();
        }
        assertTrue(privateFolderFound);

    }

    private void assertOnlyUserCanRead(final FolderObject folder, final int userIdA) {
        boolean userCanRead = false;
        for(final OCLPermission perm : folder.getPermissions()) {
            if(perm.isGroupPermission()) {
                assertFalse(perm.canReadOwnObjects() || perm.canReadAllObjects());
            } else if (userIdA != perm.getEntity()){
                assertFalse(perm.canReadOwnObjects() || perm.canReadAllObjects());
            } else {
                userCanRead = userCanRead || perm.canReadAllObjects() || perm.canReadOwnObjects();
            }
        }
        assertTrue(userCanRead);
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

}

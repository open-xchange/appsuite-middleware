/**
 * 
 */
package com.openexchange.folder;

import junit.framework.TestCase;

import com.openexchange.cache.FolderCacheManager;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.oxfolder.OXFolderAction;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class FolderTest extends TestCase {
	
	public final static int CONTEXT_ID = 1;
	
	private static boolean init = false;
	
	private static int resolveUser(final String user) throws Exception {
        final UserStorage uStorage = UserStorage.getInstance(new ContextImpl(CONTEXT_ID));
        return uStorage.getUserId(user);
    }
	
	
	private SessionObject session;
	
	private int userId;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		if (!init) {
			Init.initDB();
		}
		/*
		 * Create session
		 */
		userId = resolveUser(Init.getAJAXProperty("login"));
		// ComfireConfig.loadProperties("/opt/openexchange/conf/groupware/system.properties");
		session = SessionObjectWrapper.createSessionObject(userId, CONTEXT_ID, "thorben_session_id");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testFolderInsertSuccess() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			final FolderObject fo = new FolderObject();
			fo.setFolderName("NewCalendarTestFolder");
			fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
			fo.setModule(FolderObject.CALENDAR);
			fo.setType(FolderObject.PRIVATE);
			final OCLPermission ocl = new OCLPermission();
			ocl.setEntity(userId);
			ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
			ocl.setGroupPermission(false);
			ocl.setFolderAdmin(true);
			fo.setPermissionsAsArray(new OCLPermission[] { ocl });
			final OXFolderAction oxfa = new OXFolderAction(session);
			final int fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true);
			/*
			 * Check folder object
			 */
			assertTrue(fuid > 0);
			assertTrue(fuid == fo.getObjectID());
			assertTrue(fo.getCreatedBy() == userId);
			assertTrue(fo.getModifiedBy() == userId);
			assertTrue(fo.containsCreationDate());
			assertTrue(fo.containsLastModified());
			/*
			 * Delete Test Folder...
			 */
			oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
			FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
			assertTrue(tmp == null);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testFolderInsertFail001() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			final FolderObject fo = new FolderObject();
			fo.setFolderName("NewCalendarTestFolder");
			fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
			fo.setModule(FolderObject.CALENDAR);
			fo.setType(FolderObject.PRIVATE);
			final OCLPermission ocl = new OCLPermission();
			// Wrong user id in permission!
			ocl.setEntity(userId - 2);
			ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
			ocl.setGroupPermission(false);
			ocl.setFolderAdmin(true);
			fo.setPermissionsAsArray(new OCLPermission[] { ocl });
			final OXFolderAction oxfa = new OXFolderAction(session);
			int fuid = -1;
			try {
				fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true);
			} catch (Exception e) {
				assertTrue(true);
			}
			if (fuid != -1) {
				oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
				fail("Exception expected!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}

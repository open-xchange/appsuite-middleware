/**
 * 
 */
package com.openexchange.folder;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.openexchange.cache.FolderCacheManager;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.EffectivePermission;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.oxfolder.OXFolderTools;
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
        if (init) {
            init = false;
            Init.stopDB();
        }
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
			final int fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
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
			final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
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
			Exception exc = null;
			try {
				fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
			} catch (Exception e) {
				System.out.println("\n\n\n" + e.getMessage());
				exc = e;
			}
			assertTrue(exc != null);
			if (fuid != -1) {
				oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
				fail("Exception expected!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testFolderInsertFail002() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			final FolderObject fo = new FolderObject();
			fo.setFolderName("NewCalendarTestFolder");
			fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
			fo.setModule(FolderObject.CALENDAR);
			fo.setType(FolderObject.PRIVATE);
			final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
			OCLPermission ocl = new OCLPermission();
			// Wrong user id in permission!
			ocl.setEntity(userId);
			ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
			ocl.setGroupPermission(false);
			ocl.setFolderAdmin(true);
			perms.add(ocl);
			ocl = new OCLPermission();
			ocl.setEntity(resolveUser(Init.getAJAXProperty("seconduser")));
			ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
			ocl.setGroupPermission(false);
			ocl.setFolderAdmin(true);
			perms.add(ocl);
			fo.setPermissions(perms);
			final OXFolderAction oxfa = new OXFolderAction(session);
			int fuid = -1;
			Exception exc = null;
			try {
				fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
			} catch (Exception e) {
				System.out.println("\n\n\n" + e.getMessage());
				exc = e;
			}
			assertTrue(exc != null);
			if (fuid != -1) {
				oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
				fail("Exception expected!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testFolderInsertFail003() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			final FolderObject fo = new FolderObject();
			fo.setFolderName("NewCalendarTestFolder");
			fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
			fo.setModule(FolderObject.CALENDAR);
			fo.setType(FolderObject.PUBLIC);
			final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
			final OCLPermission ocl = new OCLPermission();
			ocl.setEntity(userId);
			ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
			ocl.setGroupPermission(false);
			ocl.setFolderAdmin(true);
			perms.add(ocl);
			fo.setPermissions(perms);
			final OXFolderAction oxfa = new OXFolderAction(session);
			int fuid = -1;
			Exception exc = null;
			try {
				fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
			} catch (Exception e) {
				System.out.println("\n\n\n" + e.getMessage());
				exc = e;
			}
			assertTrue(exc != null);
			if (fuid != -1) {
				oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
				fail("Exception expected!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testFolderInsertFail004() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			final FolderObject fo = new FolderObject();
			fo.setFolderName("NewCalendarTestFolder");
			fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
			fo.setModule(FolderObject.INFOSTORE);
			fo.setType(FolderObject.PRIVATE);
			final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
			final OCLPermission ocl = new OCLPermission();
			ocl.setEntity(userId);
			ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
			ocl.setGroupPermission(false);
			ocl.setFolderAdmin(true);
			perms.add(ocl);
			fo.setPermissions(perms);
			final OXFolderAction oxfa = new OXFolderAction(session);
			int fuid = -1;
			Exception exc = null;
			try {
				fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
			} catch (Exception e) {
				System.out.println("\n\n\n" + e.getMessage());
				exc = e;
			}
			assertTrue(exc != null);
			if (fuid != -1) {
				oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
				fail("Exception expected!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testUpdateFolderSuccessRename() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			int fuid = -1;
			OXFolderAction oxfa = null;
			try {
				FolderObject fo = new FolderObject();
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
				oxfa = new OXFolderAction(session);
				fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
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
				 * Rename folder
				 */
				fo.reset();
				fo.setObjectID(fuid);
				fo.setFolderName("NewCalendarTestFolderRenamed");
				final long lastModified = System.currentTimeMillis();
				fo = oxfa.updateMoveRenameFolder(fo, session, true, lastModified, null, null);
				assertTrue(fo.containsLastModified());
				assertTrue(fo.containsModifiedBy());
				assertTrue(fo.getLastModified().getTime() == lastModified);
				assertTrue(fo.getModifiedBy() == userId);
			} finally {
				/*
				 * Delete Test Folder...
				 */
				oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
				final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
				assertTrue(tmp == null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testUpdateFolderSuccessMove() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			int fuid = -1;
			OXFolderAction oxfa = null;
			
			try {
				FolderObject fo = new FolderObject();
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
				oxfa = new OXFolderAction(session);
				fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
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
				 * Move folder
				 */
				final int stdCalFolder = OXFolderTools.getCalendarStandardFolder(userId, session.getContext());
				fo.reset();
				fo.setObjectID(fuid);
				fo.setParentFolderID(stdCalFolder);
				final long lastModified = System.currentTimeMillis();
				fo = oxfa.updateMoveRenameFolder(fo, session, true, lastModified, null, null);
				assertTrue(fo.containsLastModified());
				assertTrue(fo.containsModifiedBy());
				assertTrue(fo.getLastModified().getTime() == lastModified);
				assertTrue(fo.getModifiedBy() == userId);
				assertTrue(fo.getParentFolderID() == stdCalFolder);
			} finally {
				/*
				 * Delete Test Folder...
				 */
				oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
				final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
				assertTrue(tmp == null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testUpdateFolderSuccessRenameMove() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			int fuid = -1;
			OXFolderAction oxfa = null;
			
			try {
				FolderObject fo = new FolderObject();
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
				oxfa = new OXFolderAction(session);
				fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
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
				 * Rename & Move folder
				 */
				final int stdCalFolder = OXFolderTools.getCalendarStandardFolder(userId, session.getContext());
				fo.reset();
				fo.setObjectID(fuid);
				fo.setParentFolderID(stdCalFolder);
				fo.setFolderName("AARRGGH!");
				final long lastModified = System.currentTimeMillis();
				fo = oxfa.updateMoveRenameFolder(fo, session, true, lastModified, null, null);
				assertTrue(fo.containsLastModified());
				assertTrue(fo.containsModifiedBy());
				assertTrue(fo.getLastModified().getTime() == lastModified);
				assertTrue(fo.getModifiedBy() == userId);
				assertTrue(fo.getParentFolderID() == stdCalFolder);
			} finally {
				/*
				 * Delete Test Folder...
				 */
				oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
				final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
				assertTrue(tmp == null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testUpdateFolderSuccessAll() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			int fuid = -1;
			OXFolderAction oxfa = null;
			
			try {
				FolderObject fo = new FolderObject();
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
				oxfa = new OXFolderAction(session);
				fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
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
				 * Rename & Move folder
				 */
				final int stdCalFolder = OXFolderTools.getCalendarStandardFolder(userId, session.getContext());
				final int secondUserId = resolveUser(Init.getAJAXProperty("seconduser"));
				fo.reset();
				fo.setObjectID(fuid);
				fo.setParentFolderID(stdCalFolder);
				fo.setFolderName("Shared_AARRGGH!");
				final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
				OCLPermission updateOCL = new OCLPermission();
				updateOCL.setEntity(userId);
				updateOCL.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				updateOCL.setGroupPermission(false);
				updateOCL.setFolderAdmin(true);
				perms.add(updateOCL);
				updateOCL = new OCLPermission();
				updateOCL.setEntity(secondUserId);
				updateOCL.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				updateOCL.setGroupPermission(false);
				updateOCL.setFolderAdmin(false);
				perms.add(updateOCL);
				fo.setPermissions(perms);
				
				final long lastModified = System.currentTimeMillis();
				fo = oxfa.updateMoveRenameFolder(fo, session, true, lastModified, null, null);
				assertTrue(fo.containsLastModified());
				assertTrue(fo.containsModifiedBy());
				assertTrue(fo.getLastModified().getTime() == lastModified);
				assertTrue(fo.getModifiedBy() == userId);
				assertTrue(fo.getParentFolderID() == stdCalFolder);
				final EffectivePermission ep = fo.getEffectiveUserPermission(secondUserId, UserConfiguration.loadUserConfiguration(secondUserId, new ContextImpl(CONTEXT_ID)));
				assertTrue(ep.isFolderVisible());
				assertTrue(ep.canCreateSubfolders());
				assertTrue(ep.canDeleteAllObjects());
				assertTrue(ep.canWriteAllObjects());
				assertTrue(ep.canReadAllObjects());
				assertFalse(ep.isFolderAdmin());
			} finally {
				/*
				 * Delete Test Folder...
				 */
				oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
				final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
				assertTrue(tmp == null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testDeleteFolder() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			int fuid = -1;
			OXFolderAction oxfa = null;
			try {
				FolderObject fo = new FolderObject();
				fo.setFolderName("NewCalendarTestFolder");
				fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
				fo.setModule(FolderObject.CALENDAR);
				fo.setType(FolderObject.PRIVATE);
				OCLPermission ocl = new OCLPermission();
				ocl.setEntity(userId);
				ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				ocl.setGroupPermission(false);
				ocl.setFolderAdmin(true);
				fo.setPermissionsAsArray(new OCLPermission[] { ocl });
				oxfa = new OXFolderAction(session);
				fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
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
				 * Create multiple subfolders
				 */
				fo.reset();
				fo.setFolderName("NewContactTestSubFolder001");
				fo.setParentFolderID(fuid);
				fo.setModule(FolderObject.CONTACT);
				fo.setType(FolderObject.PRIVATE);
				ocl = new OCLPermission();
				ocl.setEntity(userId);
				ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				ocl.setGroupPermission(false);
				ocl.setFolderAdmin(true);
				fo.setPermissionsAsArray(new OCLPermission[] { ocl });
				oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				
				fo.reset();
				fo.setFolderName("NewTaskTestSubFolder002");
				fo.setParentFolderID(fuid);
				fo.setModule(FolderObject.TASK);
				fo.setType(FolderObject.PRIVATE);
				ocl = new OCLPermission();
				ocl.setEntity(userId);
				ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				ocl.setGroupPermission(false);
				ocl.setFolderAdmin(true);
				fo.setPermissionsAsArray(new OCLPermission[] { ocl });
				oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				
				int subfolderId = -1;
				
				fo.reset();
				fo.setFolderName("NewCalendarTestSubFolder003");
				fo.setParentFolderID(fuid);
				fo.setModule(FolderObject.CALENDAR);
				fo.setType(FolderObject.PRIVATE);
				ocl = new OCLPermission();
				ocl.setEntity(userId);
				ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				ocl.setGroupPermission(false);
				ocl.setFolderAdmin(true);
				fo.setPermissionsAsArray(new OCLPermission[] { ocl });
				subfolderId = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				
				
				fo.reset();
				fo.setFolderName("NewContactTestSubSubFolder001");
				fo.setParentFolderID(subfolderId);
				fo.setModule(FolderObject.CONTACT);
				fo.setType(FolderObject.PRIVATE);
				ocl = new OCLPermission();
				ocl.setEntity(userId);
				ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				ocl.setGroupPermission(false);
				ocl.setFolderAdmin(true);
				fo.setPermissionsAsArray(new OCLPermission[] { ocl });
				oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				
				/*
				 * Delet parent folder that should also delete all subfolders
				 */
				final long lastModified = System.currentTimeMillis();
				try {
					oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, lastModified);
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
				
				fo.reset();
				fo.setObjectID(fuid);
				assertFalse(fo.exists(session.getContext()));
			} finally {
				/*
				 * Delete Test Folder...
				 */
				final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
				if (tmp != null) {
					oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, System.currentTimeMillis());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
}

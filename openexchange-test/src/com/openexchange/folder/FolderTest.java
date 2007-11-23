/**
 * 
 */
package com.openexchange.folder;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;

import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.userconfiguration.RdbUserConfigurationStorage;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.tools.oxfolder.OXFolderTools;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class FolderTest extends TestCase {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(FolderTest.class);
	
	public final static int CONTEXT_ID = 1337;
	
	private static boolean init = false;
	
	private static int resolveUser(String user) throws Exception {
		try {
			int pos = -1;
			user = (pos = user.indexOf('@')) > -1 ? user.substring(0, pos) : user; 
	        final UserStorage uStorage = UserStorage.getInstance(new ContextImpl(CONTEXT_ID));
	        return uStorage.getUserId(user);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
			return -1;
		}
    }
	
	
	private SessionObject session;
	
	private int userId;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		if (!init) {
			Init.startServer();
		}
		ContextStorage.init();
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
            Init.stopServer();
        }
		super.tearDown();
	}
	
	public void testFolderInsertSuccess() {
		final int userId = session.getUserObject().getId();
		final int[] groups = session.getUserObject().getGroups();
		//final OXFolderAction oxfa = new OXFolderAction(session);
		final OXFolderManager oxma = new OXFolderManagerImpl(session);
		int fuid = -1;
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
			/*
			 * Create folder
			 */
			fuid = oxma.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
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
			oxma.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
			fuid = -1;
			//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
			final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
			assertTrue(tmp == null);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			if (fuid != -1) {
				try {
					/*
					 * Delete Test Folder...
					 */
					oxma.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
					//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
	
	public void testFolderInsertFail001() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			FolderObject fo = new FolderObject();
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
			//final OXFolderAction oxfa = new OXFolderAction(session);
			final OXFolderManager oxma = new OXFolderManagerImpl(session); 
			Exception exc = null;
			try {
				oxma.createFolder(fo, true, System.currentTimeMillis());
			} catch (Exception e) {
				System.out.println("\n\n\n" + e.getMessage());
				exc = e;
			}
			assertTrue(exc != null);
			if (fo.containsObjectID() && fo.getObjectID() != -1) {
				oxma.deleteFolder(fo, true, System.currentTimeMillis());
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
			FolderObject fo = new FolderObject();
			fo.setFolderName("NewCalendarTestFolder");
			fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
			fo.setModule(FolderObject.CALENDAR);
			fo.setType(FolderObject.PRIVATE);
			final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
			OCLPermission ocl = new OCLPermission();
			ocl.setEntity(userId);
			ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
			ocl.setGroupPermission(false);
			ocl.setFolderAdmin(true);
			perms.add(ocl);
			/*
			 * ERROR: Define a second admin
			 */
			ocl = new OCLPermission();
			ocl.setEntity(resolveUser(Init.getAJAXProperty("seconduser")));
			ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
			ocl.setGroupPermission(false);
			ocl.setFolderAdmin(true);
			perms.add(ocl);
			fo.setPermissions(perms);
			//final OXFolderAction oxfa = new OXFolderAction(session);
			final OXFolderManager oxma = new OXFolderManagerImpl(session);
			Exception exc = null;
			try {
				oxma.createFolder(fo, true, System.currentTimeMillis());
				//fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
			} catch (Exception e) {
				System.out.println("\n\n\n" + e.getMessage());
				exc = e;
			}
			assertTrue(exc != null);
			if (fo.containsObjectID() && fo.getObjectID() != -1) {
				oxma.deleteFolder(fo, true, System.currentTimeMillis());
				//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
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
			/*
			 * ERROR: Define a wrong type. Enforced type should be private since
			 * we are going to insert beneath system private folder
			 */
			fo.setType(FolderObject.PUBLIC);
			final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
			final OCLPermission ocl = new OCLPermission();
			ocl.setEntity(userId);
			ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
			ocl.setGroupPermission(false);
			ocl.setFolderAdmin(true);
			perms.add(ocl);
			fo.setPermissions(perms);
			//final OXFolderAction oxfa = new OXFolderAction(session);
			final OXFolderManager oxma = new OXFolderManagerImpl(session);
			int fuid = -1;
			Exception exc = null;
			try {
				fuid = oxma.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
				//fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
			} catch (Exception e) {
				exc = e;
			}
			assertTrue(exc != null);
			if (fo.containsObjectID() && fuid != -1) {
				oxma.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
				//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
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
			/*
			 * ERROR: Define a prohibited module beneath system private folder
			 */
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
			//final OXFolderAction oxfa = new OXFolderAction(session);
			final OXFolderManager oxma = new OXFolderManagerImpl(session);
			int fuid = -1;
			Exception exc = null;
			try {
				fuid = oxma.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
				// fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
			} catch (Exception e) {
				System.out.println("\n\n\n" + e.getMessage());
				exc = e;
			}
			assertTrue(exc != null);
			if (fuid != -1) {
				oxma.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
				// oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
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
			OXFolderManager oxfa = null;
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
				oxfa = new OXFolderManagerImpl(session);
				fuid = oxfa.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
				// fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
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
				fo = oxfa.updateFolder(fo, true, System.currentTimeMillis());
				//fo = oxfa.updateMoveRenameFolder(fo, session, true, lastModified, null, null);
				assertTrue(fo.containsLastModified());
				assertTrue(fo.containsModifiedBy());
				assertTrue(fo.getLastModified().getTime() == lastModified);
				assertTrue(fo.getModifiedBy() == userId);
			} finally {
				/*
				 * Delete Test Folder...
				 */
				oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
				//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
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
			OXFolderManager oxfa = null;
			
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
				oxfa = new OXFolderManagerImpl(session);
				fuid = oxfa.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
				//fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
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
				final int stdCalFolder = OXFolderTools.getCalendarDefaultFolder(userId, session.getContext());
				fo.reset();
				fo.setObjectID(fuid);
				fo.setParentFolderID(stdCalFolder);
				final long lastModified = System.currentTimeMillis();
				fo = oxfa.updateFolder(fo, true, System.currentTimeMillis());
				// fo = oxfa.updateMoveRenameFolder(fo, session, true, lastModified, null, null);
				assertTrue(fo.containsLastModified());
				assertTrue(fo.containsModifiedBy());
				assertTrue(fo.getLastModified().getTime() == lastModified);
				assertTrue(fo.getModifiedBy() == userId);
				assertTrue(fo.getParentFolderID() == stdCalFolder);
			} finally {
				/*
				 * Delete Test Folder...
				 */
				oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
				//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
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
			OXFolderManager oxfa = null;
			
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
				oxfa = new OXFolderManagerImpl(session);
				fuid = oxfa.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
				//fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
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
				final int stdCalFolder = OXFolderTools.getCalendarDefaultFolder(userId, session.getContext());
				fo.reset();
				fo.setObjectID(fuid);
				fo.setParentFolderID(stdCalFolder);
				fo.setFolderName("AARRGGH!");
				final long lastModified = System.currentTimeMillis();
				fo = oxfa.updateFolder(fo, true, System.currentTimeMillis());
				//fo = oxfa.updateMoveRenameFolder(fo, session, true, lastModified, null, null);
				assertTrue(fo.containsLastModified());
				assertTrue(fo.containsModifiedBy());
				assertTrue(fo.getLastModified().getTime() == lastModified);
				assertTrue(fo.getModifiedBy() == userId);
				assertTrue(fo.getParentFolderID() == stdCalFolder);
			} finally {
				/*
				 * Delete Test Folder...
				 */
				oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
				//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
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
			OXFolderManager oxfa = null;
			
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
				oxfa = new OXFolderManagerImpl(session);
				oxfa.createFolder(fo, true, System.currentTimeMillis());
				fuid = fo.getObjectID();
				//fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
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
				final int stdCalFolder = OXFolderTools.getCalendarDefaultFolder(userId, session.getContext());
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
				//fo = oxfa.updateMoveRenameFolder(fo, session, true, lastModified, null, null);
				oxfa.updateFolder(fo, true, System.currentTimeMillis());
				assertTrue(fo.containsLastModified());
				assertTrue(fo.containsModifiedBy());
				assertTrue(fo.getLastModified().getTime() == lastModified);
				assertTrue(fo.getModifiedBy() == userId);
				assertTrue(fo.getParentFolderID() == stdCalFolder);
				final EffectivePermission ep = fo.getEffectiveUserPermission(secondUserId, RdbUserConfigurationStorage.loadUserConfiguration(secondUserId, new ContextImpl(CONTEXT_ID)));
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
				oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
				//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
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
			OXFolderManager oxfa = null;
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
				oxfa = new OXFolderManagerImpl(session);
				fo = oxfa.createFolder(fo, true, System.currentTimeMillis());
				//fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				fuid = fo.getObjectID();
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
				oxfa.createFolder(fo, true, System.currentTimeMillis());
				//oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				
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
				oxfa.createFolder(fo, true, System.currentTimeMillis());
				//oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				
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
				subfolderId = oxfa.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
				//subfolderId = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				
				
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
				oxfa.createFolder(fo, true, System.currentTimeMillis());
				//oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				
				/*
				 * Delet parent folder that should also delete all subfolders
				 */
				final long lastModified = System.currentTimeMillis();
				try {
					oxfa.deleteFolder(new FolderObject(fuid), true, lastModified);
					//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, lastModified);
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
					oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
					//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testWithModifiedUserConfig001() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			int fuid = -1;
			OXFolderManager oxfa = null;
			try {
				/*
				 * Create a public folder
				 */
				FolderObject fo = new FolderObject();
				fo.setFolderName("NewCalendarTestFolder");
				fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
				fo.setModule(FolderObject.CALENDAR);
				fo.setType(FolderObject.PUBLIC);
				OCLPermission ocl = new OCLPermission();
				ocl.setEntity(userId);
				ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				ocl.setGroupPermission(false);
				ocl.setFolderAdmin(true);
				fo.setPermissionsAsArray(new OCLPermission[] { ocl });
				oxfa = new OXFolderManagerImpl(session);
				try {
					oxfa.createFolder(fo, true, System.currentTimeMillis());
					fuid = fo.getObjectID();
					//fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				} catch (Exception e) {
					if (fuid > 0) {
						oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
					}
					fail(e.getMessage());
				}
				/*
				 * Deny creation or modifications of public folders
				 */
				session.getUserConfiguration().setFullPublicFolderAccess(false);
				/*
				 * Try to edit a public folder
				 */
				fo.setFolderName("NewCalendarTestFolder_Changed");
				Exception exc=null;
				try {
					fo = oxfa.updateFolder(fo, true, System.currentTimeMillis());
					//fo = oxfa.updateMoveRenameFolder(fo, session, true, System.currentTimeMillis(), null, null);
				} catch (Exception e) {
					System.out.println("\n\n\n" + e.getMessage());
					exc = e;
				}
				assertTrue(exc != null);
			} finally {
				session.getUserConfiguration().setFullPublicFolderAccess(true);
				/*
				 * Delete Test Folder...
				 */
				final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
				if (tmp != null) {
					oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
					//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testWithModifiedUserConfig002() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			int fuid = -1;
			OXFolderManager oxfa = null;
			try {
				/*
				 * Create a public folder
				 */
				FolderObject fo = new FolderObject();
				fo.setFolderName("NewCalendarTestFolder");
				fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
				fo.setModule(FolderObject.CALENDAR);
				fo.setType(FolderObject.PUBLIC);
				OCLPermission ocl = new OCLPermission();
				ocl.setEntity(userId);
				ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				ocl.setGroupPermission(false);
				ocl.setFolderAdmin(true);
				fo.setPermissionsAsArray(new OCLPermission[] { ocl });
				oxfa = new OXFolderManagerImpl(session);
				oxfa.createFolder(fo, true, System.currentTimeMillis());
				fuid = fo.getObjectID();
				// fuid = oxfa.createFolder(fo, userId, groups, session.getUserConfiguration(), true, true, session.getContext(), null, null, true, true);
				/*
				 * Deny calendar module access
				 */
				session.getUserConfiguration().setCalendar(false);
				/*
				 * Try to edit a public folder
				 */
				fo.setFolderName("NewCalendarTestFolder_Changed");
				Exception exc=null;
				try {
					fo = oxfa.updateFolder(fo, true, System.currentTimeMillis());
					// fo = oxfa.updateMoveRenameFolder(fo, session, true, System.currentTimeMillis(), null, null);
				} catch (Exception e) {
					System.out.println("\n\n\n" + e.getMessage());
					exc = e;
				}
				assertTrue(exc != null);
			} finally {
				session.getUserConfiguration().setCalendar(true);
				/*
				 * Delete Test Folder...
				 */
				final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
				if (tmp != null) {
					oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
					//oxfa.deleteFolder(fuid, userId, groups, session.getUserConfiguration(), true, session.getContext(), null, null, System.currentTimeMillis());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testFolderCleaning() {
		try {
			final int userId = session.getUserObject().getId();
			final int[] groups = session.getUserObject().getGroups();
			int fuid = -1;
			OXFolderManager oxfa = null;
			try {
				/*
				 * Create a public folder
				 */
				FolderObject fo = new FolderObject();
				fo.setFolderName("NewCalendarTestFolder");
				fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
				fo.setModule(FolderObject.CALENDAR);
				fo.setType(FolderObject.PUBLIC);
				OCLPermission ocl = new OCLPermission();
				ocl.setEntity(userId);
				ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
				ocl.setGroupPermission(false);
				ocl.setFolderAdmin(true);
				fo.setPermissionsAsArray(new OCLPermission[] { ocl });
				oxfa = new OXFolderManagerImpl(session);
				oxfa.createFolder(fo, true, System.currentTimeMillis());
				fuid = fo.getObjectID();
				/*
				 * Put some objects in folder
				 */
				CalendarDataObject cdao = new CalendarDataObject();
		        cdao.setTitle("testInsertAndAlarm - Step 1 - Insert");
		        cdao.setParentFolderID(fuid);
		        cdao.setContext(session.getContext());
		        cdao.setIgnoreConflicts(true);
		      
		        UserParticipant up = new UserParticipant();
		        up.setIdentifier(session.getUserObject().getId());
		        up.setAlarmMinutes(5);
		        cdao.setUsers(new UserParticipant[] { up });
		        
		        
		        Participants participants = new Participants();
		        Participant p = new UserParticipant();
		        p.setIdentifier(session.getUserObject().getId());
		        participants.add(p);
		 
		        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
		        
		        Participant p2 = new UserParticipant();
		        p2.setIdentifier(resolveUser(user2));
		        participants.add(p2);        
		        
		        cdao.setParticipants(participants.getList());        
		        
		        fillDatesInDao(cdao);
		        
		        CalendarSql csql = new CalendarSql(session);        
		        csql.insertAppointmentObject(cdao);        
		        int object_id = cdao.getObjectID();
		        CalendarDataObject testobject = csql.getObjectById(object_id, fuid);
		        
		        /*
		         * Clean folder
		         */
		        try {
					new OXFolderManagerImpl(session).clearFolder(fo, true, System.currentTimeMillis());
				} catch (Exception e) {
					fail(e.getMessage());
				}
				assertTrue(true);
			} finally {
				/*
				 * Delete Test Folder...
				 */
				final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, session.getContext());
				if (tmp != null) {
					oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	private static final void fillDatesInDao(CalendarDataObject cdao) {
        long s = System.currentTimeMillis();
        long cals = s;
        long calsmod = s%CalendarRecurringCollection.MILLI_DAY;
        cals = cals- calsmod;
        long endcalc = 3600000;
        long mod = s%3600000;
        s = s - mod;
        long saves = s;
        long e = s + endcalc;
        long savee = e;
        long u = s + (CalendarRecurringCollection.MILLI_DAY * 10);
        mod = u%CalendarRecurringCollection.MILLI_DAY;
        u = u - mod;
        
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        
    }
	
	private static final Properties getAJAXProperties() {
        Properties properties = Init.getAJAXProperties();
        return properties;
    }
	
	public void testGetSubfolders() {
		try {
			FolderSQLInterface folderSQLInterface = new RdbFolderSQLInterface(session);
			SearchIterator it = null;
			try {
				it = folderSQLInterface.getSubfolders(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, null);
				if (it.hasSize()) {
					final int size = it.size();
					assertTrue(size >= 3);
					for (int i = 0; i < size; i++) {
						FolderObject fo = (FolderObject) it.next();
						assertTrue(fo != null);
					}
				} else {
					assertTrue(it.hasNext());
					while (it.hasNext()) {
						FolderObject fo = (FolderObject) it.next();
						assertTrue(fo != null);
					}
				}
			} finally {
				if (it != null) {
					it.close();
					it = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testGetSubfoldersWithRestrictedAccess() {
		try {
			session.getUserConfiguration().setCalendar(false);
			FolderSQLInterface folderSQLInterface = new RdbFolderSQLInterface(session);
			SearchIterator it = null;
			try {
				it = folderSQLInterface.getSubfolders(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, null);
				if (it.hasSize()) {
					final int size = it.size();
					assertTrue(size >= 1);
					for (int i = 0; i < size; i++) {
						FolderObject fo = (FolderObject) it.next();
						assertTrue(fo.getModule() != FolderObject.CALENDAR);
					}
				} else {
					assertTrue(it.hasNext());
					while (it.hasNext()) {
						FolderObject fo = (FolderObject) it.next();
						assertTrue(fo.getModule() != FolderObject.CALENDAR);
					}
				}
			} finally {
				if (it != null) {
					it.close();
					it = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
}

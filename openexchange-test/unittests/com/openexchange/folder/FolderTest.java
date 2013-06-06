/**
 *
 */
package com.openexchange.folder;

import com.openexchange.exception.OXException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import junit.framework.TestCase;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.MutableUserConfiguration;
import com.openexchange.groupware.userconfiguration.RdbUserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderTools;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class FolderTest extends TestCase {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
            .getLog(FolderTest.class);

    public final static int CONTEXT_ID = 1337;

    private static boolean init = false;

    private static int resolveUser(String user, final Context ctx) throws Exception {
        try {
            int pos = -1;
            user = (pos = user.indexOf('@')) > -1 ? user.substring(0, pos) : user;
            final UserStorage uStorage = UserStorage.getInstance();
            return uStorage.getUserId(user, ctx);
        } catch (final Throwable t) {
            t.printStackTrace();
            return -1;
        }
    }

    private SessionObject session;

    private Context ctx;

    private int userId;

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (!init) {
            Init.startServer();
        }
        /*
         * Create session
         */
        ctx = new ContextImpl(CONTEXT_ID);
        userId = resolveUser(AjaxInit.getAJAXProperty("login"), ctx);
        // ComfireConfig.loadProperties("/opt/openexchange/conf/groupware/system.properties");
        session = SessionObjectWrapper.createSessionObject(userId, CONTEXT_ID, "thorben_session_id");
    }

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        if (init) {
            init = false;
            Init.stopServer();
        }
        super.tearDown();
    }

    public AppointmentSQLInterface getAppointmentHandler(){
        return ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
    }

    public void testFolderInsertSuccess() throws Throwable {
        final int userId = session.getUserId();
        // final OXFolderAction oxfa = new OXFolderAction(session);
        final OXFolderManager oxma = OXFolderManager.getInstance(session);
        int fuid = -1;
        try {
            final FolderObject fo = new FolderObject();
            fo.setFolderName("NewCalendarTestFolder");
            fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            fo.setModule(FolderObject.CALENDAR);
            fo.setType(FolderObject.PRIVATE);
            final OCLPermission ocl = new OCLPermission();
            ocl.setEntity(userId);
            ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
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
            // oxfa.deleteFolder(fuid, userId, groups,
            // session.getUserConfiguration(), true, session.getContext(), null,
            // null, System.currentTimeMillis());
            final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, ctx);
            assertTrue(tmp == null);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (fuid != -1) {
                try {
                    /*
                     * Delete Test Folder...
                     */
                    oxma.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
                    // oxfa.deleteFolder(fuid, userId, groups,
                    // session.getUserConfiguration(), true,
                    // session.getContext(), null, null,
                    // System.currentTimeMillis());
                } catch (final Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    public void testFolderInsertFail001() {
        try {
            final int userId = session.getUserId();
            final FolderObject fo = new FolderObject();
            fo.setFolderName("NewCalendarTestFolder");
            fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            fo.setModule(FolderObject.CALENDAR);
            fo.setType(FolderObject.PRIVATE);
            final OCLPermission ocl = new OCLPermission();
            // Wrong user id in permission!
            ocl.setEntity(userId - 2);
            ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            ocl.setGroupPermission(false);
            ocl.setFolderAdmin(true);
            fo.setPermissionsAsArray(new OCLPermission[] { ocl });
            // final OXFolderAction oxfa = new OXFolderAction(session);
            final OXFolderManager oxma = OXFolderManager.getInstance(session);
            Exception exc = null;
            try {
                oxma.createFolder(fo, true, System.currentTimeMillis());
            } catch (final Exception e) {
                exc = e;
            }
            assertTrue(exc != null);
            if (fo.containsObjectID() && fo.getObjectID() != -1) {
                oxma.deleteFolder(fo, true, System.currentTimeMillis());
                fail("Exception expected!");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testFolderInsertFail002() {
        try {
            final int userId = session.getUserId();
            final FolderObject fo = new FolderObject();
            fo.setFolderName("NewCalendarTestFolder");
            fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            fo.setModule(FolderObject.CALENDAR);
            fo.setType(FolderObject.PRIVATE);
            final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
            OCLPermission ocl = new OCLPermission();
            ocl.setEntity(userId);
            ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            ocl.setGroupPermission(false);
            ocl.setFolderAdmin(true);
            perms.add(ocl);
            /*
             * ERROR: Define a second admin
             */
            ocl = new OCLPermission();
            ocl.setEntity(resolveUser(AjaxInit.getAJAXProperty("seconduser"), ctx));
            ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            ocl.setGroupPermission(false);
            ocl.setFolderAdmin(true);
            perms.add(ocl);
            fo.setPermissions(perms);
            // final OXFolderAction oxfa = new OXFolderAction(session);
            final OXFolderManager oxma = OXFolderManager.getInstance(session);
            Exception exc = null;
            try {
                oxma.createFolder(fo, true, System.currentTimeMillis());
                // fuid = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);
            } catch (final Exception e) {
                exc = e;
            }
            assertTrue(exc != null);
            if (fo.containsObjectID() && fo.getObjectID() != -1) {
                oxma.deleteFolder(fo, true, System.currentTimeMillis());
                // oxfa.deleteFolder(fuid, userId, groups,
                // session.getUserConfiguration(), true, session.getContext(),
                // null, null, System.currentTimeMillis());
                fail("Exception expected!");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testFolderInsertFail003() {
        try {
            final int userId = session.getUserId();
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
            ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            ocl.setGroupPermission(false);
            ocl.setFolderAdmin(true);
            perms.add(ocl);
            fo.setPermissions(perms);
            // final OXFolderAction oxfa = new OXFolderAction(session);
            final OXFolderManager oxma = OXFolderManager.getInstance(session);
            int fuid = -1;
            Exception exc = null;
            try {
                fuid = oxma.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
                // fuid = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);
            } catch (final Exception e) {
                exc = e;
            }
            assertTrue(exc != null);
            if (fo.containsObjectID() && fuid != -1) {
                oxma.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
                // oxfa.deleteFolder(fuid, userId, groups,
                // session.getUserConfiguration(), true, session.getContext(),
                // null, null, System.currentTimeMillis());
                fail("Exception expected!");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testFolderInsertFail004() {
        try {
            final int userId = session.getUserId();
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
            ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            ocl.setGroupPermission(false);
            ocl.setFolderAdmin(true);
            perms.add(ocl);
            fo.setPermissions(perms);
            // final OXFolderAction oxfa = new OXFolderAction(session);
            final OXFolderManager oxma = OXFolderManager.getInstance(session);
            int fuid = -1;
            Exception exc = null;
            try {
                fuid = oxma.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
                // fuid = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);
            } catch (final Exception e) {
                exc = e;
            }
            assertTrue(exc != null);
            if (fuid != -1) {
                oxma.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
                // oxfa.deleteFolder(fuid, userId, groups,
                // session.getUserConfiguration(), true, session.getContext(),
                // null, null, System.currentTimeMillis());
                fail("Exception expected!");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testFolderInsertFail005() {
        try {
            final int userId = session.getUserId();
            final FolderObject fo = new FolderObject();
            fo.setFolderName("NewCalendarTestFolder");
            fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            /*
             * ERROR: Define duplicate permissions
             */
            fo.setModule(FolderObject.CALENDAR);
            fo.setType(FolderObject.PRIVATE);
            final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
            {
                final OCLPermission ocl = new OCLPermission();
                ocl.setEntity(userId);
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                perms.add(ocl);
            }
            final int secondUser = resolveUser(AjaxInit.getAJAXProperty("seconduser"), ctx);
            {
                final OCLPermission ocl = new OCLPermission();
                ocl.setEntity(secondUser);
                ocl.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS,
                        OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(false);
                perms.add(ocl);
            }
            {
                final OCLPermission ocl = new OCLPermission();
                ocl.setEntity(secondUser);
                ocl.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS,
                        OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(false);
                perms.add(ocl);
            }
            fo.setPermissions(perms);
            // final OXFolderAction oxfa = new OXFolderAction(session);
            final OXFolderManager oxma = OXFolderManager.getInstance(session);
            int fuid = -1;
            Exception exc = null;
            try {
                fuid = oxma.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
                // fuid = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);
            } catch (final Exception e) {
                exc = e;
            }
            assertTrue(exc != null);
            if (fuid != -1) {
                oxma.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
                // oxfa.deleteFolder(fuid, userId, groups,
                // session.getUserConfiguration(), true, session.getContext(),
                // null, null, System.currentTimeMillis());
                fail("Exception expected!");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testUpdateFolderSuccessRename() {
        try {
            final int userId = session.getUserId();
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
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa = OXFolderManager.getInstance(session);
                fuid = oxfa.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
                // fuid = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);
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
                fo = oxfa.updateFolder(fo, true, false, System.currentTimeMillis());
                // fo = oxfa.updateMoveRenameFolder(fo, session, true,
                // lastModified, null, null);
                assertTrue(fo.containsLastModified());
                assertTrue(fo.containsModifiedBy());
                assertTrue(fo.getLastModified().getTime() == lastModified);
                assertTrue(fo.getModifiedBy() == userId);
            } finally {
                /*
                 * Delete Test Folder...
                 */
                oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
                // oxfa.deleteFolder(fuid, userId, groups,
                // session.getUserConfiguration(), true, session.getContext(),
                // null, null, System.currentTimeMillis());
                final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, ctx);
                assertTrue(tmp == null);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testUpdateFolderSuccessMove() {
        try {
            final int userId = session.getUserId();
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
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa = OXFolderManager.getInstance(session);
                fuid = oxfa.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
                // fuid = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);
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
                final int stdCalFolder = OXFolderTools.getCalendarDefaultFolder(userId, ctx);
                fo.reset();
                fo.setObjectID(fuid);
                fo.setParentFolderID(stdCalFolder);
                final long lastModified = System.currentTimeMillis();
                fo = oxfa.updateFolder(fo, true, false, System.currentTimeMillis());
                // fo = oxfa.updateMoveRenameFolder(fo, session, true,
                // lastModified, null, null);
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
                // oxfa.deleteFolder(fuid, userId, groups,
                // session.getUserConfiguration(), true, session.getContext(),
                // null, null, System.currentTimeMillis());
                final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, ctx);
                assertTrue(tmp == null);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testUpdateFolderSuccessRenameMove() {
        try {
            final int userId = session.getUserId();
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
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa = OXFolderManager.getInstance(session);
                fuid = oxfa.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
                // fuid = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);
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
                final int stdCalFolder = OXFolderTools.getCalendarDefaultFolder(userId, ctx);
                fo.reset();
                fo.setObjectID(fuid);
                fo.setParentFolderID(stdCalFolder);
                fo.setFolderName("AARRGGH!");
                final long lastModified = System.currentTimeMillis();
                fo = oxfa.updateFolder(fo, true, false, System.currentTimeMillis());
                // fo = oxfa.updateMoveRenameFolder(fo, session, true,
                // lastModified, null, null);
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
                // oxfa.deleteFolder(fuid, userId, groups,
                // session.getUserConfiguration(), true, session.getContext(),
                // null, null, System.currentTimeMillis());
                final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, ctx);
                assertTrue(tmp == null);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testUpdateFolderSuccessAll() {
        try {
            final int userId = session.getUserId();
            int fuid = -1;
            OXFolderManager oxfa = null;

            try {
                final FolderObject fo = new FolderObject();
                fo.setFolderName("NewCalendarTestFolder");
                fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
                fo.setModule(FolderObject.CALENDAR);
                fo.setType(FolderObject.PRIVATE);
                final OCLPermission ocl = new OCLPermission();
                ocl.setEntity(userId);
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa = OXFolderManager.getInstance(session);
                oxfa.createFolder(fo, true, System.currentTimeMillis());
                fuid = fo.getObjectID();
                // fuid = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);
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
                final int stdCalFolder = OXFolderTools.getCalendarDefaultFolder(userId, ctx);
                final int secondUserId = resolveUser(AjaxInit.getAJAXProperty("seconduser"), ctx);
                fo.reset();
                fo.setObjectID(fuid);
                fo.setParentFolderID(stdCalFolder);
                fo.setFolderName("Shared_AARRGGH!");
                final ArrayList<OCLPermission> perms = new ArrayList<OCLPermission>();
                OCLPermission updateOCL = new OCLPermission();
                updateOCL.setEntity(userId);
                updateOCL.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                updateOCL.setGroupPermission(false);
                updateOCL.setFolderAdmin(true);
                perms.add(updateOCL);
                updateOCL = new OCLPermission();
                updateOCL.setEntity(secondUserId);
                updateOCL.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                updateOCL.setGroupPermission(false);
                updateOCL.setFolderAdmin(false);
                perms.add(updateOCL);
                fo.setPermissions(perms);

                final long lastModified = System.currentTimeMillis();
                // fo = oxfa.updateMoveRenameFolder(fo, session, true,
                // lastModified, null, null);
                oxfa.updateFolder(fo, true, false, System.currentTimeMillis());
                assertTrue(fo.containsLastModified());
                assertTrue(fo.containsModifiedBy());
                assertTrue(fo.getLastModified().getTime() == lastModified);
                assertTrue(fo.getModifiedBy() == userId);
                assertTrue(fo.getParentFolderID() == stdCalFolder);
                final EffectivePermission ep = fo.getEffectiveUserPermission(secondUserId, RdbUserConfigurationStorage
                        .loadUserConfiguration(secondUserId, new ContextImpl(CONTEXT_ID)));
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
                // oxfa.deleteFolder(fuid, userId, groups,
                // session.getUserConfiguration(), true, session.getContext(),
                // null, null, System.currentTimeMillis());
                final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, ctx);
                assertTrue(tmp == null);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testDeleteFolder() {
        try {
            final int userId = session.getUserId();
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
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa = OXFolderManager.getInstance(session);
                fo = oxfa.createFolder(fo, true, System.currentTimeMillis());
                // fuid = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);
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
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa.createFolder(fo, true, System.currentTimeMillis());
                // oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);

                fo.reset();
                fo.setFolderName("NewTaskTestSubFolder002");
                fo.setParentFolderID(fuid);
                fo.setModule(FolderObject.TASK);
                fo.setType(FolderObject.PRIVATE);
                ocl = new OCLPermission();
                ocl.setEntity(userId);
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa.createFolder(fo, true, System.currentTimeMillis());
                // oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);

                int subfolderId = -1;

                fo.reset();
                fo.setFolderName("NewCalendarTestSubFolder003");
                fo.setParentFolderID(fuid);
                fo.setModule(FolderObject.CALENDAR);
                fo.setType(FolderObject.PRIVATE);
                ocl = new OCLPermission();
                ocl.setEntity(userId);
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                subfolderId = oxfa.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
                // subfolderId = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);

                fo.reset();
                fo.setFolderName("NewContactTestSubSubFolder001");
                fo.setParentFolderID(subfolderId);
                fo.setModule(FolderObject.CONTACT);
                fo.setType(FolderObject.PRIVATE);
                ocl = new OCLPermission();
                ocl.setEntity(userId);
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa.createFolder(fo, true, System.currentTimeMillis());
                // oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);

                /*
                 * Delet parent folder that should also delete all subfolders
                 */
                final long lastModified = System.currentTimeMillis();
                try {
                    oxfa.deleteFolder(new FolderObject(fuid), true, lastModified);
                    // oxfa.deleteFolder(fuid, userId, groups,
                    // session.getUserConfiguration(), true,
                    // session.getContext(), null, null, lastModified);
                } catch (final Exception e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }

                fo.reset();
                fo.setObjectID(fuid);
                assertFalse(fo.exists(ctx));
            } finally {
                /*
                 * Delete Test Folder...
                 */
                final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, ctx);
                if (tmp != null) {
                    oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
                    // oxfa.deleteFolder(fuid, userId, groups,
                    // session.getUserConfiguration(), true,
                    // session.getContext(), null, null,
                    // System.currentTimeMillis());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    private static MutableUserConfigurationStorage STORAGE = null;

    public static final MutableUserConfiguration getUserConfiguration(final Context ctx, final int userId)
            throws OXException {
        if (STORAGE == null) {
            STORAGE = new MutableUserConfigurationStorage(UserConfigurationStorage.getInstance());
        }
        return STORAGE.getUserConfiguration(userId, ctx).getMutable();
    }

    public static final void saveUserConfiguration(final UserConfiguration uc) throws OXException {
        if (STORAGE == null) {
            STORAGE = new MutableUserConfigurationStorage(UserConfigurationStorage.getInstance());
        }
        STORAGE.saveUserConfiguration(uc);
    }

    public void testWithModifiedUserConfig001() {
        try {
            final int userId = session.getUserId();
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
                final OCLPermission ocl = new OCLPermission();
                ocl.setEntity(userId);
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa = OXFolderManager.getInstance(session);
                try {
                    oxfa.createFolder(fo, true, System.currentTimeMillis());
                    fuid = fo.getObjectID();
                    // fuid = oxfa.createFolder(fo, userId, groups,
                    // session.getUserConfiguration(), true, true,
                    // session.getContext(), null, null, true, true);
                } catch (final Exception e) {
                    if (fuid > 0) {
                        oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
                    }
                    fail(e.getMessage());
                }
                /*
                 * Deny creation or modifications of public folders
                 */
                final MutableUserConfiguration uc = getUserConfiguration(ctx, userId);
                uc.setFullPublicFolderAccess(false);
                saveUserConfiguration(uc);
                /*
                 * Try to edit a public folder
                 */
                fo.setFolderName("NewCalendarTestFolder_Changed");
                Exception exc = null;
                try {
                    fo = oxfa.updateFolder(fo, true, false, System.currentTimeMillis());
                    // fo = oxfa.updateMoveRenameFolder(fo, session, true,
                    // System.currentTimeMillis(), null, null);
                } catch (final Exception e) {
                    exc = e;
                }
                assertTrue(exc != null);
            } finally {
                final MutableUserConfiguration uc = getUserConfiguration(ctx, userId);
                uc.setFullPublicFolderAccess(true);
                saveUserConfiguration(uc);
                /*
                 * Delete Test Folder...
                 */
                final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, ctx);
                if (tmp != null) {
                    oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
                    // oxfa.deleteFolder(fuid, userId, groups,
                    // session.getUserConfiguration(), true,
                    // session.getContext(), null, null,
                    // System.currentTimeMillis());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testWithModifiedUserConfig002() {
        try {
            final int userId = session.getUserId();
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
                final OCLPermission ocl = new OCLPermission();
                ocl.setEntity(userId);
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa = OXFolderManager.getInstance(session);
                oxfa.createFolder(fo, true, System.currentTimeMillis());
                fuid = fo.getObjectID();
                // fuid = oxfa.createFolder(fo, userId, groups,
                // session.getUserConfiguration(), true, true,
                // session.getContext(), null, null, true, true);
                /*
                 * Deny calendar module access
                 */
                final MutableUserConfiguration uc = getUserConfiguration(ctx, userId);
                uc.setCalendar(false);
                saveUserConfiguration(uc);
                /*
                 * Try to edit a public folder
                 */
                fo.setFolderName("NewCalendarTestFolder_Changed");
                Exception exc = null;
                try {
                    fo = oxfa.updateFolder(fo, true, false, System.currentTimeMillis());
                    // fo = oxfa.updateMoveRenameFolder(fo, session, true,
                    // System.currentTimeMillis(), null, null);
                } catch (final Exception e) {
                    exc = e;
                }
                assertTrue(exc != null);
            } finally {
                final MutableUserConfiguration uc = getUserConfiguration(ctx, userId);
                uc.setCalendar(true);
                saveUserConfiguration(uc);
                /*
                 * Delete Test Folder...
                 */
                final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, ctx);
                if (tmp != null) {
                    oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
                    // oxfa.deleteFolder(fuid, userId, groups,
                    // session.getUserConfiguration(), true,
                    // session.getContext(), null, null,
                    // System.currentTimeMillis());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testFolderCleaning() {
        try {
            final int userId = session.getUserId();
            int fuid = -1;
            OXFolderManager oxfa = null;
            try {
                /*
                 * Create a public folder
                 */
                final FolderObject fo = new FolderObject();
                fo.setFolderName("NewCalendarTestFolder");
                fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
                fo.setModule(FolderObject.CALENDAR);
                fo.setType(FolderObject.PUBLIC);
                final OCLPermission ocl = new OCLPermission();
                ocl.setEntity(userId);
                ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                ocl.setGroupPermission(false);
                ocl.setFolderAdmin(true);
                fo.setPermissionsAsArray(new OCLPermission[] { ocl });
                oxfa = OXFolderManager.getInstance(session);
                oxfa.createFolder(fo, true, System.currentTimeMillis());
                fuid = fo.getObjectID();
                /*
                 * Put some objects in folder
                 */
                final CalendarDataObject cdao = new CalendarDataObject();
                cdao.setTitle("testInsertAndAlarm - Step 1 - Insert");
                cdao.setParentFolderID(fuid);
                cdao.setContext(ctx);
                cdao.setIgnoreConflicts(true);

                final UserParticipant up = new UserParticipant(session.getUserId());
                up.setAlarmMinutes(5);
                cdao.setUsers(new UserParticipant[] { up });

                final Participants participants = new Participants();
                final Participant p = new UserParticipant(session.getUserId());
                participants.add(p);

                final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");

                final Participant p2 = new UserParticipant(resolveUser(user2, ctx));
                participants.add(p2);

                cdao.setParticipants(participants.getList());

                fillDatesInDao(cdao);

                AppointmentSQLInterface csql = getAppointmentHandler();
                csql.insertAppointmentObject(cdao);
                final int object_id = cdao.getObjectID();
                csql.getObjectById(object_id, fuid);

                /*
                 * Clean folder
                 */
                try {
                    OXFolderManager.getInstance(session).clearFolder(fo, true, System.currentTimeMillis());
                } catch (final Exception e) {
                    fail(e.getMessage());
                }
                assertTrue(true);
            } finally {
                /*
                 * Delete Test Folder...
                 */
                final FolderObject tmp = FolderCacheManager.getInstance().getFolderObject(fuid, ctx);
                if (tmp != null) {
                    oxfa.deleteFolder(new FolderObject(fuid), true, System.currentTimeMillis());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static final void fillDatesInDao(final Appointment cdao) {
        long s = System.currentTimeMillis();
        long cals = s;
        final long calsmod = s % Constants.MILLI_DAY;
        cals = cals - calsmod;
        final long endcalc = 3600000;
        long mod = s % 3600000;
        s = s - mod;
        final long e = s + endcalc;
        long u = s + (Constants.MILLI_DAY * 10);
        mod = u % Constants.MILLI_DAY;
        u = u - mod;

        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));

    }

    private static final Properties getAJAXProperties() {
        final Properties properties = AjaxInit.getAJAXProperties();
        return properties;
    }

    public void testGetSubfolders() {
        try {
            final FolderSQLInterface folderSQLInterface = new RdbFolderSQLInterface(ServerSessionAdapter.valueOf(session));
            SearchIterator<?> it = null;
            try {
                it = folderSQLInterface.getSubfolders(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, null);
                if (it.size() != -1) {
                    final int size = it.size();
                    assertTrue(size >= 3);
                    for (int i = 0; i < size; i++) {
                        final FolderObject fo = (FolderObject) it.next();
                        assertTrue(fo != null);
                    }
                } else {
                    assertTrue(it.hasNext());
                    while (it.hasNext()) {
                        final FolderObject fo = (FolderObject) it.next();
                        assertTrue(fo != null);
                    }
                }
            } finally {
                if (it != null) {
                    it.close();
                    it = null;
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testGetSubfoldersWithRestrictedAccess() {
        try {
            final MutableUserConfiguration uc = getUserConfiguration(ctx, userId);
            uc.setCalendar(false);
            saveUserConfiguration(uc);
            try {
                final FolderSQLInterface folderSQLInterface = new RdbFolderSQLInterface(ServerSessionAdapter.valueOf(session));
                final SearchIterator<?> it = folderSQLInterface.getSubfolders(FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
                        null);
                try {
                    if (it.size() != -1) {
                        final int size = it.size();
                        assertTrue(size >= 1);
                        for (int i = 0; i < size; i++) {
                            final FolderObject fo = (FolderObject) it.next();
                            assertTrue(fo.getModule() != FolderObject.CALENDAR);
                        }
                    } else {
                        assertTrue(it.hasNext());
                        while (it.hasNext()) {
                            final FolderObject fo = (FolderObject) it.next();
                            assertTrue(fo.getModule() != FolderObject.CALENDAR);
                        }
                    }
                } finally {
                    it.close();
                }
            } finally {
                final MutableUserConfiguration uc0 = getUserConfiguration(ctx, userId);
                uc0.setCalendar(true);
                saveUserConfiguration(uc0);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}

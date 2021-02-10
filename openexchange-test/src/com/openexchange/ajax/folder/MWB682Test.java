
package com.openexchange.ajax.folder;

import static org.junit.Assert.assertEquals;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * 
 * {@link MWB682Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.5
 */
public class MWB682Test extends AbstractAJAXSession {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        OCLPermission permission1 = new OCLPermission();
        permission1.setEntity(getClient().getValues().getUserId());
        permission1.setGroupPermission(false);
        permission1.setFolderAdmin(true);
        permission1.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);

        OCLPermission permission2 = new OCLPermission();
        permission2.setEntity(getClient2().getValues().getUserId());
        permission2.setGroupPermission(false);
        permission2.setFolderAdmin(false);
        permission2.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);

        FolderObject appointmentFolder = new FolderObject();
        appointmentFolder.setObjectID(getClient().getValues().getPrivateAppointmentFolder());
        appointmentFolder.addPermission(permission1);
        appointmentFolder.addPermission(permission2);
        appointmentFolder.setLastModified(new Date(Long.MAX_VALUE));
        ftm.updateFolderOnServer(appointmentFolder);

        FolderObject contactFolder = new FolderObject();
        contactFolder.setObjectID(getClient().getValues().getPrivateContactFolder());
        contactFolder.addPermission(permission1);
        contactFolder.addPermission(permission2);
        contactFolder.setLastModified(new Date(Long.MAX_VALUE));
        ftm.updateFolderOnServer(contactFolder);

        FolderObject taskFolder = new FolderObject();
        taskFolder.setObjectID(getClient().getValues().getPrivateTaskFolder());
        taskFolder.addPermission(permission1);
        taskFolder.addPermission(permission2);
        taskFolder.setLastModified(new Date(Long.MAX_VALUE));
        ftm.updateFolderOnServer(taskFolder);

    }

    @Test
    public void calendarWithPermissions() throws Exception {
        FolderObject sharedCalendarFolderWithPermissions = ftm.insertFolderOnServer(ftm.generatePrivateFolder("" + System.currentTimeMillis(), FolderObject.CALENDAR, getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId()));
        FolderObject loaded = ftm.getFolderFromServer(sharedCalendarFolderWithPermissions.getObjectID());
        assertEquals(1, loaded.getPermissions().size());
        assertEquals(getClient().getValues().getUserId(), loaded.getPermissions().get(0).getEntity());
    }

    @Test
    public void calendarWithoutPermissions() throws Exception {
        FolderObject cf = ftm.generatePrivateFolder("" + System.currentTimeMillis(), FolderObject.CALENDAR, getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId());
        cf.removePermissions();
        FolderObject calendarFolderWithoutPermissions = ftm.insertFolderOnServer(cf);
        FolderObject loaded = ftm.getFolderFromServer(calendarFolderWithoutPermissions.getObjectID());
        assertEquals(1, loaded.getPermissions().size());
        assertEquals(getClient().getValues().getUserId(), loaded.getPermissions().get(0).getEntity());
    }

    @Test
    public void contactWithPermissions() throws Exception {
        FolderObject sharedContactFolderWithPermissions = ftm.insertFolderOnServer(ftm.generatePrivateFolder("" + System.currentTimeMillis(), FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId()));
        FolderObject loaded = ftm.getFolderFromServer(sharedContactFolderWithPermissions.getObjectID());
        assertEquals(1, loaded.getPermissions().size());
        assertEquals(getClient().getValues().getUserId(), loaded.getPermissions().get(0).getEntity());
    }

    @Test
    public void contactWithoutPermissions() throws Exception {
        FolderObject cof = ftm.generatePrivateFolder("" + System.currentTimeMillis(), FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        cof.removePermissions();
        FolderObject contactFolderWithoutPermissions = ftm.insertFolderOnServer(cof);
        FolderObject loaded = ftm.getFolderFromServer(contactFolderWithoutPermissions.getObjectID());
        assertEquals(1, loaded.getPermissions().size());
        assertEquals(getClient().getValues().getUserId(), loaded.getPermissions().get(0).getEntity());
    }

    @Test
    public void taskWithPermissions() throws Exception {
        FolderObject sharedTaskFolderWithPermissions = ftm.insertFolderOnServer(ftm.generatePrivateFolder("" + System.currentTimeMillis(), FolderObject.TASK, getClient().getValues().getPrivateTaskFolder(), getClient().getValues().getUserId()));
        FolderObject loaded = ftm.getFolderFromServer(sharedTaskFolderWithPermissions.getObjectID());
        assertEquals(1, loaded.getPermissions().size());
        assertEquals(getClient().getValues().getUserId(), loaded.getPermissions().get(0).getEntity());
    }

    @Test
    public void taskWithoutPermissions() throws Exception {
        FolderObject tf = ftm.generatePrivateFolder("" + System.currentTimeMillis(), FolderObject.TASK, getClient().getValues().getPrivateTaskFolder(), getClient().getValues().getUserId());
        tf.removePermissions();
        FolderObject taskFolderWithoutPermissions = ftm.insertFolderOnServer(tf);
        FolderObject loaded = ftm.getFolderFromServer(taskFolderWithoutPermissions.getObjectID());
        assertEquals(1, loaded.getPermissions().size());
        assertEquals(getClient().getValues().getUserId(), loaded.getPermissions().get(0).getEntity());
    }
}

/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.AttachmentTest;
import com.openexchange.ajax.attach.AttachmentTools;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug16249Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug16249Test extends AttachmentTest {

    private int folderId;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        folderId = getClient().getValues().getPrivateAppointmentFolder();
    }

    @Test
    public void testBug16249() throws Exception {
        Appointment a = new Appointment();
        a.setTitle("Bug 16249 Test");
        a.setStartDate(D("01.07.2010 08:00"));
        a.setEndDate(D("01.07.2010 09:00"));
        a.setParentFolderID(folderId);
        a.setIgnoreConflicts(true);
        
        Appointment added = catm.insert(a);
        int appointmentId = added.getObjectID();
        Date beforeAttach = catm.get(added).getLastModified();
        
        final AttachmentMetadata attachment = new AttachmentImpl();
        attachment.setFolderId(folderId);
        attachment.setAttachedId(appointmentId);
        attachment.setModuleId(AttachmentTools.determineModule(a));

        int attachmentId = atm.attach(attachment, testFile.getName(), FileUtils.openInputStream(testFile), null);
        
        Date afterAttach = catm.get(folderId, appointmentId).getLastModified();

        atm.detach(attachment, new int[] { attachmentId });

        Date afterDetach = catm.get(folderId, appointmentId).getLastModified();

        assertTrue("Wrong last modified after attach", beforeAttach.compareTo(afterAttach) < 0);
        assertTrue("Wrong last modified after detach", beforeAttach.compareTo(afterDetach) < 0);
        assertTrue("Wrong last modified after detach", afterAttach.compareTo(afterDetach) < 0);
    }
}

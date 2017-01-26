/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.groupware.calendar.TimeTools.D;
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

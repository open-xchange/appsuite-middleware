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

package com.openexchange.webdav.xml.appointment;

import java.util.Date;
import java.util.Locale;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;

public class ListTest extends AppointmentTest {

    public ListTest(final String name) {
        super(name);
    }

    public void testPropFindWithModified() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testPropFindWithModified");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
        final int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId1, appointmentFolderId, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadAppointment.getCreationDate();

        final Appointment[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, true, false, PROTOCOL + hostName, login, password, context);

        assertTrue("Returned list of appointments only contains " + appointmentArray.length + " appointments", appointmentArray.length >= 2);

        boolean found1 = false;
        boolean found2 = false;

        for (int a = 0; a < appointmentArray.length; a++) {
            if (objectId1 == appointmentArray[a].getObjectID()) {
                found1 = true;
                if (found1 && found2) {
                    break;
                }
                continue;
            }
            if (objectId2 == appointmentArray[a].getObjectID()) {
                found2 = true;
                if (found1 && found2) {
                    break;
                }
                continue;
            }
        }

        assertTrue("objects not found in response", found1 && found2);

        final int[][] objectIdAndFolderId = { {objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);

    }

    public void testPropFindInPublicFolder() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testPropFindInPublicFolder" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] {
            FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
        };

        folderObj.setPermissionsAsArray( permission );

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testPropFindInPublicFolder");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(parentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, parentFolderId, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadAppointment.getCreationDate();

        final Appointment[] appointmentArray = listAppointment(webCon, parentFolderId, modified, true, false, PROTOCOL + hostName, login, password, context);

        boolean found = false;

        for (int a = 0; a < appointmentArray.length; a++) {
            if (objectId == appointmentArray[a].getObjectID()) {
                found = true;
                break;
            }
        }

        assertTrue("object not found in response", found);

        deleteAppointment(getWebConversation(), objectId, parentFolderId, getHostName(), getLogin(), getPassword(), context);
        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostName(), getLogin(), getPassword(), context);
    }

    public void testPropFindInPublicFolderWithGroupPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testPropFindInPublicFolderWithGroupPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        final int usersGroupId = 1; // Users
        final OCLPermission[] permission = new OCLPermission[] {
            FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
            FolderTest.createPermission( usersGroupId, true, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, false),
        };
        folderObj.setPermissionsAsArray(permission);
        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testPropFindInPublicFolderWithGroupPermission");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(parentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostName(), getLogin(), getPassword(), context);

        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, parentFolderId, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadAppointment.getCreationDate();

        final Appointment[] appointmentArray = listAppointment(getSecondWebConversation(), parentFolderId, modified, true, false, PROTOCOL + hostName, getSecondLogin(), getPassword(), context);
        boolean found = false;
        for (int a = 0; a < appointmentArray.length; a++) {
            if (objectId == appointmentArray[a].getObjectID()) {
                found = true;
                break;
            }
        }
        assertTrue("object not found in response", found);

        deleteAppointment(getWebConversation(), objectId, parentFolderId, getHostName(), getLogin(), getPassword(), context);
        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostName(), getLogin(), getPassword(), context);
    }

    public void testPropFindWithDelete() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testPropFindWithDelete");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);
        final int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId1, appointmentFolderId, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadAppointment.getCreationDate();

        final int[][] objectIdAndFolderId = { { objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);

        final Appointment[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, false, true, PROTOCOL + hostName, login, password, context);

        boolean found = false;

        for (int a = 0; a < appointmentArray.length; a++) {
            if (objectId1 == appointmentArray[a].getObjectID()) {
                found = true;
                break;
            }
        }

        assertTrue("object not found in response", found);
    }

    public void testPropFindWithObjectId() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testPropFindWithObjectId");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

        loadAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password, context);

        final int[][] objectIdAndFolderId = { { objectId ,appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
    }

    public void testObjectNotFound() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testObjectNotFound");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

        try {
            loadAppointment(webCon, (objectId+1000), appointmentFolderId, PROTOCOL + hostName, login, password, context);
            fail("object not found exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
        }

        final int[][] objectIdAndFolderId = { { objectId ,appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
    }

    public void testListWithAllFields() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testListWithAllFields");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setLocation("Location");
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setPrivateFlag(true);
        appointmentObj.setLabel(2);
        appointmentObj.setNote("note");
        appointmentObj.setCategories("testcat1,testcat2,testcat3");
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

        final Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadAppointment.getCreationDate();

        final Appointment[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, true, false, PROTOCOL + hostName, login, password, context);

        assertTrue("wrong response array length", appointmentArray.length >= 1);

        boolean found = false;
        for (int a = 0; a < appointmentArray.length; a++) {
            final Appointment checkAppointment = appointmentArray[a];

            if (checkAppointment.getObjectID() == objectId) {
                found = true;
                appointmentObj.setObjectID(objectId);
                compareObject(appointmentObj, checkAppointment);
            }
        }

        assertTrue("object not found in response", found);

        final int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
    }

    public void testList() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testObjectNotFound");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password, context);

        final int[] idArray = listAppointment(getWebConversation(), appointmentFolderId, getHostName(), getLogin(), getPassword(), context);

        boolean found = false;
        for (int a = 0; a < idArray.length; a++) {
            if (idArray[a] == objectId) {
                found = true;
                break;
            }
        }

        assertTrue("id " + objectId + " not found in response", found);

        final int[][] objectIdAndFolderId = { { objectId ,appointmentFolderId } };
        deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password, context);
    }
}

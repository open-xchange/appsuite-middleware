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

package com.openexchange.webdav.xml.folder;

import java.util.Date;
import java.util.Locale;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;

public class ListTest extends FolderTest {

    public ListTest(final String name) {
        super(name);
    }

    public void testPropFindWithModified() throws Exception {
        FolderObject folderObj = createFolderObject(userId, "testPropFindWithModified1", FolderObject.CONTACT, false);
        final int objectId1 = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj = createFolderObject(userId, "testPropFindWithModified2", FolderObject.TASK, false);
        insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);

        final FolderObject loadFolder = loadFolder(webCon, objectId1, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadFolder.getLastModified();

        final FolderObject[] folderArray = listFolder(webCon, decrementDate(modified), true, false, PROTOCOL + hostName, login, password, context);

        assertTrue("expected response size is >= 2", folderArray.length >= 2);
    }

    public void testPropFindWithDeleted() throws Exception {
        FolderObject folderObj = createFolderObject(userId, "testPropFindWithDeleted1", FolderObject.CALENDAR, false);
        final int objectId1 = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj = createFolderObject(userId, "testPropFindWithDeleted2", FolderObject.CONTACT, false);
        final int objectId2 = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);

        // prevent master/slave problem
        Thread.sleep(1000);

        final FolderObject loadFolder = loadFolder(webCon, objectId1, getHostName(), getLogin(), getPassword(), context);
        final Date modified = loadFolder.getLastModified();

        final int[] id = { objectId1, objectId2 };

        deleteFolder(webCon, id, PROTOCOL + hostName, login, password, context);

        final FolderObject[] folderArray = listFolder(webCon, decrementDate(modified), false, true, PROTOCOL + hostName, login, password, context);

        assertTrue("expected response size is < 2", folderArray.length >= 2);
    }

    public void testPropFindWithObjectIdOnPrivateFolder() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testPropFindWithObjectIdOnPrivateFolder" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] {
            createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
        };

        folderObj.setPermissionsAsArray( permission );

        final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

        loadFolder(getWebConversation(), objectId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

        deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
    }

    public void testPropFindWithObjectIdOnPublicFolder() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testPropFindWithObjectIdOnPublicFolder" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] {
            createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
        };

        folderObj.setPermissionsAsArray( permission );

        final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

        loadFolder(getWebConversation(), objectId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

        deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
    }

    public void testObjectNotFound() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testObjectNotFound" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] {
            createPermission( userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
        };

        folderObj.setPermissionsAsArray( permission );

        final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

        try {
            loadFolder(getWebConversation(), (objectId+10000), PROTOCOL + getHostName(), getLogin(), getPassword(), context);
            fail("object not found exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
        }

        deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
    }

    public void testList() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testList" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] {
            createPermission(userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
        };
        folderObj.setPermissionsAsArray(permission);

        final int objectId = insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

        final int[] idArray = listFolder(getWebConversation(), getHostName(), getLogin(), getPassword(),context);

        boolean found = false;
        for (int a = 0; a < idArray.length; a++) {
            if (idArray[a] == objectId) {
                found = true;
                break;
            }
        }

        assertTrue("id " + objectId + " not found in response", found);
        deleteFolder(getWebConversation(), new int[] { objectId }, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
    }
}

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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.RootRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.test.FolderTestManager;


/**
 * {@link FolderTreeTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FolderTreeTest extends AbstractOAuthTest {

    private FolderObject privateContactFolder;

    private FolderObject privateCalendarFolder;

    private FolderObject privateTaskFolder;

    private FolderTestManager ftm;

    private FolderObject publicContactFolder;

    private FolderObject publicCalendarFolder;

    private FolderObject publicTaskFolder;

    private UserValues values;

    private int privateTaskFolderId;

    private int privateAppointmentFolderId;

    private int privateContactFolderId;

    private AJAXClient ajaxClient2;

    private FolderTestManager ftm2;

    private FolderObject sharedContactFolder;

    private FolderObject sharedAppointmentFolder;

    private FolderObject sharedTaskFolder;

    public FolderTreeTest() {
        super(ContactActionFactory.OAUTH_READ_SCOPE, ContactActionFactory.OAUTH_WRITE_SCOPE, AppointmentActionFactory.OAUTH_READ_SCOPE, AppointmentActionFactory.OAUTH_WRITE_SCOPE, TaskActionFactory.OAUTH_READ_SCOPE, TaskActionFactory.OAUTH_WRITE_SCOPE);
    }

    @Before
    public void setUp() throws Exception {
        ftm = new FolderTestManager(ajaxClient);
        values = ajaxClient.getValues();
        int userId = values.getUserId();
        privateContactFolderId = values.getPrivateContactFolder();
        privateAppointmentFolderId = values.getPrivateAppointmentFolder();
        privateTaskFolderId = values.getPrivateTaskFolder();
        privateContactFolder = ftm.generatePrivateFolder("oauth provider folder tree test - private contacts " + System.currentTimeMillis(), FolderObject.CONTACT, privateContactFolderId, userId);
        privateCalendarFolder = ftm.generatePrivateFolder("oauth provider folder tree test - private calendar " + System.currentTimeMillis(), FolderObject.CALENDAR, privateAppointmentFolderId, userId);
        privateTaskFolder = ftm.generatePrivateFolder("oauth provider folder tree test - private tasks " + System.currentTimeMillis(), FolderObject.TASK, privateTaskFolderId, userId);
        publicContactFolder = ftm.generatePublicFolder("oauth provider folder tree test - public contacts " + System.currentTimeMillis(), FolderObject.CONTACT, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, userId);
        publicCalendarFolder = ftm.generatePublicFolder("oauth provider folder tree test - public calendar " + System.currentTimeMillis(), FolderObject.CALENDAR, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, userId);
        publicTaskFolder = ftm.generatePublicFolder("oauth provider folder tree test - public tasks " + System.currentTimeMillis(), FolderObject.TASK, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, userId);
        ftm.insertFoldersOnServer(new FolderObject[] { privateContactFolder, privateCalendarFolder, privateTaskFolder, publicContactFolder, publicCalendarFolder, publicTaskFolder });

        // prepare shared folders
        ajaxClient2 = new AJAXClient(User.User2);
        ftm2 = new FolderTestManager(ajaxClient2);
        sharedContactFolder = ftm2.generateSharedFolder("oauth provider folder tree test - shared contacts " + System.currentTimeMillis(), FolderObject.CONTACT, ajaxClient2.getValues().getPrivateContactFolder(), ajaxClient2.getValues().getUserId(), userId);
        sharedAppointmentFolder = ftm2.generateSharedFolder("oauth provider folder tree test - shared calendar " + System.currentTimeMillis(), FolderObject.CALENDAR, ajaxClient2.getValues().getPrivateAppointmentFolder(), ajaxClient2.getValues().getUserId(), userId);
        sharedTaskFolder = ftm2.generateSharedFolder("oauth provider folder tree test - shared tasks " + System.currentTimeMillis(), FolderObject.TASK, ajaxClient2.getValues().getPrivateTaskFolder(), ajaxClient2.getValues().getUserId(), userId);
        ftm2.insertFoldersOnServer(new FolderObject[] { sharedContactFolder, sharedAppointmentFolder, sharedTaskFolder });
    }

    @Test
    public void testFolderTreeNavigation() throws Exception {
        // expect root folders
        Set<Integer> expectedFolderIds = new HashSet<>();
        expectedFolderIds.add(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        expectedFolderIds.add(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        expectedFolderIds.add(FolderObject.SYSTEM_SHARED_FOLDER_ID);

        Set<Integer> rootFolderIds = collectFolderIds(new RootRequest(EnumAPI.OX_NEW));
        Assert.assertTrue(rootFolderIds.containsAll(expectedFolderIds));
        Assert.assertFalse(rootFolderIds.contains(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID));

        // expect private folders
        expectedFolderIds.clear();
        expectedFolderIds.add(privateContactFolderId);
        expectedFolderIds.add(privateAppointmentFolderId);
        expectedFolderIds.add(privateTaskFolderId);

        List<FolderObject> privateFolders = listFolders(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
        Set<Integer> privateFolderIds = collectFolderIds(privateFolders);
        Assert.assertTrue(privateFolderIds.containsAll(expectedFolderIds));
        for (FolderObject folder : privateFolders) {
            expectedFolderIds.clear();
            AJAXRequest<ListResponse> request = null;
            int objectID = folder.getObjectID();
            if (objectID == privateContactFolderId) {
                request = new ListRequest(EnumAPI.OX_NEW, privateContactFolderId);
                expectedFolderIds.add(privateContactFolder.getObjectID());
            } else if (objectID == privateAppointmentFolderId) {
                request = new ListRequest(EnumAPI.OX_NEW, privateAppointmentFolderId);
                expectedFolderIds.add(privateCalendarFolder.getObjectID());
            } else if (objectID == privateTaskFolderId) {
                request = new ListRequest(EnumAPI.OX_NEW, privateTaskFolderId);
                expectedFolderIds.add(privateTaskFolder.getObjectID());
            }

            if (request != null) {
                Assert.assertTrue(collectFolderIds(request).containsAll(expectedFolderIds));
            }
        }

        // expect public folders
        expectedFolderIds.clear();
        expectedFolderIds.add(publicContactFolder.getObjectID());
        expectedFolderIds.add(publicCalendarFolder.getObjectID());
        expectedFolderIds.add(publicTaskFolder.getObjectID());
        Assert.assertTrue(collectFolderIds(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_PUBLIC_FOLDER_ID)).containsAll(expectedFolderIds));

        // expect shared folders
        List<FolderObject> sharedFolders = listFolders(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_SHARED_FOLDER_ID));
        FolderObject client2Folder = null;
        for (FolderObject folder : sharedFolders) {
            if (folder.getFullName().equals("u:" + ajaxClient2.getValues().getUserId())) {
                client2Folder = folder;
                break;
            }
        }
        Assert.assertNotNull(client2Folder);
        expectedFolderIds.clear();
        expectedFolderIds.add(sharedContactFolder.getObjectID());
        expectedFolderIds.add(sharedAppointmentFolder.getObjectID());
        expectedFolderIds.add(sharedTaskFolder.getObjectID());
        Assert.assertTrue(collectFolderIds(new ListRequest(EnumAPI.OX_NEW, "u:" + ajaxClient2.getValues().getUserId())).containsAll(expectedFolderIds));
    }

    private List<FolderObject> listFolders(AJAXRequest<ListResponse> request) throws OXException, IOException, JSONException {
        ListResponse response = client.execute(request);
        Assert.assertFalse(response.hasError());
        Assert.assertFalse(response.hasWarnings());

        List<FolderObject> folders = new ArrayList<>();
        Iterator<FolderObject> it = response.getFolder();
        while (it.hasNext()) {
            folders.add(it.next());
        }

        return folders;
    }

    private Set<Integer> collectFolderIds(List<FolderObject> folders) {
        Iterator<FolderObject> it = folders.iterator();
        Set<Integer> folderIds = new HashSet<>();
        while (it.hasNext()) {
            folderIds.add(it.next().getObjectID());
        }
        return folderIds;
    }

    private Set<Integer> collectFolderIds(AJAXRequest<ListResponse> request) throws OXException, IOException, JSONException {
        List<FolderObject> folders = listFolders(request);
        return collectFolderIds(folders);
    }

    @After
    public void tearDown() throws Exception {
        if (ftm != null) {
            ftm.cleanUp();
        }

        if (ftm2 != null) {
            ftm2.cleanUp();
        }

        if (ajaxClient2 != null) {
            ajaxClient2.logout();
        }
    }

}

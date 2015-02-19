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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.RootRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.test.FolderTestManager;


/**
 * {@link FolderTreeTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@RunWith(Parameterized.class)
public class FolderTreeTest extends AbstractOAuthTest {

    private FolderTestManager ftm;

    private UserValues values;

    private AJAXClient ajaxClient2;

    private FolderTestManager ftm2;

    private final String scope;

    private final ContentType contentType;

    private FolderObject privateSubfolder;

    private FolderObject publicSubfolder;

    private FolderObject sharedSubfolder;

    @Parameters(name = "{1}")
    public static Collection<Object[]> generateData() {
        return Arrays.asList(new Object[][] {
            { ContactActionFactory.OAUTH_READ_SCOPE, ContactContentType.getInstance() },
            { AppointmentActionFactory.OAUTH_READ_SCOPE, CalendarContentType.getInstance() },
            { TaskActionFactory.OAUTH_READ_SCOPE, TaskContentType.getInstance() }
        });
    }

    public FolderTreeTest(String scope, ContentType contentType) throws OXException {
        super();
        this.scope = scope;
        this.contentType = contentType;
    }

    private int moduleId() {
        if (contentType == ContactContentType.getInstance()) {
            return FolderObject.CONTACT;
        } else if (contentType == CalendarContentType.getInstance()) {
            return FolderObject.CALENDAR;
        } else if (contentType == TaskContentType.getInstance()) {
            return FolderObject.TASK;
        }
        return -1;
    }

    private int privateFolderId() throws OXException, IOException, JSONException {
        return privateFolderId(ajaxClient);
    }

    private int privateFolderId(AJAXClient client) throws OXException, IOException, JSONException {
        if (contentType == ContactContentType.getInstance()) {
            return client.getValues().getPrivateContactFolder();
        } else if (contentType == CalendarContentType.getInstance()) {
            return client.getValues().getPrivateAppointmentFolder();
        } else if (contentType == TaskContentType.getInstance()) {
            return client.getValues().getPrivateTaskFolder();
        }

        return -1;
    }

    @Before
    public void setUp() throws Exception {
        ftm = new FolderTestManager(ajaxClient);
        values = ajaxClient.getValues();
        int userId = values.getUserId();

        privateSubfolder = ftm.generatePrivateFolder("oauth provider folder tree test - private " + contentType.toString() + " " + System.currentTimeMillis(), moduleId(), privateFolderId(), userId);
        publicSubfolder = ftm.generatePublicFolder("oauth provider folder tree test - public " + contentType.toString() + " " + System.currentTimeMillis(), moduleId(), FolderObject.SYSTEM_PUBLIC_FOLDER_ID, userId);
        ftm.insertFoldersOnServer(new FolderObject[] { privateSubfolder, publicSubfolder });

        // prepare shared folders
        ajaxClient2 = new AJAXClient(User.User2);
        ftm2 = new FolderTestManager(ajaxClient2);
        sharedSubfolder = ftm2.generateSharedFolder("oauth provider folder tree test - shared " + contentType.toString() + " "  + System.currentTimeMillis(), moduleId(), privateFolderId(ajaxClient2), ajaxClient2.getValues().getUserId(), userId);
        ftm2.insertFoldersOnServer(new FolderObject[] { sharedSubfolder });

        client.logout();
        client = new OAuthClient(clientApp.getId(), clientApp.getSecret(), clientApp.getRedirectURIs().get(0), scope);
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

        List<FolderObject> privateFolders = listFolders(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
        assertContentType(privateFolders);
        Assert.assertTrue(collectFolderIds(privateFolders).contains(privateFolderId()));

        List<FolderObject> privateSubFolders = listFolders(new ListRequest(EnumAPI.OX_NEW, privateFolderId()));
        assertContentType(privateSubFolders);
        Assert.assertTrue(collectFolderIds(privateSubFolders).contains(privateSubfolder.getObjectID()));

        // expect public folders
        List<FolderObject> publicSubFolders = listFolders(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_PUBLIC_FOLDER_ID));
        assertContentType(publicSubFolders);
        Assert.assertTrue(collectFolderIds(publicSubFolders).contains(publicSubfolder.getObjectID()));

        // expect shared folders
        List<FolderObject> sharedFolders = listFolders(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_SHARED_FOLDER_ID));
        assertContentType(sharedFolders);
        FolderObject client2Folder = null;
        for (FolderObject folder : sharedFolders) {
            if (folder.getFullName().equals("u:" + ajaxClient2.getValues().getUserId())) {
                client2Folder = folder;
                break;
            }
        }
        Assert.assertNotNull(client2Folder);
        Assert.assertTrue(collectFolderIds(new ListRequest(EnumAPI.OX_NEW, "u:" + ajaxClient2.getValues().getUserId())).contains(sharedSubfolder.getObjectID()));
    }

    private void assertContentType(List<FolderObject> folders) {
        Iterator<FolderObject> it = folders.iterator();
        while (it.hasNext()) {
            FolderObject folder = it.next();
            Assert.assertTrue(moduleId() == folder.getModule() || FolderObject.SYSTEM_MODULE == folder.getModule());
        }
    }

    @Test
    public void testAllVisibleFolders() throws Exception {
        Set<Integer> expectedFolderIds = new HashSet<>();
        VisibleFoldersResponse response = client.execute(new VisibleFoldersRequest(EnumAPI.OX_NEW, contentType.toString()));

        // private
        List<FolderObject> privateFolders = toList(response.getPrivateFolders());
        assertContentType(privateFolders);
        expectedFolderIds.add(privateFolderId());
        expectedFolderIds.add(privateSubfolder.getObjectID());
        Assert.assertTrue(collectFolderIds(privateFolders).containsAll(expectedFolderIds));

        // public
        List<FolderObject> publicFolders = toList(response.getPublicFolders());
        assertContentType(publicFolders);
        expectedFolderIds.clear();
        expectedFolderIds.add(publicSubfolder.getObjectID());
        Assert.assertTrue(collectFolderIds(publicFolders).containsAll(expectedFolderIds));

        // shared
        List<FolderObject> sharedFolders = toList(response.getSharedFolders());
        assertContentType(sharedFolders);
        expectedFolderIds.clear();
        expectedFolderIds.add(sharedSubfolder.getObjectID());
        Assert.assertTrue(collectFolderIds(sharedFolders).containsAll(expectedFolderIds));
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

    private static <T> List<T> toList(Iterator<T> iterator) {
        List<T> list = new LinkedList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }

        return list;
    }

    private static Set<Integer> collectFolderIds(List<FolderObject> folders) {
        Iterator<FolderObject> it = folders.iterator();
        return collectFolderIds(it);
    }

    private static Set<Integer> collectFolderIds(Iterator<FolderObject> it) {
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

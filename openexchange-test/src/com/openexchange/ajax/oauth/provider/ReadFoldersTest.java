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

package com.openexchange.ajax.oauth.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import com.openexchange.ajax.folder.actions.FolderUpdatesResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.PathRequest;
import com.openexchange.ajax.folder.actions.PathResponse;
import com.openexchange.ajax.folder.actions.RootRequest;
import com.openexchange.ajax.folder.actions.UpdatesRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.TimeZones;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.scope.Scope;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.test.FolderTestManager;


/**
 * {@link ReadFoldersTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@RunWith(Parameterized.class)
public class ReadFoldersTest extends AbstractOAuthTest {

    private FolderTestManager ftm;

    private UserValues values;

    private AJAXClient ajaxClient2;

    private FolderTestManager ftm2;

    private final ContentType contentType;

    private FolderObject privateSubfolder;

    private FolderObject publicSubfolder;

    private FolderObject sharedSubfolder;

    private int userId;

    private Set<Integer> groups;

    private static final Map<Scope, ContentType> S2CT = new HashMap<>();
    static {
        S2CT.put(Scope.newInstance(ContactActionFactory.OAUTH_READ_SCOPE), ContactContentType.getInstance());
        S2CT.put(Scope.newInstance(AppointmentActionFactory.OAUTH_READ_SCOPE), CalendarContentType.getInstance());
        S2CT.put(Scope.newInstance(TaskActionFactory.OAUTH_READ_SCOPE), TaskContentType.getInstance());
    }

    @Parameters(name = "{1}")
    public static Collection<Object[]> generateData() {
        List<Object[]> params = new ArrayList<>(S2CT.size());
        for (Scope scope : S2CT.keySet()) {
            params.add(new Object[] { scope, S2CT.get(scope) });
        }
        return params;
    }

    public ReadFoldersTest(Scope scope, ContentType contentType) throws OXException {
        super(scope);
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
        userId = values.getUserId();
        GetResponse getResponse = ajaxClient.execute(new GetRequest(userId, TimeZones.UTC));
        int[] userGroups = getResponse.getUser().getGroups();
        groups = new HashSet<>();
        groups.add(GroupStorage.GROUP_ZERO_IDENTIFIER);
        if (userGroups != null) {
            for (int g : userGroups) {
                groups.add(g);
            }
        }

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
        assertContentTypeAndPermissions(privateFolders);
        Assert.assertTrue(collectFolderIds(privateFolders).contains(privateFolderId()));

        List<FolderObject> privateSubFolders = listFolders(new ListRequest(EnumAPI.OX_NEW, privateFolderId()));
        assertContentTypeAndPermissions(privateSubFolders);
        Assert.assertTrue(collectFolderIds(privateSubFolders).contains(privateSubfolder.getObjectID()));

        // expect public folders
        List<FolderObject> publicSubFolders = listFolders(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_PUBLIC_FOLDER_ID));
        assertContentTypeAndPermissions(publicSubFolders);
        Assert.assertTrue(collectFolderIds(publicSubFolders).contains(publicSubfolder.getObjectID()));

        // expect shared folders
        List<FolderObject> sharedFolders = listFolders(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_SHARED_FOLDER_ID));
        assertContentTypeAndPermissions(sharedFolders);
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

    @Test
    public void testAllVisibleFolders() throws Exception {
        Set<Integer> expectedFolderIds = new HashSet<>();
        VisibleFoldersResponse response = client.execute(new VisibleFoldersRequest(EnumAPI.OX_NEW, contentType.toString()));
        assertNoErrorsAndWarnings(response);

        // private
        List<FolderObject> privateFolders = toList(response.getPrivateFolders());
        assertContentTypeAndPermissions(privateFolders);
        expectedFolderIds.add(privateFolderId());
        expectedFolderIds.add(privateSubfolder.getObjectID());
        Assert.assertTrue(collectFolderIds(privateFolders).containsAll(expectedFolderIds));

        // public
        List<FolderObject> publicFolders = toList(response.getPublicFolders());
        assertContentTypeAndPermissions(publicFolders);
        expectedFolderIds.clear();
        expectedFolderIds.add(publicSubfolder.getObjectID());
        Assert.assertTrue(collectFolderIds(publicFolders).containsAll(expectedFolderIds));

        // shared
        List<FolderObject> sharedFolders = toList(response.getSharedFolders());
        assertContentTypeAndPermissions(sharedFolders);
        expectedFolderIds.clear();
        expectedFolderIds.add(sharedSubfolder.getObjectID());
        Assert.assertTrue(collectFolderIds(sharedFolders).containsAll(expectedFolderIds));
    }

    @Test
    public void testUpdates() throws Exception {
        FolderUpdatesResponse updatesResponse = client.execute(new UpdatesRequest(EnumAPI.OX_NEW, ListRequest.DEFAULT_COLUMNS, -1, null, new Date(privateSubfolder.getLastModified().getTime() - 1000), Ignore.NONE));
        assertNoErrorsAndWarnings(updatesResponse);
        List<FolderObject> folders = updatesResponse.getFolders();
        assertContentTypeAndPermissions(folders);
        Set<Integer> expectedFolderIds = new HashSet<>();
        expectedFolderIds.add(privateSubfolder.getObjectID());
        expectedFolderIds.add(publicSubfolder.getObjectID());
        expectedFolderIds.add(sharedSubfolder.getObjectID());
        Assert.assertTrue(collectFolderIds(folders).containsAll(expectedFolderIds));
    }

    @Test
    public void testPath() throws Exception {
        PathResponse pathResponse = client.execute(new PathRequest(EnumAPI.OX_NEW, Integer.toString(privateSubfolder.getObjectID())));
        assertNoErrorsAndWarnings(pathResponse);
        List<FolderObject> folders = toList(pathResponse.getFolder());
        assertContentTypeAndPermissions(folders);
        Set<Integer> expectedFolderIds = new HashSet<>();
        expectedFolderIds.add(privateSubfolder.getObjectID());
        expectedFolderIds.add(privateFolderId());
        expectedFolderIds.add(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        Set<Integer> collectFolderIds = collectFolderIds(folders);
        Assert.assertTrue(collectFolderIds.containsAll(expectedFolderIds));
        collectFolderIds.removeAll(expectedFolderIds);
        Assert.assertTrue(collectFolderIds.isEmpty());
    }

    @Test
    public void testGet() throws Exception {
        Set<Integer> folderIds = new HashSet<>();
        folderIds.add(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        folderIds.add(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        folderIds.add(FolderObject.SYSTEM_SHARED_FOLDER_ID);
        folderIds.add(privateFolderId());
        folderIds.add(privateSubfolder.getObjectID());
        folderIds.add(publicSubfolder.getObjectID());
        folderIds.add(sharedSubfolder.getObjectID());

        for (int folderId : folderIds) {
            com.openexchange.ajax.folder.actions.GetResponse response = client.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, folderId));
            assertNoErrorsAndWarnings(response);
            assertContentTypeAndPermissions(response.getFolder());
        }
    }

    @Test
    public void testInsufficientScopeOnAllVisibleFolders() throws Exception {
        HashSet<Scope> invalidScopes = new HashSet<>(S2CT.keySet());
        invalidScopes.remove(scope);
        for (Scope invalidScope : invalidScopes) {
            ContentType invalidContentType = S2CT.get(invalidScope);
            VisibleFoldersResponse response = client.execute(new VisibleFoldersRequest(EnumAPI.OX_NEW, invalidContentType.toString(), VisibleFoldersRequest.DEFAULT_COLUMNS, false));
            assertFolderNotVisibleError(response, invalidScope);
        }
    }

    @Test
    public void testInsufficientScopeOnGet() throws Exception {
        HashSet<Scope> invalidScopes = new HashSet<>(S2CT.keySet());
        invalidScopes.remove(scope);
        for (Scope invalidScope : invalidScopes) {
            ContentType invalidContentType = S2CT.get(invalidScope);
            // get folders via ajax client and verify that every single get-request for those folders fails
            // for the according OAuth client because of insufficient scope
            VisibleFoldersResponse allResponse = ajaxClient.execute(new VisibleFoldersRequest(EnumAPI.OX_NEW, invalidContentType.toString(), VisibleFoldersRequest.DEFAULT_COLUMNS, false));
            assertNoErrorsAndWarnings(allResponse);
            List<FolderObject> allFolders = new LinkedList<>();
            allFolders.addAll(toList(allResponse.getPrivateFolders()));
            allFolders.addAll(toList(allResponse.getPublicFolders()));
            allFolders.addAll(toList(allResponse.getSharedFolders()));
            for (FolderObject folder : allFolders) {
                com.openexchange.ajax.folder.actions.GetResponse response;
                if (folder.getObjectID() < 0) {
                    response = client.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, folder.getFullName(), false));
                } else {
                    response = client.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, folder.getObjectID(), false));
                }

                assertFolderNotVisibleError(response, invalidScope);
            }
        }
    }

    private void assertFolderNotVisibleError(AbstractAJAXResponse response, Scope requiredScope) {
        OXException e = response.getException();
        Assert.assertTrue(e instanceof OAuthInsufficientScopeException || FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.equals(e));
        if (e instanceof OAuthInsufficientScopeException) {
            Assert.assertEquals(requiredScope.toString(), ((OAuthInsufficientScopeException)e).getScope());
        }
    }

    private void assertContentTypeAndPermissions(List<FolderObject> folders) {
        Iterator<FolderObject> it = folders.iterator();
        while (it.hasNext()) {
            FolderObject folder = it.next();
            assertContentTypeAndPermissions(folder);
        }
    }

    private void assertContentTypeAndPermissions(FolderObject folder) {
        Assert.assertTrue(moduleId() == folder.getModule() || FolderObject.SYSTEM_MODULE == folder.getModule());
        boolean canRead = false;
        for (OCLPermission p : folder.getPermissions()) {
            if (p.getEntity() == userId || (p.isGroupPermission() && groups.contains(p.getEntity()))) {
                canRead = p.isFolderVisible();
                break;
            }
        }
        Assert.assertTrue("Request returned folder " + folder.toString() + " but folder must not be visible", canRead);
    }

    private void assertNoErrorsAndWarnings(AbstractAJAXResponse response) {
        Assert.assertFalse(response.hasError());
        Assert.assertFalse(response.hasWarnings());
    }

    private List<FolderObject> listFolders(AJAXRequest<ListResponse> request) throws OXException, IOException, JSONException {
        ListResponse response = client.execute(request);
        assertNoErrorsAndWarnings(response);
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

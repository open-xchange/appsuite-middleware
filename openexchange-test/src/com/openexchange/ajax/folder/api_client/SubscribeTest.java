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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.folder.api_client;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.FoldersApi;

/**
 * {@link SubscribeTest} contains tests which tests to subscribe and unsubscribe task/contact/calendar folders
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
@RunWith(Parameterized.class)
public class SubscribeTest extends AbstractConfigAwareAPIClientSession {

    private static final String TREE = "0";

    private FoldersApi foldersApi;
    private String defaultFolder;
    private HashSet<String> toDelete;

    @Parameter(value = 0)
    public String module;

    @Parameters(name = "module={0}")
    public static Iterable<Object[]> params() {
        List<Object[]> timeZones = new ArrayList<>(3);
        timeZones.add(new Object[] { "contacts" });
        timeZones.add(new Object[] { "event" });
        timeZones.add(new Object[] { "tasks"});
        return timeZones;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        foldersApi = new FoldersApi(apiClient);
        defaultFolder = getDefaultFolder(getSessionId(), foldersApi);
        toDelete = new HashSet<>();
    }

    /**
     * Retrieves the default calendar folder of the user with the specified session
     *
     * @param session The session of the user
     * @param foldersApi The {@link FoldersApi}
     * @return The default calendar folder of the user
     * @throws Exception if the default calendar folder cannot be found
     */
    protected String getDefaultFolder(String session, FoldersApi foldersApi) throws Exception {
        ArrayList<ArrayList<?>> privateList = getPrivateFolderList(foldersApi, session, module, "1,308", TREE);
        if (privateList.size() == 1) {
            return (String) privateList.get(0).get(0);
        }
        for (ArrayList<?> folder : privateList) {
            if (folder.get(1) != null && ((Boolean) folder.get(1)).booleanValue()) {
                return (String) folder.get(0);
            }
        }
        throw new Exception("Unable to find default folder!");
    }

    /**
     * @param api The {@link FoldersApi} to use
     * @param session The session of the user
     * @param module The folder module
     * @param columns The columns identifier
     * @param tree The folder tree identifier
     * @return List of available folders
     * @throws Exception if the api call fails
     */
    @SuppressWarnings({ "unchecked" })
    protected ArrayList<ArrayList<?>> getPrivateFolderList(FoldersApi foldersApi, String session, String module, String columns, String tree) throws Exception {
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders(session, module, columns, tree, null, Boolean.TRUE);
        if (visibleFolders.getError() != null) {
            throw new OXException(new Exception(visibleFolders.getErrorDesc()));
        }
        Object privateFolders = visibleFolders.getData().getPrivate();
        return (ArrayList<ArrayList<?>>) privateFolders;
    }

    @Test
    public void checkDefaultFolder() throws ApiException {
        FolderData root = checkResponse(foldersApi.getFolder(getSessionId(), defaultFolder, TREE, module, null));
        assertTrue(root.getStandardFolder().booleanValue());

        FolderBody body = new FolderBody();
        FolderData update = new FolderData();
        update.setId(defaultFolder);
        update.setSubscribed(FALSE);
        body.setFolder(update);
        FolderUpdateResponse resp = foldersApi.updateFolder(getSessionId(), defaultFolder, body, FALSE, getLastTimeStamp(), TREE, module, FALSE, null, null);
        assertNotNull(resp.getError());
        assertEquals("FLD-1044", resp.getCode());
    }

    @Test
    public void checkRoundtrip() throws ApiException {
        // 1. Create new folder
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder data = new NewFolderBodyFolder();
        String title = this.getClass().getSimpleName()+"_"+System.currentTimeMillis();
        data.setTitle(title);
        data.setSubscribed(TRUE);
        data.setModule(module);

        FolderPermission perm = new FolderPermission();
        perm.entity(I(getUserId()));
        perm.setGroup(Boolean.FALSE);
        perm.setBits(I(403710016));
        List<FolderPermission> perms = Collections.singletonList(perm);
        data.setPermissions(perms);

        body.setFolder(data);
        FolderUpdateResponse resp = foldersApi.createFolder(defaultFolder, getSessionId(), body, TREE, module, null, null);
        assertNull(resp.getError());
        assertNotNull(resp.getData());
        String newFolder = rememberFolder(resp.getData());

        // 2. Check is subscribed
        FolderData fData = checkResponse(foldersApi.getFolder(getSessionId(), newFolder, TREE, module, null));
        assertTrue("Folder should be subscribed", fData.getSubscribed().booleanValue());

        // 3. Change subscription to false
        FolderBody updateBody = new FolderBody();
        FolderData updateData = new FolderData();
        updateData.setId(newFolder);
        updateData.setSubscribed(FALSE);
        updateData.setPermissions(perms);
        updateBody.setFolder(updateData);
        FolderUpdateResponse updateResponse = foldersApi.updateFolder(getSessionId(), newFolder, updateBody, FALSE, getLastTimeStamp(), TREE, module, FALSE, null, null);
        assertNull(updateResponse.getError());
        assertNotNull(updateResponse.getData());

        // 4. Check subscribed status again
        fData = checkResponse(foldersApi.getFolder(getSessionId(), newFolder, TREE, module, null));
        assertFalse("Folder shouldn't be subscribed anymore", fData.getSubscribed().booleanValue());

        // 5. Change subscription back to true
        updateBody = new FolderBody();
        updateData = new FolderData();
        updateData.setId(newFolder);
        updateData.setSubscribed(TRUE);
        updateData.setPermissions(perms);
        updateBody.setFolder(updateData);
        updateResponse = foldersApi.updateFolder(getSessionId(), newFolder, updateBody, FALSE, getLastTimeStamp(), TREE, module, FALSE, null, null);
        assertNull(updateResponse.getError());
        assertNotNull(updateResponse.getData());

        // 6. Check subscribed status again
        fData = checkResponse(foldersApi.getFolder(getSessionId(), newFolder, TREE, module, null));
        assertTrue("Folder should be subscribed again", fData.getSubscribed().booleanValue());
    }

    private String rememberFolder(String id) {
        toDelete.add(id);
        return id;
    }

    @Override
    public void tearDown() throws Exception {
        ArrayList<String> list = toDelete.stream().collect(Collectors.toCollection(ArrayList::new));
        foldersApi.deleteFolders(getSessionId(), list, TREE, getLastTimeStamp(), module, TRUE, FALSE, FALSE, null);
        super.tearDown();
    }

    private Long getLastTimeStamp() {
        return timestamp == null ? L(System.currentTimeMillis()) : timestamp;
    }

    private static Long timestamp;

    private FolderData checkResponse(FolderResponse resp) {
        assertNull(resp.getError());
        assertNotNull(resp.getData());
        timestamp = resp.getTimestamp();
        return resp.getData();
    }

}

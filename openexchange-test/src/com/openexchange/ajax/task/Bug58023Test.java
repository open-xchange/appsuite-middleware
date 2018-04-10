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

package com.openexchange.ajax.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Strings;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.models.TaskResponse;
import com.openexchange.testing.httpclient.models.TasksResponse;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.TasksApi;

/**
 * 
 * {@link Bug58023Test}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class Bug58023Test extends AbstractAPIClientSession {

    private TasksApi tasksApi;
    private FoldersApi foldersApi;
    private String privateTaskFolder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
//        apiClient.login(testUser.getLogin(), testUser.getPassword());
        tasksApi = new TasksApi(apiClient);
        foldersApi = new FoldersApi(apiClient);
        privateTaskFolder = foldersApi.createFolder(getPrivateTaskFolder(), apiClient.getSession(), generateFolderBody(), "1", Module.TASK.getName()).getData();
    }

    public String getPrivateTaskFolder() throws ApiException {
        ConfigApi configApi = new ConfigApi(apiClient);
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateTaskFolder.getPath(), apiClient.getSession());
        Object data = checkResponse(configNode);
        return String.valueOf(data);
    }

    private Object checkResponse(ConfigResponse resp) {
        Assert.assertNull(resp.getErrorDesc(), resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

    private NewFolderBody generateFolderBody() {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setTitle(this.getClass().getSimpleName() + "_" + UUID.randomUUID().toString());
        folder.setModule(Module.TASK.getName());
        addPermissions(folder);
        body.setFolder(folder);
        return body;
    }

    private void addPermissions(NewFolderBodyFolder folder) {
        FolderPermission perm = new FolderPermission();
        perm.setEntity(apiClient.getUserId());
        perm.setGroup(Boolean.FALSE);
        perm.setBits(403710016);

        List<FolderPermission> permissions = new ArrayList<>();
        permissions.add(perm);

        folder.setPermissions(permissions);
    }

    @Test
    public void testGetAllTasksInFolder() throws Exception {
        ApiClient apiClient2 = generateApiClient(testUser2);
        rememberClient(apiClient2);
        TasksApi tasksApi2 = new TasksApi(apiClient2);

        TasksResponse allTasks = tasksApi2.getAllTasks(apiClient2.getSession(), privateTaskFolder, Strings.concat(",", Strings.convert(Task.ALL_COLUMNS)), Integer.toString(CalendarObject.START_DATE), "asc");

        assertEquals("Folder cannot be found.", allTasks.getError());
        assertEquals("TSK-0006", allTasks.getCode());
        assertTrue(allTasks.getErrorDesc().startsWith("Folder " + privateTaskFolder));

        allTasks = tasksApi.getAllTasks(apiClient.getSession(), privateTaskFolder, Strings.concat(",", Strings.convert(Task.ALL_COLUMNS)), Integer.toString(CalendarObject.START_DATE), "asc");
        @SuppressWarnings("unchecked") ArrayList<ArrayList<Object>> allTasksArray = (ArrayList<ArrayList<Object>>) allTasks.getData();
        assertEquals(0, allTasksArray.size());
    }

    @Test
    public void testGetSingleTasksInFolder() throws Exception {
        ApiClient apiClient2 = generateApiClient(testUser2);
        rememberClient(apiClient2);
        TasksApi tasksApi2 = new TasksApi(apiClient2);

        TaskResponse task = tasksApi2.getTask(apiClient2.getSession(), "333", privateTaskFolder);

        assertEquals("Folder cannot be found.", task.getError());
        assertEquals("TSK-0006", task.getCode());
        assertTrue(task.getErrorDesc().startsWith("Folder " + privateTaskFolder));

        task = tasksApi.getTask(apiClient.getSession(), "333", privateTaskFolder);
        assertTrue(Strings.isNotEmpty(task.getError())); // task does not exist
    }

}

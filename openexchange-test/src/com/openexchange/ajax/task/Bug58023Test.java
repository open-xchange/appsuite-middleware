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

package com.openexchange.ajax.task;

import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.test.common.test.TestClassConfig;
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
        tasksApi = new TasksApi(getApiClient());
        foldersApi = new FoldersApi(getApiClient());
        privateTaskFolder = foldersApi.createFolder(getPrivateTaskFolder(), generateFolderBody(), "1", Module.TASK.getName(), null, null).getData();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(2).build();
    }

    public String getPrivateTaskFolder() throws ApiException {
        ConfigApi configApi = new ConfigApi(getApiClient());
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateTaskFolder.getPath());
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
        perm.setEntity(I(testUser.getUserId()));
        perm.setGroup(Boolean.FALSE);
        perm.setBits(I(403710016));

        List<FolderPermission> permissions = new ArrayList<>();
        permissions.add(perm);

        folder.setPermissions(permissions);
    }

    @Test
    public void testGetAllTasksInFolder() throws Exception {
        ApiClient apiClient2 = testUser2.getApiClient();
        TasksApi tasksApi2 = new TasksApi(apiClient2);

        TasksResponse allTasks = tasksApi2.getAllTasks(privateTaskFolder, Strings.concat(",", Strings.convert(Task.ALL_COLUMNS)), Integer.toString(CalendarObject.START_DATE), "asc");

        assertEquals("Folder cannot be found.", allTasks.getError());
        assertEquals("TSK-0006", allTasks.getCode());
        assertTrue(allTasks.getErrorDesc().startsWith("Folder " + privateTaskFolder));

        allTasks = tasksApi.getAllTasks(privateTaskFolder, Strings.concat(",", Strings.convert(Task.ALL_COLUMNS)), Integer.toString(CalendarObject.START_DATE), "asc");
        @SuppressWarnings("unchecked") ArrayList<ArrayList<Object>> allTasksArray = (ArrayList<ArrayList<Object>>) allTasks.getData();
        assertEquals(0, allTasksArray.size());
    }

    @Test
    public void testGetSingleTasksInFolder() throws Exception {
        TasksApi tasksApi2 = new TasksApi(testUser2.getApiClient());

        TaskResponse task = tasksApi2.getTask("333", privateTaskFolder);

        assertEquals("Folder cannot be found.", task.getError());
        assertEquals("TSK-0006", task.getCode());
        assertTrue(task.getErrorDesc().startsWith("Folder " + privateTaskFolder));

        task = tasksApi.getTask("333", privateTaskFolder);
        assertTrue(Strings.isNotEmpty(task.getError())); // task does not exist
    }

}

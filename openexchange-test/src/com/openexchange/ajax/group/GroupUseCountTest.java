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

package com.openexchange.ajax.group;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.GroupData;
import com.openexchange.testing.httpclient.models.GroupListElement;
import com.openexchange.testing.httpclient.models.GroupSearchBody;
import com.openexchange.testing.httpclient.models.GroupUpdateResponse;
import com.openexchange.testing.httpclient.models.GroupsResponse;
import com.openexchange.testing.httpclient.models.TaskData;
import com.openexchange.testing.httpclient.models.TaskListElement;
import com.openexchange.testing.httpclient.models.TaskParticipant;
import com.openexchange.testing.httpclient.models.TaskUpdateResponse;
import com.openexchange.testing.httpclient.modules.GroupsApi;
import com.openexchange.testing.httpclient.modules.TasksApi;

/**
 * {@link GroupUseCountTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class GroupUseCountTest extends AbstractChronosTest {

    private GroupsApi groupsApi;
    private Integer[] groupIds;
    private Long timestamp;
    private TasksApi taskApi;
    private Long taskTimestamp;
    private List<TaskListElement> tasksToDelete;

    /**
     * Initializes a new {@link GroupUseCountTest}.
     */
    public GroupUseCountTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        groupsApi = new GroupsApi(getApiClient());
        GroupData groupData = new GroupData();
        groupData.setDisplayName("Test1");
        groupData.setName("Test1");
        GroupUpdateResponse response = groupsApi.createGroup(getSessionId(), groupData);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        groupIds = new Integer[2];
        groupIds[0] = response.getData().getId();
        groupData = new GroupData();
        groupData.setDisplayName("Test2");
        groupData.setName("Test2");
        List<Integer> members = new ArrayList<>(2);
        members.add(3);
        members.add(4);
        groupData.setMembers(members);
        response = groupsApi.createGroup(getSessionId(), groupData);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        groupIds[1] = response.getData().getId();
        timestamp = response.getTimestamp();

        tasksToDelete = new ArrayList<TaskListElement>();
        taskApi = new TasksApi(getApiClient());
    }

    private synchronized void rememberTask(String folderId, String id, Long timestamp) {
        TaskListElement element = new TaskListElement();
        element.setId(id);
        element.setFolder(folderId);
        this.tasksToDelete.add(element);
        this.taskTimestamp = timestamp;
    }

    @Override
    public void tearDown() throws Exception {
        GroupListElement body = new GroupListElement();
        if (groupIds != null) {
            for (Integer id : groupIds) {
                body.setId(id);
                groupsApi.deleteGroup(getSessionId(), timestamp, body);
            }
        }
        if(tasksToDelete != null && !tasksToDelete.isEmpty()) {
            taskApi.deleteTasks(getSessionId(), taskTimestamp, tasksToDelete);
        }
        super.tearDown();
    }

    @Test
    public void testUseCount() throws ApiException {
        GroupSearchBody body = new GroupSearchBody();
        body.setPattern("Test");
        GroupsResponse response = groupsApi.searchGroups(getSessionId(), body);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        List<GroupData> groups = response.getData();
        Assert.assertEquals(2, groups.size());
        int x = 0;
        // Check that groups are returned in the same order
        for (GroupData group : groups) {
            Assert.assertEquals(groupIds[x++], group.getId());
        }

        // use group 2
        EventData eventData = EventFactory.createSingleTwoHourEvent(defaultUserApi.getCalUser(), "testUseCount", folderId);
        Attendee att = new Attendee();
        att.setEntity(groupIds[1]);
        att.setCuType(CuTypeEnum.GROUP);
        eventData.addAttendeesItem(att);
        eventManager.createEvent(eventData, true);

        // Check order again
        body = new GroupSearchBody();
        body.setPattern("Test");
        response = groupsApi.searchGroups(getSessionId(), body);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        groups = response.getData();
        Assert.assertEquals(2, groups.size());
        x = 1;
        // Check that groups are returned in the inverse order now
        for (GroupData group : groups) {
            Assert.assertEquals(groupIds[x--], group.getId());
        }
    }

    @Test
    public void testUseCountWithTask() throws Exception {
        GroupSearchBody body = new GroupSearchBody();
        body.setPattern("Test");
        GroupsResponse response = groupsApi.searchGroups(getSessionId(), body);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        List<GroupData> groups = response.getData();
        Assert.assertEquals(2, groups.size());
        int x = 0;
        // Check that groups are returned in the same order
        for (GroupData group : groups) {
            Assert.assertEquals(groupIds[x++], group.getId());
        }

        // use group 2
        TaskData task = new TaskData();
        task.setTitle("testUseCountWithTask");
        task.setFolderId(getTaskFolderId());
        TaskParticipant participant = new TaskParticipant();
        participant.setId(groupIds[1]);
        participant.setType(2); // 2 == user group
        task.addParticipantsItem(participant);
        TaskUpdateResponse createTask = taskApi.createTask(getSessionId(), task);
        Assert.assertNull(createTask.getError(), createTask.getErrorDesc());
        rememberTask(task.getFolderId(), createTask.getData().getId(), createTask.getTimestamp());

        // Check order again
        body = new GroupSearchBody();
        body.setPattern("Test");
        response = groupsApi.searchGroups(getSessionId(), body);
        Assert.assertNull(response.getError(), response.getErrorDesc());
        groups = response.getData();
        Assert.assertEquals(2, groups.size());
        x = 1;
        // Check that groups are returned in the inverse order now
        for (GroupData group : groups) {
            Assert.assertEquals(groupIds[x--], group.getId());
        }
    }

    /**
     * @return
     * @throws Exception
     */
    private String getTaskFolderId() throws Exception {
        ArrayList<ArrayList<?>> privateList = getPrivateFolderList(foldersApi, getSessionId(), "tasks", "1,308", "0");
        if (privateList.size() == 1) {
            return (String) privateList.get(0).get(0);
        }
        for (ArrayList<?> folder : privateList) {
            if ((Boolean) folder.get(1)) {
                return (String) folder.get(0);
            }
        }
        throw new Exception("Unable to find default tasks folder!");
    }

}

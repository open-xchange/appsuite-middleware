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

import static org.junit.Assert.assertFalse;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug10400Test extends AbstractTaskTest {

    /**
     * Default constructor.
     *
     * @param name Name of the test.
     */
    public Bug10400Test() {
        super();
    }

    /**
     * Checks if the only task participant is able to remove himself and add the
     * creator as participant.
     *
     * @throws Throwable if an exception occurs.
     */
    @Test
    public void testRemoveDelegateAddCreator() throws Throwable {
        final AJAXClient anton = getClient();
        final int antonFID = anton.getValues().getPrivateTaskFolder();
        final AJAXClient berta = testUser2.getAjaxClient();
        final TimeZone bertaTZ = berta.getValues().getTimeZone();
        Task task = Create.createWithDefaults();
        task.setTitle("Bug10400Test1");
        task.setParentFolderID(antonFID);
        task.addParticipant(new UserParticipant(berta.getValues().getUserId()));
        final InsertResponse insert = anton.execute(new InsertRequest(task, anton.getValues().getTimeZone()));
        task.setLastModified(insert.getTimestamp());
        try {
            final GetResponse get = berta.execute(new GetRequest(berta.getValues().getPrivateTaskFolder(), insert.getId()));
            task = get.getTask(bertaTZ);
            task.setParticipants(new Participant[] { new UserParticipant(anton.getValues().getUserId()) });
            final UpdateResponse update = berta.execute(new UpdateRequest(task, bertaTZ));
            task.setLastModified(update.getTimestamp());
            assertFalse("Berta was not able to remove herself and add Anton as " + "task participant.", update.hasError());
        } finally {
            anton.execute(new DeleteRequest(antonFID, task.getObjectID(), task.getLastModified()));
        }
    }

    /**
     * Checks if the only participant is able to remove himself from the
     * participant list.
     *
     * @throws Throwable if an exception occurs.
     */
    @Test
    public void testCreatorAddAsParticipantAndRemove() throws Throwable {
        final AJAXClient anton = getClient();
        final int antonFID = anton.getValues().getPrivateTaskFolder();
        final TimeZone antonTZ = anton.getValues().getTimeZone();
        final Task task = Create.createWithDefaults();
        task.setTitle("Bug10400Test2");
        task.setParentFolderID(antonFID);
        final InsertResponse insert = anton.execute(new InsertRequest(task, antonTZ));
        task.setObjectID(insert.getId());
        task.setLastModified(insert.getTimestamp());
        try {
            task.setParticipants(new Participant[] { new UserParticipant(anton.getValues().getUserId()) });
            task.removeTitle();
            UpdateResponse update = anton.execute(new UpdateRequest(task, antonTZ));
            task.setLastModified(update.getTimestamp());
            task.setParticipants(new Participant[] {});
            update = anton.execute(new UpdateRequest(task, antonTZ));
            task.setLastModified(update.getTimestamp());
            final GetResponse get = anton.execute(new GetRequest(antonFID, task.getObjectID()));
            assertFalse("Task disappeared due to deleted folder mapping.", get.hasError());
        } finally {
            anton.execute(new DeleteRequest(antonFID, task.getObjectID(), task.getLastModified()));
        }
    }

    @Test
    public void testRemoveDelegate() throws Throwable {
        final AJAXClient anton = getClient();
        final int antonFID = anton.getValues().getPrivateTaskFolder();
        final AJAXClient berta = testUser2.getAjaxClient();
        final TimeZone bertaTZ = berta.getValues().getTimeZone();
        Task task = Create.createWithDefaults();
        task.setTitle("Bug10400Test2");
        task.setParentFolderID(antonFID);
        task.addParticipant(new UserParticipant(berta.getValues().getUserId()));
        final InsertResponse insert = anton.execute(new InsertRequest(task, anton.getValues().getTimeZone()));
        task.setLastModified(insert.getTimestamp());
        try {
            final GetResponse get = berta.execute(new GetRequest(berta.getValues().getPrivateTaskFolder(), insert.getId()));
            task = get.getTask(bertaTZ);
            task.setParticipants(new Participant[] {});
            final UpdateResponse update = berta.execute(new UpdateRequest(task, bertaTZ));
            task.setLastModified(update.getTimestamp());
            assertFalse("Berta was not able to remove herself.", update.hasError());
        } finally {
            anton.execute(new DeleteRequest(antonFID, task.getObjectID(), task.getLastModified()));
        }
    }
}

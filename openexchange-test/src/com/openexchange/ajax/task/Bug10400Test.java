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

import java.util.TimeZone;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug10400Test extends AbstractTaskTest {

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public Bug10400Test(final String name) {
        super(name);
    }

    /**
     * Checks if the only task participant is able to remove himself and add the
     * creator as participant.
     * @throws Throwable if an exception occurs.
     */
    public void testRemoveDelegateAddCreator() throws Throwable {
        final AJAXClient anton = getClient();
        final int antonFID = anton.getValues().getPrivateTaskFolder();
        final AJAXClient berta = new AJAXClient(User.User2);
        final TimeZone bertaTZ = berta.getValues().getTimeZone();
        Task task = Create.createWithDefaults();
        task.setTitle("Bug10400Test1");
        task.setParentFolderID(antonFID);
        task.addParticipant(new UserParticipant(berta.getValues().getUserId()));
        final InsertResponse insert = anton.execute(new InsertRequest(task, anton.getValues().getTimeZone()));
        task.setLastModified(insert.getTimestamp());
        try {
            final GetResponse get = TaskTools.get(berta, new GetRequest(berta
                .getValues().getPrivateTaskFolder(), insert.getId()));
            task = get.getTask(bertaTZ);
            task.setParticipants(new Participant[] { new UserParticipant(anton
                .getValues().getUserId()) });
            final UpdateResponse update = TaskTools.update(berta,
                new UpdateRequest(task, bertaTZ));
            task.setLastModified(update.getTimestamp());
            assertFalse("Berta was not able to remove herself and add Anton as "
                + "task participant.", update.hasError());
        } finally {
            anton.execute(new DeleteRequest(antonFID, task.getObjectID(), task.getLastModified()));
        }
    }

    /**
     * Checks if the only participant is able to remove himself from the
     * participant list.
     * @throws Throwable if an exception occurs.
     */
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
            task.setParticipants(new Participant[] { new UserParticipant(anton
                .getValues().getUserId()) });
            task.removeTitle();
            UpdateResponse update = TaskTools.update(anton, new UpdateRequest(
                task, antonTZ));
            task.setLastModified(update.getTimestamp());
            task.setParticipants(new Participant[] { });
            update = TaskTools.update(anton, new UpdateRequest(
                task, antonTZ));
            task.setLastModified(update.getTimestamp());
            final GetResponse get = TaskTools.get(anton, new GetRequest(antonFID,
                task.getObjectID()));
            assertFalse("Task disappeared due to deleted folder mapping.",
                get.hasError());
        } finally {
            anton.execute(new DeleteRequest(antonFID, task.getObjectID(), task.getLastModified()));
        }
    }

    public void testRemoveDelegate() throws Throwable {
        final AJAXClient anton = getClient();
        final int antonFID = anton.getValues().getPrivateTaskFolder();
        final AJAXClient berta = new AJAXClient(User.User2);
        final TimeZone bertaTZ = berta.getValues().getTimeZone();
        Task task = Create.createWithDefaults();
        task.setTitle("Bug10400Test2");
        task.setParentFolderID(antonFID);
        task.addParticipant(new UserParticipant(berta.getValues().getUserId()));
        final InsertResponse insert = anton.execute(new InsertRequest(task, anton.getValues().getTimeZone()));
        task.setLastModified(insert.getTimestamp());
        try {
            final GetResponse get = TaskTools.get(berta, new GetRequest(berta
                .getValues().getPrivateTaskFolder(), insert.getId()));
            task = get.getTask(bertaTZ);
            task.setParticipants(new Participant[] { });
            final UpdateResponse update = TaskTools.update(berta,
                new UpdateRequest(task, bertaTZ));
            task.setLastModified(update.getTimestamp());
            assertFalse("Berta was not able to remove herself.",
                update.hasError());
        } finally {
            anton.execute(new DeleteRequest(antonFID, task.getObjectID(), task.getLastModified()));
        }
    }
}

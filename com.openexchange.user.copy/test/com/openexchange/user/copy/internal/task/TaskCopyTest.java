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

package com.openexchange.user.copy.internal.task;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskIterator;
import com.openexchange.groupware.tasks.TaskStorage;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;
import com.openexchange.user.copy.internal.IntegerMapping;
import com.openexchange.user.copy.internal.tasks.TaskCopyTask;

/**
 * {@link TaskCopyTest}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class TaskCopyTest extends AbstractUserCopyTest {

    private Connection srcCon;

    private Connection dstCon;

    private Context srcCtx;

    private Context dstCtx;

    private int srcUser;

    private int dstUser;

    /**
     * Initializes a new {@link TaskCopyTest}.
     * 
     * @param name
     */
    public TaskCopyTest(final String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#getSequenceTables()
     */
    @Override
    protected String[] getSequenceTables() {
        return new String[] { "sequence_task" };
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        srcCon = getSourceConnection();
        dstCon = getDestinationConnection();
        srcCtx = getSourceContext();
        dstCtx = getDestinationContext();
        srcUser = getSourceUserId();
        dstUser = getDestinationUserId();
    }
    
    @Override
    protected void tearDown() throws Exception {
        DBUtils.autocommit(dstCon);
        deleteAllFromTablesForCid(dstCtx.getContextId(), "cid", dstCon, "task", "task_eparticipant", "task_folder", "task_participant", "task_removedparticipant");
        super.tearDown();
    }

    public void testTaskCopy() throws Exception {
        final List<Task> srcTasks = loadTasksFromDB(srcCtx, srcCon, srcUser);
        final TaskCopyTask task = new TaskCopyTask(userService);
        IntegerMapping mapping = null;
        try {
            disableForeignKeyChecks(dstCon);
            DBUtils.startTransaction(dstCon);
            mapping = task.copyUser(getObjectMappingWithFolders());
            enableForeignKeyChecks(dstCon);
            dstCon.commit();
        } catch (final OXException e) {
            dstCon.rollback();
            e.printStackTrace();
            fail("A UserCopyException occurred.");
        } finally {
            DBUtils.autocommit(dstCon);
        }
        final List<Task> target = loadTasksFromDB(dstCtx, dstCon, dstUser, 1337);
        checkTasks(srcTasks, target, mapping);
    }

    private List<Task> loadTasksFromDB(final Context ctx, final Connection con, final int userId, final int folder) throws Exception {
        final List<Task> srcTasks = new ArrayList<Task>();
        final TaskIterator taskIterator = TaskStorage.getInstance().list(
            ctx,
            folder,
            0,
            -1,
            0,
            Order.NO_ORDER,
            Task.ALL_COLUMNS,
            false,
            userId,
            false,
            con);
        while (taskIterator.hasNext()) {
            final Task task = taskIterator.next();
            srcTasks.add(task);
        }
        return srcTasks;
    }

    private List<Task> loadTasksFromDB(final Context ctx, final Connection con, final int userId) throws Exception {
        final List<Task> srcTasks = new ArrayList<Task>();
        final List<Integer> folders = loadFolderIdsFromDB(con, ctx.getContextId(), userId);
        for (final int folderId : folders) {
            final TaskIterator taskIterator = TaskStorage.getInstance().list(
                ctx,
                folderId,
                0,
                -1,
                0,
                Order.NO_ORDER,
                Task.ALL_COLUMNS,
                false,
                userId,
                false,
                con);
            while (taskIterator.hasNext()) {
                final Task task = taskIterator.next();
                srcTasks.add(task);
            }
        }
        return srcTasks;
    }

    private void checkTasks(final List<Task> orig, final List<Task> target, final IntegerMapping mapping) {
        for (final Task task : orig) {
            final int originalId = task.getObjectID();
            final Integer targetId = mapping.getDestination(originalId);
            if (targetId == null) {
                final int origMasterId = task.getRecurrenceID();
                if (origMasterId == -1 || origMasterId == originalId || mapping.getSourceKeys().contains(origMasterId)) {
                    fail("Mapping did not contain task");
                }
            } else {
                compareTasks(target, targetId, task, mapping);
            }
        }
    }

    private void compareTasks(final List<Task> target, final int targetId, final Task original, final IntegerMapping mapping) {
        Task targetTask = null;
        for (final Task t : target) {
            if (t.getObjectID() == targetId) {
                targetTask = t;
                break;
            }
        }
        if (targetTask == null) {
            fail("Did not find target for source " + original.getObjectID());
        }
        if (original.getRecurrenceID() != -1) {
            // checkReccurence(original, targetTask, mapping);
        }
        compareTasks(original, targetTask, mapping);
    }

    private void compareTasks(final Task original, final Task copied, final IntegerMapping mapping) {
        assertEquals(original.getActualCosts(), copied.getActualCosts());
        assertEquals(original.getActualDuration(), copied.getActualDuration());
        assertEquals(original.getAfterComplete(), copied.getAfterComplete());
        assertEquals(original.getAlarmFlag(), copied.getAlarmFlag());
        assertEquals(original.getAlarm(), copied.getAlarm());
        assertEquals(original.getBillingInformation(), copied.getBillingInformation());
        assertEquals(original.getCategories(), copied.getCategories());
        assertEquals(original.getCompanies(), copied.getCompanies());
        assertEquals(original.getConfirm(), copied.getConfirm());
        assertEquals(original.getConfirmMessage(), copied.getConfirmMessage());
        assertEquals(original.getCurrency(), copied.getCurrency());
        assertEquals(original.getChangeException(), copied.getChangeException());
        assertEquals(original.getConfirmations(), copied.getConfirmations());
        assertEquals(original.getDayInMonth(), copied.getDayInMonth());
        assertEquals(original.getDays(), copied.getDays());
        assertEquals(original.getDateCompleted(), copied.getDateCompleted());
        assertEquals(original.getDeleteException(), copied.getDeleteException());
        assertEquals(original.getEndDate(), copied.getEndDate());
        assertEquals(original.getInterval(), copied.getInterval());
        assertEquals(original.getLabel(), copied.getLabel());
        assertEquals(original.getLastModified(), copied.getLastModified());
        assertEquals(original.getLastModifiedOfNewestAttachment(), copied.getLastModifiedOfNewestAttachment());
        assertEquals(original.getMonth(), copied.getMonth());
        assertEquals(original.getNote(), copied.getNote());
        assertEquals(original.getNotification(), copied.getNotification());
        assertEquals(original.getNumberOfAttachments(), copied.getNumberOfAttachments());
        assertEquals(original.getNumberOfLinks(), copied.getNumberOfLinks());
        assertEquals(original.getOccurrence(), copied.getOccurrence());
        assertEquals(original.getOrganizer(), copied.getOrganizer());
        assertEquals(original.getPercentComplete(), copied.getPercentComplete());
        assertEquals(original.getPriority(), copied.getPriority());
        assertEquals(original.getPrivateFlag(), copied.getPrivateFlag());
        assertEquals(original.getSequence(), copied.getSequence());
        assertEquals(original.getStatus(), copied.getStatus());
        assertEquals(original.getStartDate(), copied.getStartDate());
        assertEquals(original.getTitle(), copied.getTitle());
        assertEquals(original.getTripMeter(), copied.getTripMeter());
        assertEquals(original.getTargetCosts(), copied.getTargetCosts());
        assertEquals(original.getTargetDuration(), copied.getTargetDuration());
        assertEquals(original.getUntil(), copied.getUntil());
        assertEquals(original.getUid(), copied.getUid());
        assertEquals(original.getFilename(), copied.getFilename());
        if (original.getParticipants() != null) {
            checkParticipants(Arrays.asList(original.getParticipants()), Arrays.asList(copied.getParticipants()), mapping);
        }
    }

    private void checkParticipants(final List<Participant> original, final List<Participant> copied, final IntegerMapping mapping) {
        if (original.size() != copied.size()) {
            fail("Participant count is not equal.");
        }
        for (int i = 0; i < original.size(); i++) {
            final Participant o = original.get(i);
            if (o.getType() == Participant.USER) {
                if (o.getIdentifier() == srcUser) {
                    boolean found = false;
                    for (int j = 0; j < copied.size(); j++) {
                        final Participant p = copied.get(j);
                        if (p.getType() == Participant.USER && p.getIdentifier() == dstUser) {
                            found = true;
                        }
                    }
                    if (!found) {
                        fail("User not found");
                    }
                } else {
                    try {
                        final User user = userService.getUser(o.getIdentifier(), srcCtx);
                        final ExternalUserParticipant extUser = new ExternalUserParticipant(user.getMail());
                        extUser.setDisplayName(user.getDisplayName());
                        if (!copied.contains(extUser)) {
                            fail("Internal participant missing");
                        }
                    } catch (final OXException e) {
                        fail("Failed to get UserService");
                    }
                }
            } else {
                if (!copied.contains(o)) {
                    fail("External participant missing");
                }
            }
        }
    }

}

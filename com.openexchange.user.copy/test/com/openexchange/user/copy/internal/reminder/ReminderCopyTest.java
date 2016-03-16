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

package com.openexchange.user.copy.internal.reminder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;
import com.openexchange.user.copy.internal.IntegerMapping;

/**
 * {@link ReminderCopyTest}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ReminderCopyTest extends AbstractUserCopyTest {

    private Connection srcCon;

    private Connection dstCon;

    private Context srcCtx;

    private Context dstCtx;

    private User srcUser;

    private int srcUserId;

    private int dstUserId;

    /**
     * Initializes a new {@link ReminderCopyTest}.
     * 
     * @param name
     */
    public ReminderCopyTest(final String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#getSequenceTables()
     */
    @Override
    protected String[] getSequenceTables() {
        return new String[] { "sequence_reminder" };
    }
    
    @Override
    public void tearDown() throws Exception {
        PreparedStatement stmt = null;
        try {
            stmt = srcCon.prepareStatement("DELETE FROM reminder WHERE cid = ? AND userid = ?");
            stmt.setInt(1, 999);
            stmt.setInt(2, 111);
            stmt.execute();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        srcCon = getSourceConnection();
        dstCon = getDestinationConnection();
        srcCtx = getSourceContext();
        dstCtx = getDestinationContext();
        srcUserId = getSourceUserId();
        dstUserId = getDestinationUserId();
        srcUser = userService.getUser(srcUserId, srcCtx);
    }

    public void testReminderCopy() throws Exception {
        final Map<String, ObjectMapping<?>> mapping = getBasicObjectMapping();
        fillObjectMappingAndGetReminderIds(srcCon, srcCtx.getContextId(), srcUserId, mapping);
        final Date end = new Date(Long.MAX_VALUE);
        final ReminderObject[] srcReminders = ReminderStorage.getInstance().selectReminder(srcCtx, srcCon, srcUser, end);
        final ReminderCopyTask reminderCopy = new ReminderCopyTask();

        IntegerMapping reminderMapping = null;
        try {
            disableForeignKeyChecks(dstCon);
            DBUtils.startTransaction(dstCon);
            reminderMapping = reminderCopy.copyUser(mapping);
            mapping.put(ReminderObject.class.getName(), reminderMapping);
            enableForeignKeyChecks(dstCon);
            dstCon.commit();
        } catch (final OXException e) {
            dstCon.rollback();
            e.printStackTrace();
            fail("A UserCopyException occurred.");
        } finally {
            DBUtils.autocommit(dstCon);
        }
        final ReminderObject[] copiedReminders = ReminderStorage.getInstance().selectReminder(dstCtx, dstCon, dstUserId, end);
        checkReminders(convertArrayToMap(srcReminders), convertArrayToMap(copiedReminders), mapping);
    }

    private void checkReminders(final Map<Integer, ReminderObject> srcReminders, final Map<Integer, ReminderObject> copiedReminders, final Map<String, ObjectMapping<?>> mapping) {
        if (srcReminders.size() != copiedReminders.size()) {
            fail("Map's size does not match");
        }
        final IntegerMapping appointmentMapping = (IntegerMapping) mapping.get(Appointment.class.getName());
        final IntegerMapping taskMapping = (IntegerMapping) mapping.get(Task.class.getName());
        final IntegerMapping reminderMapping = (IntegerMapping) mapping.get(ReminderObject.class.getName());
        for (final int copiedReminderId : copiedReminders.keySet()) {
            final ReminderObject copy = copiedReminders.get(copiedReminderId);
            int originalReminderId = 0;
            for (final int copy2 : reminderMapping.getSourceKeys()) {
                if (reminderMapping.getDestination(copy2) == copiedReminderId) {
                    originalReminderId = copy2;
                }
            }
            final ReminderObject original = srcReminders.get(originalReminderId);
            
            if (copy.getDescription() != null && original.getDescription() != null) {
                assertEquals("Description not equal", copy.getDescription(), original.getDescription());
            }
            assertEquals("Module not equal", copy.getModule(), original.getModule());
            assertEquals("Recurrence not equal", copy.getRecurrencePosition(), original.getRecurrencePosition());
            if (copy.getModule() == Types.TASK) {
                assertEquals("Task not equal", copy.getTargetId(), taskMapping.getSource(copy.getTargetId()).intValue());
            }
            if (copy.getModule() == Types.APPOINTMENT) {
                assertEquals("Appointment not equal", copy.getTargetId(), appointmentMapping.getSource(copy.getTargetId()).intValue());
            }
            assertEquals("Alarm not equal", copy.getDate(), original.getDate());
            assertEquals("Last modified not equal", copy.getLastModified(), original.getLastModified());
        }
    }
    
    private void fillObjectMappingAndGetReminderIds(final Connection con, final int cid, final int uid, final Map<String, ObjectMapping<?>> mapping) throws OXException {
        final IntegerMapping appointmentMapping = new IntegerMapping();
        final IntegerMapping taskMapping = new IntegerMapping();
        
        mapping.put(Appointment.class.getName(), appointmentMapping);
        mapping.put(Task.class.getName(), taskMapping);
        
        final Date end = new Date(Long.MAX_VALUE);
        final ReminderObject[] srcReminders = ReminderStorage.getInstance().selectReminder(srcCtx, srcCon, srcUser, end);
        for (final ReminderObject ro : srcReminders) {
            if (ro.getModule() == Types.APPOINTMENT) {
                appointmentMapping.addMapping(ro.getObjectId(), ro.getTargetId());
            }
            if (ro.getModule() == Types.TASK) {
                taskMapping.addMapping(ro.getObjectId(), ro.getTargetId());
            }
        }
    }
    
    private Map<Integer, ReminderObject> convertArrayToMap(final ReminderObject[] reminders) {
        final Map<Integer, ReminderObject> res = new HashMap<Integer, ReminderObject>();
        for (final ReminderObject r : reminders) {
            res.put(r.getObjectId(), r);
        }
        return res;
    }


}

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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mobilenotifier.reminder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.api2.ReminderService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.mobilenotifier.AbstractMobileNotifierService;
import com.openexchange.mobilenotifier.MobileNotifierExceptionCodes;
import com.openexchange.mobilenotifier.MobileNotifierProviders;
import com.openexchange.mobilenotifier.NotifyItem;
import com.openexchange.mobilenotifier.NotifyTemplate;
import com.openexchange.mobilenotifier.utility.DateUtility;
import com.openexchange.mobilenotifier.utility.MobileNotifierFileUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link MobileNotifierReminderImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobileNotifierReminderImpl extends AbstractMobileNotifierService {

    private final ServiceLookup services;

    public MobileNotifierReminderImpl(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public String getProviderName() {
        return MobileNotifierProviders.REMINDER.getProviderName();
    }

    @Override
    public String getFrontendName() {
        return MobileNotifierProviders.REMINDER.getFrontendName();
    }

    @Override
    public List<List<NotifyItem>> getItems(final Session session) throws OXException {
        Context cs = ContextStorage.getStorageContext(session.getContextId());
        User user = UserStorage.getInstance().getUser(session.getUserId(), session.getContextId());
        final Date currentDate = new Date(System.currentTimeMillis());
        final Date range = new Date(currentDate.getTime() + (60L * 60L * 1000L)); // one hour
        final ReminderService reminderSql = new ReminderHandler(cs);
        SearchIterator<ReminderObject> reminderObjects = reminderSql.getArisingReminder(session, cs, user, range);

        final List<List<NotifyItem>> items = new ArrayList<List<NotifyItem>>();

        while (reminderObjects.hasNext()) {
            ReminderObject ro = reminderObjects.next();
            final List<NotifyItem> notifyItem = new ArrayList<NotifyItem>();
            int module = ro.getModule();

            if (module == Types.APPOINTMENT) {
                final int folderId = ro.getFolder();
                final int objectId = ro.getTargetId();
                final AppointmentSqlFactoryService factory = services.getService(AppointmentSqlFactoryService.class);
                final CalendarCollectionService collectionService = services.getService(CalendarCollectionService.class);
                try {
                    final Appointment appointment = factory.createAppointmentSql(session).getObjectById(objectId, folderId);
                    final Appointment copyAppointment = appointment.clone();

                    /***************** Calculates the time of appointment in a serie **********************/
                    final RecurringResultsInterface recurringResult = collectionService.calculateRecurring(
                        copyAppointment,
                        DateUtility.convertDateToTimestamp(currentDate),
                        DateUtility.getEndOfDay(currentDate),
                        0);

                    if (recurringResult != null && recurringResult.size() > 0) {
                        final RecurringResultInterface rri = recurringResult.getRecurringResult(0);
                        copyAppointment.setStartDate(new Date(rri.getStart()));
                        copyAppointment.setEndDate(new Date(rri.getEnd()));
                    }
                    /***************************************************************************************/

                    notifyItem.add(new NotifyItem("folder", ro.getFolder()));
                    notifyItem.add(new NotifyItem("id", ro.getTargetId()));
                    notifyItem.add(new NotifyItem("alarm", ro.getDate()));
                    notifyItem.add(new NotifyItem("title", copyAppointment.getTitle()));
                    notifyItem.add(new NotifyItem("location", copyAppointment.getLocation()));
                    notifyItem.add(new NotifyItem("alarm", ro.getDate().getTime()));
                    notifyItem.add(new NotifyItem("last_modified", DateUtility.convertDateToTimestamp(ro.getLastModified())));
                    notifyItem.add(new NotifyItem("start_date", DateUtility.convertDateToTimestamp(copyAppointment.getStartDate())));
                    notifyItem.add(new NotifyItem("end_date", DateUtility.convertDateToTimestamp(copyAppointment.getEndDate())));
                    notifyItem.add(new NotifyItem("server_time", System.currentTimeMillis()));
                    items.add(notifyItem);
                } catch (SQLException e) {
                    throw MobileNotifierExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
                }
            }
        }
        return items;
    }

    @Override
    public NotifyTemplate getTemplate() throws OXException {
        final String template = MobileNotifierFileUtility.getTemplateFileContent(MobileNotifierProviders.REMINDER.getTemplateFileName());
        final String title = MobileNotifierProviders.REMINDER.getTitle();
        return new NotifyTemplate(title, template, false, MobileNotifierProviders.REMINDER.getIndex());
    }

    @Override
    public void putTemplate(String changedTemplate) throws OXException {
        MobileNotifierFileUtility.writeTemplateFileContent(MobileNotifierProviders.REMINDER.getTemplateFileName(), changedTemplate);
    }
}

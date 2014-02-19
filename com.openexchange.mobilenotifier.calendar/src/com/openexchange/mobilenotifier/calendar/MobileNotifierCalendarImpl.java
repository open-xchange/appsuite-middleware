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

package com.openexchange.mobilenotifier.calendar;

import java.awt.SystemColor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mobilenotifier.AbstractMobileNotifierService;
import com.openexchange.mobilenotifier.MobileNotifierProviders;
import com.openexchange.mobilenotifier.NotifyItem;
import com.openexchange.mobilenotifier.NotifyTemplate;
import com.openexchange.mobilenotifier.utility.LocaleAndTimeZone;
import com.openexchange.mobilenotifier.utility.LocalizationUtility;
import com.openexchange.mobilenotifier.utility.MobileNotifierFileUtil;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link MobileNotifierCalendarImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobileNotifierCalendarImpl extends AbstractMobileNotifierService {

    private final ServiceLookup services;

    public MobileNotifierCalendarImpl(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public String getProviderName() {
        return MobileNotifierProviders.APPOINTMENT.getProviderName();
    }

    @Override
    public String getFrontendName() {
        return MobileNotifierProviders.APPOINTMENT.getFrontendName();
    }

    @Override
    public List<List<NotifyItem>> getItems(Session session) throws OXException {
        AppointmentSqlFactoryService factory = services.getService(AppointmentSqlFactoryService.class);
        final int user_id = session.getUserId();
        final List<List<NotifyItem>> notifyItems = new ArrayList<List<NotifyItem>>();

        // test range
        final Date start = new Date(System.currentTimeMillis());
        final Date end = new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000));

        SearchIterator<Appointment> appointments = factory.createAppointmentSql(session).getActiveAppointments(
            user_id,
            start,
            end,
            new int[] {
                Appointment.FOLDER_ID, Appointment.OBJECT_ID, Appointment.TITLE, Appointment.LOCATION, Appointment.RECURRENCE_TYPE,
                Appointment.START_DATE, Appointment.ORGANIZER, Appointment.CONFIRMATIONS, Appointment.NOTE });
        while (appointments.hasNext()) {
            List<NotifyItem> item = new ArrayList<NotifyItem>();
            Appointment appointment = appointments.next();
            item.add(new NotifyItem("id", appointment.getObjectID()));
            item.add(new NotifyItem("folder", appointment.getParentFolderID()));
            item.add(new NotifyItem("title", appointment.getTitle()));
            item.add(new NotifyItem("location", appointment.getLocation()));
            item.add(new NotifyItem("start_date", appointment.getStartDate()));
            item.add(new NotifyItem("end_date", appointment.getEndDate()));
            item.add(new NotifyItem("organizer", appointment.getOrganizer()));
            item.add(new NotifyItem("note", appointment.getNote()));
            notifyItems.add(item);
        }
        return notifyItems;
    }

    @Override
    public NotifyTemplate getTemplate() throws OXException {
        final String template = MobileNotifierFileUtil.getTeamplateFileContent(MobileNotifierProviders.APPOINTMENT.getTemplateFileName());
        final String title = MobileNotifierProviders.APPOINTMENT.getTitle();
        return new NotifyTemplate(title, template, true, 2);
    }

    @Override
    public void putTemplate(String changedTemplate) throws OXException {
        MobileNotifierFileUtil.writeTemplateFileContent(MobileNotifierProviders.APPOINTMENT.getTemplateFileName(), changedTemplate);
    }

}

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

package com.openexchange.ajax.appointment.bugtests;

import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug37198Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Bug37198Test extends AbstractAJAXSession {

    private CalendarTestManager ctm;
    private Appointment app;
    private TimeZone utc;

    public Bug37198Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ctm = new CalendarTestManager(client);

        app = new Appointment();
        app.setTitle("Bug 37198 Test");
        utc = TimeZone.getTimeZone("UTC");
        app.setStartDate(TimeTools.D("12.03.2015 00:00", utc));
        app.setEndDate(TimeTools.D("13.03.2015 00:00", utc));
        app.setFullTime(true);
        app.setRecurrenceType(Appointment.DAILY);
        app.setUntil(TimeTools.D("13.03.2015 00:00", utc));
        app.setInterval(1);
        app.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        app.setIgnoreConflicts(true);
    }

    public void testBug37198() throws Exception {
        ctm.setFailOnError(true);
        ctm.insert(app);

        Appointment update = new Appointment();
        update.setObjectID(app.getObjectID());
        update.setParentFolderID(app.getParentFolderID());
        update.setEndDate(TimeTools.D("14.03.2015 00:00", utc));
        update.setRecurrenceType(Appointment.NO_RECURRENCE);
        update.setLastModified(new Date(Long.MAX_VALUE));
        update.setIgnoreConflicts(true);

        ctm.update(update);
    }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        super.tearDown();
    }

}

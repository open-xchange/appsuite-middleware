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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.appointment;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.AppointmentObject;

/**
 * This class contains test methods of calendar problems described by Funambol.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FunambolTest extends AbstractAJAXSession {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(FunambolTest.class);

    private static final int MAX_DIFFERENCE = 1000; // 1 second

    /**
     * @param name
     */
    public FunambolTest(final String name) {
        super(name);
    }

    public void testAppointmentCreationTime() throws Throwable {
        final AJAXClient client = getClient();
        final int folder = client.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = client.getValues().getTimeZone();
        final AppointmentObject app = new AppointmentObject();
        app.setParentFolderID(folder);
        app.setTitle("TestCreationTime");
        app.setStartDate(new Date(getHour(0)));
        app.setEndDate(new Date(getHour(1)));
        app.setIgnoreConflicts(true);
        final Date serverTime = client.getValues().getServerTime();
        System.out.println("ServerTime: " + serverTime);
        final InsertResponse insertR = (InsertResponse) Executor.execute(
            client, new InsertRequest(app, tz));
        final GetResponse getR = (GetResponse) Executor.execute(client,
            new GetRequest(folder, insertR));
        final AppointmentObject reload = getR.getAppointment(tz);
        final Date creationDate = reload.getCreationDate();
        System.out.println("CreationDate: " + creationDate);
        final long difference = Math.abs(serverTime.getTime() - creationDate.getTime());
        LOG.info("Time difference: " + difference);
        Executor.execute(client, new DeleteRequest(folder, insertR.getId(), new Date()));
        assertTrue("Too big time difference: ", difference < MAX_DIFFERENCE);
    }

    public static long getHour(final int diff) {
        return (System.currentTimeMillis() / 3600000 + diff) * 3600000;
    }
}

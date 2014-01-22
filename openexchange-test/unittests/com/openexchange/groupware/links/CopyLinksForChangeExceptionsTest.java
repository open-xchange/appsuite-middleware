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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
package com.openexchange.groupware.links;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarListener;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.tools.CommonAppointments;
import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.session.Session;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.api2.LinkSQLInterface;
import com.openexchange.api2.RdbLinkSQLInterface;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CopyLinksForChangeExceptionsTest extends TestCase {

    private CalendarListener listener;
    private String user;
    private Context ctx;
    private CommonAppointments appointments;
    private int userId;
    private Session session;
    private final List<CalendarDataObject> clean = new ArrayList<CalendarDataObject>();
    private User userObject;
    private UserConfiguration userConfig;

    private LinkSQLInterface links;

    @Override
    public void setUp() throws Exception {
        Init.startServer();
        links = new RdbLinkSQLInterface();
        listener = new CopyLinksForChangeExceptions(links);


        final TestConfig config = new TestConfig();

        user = config.getUser();
        final TestContextToolkit tools = new TestContextToolkit();
        ctx = tools.getDefaultContext();

        appointments = new CommonAppointments(ctx, user);


        userId = tools.resolveUser(user, ctx);

        userObject = UserStorage.getInstance().getUser(userId, ctx);
        userConfig = UserConfigurationStorage.getInstance().getUserConfiguration(userId, ctx);

        session = tools.getSessionForUser(user, ctx);

    }

    @Override
    public void tearDown() throws Exception {
        appointments.removeAll(user, clean);
        Init.stopServer();
    }

    // Bug 12377

    public void testShouldCopyLinks() throws OXException, IOException {
        final CalendarDataObject master = appointments.buildBasicAppointment(D("10/02/2008 10:00"), D("10/02/2008 12:00"));
        master.setRecurrenceType(CalendarDataObject.DAILY);
        master.setInterval(1);
        master.setOccurrence(10);
        appointments.save(master);
        clean.add(master);

        final CalendarDataObject exception = appointments.createIdentifyingCopy(master);
        exception.setRecurrencePosition(3);
        exception.setStartDate(D("13/02/2008 13:00"));
        exception.setEndDate(D("13/02/2008 15:00"));

        appointments.save(exception);


        CalendarDataObject appointmentToLinkTo = appointments.buildBasicAppointment(D("10/02/2008 17:00"), D("10/02/2008 19:00"));
        appointments.save(appointmentToLinkTo);
        clean.add(appointmentToLinkTo);

        LinkObject link = new LinkObject();
        link.setFirstFolder(master.getParentFolderID());
        link.setFirstId(master.getObjectID());
        link.setFirstType(Types.APPOINTMENT);

        link.setSecondFolder(appointmentToLinkTo.getParentFolderID());
        link.setSecondId(appointmentToLinkTo.getObjectID());
        link.setSecondType(Types.APPOINTMENT);

        link.setContext(ctx.getContextId());
        links.saveLink(link, userId, userConfig.getGroups(), session);

        listener.createdChangeExceptionInRecurringAppointment(master, exception,0, ServerSessionAdapter.valueOf(session));

        LinkObject[] loadedLinks = links.getLinksOfObject(exception.getObjectID(), Types.APPOINTMENT, exception.getParentFolderID(), userId, userConfig.getGroups(), session);
        assertNotNull(loadedLinks);
        assertEquals(1, loadedLinks.length);

        LinkObject loadedLink = loadedLinks[0];

        assertEquals(exception.getObjectID(), loadedLink.getFirstId());
        assertEquals(Types.APPOINTMENT, loadedLink.getFirstType());
        assertEquals(exception.getParentFolderID(), loadedLink.getFirstFolder());


        assertEquals(appointmentToLinkTo.getObjectID(), loadedLink.getSecondId());
        assertEquals(Types.APPOINTMENT, loadedLink.getSecondType());
        assertEquals(appointmentToLinkTo.getParentFolderID(), loadedLink.getSecondFolder());

    }
}

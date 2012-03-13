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

package com.openexchange.groupware.notify.imip;

import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.tools.CommonAppointments;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.ParticipantNotifyTest;
import com.openexchange.session.Session;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.setuptools.TestFolderToolkit;

/**
 * {@link IMipTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class IMipTest extends ParticipantNotifyTest {

    protected TestFolderToolkit folders;

    protected Context ctx;

    protected String user;

    protected String secondUser;

    protected String thirdUser;

    protected int userId;

    protected int secondUserId;

    protected int thirdUserId;

    protected String userMail;

    protected String secondUserMail;

    protected String thirdUserMail;

    protected Session so;

    protected CommonAppointments appointments;

    protected CalendarDataObject appointment;

    protected TestContextToolkit contextTools;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        folders = new TestFolderToolkit();
        final TestConfig config = new TestConfig();
        contextTools = new TestContextToolkit();

        ctx = contextTools.getContextByName(config.getContextName());
        user = config.getUser();
        secondUser = config.getSecondUser();
        thirdUser = config.getThirdUser();
        userId = contextTools.resolveUser(user, ctx);
        secondUserId = contextTools.resolveUser(secondUser, ctx);
        thirdUserId = contextTools.resolveUser(thirdUser, ctx);
        userMail = contextTools.loadUser(userId, ctx).getMail();
        secondUserMail = contextTools.loadUser(secondUserId, ctx).getMail();
        thirdUserMail = contextTools.loadUser(thirdUserId, ctx).getMail();

        so = contextTools.getSessionForUser(user, ctx);

        appointments = new CommonAppointments(ctx, user);

        notify.realUsers = true;
    }

    protected void checkState(Object message, ITipMethod method) throws Exception {

        assertTrue("message should be a multipart", MimeMultipart.class.isInstance(message));
        MimeMultipart msg = (MimeMultipart) message;
        assertTrue("wrong content type", msg.getContentType().startsWith("multipart/alternative"));

        BodyPart calendarPart = null;
        for (int i = 0; i < msg.getCount(); i++) {
            if (msg.getBodyPart(i).getContentType().startsWith("text/calendar")) {
                calendarPart = msg.getBodyPart(i);
                break;
            }
        }

        if (method == ITipMethod.NO_METHOD) {
            assertFalse("method in content type", calendarPart.getContentType().contains("method"));
        } else {
            assertTrue("missing method in content type", calendarPart.getContentType().contains(method.getMethod()));
        }
    }

}

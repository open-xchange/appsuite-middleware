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

package com.openexchange.calendar.itip.analyzers;

import static com.openexchange.time.TimeTools.D;
import java.util.Arrays;
import java.util.Set;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.session.Session;
import com.openexchange.session.SimSession;
import static org.junit.Assert.*;

/**
 * {@link AbstractITipAnalyzerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AbstractITipAnalyzerTest {
    protected Session session = new SimSession(12,1337);
    protected  UserParticipant theUser = new UserParticipant(session.getUserId());

    // Test Data

    protected CalendarDataObject appointment(String uid) {
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setUid(uid);
        cdao.setStartDate(D("tomorrow at 12:00"));
        cdao.setEndDate(D("tomorrow at 13:00"));
        cdao.setTitle("A new title!");

        theUser.setConfirm(ConfirmStatus.ACCEPT.getId());
        cdao.setParticipants(Arrays.asList(theUser));
        cdao.setUsers(Arrays.asList(theUser));


        return cdao;
    }

    public static void assertActions(ITipAnalysis analysis, ITipAction...actions) {
        String expected = Arrays.asList(actions).toString();
        String got = analysis.getActions().toString();

        Set<ITipAction> recommendedActions = analysis.getActions();
        for (ITipAction action : actions) {
            assertTrue(expected+" != "+got, recommendedActions.remove(action));
        }
        assertTrue(expected+" != "+got, recommendedActions.isEmpty());

    }

}

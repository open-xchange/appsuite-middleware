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

package com.openexchange.chronos.itip;

import java.util.Collections;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.support.membermodification.MemberModifier;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.analyzers.AbstractITipAnalyzer;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ITipMockFactory}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public final class ITipMockFactory {

    public static ITipMessage mockITipMessage(ITipMethod method, final Event update) {
        return mockITipMessage(method, update, false);
    }

    public static ITipMessage mockITipMessage(ITipMethod method, final Event update, boolean microsoft) {
        ITipMessage message = Mockito.mock(ITipMessage.class);
        Mockito.when(message.getMethod()).thenReturn(method);
        Mockito.when(message.getEvent()).thenReturn(update);
        Mockito.when(message.exceptions()).thenReturn(Collections.emptyList());
        Mockito.when(message.hasFeature(ITipSpecialHandling.MICROSOFT)).thenReturn(Boolean.valueOf(microsoft));
        return message;
    }

    public static CalendarSession mockCalendarSession(final int contextId, final int userId, final ServerSession serverSession, final CalendarUtilities utilities) {
        CalendarSession session = Mockito.mock(CalendarSession.class);
        Mockito.when(session.getContextId()).thenReturn(Integer.valueOf(contextId));
        Mockito.when(session.getUserId()).thenReturn(Integer.valueOf(userId));
        Mockito.when(session.getEntityResolver()).thenReturn(null);
        Mockito.when(session.getSession()).thenReturn(serverSession);
        Mockito.when(session.getUtilities()).thenReturn(utilities);
        return session;
    }

    public static CalendarUtilities mockUtilities() throws OXException {
        CalendarUtilities utilities = Mockito.mock(CalendarUtilities.class);
        Mockito.when(utilities.copyEvent(ArgumentMatchers.any(), ArgumentMatchers.any())).then((i) -> {
            return EventMapper.getInstance().copy(i.getArgument(0), new Event(), (EventField[]) i.getArguments()[1]);
        });
        return utilities;
    }

    public static ITipIntegrationUtility mockUtil(final Event original) throws OXException {
        ITipIntegrationUtility util = Mockito.mock(ITipIntegrationUtility.class);
        Mockito.when(util.resolveUid(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(original);
        return util;
    }

    public static <T extends AbstractITipAnalyzer> void injectUtil(T analyzer, ITipIntegrationUtility util) throws IllegalArgumentException, IllegalAccessException {
        MemberModifier.field(analyzer.getClass(), "util").set(analyzer, util);
    }

    public static ServerSession getServerSession(final Context context, final int contextId, final User user, final int userId) {
        ServerSession serverSession = Mockito.mock(ServerSession.class);
        Mockito.when(serverSession.getContext()).thenReturn(context);
        Mockito.when(serverSession.getContextId()).thenReturn(Integer.valueOf(contextId));

        Mockito.when(serverSession.getUser()).thenReturn(user);
        Mockito.when(serverSession.getUserId()).thenReturn(Integer.valueOf(userId));
        return serverSession;
    }

    public static Context getContext(final int contextId) {
        Context lContext = Mockito.mock(Context.class);
        Mockito.when(lContext.getContextId()).thenReturn(Integer.valueOf(contextId));
        return lContext;
    }

    public static User getUser(final int userId) {
        User lUser = Mockito.mock(User.class);
        Mockito.when(lUser.getId()).thenReturn(Integer.valueOf(userId));

        return lUser;
    }

}

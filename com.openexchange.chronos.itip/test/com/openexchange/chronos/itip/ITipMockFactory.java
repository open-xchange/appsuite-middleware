/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

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
        Mockito.when(Boolean.valueOf(message.hasFeature(ITipSpecialHandling.MICROSOFT))).thenReturn(Boolean.valueOf(microsoft));
        return message;
    }

    public static CalendarSession mockCalendarSession(final int contextId, final int userId, final ServerSession serverSession, final CalendarUtilities utilities) {
        CalendarSession session = Mockito.mock(CalendarSession.class);
        Mockito.when(Integer.valueOf(session.getContextId())).thenReturn(Integer.valueOf(contextId));
        Mockito.when(Integer.valueOf(session.getUserId())).thenReturn(Integer.valueOf(userId));
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
        Mockito.when(Integer.valueOf(serverSession.getContextId())).thenReturn(Integer.valueOf(contextId));

        Mockito.when(serverSession.getUser()).thenReturn(user);
        Mockito.when(Integer.valueOf(serverSession.getUserId())).thenReturn(Integer.valueOf(userId));
        return serverSession;
    }

    public static Context getContext(final int contextId) {
        Context lContext = Mockito.mock(Context.class);
        Mockito.when(Integer.valueOf(lContext.getContextId())).thenReturn(Integer.valueOf(contextId));
        return lContext;
    }

    public static User getUser(final int userId) {
        User lUser = Mockito.mock(User.class);
        Mockito.when(Integer.valueOf(lUser.getId())).thenReturn(Integer.valueOf(userId));

        return lUser;
    }

}

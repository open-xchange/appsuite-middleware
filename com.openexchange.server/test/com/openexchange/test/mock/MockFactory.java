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

package com.openexchange.test.mock;

import static com.openexchange.java.Autoboxing.I;
import org.mockito.Mockito;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link MockFactory}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class MockFactory {

    public static int USER_ID = 3;

    public static int CONTEXT_ID = 7;

    private static User user = getUser();
    private static Context context = getContext();

    public static ServerSession getServerSession() {
        ServerSession serverSession = Mockito.mock(ServerSession.class);
        Mockito.when(serverSession.getContext()).thenReturn(context);
        Mockito.when(I(serverSession.getContextId())).thenReturn(I(getContextId()));

        Mockito.when(serverSession.getUser()).thenReturn(user);
        Mockito.when(I(serverSession.getUserId())).thenReturn(I(getUserId()));
        return serverSession;
    }

    public static Context getContext() {
        Context lContext = Mockito.mock(Context.class);
        Mockito.when(I(lContext.getContextId())).thenReturn(I(getContextId()));
        return lContext;
    }

    public static int getContextId() {
        return CONTEXT_ID;
    }

    public static User getUser() {
        User lUser = Mockito.mock(User.class);
        Mockito.when(I(lUser.getId())).thenReturn(I(getUserId()));

        return lUser;
    }

    public static int getUserId() {
        return USER_ID;
    }

}

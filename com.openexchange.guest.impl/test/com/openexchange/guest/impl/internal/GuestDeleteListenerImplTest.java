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

package com.openexchange.guest.impl.internal;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.guest.GuestService;

/**
 * {@link GuestDeleteListenerImplTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
@RunWith(PowerMockRunner.class)
public class GuestDeleteListenerImplTest {

    private static final int CONTEXT_ID = 1;
    private static final int USER_ID = 11;

    private GuestDeleteListenerImpl guestDeleteListenerImpl;

    @Mock
    private GuestService guestService;

    @Mock
    private DeleteEvent deleteEvent;

    @Mock
    private Connection connection;

    @Mock
    private Context context;

    /**
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(deleteEvent.getContext()).thenReturn(context);
        Mockito.when(I(deleteEvent.getId())).thenReturn(I(USER_ID));
        Mockito.when(I(context.getContextId())).thenReturn(I(CONTEXT_ID));

        this.guestDeleteListenerImpl = new GuestDeleteListenerImpl(guestService);
    }

     @Test
     public void testDeletePerformed_wrongType_doNotRemoveGuest() throws OXException {
        Mockito.when(I(deleteEvent.getType())).thenReturn(I(DeleteEvent.TYPE_GROUP));

        guestDeleteListenerImpl.deletePerformed(deleteEvent, connection, connection);

        Mockito.verify(guestService, Mockito.never()).removeGuest(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
    }

     @Test
     public void testDeletePerformed_deleteCalledForUser_removeGuest() throws OXException {
        Mockito.when(I(deleteEvent.getType())).thenReturn(I(DeleteEvent.TYPE_USER));

        guestDeleteListenerImpl.deletePerformed(deleteEvent, connection, connection);

        Mockito.verify(guestService, Mockito.times(1)).removeGuest(CONTEXT_ID, USER_ID);
    }

     @Test
     public void testDeletePerformed_wrongType_doNotRemoveGuests() throws OXException {
        Mockito.when(I(deleteEvent.getType())).thenReturn(I(DeleteEvent.TYPE_GROUP));

        guestDeleteListenerImpl.deletePerformed(deleteEvent, connection, connection);

        Mockito.verify(guestService, Mockito.never()).removeGuests(ArgumentMatchers.anyInt());
    }

     @Test
     public void testDeletePerformed_deleteCalledForContext_removeGuests() throws OXException {
        Mockito.when(I(deleteEvent.getType())).thenReturn(I(DeleteEvent.TYPE_CONTEXT));

        guestDeleteListenerImpl.deletePerformed(deleteEvent, connection, connection);

        Mockito.verify(guestService, Mockito.times(1)).removeGuests(CONTEXT_ID);
    }
}

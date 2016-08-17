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

package com.openexchange.guest.impl.internal;

import java.sql.Connection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.guest.GuestService;
import com.openexchange.guest.impl.internal.GuestDeleteListenerImpl;

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
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Mockito.when(deleteEvent.getContext()).thenReturn(context);
        Mockito.when(deleteEvent.getId()).thenReturn(USER_ID);
        Mockito.when(context.getContextId()).thenReturn(CONTEXT_ID);

        this.guestDeleteListenerImpl = new GuestDeleteListenerImpl(guestService);
    }

    @Test
    public void testDeletePerformed_wrongType_doNotRemoveGuest() throws OXException {
        Mockito.when(deleteEvent.getType()).thenReturn(DeleteEvent.TYPE_GROUP);

        guestDeleteListenerImpl.deletePerformed(deleteEvent, connection, connection);

        Mockito.verify(guestService, Mockito.never()).removeGuest(Matchers.anyInt(), Matchers.anyInt());
    }

    @Test
    public void testDeletePerformed_deleteCalledForUser_removeGuest() throws OXException {
        Mockito.when(deleteEvent.getType()).thenReturn(DeleteEvent.TYPE_USER);

        guestDeleteListenerImpl.deletePerformed(deleteEvent, connection, connection);

        Mockito.verify(guestService, Mockito.times(1)).removeGuest(CONTEXT_ID, USER_ID);
    }

    @Test
    public void testDeletePerformed_wrongType_doNotRemoveGuests() throws OXException {
        Mockito.when(deleteEvent.getType()).thenReturn(DeleteEvent.TYPE_GROUP);

        guestDeleteListenerImpl.deletePerformed(deleteEvent, connection, connection);

        Mockito.verify(guestService, Mockito.never()).removeGuests(Matchers.anyInt());
    }

    @Test
    public void testDeletePerformed_deleteCalledForContext_removeGuests() throws OXException {
        Mockito.when(deleteEvent.getType()).thenReturn(DeleteEvent.TYPE_CONTEXT);

        guestDeleteListenerImpl.deletePerformed(deleteEvent, connection, connection);

        Mockito.verify(guestService, Mockito.times(1)).removeGuests(CONTEXT_ID);
    }
}

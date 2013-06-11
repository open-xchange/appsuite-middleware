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

package com.openexchange.realtime.client.room.impl;

import static org.junit.Assert.assertNotNull;
import org.json.JSONValue;
import org.junit.Test;
import org.mockito.Mockito;
import com.openexchange.realtime.client.RTConnection;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.realtime.client.user.RTUser;


/**
 * Tests for class {@link RTRoomImpl}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class RTRoomImplTest {

    RTUser mockedUser = Mockito.mock(RTUser.class);

    RTConnectionProperties mockedConnectionProperties = Mockito.mock(RTConnectionProperties.class);

    RTMessageHandler mockedMessageHandler = Mockito.mock(RTMessageHandler.class);

    @Test
    public void testCreateRoom_Fine_Created() {
        RTRoomImpl room = new RTRoomImpl(mockedUser, mockedConnectionProperties, mockedMessageHandler);

        assertNotNull(room);
    }

    @Test
    public void testCreateRoom_Fine_CreatedAndUserNotNull() {
        RTRoomImpl room = new RTRoomImpl(mockedUser, mockedConnectionProperties, mockedMessageHandler);
        assertNotNull(room.getRtUser());
    }

    @Test
    public void testCreateRoom_Fine_CreatedAndConnectionPropertiesNotNull() {
        RTRoomImpl room = new RTRoomImpl(mockedUser, mockedConnectionProperties, mockedMessageHandler);

        assertNotNull(room.getRtConnectionProperties());
    }

    @Test
    public void testCreateRoom_Fine_CreatedAndMessageHandlerNotNull() {
        RTRoomImpl room = new RTRoomImpl(mockedUser, mockedConnectionProperties, mockedMessageHandler);

        assertNotNull(room.getRtMessageHandler());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRoom_userMissing_ThrowException() {
        new RTRoomImpl(null, mockedConnectionProperties, mockedMessageHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRoom_connectionMissing_ThrowException() {
        new RTRoomImpl(mockedUser, null, mockedMessageHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRoom_messageHandlerMissing_ThrowException() {
        new RTRoomImpl(mockedUser, mockedConnectionProperties, null);
    }

    /**
     * Test method for {@link com.openexchange.realtime.client.room.impl.RTRoomImpl#join()}.
     * 
     * @throws RTException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testJoin_NameNull_ThrowException() throws RTException {
        RTRoomImpl room = new RTRoomImpl(mockedUser, mockedConnectionProperties, mockedMessageHandler);
        room.join(null, "theToAddress", mockedMessageHandler);
    }

    /**
     * Test method for {@link com.openexchange.realtime.client.room.impl.RTRoomImpl#join()}.
     * 
     * @throws RTException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testJoin_ToNull_ThrowException() throws RTException {
        RTRoomImpl room = new RTRoomImpl(mockedUser, mockedConnectionProperties, mockedMessageHandler);
        room.join("TheRoomName", null, mockedMessageHandler);
    }

    /**
     * Test method for {@link com.openexchange.realtime.client.room.impl.RTRoomImpl#join()}.
     * 
     * @throws RTException
     */
    @Test
    public void testJoin_Fine_ToAddressSet() throws RTException {
        RTRoomImpl room = new RTRoomImpl(mockedUser, mockedConnectionProperties, mockedMessageHandler) {
            @Override
            protected void send(JSONValue objectToSend) {
                return;
            }

            @Override
            protected void setupTimer() {
                return;
            }

            @Override
            protected void loginAndConnect() {
                return;
            }

        };
        room.join("TheRoomName", "theToAddress", mockedMessageHandler);

        assertNotNull(room.getToAddress());
    }

    /**
     * Test method for {@link com.openexchange.realtime.client.room.impl.RTRoomImpl#setupTimer()}.
     * 
     * @throws RTException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetupTimer_connectionNull_ThrowExcpetion() {
        RTRoomImpl room = new RTRoomImpl(mockedUser, mockedConnectionProperties, mockedMessageHandler);
        room.setupTimer();
    }

    /**
     * Test method for {@link com.openexchange.realtime.client.room.impl.RTRoomImpl#setupTimer()}.
     * 
     * @throws RTException
     */
    @Test
    public void testSetupTimer_Fine_TimerInitialized() {
        RTRoomImpl room = new RTRoomImpl(mockedUser, mockedConnectionProperties, mockedMessageHandler);
        room.setRtConnection(Mockito.mock(RTConnection.class));
        room.setupTimer();

        assertNotNull(room.getPingTimer());
    }
}

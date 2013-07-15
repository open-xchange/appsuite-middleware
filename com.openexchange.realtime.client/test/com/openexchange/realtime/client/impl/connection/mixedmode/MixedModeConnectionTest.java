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

package com.openexchange.realtime.client.impl.connection.mixedmode;

import org.json.JSONValue;
import org.junit.Test;
import com.openexchange.realtime.client.ID;
import com.openexchange.realtime.client.RTConnection;
import com.openexchange.realtime.client.RTConnectionFactory;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTConnectionProperties.RTConnectionType;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.realtime.client.room.RTRoom;
import com.openexchange.realtime.client.room.RTRoomFactory;
import com.openexchange.realtime.client.room.chinese.ChineseRoomFactory;


/**
 * {@link MixedModeConnectionTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class MixedModeConnectionTest {

    @Test
    public void testWasyncConnection() throws RTException {
        RTConnectionProperties connectionProperties = RTConnectionProperties.newBuilder("marc.arens", "secret", "chineseRoom")
            .setHost("localhost")
            .setConnectionType(RTConnectionType.LONG_POLLING)
            .setSecure(true)
            .build();
        RTConnectionFactory.getInstance().newConnection(connectionProperties);
    }

    @Test
    public void testRoom() throws Exception {
        RTConnectionProperties connectionProperties = RTConnectionProperties.newBuilder("marc.arens@premium", "secret", "desktop1")
            .setHost("localhost")
            .setConnectionType(RTConnectionType.LONG_POLLING)
            .setSecure(true)
            .build();
        RTConnection connection = RTConnectionFactory.getInstance().newConnection(connectionProperties);
        RTRoomFactory roomFactory = new ChineseRoomFactory();
        RTRoom chineseRoom = roomFactory.newRoom(connection);
        chineseRoom.join(new ID("synthetic.china://room1"), new RTMessageHandler() {
            @Override
            public void onMessage(JSONValue message) {
                System.out.println(message.toString());
            }
        });
        chineseRoom.say("Hello World");
        Thread.sleep(120000);
        chineseRoom.say("This is Marc speaking");
        chineseRoom.leave();
    }

}

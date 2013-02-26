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

package om.openexchange.realtime.dispatch.impl;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.dispatch.impl.MessageDispatcherImpl;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.ElementPath;


/**
 * {@link MessageDispatcherTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MessageDispatcherTest extends MessageDispatcherImpl {
    
    @Test
    public void testChannelChoosing() throws Exception {
        Stanza stanza = new Stanza() {
            @Override
            public ID getTo() {
                return new ID("ox", "some.body", "context", "resource");
            }

            @Override
            public void initializeDefaults() throws OXException {
                // TODO Auto-generated method stub
            }
        };
        
        Channel c1 = new MockChannel("nox", 25);
        Channel c2 = new MockChannel("ox", 15);
        Channel c3 = new MockChannel("ab", 15);
        Channel c4 = new MockChannel("cd", 20);
        Channel[] expected = new Channel[] { c2, c1, c4, c3 };
        
        addChannel(c1);
        addChannel(c2);
        addChannel(c3);
        addChannel(c4);
        
        SortedSet<Channel> chosenChannels = chooseChannels(stanza);
        Assert.assertEquals("Wrong set size.", expected.length, chosenChannels.size());
        
        Iterator<Channel> it = chosenChannels.iterator();
        for (int i = 0; i < expected.length; i++) {
            Channel channel = it.next();
            Assert.assertEquals("Wrong channel", expected[i], channel);
        }
    }
    
    private static final class MockChannel implements Channel {
        
        private final String protocol;
        
        private final int priority;

        /**
         * Initializes a new {@link MockChannel}.
         * @param protocol
         * @param priority
         */
        public MockChannel(String protocol, int priority) {
            super();
            this.protocol = protocol;
            this.priority = priority;
        }

        @Override
        public String getProtocol() {
            return protocol;
        }

        @Override
        public boolean canHandle(Set<ElementPath> elementPaths, ID recipient) throws OXException {
            return true;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public boolean isConnected(ID id) throws OXException {
            return true;
        }

        @Override
        public void send(Stanza stanza) throws OXException { }
        
    }

}

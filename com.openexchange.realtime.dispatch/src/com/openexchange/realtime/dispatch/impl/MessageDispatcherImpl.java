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

package com.openexchange.realtime.dispatch.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link MessageDispatcherImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class MessageDispatcherImpl implements MessageDispatcher {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(MessageDispatcher.class));

    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    @Override
    public void send(final Stanza stanza) throws OXException {
        SortedSet<Channel> chosenChannels = chooseChannels(stanza);
        if (chosenChannels.isEmpty()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Couldn't find appropriate channel for sending stanza");
            }
            return;
            // return for now/testing, otherwise throw Exception
            // throw RealtimeExceptionCodes.NO_APPROPRIATE_CHANNEL.create(stanza.getTo().toString(), stanza);
        }

        for (Channel channel : chosenChannels) {
            // TODO: It might not be correct to send the message to all channels. Maybe we have to decide this by Stanza.
            channel.send(stanza);
        }
    }

    /**
     * Choose a channel based on the following properties:
     * <ol>
     * <li><b>Protocol</b>: Check the full id of the recipient for the protocol used to address him and choose a channel able to handle that
     * protocol</li>
     * <li><b>Priority and Capabilities</b>: Get the preferred channel that can handle the Stanza if no protocol is given</li>
     * </ol>
     *
     * @param stanza The stanza to dispatch
     * @return Null or the chosen channel that is able to handle the stanza.
     * @throws OXException If the lookup of a channel fails for any reason
     */
    protected SortedSet<Channel> chooseChannels(Stanza stanza) throws OXException {
        ID to = stanza.getTo();
        Set<Channel> allChannels = new HashSet<Channel>(channels.values());
        
        final String protocol = to.getProtocol();
        SortedSet<Channel> chosen = new TreeSet<Channel>(new Comparator<Channel>() {
            @Override
            public int compare(Channel c1, Channel c2) {
                if (protocol == null) {
                    return comparePriority(c1, c2);
                } else if (c1.getProtocol().equals(protocol) && c2.getProtocol().equals(protocol)) {
                    return comparePriority(c1, c2);
                } else if (c1.getProtocol().equals(protocol)) {
                    return -1;
                } else if (c2.getProtocol().equals(protocol)) {
                    return 1;
                } else {
                    return comparePriority(c1, c2);
                }
            }
            
            private int comparePriority(Channel c1, Channel c2) {
                int p1 = c1.getPriority();
                int p2 = c2.getPriority();
                if (p1 >= p2) {
                    if (p1 == p2) {
                        return 0;
                    }
                    return -1;
                }
                return 1;
            }
        });
        
        Set<ElementPath> namespaces = new HashSet<ElementPath>(stanza.getElementPaths());
        if (protocol != null) { // Choose channel based on protocol
            Channel channel = channels.get(protocol);
            if (channel != null && channel.canHandle(namespaces, to)) {
                chosen.add(channel);
                allChannels.remove(channel);
            }
        }

        for (Channel channel : allChannels) { // Choose channels based on priority
            if (channel.canHandle(namespaces, to)) {
                chosen.add(channel);
            }
        }
            
        return chosen;
    }

    @Override
    public void addChannel(final Channel channel) {
        channels.put(channel.getProtocol(), channel);
    }

    @Override
    public void removeChannel(final Channel channel) {
        channels.remove(channel.getProtocol());
    }

    @Override
    public boolean canDeliverInstantly(Stanza stanza) throws OXException {
        return !chooseChannels(stanza).isEmpty();
    }

}

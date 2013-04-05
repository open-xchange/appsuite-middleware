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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.dispatch.DispatchExceptionCode;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.StanzaSequenceGate;

/**
 * {@link LocalMessageDispatcherImpl}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class LocalMessageDispatcherImpl implements LocalMessageDispatcher {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(MessageDispatcher.class));

    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    private StanzaSequenceGate gate = new StanzaSequenceGate() {

        @Override
        public void handleInternal(Stanza stanza, ID recipient) throws OXException {
            String protocol = recipient.getProtocol();
            Channel channel = channels.get(protocol);
            if (channel == null) {
                throw DispatchExceptionCode.UNKNOWN_CHANNEL.create(protocol);
            } else {
                if (channel.isConnected(recipient)) {
                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Sending Stanza: " + stanza + " to " + recipient.toString());
                        }
                        channel.send(stanza, recipient);
                    } catch (OXException e) {
                        if (RealtimeExceptionCodes.RESOURCE_NOT_AVAILABLE.equals(e)) {
                            throw DispatchExceptionCode.RESOURCE_OFFLINE.create(recipient.toString());
                        } else {
                            throw e;
                        }
                    } catch (RuntimeException e) {
                        throw DispatchExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                } else {
                    LOG.error(DispatchExceptionCode.RESOURCE_OFFLINE.create(recipient.toString()));
                    throw DispatchExceptionCode.RESOURCE_OFFLINE.create(recipient.toString());
                }
            }
        }
    };

    @Override
    public Map<ID, OXException> send(Stanza stanza, Set<ID> recipients) throws OXException {
        stanza.transformPayloadsToInternal();
        Map<ID, OXException> exceptions = new HashMap<ID, OXException>();
        for (ID recipient : recipients) {
            try {
                gate.handle(stanza, recipient);
            } catch (OXException e) {
                exceptions.put(recipient, e);
            }
        }

        return exceptions;
    }

    @Override
    public void addChannel(final Channel channel) {
        channels.put(channel.getProtocol(), channel);
    }

    @Override
    public void removeChannel(final Channel channel) {
        channels.remove(channel.getProtocol());
    }
}

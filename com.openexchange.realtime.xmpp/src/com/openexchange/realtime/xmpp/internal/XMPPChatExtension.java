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

package com.openexchange.realtime.xmpp.internal;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.xmpp.XMPPDelivery;
import com.openexchange.realtime.xmpp.XMPPExtension;
import com.openexchange.realtime.xmpp.packet.JID;
import com.openexchange.realtime.xmpp.packet.XMPPMessage;
import com.openexchange.realtime.xmpp.packet.XMPPStanza;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link XMPPChatExtension}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class XMPPChatExtension implements XMPPExtension {

    public static ServiceLookup services;

    @Override
    public String getServiceName() {
        return "chat";
    }

    @Override
    public boolean canHandle(Stanza stanza) {
        return Message.class.isInstance(stanza) && canHandleNamespace(stanza.getNamespace()) && ((Message) stanza).getType() == Message.Type.chat;
    }

    @Override
    public void handleOutgoing(Stanza stanza, XMPPDelivery delivery, ServerSession session) throws OXException {
        Message msg = (Message) stanza;
        XMPPMessage xmpp = new XMPPMessage(XMPPMessage.Type.chat, session);
        transform(msg, xmpp, session);

        delivery.deliver(xmpp, session);
    }

    @Override
    public void handleIncoming(XMPPStanza xmpp, ServerSession session) throws OXException {
        Message message = new Message();
        message.setType(Message.Type.chat);
        transform((XMPPMessage) xmpp, message, session);
        services.getService(MessageDispatcher.class).send(message, xmpp.getSession());
    }

    private boolean canHandleNamespace(String namespace) {
        return namespace != null && namespace.trim().equals("chat");
    }

    private void transform(Message message, XMPPMessage xmpp, ServerSession session) throws OXException {
        ID from = message.getFrom();
        xmpp.setFrom(new JID(from.getUser(), from.getContext(), from.getResource()));

        ID to = message.getTo();
        xmpp.setTo(new JID(to.getUser(), to.getContext(), to.getResource()));

        xmpp.setPayload(message.getPayload().to("xmpp", session));
    }

    private void transform(XMPPMessage xmpp, Message message, ServerSession session) throws OXException {
        JID from = xmpp.getFrom();
        message.setFrom(new ID(null, from.getUser(), from.getDomain(), from.getResource()));

        JID to = xmpp.getTo();
        message.setTo(new ID(null, to.getUser(), to.getDomain(), to.getResource()));

        message.setPayload(xmpp.getPayload().to("chatMessage", session));

        message.setNamespace("chat");
    }

}

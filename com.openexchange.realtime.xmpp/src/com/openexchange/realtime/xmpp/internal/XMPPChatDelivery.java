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

import static org.joox.JOOX.$;
import java.util.UUID;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.joox.JOOX;
import org.joox.Match;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.xmpp.XMPPChannel;
import com.openexchange.realtime.xmpp.XMPPDelivery;
import com.openexchange.realtime.xmpp.packet.XMPPIq;
import com.openexchange.realtime.xmpp.packet.XMPPStanza;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link XMPPChatDelivery}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class XMPPChatDelivery extends SimpleChannelUpstreamHandler implements XMPPDelivery {

    private static final String AUTH_SASL = "urn:ietf:params:xml:ns:xmpp-sasl";

    private static final String NS_BIND = "urn:ietf:params:xml:ns:xmpp-bind";

    private ChannelHandlerContext ctx;

    private final XMPPChannel channel;

    private final XMPPHandler handler;

    private ServerSession session;

    private ID id;

    private UUID streamId;

    private String domain;

    private State state = State.init;
    
    private static boolean DEBUG = true;

    enum State {
        init, preLogin, postLogin, open, preBind, preSession;
    }

    public XMPPChatDelivery(XMPPChannel channel, XMPPHandler handler) {
        this.channel = channel;
        this.handler = handler;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        this.ctx = ctx;
        super.channelConnected(ctx, e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if (id != null) {
            channel.removeDelivery(this.id);
        }
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        String message = (String) e.getMessage();
        if (DEBUG) {
            System.out.println("<--- IN ---\n" + message);
        }
        XMPPContainer container = new XMPPContainer(message.trim());

        if (state == State.init) {
            negotiateStreamDetails(container);
            state = State.preLogin;
        } else if (state == State.preLogin) {
            doLogin(container);
            state = State.postLogin;
        } else if (state == State.postLogin) {
            negotiateStreamDetails(container);
            state = State.preBind;
        } else if (state == State.preBind) {
            bind(container);
            state = State.preSession;
        } else if (state == State.preSession) {
            handleSession(container);
            state = State.open;
        } else if (state == State.open) {
            handler.handle(container);
        }
    }

    private void handleSession(XMPPContainer container) {
        Match xml = $(container.getXml());
        if (xml.tag().trim().equalsIgnoreCase("iq") && xml.child().tag().trim().equalsIgnoreCase("session")) {
            write($("iq").attr("id", xml.id()).attr("type", XMPPIq.Type.result.name()).toString());
        }
    }

    private void bind(XMPPContainer container) throws OXException {
        Match xml = $(container.getXml());
        String stanza = xml.tag().trim().toLowerCase();

        if (!stanza.equals("iq")) {
            // TODO: throw error
        }

        String iqId = xml.attr("id");

        String resource = xml.child("bind").child("resource").content();
        ID prospect = new ID("xmpp", session.getLoginName(), session.getContext().getName(), resource);
        int suffix = 0;
        while (channel.isConnected(prospect, session)) {
            prospect.setResource(prospect.getResource() + "_" + ++suffix);
        }

        id.setResource(prospect.getResource());

        Match response = $("iq").attr("id", iqId).attr("type", XMPPIq.Type.result.name());
        Match jid = $("jid", id.toGeneralForm().toString() + "/" + id.getResource());
        response.append($("bind").attr("xmlns", NS_BIND).append(jid));
        
        write(response.toString());
    }

    private void negotiateStreamDetails(XMPPContainer container) {
        String xml = container.getXml();
        if (xml.contains("<stream:stream ")) {
            xml = xml + "</stream:stream>";
        }

        Match match = $(xml);
        String xmlns = match.attr("xmlns");
        if (xmlns != null) {
            if (xmlns.trim().equalsIgnoreCase("jabber:client")) {
                streamId = UUID.randomUUID();
                StreamHandler streamHandler = new StreamHandler(streamId);
                domain = match.attr("to");
                write(streamHandler.createClientResponseStream(domain));
                write(streamHandler.getStreamFeatures(state));
            } else if (xmlns.trim().equalsIgnoreCase("jabber:server")) {
                // TODO:
            } else {
                // TODO: Error
            }
        }
    }

    @Override
    public void deliver(XMPPStanza stanza, ServerSession session) throws OXException {
        write(stanza.toXML(session));
    }

    private void doLogin(XMPPContainer container) throws OXException {
        String xml = container.getXml();
        Match match = JOOX.$(xml);

        if (!match.tag().equals("auth")) {
            // TODO: Throw error
        }

        String namespace = match.attr("xmlns");
        if (!namespace.trim().equals(AUTH_SASL)) {
            // TODO: Support other auth methods.
        }

        String[] userAndPassword = new String(Base64.decode(match.content())).trim().split("\0");
        String user = userAndPassword[0] + "@" + domain;
        String password = userAndPassword[1];

        LoginResult loginResult = LoginPerformer.getInstance().doLogin(new XMPPLoginRequest(user, password));

        this.session = ServerSessionAdapter.valueOf(loginResult.getSession());
        this.id = new ID("xmpp", session.getLoginName(), session.getContext().getName(), null);

        channel.addDelivery(id, this);

        container.setSession(session);

        write("<success xmlns=\"" + AUTH_SASL + "\"/>");

        container.setSession(session);
    }

    private void write(String message) {
        if (DEBUG) {
            System.out.println("--- OUT --->\n" + message);
        }
        ctx.getChannel().write(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        e.getCause().printStackTrace();
    }

}

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

    private ChannelHandlerContext ctx;

    private XMPPChannel channel;

    private XMPPHandler handler;

    private ServerSession session;

    private ID id;

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
        channel.removeDelivery(this.id);
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        String message = (String) e.getMessage();
        XMPPContainer container = new XMPPContainer(message.trim());
        
        if (session == null) {
            doLogin(container);
        } else {
            container.setSession(session);
            handler.handle(container);
        }
    }

    @Override
    public void deliver(XMPPStanza stanza, ServerSession session) throws OXException {
        ctx.getChannel().write(stanza.toXML(session));
    }

    private void doLogin(XMPPContainer container) throws OXException {
        Match match = JOOX.$(container.getXml());

        if (!match.tag().equals("auth")) {
            // TODO: Throw error
        }
        
        String namespace = match.attr("xmlns");
        if (!namespace.trim().equals(AUTH_SASL)) {
            // TODO: Support other auth methods.
        }

        String[] userAndPassword = new String(Base64.decode(match.content())).trim().split("\0");
        String user = userAndPassword[0];
        String password = userAndPassword[1];

        LoginResult loginResult = LoginPerformer.getInstance().doLogin(new XMPPLoginRequest(user, password));

        this.session = ServerSessionAdapter.valueOf(loginResult.getSession());
        this.id = new ID("xmpp", session.getLoginName(), session.getContext().getName(), null);

        channel.addDelivery(id, this);

        container.setSession(session);

        ctx.getChannel().write("<success xmlns=\"" + AUTH_SASL + "\">" + session.getSessionID() + "</success>");
    }
    
    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        e.getCause().printStackTrace();
    }

}

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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.util.CharsetUtil;
import org.joox.Match;


/**
 * {@link XMPPStreamDecoder}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class XMPPStreamDecoder extends SimpleChannelHandler {

    private static final QName STREAM_NAME = new QName("stream='http://etherx.jabber.org/streams", "stream", "stream");

    private static enum Status {
        CONNECT, AUTHENTICATE, READY, DISCONNECTED;
    }

    private final String serverName;
    private final String secret;
    private Status status;
    private String streamID;

    public XMPPStreamDecoder(String serverName, String secret) throws XMLStreamException {
        super();

        this.serverName = serverName;
        this.secret = secret;

        status = Status.CONNECT;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        /* TODO:
        if (e.getMessage() instanceof XMLEvent) {
            final XMLEvent event = (XMLEvent) e.getMessage();

            switch (status) {
            case CONNECT:
                if (event.isStartElement()) {
                    final StartElement element = event.asStartElement();

                    if (STREAM_NAME.equals(element.getName()) && XMPPNamespaces.ACCEPT.equals(element.getNamespaceURI(null))) {
                        if (!serverName.equals(element.getAttributeByName(new QName("from")).getValue())) {
                            throw new Exception("server name mismatch");
                        }
                        streamID = element.getAttributeByName(new QName("id")).getValue();

                        status = Status.AUTHENTICATE;
                        Channels.write(ctx.getChannel(), ChannelBuffers.copiedBuffer("<handshake>" + Hashing.sha1().hashString(streamID + secret, CharsetUtil.UTF_8).toString() + "</handshake>", CharsetUtil.UTF_8));
                    }
                } else {
                    throw new Exception("Expected stream:stream element");
                }
                break;
            case AUTHENTICATE:
            case READY:
                if (event.isEndElement()) {
                    final EndElement element = event.asEndElement();

                    if (STREAM_NAME.equals(element.getName())) {
                        Channels.disconnect(ctx.getChannel());
                        return;
                    }
                }
                break;
            case DISCONNECTED:
                throw new Exception("received DISCONNECTED");
            }
        }
        else if (e.getMessage() instanceof Match) {
            final Match element = (Match) e.getMessage();

            switch (status) {
            case AUTHENTICATE:
                if (!"handshake".equals(element.getTagName()))
                    throw new Exception("expected handshake");
                status = Status.READY;
                System.out.println("logged in");

                ctx.getPipeline().get(XMPPStreamHandler.class).loggedIn();

                break;
            case READY:
                final Stanza stanza = Stanza.fromElement(element);
                if (stanza == null)
                    throw new Exception("Unknown stanza");

                Channels.fireMessageReceived(ctx, stanza);
                break;
            default:
                throw new Exception("unexpected handleElement");
            }
        }
        else {
            ctx.sendUpstream(e);
        }
        */
    }

    @Override
    public void disconnectRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Channels.write(ctx, e.getFuture(), ChannelBuffers.copiedBuffer("</stream:stream>", CharsetUtil.UTF_8));
    }

}

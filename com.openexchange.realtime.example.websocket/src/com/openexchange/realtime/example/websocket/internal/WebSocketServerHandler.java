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

package com.openexchange.realtime.example.websocket.internal;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.example.websocket.StanzaSender;
import com.openexchange.realtime.example.websocket.WSHandler;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link WebSocketServerHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class WebSocketServerHandler extends SimpleChannelUpstreamHandler implements StanzaSender {

	    private static final String WEBSOCKET_PATH = "/rt";

	    private WebSocketServerHandshaker handshaker;
	    
	    
	    private final ServiceLookup services;
	    private final HandlerLibrary library;
	    private final WSChannel channel;
	    
		private ChannelHandlerContext ctx;
		private ServerSession session;
		private ID id;
		
		public WebSocketServerHandler(WSChannel channel, HandlerLibrary library, ServiceLookup services) {
			this.services = services;
			this.library = library;
			this.channel = channel;
		}
		

	    @Override
	    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
	        Object msg = e.getMessage();
	        if (msg instanceof HttpRequest) {
	            handleHttpRequest(ctx, (HttpRequest) msg);
	        } else if (msg instanceof WebSocketFrame) {
	            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
	        }
	    }

	    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
	        // Handshake
	        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
	                getWebSocketLocation(req), null, false);
	        handshaker = wsFactory.newHandshaker(req);
	        if (handshaker == null) {
	            wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
	        } else {
	            handshaker.handshake(ctx.getChannel(), req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
	            this.ctx = ctx;
	        }
	    }

	    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws OXException {
	    	// This is a good breakpoint to see how a message is handled
	    	
	        // Check for closing frame
	        if (frame instanceof CloseWebSocketFrame) {
	            handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
	            channel.removeHandler(this);
	            return;
	        } else if (frame instanceof PingWebSocketFrame) {
	            ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
	            return;
	        } else if (!(frame instanceof TextWebSocketFrame)) {
	            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
	                    .getName()));
	        }

	        // Send the uppercase string back.
	        String request = ((TextWebSocketFrame) frame).getText();
	        
	        Stanza stanza = RequestParser.parse(request);
	        
	        if (isInternal(stanza)) {
	        	handleInternally(stanza);
	        } else {
	        	stampStanza(stanza);
	        	dispatchStanza(stanza);
	        }
	        
	    }

	    
		private boolean isInternal(Stanza stanza) {
			return stanza.getNamespace().startsWith("ws:");
		}

		private void handleInternally(Stanza stanza) throws OXException {
			if ("ws:handshake".equals(stanza.getNamespace())) {
				getSession(stanza);
			}
		}

		private void stampStanza(Stanza stanza) {
			stanza.setFrom(id);
		}

		private void dispatchStanza(Stanza stanza) throws OXException {
			WSHandler transformer = library.getHandlerFor(stanza.getNamespace());
			if (transformer == null) {
				throw OXException.general("No transformer for namesapce "+stanza.getNamespace());
			}
			transformer.incoming(stanza, session);
		}
		
		public void sendStanza(Stanza stanza) throws OXException {
			WSHandler transformer = library.getHandlerFor(stanza.getNamespace());
			if (transformer == null) {
				throw OXException.general("No transformer for namespace "+stanza.getNamespace());
			}
			transformer.outgoing(stanza, session, this);
			
		}
		

		
		private void getSession(Stanza stanza) throws OXException {
			try {
				JSONObject json = (JSONObject) stanza.getPayload().getData();
				
				String sessionId = json.getString("session");
				
				Session session = services.getService(SessiondService.class).getSession(sessionId);
				if (session == null) {
					throw OXException.general("Invalid sessionId "+sessionId);
				}
				
				this.session = ServerSessionAdapter.valueOf(session);
				this.id = new ID("ws", session.getLoginName(), contextName(session.getLogin()), json.optString("resource"));
				this.channel.addHandler(this);
			} catch (JSONException x) {
				throw OXException.general(x.toString());
			}
			
		}


		private String contextName(String login) {
			int index = login.indexOf('@');
			if (index < 0) {
				return "";
			}
			return login.substring(index+1);
		}

		@Override
	    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
	        e.getCause().printStackTrace();
	        e.getChannel().close();
	    }

	    private static String getWebSocketLocation(HttpRequest req) {
	        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
	    }

		public ID getID() {
			return id;
		}


		@Override
        public void send(Stanza stanza) throws OXException {
			ctx.getChannel().write(new TextWebSocketFrame(RequestWriter.write(stanza).toString()));
		}


}

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

package com.openexchange.realtime.example.telnet.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.openexchange.authentication.Cookie;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.realtime.example.telnet.TelnetChatMessage;
import com.openexchange.realtime.example.telnet.TelnetMessageDelivery;
import com.openexchange.realtime.packet.ID;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link TelnetChatDecoder}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 */
public class TelnetChatDecoder extends SimpleChannelUpstreamHandler implements TelnetMessageDelivery{

	private enum State {
		INITIAL, HEADERS_DONE, CONTENT_DONE;
	}

	private final Map<String, String> headers = new HashMap<String, String>();
	private final StringBuilder payload = new StringBuilder();
	private final TelnetChannel channel;

	private State state = State.INITIAL;
	private final ExtensibleTelnetMessageHandler messageHandler;
	private ServerSession session;
	private ID id;
	private ChannelHandlerContext ctx;

	/**
	 * Initializes a new {@link TelnetChatDecoder}.
	 * 
	 * @param services
	 */
	public TelnetChatDecoder(ExtensibleTelnetMessageHandler messageHandler, TelnetChannel channel) {
		super();
		this.messageHandler = messageHandler;
		this.channel = channel;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		this.ctx = ctx;
		super.channelConnected(ctx, e);
	}
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		this.ctx = null;
		// TODO: Logout
		
		channel.forgetDelivery(this.id);
		
		super.channelDisconnected(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		String line = (String) e.getMessage();

		switch (state) {
		case INITIAL:
			parseHeader(line);
			break;
		case HEADERS_DONE:
			appendPayload(line);
			break;
		}

		if (state == State.CONTENT_DONE) {
			// Do something with the message;
			// This is a good breakpoint to see how a message is handled
			TelnetChatMessage message = new TelnetChatMessage(payload.toString(),
					headers, id, session);

			if (isInternalMessage(message)) {
				handleInternally(message, ctx);

			} else {
				messageHandler.handle(message);
			}

			headers.clear();
			payload.setLength(0);
			state = State.INITIAL;

		}
	}

	private boolean isInternalMessage(TelnetChatMessage message) {
		return message.getHeader("service").equals("authentication");
	}

	private void handleInternally(TelnetChatMessage message,
			ChannelHandlerContext ctx) throws OXException {
		// Log in

		final String user = message.getHeader("login");
		final String password = message.getHeader("password");
		final String resource = message.getHeader("resource");

		LoginResult result = LoginPerformer.getInstance().doLogin(
				new LoginRequest() {

					@Override
                    public String getLogin() {
						return user;
					}

					@Override
                    public String getPassword() {
						return password;
					}

					@Override
                    public String getClientIP() {
						return "";
					}

					@Override
                    public String getUserAgent() {
						return "chat";
					}

					@Override
                    public String getAuthId() {
						return UUIDs.getUnformattedString(UUID.randomUUID());
					}

					@Override
                    public String getClient() {
						return "chat";
					}

					@Override
                    public String getVersion() {
						return "";
					}

					@Override
                    public String getHash() {
						return "";
					}

					@Override
                    public Interface getInterface() {
						return Interface.HTTP_JSON;
					}

					@Override
                    public Map<String, List<String>> getHeaders() {
						return Collections.emptyMap();
					}

					@Override
                    public Cookie[] getCookies() {
						return new Cookie[0];
					}

				});

		this.session = ServerSessionAdapter.valueOf(result.getSession());
		this.id = new ID("plain", session.getLoginName(), contextName(user), resource); // FIXME: what about the 'defaultcontext'? 
		
		channel.registerDelivery(id, this);
		TelnetChatMessage welcome = new TelnetChatMessage("Welcome "+id+"\n", id, session);
		welcome.setHeader("session", session.getSessionID());
		
		deliver(welcome);
		
	}

	private String contextName(String user) {
		int index = user.lastIndexOf('@');
		if (index < 0) {
			return "";
		}
		return user.substring(index+1);
	}

	private void parseHeader(String line) {
		if (line.equals("")) {
			state = State.HEADERS_DONE;
			return;
		}
		int index = line.indexOf(':');
		String header = line.substring(0, index);
		String value = line.substring(index + 1);

		headers.put(header.trim(), value.trim());
	}

	private void appendPayload(String line) {
		if (line.equals("")) {
			state = State.CONTENT_DONE;
			return;
		}
		payload.append(line).append('\n');

	}

	@Override
	public void deliver(TelnetChatMessage message) throws OXException {
		StringBuilder b = new StringBuilder();
		
		for(Map.Entry<String, String> header: message.getHeaders().entrySet()) {
			b.append(header.getKey()+": "+header.getValue()+"\n");
		}
		b.append("from: ").append(message.getFrom().toString()).append('\n');
		b.append("\n");
		b.append(message.getPayload());
		b.append("\n");
		
		ctx.getChannel().write(b.toString());
	}

}

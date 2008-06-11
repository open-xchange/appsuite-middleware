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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.server.services;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.ajax.requesthandler.AJAXRequestHandler;

/**
 * {@link ServerRequestHandlerRegistry} - A registry for request handlers
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ServerRequestHandlerRegistry {

	private static final ServerRequestHandlerRegistry INSTANCE = new ServerRequestHandlerRegistry();

	/**
	 * Gets the server's request handler registry
	 * 
	 * @return The server's request handler registry
	 */
	public static ServerRequestHandlerRegistry getInstance() {
		return INSTANCE;
	}

	private final Map<String, AJAXRequestHandler> requestHandlers;

	/**
	 * Initializes a new {@link ServerRequestHandlerRegistry}
	 */
	private ServerRequestHandlerRegistry() {
		super();
		requestHandlers = new ConcurrentHashMap<String, AJAXRequestHandler>();
	}

	/**
	 * Clears the whole registry
	 */
	public void clearRegistry() {
		requestHandlers.clear();
	}

	/**
	 * Removes a request handler bound to given registration name from this
	 * registry
	 * 
	 * @param registrationName
	 *            The registration name
	 */
	public void removeService(final String registrationName) {
		requestHandlers.remove(registrationName);
	}

	/**
	 * Adds a request handler bound to given registration name to this registry
	 * 
	 * @param registrationName
	 *            The registration name
	 * @param requestHandler
	 *            The request handler
	 */
	public void addService(final String registrationName, final AJAXRequestHandler requestHandler) {
		requestHandlers.put(registrationName, requestHandler);
	}

	/**
	 * Gets the request handler by given registration name
	 * 
	 * @param registrationName
	 *            The registration name
	 * @return The request handle if present; otherwise <code>null</code>
	 */
	public AJAXRequestHandler getService(final String registrationName) {
		return requestHandlers.get(registrationName);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(256);
		sb.append("Server request handler registry:\n");
		if (requestHandlers.isEmpty()) {
			sb.append("<empty>");
		} else {
			final Iterator<Map.Entry<String, AJAXRequestHandler>> iter = requestHandlers.entrySet().iterator();
			while (true) {
				final Map.Entry<String, AJAXRequestHandler> e = iter.next();
				sb.append(e.getKey()).append(": ").append(e.getValue().getClass().getName());
				if (iter.hasNext()) {
					sb.append('\n');
				} else {
					break;
				}
			}
		}
		return sb.toString();
	}

}

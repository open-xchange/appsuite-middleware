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

package com.openexchange.realtime;

import java.util.Set;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Channel} - Represents a communication channel for transmitting messages.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public interface Channel {
	
	/**
	 * Gets the protocol identifying this channel.
	 * 
	 * @return The protocol name
	 */
	public String getProtocol();
	
	/**
	 * Checks if this channel can dispatch a specific Stanza class to a given recipient.
	 * 
	 * @param namespaces The namespaces of the payloads contained in the Stanza that has to be handled by this Channel.
	 * @param recipient The recipient
	 * @param session The session
	 * @return <code>true</code> if this channel can dispatch messages to given recipient; otherwise <code>false</code>
	 * @throws OXException If check fails for any reason
	 */
	public boolean canHandle(Set<String> namespaces, ID recipient, ServerSession session) throws OXException;
	
	/**
	 * Gets the priority used for building a ranking for concurrent channels.
	 * 
	 * @return The priority
	 */
	public int getPriority();
	
	/**
	 * Checks if this channel is connected to given end point identifier.
	 * 
	 * @param id The end point identifier
	 * @param session The session
	 * @return <code>true</code> if this channel is connected to given end point identifier; otherwise <code>false</code>
	 * @throws OXException If check fails for any reason
	 */
	public boolean isConnected(ID id, ServerSession session) throws OXException;
	
	/**
	 * Sends specified stanza.
	 * 
	 * @param stanza The stanza to send
	 * @param session The session
	 * @throws OXException If send operation fails for any reason
	 */
	public void send(Stanza stanza, ServerSession session) throws OXException;
}

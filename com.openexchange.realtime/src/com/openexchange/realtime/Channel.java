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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import com.openexchange.realtime.util.ElementPath;

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
	 * @param elementPaths The elementPaths of the payloads contained in the Stanza that has to be handled by this Channel.
	 * @param recipient The recipient
	 * @return <code>true</code> if this channel can dispatch messages to given recipient; otherwise <code>false</code>
	 * @throws OXException If check fails for any reason
	 */
	public boolean canHandle(Set<ElementPath> elementPaths, ID recipient) throws OXException;

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
	 * @return <code>true</code> if this channel is connected to given end point identifier; otherwise <code>false</code>
	 * @throws OXException If check fails for any reason
	 */
	public boolean isConnected(ID id) throws OXException;

	/**
	 * Have the channel try to create a recipient. If the channel can initiate a connection to the recipient, this method should do so and return true, if
	 * it can not initiate a connection to the recipient it should return false. After a successful conjuration, {@link #isConnected(ID)} should return true and {@link #send(Stanza)} with
	 * the given recipient should succeeed.
	 * 
	 * @param id The id to try to connect to
	 * @return true if the connection could be established, false otherwise
	 * @throws OXException
	 */
	public boolean conjure(ID id) throws OXException;
	
	/**
	 * Sends specified stanza.
	 *
	 * @param stanza The stanza to send
	 * @param recipient The recipient that shall receive the stanza
	 * @throws OXException If send operation fails for any reason
	 */
	public void send(Stanza stanza, ID recipient) throws OXException;
	
	
}

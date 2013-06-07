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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.realtime.client;

import org.json.JSONValue;

/**
 * A {@link RTConnection} is the interface for establishing RT connections to post and receive {@link Stanza}s.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface RTConnection {

    /**
     * Establishes a connection to the OX RT component. This includes creating
     * a valid user session.
     *
     * @param messageHandler The message handler that is called on received messages.
     * If <code>null</code>, incoming messages will be discarded immediately.
     * @return The clients resource identifier.
     * @throws RTException
     */
     RTUserState connect(RTMessageHandler messageHandler) throws RTException;
     
     /**
      * Establishes a connection to the OX RT component. This includes creating
      * a valid user session.
      *
      * @param messageHandler The message handler that is called on received messages.
      * If <code>null</code>, incoming messages will be discarded immediately.
      * @param changeListener A listener that will be invoked upon RTUserState changes
      * @return The clients resource identifier.
      * @throws RTException
      */
      RTUserState connect(RTMessageHandler messageHandler, RTUserStateChangeListener changeListener) throws RTException;

    /**
     * Sends a message to the server.
     * It's not guaranteed that the message arrives. This call returns immediately.
     *
     * @param message The message.
     * @throws RTException
     */
    void post(JSONValue message) throws RTException;

    /**
     * Sends a message to the server. This method must be used for reliable delivery.
     * The call returns after the message was successfully delivered.
     *
     * @param message The message.
     * @throws RTException
     */
    void postReliable(JSONValue message) throws RTException;

    /**
     * Closes the connection and frees all resources. The underlying user session will also be closed.
     *
     * @throws RTException
     */
    void close() throws RTException;

}

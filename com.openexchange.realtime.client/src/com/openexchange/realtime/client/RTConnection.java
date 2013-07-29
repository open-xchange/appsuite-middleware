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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONObject;

/**
 * A {@link RTConnection} is the interface for establishing RT connections to post and receive {@link Stanza}s.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface RTConnection {

    /**
     * Returns the OX session owned by this connection instance. If the connection is already closed or could not be established
     * an {@link IllegalStateException} will be thrown.
     */
    RTSession getOXSession();

    /**
     * Registers a new {@link RTMessageHandler} for the given selector. To ensure that no message gets lost, it's best practice to
     * register message handlers before calling {@link #login(RTMessageHandler)}.
     *
     * @param selector The selector
     * @param messageHandler The message handler
     * @throws RTException if it would overwrite an already existing MessageHandler for the given selector.
     */
    void registerHandler(String selector, RTMessageHandler messageHandler) throws RTException;

    /**
     * Remove the message handler that is associated with the given selector from the connection to let it know that we aren't interested in
     * further messages.
     *
     * @param selector The selector
     */
    void unregisterHandler(String selector);

    /**
     * Sends a message to the server.
     * It's not guaranteed that the message arrives. This call returns immediately.
     *
     * @param message The message.
     * @throws RTException
     */
    void post(JSONObject message) throws RTException;

    /**
     * Sends a message to the server. This method must be used for reliable delivery.
     *
     * @param message The message.
     * @throws RTException
     */
    void send(JSONObject message) throws RTException;

    /**
     * Sends a message to the server. This method must be used for reliable delivery.
     *
     * @param message The message.
     * @throws RTException
     * @throws InterruptedException
     */
    void sendBlocking(JSONObject message, long timeout, TimeUnit unit) throws RTException, TimeoutException, InterruptedException;

    /**
     * Closes the connection and frees all resources. The underlying user session will also be closed.
     *
     * @throws RTException
     */
    void close() throws RTException;

}

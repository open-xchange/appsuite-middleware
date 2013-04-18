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

package com.openexchange.realtime.test.loadtest;

import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.group.GroupDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.ActionHandler;
import com.openexchange.realtime.util.ElementPath;

/**
 * Receives the message sent by the client, processes it and returns an updated version to the client
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class LoadTestDispatcher extends GroupDispatcher implements ComponentHandle {

    /**
     * Logger
     */
    private static final org.apache.commons.logging.Log LOG = Log.loggerFor(LoadTestDispatcher.class);

    /**
     * An introspecting handler that allows clients to formulate stanzas that call all handle methods
     */
    private static final ActionHandler handler = new ActionHandler(LoadTestDispatcher.class);

    /**
     * Initializes a new {@link LoadTestDispatcher}.
     * 
     * @param id - the identifier of the endpoint
     */
    public LoadTestDispatcher(final ID id) {
        super(id, handler);
    }

    /**
     * Method that is called from the sent stanza. The method just set a timestamp into the received stanza to receipt the sustaining of the
     * message
     * 
     * @param stanza - the Stanza that was send by the client
     * @throws OXException
     */
    public void handleShoutInTheRoom(final Stanza stanza) throws OXException {

        final StringBuilder message = new StringBuilder();
        // We're iterating over all messages that are constructed with the china.message element path
        for (final PayloadTree messages : stanza.getPayloads(new ElementPath("loadTest", "sentData"))) {
            message.append(messages.toString().replace("_____", System.currentTimeMillis() + "_____"));
        }

        // Send the message to all participants in the chat (including the one who said it originally
        this.sendToAll(stanza, message.toString());
    }

    /**
     * Prepares the message which should be sent to the client and relays it to all.
     * 
     * @param stanza - original stanza that was received from the client
     * @param returnMessage - String with the message that should be returned to the client
     * @throws OXException
     */
    private void sendToAll(final Stanza stanza, final String returnMessage) throws OXException {
        final Message message = new Message();

        message.setFrom(stanza.getFrom());
        message.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(
            new PayloadElement(stanza.getFrom().getUser() + " shoutet: " + returnMessage, "string", "loadTest", "message")).build()));

        this.relayToAll(message);
    }

}

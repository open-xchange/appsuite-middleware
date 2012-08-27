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

package com.openexchange.realtime.atmosphere.presence;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.atmosphere.OXRTHandler;
import com.openexchange.realtime.atmosphere.StanzaSender;
import com.openexchange.realtime.example.presence.PresenceStatus;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.tools.session.ServerSession;

/*
 * Build OXRTHandler(s) and SimpleConverters to handle 




 */

public class OXRTPresenceHandler implements OXRTHandler {

    @Override
    public String getNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void incoming(Stanza stanza, ServerSession session) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * Transport status changes and subscribe requests
     */
    @Override
    public void outgoing(Stanza stanza, ServerSession session, StanzaSender sender) throws OXException {
        // TODO Auto-generated method stub

    }

    /**
     * Update the PresenceStatus of a client. This involves several steps:
     * <ol>
     * <li>Set status in central status registry</li>
     * <li>Notify the user about the successful status update by sending him the new status back</li>
     * <li>Get active users from the roster of the client that sent the status update</li>
     * <li>Notify active users about the status update</li>
     * </ol>
     * 
     * @param client The client that sent a new PresenceStatus
     * @param status The new PresenceStatus of the client that has to be set
     * @param session The server session associated with the update request
     */
    public void updatePresenceStatus(ID client, PresenceStatus status, ServerSession session) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Get a list of clients a given client is subscribed to from the PresenceSubscriptionService and query their status from the
     * PresenceStatus.
     * 
     * @param requester The client requesting thes status map of clients he is subscribed to
     * @param session The associated session
     * @return a map of clients and associated status that a given client is subscribed to
     */
    public IDMap<PresenceStatus> getSubscriptionStatus(ID requester, ServerSession session) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Ask someone else for permission to subscribe/see his status. Done by triggering a method on the PresenceSubscriptionService.
     * 
     * @param requester The client asking for permission to subscribe to someone else
     * @param receiver The client the requester wants to ask for permission to subscribe
     * @param session The associated server session
     */
    public void askForSubscriptionAuthorization(ID requester, ID receiver, ServerSession session) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Give somebody else the permission to subscribe to me and see my status. This will persist the permission in the
     * PresenceSubscriptionService.
     * 
     * @param granter The client granting someone the permission to subscribe
     * @param receiver The client asking for permission to subscribe
     * @param session The associated serversession
     */
    public void grantSubscriptionAUthorization(ID granter, ID receiver, ServerSession session) {

    }

}

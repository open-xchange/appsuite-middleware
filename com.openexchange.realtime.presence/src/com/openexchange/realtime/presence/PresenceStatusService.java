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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.realtime.presence;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link PresenceStatusService} - Service to store and query the PresenceStatus of currently connected clients.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public interface PresenceStatusService {

    /**
     * Register a new PresenceChangeListener
     * @param presenceChangeListener the new PresenceChangeListener
     */
    void registerPresenceChangeListener(PresenceChangeListener presenceChangeListener);

    /**
     * Unregister an already registered PresenceChangeListener
     * @param presenceChangeListener the already registered PresenceChangeListener
     */
    void unregisterPresenceChangeListener(PresenceChangeListener presenceChangeListener);

    /**
     * Change the PresenceStatus of an ID. This involves:
     * <ol>
     * <li>Set status in central status registry</li>
     * <li>Notify the user about the successful status update by sending him the new status back</li>
     * <li>Get subscribed and active users from the roster of the client that sent the status update</li>
     * <li>Notify users about the status update</li>
     * <li>Stores the last time ID changed its PresenceStaus <delay xmlns="urn:xmpp:dely"></li>
     * </ol>
     *
     * @param client    Client that wants to change its PresenceStatus
     * @param status    The new PresenceData
     * @param session   The associated ServerSession
     * @throws OXException If changing the PresenceStatus fails
     */
    void changePresenceStatus(Presence stanza) throws OXException;

    /**
     * Get the current PresenceStatus of only one ID.
     *
     * @param id The ID whose PresenceStatus should be queried
     * @return The current PresenceStatus of ID, Offline if no information can be found for the ID
     * @throws OXException
     */
    public PresenceData getPresenceStatus(ID id) throws OXException;

    /**
     * Get the current PresenceStatus of one or more IDs.
     *
     * @param ids The IDs whose PresenceStatus should be queried
     * @return The current PresenceStatus of IDs, Offline if no information can be found for the ID
     * @throws OXException
     */
    public IDMap<PresenceData> getPresenceStatus(Collection<ID> ids) throws OXException;
}

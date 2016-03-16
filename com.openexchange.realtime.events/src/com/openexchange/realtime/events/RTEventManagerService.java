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

package com.openexchange.realtime.events;

import java.util.Map;
import java.util.Set;
import com.openexchange.realtime.packet.ID;
import com.openexchange.session.Session;

/**
 * {@link RTEventManagerService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface RTEventManagerService {


    /**
     * Gets the sum total of all supported events. These events are namespaced per known emitter.
     */
    public abstract Set<String> getSupportedEvents();

    /**
     * Subscribe to a namespaced event
     * @param event The name of the event, complete with the namespace eg. calendar:new
     * @param selector An arbitrary string that will be sent along with all event stanzas as their selector
     * @param id The ID of the client that wants to be notified of events
     * @param session The session of the client
     * @param parameters Arbitrary parameters that may or may not mean something to the {@link RTEventEmitterService} instance associated with the namespace
     */
    public abstract void subscribe(String event, String selector, ID id, Session session, Map<String, String> parameters);

    /**
     * Retrieve all namespaced event names a given ID is subscribed to
     */
    public abstract Set<String> getSubscriptions(ID id);

    /**
     * Unsubscribe from all events this ID subscribed to.
     */
    public abstract void unsubscribe(ID id);

    /**
     * Unsubscribe from a specific event this ID subscribed to
     */
    public abstract void unsubscribe(String event, ID id);

}

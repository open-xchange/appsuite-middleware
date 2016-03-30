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

import java.util.Set;


/**
 * Provide an {@link RTEventEmitterService} via OSGi to contribute event emitters to the framework. Whenever a backend bundle
 * is supposed to act as an event source, implement this interface.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface RTEventEmitterService {
    
    /**
     * Provide a namespace for the events. Clients can register for events my joining the namespace with a colon and the name of one of the
     * supported events. E.g. this RTEventEmitter is of the namespace calendar and supports the events 'new', 'updated', 'deleted', then clients can subscribe
     * to calendar:new, calendar:updated and calendar:deleted events.
     * @return
     */
    String getNamespace();

    /**
     * Get a list of supported events (without the namespace). E.g. 'new', 'updated' and 'deleted'
     * @return
     */
    Set<String> getSupportedEvents();

    /**
     * Used to register an event listener for a given (non-namespaced) event
     * @param eventName e.g. 'new'
     * @param listener The listener this emitter will trigger when the event occurs
     */
    void register(String eventName, RTListener listener);
    
    /**
     * Called to unregister a listener, either because no one is listening or the associated client 
     * has gone offline.
     * @param eventName e.g. 'new'
     * @param listener The listener to unregister
     */
    void unregister(String eventName, RTListener listener);

}

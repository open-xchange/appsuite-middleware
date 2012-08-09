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

package com.openexchange.mdns;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link MDNSService} - The <a href="http://en.wikipedia.org/wiki/MDNS">mDNS</a> service.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MDNSService {

    /**
     * Lists all resolved services by given service identifier.
     * 
     * @param serviceId The service identifier; such as <code>"openexchange.messaging.service"</code>.
     * @return A list containing all resolved services matching given service identifier.
     * @throws OXException If listing fails
     */
    List<MDNSServiceEntry> listByService(String serviceId) throws OXException;

    /**
     * Adds a listener.
     * 
     * @param listener The listener to add
     */
    void addListener(MDNSServiceListener listener);

    /**
     * Removes a previously added listener
     * 
     * @param listener The listener to remove
     */
    void removeListener(MDNSServiceListener listener);

    /**
     * Registers a new service.
     * 
     * @param serviceId The service identifier; such as <code>"openexchange.messaging.service"</code>.
     * @param port The service port
     * @param info The service information; arbitrary text such as <code>"path=index.html"</code>
     * @throws OXException If registration fails
     */
    MDNSServiceInfo registerService(String serviceId, int port, String info) throws OXException;

    /**
     * Unregisters the service identifier by given service information.
     * 
     * @param serviceInfo The service information identifying the service
     * @throws OXException If service cannot be unregistered
     */
    void unregisterService(MDNSServiceInfo serviceInfo) throws OXException;

}

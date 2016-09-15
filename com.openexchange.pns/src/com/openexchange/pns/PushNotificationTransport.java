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

package com.openexchange.pns;

import java.util.Collection;
import com.openexchange.exception.OXException;

/**
 * {@link PushNotificationTransport} - Transports specified events to the push service end-point.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface PushNotificationTransport {

    /**
     * Checks if this transport is enabled for specified topic, client and user.
     *
     * @param topic The topic that is about to be sent
     * @param client The identifier of the client to send to
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isEnabled(String topic, String client, int userId, int contextId) throws OXException;

    /**
     * Transports a notification to the service provider
     *
     * @param notification The notification to transport
     * @param matches The associated matches
     * @throws OXException If given notification cannot be transported
     */
    void transport(PushNotification notification, Collection<PushMatch> matches) throws OXException;

    /**
     * Gets this service's identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Checks if this transport serves the client denoted by given identifier
     *
     * @param client The client identifier
     * @return <code>true</code> if this transport serves the client; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean servesClient(String client) throws OXException;

}

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

package com.openexchange.realtime.dispatch;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;

/**
 * The Message dispatcher chooses an appropriate {@link Channel} to push data (aka. a Stanza) to listening clients
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface MessageDispatcher {

    /**
     * Delivers a {@link Stanza} to a list of given recipients.
     *
     * @param stanza The stanza to send
     * @return A map of IDs that could not be reached because of an occurred exception.
     * @throws OXException If send operation fails for any reason
     */
    Map<ID, OXException> send(Stanza stanza, IDMap<Resource> recipients) throws OXException;

    /**
     * Delivers a stanza using the resource directory to resolve the recipients
     *
     * @param stanza The Stanza to send
     * @throws OXException when delivery fails
     */
    void send(Stanza stanza) throws OXException;

    /**
     * Send a message and synchronously waits for a response. The recipient is supposed to send exactly one Stanza
     * back to the 'from' ID. The 'from' ID is generated in this method.
     */
    Stanza sendSynchronously(Stanza stanza, long timeout, TimeUnit unit) throws OXException;
}

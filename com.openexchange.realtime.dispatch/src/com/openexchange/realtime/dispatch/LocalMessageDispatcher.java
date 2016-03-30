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
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;


/**
 * {@link LocalMessageDispatcher}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface LocalMessageDispatcher {
    
    /**
     * Push a {@link Stanza} to a set of given recipients. The recipients must be reachable locally.
     * That means the corresponding {@link ID} must have the resource field set and that resource must be
     * registered on this node.
     *
     * @param stanza The stanza to send
     * @param recipients The local recipients for this stanza
     * @return A map of IDs that could not be reached because of an occurred exception.
     * @throws OXException If send operation fails for any reason
     */
    public Map<ID, OXException> send(Stanza stanza, Set<ID> recipients) throws OXException;
    
    /**
     * Add a Channel that can be used to send Stanzas to this MessageDispatcher
     * @param channel a Channel that can be used to send Stanzas
     */
    public void addChannel(final Channel channel);

    /**
     * Remove a Channel that can be used to send Stanzas from this MessageDispatcher
     * @param channel a Channel that can be used to send Stanzas
     */
    public void removeChannel(final Channel channel);

}

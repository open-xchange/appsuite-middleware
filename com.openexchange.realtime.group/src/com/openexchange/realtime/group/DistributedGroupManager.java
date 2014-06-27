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

package com.openexchange.realtime.group;

import java.util.Collection;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.util.Duration;


/**
 * {@link DistributedGroupManager} - Allows acces to the distributed group infos stored in Hazelcast.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public interface DistributedGroupManager {

    /**
     * Adds a client <-> group mapping to this manager
     * 
     * @param selectorChoice The {@link SelectorChoice} containing the needed client, group and selector
     * @return True if the mapping was added to the manager, false otherwise.
     * @throws IllegalStateException if any parameter is null
     */
    boolean add(SelectorChoice selectorChoice)  throws OXException;

    /**
     * Remove all client <-> group mappings for a given client {@link ID}.
     * 
     * @param client The client {@link ID}
     * @return A {@link Collection} of groups {@link ID}s that the client was member of
     */
    Collection<ID> remove(ID client)  throws OXException;

    /**
     * Remove a single client <-> group mapping.
     *
     * @param selectorChoice The {@link SelectorChoice} containing the needed client, group and selector
     * @return true if the client <-> group mapping was removed from the manager, false otherwise.
     * @throws IllegalStateException if any parameter is null
     */
    boolean remove(SelectorChoice selectorChoice) throws OXException;

    /**
     * Get the Groups that a given client is a member of.
     * @param id The client {@link ID}
     * @return The Groups that a given client is a member of.
     */
    Set<ID> getGroups(ID id) throws OXException;

    /**
     * Get the current members of a given GroupDispatcher.
     * 
     * @param id The {@link GroupDispatcher}'s {@link ID}
     * @return The current members of a given GroupDispatcher.
     */
    Set<ID> getMembers(ID id)  throws OXException;

    /**
     * Set the duration of inactivity for a given client. The GroupManagerService will inform all Groups the client had joined previously
     * about the inactivity of the client.
     * 
     * @param id The client {@link ID}
     * @param duration The {@link Duration} of inactivity
     */
    void setInactivity(ID id, Duration duration) throws OXException;

}

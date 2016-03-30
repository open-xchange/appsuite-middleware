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

package com.openexchange.realtime.group;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.group.commands.LeaveCommand;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.util.Duration;


/**
 * {@link DistributedGroupManager} - Allows acces to the distributed client, group infos stored in Hazelcast. It tracks client -> groups(
 * {@link SelectorChoice} actually) and group -> members({@link SelectorChoice} actually) mappings. See {@link SelectorChoice} for more details.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public interface DistributedGroupManager {

    /**
     * Adds client -> group({@link SelectorChoice} actually), group -> client({@link SelectorChoice} actually) mapping to this manager.
     * 
     * @param selectorChoice The {@link SelectorChoice} containing the needed client, group and selector to add the mappings
     * @return True if the mapping was added to the manager, false otherwise
     * @throws IllegalStateException if any parameter is null
     */
    boolean addChoice(SelectorChoice selectorChoice) throws OXException;

    /**
     * Remove single clientX -> groupY({@link SelectorChoice} actually), groupY -> clientX({@link SelectorChoice} actually) mappings without
     * sending a LeaveCommand
     * 
     * @param selectorChoice The {@link SelectorChoice} containing the needed client, group and selector
     * @return true if the client <-> group mappings were removed from the manager, false otherwise
     * @throws IllegalStateException if any parameter is null
     */
    boolean removeChoice(SelectorChoice selectorChoice) throws OXException;

    /**
     * Remove a single client -> group ({@link SelectorChoice} actually) mapping
     * 
     * @param selectorChoice contains all needed infos to remove the mapping
     * @return true if the mapping could be removed, false otherwise
     */
    boolean removeClientToSelectorChoice(SelectorChoice selectorChoice) throws OXException;

    /**
     * Remove a single group -> member({@link SelectorChoice} actually) mapping
     * 
     * @param selectorChoice contains all needed infos to remove the mapping
     * @return true if the mapping could be removed, false otherwise
     */
    boolean removeGroupToSelectorChoice(SelectorChoice selectorChoice) throws OXException;

    /**
     * Remove all client -> group mappings for a given client {@link ID}. This will send a {@link LeaveCommand} to all GroupDispatchers that
     * hold the client as member and thus remove the group -> client mapping.
     * 
     * @param client The client {@link ID}
     * @return A {@link Collection} of groups {@link ID}s that the client was member of
     */
    Collection<? extends SelectorChoice> removeClient(ID client) throws OXException;

    /**
     * Remove all group -> member mappings for a given group {@link ID} and additionally remove all client -> group mappings of affected
     * members. This will send a {@link NotMember} to all members of that group.
     * 
     * @param group The group {@link ID}
     * @return A collection of {@link SelectorChoice}s representing the previous members of the group
     */
    Collection<? extends SelectorChoice> removeGroup(ID group) throws OXException;

    /**
     * Get the groups that a given client is a member of.
     * @param id The client {@link ID}
     * @return The {@link SelectorChoices} representing the joined groups.
     */
    Collection<? extends SelectorChoice> getGroups(ID id) throws OXException;

    /**
     * Get the current members of a given GroupDispatcher.
     * 
     * @param id The {@link GroupDispatcher}'s {@link ID}
     * @return The {@link SelectorChoices} representing the members that joined the group.
     */
    Collection<? extends SelectorChoice> getMembers(ID id)  throws OXException;

    /**
     * Set the duration of inactivity for a given client. The GroupManagerService will inform all Groups the client had joined previously
     * about the inactivity of the client.
     * 
     * @param id The client {@link ID}
     * @param duration The {@link Duration} of inactivity
     */
    void setInactivity(ID id, Duration duration) throws OXException;

}

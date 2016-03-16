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

package com.openexchange.realtime.directory;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link ResourceDirectory}
 *
 * Directory for presence resources.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface ResourceDirectory {

    /**
     * Registers a new change listener that will receive notifications on directory updates.
     *
     * @param listener The listener to add
     */
    void addListener(ChangeListener listener);

    /**
     * Removes a previously registered listener.
     *
     * @param listener The listener to remove
     */
    void removeListener(ChangeListener listener) throws OXException;

    /**
     * Gets all available resources for the supplied ID, or an empty map if no matching resources are known. If the ID is in general
     * form, multiple entries may be returned, each mapped to their concrete ID. If the ID denotes a concrete resource, i.e. it's
     * resource-part is set, only one entry is returned if found.
     *
     * @param id The ID to lookup the status for
     * @return The resolved resources matching the supplied ID
     * @throws OXException
     */
    IDMap<Resource> get(ID id) throws OXException;

    /**
     * Gets all available resources for the supplied IDs, or an empty map if no matching resources are known. For each supplied ID, if
     * it is in general form, multiple entries may be returned, each mapped to their concrete ID. If a supplied ID denotes a concrete
     * resource, i.e. it's resource-part is set, only one entry is returned for that ID if found.
     *
     * @param ids The IDs to lookup the status for
     * @return The resolved resources matching the supplied ID
     * @throws OXException
     */
    IDMap<Resource> get(Collection<ID> ids) throws OXException;

    /**
     * Sets or updates the presence data of a resource identified by the supplied ID.
     *
     * @param id The (concrete) resource ID to set the status for
     * @param resource The resource data to set
     * @return The previously associated resource of the ID in case of an update, <code>null</code> if there was no value associated
     *         with the ID before
     * @throws OXException
     */
    Resource set(ID id, Resource resource) throws OXException;
    
    /**
     * Sets or updates the presence data of a resource identified by the supplied ID if and only if no previous resource was set for this id.
     *
     * @param id The (concrete) resource ID to set the status for
     * @param resource The resource data to set
     * @return The previously associated resource of the ID in case of an update, <code>null</code> if there was no value associated
     *         with the ID before. If a non-null value is returned here, the resource directory has not saved the supplied value.
     * @throws OXException
     */
    Resource setIfAbsent(ID id, Resource resource) throws OXException;

    /**
     * Removes all available resources for the supplied ID. If the ID is in general form, all matching entries are removed from the
     * directory. If the ID denotes a concrete resource, i.e. it's resource-part is set, only one entry is removed if found.
     *
     * @param id The ID to remove from the directory
     * @return All previously associated resources mapped to the ID, each mapped to the it's concrete resource ID.
     * @throws OXException
     */
    IDMap<Resource> remove(ID id) throws OXException;

    /**
     * Removes all available resources for the supplied IDs. For each ID, if it is in general form, all matching entries are removed
     * from the directory. If an ID denotes a concrete resource, i.e. it's resource-part is set, only one entry is removed if found.
     *
     * @param ids The IDs to remove from the directory
     * @return All previously associated resource mapped to the IDs, each mapped to the it's concrete resource ID.
     * @throws OXException
     */
    IDMap<Resource> remove(Collection<ID> ids) throws OXException;
    
    /**
     * Get the most recent PresenceState with a positive Priority iow. that wants to receive messages.
     * @return null or the most recent PresenceState with a positive Priority
     * @throws OXException when the Presence lookup fails
     */
    Presence getPresence(ID id) throws OXException;

}

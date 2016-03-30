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

package com.openexchange.realtime.cleanup;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.ID;

/**
 * {@link GlobalRealtimeCleanup} - A central service that can be used for cluster-wide cleanup tasks of realtime components. The realtime
 * framework has to manage various states at different places. Under certain conditions e.g. a client leaves or a {@link GroupDispatcher}
 * gets disposed those states have to be cleaned. Therefore the {@link GlobalRealtimeCleanup} instructs all {@link LocalRealtimeCleanups}s
 * located on the different nodes of the cluster to cleanup.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public interface GlobalRealtimeCleanup extends RealtimeCleanup {

    /**
     * Removes all available ResourceDirectory entries for the supplied ID. If the ID is in general form, all matching entries are removed
     * from the directory. If the ID denotes a concrete resource, i.e. it's resource-part is set, only one entry is removed if found.
     * 
     * @param id The ID to remove from the directory
     * @return The id's that were removed or an empty Collection
     * @throws OXException
     */
    public Collection<ID> removeFromResourceDirectory(ID id) throws OXException;

    /**
     * Clean up states that were kept for the given id but only if the timestamp didn't change in the meantime 
     * 
     * @param id The {@link ID} to clean up
     * @param stamp The timestamp of the ID at the time the cleanup was initiated.
     */
    public void cleanForId(ID id, long timestamp);

    /**
     * Removes all available ResourceDirectory entries for the supplied IDs. For each ID, if it is in general form, all matching entries are
     * removed from the directory. If an ID denotes a concrete resource, i.e. it's resource-part is set, only one entry is removed if found.
     *
     * @param ids The IDs to remove from the directory
     * @return The id's that were removed or an empty Collection
     * @throws OXException
     */
    public Collection<ID> removeFromResourceDirectory(Collection<ID> ids) throws OXException;

}

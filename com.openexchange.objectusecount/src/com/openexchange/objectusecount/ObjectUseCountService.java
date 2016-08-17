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

package com.openexchange.objectusecount;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ObjectUseCountService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public interface ObjectUseCountService {

    /**
     * Get use count for object
     *
     * @param session The associated session
     * @param folder The identifier of the folder in which the object resides
     * @param objectId The identifier of the object
     * @return The object's use count
     * @throws OXException If use count cannot be returned
     */
    int getObjectUseCount(Session session, int folder, int objectId) throws OXException;

    /**
     * Get use count for object
     *
     * @param session The associated session
     * @param folder The identifier of the folder in which the object resides
     * @param objectId The identifier of the object
     * @param con An existing connection to database or <code>null</code> to fetch a new one
     * @return The object's use count
     * @throws OXException If use count cannot be returned
     */
    int getObjectUseCount(Session session, int folder, int objectId, Connection con) throws OXException;

    /**
     * Sets use count according to specified arguments
     *
     * @param session The associated session
     * @param arguments The arguments determining how/what to set
     * @throws OXException If setting user count(s) fails and arguments signal to throw an error
     */
    void setObjectUseCount(Session session, SetArguments arguments) throws OXException;

    /**
     * Increments the use count(s) according to specified arguments
     *
     * @param session The associated session
     * @param arguments The arguments determining how/what to update
     * @throws OXException If incrementing user count(s) fails and arguments signal to throw an error
     */
    void incrementObjectUseCount(Session session, IncrementArguments arguments) throws OXException;

    /**
     * Reset use count for object
     *
     * @param session The associated session
     * @param folder The identifier of the folder in which the object resides
     * @param objectId The identifier of the object
     * @throws OXException If reset operation fails
     */
    void resetObjectUseCount(Session session, int folder, int objectId) throws OXException;

    /**
     * Reset use count for object
     *
     * @param session The associated session
     * @param folder The identifier of the folder in which the object resides
     * @param objectId The identifier of the object
     * @param con A writable connection to database or <code>null</code> to fetch a new one
     * @throws OXException If reset operation fails
     */
    void resetObjectUseCount(Session session, int folder, int objectId, Connection con) throws OXException;

}

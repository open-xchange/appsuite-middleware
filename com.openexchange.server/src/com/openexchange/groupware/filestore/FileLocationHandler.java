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

package com.openexchange.groupware.filestore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;


/**
 * {@link FileLocationHandler} - Performs various actions related to file locations.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Renamed & added more methods
 * @since 7.6.0
 */
public interface FileLocationHandler {

    /**
     * Updates file locations
     *
     * @param prevFileName2newFileName The previous file name to new file name mapping
     * @param contextId The context identifier
     * @param con The connection to use
     * @throws SQLException If an SQL error occurs
     */
    void updateFileLocations(Map<String, String> prevFileName2newFileName, int contextId, Connection con) throws SQLException;

    /**
     * Determines the context-associated file locations from this handler.
     *
     * @param contextId The context identifier
     * @param con The connection to use
     * @return The file locations
     * @throws OXException If an Open-Xchange error occurs
     * @throws SQLException If an SQL error occurs
     * @since v7.8.0
     */
    Set<String> determineFileLocationsFor(int contextId, Connection con) throws OXException, SQLException;

    /**
     * Determines the user-associated file locations from this handler.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @return The file locations
     * @throws OXException If an Open-Xchange error occurs
     * @throws SQLException If an SQL error occurs
     * @since v7.8.0
     */
    Set<String> determineFileLocationsFor(int userId, int contextId, Connection con) throws OXException, SQLException;

}

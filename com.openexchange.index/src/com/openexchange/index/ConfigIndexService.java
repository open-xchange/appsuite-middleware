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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.index;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;

/**
 * {@link ConfigIndexService} - The configuration interface for index module.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ConfigIndexService {

    /**
     * Gets the appropriate read-only URL to index host for specified arguments.
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param module The module; see <code>com.openexchange.groupware.Types</code>
     * @return The appropriate read-only URL to index host
     * @throws OXException If index URL cannot be returned
     */
    IndexUrl getReadOnlyURL(int contextId, int userId, int module) throws OXException;

    /**
     * Gets the appropriate read-write URL to index host for specified arguments.
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param module The module; see <code>com.openexchange.groupware.Types</code>
     * @return The appropriate read-write URL to index host
     * @throws OXException If index URL cannot be returned
     */
    IndexUrl getWriteURL(int contextId, int userId, int module) throws OXException;
    
    /**
     * Registers a new index search server at the config db.
     * 
     * @param server The index server to register
     * @return The id of the registered server
     * @throws OXException If the server could not be registered
     */
    int registerIndexServer(IndexServer server) throws OXException;
    
    /**
     * Modifies an index search server.
     * 
     * @param server The index server to modify
     * @throws OXException If the server could not be modified
     */
    void modifyIndexServer(IndexServer server) throws OXException;
    
    /**
     * Removes an index search server from the config db.
     * 
     * @param serverId The id of the server to remove
     * @param deleteMappings <code>true</code> if all mapping entries shall be removed that use this server.
     *                       <code>false</code> if if all mappings should be kept.
     * @throws OXException
     */
    void unregisterIndexServer(int serverId, boolean deleteMappings) throws OXException;
    
    /**
     * Gets all registered index search servers.
     * 
     * @return An array of IndexUrls. Every IndexUrl points to one server.
     * @throws OXException
     */
    List<IndexServer> getAllIndexServers() throws OXException;
    
    /**
     * Adds a new (Context, User, Module) to (Server, Index) mapping to the configuration database.
     * A suitable index server will be chosen by the system. 
     * Depending on the number of already existing and possible indices.
     * 
     * @param cid The context id
     * @param uid The user id
     * @param module The module. See {@link Types}
     * @param index Name of the index
     * @throws OXException If mapping could not be added
     */
    void addIndexMapping(int cid, int uid, int module, String index) throws OXException;
    
    /**
     * Removes an index mapping for a (Context, User, Module) relation.
     * 
     * @param cid The context id
     * @param uid The user id
     * @param module The module. See {@link Types}
     * @throws OXException If mapping could not be removed
     */
    void removeIndexMapping(int cid, int uid, int module) throws OXException;
    
    /**
     * Modifies an existing (Context, User, Module) to (Server, Index) mapping.
     * 
     * @param cid The context id
     * @param uid The user id
     * @param module The module. See {@link Types}
     * @param server The server id
     * @param index Name of the index
     * @throws OXException If mapping could not be modified
     */
    void modifiyIndexMapping(int cid, int uid, int module, int server, String index) throws OXException;

}

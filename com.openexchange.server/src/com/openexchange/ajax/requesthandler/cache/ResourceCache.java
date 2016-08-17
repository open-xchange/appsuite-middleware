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

package com.openexchange.ajax.requesthandler.cache;

import com.openexchange.exception.OXException;

/**
 * {@link ResourceCache} - The cache for all kinds of requested resources that support ETag-wise access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ResourceCache {

    /**
     * Checks whether caching is allowed for the given (contextId, userId) tuple. This must
     * be invoked before any other operation.
     * @param userId The user identifier. May be &lt;= 0 for context-global caching.
     * @param contextId The context identifier.
     * @return <code>true</code> if caching is allowed, <code>false</code> if not.
     * @throws OXException If operations fails
     */
    boolean isEnabledFor(int contextId, int userId) throws OXException;

    /**
     * Stores given resource's binary content. Before saving, you have to check, if caching is allowed.
     * See {@link ResourceCache#isEnabledFor(int, int)}.
     *
     * @param id The identifier (cache key) for the cached document
     * @param resource The cached preview
     * @param userId The user identifier. May be &lt;= 0 context-global caching.
     * @param contextId The context identifier
     * @return <code>true</code> if successfully saved; otherwise <code>false</code> if impossible to store (e.g. due to quota restrictions)
     * @throws OXException If operations fails
     */
    boolean save(String id, CachedResource resource, int userId, int contextId) throws OXException;

    /**
     * Gets the resource.
     *
     * @param id The document identifier
     * @param userId The user identifier or &lt;= 0 for context-global document
     * @param contextId The context identifier
     * @return The preview document or <code>null</code>
     * @throws OXException If retrieving document data fails
     */
    CachedResource get(String id, int userId, int contextId) throws OXException;

    /**
     * Removes the resources associated with specified user.
     *
     * @param userId The user identifier or &lt;= 0 for context-global document
     * @param contextId The context identifier
     * @throws OXException If deleting document data fails
     */
    void remove(int userId, int contextId) throws OXException;

    /**
     * Removes the resources associated with specified user.
     *
     * @param id The document identifier prefix
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If deleting document data fails
     */
    void removeAlikes(String id, int userId, int contextId) throws OXException;

    /**
     * Clears all cache entries belonging to given context.
     *
     * @param contextId The context identifier
     * @throws OXException If clear operation fails
     */
    void clearFor(int contextId) throws OXException;

    /**
     * Tests for existence of denoted resource.
     *
     * @param id The identifier (cache key) for the cached resource
     * @param userId The user identifier or &lt;= 0 for context-global document
     * @param contextId The context identifier
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws OXException If an error occurs while checking existence
     */
    boolean exists(String id, int userId, int contextId) throws OXException;

}

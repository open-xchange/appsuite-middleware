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

package com.openexchange.filestore;

import java.net.URI;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link QuotaFileStorageService} - The service to access {@link QuotaFileStorage}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
@SingletonService
public interface QuotaFileStorageService {

    /**
     * Gets a usage-accounting, but unlimited {@link QuotaFileStorage} instance rather for administrative purposes using specified arguments.
     * <p>
     * This methods is intended to be used by provisioning-related operations that do not honor possible quota-exceeding file operations.
     *
     * @param baseUri The base URI for the file storage
     * @param optOwner The optional user identifier
     * @param contextId The context identifier
     * @return An appropriate {@code QuotaFileStorage} instance
     * @throws OXException If an appropriate {@code QuotaFileStorage} instance cannot be returned
     */
    QuotaFileStorage getUnlimitedQuotaFileStorage(URI baseUri, int optOwner, int contextId) throws OXException;

    /**
     * Gets a {@link QuotaFileStorage} instance for specified context.
     * <p>
     * This the same as calling {@link #getQuotaFileStorage(int, int, Info)} with the first parameter set to <code>-1</code>.
     *
     * @param contextId The context identifier
     * @param info The information for what/whom the storage is supposed to be used
     * @return An appropriate {@code QuotaFileStorage} instance
     * @throws OXException If an appropriate {@code QuotaFileStorage} instance cannot be returned
     */
    QuotaFileStorage getQuotaFileStorage(int contextId, Info info) throws OXException;

    /**
     * Gets a {@link QuotaFileStorage} instance for specified user (or context in case <tt>userId</tt> is <code>-1</code>).
     *
     * @param userId The user identifier; pass <code>-1</code> to access context-specific file storage
     * @param contextId The context identifier
     * @param info The information for what/whom the storage is supposed to be used
     * @return An appropriate {@code QuotaFileStorage} instance
     * @throws OXException If an appropriate {@code QuotaFileStorage} instance cannot be returned
     */
    QuotaFileStorage getQuotaFileStorage(int userId, int contextId, Info info) throws OXException;

    /**
     * Gets the appropriate URI pointing to proper file storage for specified user (or context in case <tt>userId</tt> is <code>-1</code>).
     *
     * @param userId The user identifier; pass <code>-1</code> to access context-specific file storage
     * @param contextId The context identifier
     * @return The URI pointing to file storage
     * @throws OXException If URI cannot be returned
     */
    URI getFileStorageUriFor(int userId, int contextId) throws OXException;

    /**
     * Determines the appropriate file storage path for given user/context pair<br>
     * (while user information might not be set (<code>userId &lt;= 0</code>) for calls accessing the context-associated file storage)
     * <p>
     * Either <span style="margin-left: 0.1in;">''<i>context</i> + <code>"_ctx_store"</code>''</span><br>
     * or <span style="margin-left: 0.1in;">''<i>context</i> + <code>"_ctx_"</code> + <i>user</i> + <code>"_user_store"</code>''</span>
     * <hr>
     * Assuming <code>contextId=57462</code>, <code>userId=5</code>, and <code>ownerId=2</code>
     * <p>
     * <ul>
     * <li>If <code>userId &lt;= 0</code> the context-associated file storage is returned --&gt; <code>"57462_ctx_store"</code></li><br>
     * <li>Otherwise the user is examined if a dedicated file storage is referenced. If no dedicated file storage is referenced
     *     (<code>user.getFilestoreId() &lt;= 0</code>) the context-associated file storage is returned  --&gt; <code>"57462_ctx_store"</code></li><br>
     * <li>In case <code>user.getFilestoreId() &gt; 0</code> is signaled, the user is further checked if that referenced file storage is assigned to another user instance acting as owner.
     *     If <code>user.getFileStorageOwner() &lt;= 0</code> the user itself is returned as owner --&gt; <code>"57462_ctx_5_user_store"</code></li><br>
     * <li>In case <code>user.getFileStorageOwner() &gt; 0</code> the owner is returned --&gt; <code>"57462_ctx_2_user_store"</code></li><br>
     * </ul>
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The appropriate file storage information to pass the proper URI to the <code>FileStorage</code> instance
     * @throws OXException If file storage information cannot be returned
     */
    StorageInfo getFileStorageInfoFor(int userId, int contextId) throws OXException;

    /**
     * Invalidates the cache entries bound to given context
     *
     * @param contextId The context identifier
     */
    void invalidateCacheFor(int contextId);

    /**
     * Invalidates the cache entries bound to given user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    void invalidateCacheFor(int userId, int contextId);

    /**
     * Checks if the specified user has an individual file storage configured that he/she owns.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if a user-associated file storage is set; otherwise <code>false</code> if specified user accesses either the context-associated or master-associated one
     * @throws OXException If check for user-associated file storage fails
     */
    boolean hasIndividualFileStorage(int userId, int contextId) throws OXException;

}

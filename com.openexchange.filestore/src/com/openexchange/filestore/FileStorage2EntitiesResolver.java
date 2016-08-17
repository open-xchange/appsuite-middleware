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

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link FileStorage2EntitiesResolver} - Resolves a certain file storage to those contexts or users that either itself or at least one of context's users use that file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
public interface FileStorage2EntitiesResolver {

    /**
     * Gets the identifiers of all contexts that either itself or at least of its users uses the denoted file storage.
     *
     * @param fileStorageId The file storage identifier
     * @return The identifiers of all contexts
     * @throws OXException If identifiers cannot be returned
     */
    int[] getIdsOfContextsUsing(int fileStorageId) throws OXException;

    /**
     * Gets the identifiers of those file storages that are used by given context. The one used by itself and the ones used by context's users.
     *
     * @param contextId The context identifier
     * @return The identifiers of used file storages
     * @throws OXException If identifiers cannot be returned
     */
    int[] getIdsOfFileStoragesUsedBy(int contextId) throws OXException;

    /**
     * Gets those file storages that are used by given context. The one used by itself and the ones used by context's users.
     * <p>
     * The file storage used by context is always the first in return listing.
     *
     * @param contextId The context identifier
     * @param quotaAware Whether returned <code>FileStorage</code> instances are supposed to be quota-aware or not
     * @return The used file storages with the one used by context at first positions
     * @throws OXException If file storages cannot be returned
     */
    List<FileStorage> getFileStoragesUsedBy(int contextId, boolean quotaAware) throws OXException;

    /**
     * Gets the file storage that is used by the given context.
     *
     * @param contextId The context identifier
     * @param quotaAware Whether returned <code>FileStorage</code> instances are supposed to be quota-aware or not
     * @return The used file storage
     * @throws OXException If file storages cannot be returned
     */
    FileStorage getFileStorageUsedBy(int contextId, boolean quotaAware) throws OXException;

    /**
     * Gets the identifiers of all users that use the denoted file storage.
     *
     * @param fileStorageId The file storage identifier
     * @return The identifiers of all users
     * @throws OXException If identifiers cannot be returned
     */
    Map<Integer, List<Integer>> getIdsOfUsersUsing(int fileStorageId) throws OXException;

    /**
     * Gets the file storage that is used by the given user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param quotaAware Whether returned <code>FileStorage</code> instances are supposed to be quota-aware or not
     * @return The used file storage
     * @throws OXException If file storages cannot be returned
     */
    FileStorage getFileStorageUsedBy(int contextId, int userId, boolean quotaAware) throws OXException;

}

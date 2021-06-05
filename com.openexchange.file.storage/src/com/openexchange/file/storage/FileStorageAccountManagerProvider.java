/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.file.storage;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FileStorageAccountManagerProvider} - Provides the {@link FileStorageAccountManager account manager} appropriate for a certain
 * {@link FileStorageService file storage service}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FileStorageAccountManagerProvider {

    /**
     * The default ranking: <code>0</code>.
     */
    public static final int DEFAULT_RANKING = 0;

    /**
     * Whether this provider supports specified {@link FileStorageService file storage service}.
     *
     * @param serviceId The file storage service identifier
     * @return <code>true</code> if this provider supports specified file storage service; otherwise <code>false</code>
     */
    boolean supports(String serviceId);

    /**
     * Gets the appropriate file storage account manager for specified account identifier and session.
     *
     * @param accountId The account identifier
     * @param session The session providing needed user data
     * @return The file storage account manager or <code>null</code>
     * @throws OXException If retrieval fails
     */
    FileStorageAccountManager getAccountManager(String accountId, Session session) throws OXException;

    /**
     * Gets the appropriate account manager for specified {@link FileStorageService file storage service}.
     *
     * @param serviceId The file storage service identifier
     * @return The appropriate account manager for specified file storage service.
     * @throws OXException If an appropriate account manager cannot be returned
     */
    FileStorageAccountManager getAccountManagerFor(String serviceId) throws OXException;

    /**
     * Gets the ranking of this provider.
     * <p>
     * The ranking is used to determine the <i>natural order</i> of providers and the <i>default</i> provider to be returned.
     * <p>
     * A provider with a ranking of <code>Integer.MAX_VALUE</code> is very likely to be returned as the default service, whereas a provider
     * with a ranking of <code>Integer.MIN_VALUE</code> is very unlikely to be returned.
     *
     * @return The ranking
     */
    int getRanking();

}

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

package com.openexchange.file.storage.owncloud;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link NextCloudFileStorageService}
 * <p>
 *  Nextcloud is mostly compatible with owncloud, but requires some specific search handling
 * </p>
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class NextCloudFileStorageService extends OwnCloudFileStorageService {

    private static final String SERVICE_ID = "nextcloud";
    private static final String DISPLAY_NAME = "Nextcloud file Storage Service";
    /**
     * Initializes a new {@link NextCloudFileStorageService}.
     *
     * @param services The {@link ServiceLookup}
     */
    public NextCloudFileStorageService(ServiceLookup services) {
        super(services, DISPLAY_NAME, SERVICE_ID);
    }

    @SuppressWarnings("null")
    @Override
    public FileStorageAccountAccess getAccountAccess(String accountId, Session session) throws OXException {
        checkCapability(session);
        final FileStorageAccount account = getAccountAccess(session, accountId);
        return new NextCloudAccountAccess(this, account, session);
    }
}

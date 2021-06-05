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

package com.openexchange.file.storage.webdav.generic;

import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.webdav.AbstractWebDAVFileStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.webdav.client.WebDAVClientFactory;

/**
 * {@link GenericWebDAVFileStorageService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public class GenericWebDAVFileStorageService extends AbstractWebDAVFileStorageService {

    public static final String CAPABILITY = CAPABILITY_PREFIX + "webdav";

    /**
     * Initializes a new {@link GenericWebDAVFileStorageService}.
     * <p/>
     * Note: The supplied service lookup reference should yield the services {@link FileStorageAccountManagerLookupService} and
     * {@link WebDAVClientFactory}.
     *
     * @param services A service lookup reference
     */
    public GenericWebDAVFileStorageService(ServiceLookup services) {
        super(services, "WebDAV", "webdav");
    }

    @Override
    public FileStorageAccountAccess getAccountAccess(String accountId, Session session) throws OXException {
        FileStorageAccount account = getAccountAccess(session, accountId);
        return new GenericWebDAVAccountAccess(this, account, session);
    }

    @Override
    public Optional<String> getCapability() {
        return Optional.of(CAPABILITY);
    }
}

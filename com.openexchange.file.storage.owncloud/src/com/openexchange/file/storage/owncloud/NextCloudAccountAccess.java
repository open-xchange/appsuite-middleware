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

import java.util.Optional;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.webdav.AbstractWebDAVFileAccess;
import com.openexchange.session.Session;
import com.openexchange.webdav.client.WebDAVClient;

/**
 * {@link NextCloudAccountAccess}
 * <p>
 *  Nextcloud is mostly compatible with owncloud, but requires some specific search handling
 * </p>
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class NextCloudAccountAccess extends OwnCloudAccountAccess {

    @SuppressWarnings("hiding")
    public final static String HTTP_CLIENT_ID = "nextcloud";

    /**
     * Initializes a new {@link NextCloudAccountAccess}.
     *
     * @param service The {@link OwnCloutFileStorageService}
     * @param account The {@link FileStorageAccount}
     * @param session The {@link Session}
     */
    protected NextCloudAccountAccess(OwnCloudFileStorageService service, @NonNull FileStorageAccount account, @NonNull Session session) {
        super(service, account, session);
    }

    @Override
    protected AbstractWebDAVFileAccess initWebDAVFileAccess(WebDAVClient webdavClient) throws OXException {
        return new NextCloudFileAccess(webdavClient, this);
    }

    @Override
    protected Optional<String> optHttpClientId() {
        return Optional.of(HTTP_CLIENT_ID);
    }
}
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

package com.openexchange.file.storage.dropbox.access;

import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.v2.DbxClientV2;

/**
 * {@link DropboxClient}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DropboxClient {

    /** The actual Dropbox client */
    public final DbxClientV2 dbxClient;

    /** The HTTP requester, whcih is used by Dropbox client */
    public final HttpRequestor httpRequestor;

    /**
     * Initializes a new {@link DropboxClient}.
     */
    public DropboxClient(DbxClientV2 client, HttpRequestor httpRequestor) {
        super();
        this.dbxClient = client;
        this.httpRequestor = httpRequestor;
    }

}

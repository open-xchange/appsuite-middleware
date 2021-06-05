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

package com.openexchange.file.storage.dropbox.http;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.version.VersionService;

/**
 * {@link DropboxHttpClientConfiguration}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class DropboxHttpClientConfiguration extends DefaultHttpClientConfigProvider {

    /** The identifier of the HTTP client used for Dropbox communication. */
    public static final String HTTP_CLIENT_DROPBOX = "dropbox";

    /**
     * Initializes a new {@link DropboxHttpClientConfiguration}.
     *
     * @param versionService The version service
     */
    public DropboxHttpClientConfiguration(VersionService versionService) {
        super(HTTP_CLIENT_DROPBOX, "Open-Xchange Dropbox HttpClient v", Optional.ofNullable(versionService));
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        return config
            .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(20))     // See com.dropbox.core.http.HttpRequestor.DEFAULT_CONNECT_TIMEOUT_MILLIS
            .setSocketReadTimeout((int) TimeUnit.MINUTES.toMillis(2))   // See com.dropbox.core.http.HttpRequestor.DEFAULT_READ_TIMEOUT_MILLIS
            .setMaxTotalConnections(100)
            .setMaxConnectionsPerRoute(100);
    }
}

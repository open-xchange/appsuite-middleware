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

package com.openexchange.dav.push.http;

import java.util.Optional;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.version.VersionService;

/**
 * {@link DavHttpClientConfiguration}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class DavHttpClientConfiguration extends DefaultHttpClientConfigProvider {

    /**
     * The DavHttpClientConfiguration.java.
     */
    public static final String HTTP_CLIENT_ID = "davpush";

    /**
     * Initializes a new {@link DavHttpClientConfiguration}.
     *
     * @param versionService <code>null</code> or the {@link VersionService}
     */
    public DavHttpClientConfiguration(VersionService versionService) {
        super(HTTP_CLIENT_ID, "OX DAV-Push Gateway Client v", Optional.ofNullable(versionService));
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        return config.setMaxTotalConnections(100).setMaxConnectionsPerRoute(100).setConnectionTimeout(5000);
    }

}

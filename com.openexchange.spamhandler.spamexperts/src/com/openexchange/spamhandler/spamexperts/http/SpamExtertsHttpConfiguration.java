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

package com.openexchange.spamhandler.spamexperts.http;

import java.util.Optional;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.version.VersionService;

/**
 * {@link SpamExtertsHttpConfiguration}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class SpamExtertsHttpConfiguration extends DefaultHttpClientConfigProvider {

    /**
     * Initializes a new {@link SpamExtertsHttpConfiguration}.
     *
     * @param versionService The version service
     */
    public SpamExtertsHttpConfiguration(VersionService versionService) {
        super("spamexperts", "OX Spam Experts Client v", Optional.ofNullable(versionService));
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        return config.setMaxTotalConnections(32).setMaxConnectionsPerRoute(32).setConnectTimeout(5000);
    }

}

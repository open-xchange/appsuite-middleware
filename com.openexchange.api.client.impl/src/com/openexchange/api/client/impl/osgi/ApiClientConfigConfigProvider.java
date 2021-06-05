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

package com.openexchange.api.client.impl.osgi;

import org.apache.http.impl.client.HttpClientBuilder;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;

/**
 * {@link ApiClientConfigConfigProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ApiClientConfigConfigProvider extends DefaultHttpClientConfigProvider {

    /** The identifier for HTTP clients that are used to communicate with other OX nodes */
    public static final String HTTP_CLIENT_IDENTIFIER = "apiClient";

    /**
     * Initializes a new {@link ApiClientConfigConfigProvider}.
     */
    public ApiClientConfigConfigProvider() {
        super(HTTP_CLIENT_IDENTIFIER);
    }

    @Override
    public void modify(HttpClientBuilder builder) {
        //We do handle redirects manually in order to resolve share links
        builder.disableRedirectHandling();
        super.modify(builder);
    }
}

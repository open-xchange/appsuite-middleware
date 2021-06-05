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

package com.openexchange.pns.transport.apns_http2.util;


/**
 * {@link ApnsHttp2OptionsPerClient} - A pair of client identifier and associated APNS HTTP/2 options.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ApnsHttp2OptionsPerClient {

    private final String client;
    private final ApnsHttp2Options options;

    /**
     * Initializes a new {@link ApnsHttp2OptionsPerClient}.
     *
     * @param client The client
     * @param options The associated APNS HTTP/2 options
     */
    public ApnsHttp2OptionsPerClient(String client, ApnsHttp2Options options) {
        super();
        this.client = client;
        this.options = options;
    }

    /**
     * Gets the client
     *
     * @return The client
     */
    public String getClient() {
        return client;
    }

    /**
     * Gets the APNS HTTP/2 options
     *
     * @return The APNS HTTP/2 options
     */
    public ApnsHttp2Options getOptions() {
        return options;
    }

}

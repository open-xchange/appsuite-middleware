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

package com.openexchange.mail.authenticity.impl.trusted.internal.fetcher;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TrustedMailIconURLFetcher}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TrustedMailIconURLFetcher extends AbstractTrustedMailIconFetcher implements TrustedMailIconFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(TrustedMailIconURLFetcher.class);
    private static final String HEAD = "HEAD";
    private static final String GET = "GET";
    private static final int CONNECTION_TIMEOUT = 2000;
    private static final int READ_TIMEOUT = 2000;

    /**
     * Initialises a new {@link TrustedMailIconURLFetcher}.
     */
    public TrustedMailIconURLFetcher() {
        super();
    }

    @Override
    public boolean exists(String resourceUrl) {
        HttpURLConnection connection = null;
        try {
            connection = prepareConnection(resourceUrl, HEAD);
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            LOG.error("An I/O error occurred while reading the resource URL '{}': {}", resourceUrl, e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public byte[] fetch(String url) {
        HttpURLConnection connection = null;
        try {
            connection = prepareConnection(url, GET);
            return process(ImageIO.read(connection.getInputStream()));
        } catch (IOException e) {
            LOG.error("An I/O error occurred while reading the resource URL '{}': {}", url, e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * Prepares an {@link HttpURLConnection} for the specified resource URL
     * 
     * @param resourceUrl The resource URL
     * @param httpMethod The HTTP method to use
     * @return The prepared {@link HttpURLConnection}
     * @throws IOException if an I/O error occurs
     */
    private HttpURLConnection prepareConnection(String resourceUrl, String httpMethod) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(resourceUrl).openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestMethod(httpMethod);
        connection.setInstanceFollowRedirects(false);
        return connection;
    }
}

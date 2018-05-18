/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2018-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
        HttpURLConnection.setFollowRedirects(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.impl.trusted.internal.fetcher.TrustedMailIconFetcher#exists(java.lang.String)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.impl.trusted.internal.fetcher.TrustedMailIconFetcher#fetch(java.lang.String, java.lang.String)
     */
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
        return connection;
    }
}

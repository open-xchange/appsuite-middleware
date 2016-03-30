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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.rest.client.session;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.session.pair.AccessTokenPair;
import com.openexchange.rest.client.session.pair.AppKeyPair;
import com.openexchange.rest.client.session.pair.ConsumerPair;

/**
 * {@link Session}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface Session {

    /**
     * Returns whether or not this session has a user's access token and secret.
     * 
     * @return true if this session has a user's access token and secret linked; false otherwise
     */
    public boolean isLinked();

    /**
     * Unlinks the session by removing any stored access token and secret.
     */
    public void unlink();

    /**
     * OAuth signs the request with the currently-set tokens and secrets.
     * 
     * @param request an {@link HttpRequest}.
     * @throws OXException If signing request fail
     */
    public void sign(HttpRequestBase request) throws OXException;

    /**
     * Will be called every time a REST request is fired, in case the proxy changes between requests. Return null if you do not want to use
     * a proxy, or a {@link ProxyInfo} object with a host and optionally a port set.
     */
    public ProxyInfo getProxyInfo();

    /**
     * Will be called every time a REST request is fired, in case you want to use a new client every time. However, it's highly recommended
     * to create a client once and reuse it to take advantage of connection reuse.
     */
    public HttpClient getHttpClient();

    /**
     * Will be called every time right before a REST request is fired. It should set the socket and connection timeouts on the request if
     * the default values need to be overridden. This is abstracted out to cope with signature changes in the Apache HttpClient libraries.
     */
    public void setRequestTimeout(HttpUriRequest request);

    /**
     * Returns the app key and secret.
     * 
     * @return The app key and secret.
     */
    public AppKeyPair getAppKeyPair();

    /**
     * Returns the currently logged in user's access token and secret.
     * 
     * @return The currently logged in user's access token and secret.
     */
    public AccessTokenPair getAccessTokenPair();

    /**
     * Returns the consumer key/secret pair
     * 
     * @return The consumer key/secret pair
     */
    public ConsumerPair getConsumerPair();

    /**
     * Describes a proxy.
     */
    public static final class ProxyInfo {

        /** The address of the proxy. */
        public final String host;

        /** The port of the proxy, or -1 to use the default port. */
        public final int port;

        /**
         * Creates a proxy info.
         * 
         * @param host the host to use without a protocol (required).
         * @param port the port to use, or -1 for default port.
         */
        public ProxyInfo(final String host, final int port) {
            this.host = host;
            this.port = port;
        }

        /**
         * Creates a proxy info using the default port.
         * 
         * @param host the host to use without a protocol (required).
         */
        public ProxyInfo(final String host) {
            this(host, -1);
        }
    }
}

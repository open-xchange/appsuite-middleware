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


package com.openexchange.xing.session;

import java.util.Locale;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestBase;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.exception.XingException;

/**
 * Keeps track of a logged in user. Contains configuration options for the
 * {@link XingAPI}.
 */
public interface Session {

    /**
     * Returns the app key and secret.
     */
    public AppKeyPair getAppKeyPair();

    /**
     * Returns the currently logged in user's access token and secret.
     */
    public AccessTokenPair getAccessTokenPair();
    
    /**
     * Returns the consumer key/secret pair
     */
    public ConsumerPair getConsumerPair();

    /**
     * Returns the locale to use. Must not return null. Currently, this is used
     * for user-facing messages that are returned by the API. You should not
     * cache this value, but instead get it every time from the system in case
     * the locale changes.
     */
    public Locale getLocale();

    /**
     * Returns whether or not this session has a user's access token and
     * secret.
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
     * @throws XingException If signing request fail
     */
    public void sign(HttpRequestBase request) throws XingException;

    /**
     * Will be called every time a request is made to Xing, in case the
     * proxy changes between requests. Return null if you do not want to use
     * a proxy, or a {@link ProxyInfo} object with a host and optionally a
     * port set.
     */
    public ProxyInfo getProxyInfo();

    /**
     * Will be called every time right before a request is sent to Xing. It
     * should set the socket and connection timeouts on the request if you want
     * to override the default values. This is abstracted out to cope with
     * signature changes in the Apache HttpClient libraries.
     */
    public void setRequestTimeout(HttpRequestBase request);

    /**
     * Returns the XING API server. Changing this will break things.
     */
    public String getAPIServer();

    /**
     * Returns the XING content server. Changing this will break things.
     */
    public String getContentServer();

    /**
     * Returns the XING web server. Changing this will break things.
     */
    public String getWebServer();

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
        public ProxyInfo(String host, int port) {
            this.host = host;
            this.port = port;
        }

        /**
         * Creates a proxy info using the default port.
         *
         * @param host the host to use without a protocol (required).
         */
        public ProxyInfo(String host) {
            this(host, -1);
        }
    }
}

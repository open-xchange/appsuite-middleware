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

import java.util.concurrent.atomic.AtomicReference;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.API;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.session.pair.AccessTokenPair;
import com.openexchange.rest.client.session.pair.AppKeyPair;
import com.openexchange.rest.client.session.pair.ConsumerPair;

/**
 * {@link AbstractSession}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractSession implements Session {

    private final AppKeyPair appKeyPair;

    private ConsumerPair consumerPair;

    private AccessTokenPair accessTokenPair;

    private volatile CommonsHttpOAuthConsumer signer = null;

    protected final AtomicReference<HttpClient> client = new AtomicReference<HttpClient>();


    /** The default timeout for client connections. */
    private static final int DEFAULT_TIMEOUT_MILLIS = 30000;

    /** How long connections are kept alive. */
    private static final int KEEP_ALIVE_DURATION_SECS = 20;

    /** How often the monitoring thread checks for connections to close. */
    private static final int KEEP_ALIVE_MONITOR_INTERVAL_SECS = 5;

    /** Maximum total connections available for the connection manager */
    private static final int MAX_TOTAL_CONNECTIONS = 20;

    /** Maximum connections per route */
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10;

    /**
     * Initializes a new {@link AbstractSession}. Creates a new session with the given app key and secret. The session will not be linked
     * because it has no access token pair.
     *
     * @param appKeyPair The application key pair
     */
    protected AbstractSession(final AppKeyPair appKeyPair) {
        this(appKeyPair, (AccessTokenPair)null);
    }

    /**
     * Initializes a new {@link AbstractSession}. Creates a new session with the given app key and secret. The session will be linked to the
     * account corresponding to the given access token pair.
     *
     * @param appKeyPair The application key pair
     * @param accessTokenPair The access token pair
     */
    protected AbstractSession(final AppKeyPair appKeyPair, final AccessTokenPair accessTokenPair) {
        super();
        this.appKeyPair = appKeyPair;
        this.accessTokenPair = accessTokenPair;
    }

    /**
     * Initializes a new {@link WebAuthSession} with the specified {@link ConsumerPair}. The session will be used to create an account to
     * the remote system, based on the OX account (upsell).
     *
     * @param appKeyPair
     * @param consumerPair
     */
    protected AbstractSession(final AppKeyPair appKeyPair, final ConsumerPair consumerPair) {
        super();
        if (appKeyPair == null) {
            throw new IllegalArgumentException("'appKeyPair' must be non-null");
        }
        this.appKeyPair = appKeyPair;
        this.consumerPair = consumerPair;
    }

    @Override
    public AppKeyPair getAppKeyPair() {
        return appKeyPair;
    }

    @Override
    public AccessTokenPair getAccessTokenPair() {
        return accessTokenPair;
    }

    @Override
    public ConsumerPair getConsumerPair() {
        return consumerPair;
    }

    @Override
    public boolean isLinked() {
        return accessTokenPair != null;
    }

    @Override
    public void unlink() {
        accessTokenPair = null;
    }

    /**
     * Signs the request by using's OAuth's HTTP header authorization scheme and the PLAINTEXT signature method. As such, this should only
     * be used over secure connections (i.e. HTTPS). Using this over regular HTTP connections is completely insecure.
     *
     * @see Session#sign
     */
    @Override
    public void sign(final HttpRequestBase request) throws OXException {
        try {
            getSigner().sign(request);
        } catch (final OAuthCommunicationException e) {
            throw RESTExceptionCodes.ERROR.create(e);
        } catch (final OAuthMessageSignerException e) {
            throw RESTExceptionCodes.ERROR.create(e);
        } catch (final OAuthExpectationFailedException e) {
            throw RESTExceptionCodes.ERROR.create(e);
        }
    }

    @Override
    public synchronized ProxyInfo getProxyInfo() {
        return null;
    }

    @Override
    public HttpClient getHttpClient() {
        HttpClient client = this.client.get();
        if (client == null) {
            synchronized (this) {
                client = this.client.get();
                if (client == null) {
                    client = HttpClients.getHttpClient(API.getUserAgent());
                    this.client.set(client);
                }
            }
        }
        return client;
    }

    @Override
    public void setRequestTimeout(HttpUriRequest request) {
        HttpClients.setDefaultRequestTimeout(request);
    }

    /**
     * Get the signer
     *
     * @return The signer
     */
    private CommonsHttpOAuthConsumer getSigner() {
        CommonsHttpOAuthConsumer tmp = signer;
        if (null == tmp) {
            synchronized (this) {
                tmp = signer;
                if (null == tmp) {
                    tmp = new CommonsHttpOAuthConsumer(appKeyPair.key, appKeyPair.secret);
                    if (null != accessTokenPair) {
                        tmp.setTokenWithSecret(accessTokenPair.key, accessTokenPair.secret);
                    }
                    signer = tmp;
                }
            }
        }
        return tmp;
    }

}

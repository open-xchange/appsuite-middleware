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

package com.openexchange.rest.client.session;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.session.pair.AccessTokenPair;
import com.openexchange.rest.client.session.pair.AppKeyPair;
import com.openexchange.rest.client.session.pair.ConsumerPair;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

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
        } catch (OAuthCommunicationException e) {
            throw RESTExceptionCodes.ERROR.create(e);
        } catch (OAuthMessageSignerException e) {
            throw RESTExceptionCodes.ERROR.create(e);
        } catch (OAuthExpectationFailedException e) {
            throw RESTExceptionCodes.ERROR.create(e);
        }
    }

    @Override
    public synchronized ProxyInfo getProxyInfo() {
        return null;
    }

    @Override
    public void setRequestTimeout(HttpUriRequest request) {
        HttpClients.setDefaultRequestTimeout((HttpRequestBase) request);
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

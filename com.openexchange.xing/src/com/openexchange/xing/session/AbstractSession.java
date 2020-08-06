/*-
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

package com.openexchange.xing.session;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.exception.XingException;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

/**
 * Keeps track of a logged in user and contains configuration options for the
 * {@link XingAPI}. This is a base class to use for creating your own
 * {@link Session}s.
 */
public abstract class AbstractSession implements Session {

    private static final String API_SERVER = "api.xing.com";
    private static final String CONTENT_SERVER = "api.xing.com";
    private static final String WEB_SERVER = "www.xing.com";

    /** The default timeout for client connections. */
    static final int DEFAULT_TIMEOUT_MILLIS = 30000; // 30 seconds

    private ConsumerPair consumerPair;
    private final AppKeyPair appKeyPair;
    private AccessTokenPair accessTokenPair = null;
    private volatile CommonsHttpOAuthConsumer signer = null;

    protected final AtomicReference<HttpClient> client = new AtomicReference<HttpClient>();

    /**
     * Creates a new session with the given app key and secret, and access
     * type. The session will not be linked because it has no access token pair.
     */
    protected AbstractSession(final AppKeyPair appKeyPair) {
        this(appKeyPair, (AccessTokenPair) null);
    }

    /**
     * Creates a new session with the given app key and secret, and access
     * type. The session will be linked to the account corresponding to the
     * given access token pair.
     */
    protected AbstractSession(final AppKeyPair appKeyPair, final AccessTokenPair accessTokenPair) {
        super();
        if (appKeyPair == null) {
            throw new IllegalArgumentException("'appKeyPair' must be non-null");
        }
        this.appKeyPair = appKeyPair;
        this.accessTokenPair = accessTokenPair;
    }

    /**
     * Initializes a new {@link WebAuthSession} with the specified {@link ConsumerPair}.
     * The session will be used to create a Xing profile, based on the OX account (upsell).
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

    /**
     * Links the session with the given access token and secret.
     */
    public void setAccessTokenPair(final AccessTokenPair accessTokenPair) {
        if (accessTokenPair == null) {
            throw new IllegalArgumentException("'accessTokenPair' must be non-null");
        }
        this.accessTokenPair = accessTokenPair;
        this.signer = null;
    }

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

    /**
     * {@inheritDoc}
     * <br/><br/>
     * The default implementation always returns {@code Locale.ENLISH}, but you
     * are highly encouraged to localize your application and return the system
     * locale instead. Note: as of the time this was written, XING supports
     * the de, en, es, fr, and ja locales - if you use a locale other than
     * these, messages will be returned in English. However, it is good
     * practice to pass along the correct locale as we will add more languages
     * in the future.
     */
    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
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
     * Signs the request by using's OAuth's HTTP header authorization scheme
     * and the PLAINTEXT signature method. As such, this should only be used
     * over secure connections (i.e. HTTPS). Using this over regular HTTP
     * connections is completely insecure.
     *
     * @see Session#sign
     */
    @Override
    public void sign(final HttpRequestBase request) throws XingException {
        try {
            getSigner().sign(request);
        } catch (OAuthCommunicationException e) {
            throw new XingException(e);
        } catch (OAuthMessageSignerException e) {
            throw new XingException(e);
        } catch (OAuthExpectationFailedException e) {
            throw new XingException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <br/><br/>
     * The default implementation always returns null.
     */
    @Override
    public synchronized ProxyInfo getProxyInfo() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <br/><br/>
     * The default implementation always sets a 30 second timeout.
     */
    @Override
    public void setRequestTimeout(final HttpRequestBase request) {
        HttpClients.setRequestTimeout(DEFAULT_TIMEOUT_MILLIS, request);
    }

    @Override
    public String getAPIServer() {
        return API_SERVER;
    }

    @Override
    public String getContentServer() {
        return CONTENT_SERVER;
    }

    @Override
    public String getWebServer() {
        return WEB_SERVER;
    }

}

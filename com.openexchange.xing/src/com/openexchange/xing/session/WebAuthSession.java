/*
 * Copyright (c) 2009-2011 Xing, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.openexchange.xing.session;

import java.util.Map;

import org.apache.http.HttpResponse;
import com.openexchange.xing.RESTUtility;
import com.openexchange.xing.RESTUtility.Method;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingParseException;

/**
 * Keeps track of a logged in user contains configuration options for the
 * {@link XingAPI}. This type of {@link Session} uses the web OAuth flow
 * to authenticate users:
 * <ol>
 *   <li>A request token + secret and redirect URL are retrieved using
 *   {@link WebAuthSession#getAuthInfo()} or
 *   {@link WebAuthSession#getAuthInfo(String)}.</li>
 *   <li>You store the request token + secret, and redirect the
 *   user to the redirect URL where they will authenticate with Xing and
 *   grant your app permission to access their account.</li>
 *   <li>Xing will redirect back to your site if it was provided a URL to
 *   do so (otherwise, you have to ask the user when he/she is done).</li>
 *   <li>The user's access token + secret are set on this session when you
 *   call {@link WebAuthSession#retrieveWebAccessToken(RequestTokenPair)} with
 *   the previously-saved request token + secret. You have a limited amount
 *   of time to make this call or the request token will expire.</li>
 * </ol>
 */
public class WebAuthSession extends AbstractSession {

    /**
     * Contains the info needed to send the user to the Xing web auth page
     * and later retrieve an access token + secret.
     */
    public static final class WebAuthInfo {

        /** The URL to redirect the user to. */
        public final String url;

        /**
         * The request token to later use with
         * {@link WebAuthSession#retrieveWebAccessToken(RequestTokenPair)}.
         * Expires after a short amount of time (currently 5 minutes).
         */
        public final RequestTokenPair requestTokenPair;

        WebAuthInfo(String url, RequestTokenPair requestTokenPair) {
            this.url = url;
            this.requestTokenPair = requestTokenPair;
        }
    }

    /**
     * Creates a new web auth session with the given app key pair and access
     * type. The session will not be linked because it has no access token or
     * secret.
     */
    public WebAuthSession(AppKeyPair appKeyPair) {
        super(appKeyPair);
    }

    /**
     * Creates a new web auth session with the given app key pair and access
     * type. The session will be linked to the account corresponding to the
     * given access token pair.
     */
    public WebAuthSession(AppKeyPair appKeyPair, AccessTokenPair accessTokenPair) {
        super(appKeyPair, accessTokenPair);
    }

    /**
     * Starts an authentication request with Xing servers and gets all the
     * info you need to start authenticating a user. This call blocks for a
     * non-trivial amount of time due to a network operation. Because a
     * callback URL is not provided, you will have to somehow determine when
     * the user has finished authenticating on the Xing site (for example,
     * put up a prompt after you open a browser window for authentication). If
     * you want to provide a callback URL, see
     * {@link WebAuthSession#getAuthInfo(String)}.
     *
     * @return a {@link WebAuthInfo}, from which you can obtain the URL to
     *         redirect the user to and a request token + secret to log the
     *         user in later.
     *
     * @throws XingServerException if the server responds with an error
     *         code. See the constants in {@link XingServerException} for
     *         the meaning of each error code. The most common error codes you
     *         can expect from this call are 500, 502, and 503 (all related to
     *         internal Xing server issues).
     * @throws XingIOException if any network-related error occurs.
     * @throws XingParseException if a malformed or unknown response was
     *         received from the server.
     * @throws XingException for any other unknown errors. This is also a
     *         superclass of all other Xing exceptions, so you may want to
     *         only catch this exception which signals that some kind of error
     *         occurred.
     */
    public WebAuthInfo getAuthInfo() throws XingException {
        return getAuthInfo(null);
    }

    /**
     * Starts an authentication request with Xing servers and gets all the
     * info you need to start authenticating a user. This call blocks for a
     * non-trivial amount of time due to a network operation.
     *
     * @param callbackUrl the URL to which Xing will redirect the user after
     *         he/she has authenticated on the Xing site.
     *
     * @return a {@link WebAuthInfo}, from which you can obtain the URL to
     *         redirect the user to and a request token + secret to log the
     *         user in later.
     *
     * @throws XingServerException if the server responds with an error
     *         code. See the constants in {@link XingServerException} for
     *         the meaning of each error code. The most common error codes you
     *         can expect from this call are 500, 502, and 503 (all for
     *         internal Xing server issues).
     * @throws XingIOException if any network-related error occurs.
     * @throws XingParseException if a malformed or unknown response was
     *         received from the server.
     * @throws XingException for any other unknown errors. This is also a
     *         superclass of all other Xing exceptions, so you may want to
     *         only catch this exception which signals that some kind of error
     *         occurred.
     */
    public WebAuthInfo getAuthInfo(String callbackUrl)
            throws XingException {
        setUpToken("/oauth/request_token");

        // Request token pair was set as access token pair as they act the
        // same. Convert it here.
        AccessTokenPair accessTokenPair = getAccessTokenPair();
        RequestTokenPair requestTokenPair = new RequestTokenPair(
                accessTokenPair.key, accessTokenPair.secret);

        String[] args;
        if (callbackUrl != null) {
            args = new String[] {"oauth_token", requestTokenPair.key,
                                 "oauth_callback", callbackUrl,
                                 "locale", getLocale().toString()};
        } else {
            args = new String[] {"oauth_token", requestTokenPair.key,
                                 "locale", getLocale().toString()};
        }

        String url = RESTUtility.buildURL(getWebServer(),
                1, "/oauth/authorize", args);

        return new WebAuthInfo(url, requestTokenPair);
    }

    /**
     * When called after the user is done authenticating, sets the user's
     * access token + secret on this session. This call blocks for a
     * non-trivial amount of time due to a network operation. Since the request
     * token + secret expire after a short time (currently 5 minutes), this
     * should be called right after a user comes back from auth on the Xing
     * site.
     *
     * @param requestTokenPair the request token pair from the {@link WebAuthInfo}
     *         returned from {@code getAuthInfo()}.
     *
     * @return the Xing UID of the authenticated user.
     *
     * @throws XingServerException if the server responds with an error
     *         code. See the constants in {@link XingServerException} for
     *         the meaning of each error code. The most common error codes you
     *         can expect from this call are 401 (bad request token), 403 (bad
     *         app key pair), 500, 502, and 503 (all for internal Xing
     *         server issues).
     * @throws XingIOException if any network-related error occurs.
     * @throws XingParseException if a malformed or unknown response was
     *         received from the server.
     * @throws XingException for any other unknown errors. This is also a
     *         superclass of all other Xing exceptions, so you may want to
     *         only catch this exception which signals that some kind of error
     *         occurred.
     */
    public String retrieveWebAccessToken(RequestTokenPair requestTokenPair)
            throws XingException {
        setAccessTokenPair(requestTokenPair);
        Map<String, String> result = setUpToken("/oauth/access_token");
        return result.get("uid");
    }

    private Map<String, String> setUpToken(String path)
            throws XingException {
        HttpResponse response = RESTUtility.streamRequest(Method.GET,
                getAPIServer(), path,
                1,
                new String[] {"locale", getLocale().toString()},
                this).response;
        Map<String, String> result = RESTUtility.parseAsQueryString(response);

        if (!result.containsKey("oauth_token") ||
                !result.containsKey("oauth_token_secret")) {
            throw new XingParseException("Did not get tokens from Xing");
        }

        setAccessTokenPair(new AccessTokenPair(
            result.get("oauth_token"), result.get("oauth_token_secret")));

        return result;
    }
}

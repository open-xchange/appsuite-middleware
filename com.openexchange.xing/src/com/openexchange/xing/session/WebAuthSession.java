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

/**
 * Keeps track of a logged in user contains configuration options for the {@link XingAPI}. This type of {@link Session} uses the web OAuth
 * flow to authenticate users:
 * <ol>
 * <li>A request token + secret and redirect URL are retrieved using {@link WebAuthSession#getAuthInfo()} or
 * {@link WebAuthSession#getAuthInfo(String)}.</li>
 * <li>You store the request token + secret, and redirect the user to the redirect URL where they will authenticate with XING and grant your
 * app permission to access their account.</li>
 * <li>XING will redirect back to your site if it was provided a URL to do so (otherwise, you have to ask the user when he/she is done).</li>
 * <li>The user's access token + secret are set on this session when you call
 * {@link WebAuthSession#retrieveWebAccessToken(RequestTokenPair)} with the previously-saved request token + secret. You have a limited
 * amount of time to make this call or the request token will expire.</li>
 * </ol>
 */
public class WebAuthSession extends AbstractSession {

    /**
     * Creates a new web auth session with the given app key pair and access type. The session will not be linked because it has no access
     * token or secret.
     */
    public WebAuthSession(AppKeyPair appKeyPair) {
        super(appKeyPair);
    }

    /**
     * Creates a new web auth session with the given app key pair and access type. The session will be linked to the account corresponding
     * to the given access token pair.
     */
    public WebAuthSession(AppKeyPair appKeyPair, AccessTokenPair accessTokenPair) {
        super(appKeyPair, accessTokenPair);
    }

}

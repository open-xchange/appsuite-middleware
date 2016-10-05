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

package com.openexchange.oauth.dropbox;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

/**
 * {@link DropboxApi2}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxApi2 extends DefaultApi20 {

    private static final String AUTHORISE_URL = "https://www.dropbox.com/oauth2/authorize?client_id=%s&response_type=code&redirect_uri=%s";

    /**
     * Initialises a new {@link DropboxApi2}.
     */
    public DropboxApi2() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.scribe.builder.api.DefaultApi20#getAccessTokenEndpoint()
     */
    @Override
    public String getAccessTokenEndpoint() {
        return "https://api.dropboxapi.com/oauth2/token";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.scribe.builder.api.DefaultApi20#getAuthorizationUrl(org.scribe.model.OAuthConfig)
     */
    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(AUTHORISE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.scribe.builder.api.DefaultApi20#createService(org.scribe.model.OAuthConfig)
     */
    @Override
    public OAuthService createService(OAuthConfig config) {
        return new DropboxOAuth2Service(this, config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.scribe.builder.api.DefaultApi20#getAccessTokenVerb()
     */
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.scribe.builder.api.DefaultApi20#getAccessTokenExtractor()
     */
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        // Dropbox returns a JSONObject thus we need the JsonTokenExtractor to extract the 'access_token'
        return new JsonTokenExtractor();
    }

    /**
     * {@link DropboxOAuth2Service}
     */
    public static class DropboxOAuth2Service extends OAuth20ServiceImpl {

        private DefaultApi20 api;
        private OAuthConfig config;

        /**
         * Initialises a new {@link DropboxOAuth2Service}.
         * 
         * @param api
         * @param config
         */
        public DropboxOAuth2Service(DefaultApi20 api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.scribe.oauth.OAuth20ServiceImpl#getAccessToken(org.scribe.model.Token, org.scribe.model.Verifier)
         */
        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());

            request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
            request.addBodyParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.GRANT_TYPE_AUTHORIZATION_CODE);
            request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
            request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
            request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());

            Response response = request.send();
            return api.getAccessTokenExtractor().extract(response.getBody());
        }
    }
}

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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.oauth.linkedin;

import java.util.List;
import java.util.Scanner;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.linkedin.osgi.Activator;

/**
 * {@link LinkedInConnectionsService}
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class LinkedInConnectionsService {

    // private static final String AUTHORIZE_URL = "https://api.linkedin.com/uas/oauth/authorize?oauth_token=";
    private static final String PROTECTED_RESOURCE_URL = "http://api.linkedin.com/v1/people/~/connections:(id,first-name,last-name,phone-numbers,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions)";

    private Activator activator;

    // TODO: these need to be in a config-file
    private static final String API_KEY = "Ra7yTqolxUk_6UVpIAIsbv6kwLpIZCdNeUYxAA1n2Lnf05Dkr7D41dw-ivK-z4vA";

    private static final String API_SECRET = "vEPBnxJvXvqf9NsBby0kZ1hcgQCM7JBO7iCjlw4KIDhw_7lwPIln7zIvtP3dbL-i";

    public LinkedInConnectionsService(Activator activator) {
        this.activator = activator;
    }

    public void getData(int user, int contextId) {
        OAuthService service = new ServiceBuilder().provider(LinkedInApi.class).apiKey(API_KEY).apiSecret(API_SECRET).callback("http://localhost/ajax/oauth/account&action=create").build();

        // TODO: This will be replaced by the OAuth-Keyring-Service. One will ask for an account here and it will deliver the relevant
        // AccessToken
        // Only if there is no Account yet the following will need to be done (and the account saved after that)

        try {
            com.openexchange.oauth.OAuthService oAuthService = activator.getOauthService();
            List<OAuthAccount> accounts = oAuthService.getAccounts(activator.getLinkedInMetadata().getId(), user, contextId);
        } catch (OAuthException e) {
            
        }
        
        System.out.println("=== LinkedIn's OAuth Workflow ===");
        System.out.println();

        // Obtain the Request Token
        System.out.println("Fetching the Request Token...");
        Token requestToken = service.getRequestToken();
        System.out.println("Got the Request Token!");
        System.out.println();

        DefaultOAuthToken oAuthToken = new DefaultOAuthToken();
        oAuthToken.setToken(requestToken.getToken());
        oAuthToken.setSecret(requestToken.getSecret());

        // TODO: The verifier needs to come from the keyring / generic OAuth-Service here
        System.out.println(activator.getLinkedInMetadata().getAuthorizationURL(oAuthToken));
        System.out.println("And paste the verifier here");
        System.out.print(">>");

        Scanner in = new Scanner(System.in);
        Verifier verifier = new Verifier(in.nextLine());
        System.out.println();

        // Trade the Request Token and Verifier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        Token accessToken = service.getAccessToken(requestToken, verifier);
        System.out.println("Got the Access Token!");
        System.out.println("(if your curious it looks like this: " + accessToken + " )");
        System.out.println();

        // The real access after authorization
        OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
        service.signRequest(accessToken, request);
        Response response = request.send();

        // TODO: This needs to be parsed into a class-hierarchy
        System.out.println(response.getBody());

    }

    public Activator getActivator() {
        return activator;
    }

    public void setActivator(Activator activator) {
        this.activator = activator;
    }

    public void getAccount() {

    }

    public void createAccount() {

    }

}

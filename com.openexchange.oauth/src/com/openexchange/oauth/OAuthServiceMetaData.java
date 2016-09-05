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

package com.openexchange.oauth;

import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;

/**
 * {@link OAuthServiceMetaData} - Represents the OAuth service meta data.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface OAuthServiceMetaData {

    /**
     * Gets this meta data's identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets the display name.
     *
     * @return The display name
     */
    String getDisplayName();

    /**
     * Checks if the given user may use this service.
     *
     * @param contextId The users context
     * @param userId The users id
     * @return Whether this service is enabled for the user or not
     */
    boolean isEnabled(int userId, int contextId) throws OXException;

    /**
     * Use {@link #getAPIKey(Session)} and also please implement {@link #getAPIKey(Session)}
     *
     * <p><u><b>NOTE</b></u>:This method is <b>deprecated</b> and it will be removed in a future release.</p>
     *
     * @return The API key
     */
    @Deprecated
    String getAPIKey();

    /**
     * Gets the API key that belongs to a given session
     * 
     * @param session
     * @return The API key
     * @throws OXException
     */
    String getAPIKey(Session session) throws OXException;

    /**
     * Use {@link #getAPISecret(Session)} and also implement {@link #getAPISecret(Session)}
     * 
     * <p><u><b>NOTE</b></u>:This method is <b>deprecated</b> and it will be removed in a future release.</p>
     * 
     * @return The API secret
     */
    @Deprecated
    String getAPISecret();

    /**
     * Gets the API secret that belongs to a given session
     * 
     * @param session
     * @return The API key
     * @throws OXException
     */
    String getAPISecret(Session session) throws OXException;

    /**
     * Get the consumer key (upsell)
     * 
     * @return the consumer key
     */
    String getConsumerKey();

    /**
     * Get the consumer secret (upsell)
     * 
     * @return the consumer secret
     */
    String getConsumerSecret();

    /**
     * Indicates if this meta data needs a request token to obtain authorization URL.
     *
     * @return <code>true</code> if this meta data needs a request token to obtain authorization URL; otherwise <code>false</code> to pass
     *         <code>null</code>
     */
    boolean needsRequestToken();

    /**
     * Gets the optional scope; a comma-separated list of scope items.
     * 
     * <p><u><b>NOTE</b></u>:This method is <b>deprecated</b> and it will be removed in a future release.</p>
     *
     * @return The scope or <code>null</code>
     * @deprecated Use {@link #getAvailableScopes()} instead.
     */
    String getScope();

    /**
     * Processes specified authorization URL.
     *
     * @return The processed authorization URL
     */
    String processAuthorizationURL(String authUrl);

    /**
     * Processes specified authorization URL with respect to call-back.
     * <p>
     * Intended to be called after {@link #processAuthorizationURL(String)} method.
     *
     * @param authUrl The processed authorization URL
     * @param callback The call-back string
     * @return The processed authorization URL
     */
    String processAuthorizationURLCallbackAware(String authUrl, String callback);

    /**
     * Processes specified arguments.
     *
     * @param arguments The arguments. You can store additional information here
     * @param parameter The parameters. The request parameters sent to the callback URL. You may want to extract items from these and store them in arguments for later processing
     * @param state The state
     * @throws OXException If an error occurs
     */
    void processArguments(Map<String, Object> arguments, Map<String, String> parameter, Map<String, Object> state) throws OXException;

    /**
     * Gets the optional OAuth token.
     *
     * @param arguments The OAuth arguments
     * @param scopes A {@link Set} with the requested {@link OAuthScope}s
     * @return The OAuth token or <code>null</code>
     * @throws OXException If an error occurs returning the token
     */
    OAuthToken getOAuthToken(Map<String, Object> arguments, Set<OAuthScope> scopes) throws OXException;

    /**
     * Initiates contact and returns the initial OAuth interaction.
     * <p>
     * This is an optional method, just return <code>null</code> when you do not need to do anything special here.
     *
     * @param callbackUrl The call-back URL
     * @param session The associated session
     * @return The OAuth interaction or <code>null</code>
     */
    OAuthInteraction initOAuth(String callbackUrl, Session session) throws OXException;

    /**
     * Gives the strategy the opportunity to modify a callback URL.
     *
     * @param callbackUrl The call-back URL
     * @param currentHost The name of the current host
     * @param session The associated session
     * @return The modified callback URL
     */
    String modifyCallbackURL(String callbackUrl, String currentHost, Session session);

    /**
     * Gets the style of API (e.g. Twitter...).
     *
     * @return The API reference
     */
    API getAPI();

    /**
     * Whether to register a token based deferrer.
     * <p>
     * Note: This method is only considered if {@link #doCustomRegistration(String, CallbackRegistry)} did not return <code>null</code>
     *
     * @return <code>true</code> to register a token based deferrer; otherwise <code>false</code>
     */
    boolean registerTokenBasedDeferrer();

    /**
     * Gets an optional registration token in case {@link #registerTokenBasedDeferrer()} returns <code>true</code>, but there is no common
     * <code>"oauth_token"</code>.
     *
     * @param authUrl The authorization URL from which to yield the token
     * @return A registration token (starting with <code>"__ox"</code>) or <code>null</code>
     */
    String getRegisterToken(String authUrl);

    /**
     * Returns an unmodifiable {@link Set} with all available {@link OAuthScope}s
     * 
     * @return an unmodifiable {@link Set} with all available {@link OAuthScope}s
     */
    Set<OAuthScope> getAvailableScopes();
}

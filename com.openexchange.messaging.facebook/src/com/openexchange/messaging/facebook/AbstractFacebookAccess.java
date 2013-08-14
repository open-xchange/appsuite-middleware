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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.messaging.facebook;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.scribe.model.Token;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.facebook.services.Services;
import com.openexchange.messaging.facebook.session.FacebookOAuthAccess;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link AbstractFacebookAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractFacebookAccess {

    protected static Set<String> KNOWN_FOLDER_IDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        MessagingFolder.ROOT_FULLNAME,
        FacebookConstants.FOLDER_WALL)));

    protected final Session session;

    protected final MessagingAccount messagingAccount;

    protected final FacebookOAuthAccess facebookOAuthAccess;

    protected final int id;

    protected final int user;

    protected final int cid;

    protected final String facebookUserId;

    protected final String facebookUserName;

    protected final org.scribe.oauth.OAuthService facebookOAuthService;

    protected final Token facebookAccessToken;

    protected volatile Locale userLocale;

    /**
     * Initializes a new {@link AbstractFacebookAccess}.
     */
    protected AbstractFacebookAccess(final FacebookOAuthAccess facebookOAuthAccess, final MessagingAccount messagingAccount, final Session session) {
        super();
        this.session = session;
        this.messagingAccount = messagingAccount;
        this.facebookOAuthAccess = facebookOAuthAccess;
        id = messagingAccount.getId();
        user = session.getUserId();
        cid = session.getContextId();
        this.facebookUserId = facebookOAuthAccess.getFacebookUserId();
        facebookUserName = facebookOAuthAccess.getFacebookUserName();
        this.facebookOAuthService = facebookOAuthAccess.getFacebookOAuthService();
        this.facebookAccessToken = facebookOAuthAccess.getFacebookAccessToken();
    }

    public String getFacebookUserName() {
        return facebookUserName;
    }

    protected Locale getUserLocale() throws OXException {
        Locale tmp = userLocale;
        if (null == tmp) {
            /*
             * Duplicate initialization isn't harmful; no "synchronized" needed
             */
            final ContextService cs = Services.getService(ContextService.class);
            userLocale = tmp = Services.getService(UserService.class).getUser(user, cs.getContext(cid)).getLocale();
        }
        return tmp;
    }

    /**
     * URL-encodes specified string.
     *
     * @param string The string
     * @return The URL-encoded string
     */
    protected static String encode(final String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return string;
        }
    }

    private static final String FQL_JSON_START = "https://api.facebook.com/method/fql.query?format=JSON&query=";

    /**
     * Performs specified FQL query and returns its result as a JSON object.
     *
     * @param fqlQuery The FQL query
     * @return The queried JSON object
     * @throws OXException If FQL query fails
     */
    protected JSONObject performFQLQuery(final String fqlQuery) throws OXException {
        try {
            final String encodedQuery = encode(fqlQuery);
            final JSONObject result =
                (JSONObject) facebookOAuthAccess.executeGETJsonRequest(new StringBuilder(FQL_JSON_START.length() + encodedQuery.length()).append(
                    FQL_JSON_START).append(encodedQuery));
            if (result.has("error")) {
                final JSONObject error = result.getJSONObject("error");
                final String type = error.optString("type");
                final String message = error.optString("message");
                if ("OAuthException".equals(type)) {
                    throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(null == message ? "" : message);
                }
                throw FacebookMessagingExceptionCodes.FQL_ERROR.create(null == type ? "<unknown>" : type, null == message ? "" : message);
            }
            return result;
        } catch (final JSONException e) {
            throw FacebookMessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FacebookMessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs specified FQL query and returns its result as a JSON result.
     *
     * @param fqlQuery The FQL query
     * @return The queried JSON result
     * @throws OXException If FQL query fails
     */
    protected <V extends JSONValue> V performFQLQuery(final Class<V> clazz, final String fqlQuery) throws OXException {
        try {
            final String encodedQuery = encode(fqlQuery);
            if (JSONObject.class.equals(clazz)) {
                final JSONObject result =
                    (JSONObject) facebookOAuthAccess.executeGETJsonRequest(new StringBuilder(
                        FQL_JSON_START.length() + encodedQuery.length()).append(FQL_JSON_START).append(encodedQuery));
                if (result.has("error")) {
                    final JSONObject error = result.getJSONObject("error");
                    final String type = error.optString("type");
                    final String message = error.optString("message");
                    if ("OAuthException".equals(type)) {
                        throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(null == message ? "" : message);
                    }
                    throw FacebookMessagingExceptionCodes.FQL_ERROR.create(
                        null == type ? "<unknown>" : type,
                        null == message ? "" : message);
                }
                return (V) result;
            }
            if (JSONArray.class.equals(clazz)) {
                return (V) (facebookOAuthAccess.executeGETJsonRequest(new StringBuilder(
                    FQL_JSON_START.length() + encodedQuery.length()).append(FQL_JSON_START).append(encodedQuery)));
            }
            throw new IllegalArgumentException("Unsupported return type: " + clazz.getName());
        } catch (final JSONException e) {
            throw FacebookMessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FacebookMessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}

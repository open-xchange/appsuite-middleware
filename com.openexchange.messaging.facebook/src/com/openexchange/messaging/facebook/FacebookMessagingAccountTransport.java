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

import static com.openexchange.messaging.facebook.utility.FacebookMessagingUtility.checkContent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.facebook.session.FacebookOAuthAccess;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility;
import com.openexchange.messaging.generic.Utility;
import com.openexchange.session.Session;

/**
 * {@link FacebookMessagingAccountTransport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookMessagingAccountTransport extends FacebookMessagingResource implements MessagingAccountTransport {

    /**
     * Initializes a new {@link FacebookMessagingAccountTransport}.
     *
     * @param messagingAccount The facebook account
     * @param session The session
     * @throws OXException If initialization fails
     */
    public FacebookMessagingAccountTransport(final MessagingAccount messagingAccount, final Session session) throws OXException {
        super(messagingAccount, session);
    }

    @Override
    public void transport(final MessagingMessage message, final Collection<MessagingAddressHeader> recipients) throws OXException {
        transport(message, recipients, facebookOAuthInfo, facebookOAuthInfo.getFacebookUserId());
    }

    /**
     * Transports given message; either a status update or a post on a user's wall.
     *
     * @param message The message
     * @param recipients The recipients
     * @param facebookRestClient The facebook REST client
     * @param facebookUserId The facebook user identifier
     * @throws OXException If transport fails
     */
    public static void transport(final MessagingMessage message, final Collection<MessagingAddressHeader> recipients, final FacebookOAuthAccess facebookOAuthAccess, final String facebookUserId) throws OXException {
        try {
            /*
             * Recipient identifier
             */
            final long targetId;
            if (null == recipients || recipients.isEmpty()) {
                final MessagingAddressHeader header =
                    (MessagingAddressHeader) message.getFirstHeader(MessagingHeader.KnownHeader.TO.toString());
                if (null == header) {
                    throw MessagingExceptionCodes.MISSING_PARAMETER.create(MessagingHeader.KnownHeader.TO.toString());
                }
                targetId = FacebookMessagingUtility.parseUnsignedLong(header.getAddress());
                if (targetId < 0) {
                    throw MessagingExceptionCodes.ADDRESS_ERROR.create(header.getAddress());
                }
            } else {
                if (recipients.size() > 1) {
                    throw MessagingExceptionCodes.UNEXPECTED_ERROR.create("Facebook supports only exactly one recipient.");
                }
                final String address = recipients.iterator().next().getAddress();
                targetId = FacebookMessagingUtility.parseUnsignedLong(address);
                if (targetId < 0) {
                    throw MessagingExceptionCodes.ADDRESS_ERROR.create(address);
                }
            }
            /*
             * Publish to recipient's stream
             */
            final MessagingContent content = message.getContent();
            if (content instanceof MultipartContent) {
                if (message.getContentType().startsWith("mulitpart/alternative")) {
                    /*-
                     * Get text/plain content from alternative content:
                     *
                     * Parse content to look for links?
                     */
                    final MultipartContent mp = (MultipartContent) content;
                    final StringContent stringContent = checkContent(StringContent.class, mp.get(0));
                    /*
                     * Post it
                     */
                    final OAuthRequest request =
                        new OAuthRequest(
                            Verb.POST,
                            "https://graph.facebook.com/" + targetId + "/feed?message=" + encode(stringContent.getData()));
                    facebookOAuthAccess.getFacebookOAuthService().signRequest(facebookOAuthAccess.getFacebookAccessToken(), request);
                    final Response response = request.send();
                    final JSONObject result = new JSONObject(response.getBody());
                    if (result.has("error")) {
                        final JSONObject error = result.getJSONObject("error");
                        final String type = error.optString("type");
                        final String msg = error.optString("message");
                        if ("OXException".equals(type)) {
                            throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(null == msg ? "" : msg);
                        }
                        throw FacebookMessagingExceptionCodes.FQL_ERROR.create(null == type ? "<unknown>" : type, null == msg ? "" : msg);
                    }
                }
            } else {
                /*
                 * Assume text/plain
                 */
                final StringContent stringContent = checkContent(StringContent.class, message);
                if (message.getContentType().startsWith("text/htm")) {
                    /*
                     * Post it
                     */
                    final OAuthRequest request =
                        new OAuthRequest(
                            Verb.POST,
                            "https://graph.facebook.com/" + targetId + "/feed?message=" + encode(Utility.textFormat(stringContent.getData())));
                    facebookOAuthAccess.getFacebookOAuthService().signRequest(facebookOAuthAccess.getFacebookAccessToken(), request);
                    final Response response = request.send();
                    final JSONObject result = new JSONObject(response.getBody());
                    if (result.has("error")) {
                        final JSONObject error = result.getJSONObject("error");
                        final String type = error.optString("type");
                        final String msg = error.optString("message");
                        if ("OAuthException".equals(type)) {
                            throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(null == msg ? "" : msg);
                        }
                        throw FacebookMessagingExceptionCodes.FQL_ERROR.create(null == type ? "<unknown>" : type, null == msg ? "" : msg);
                    }
                } else {
                    /*
                     * Post it
                     */
                    final OAuthRequest request =
                        new OAuthRequest(
                            Verb.POST,
                            "https://graph.facebook.com/" + targetId + "/feed?message=" + encode(stringContent.getData()));
                    facebookOAuthAccess.getFacebookOAuthService().signRequest(facebookOAuthAccess.getFacebookAccessToken(), request);
                    final Response response = request.send();
                    final JSONObject result = new JSONObject(response.getBody());
                    if (result.has("error")) {
                        final JSONObject error = result.getJSONObject("error");
                        final String type = error.optString("type");
                        final String msg = error.optString("message");
                        if ("OAuthException".equals(type)) {
                            throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(null == msg ? "" : msg);
                        }
                        throw FacebookMessagingExceptionCodes.FQL_ERROR.create(null == type ? "<unknown>" : type, null == msg ? "" : msg);
                    }
                }
            }
        } catch (final JSONException e) {
            throw FacebookMessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FacebookMessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * URL-encodes specified string.
     *
     * @param string The string
     * @return The URL-encoded string
     */
    private static String encode(final String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return string;
        }
    }

}

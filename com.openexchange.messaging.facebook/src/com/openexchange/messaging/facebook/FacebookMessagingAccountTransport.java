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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.util.Collection;
import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.IFacebookRestClient;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.StringContent;
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
     * @throws FacebookMessagingException If initialization fails
     */
    public FacebookMessagingAccountTransport(final MessagingAccount messagingAccount, final Session session) throws FacebookMessagingException {
        super(messagingAccount, session);
    }

    /**
     * Initializes a new {@link FacebookMessagingAccountTransport} for testing purpose.
     * 
     * @param login The facebook login
     * @param password The facebook password
     * @param apiKey The API key
     * @param secretKey The secret key
     */
    public FacebookMessagingAccountTransport(final String login, final String password, final String apiKey, final String secretKey) {
        super(login, password, apiKey, secretKey);
    }

    public void transport(final MessagingMessage message, final Collection<MessagingAddressHeader> recipients) throws MessagingException {
        transport(message, recipients, facebookSession.getFacebookRestClient(), facebookSession.getFacebookUserId());
    }

    /**
     * Transports given message; either a status update or a post on a user's wall.
     * 
     * @param message The message
     * @param recipients The recipients
     * @param facebookRestClient The facebook REST client
     * @param facebookUserId The facebook user identifier
     * @throws MessagingException If transport fails
     */
    public static void transport(final MessagingMessage message, final Collection<MessagingAddressHeader> recipients, final IFacebookRestClient<Object> facebookRestClient, final long facebookUserId) throws MessagingException {
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
                    facebookRestClient.stream_publish(
                        stringContent.getData(),
                        null,
                        null,
                        Long.valueOf(targetId),
                        Long.valueOf(facebookUserId));
                }
            } else {
                /*
                 * Assume text/plain
                 */
                final StringContent stringContent = checkContent(StringContent.class, message);
                if (message.getContentType().startsWith("text/htm")) {
                    facebookRestClient.stream_publish(
                        Utility.textFormat(stringContent.getData()),
                        null,
                        null,
                        Long.valueOf(targetId),
                        Long.valueOf(facebookUserId));
                } else {
                    facebookRestClient.stream_publish(
                        stringContent.getData(),
                        null,
                        null,
                        Long.valueOf(targetId),
                        Long.valueOf(facebookUserId));
                }
            }
        } catch (final FacebookException e) {
            throw FacebookMessagingException.create(e);
        }
    }

}

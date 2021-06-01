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

package com.openexchange.messaging.twitter;

import static com.openexchange.messaging.twitter.TwitterMessagingUtility.checkContent;
import static com.openexchange.messaging.twitter.TwitterMessagingUtility.parseUnsignedLong;
import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.StringContent;
import com.openexchange.session.Session;
import com.openexchange.twitter.TwitterExceptionCodes;

/**
 * {@link TwitterMessagingAccountTransport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingAccountTransport extends AbstractTwitterMessagingAccess implements MessagingAccountTransport {

    /**
     * Initializes a new {@link TwitterMessagingAccountTransport}.
     *
     * @throws OXException If initialization fails
     */
    public TwitterMessagingAccountTransport(final MessagingAccount account, final Session session) throws OXException {
        super(account, session);
    }

    @Override
    public void transport(final MessagingMessage message, final Collection<MessagingAddressHeader> recipients) throws OXException {
        final String messageType;
        {
            final MessagingHeader header = message.getFirstHeader(MessagingHeader.KnownHeader.MESSAGE_TYPE.toString());
            messageType = null == header ? null : header.getValue();
        }
        if (TwitterConstants.TYPE_DIRECT_MESSAGE.equalsIgnoreCase(messageType)) {
            /*
             * A direct message
             */
            try {
                final StringContent content = checkContent(StringContent.class, message);
                final String screenName;
                {
                    final MessagingHeader header = message.getFirstHeader(MessagingHeader.KnownHeader.TO.toString());
                    screenName = null == header ? null : header.getValue();
                }
                if (null == screenName) {
                    throw TwitterExceptionCodes.MISSING_PROPERTY.create(MessagingHeader.KnownHeader.TO.toString());
                }
                twitterAccess.sendDirectMessage(screenName, content.toString());
            } catch (OXException e) {
                throw e;
            }
        } else if (TwitterConstants.TYPE_RETWEET.equalsIgnoreCase(messageType)) {
            /*
             * A retweet
             */
            try {
                final StringContent content = checkContent(StringContent.class, message);
                final long inReplyTo;
                {
                    final MessagingHeader header = message.getFirstHeader(TwitterConstants.HEADER_STATUS_ID);
                    inReplyTo = null == header ? -1L : parseUnsignedLong(header.getValue());
                }
                if (inReplyTo > 0) {
                    twitterAccess.updateStatus(content.toString(), inReplyTo);
                } else {
                    twitterAccess.updateStatus(content.toString());
                }
            } catch (OXException e) {
                throw e;
            }

        } else {
            /*
             * A normal tweet
             */
            try {
                final StringContent content = checkContent(StringContent.class, message);
                twitterAccess.updateStatus(content.toString());
            } catch (OXException e) {
                throw e;
            }
        }
    }

    @Override
    public void close() {
        connected = false;
    }

    @Override
    public void connect() throws OXException {
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean cacheable() {
        // Nothing to cache
        return false;
    }

}

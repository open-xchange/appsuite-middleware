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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.ContentDisposition;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;
import com.openexchange.messaging.generic.internet.MimeContentDisposition;
import com.openexchange.messaging.generic.internet.MimeContentType;
import com.openexchange.twitter.Status;
import com.openexchange.twitter.User;

/**
 * {@link TwitterRetweetMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterRetweetMessage implements MessagingMessage {

    private static final long serialVersionUID = 4916165244981260891L;

    private static final ContentType CONTENT_TYPE;

    private static final ContentDisposition CONTENT_DISPOSITION;

    static {
        final ContentType contentType = new MimeContentType();
        contentType.setPrimaryType("text");
        contentType.setSubType("plain");
        CONTENT_TYPE = contentType;

        final ContentDisposition contentDisposition = new MimeContentDisposition();
        contentDisposition.setDisposition(MessagingPart.INLINE);
        CONTENT_DISPOSITION = contentDisposition;
    }

    private final Map<String, Collection<MessagingHeader>> headers;

    private final StringContent content;

    private final long size;

    /**
     * Initializes a new {@link TwitterRetweetMessage}.
     *
     * @param status The status to retweet
     * @param user The authenticated user
     */
    public TwitterRetweetMessage(final Status status, final User user) {
        super();
        /*
         * Assign string content and size
         */
        final String retweet;
        {
            final String text = status.getText();

            retweet =
                new StringBuilder(text.length() + 16).append("RT @").append(status.getUser().getScreenName()).append(": ").append(text).toString();
            final int len = retweet.length();

            final int maxTweetLength = TwitterConstants.MAX_TWEET_LENGTH;
            if (len > maxTweetLength) {
                content = new StringContent(retweet.substring(0, maxTweetLength));
                size = maxTweetLength;
            } else {
                content = new StringContent(retweet);
                size = len;
            }
        }
        /*
         * Assign headers
         */
        {
            final Map<String, Collection<MessagingHeader>> m = new HashMap<String, Collection<MessagingHeader>>(16);
            m.put(CONTENT_TYPE.getName(), wrap(CONTENT_TYPE));
            m.put(CONTENT_DISPOSITION.getName(), wrap(CONTENT_DISPOSITION));
            {
                final String name = MessagingHeader.KnownHeader.FROM.toString();
                m.put(name, wrap(MimeAddressMessagingHeader.valueOfPlain(name, user.getName(), user.getScreenName())));
            }
            {
                final String name = MessagingHeader.KnownHeader.SUBJECT.toString();
                m.put(name, getSimpleHeader(name, retweet));
            }
            {
                final String name = MessagingHeader.KnownHeader.MESSAGE_TYPE.toString();
                m.put(name, getSimpleHeader(name, TwitterConstants.TYPE_RETWEET));
            }
            {
                final String name = TwitterConstants.HEADER_STATUS_ID;
                m.put(name, getSimpleHeader(name, Long.toString(status.getId())));
            }
            headers = Collections.unmodifiableMap(m);
        }
    }

    @Override
    public int getColorLabel() {
        return 0;
    }

    @Override
    public int getFlags() {
        return 0;
    }

    @Override
    public String getFolder() {
        return MessagingFolder.ROOT_FULLNAME;
    }

    @Override
    public long getReceivedDate() {
        return -1L;
    }

    @Override
    public Collection<String> getUserFlags() {
        return Collections.emptyList();
    }

    @Override
    public MessagingContent getContent() throws OXException {
        return content;
    }

    @Override
    public String getDisposition() throws OXException {
        return MessagingPart.INLINE;
    }

    @Override
    public String getFileName() throws OXException {
        return null;
    }

    @Override
    public MessagingHeader getFirstHeader(final String name) throws OXException {
        final Collection<MessagingHeader> collection = getHeader(name);
        return null == collection ? null : (collection.isEmpty() ? null : collection.iterator().next());
    }

    @Override
    public Collection<MessagingHeader> getHeader(final String name) {
        return headers.get(name);
    }

    @Override
    public Map<String, Collection<MessagingHeader>> getHeaders() {
        return headers;
    }

    @Override
    public String getSectionId() {
        return null;
    }

    @Override
    public void writeTo(final OutputStream os) throws IOException, OXException {
        // Nothing to do
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public int getThreadLevel() {
        return 0;
    }

    @Override
    public ContentType getContentType() throws OXException {
        return CONTENT_TYPE;
    }

    private static Collection<MessagingHeader> wrap(final MessagingHeader... headers) {
        return Collections.unmodifiableCollection(Arrays.asList(headers));
    }

    private static Collection<MessagingHeader> getSimpleHeader(final String name, final String value) {
        return wrap(new StringMessageHeader(name, value));
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getPicture() {
        return null;
    }

    @Override
    public String getUrl() throws OXException {
        return null;
    }

}

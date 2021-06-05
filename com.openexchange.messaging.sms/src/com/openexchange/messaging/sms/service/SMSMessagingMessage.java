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

package com.openexchange.messaging.sms.service;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * A class for sending SMS messages
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public final class SMSMessagingMessage implements MessagingMessage {

    private static final String MESSAGE_ID = "smsMessage";

    private static final long serialVersionUID = 5324611878535898301L;

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

    private final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>(16);

    private final StringContent content;

    private final long size;

    private CaptchaParams params;

    /**
     * Initializes a new {@link SMSMessagingMessage}.
     *
     * @param recipient The recipient of the direct message
     * @param from The sending user
     */
    public SMSMessagingMessage(final String sender, final String receiver, final String message) {
        super();
        /*
         * Assign string content and size
         */
        content = new StringContent(message);
        size = message.length();
        /*
         * Assign headers
         */
        headers.put(CONTENT_TYPE.getName(), wrap(CONTENT_TYPE));
        headers.put(CONTENT_DISPOSITION.getName(), wrap(CONTENT_DISPOSITION));
        {
            final String name = MessagingHeader.KnownHeader.FROM.toString();
            headers.put(name, wrap(MimeAddressMessagingHeader.valueOfPlain(name, null, sender)));
        }
        {
            final String name = MessagingHeader.KnownHeader.TO.toString();
            headers.put(name, wrap(MimeAddressMessagingHeader.valueOfPlain(name, null, receiver)));
        }
        {
            final String name = MessagingHeader.KnownHeader.SUBJECT.toString();
            headers.put(name, getSimpleHeader(name, message));
        }
        {
            final String name = MessagingHeader.KnownHeader.MESSAGE_TYPE.toString();
            headers.put(name, getSimpleHeader(name, MESSAGE_ID));
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
    public MessagingContent getContent() {
        return content;
    }

    @Override
    public String getDisposition() {
        return MessagingPart.INLINE;
    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public MessagingHeader getFirstHeader(final String name) {
        final Collection<MessagingHeader> collection = getHeader(name);
        return null == collection ? null : (collection.isEmpty() ? null : collection.iterator().next());
    }

    @Override
    public Collection<MessagingHeader> getHeader(final String name) {
        return headers.get(name);
    }

    @Override
    public Map<String, Collection<MessagingHeader>> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public String getSectionId() {
        return null;
    }

    @Override
    public void writeTo(final OutputStream os) {
        // Nothing to do.
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
    public ContentType getContentType() {
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
    public String getUrl() {
        return null;
    }

    public static class CaptchaParams {
        private String challenge;
        private String response;
        private String address;

        public String getChallenge() {
            return challenge;
        }

        public void setChallenge(String challenge) {
            this.challenge = challenge;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public String getHost() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public void setCaptchaParameters(CaptchaParams params) {
        this.params = params;
    }

    public CaptchaParams getCaptchaParams() {
        return params;
    }

    public void addAttachment(String attachmentId) {
        if (headers.containsKey(MessagingPart.ATTACHMENT)) {
            headers.get(MessagingPart.ATTACHMENT).add(new StringMessageHeader(MessagingPart.ATTACHMENT, attachmentId));
        } else {
            List<MessagingHeader> list = new ArrayList<MessagingHeader>();
            list.add(new StringMessageHeader(MessagingPart.ATTACHMENT, attachmentId));
            headers.put(MessagingPart.ATTACHMENT, list);
        }
    }
}

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

package com.openexchange.messaging.twitter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;
import com.openexchange.messaging.generic.internet.MimeContentType;
import com.openexchange.messaging.generic.internet.MimeDateMessagingHeader;
import com.openexchange.session.Session;
import com.openexchange.twitter.Status;
import com.openexchange.twitter.User;

/**
 * {@link TwitterMessagingMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingMessage implements MessagingMessage {

    private static final long serialVersionUID = 839481777862807398L;

    private static final ContentType CONTENT_TYPE;

    static {
        final ContentType contentType = new MimeContentType();
        contentType.setPrimaryType("multipart");
        contentType.setSubType("alternative");
        CONTENT_TYPE = contentType;
    }

    private final Status status;

    private final Map<String, Collection<MessagingHeader>> headers;

    private final MultipartContent content;

    private final long size;

    private final String picture;

    /**
     * Initializes a new {@link TwitterMessagingMessage}.
     *
     * @param status The twitter status
     */
    public TwitterMessagingMessage(final Status status, final Session session) {
        super();
        this.status = status;

        final Map<String, Collection<MessagingHeader>> m = new HashMap<String, Collection<MessagingHeader>>(16);
        m.put(CONTENT_TYPE.getName(), wrap(CONTENT_TYPE));
        {
            final String name = MessagingHeader.KnownHeader.FROM.toString();
            final User user = status.getUser();
            m.put(name, wrap(MimeAddressMessagingHeader.valueOfPlain(name, user.getName(), user.getScreenName())));
        }
        {
            final String name = MessagingHeader.KnownHeader.SUBJECT.toString();
            m.put(name, getSimpleHeader(name, status.getText()));
        }
        {
            final String name = MessagingHeader.KnownHeader.DATE.toString();
            m.put(name, wrap(new MimeDateMessagingHeader(name, status.getCreatedAt())));
        }
        {
            final String name = MessagingHeader.KnownHeader.MESSAGE_TYPE.toString();
            m.put(name, getSimpleHeader(name, TwitterConstants.TYPE_TWEET));
        }
        headers = Collections.unmodifiableMap(m);

        final TwitterMultipartContent multipartContent = TwitterMultipartContent.newInstance(status, session);
        content = multipartContent;

        long sz = -1;
        try {
            sz = multipartContent.get(0).getSize() + multipartContent.get(1).getSize();
        } catch (final OXException e) {
            // Cannot occur
            org.slf4j.LoggerFactory.getLogger(TwitterMessagingMessage.class).error("", e);
        }
        size = sz;

        picture = status.getUser().getProfileImageURL().toString();
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
        return status.getCreatedAt().getTime();
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
        // Always top level
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
        return Collections.<MessagingHeader> singletonList(new StringMessageHeader(name, value));
    }

    @Override
    public String getId() {
        return Long.toString(status.getId());
    }

    @Override
    public String getPicture() {
        return picture;
    }

    @Override
    public String getUrl() throws OXException {
        return null;
    }

}

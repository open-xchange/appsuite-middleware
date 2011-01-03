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

package com.openexchange.messaging.twitter;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.html.HTMLService;
import com.openexchange.messaging.ContentDisposition;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.generic.Utility;
import com.openexchange.messaging.generic.internet.MimeContentDisposition;
import com.openexchange.messaging.generic.internet.MimeContentType;
import com.openexchange.messaging.twitter.services.TwitterMessagingServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.twitter.Status;

/**
 * {@link TwitterMessagingBodyPart}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingBodyPart implements MessagingBodyPart {

    private static final Pattern PATTERN_BODY = Pattern.compile("(<body[^>]*>)");

    private static final Pattern PATTERN_USERREF = Pattern.compile("@([\\d\\w]+)");

    private static final ContentType CONTENT_TYPE_PLAIN;

    private static final ContentType CONTENT_TYPE_HTML;

    private static final ContentDisposition CONTENT_DISPOSITION;

    static {

        CONTENT_TYPE_PLAIN = new MimeContentType();
        CONTENT_TYPE_PLAIN.setPrimaryType("text");
        CONTENT_TYPE_PLAIN.setSubType("plain");
        CONTENT_TYPE_PLAIN.setCharsetParameter("UTF-8");

        CONTENT_TYPE_HTML = new MimeContentType();
        CONTENT_TYPE_HTML.setPrimaryType("text");
        CONTENT_TYPE_HTML.setSubType("html");
        CONTENT_TYPE_HTML.setCharsetParameter("UTF-8");

        CONTENT_DISPOSITION = new MimeContentDisposition();
        CONTENT_DISPOSITION.setDisposition(MessagingPart.INLINE);
    }

    private final ContentType contentType;

    private final MultipartContent parent;

    private final Map<String, Collection<MessagingHeader>> headers;

    private final MessagingContent content;

    private final String sectionId;

    private final long size;

    /**
     * Initializes a new {@link TwitterMessagingBodyPart}.
     * 
     * @param status The twitter status
     * @param html <code>true</code> to convert status to HTML; otherwise <code>false</code> for text-plain
     * @param parent The parental multipart
     */
    public TwitterMessagingBodyPart(final Status status, final boolean html, final MultipartContent parent, final Session session) {
        super();
        this.parent = parent;
        contentType = html ? CONTENT_TYPE_HTML : CONTENT_TYPE_PLAIN;

        final Map<String, Collection<MessagingHeader>> map = new HashMap<String, Collection<MessagingHeader>>(16);
        map.put(contentType.getName(), wrap(contentType));
        map.put(CONTENT_DISPOSITION.getName(), wrap(CONTENT_DISPOSITION));
        headers = Collections.unmodifiableMap(map);

        if (html) {
            String htmlContent = Utility.getConformHTML(Utility.formatHrefLinks(Utility.htmlFormat(status.getText())), "UTF-8");
            {
                final Matcher userMat = PATTERN_USERREF.matcher(htmlContent);
                htmlContent = userMat.replaceAll("<a href=\"http://twitter.com/$1\">$0</a>");
            }
            {
                final Matcher m = PATTERN_BODY.matcher(htmlContent);
                htmlContent = m.replaceAll(MessageFormat.format("$1\r\n    <img src=\"{0}\" />", status.getUser().getProfileImageURL()));
            }
            
            final HTMLService htmlService = TwitterMessagingServiceRegistry.getServiceRegistry().getService(HTMLService.class);
            content = new StringContent(null == htmlService ? htmlContent: htmlService.replaceImages(htmlContent, session.getSessionID()));
            sectionId = "2";
            size = htmlContent.length();
        } else {
            final String text = status.getText();
            content = new StringContent(text);
            sectionId = "1";
            size = text.length();
        }
    }

    public void writeTo(final OutputStream os) throws IOException, MessagingException {
        // Nothing to do
    }

    public long getSize() throws MessagingException {
        return size;
    }

    public String getSectionId() {
        return sectionId;
    }

    public Map<String, Collection<MessagingHeader>> getHeaders() {
        return headers;
    }

    public MessagingHeader getFirstHeader(final String name) throws MessagingException {
        final Collection<MessagingHeader> collection = getHeader(name);
        return null == collection ? null : (collection.isEmpty() ? null : collection.iterator().next());
    }

    public Collection<MessagingHeader> getHeader(final String name) {
        return headers.get(name);
    }

    public String getFileName() throws MessagingException {
        return null;
    }

    public String getDisposition() throws MessagingException {
        return CONTENT_DISPOSITION.getDisposition();
    }

    public ContentType getContentType() throws MessagingException {
        return contentType;
    }

    public MessagingContent getContent() throws MessagingException {
        return content;
    }

    public MultipartContent getParent() throws MessagingException {
        return parent;
    }

    private static Collection<MessagingHeader> wrap(final MessagingHeader... headers) {
        return Collections.unmodifiableCollection(Arrays.asList(headers));
    }

}

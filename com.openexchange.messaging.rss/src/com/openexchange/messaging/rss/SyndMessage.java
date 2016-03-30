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

package com.openexchange.messaging.rss;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingHeader.KnownHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.Utility;
import com.openexchange.messaging.generic.internet.MimeContentType;
import com.openexchange.messaging.generic.internet.MimeMessagingBodyPart;
import com.openexchange.messaging.generic.internet.MimeMultipartContent;
import com.openexchange.proxy.ImageContentTypeRestriction;
import com.openexchange.proxy.ProxyRegistration;
import com.openexchange.proxy.ProxyRegistry;
import com.openexchange.session.Session;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndImage;

public class SyndMessage implements MessagingMessage {

    private static final long serialVersionUID = 7251763923455787058L;

    private static final String CONTENT_TYPE = "Content-Type";

    private final SyndEntry entry;
    private final Map<String, Collection<MessagingHeader>> headers = new HashMap<String, Collection<MessagingHeader>>();
    private MessagingContent content;
    private final String folder;
    private final SyndFeed feed;
    private final String sessionId;
    private boolean b_picUrl;
    private String picUrl;
    private final String id;

    public SyndMessage(final SyndFeed feed, final SyndEntry syndEntry, final String folder, final Session session) throws OXException {
        entry = syndEntry;
        this.folder = folder;
        this.feed = feed;
        this.sessionId = session.getSessionID();
        this.id = createId(entry.getLink(), entry.getTitle(), entry.getPublishedDate());

        addStringHeader(KnownHeader.SUBJECT, syndEntry.getTitle());
        final List<SyndContent> contents = syndEntry.getContents();
        // For now we'll only use the first content element

        if(contents.size() > 0) {
            final SyndContent content = contents.get(0);
            setContent(content, sessionId);
        } else if (entry.getDescription() != null){
            setContent(entry.getDescription(), sessionId);
        } else if (entry.getTitle() != null) {
            setContent(entry.getTitleEx(), sessionId);
        }
    }

    private String createId(final String link, final String title, final Date publishedDate) {
        final StringBuilder sb = new StringBuilder();
        if (link != null) {
            sb.append(link);
        }

        if (title != null) {
            sb.append(title);
        }

        if (publishedDate != null) {
            sb.append(publishedDate.getTime());
        }

        return "" + sb.toString().hashCode();
    }

    private void setContent(final SyndContent content, final String sessionId) throws OXException {
        String type = content.getType();
        if(type == null) {
            type = "text/plain";
        }
        if(knowsType(type)) {
            if(!type.startsWith("text")) {
                type = "text/"+type;
            }
        }

        if (isHTML(type) ) {
            final MimeMultipartContent multipart = new MimeMultipartContent();
            // Text plain
            {
                final MimeMessagingBodyPart textPart = new MimeMessagingBodyPart();
                textPart.setContent(new StringContent(Utility.textFormat(content.getValue())), "text/plain");
                multipart.addBodyPart(textPart);
            }
            // HTML
            {
                final MimeMessagingBodyPart htmlPart = new MimeMessagingBodyPart();
                final HtmlService htmlService = HTMLServiceProvider.getInstance().getHTMLService();
                if (null == htmlService) {
                    htmlPart.setContent(new StringContent(content.getValue()), type);
                } else {
                    htmlPart.setContent(new StringContent(htmlService.replaceImages(htmlService.sanitize(content.getValue(), null, false, null, null), sessionId)), type);
                }
                multipart.addBodyPart(htmlPart);
            }

            final MimeContentType contentType = new MimeContentType("multipart/alternative");
            addHeader(KnownHeader.CONTENT_TYPE, contentType);

            this.content = multipart;
        } else {
            final MimeContentType contentType = new MimeContentType(type);
            addHeader(KnownHeader.CONTENT_TYPE, contentType);
            this.content = new StringContent(content.getValue());
        }
    }

    private boolean isHTML(final String type) {
        return type.endsWith("html");
    }

    private boolean knowsType(final String type) {
        return type.contains("plain") || type.contains("html");
    }

    private void addStringHeader(final KnownHeader header, final String value) {
        headers.put(header.toString(), Arrays.asList((MessagingHeader)new StringMessageHeader(header.toString(), value)));
    }

    private void addHeader(final KnownHeader header, final MessagingHeader value) {
        headers.put(header.toString(), Arrays.asList(value));
    }

    @Override
    public int getColorLabel() throws OXException {
        return -1;
    }

    @Override
    public int getFlags() throws OXException {
        return -1;
    }

    @Override
    public String getFolder() {
        return folder;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getReceivedDate() {
        return ((Date) tryThese(entry.getPublishedDate(), entry.getUpdatedDate(), new Date(-1))).getTime();
    }

    @Override
    public int getThreadLevel() {
        return -1;
    }

    @Override
    public Collection<String> getUserFlags() throws OXException {
        final List categories = entry.getCategories();
        if(categories == null) {
            return null;
        }
        final List<String> strings = new LinkedList<String>();
        for(final Object cat : categories) {
            strings.add(cat.toString());
        }
        return strings;
    }

    @Override
    public MessagingContent getContent() throws OXException {
        return content;
    }

    @Override
    public ContentType getContentType() throws OXException {
        return (ContentType) getFirstHeader(CONTENT_TYPE);
    }

    @Override
    public String getDisposition() throws OXException {
        return INLINE;
    }

    @Override
    public String getFileName() throws OXException {
        return null;
    }

    @Override
    public MessagingHeader getFirstHeader(final String name) throws OXException {
        if(headers.containsKey(name)) {
            return headers.get(name).iterator().next();
        }
        return null;
    }

    @Override
    public Collection<MessagingHeader> getHeader(final String name) throws OXException {
        if(headers.containsKey(name)) {
            return headers.get(name);
        }
        return null;
    }

    @Override
    public Map<String, Collection<MessagingHeader>> getHeaders() throws OXException {
        return headers;
    }

    @Override
    public String getSectionId() {
        return null;
    }

    @Override
    public long getSize() throws OXException {
        return 0;
    }

    @Override
    public void writeTo(final OutputStream os) throws IOException, OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create();
    }

    @Override
    public String getPicture() {
        if (!b_picUrl) {
            final SyndImage image = ((entry.getSource() != null) ? entry.getSource() : feed).getImage();
            if (null != image) {
                try {
                    final URL imageUrl = new URL(image.getUrl());
                    /*
                     * Check presence of ProxyRegistry
                     */
                    final ProxyRegistry proxyRegistry = ProxyRegistryProvider.getInstance().getProxyRegistry();
                    if (null == proxyRegistry) {
                        org.slf4j.LoggerFactory.getLogger(SyndMessage.class).warn("Missing ProxyRegistry service. Replacing image URL skipped.");
                        return null;
                    }
                    picUrl = proxyRegistry.register(new ProxyRegistration(imageUrl, sessionId, ImageContentTypeRestriction.getInstance())).toString();
                } catch (final MalformedURLException e) {
                    /*
                     * Not a valid URL
                     */
                    org.slf4j.LoggerFactory.getLogger(SyndMessage.class).warn("Not a valid image URL. Replacing image URL skipped.", e);
                    picUrl = null;
                } catch (final OXException e) {
                    /*
                     * Not a valid URL
                     */
                    org.slf4j.LoggerFactory.getLogger(SyndMessage.class).warn("Proxying image URL failed. Replacing image URL skipped.", e);
                    picUrl = null;
                }
            } else {
                picUrl = null;
            }
            b_picUrl = true;
        }
        return picUrl;
    }

    protected Object tryThese(final Object...objects) {
        for (final Object object : objects) {
            if(object != null) {
                return object;
            }
        }
        return null;
    }

    @Override
    public String getUrl() throws OXException {
        return entry.getLink();
    }

}

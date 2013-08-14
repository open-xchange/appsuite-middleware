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

package com.openexchange.messaging.facebook.parser.stream;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.log.LogFactory;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.facebook.FacebookMessagingExceptionCodes;
import com.openexchange.messaging.facebook.FacebookURLConnectionContent;
import com.openexchange.messaging.facebook.services.Services;
import com.openexchange.messaging.facebook.utility.FacebookMessagingMessage;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility;
import com.openexchange.messaging.generic.Utility;
import com.openexchange.messaging.generic.internet.MimeContentDisposition;
import com.openexchange.messaging.generic.internet.MimeContentType;
import com.openexchange.messaging.generic.internet.MimeDateMessagingHeader;
import com.openexchange.messaging.generic.internet.MimeMessagingBodyPart;
import com.openexchange.messaging.generic.internet.MimeMultipartContent;
import com.openexchange.messaging.generic.internet.MimeStringMessagingHeader;
import com.openexchange.session.Session;

/**
 * {@link FacebookFQLStreamParser} - Parses a given Facebook stream element into a MIME message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookFQLStreamParser {

    private static final String HTML_ANCHOR_END = "</a>";

    private static final String HTML_ANCHOR_START = "<a href='";

    private static final String HTML_BR = "<br />";

    /**
     * Initializes a new {@link FacebookFQLStreamParser}.
     */
    private FacebookFQLStreamParser() {
        super();
    }

    private interface ItemHandler {

        void handleItem(Node item, FacebookMessagingMessage message) throws OXException;
    }

    private static final class MultipartProvider {

        private MimeMultipartContent multipartContent;

        MultipartProvider() {
            super();
        }

        public MimeMultipartContent getMultipartContent() {
            return multipartContent;
        }

        public void setMultipartContent(final MimeMultipartContent multipartContent) {
            this.multipartContent = multipartContent;
        }

    }

    private interface AttachmentHandler {

        void handleAttachment(NodeList attachNodes, int len, FacebookMessagingMessage message, MultipartProvider multipartProvider) throws OXException;
    }

    private static final Map<String, ItemHandler> ITEM_HANDLERS;

    private static final Map<String, AttachmentHandler> ATTACH_HANDLERS;

    static {
        {
            final Map<String, ItemHandler> m = new HashMap<String, ItemHandler>(8);
            m.put("post_id", new ItemHandler() {

                @Override
                public void handleItem(final Node item, final FacebookMessagingMessage message) {
                    final String content = item.getTextContent();
                    message.setId(content);
                    // message.setPostId(FacebookMessagingUtility.parseUnsignedLong(content));
                }
            });
            m.put("actor_id", new ItemHandler() {

                @Override
                public void handleItem(final Node item, final FacebookMessagingMessage message) throws OXException {
                    // message.setHeader(MessagingHeader.KnownHeader.FROM.toString(), item.getTextContent());
                    final String content = item.getTextContent();
                    final long fromId = FacebookMessagingUtility.parseUnsignedLong(null == content ? null : content.trim());
                    if (fromId < 0) {
                        com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamParser.class)).warn(
                            new StringBuilder("Field actor_id cannot be parsed to a long: ``").append(content).append("\u00b4\u00b4").toString());
                    }
                    message.setFromId(fromId);
                }
            });
            m.put("created_time", new ItemHandler() {

                @Override
                public void handleItem(final Node item, final FacebookMessagingMessage message) throws OXException {
                    final long time = FacebookMessagingUtility.parseUnsignedLong(item.getTextContent()) * 1000L;
                    message.setHeader(new MimeDateMessagingHeader(MessagingHeader.KnownHeader.DATE.toString(), time));
                    message.setReceivedDate(time);
                }
            });
            m.put("updated_time", new ItemHandler() {

                @Override
                public void handleItem(final Node item, final FacebookMessagingMessage message) throws OXException {
                    final long time = FacebookMessagingUtility.parseUnsignedLong(item.getTextContent()) * 1000L;
                    message.setHeader(new MimeDateMessagingHeader("X-Facebook-Updated-Time", time));
                }
            });
            m.put("filter_key", new ItemHandler() {

                @Override
                public void handleItem(final Node item, final FacebookMessagingMessage message) throws OXException {
                    if (item.hasChildNodes()) {
                        message.setHeader("X-Facebook-Filter-Key", item.getTextContent());
                    }
                }
            });
            m.put("message", new ItemHandler() {

                @Override
                public void handleItem(final Node item, final FacebookMessagingMessage message) throws OXException {
                    /*-
                     * Text is already HTML-escaped: '<' ==> &lt;
                     */
                    message.appendTextContent(item.getTextContent());
                }
            });
            ITEM_HANDLERS = Collections.unmodifiableMap(m);
        }

        final Map<String, AttachmentHandler> m = new HashMap<String, AttachmentHandler>(8);
        m.put("album", new AttachmentHandler() {

            @Override
            public void handleAttachment(final NodeList attachNodes, final int len, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {
                String name = null;
                String href = null;
                int flag = 0;
                for (int i = 0; flag < 2 && i < len; i++) {
                    final Node item = attachNodes.item(i);
                    if ("name".equals(item.getLocalName())) {
                        name = item.getTextContent();
                        flag++;
                    } else if ("href".equals(item.getLocalName())) {
                        href = item.getTextContent();
                        flag++;
                    }
                }
                if (null != href && null != name) {
                    final StringBuilder messageText = message.getMessageText();
                    messageText.append(HTML_BR);
                    final int pos = href.indexOf('?');
                    if (pos < 0) {
                        messageText.append(HTML_ANCHOR_START).append(href).append("'>").append(name).append(HTML_ANCHOR_END);
                    } else {
                        final int spos = href.indexOf("u=");
                        final int epos = href.indexOf("&h=", spos + 2);
                        if (epos < 0 || spos < 0) {
                            messageText.append(HTML_ANCHOR_START).append(href).append("'>").append(name).append(HTML_ANCHOR_END);
                        } else {
                            /*-
                             * Extract real URL from:
                             * http://www.facebook.com/l.php?u=http%253A%252F%252Fwww.open-xchange.com&amp;h=f60781c95f3b41983168bbd0d0f52c95
                             */
                            try {
                                messageText.append(HTML_ANCHOR_START).append(
                                    URLDecoder.decode(URLDecoder.decode(href.substring(spos + 2, epos), "ISO-8859-1"), "ISO-8859-1")).append(
                                    "'>").append(name).append(HTML_ANCHOR_END);
                            } catch (final UnsupportedEncodingException e) {
                                messageText.append(HTML_ANCHOR_START).append(href).append("'>").append(name).append(HTML_ANCHOR_END);
                            }
                        }
                    }
                }
            }

        });
        m.put("link", new AttachmentHandler() {

            @Override
            public void handleAttachment(final NodeList attachNodes, final int len, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) {
                String name = null;
                String href = null;
                int flag = 0;
                for (int i = 0; flag < 2 && i < len; i++) {
                    final Node item = attachNodes.item(i);
                    if ("name".equals(item.getLocalName())) {
                        name = item.getTextContent();
                        flag++;
                    } else if ("href".equals(item.getLocalName())) {
                        href = item.getTextContent();
                        flag++;
                    }
                }
                if (null != href && null != name) {
                    final StringBuilder messageText = message.getMessageText();
                    messageText.append(HTML_BR);
                    final int pos = href.indexOf('?');
                    if (pos < 0) {
                        messageText.append(HTML_ANCHOR_START).append(href).append("'>").append(name).append(HTML_ANCHOR_END);
                    } else {
                        final int spos = href.indexOf("u=");
                        final int epos = href.indexOf("&h=", spos + 2);
                        if (epos < 0 || spos < 0) {
                            messageText.append(HTML_ANCHOR_START).append(href).append("'>").append(name).append(HTML_ANCHOR_END);
                        } else {
                            /*-
                             * Extract real URL from:
                             * http://www.facebook.com/l.php?u=http%253A%252F%252Fwww.open-xchange.com&amp;h=f60781c95f3b41983168bbd0d0f52c95
                             */
                            try {
                                messageText.append(HTML_ANCHOR_START).append(
                                    URLDecoder.decode(URLDecoder.decode(href.substring(spos + 2, epos), "ISO-8859-1"), "ISO-8859-1")).append(
                                    "'>").append(name).append(HTML_ANCHOR_END);
                            } catch (final UnsupportedEncodingException e) {
                                messageText.append(HTML_ANCHOR_START).append(href).append("'>").append(name).append(HTML_ANCHOR_END);
                            }
                        }
                    }
                }
            } // End of handleAttachment
        });
        m.put("group", new AttachmentHandler() {

            @Override
            public void handleAttachment(final NodeList attachNodes, final int len, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {
                /*
                 * A group post
                 */
                message.setGroup(true);
                /*
                 * Get group's link
                 */
                String name = null;
                String href = null;
                int flag = 0;
                for (int i = 0; flag < 2 && i < len; i++) {
                    final Node item = attachNodes.item(i);
                    if ("name".equals(item.getLocalName())) {
                        name = item.getTextContent();
                        flag++;
                    } else if ("href".equals(item.getLocalName())) {
                        href = item.getTextContent();
                        flag++;
                    }
                }
                if (null != href && null != name) {
                    final StringBuilder messageText = message.getMessageText();
                    messageText.append(HTML_BR);
                    final int pos = href.indexOf('?');
                    if (pos < 0) {
                        messageText.append(HTML_ANCHOR_START).append(href).append("'>").append(name).append(HTML_ANCHOR_END);
                    } else {
                        final int spos = href.indexOf("u=");
                        final int epos = href.indexOf("&h=", spos + 2);
                        if (epos < 0 || spos < 0) {
                            messageText.append(HTML_ANCHOR_START).append(href).append("'>").append(name).append(HTML_ANCHOR_END);
                        } else {
                            /*-
                             * Extract real URL from:
                             * http://www.facebook.com/l.php?u=http%253A%252F%252Fwww.open-xchange.com&amp;h=f60781c95f3b41983168bbd0d0f52c95
                             */
                            try {
                                messageText.append(HTML_ANCHOR_START).append(
                                    URLDecoder.decode(URLDecoder.decode(href.substring(spos + 2, epos), "ISO-8859-1"), "ISO-8859-1")).append(
                                    "'>").append(name).append(HTML_ANCHOR_END);
                            } catch (final UnsupportedEncodingException e) {
                                messageText.append(HTML_ANCHOR_START).append(href).append("'>").append(name).append(HTML_ANCHOR_END);
                            }
                        }
                    }
                }
            } // End of handleAttachment
        });
        m.put("video", new AttachmentHandler() {

            @Override
            public void handleAttachment(final NodeList attachNodes, final int len, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {
                String sourceURL = null;
                String ext = null;
                /*
                 * "media" node
                 */
                final Node media = getNodeByName("media", attachNodes, len);
                if (null != media) {
                    final Node streamMedia = getNodeByName("stream_media", media);
                    if (null != streamMedia) {
                        /*
                         * "video" node
                         */
                        final Node video = getNodeByName("video", streamMedia);
                        if (null != video) {
                            final Node sourceUrlNode = getNodeByName("source_url", video);
                            if (null != sourceUrlNode) {
                                sourceURL = sourceUrlNode.getTextContent();

                                final int pos = sourceURL.lastIndexOf('.');
                                if (pos >= 0) {
                                    final String extension = sourceURL.substring(pos + 1);
                                    if (!"application/octet-stream".equals(Utility.getContentTypeByExtension(extension))) {
                                        /*
                                         * A known extension
                                         */
                                        ext = extension;
                                    }
                                }
                            }
                        }

                    }
                }
                /*
                 * Source URL present?
                 */
                if (null != sourceURL) {
                    try {
                        final FacebookURLConnectionContent content = new FacebookURLConnectionContent(sourceURL, true);
                        /*
                         * Create part
                         */
                        final MimeMessagingBodyPart part = new MimeMessagingBodyPart();
                        final String ct;
                        final String filename;
                        {
                            final MimeContentType mct = new MimeContentType(content.getMimeType());
                            if (null == ext) {
                                filename = new StringBuilder("video.").append(Utility.getFileExtensions(mct.getBaseType()).get(0)).toString();
                            } else {
                                filename = new StringBuilder("video.").append(ext).toString();
                            }
                            mct.setNameParameter(filename);
                            ct = mct.toString();
                        }
                        part.setContent(content, ct);
                        part.setHeader("Content-Type", ct);
                        /*
                         * Force base64 encoding to keep data as it is
                         */
                        part.setHeader("Content-Transfer-Encoding", "base64");
                        {
                            final MimeContentDisposition mcd = new MimeContentDisposition();
                            mcd.setDisposition("attachment");
                            mcd.setFilenameParameter(filename);
                            part.setHeader("Content-Disposition", mcd.toString());
                        }
                        /*
                         * Add to multipart
                         */
                        final MimeMultipartContent multipartContent = new MimeMultipartContent();
                        multipartContent.addBodyPart(part);
                        multipartProvider.setMultipartContent(multipartContent);
                    } catch (final OXException e) {
                        if (!MessagingExceptionCodes.IO_ERROR.equals(e)) {
                            throw e;
                        }
                        // Something went wrong loading URL content... Ignore it
                        LogFactory.getLog(FacebookFQLStreamParser.class).debug("Couldn't load URL: " + sourceURL, e);
                    }
                }

            } // End of handleAttachment
        });
        m.put("photo", new AttachmentHandler() {

            @Override
            public void handleAttachment(final NodeList attachNodes, final int len, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {
                String sourceUrlBig = null; // src_big - The URL to the full-sized version of the photo being queried. The image can have a maximum width or height of 960px. This URL may be blank.
                String sourceUrl = null; // src - The URL to the album view version of the photo being queried. The image can have a maximum width or height of 130px. This URL may be blank.
                String ext = null;
                /*
                 * "media" node
                 */
                final Node media = getNodeByName("media", attachNodes, len);
                // System.out.println(FacebookMessagingUtility.toString(media));
                if (null != media) {
                    final Node streamMedia = getNodeByName("stream_media", media);
                    if (null != streamMedia) {
                        Node src = getNodeByName("src_big", streamMedia);
                        if (null != src) {
                            sourceUrlBig = src.getTextContent();
                            final int pos = sourceUrlBig.lastIndexOf('.');
                            if (pos >= 0) {
                                final String extension = sourceUrlBig.substring(pos + 1);
                                if (!"application/octet-stream".equals(Utility.getContentTypeByExtension(extension))) {
                                    /*
                                     * A known extension
                                     */
                                    ext = extension;
                                }
                            }
                        } else {
                            /*
                             * "src" node
                             */
                            src = getNodeByName("src", streamMedia);
                            if (null != src) {
                                sourceUrl = src.getTextContent();
                                final int pos = sourceUrl.lastIndexOf('.');
                                if (pos >= 0) {
                                    final String extension = sourceUrl.substring(pos + 1);
                                    final String prefix = sourceUrl.substring(0, pos);
                                    if (prefix.toLowerCase(Locale.US).endsWith("_s")) {
                                        sourceUrl = prefix.substring(0, prefix.length() - 2) + "_b." + extension;
                                    }
                                    if (!"application/octet-stream".equals(Utility.getContentTypeByExtension(extension))) {
                                        /*
                                         * A known extension
                                         */
                                        ext = extension;
                                    }
                                }
                            }
                        }
                    }
                }
                /*
                 * Source URL present?
                 */
                if (null != sourceUrlBig) {
                    final StringBuilder messageText = message.getMessageText();
                    messageText.append(HTML_BR);
                    messageText.append("<img src='").append(sourceUrlBig);
                    if (null == ext) {
                        messageText.append("' />");
                    } else {
                        messageText.append("' alt=\"photo.").append(ext).append("\" />");
                    }
                } else if (null != sourceUrl) {
                    final StringBuilder messageText = message.getMessageText();
                    messageText.append(HTML_BR);
                    messageText.append("<img src='").append(sourceUrl);
                    if (null == ext) {
                        messageText.append("' />");
                    } else {
                        messageText.append("' alt=\"photo.").append(ext).append("\" />");
                    }
                }
            }
        });
        m.put("swf", new AttachmentHandler() {

            @Override
            public void handleAttachment(final NodeList attachNodes, final int len, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {
                String sourceURL = null;
                /*
                 * "media" node
                 */
                final Node media = getNodeByName("media", attachNodes, len);
                if (null != media) {
                    final Node streamMedia = getNodeByName("stream_media", media);
                    if (null != streamMedia) {
                        final Node swf = getNodeByName("swf", streamMedia);
                        if (null != swf) {
                            /*
                             * "source_url" node
                             */
                            final Node src = getNodeByName("source_url", swf);
                            if (null != src) {
                                sourceURL = src.getTextContent();
                            }
                        }
                    }
                }
                String nameStr = null;
                /*
                 * "name" node
                 */
                final Node name = getNodeByName("name", attachNodes, len);
                if (null != name) {
                    nameStr = name.getTextContent();
                }
                /*
                 * Source URL present?
                 */
                if (null != sourceURL && null != nameStr) {
                    final StringBuilder messageText = message.getMessageText();
                    messageText.append(HTML_BR);
                    final int pos = sourceURL.indexOf('?');
                    if (pos < 0) {
                        messageText.append(HTML_ANCHOR_START).append(sourceURL).append("'>").append(nameStr).append(HTML_ANCHOR_END);
                    } else {
                        final int spos = sourceURL.indexOf("u=");
                        final int epos = sourceURL.indexOf("&h=", spos + 2);
                        if (epos < 0 || spos < 0) {
                            messageText.append(HTML_ANCHOR_START).append(sourceURL).append("'>").append(nameStr).append(HTML_ANCHOR_END);
                        } else {
                            /*-
                             * Extract real URL from:
                             * http://www.facebook.com/l.php?u=http%253A%252F%252Fwww.open-xchange.com&amp;h=f60781c95f3b41983168bbd0d0f52c95
                             */
                            try {
                                messageText.append(HTML_ANCHOR_START).append(
                                    URLDecoder.decode(URLDecoder.decode(sourceURL.substring(spos + 2, epos), "ISO-8859-1"), "ISO-8859-1")).append(
                                    "'>").append(nameStr).append(HTML_ANCHOR_END);
                            } catch (final UnsupportedEncodingException e) {
                                messageText.append(HTML_ANCHOR_START).append(sourceURL).append("'>").append(nameStr).append(HTML_ANCHOR_END);
                            }
                        }
                    }
                }
            }
        });
        m.put("event", new AttachmentHandler() {

            @Override
            public void handleAttachment(final NodeList attachNodes, final int len, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {

                final Node name = getNodeByName("name", attachNodes, len);
                if (null != name) {
                    final StringBuilder messageText = message.getMessageText();
                    final Node href = getNodeByName("href", attachNodes, len);
                    if (null == href) {
                        messageText.append(HTML_BR).append(name.getTextContent());
                    } else {
                        messageText.append("<br /><a href='").append(href.getTextContent()).append("'>").append(name.getTextContent()).append(
                            HTML_ANCHOR_END);
                    }

                    final Node properties = getNodeByName("properties", attachNodes, len);
                    if (null != properties) {
                        final List<Node> streamProps = getNodesByName("stream_property", properties);
                        for (final Node streamProperty : streamProps) {
                            final Node nameProperty = getNodeByName("name", streamProperty);
                            if (null != nameProperty) {
                                messageText.append(HTML_BR).append(nameProperty.getTextContent());
                                messageText.append(": ").append(getNodeByName("text", streamProperty).getTextContent());
                            }
                        }
                    }
                }
            }
        });

        ATTACH_HANDLERS = Collections.unmodifiableMap(m);
    }

    /**
     * Parses given Facebook stream element into a MIME message.
     *
     * @param streamElement The Facebook stream element
     * @param locale The user's locale
     * @return The resulting MIME message
     * @throws OXException If parsing fails
     */
    public static FacebookMessagingMessage parseStreamDOMElement(final Element streamElement, final Locale locale, final Session session) throws OXException {
        if (!streamElement.hasChildNodes()) {
            return null;
        }
        checkFacebookError(streamElement);
        final FacebookMessagingMessage message = new FacebookMessagingMessage(locale);
        /*
         * Iterate child nodes
         */
        boolean anyHandlerFound = false;
        Node attachmentNode = null;
        {
            final NodeList childNodes = streamElement.getChildNodes();
            final int len = childNodes.getLength();
            for (int i = 0; i < len; i++) {
                final Node item = childNodes.item(i);
                final String localName = item.getLocalName();
                if (null != localName) {
                    final ItemHandler itemHandler = ITEM_HANDLERS.get(localName);
                    if (null == itemHandler) {
                        if ("attachment".equals(localName)) {
                            attachmentNode = item;
                        } else {
                            com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamParser.class)).warn("Un-handled item: " + localName);
                        }
                    } else {
                        itemHandler.handleItem(item, message);
                        anyHandlerFound = true;
                    }
                }
            }
        }
        /*
         * Add subject & size
         */
        if (!anyHandlerFound && null == attachmentNode) {
            /*
             * Empty message... ignore it!
             */
            com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamParser.class)).warn("Empty Facebook message detected:\n" + streamElement);
            return null;
        }
        /*
         * Check attachment node
         */
        final MultipartProvider multipartProvider = new MultipartProvider();
        boolean attachmentHandlerFound = false;
        if (null != attachmentNode && attachmentNode.hasChildNodes()) {
            final NodeList attachmentChildNodes = attachmentNode.getChildNodes();
            final int len = attachmentChildNodes.getLength();
            for (int i = 0; i < len; i++) {
                final Node item = attachmentChildNodes.item(i);
                if ("fb_object_type".equals(item.getLocalName())) {
                    if (item.hasChildNodes()) {
                        /*
                         * A file attachment is present
                         */
                        final String type = item.getTextContent();
                        final AttachmentHandler attachmentHandler = ATTACH_HANDLERS.get(type);
                        if (null == attachmentHandler) {
                            final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamParser.class));
                            logger.warn("Unknown attachment type: " + type);
                            logger.debug("Stream element:\n" + Utility.prettyPrintXML(streamElement));
                        } else {
                            attachmentHandler.handleAttachment(attachmentChildNodes, len, message, multipartProvider);
                        }
                        attachmentHandlerFound = true;
                        break;
                    }
                    /*
                     * A link attachment; append it to message
                     */
                    final AttachmentHandler attachmentHandler = ATTACH_HANDLERS.get("link");
                    attachmentHandler.handleAttachment(attachmentChildNodes, len, message, multipartProvider);
                    attachmentHandlerFound = true;
                    break;
                }
            }
            if (!attachmentHandlerFound) {
                final Node media = getNodeByName("media", attachmentChildNodes, len);
                if (null != media) {
                    final Node streamMedia = getNodeByName("stream_media", media);
                    if (null != streamMedia) {
                        final Node type = getNodeByName("type", streamMedia);
                        if (null != type) {
                            final String sType = type.getTextContent();
                            final AttachmentHandler attachmentHandler = ATTACH_HANDLERS.get(sType);
                            if (null == attachmentHandler) {
                                final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamParser.class));
                                logger.warn("Unknown attachment type: " + sType);
                                logger.debug("Stream element:\n" + Utility.prettyPrintXML(streamElement));
                            } else {
                                attachmentHandler.handleAttachment(attachmentChildNodes, len, message, multipartProvider);
                            }
                            attachmentHandlerFound = true;
                        }
                    }
                }
            }
        }
        /*
         * Set subject & size
         */
        final StringBuilder messageText = message.getMessageText();
        final int size = messageText.length();
        /*
         * Debug
         */
        if (size <= 0 && !attachmentHandlerFound) {
            final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamParser.class));
            logger.debug("Stream element lead to empty message:\n" + Utility.prettyPrintXML(streamElement));
        }
        message.setSize(size);
        final String htmlContent;
        final String preparedContent;
        {
            String tmp = messageText.toString();
            final HtmlService service = Services.getService(HtmlService.class);
            htmlContent = service.replaceImages(tmp, session.getSessionID());
            tmp = replaceImages(tmp);
            preparedContent = service.replaceImages(tmp, session.getSessionID());
        }
        final String subject = FacebookMessagingUtility.abbreviate(htmlContent, 140);
        message.setHeader(new MimeStringMessagingHeader(MessagingHeader.KnownHeader.SUBJECT.toString(), subject));
        message.setToString(subject);
        /*
         * Set content
         */
        final MimeMultipartContent multipartContent = multipartProvider.getMultipartContent();
        if (null == multipartContent) {
            message.setContent(createAlternative(htmlContent, preparedContent));
        } else {
            final MimeMessagingBodyPart altBodyPart = new MimeMessagingBodyPart();
            altBodyPart.setContent(createAlternative(htmlContent, preparedContent));
            multipartContent.addBodyPart(altBodyPart, 0);
            message.setContent(multipartContent);
        }
        /*
         * Return parsed message
         */
        return message;
    }

    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static String replaceImages(final String content) {
        if (null == content) {
            return null;
        }
        try {
            final Matcher imgMatcher = IMG_PATTERN.matcher(content);
            if (imgMatcher.find()) {
                /*
                 * Start replacing with href
                 */
                final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(content.length());
                int lastMatch = 0;
                do {
                    sb.append(content.substring(lastMatch, imgMatcher.start()));
                    final String imgTag = imgMatcher.group();
                    replaceWithSrcAttribute(imgTag, sb);
                    lastMatch = imgMatcher.end();
                } while (imgMatcher.find());
                sb.append(content.substring(lastMatch));
                return sb.toString();
            }

        } catch (final Exception e) {
            // Ignore
        }
        return content;
    }

    private static final Pattern SRC_PATTERN = Pattern.compile(
        "(?:src=\"([^\"]*)\")|(?:src='([^']*)')|(?:src=[^\"']([^\\s>]*))",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static void replaceWithSrcAttribute(final String imgTag, final com.openexchange.java.StringAllocator sb) {
        final Matcher srcMatcher = SRC_PATTERN.matcher(imgTag);
        if (srcMatcher.find()) {
            /*
             * Extract URL
             */
            int group = 1;
            String urlStr = srcMatcher.group(group);
            if (urlStr == null) {
                urlStr = srcMatcher.group(++group);
                if (urlStr == null) {
                    urlStr = srcMatcher.group(++group);
                }
            }
            if (urlStr == null) {
                sb.append(imgTag);
            } else {
                sb.append(urlStr);
            }
        } else {
            sb.append(imgTag);
        }
    }

    private static void checkFacebookError(final Element streamElement) throws OXException {
        long errorCode = -1L;
        String errorMsg = null;
        final NodeList childNodes = streamElement.getChildNodes();
        final int length = childNodes.getLength();
        for (int i = 0; i < length; i++) {
            final Node item = childNodes.item(i);
            final String localName = item.getLocalName();
            if (null != localName) {
                if ("error_code".equals(localName)) {
                    errorCode = FacebookMessagingUtility.parseUnsignedLong(item.getTextContent());
                } else if ("error_msg".equals(localName)) {
                    errorMsg = item.getTextContent();
                }
            }
        }
        if (errorCode >= 0) {
            throw FacebookMessagingExceptionCodes.FB_API_ERROR.create(Long.valueOf(errorCode), null == errorMsg ? "" : errorMsg);
        }
    }

    /**
     * Gets the first node with specified name occurring as child below given node.
     *
     * @param name The node name to look-up
     * @param node The parent node
     * @return The appropriate node or <code>null</code> if none found
     */
    static Node getNodeByName(final String name, final Node node) {
        if (null == name || null == node || !node.hasChildNodes()) {
            return null;
        }
        final NodeList nodes = node.getChildNodes();
        return getNodeByName(name, nodes, nodes.getLength());
    }

    /**
     * Gets the first node with specified name occurring in given node list.
     *
     * @param name The node name to look-up
     * @param nodes The node list
     * @param len The length of the node list
     * @return The appropriate node or <code>null</code> if none found
     */
    static Node getNodeByName(final String name, final NodeList nodes, final int len) {
        for (int i = 0; i < len; i++) {
            final Node item = nodes.item(i);
            if (name.equals(item.getLocalName())) {
                return item;
            }
        }
        return null;
    }

    /**
     * Gets all nodes with specified name occurring as child below given node.
     *
     * @param name The node name to look-up
     * @param node The parent node
     * @return The appropriate nodes
     */
    static List<Node> getNodesByName(final String name, final Node node) {
        if (null == name || null == node || !node.hasChildNodes()) {
            return Collections.emptyList();
        }
        final NodeList nodes = node.getChildNodes();
        final int len = nodes.getLength();
        final List<Node> ret = new ArrayList<Node>(len);
        for (int i = 0; i < len; i++) {
            final Node item = nodes.item(i);
            if (name.equals(item.getLocalName())) {
                ret.add(item);
            }
        }
        return ret;
    }

    private static final String HTML_SPACE = "&#160;";

    private static final String UTF_8 = "UTF-8";

    private static MimeMultipartContent createAlternative(final String messageText, final String altText) throws OXException {
        final MimeMultipartContent alt = new MimeMultipartContent("alternative");
        {
            final MimeMessagingBodyPart text = new MimeMessagingBodyPart();
            text.setText(Utility.textFormat(null == altText ? messageText : altText), UTF_8, "plain");
            text.setHeader("MIME-Version", "1.0");
            text.setHeader("Content-Type", "text/plain; charset=UTF-8");
            alt.addBodyPart(text);
        }
        {
            final MimeMessagingBodyPart html = new MimeMessagingBodyPart();
            final String contentType = "text/html; charset=UTF-8";
            if (messageText == null || messageText.length() == 0) {
                html.setContent(new StringContent(Utility.getConformHTML(HTML_SPACE, UTF_8).replaceFirst(HTML_SPACE, "")), contentType);
            } else {
                html.setContent(new StringContent(Utility.getConformHTML(messageText, UTF_8)), contentType);
            }
            html.setHeader("MIME-Version", "1.0");
            html.setHeader("Content-Type", contentType);
            alt.addBodyPart(html);
        }
        return alt;
    }

}

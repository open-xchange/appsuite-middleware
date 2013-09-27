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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Strings;
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
 * {@link FacebookFQLStreamJsonParser} - Parses a given Facebook stream element into a MIME message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookFQLStreamJsonParser {

    private static final String HTML_ANCHOR_END = "</a>";
    private static final String HTML_ANCHOR_START = "<a href='";
    private static final String HTML_BR = "<br>";

    protected static volatile Boolean addAsBinaryParts;
    protected static boolean addAsBinaryParts() {
        Boolean b = addAsBinaryParts;
        if (null == b) {
            synchronized (FacebookFQLStreamJsonParser.class) {
                b = addAsBinaryParts;
                if (null == b) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return false;
                    }
                    b = Boolean.valueOf(service.getBoolProperty("com.openexchange.messaging.facebook.addAsBinaryParts", false));
                    addAsBinaryParts = b;
                }
            }
        }
        return b.booleanValue();
    }

    /**
     * Initializes a new {@link FacebookFQLStreamJsonParser}.
     */
    private FacebookFQLStreamJsonParser() {
        super();
    }

    private interface ItemHandler {

        void handleItem(JSONObject streamInformation, FacebookMessagingMessage message) throws OXException, JSONException;
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

        boolean handleAttachment(JSONObject attachmentInformation, FacebookMessagingMessage message, MultipartProvider multipartProvider) throws OXException, JSONException;
    }

    private static final Map<String, ItemHandler> ITEM_HANDLERS;

    private static final Map<String, AttachmentHandler> ATTACH_HANDLERS;

    static {
        {
            final Map<String, ItemHandler> m = new HashMap<String, ItemHandler>(8);
            m.put("post_id", new ItemHandler() {

                @Override
                public void handleItem(final JSONObject streamInformation, final FacebookMessagingMessage message) throws JSONException {
                    final String content = streamInformation.getString("post_id");
                    message.setId(content);
                    // message.setPostId(FacebookMessagingUtility.parseUnsignedLong(content));
                }
            });
            m.put("actor_id", new ItemHandler() {

                @Override
                public void handleItem(final JSONObject streamInformation, final FacebookMessagingMessage message) throws OXException {
                    // message.setHeader(MessagingHeader.KnownHeader.FROM.toString(), item.getTextContent());
                    final long fromId = streamInformation.optLong("actor_id", -1L);
                    if (fromId < 0) {
                        com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamJsonParser.class)).warn(
                            new StringBuilder("Field actor_id cannot be parsed to a long: ``").append(streamInformation.opt("actor_id")).append("\u00b4\u00b4").toString());
                    }
                    message.setFromId(fromId);
                }
            });
            m.put("created_time", new ItemHandler() {

                @Override
                public void handleItem(final JSONObject streamInformation, final FacebookMessagingMessage message) throws OXException, JSONException {
                    final long time = streamInformation.getLong("created_time") * 1000L;
                    message.setHeader(new MimeDateMessagingHeader(MessagingHeader.KnownHeader.DATE.toString(), time));
                    message.setReceivedDate(time);
                }
            });
            m.put("updated_time", new ItemHandler() {

                @Override
                public void handleItem(final JSONObject streamInformation, final FacebookMessagingMessage message) throws OXException, JSONException {
                    final long time = streamInformation.getLong("updated_time") * 1000L;
                    message.setHeader(new MimeDateMessagingHeader("X-Facebook-Updated-Time", time));
                }
            });
            m.put("filter_key", new ItemHandler() {

                @Override
                public void handleItem(final JSONObject streamInformation, final FacebookMessagingMessage message) throws OXException {
                    message.setHeader("X-Facebook-Filter-Key", streamInformation.optString("filter_key", null));
                }
            });
            m.put("message", new ItemHandler() {

                @Override
                public void handleItem(final JSONObject streamInformation, final FacebookMessagingMessage message) throws OXException, JSONException {
                    /*-
                     * Text is already HTML-escaped: '<' ==> &lt;
                     */
                    message.appendTextContent(streamInformation.getString("message"));
                }
            });
            ITEM_HANDLERS = Collections.unmodifiableMap(m);
        }

        final Map<String, AttachmentHandler> m = new HashMap<String, AttachmentHandler>(8);
        m.put("album", new AttachmentHandler() {

            @Override
            public boolean handleAttachment(final JSONObject attachmentInformation, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException, JSONException {
                boolean changed = false;
                final String name = attachmentInformation.optString("name", null);
                final String href = attachmentInformation.optString("href", null);
                if (!Strings.isEmpty(href) && !Strings.isEmpty(name)) {
                    changed = true;
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
                /*
                 * "media" node
                 */
                final JSONArray media = attachmentInformation.optJSONArray("media");
                if (null != media) {
                    final int length = media.length();
                    for (int i = 0; i < length; i++) {
                        final JSONObject mediaInfo = media.getJSONObject(i);
                        final String alt = mediaInfo.optString("alt", null);
                        final String fallbackUrl = mediaInfo.optString("src", null);
                        final JSONObject photoInfo = mediaInfo.optJSONObject("photo");
                        String url = null;
                        if (null != photoInfo) {
                            final JSONArray images = photoInfo.optJSONArray("images");
                            if (null != images) {
                                final int il = images.length();
                                for (int j = 0; j < il; j++) {
                                    final JSONObject imageInfo = images.getJSONObject(j);
                                    final String tmpUrl = imageInfo.optString("src", null);
                                    url = prefer(url, tmpUrl);
                                }
                            }
                        }
                        if (null == url) {
                            url = fallbackUrl;
                        }
                        final StringBuilder messageText = message.getMessageText();
                        changed = true;
                        messageText.append(HTML_BR);
                        messageText.append("<img src='").append(url);
                        if (null == alt) {
                            messageText.append("'>");
                        } else {
                            messageText.append("' alt=\"").append(alt).append("\">");
                        }
                    }
                }
                return changed;
            }

            private String prefer(final String url1, final String url2) {
                if (null == url1) {
                    return url2;
                }
                if (url1.indexOf("_s.") > 0) {
                    return url2;
                }
                if (url1.indexOf("_n.") > 0) {
                    if (url2.indexOf("_b.") > 0) {
                        return url2;
                    }
                }
                return url1;
            }
        });
        m.put("link", new AttachmentHandler() {

            @Override
            public boolean handleAttachment(final JSONObject attachmentInformation, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) {
                boolean changed = false;
                final String name = attachmentInformation.optString("name", null);
                final String href = attachmentInformation.optString("href", null);
                if (null != href && null != name) {
                    final StringBuilder messageText = message.getMessageText();
                    changed = true;
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
                return changed;
            } // End of handleAttachment
        });
        m.put("group", new AttachmentHandler() {

            @Override
            public boolean handleAttachment(final JSONObject attachmentInformation, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {
                boolean changed = false;
                /*
                 * A group post
                 */
                message.setGroup(true);
                /*
                 * Get group's link
                 */
                final String name = attachmentInformation.optString("name", null);
                final String href = attachmentInformation.optString("href", null);
                if (null != href && null != name) {
                    final StringBuilder messageText = message.getMessageText();
                    changed = true;
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
                return changed;
            } // End of handleAttachment
        });
        m.put("video", new AttachmentHandler() {

            @Override
            public boolean handleAttachment(final JSONObject attachmentInformation, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {
                boolean changed = false;
                String sourceURL = null;
                String alt = null;
                String ext = null;
                /*
                 * "media" node
                 */
                final JSONArray media = attachmentInformation.optJSONArray("media");
                final int length = media.length();
                for (int i = 0; i < length; i++) {
                    final JSONObject mediaInformation = media.optJSONObject(i);
                    if (null != mediaInformation) {
                        /*
                         * "video" node
                         */
                        final JSONObject video = mediaInformation.optJSONObject("video");
                        if (null != video) {
                            sourceURL = video.optString("source_url", null);
                            if (null != sourceURL) {
                                alt = mediaInformation.optString("alt", null);
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
                    if (addAsBinaryParts()) {
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
                            LogFactory.getLog(FacebookFQLStreamJsonParser.class).debug("Couldn't load URL: " + sourceURL, e);
                        }
                    } else {
                        final StringBuilder messageText = message.getMessageText();
                        messageText.append(HTML_BR);
                        messageText.append("<a href='").append(sourceURL).append("'>");
                        messageText.append(Strings.isEmpty(alt) ? sourceURL : alt).append("</a>");
                    }
                    changed = true;
                }

                return changed;
            } // End of handleAttachment
        });
        m.put("photo", new AttachmentHandler() {

            @Override
            public boolean handleAttachment(final JSONObject attachmentInformation, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {
                boolean changed = false;
                String sourceUrlBig = null; // src_big - The URL to the full-sized version of the photo being queried. The image can have a maximum width or height of 960px. This URL may be blank.
                String sourceUrl = null; // src - The URL to the album view version of the photo being queried. The image can have a maximum width or height of 130px. This URL may be blank.
                String ext = null;
                /*
                 * "media" node
                 */
                final JSONArray media = attachmentInformation.optJSONArray("media");
                final int length = media.length();
                for (int i = 0; i < length; i++) {
                    final JSONObject mediaInformation = media.optJSONObject(i);
                    if (null != mediaInformation) {
                        sourceUrlBig = mediaInformation.optString("src_big", null);
                        if (null != sourceUrlBig) {
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
                            sourceUrl = mediaInformation.optString("src", null);
                            if (null != sourceUrl) {
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
                    changed = true;
                    final StringBuilder messageText = message.getMessageText();
                    messageText.append(HTML_BR);
                    messageText.append("<img src='").append(sourceUrlBig);
                    if (null == ext) {
                        messageText.append("' />");
                    } else {
                        messageText.append("' alt=\"photo.").append(ext).append("\" />");
                    }
                } else if (null != sourceUrl) {
                    changed = true;
                    final StringBuilder messageText = message.getMessageText();
                    messageText.append(HTML_BR);
                    messageText.append("<img src='").append(sourceUrl);
                    if (null == ext) {
                        messageText.append("' />");
                    } else {
                        messageText.append("' alt=\"photo.").append(ext).append("\" />");
                    }
                }
                return changed;
            }
        });
        m.put("swf", new AttachmentHandler() {

            @Override
            public boolean handleAttachment(final JSONObject attachmentInformation, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {
                boolean changed = false;
                String sourceURL = null;
                /*
                 * "media" node
                 */
                final JSONArray media = attachmentInformation.optJSONArray("media");
                final int length = media.length();
                for (int i = 0; i < length; i++) {
                    final JSONObject mediaInformation = media.optJSONObject(i);
                    if (null != mediaInformation) {
                        final JSONObject swf = mediaInformation.optJSONObject("swf");
                        if (null != swf) {
                            /*
                             * "source_url" node
                             */
                            sourceURL = swf.optString("source_url", null);
                        }
                    }
                }
                /*
                 * "name" node
                 */
                final String nameStr = attachmentInformation.optString("name", null);
                /*
                 * Source URL present?
                 */
                if (null != sourceURL && null != nameStr) {
                    changed = true;
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
                return changed;
            }
        });
        m.put("event", new AttachmentHandler() {

            @Override
            public boolean handleAttachment(final JSONObject attachmentInformation, final FacebookMessagingMessage message, final MultipartProvider multipartProvider) throws OXException {
                boolean changed = false;
                final String name = attachmentInformation.optString("name", null);
                if (null != name) {
                    changed = true;
                    final StringBuilder messageText = message.getMessageText();
                    final String href = attachmentInformation.optString("href", null);
                    if (null == href) {
                        messageText.append(HTML_BR).append(name);
                    } else {
                        messageText.append("<br><a href='").append(href).append("'>").append(name).append(
                            HTML_ANCHOR_END);
                    }

                    final JSONObject properties = attachmentInformation.optJSONObject("properties");
                    if (null != properties && properties.hasAndNotNull("stream_property")) {
                        final JSONObject streamProperties = properties.optJSONObject("stream_properties");
                        if (null != streamProperties) {
                            final String nameProperty = streamProperties.optString("name", null);
                            if (null != nameProperty) {
                                messageText.append(HTML_BR).append(nameProperty);
                                messageText.append(": ").append(streamProperties.optString("text", null));
                            }
                        }
                    }
                }
                return changed;
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
    public static FacebookMessagingMessage parseStreamJsonElement(final JSONObject streamElement, final Locale locale, final Session session) throws OXException {
        if (null == streamElement || 0 == streamElement.length()) {
            return null;
        }
        try {
            checkFacebookError(streamElement);
            final FacebookMessagingMessage message = new FacebookMessagingMessage(locale);
            /*
             * Iterate child nodes
             */
            boolean anyHandlerFound = false;
            JSONObject attachmentNode = null;
            {
                for (final String name : streamElement.keySet()) {
                    final ItemHandler itemHandler = ITEM_HANDLERS.get(name);
                    if (null == itemHandler) {
                        if ("attachment".equals(name)) {
                            attachmentNode = streamElement.getJSONObject("attachment");
                        } else {
                            com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamJsonParser.class)).warn("Un-handled item: " + name);
                        }
                    } else {
                        itemHandler.handleItem(streamElement, message);
                        anyHandlerFound = true;
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
                com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamJsonParser.class)).warn("Empty Facebook message detected:\n" + streamElement);
                return null;
            }
            /*
             * Check attachment node
             */
            final MultipartProvider multipartProvider = new MultipartProvider();
            boolean attachmentHandlerApplied = false;
            if (null != attachmentNode) {
                if (attachmentNode.hasAndNotNull("fb_object_type")) {
                    /*
                     * A file attachment is present
                     */
                    final String type = attachmentNode.getString("fb_object_type");
                    if (!Strings.isEmpty(type)) {
                        final AttachmentHandler attachmentHandler = ATTACH_HANDLERS.get(type);
                        if (null == attachmentHandler) {
                            final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamJsonParser.class));
                            logger.warn("Unknown attachment type: " + type);
                            logger.debug("Stream element:\n" + streamElement.toString(2));
                        } else {
                            attachmentHandlerApplied = attachmentHandler.handleAttachment(attachmentNode, message, multipartProvider);
                        }
                    }
                }
                if (!attachmentHandlerApplied && attachmentNode.hasAndNotNull("media")) {
                    final JSONArray media = attachmentNode.getJSONArray("media");
                    final int length = media.length();
                    for (int i = 0; i < length; i++) {
                        final JSONObject mediaInformation = media.optJSONObject(i);
                        if (null != mediaInformation) {
                            final String sType = mediaInformation.optString("type", null);
                            if (null != sType) {
                                final AttachmentHandler attachmentHandler = ATTACH_HANDLERS.get(sType);
                                if (null == attachmentHandler) {
                                    final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamJsonParser.class));
                                    logger.warn("Unknown attachment type: " + sType);
                                    logger.debug("Stream element:\n" + streamElement.toString(2));
                                } else {
                                    attachmentHandlerApplied = attachmentHandler.handleAttachment(attachmentNode, message, multipartProvider);
                                }
                            }
                        }
                    }
                }
            }
            /*
             * Set subject & size
             */
            final StringBuilder messageText = message.getMessageText();
            int size = messageText.length();
            if (size <= 0) {
                if (attachmentHandlerApplied) {
                    messageText.append(HTML_BR);
                    messageText.append("&lt;see attachments.&gt;");

                    // System.out.println("-----------------------------------------");
                    // System.out.println("Applied, but empty:\n"+streamElement.toString(2));
                    // System.out.println("-----------------------------------------");

                    size = messageText.length();
                } else {
                    final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookFQLStreamJsonParser.class));
                    logger.debug("Stream element is an empty message:\n" + streamElement.toString(2));
                    return null;
                }
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
        } catch (final JSONException e) {
            String sJson;
            try {
                sJson = streamElement.toString(2);
            } catch (final JSONException x) {
                sJson = null;
            }
            throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage() + (null == sJson ? "" : "\nJSON data:\n" + sJson));
        } catch (final RuntimeException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
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

    private static void checkFacebookError(final JSONObject streamElement) throws OXException {
        long errorCode = -1L;
        String errorMsg = null;
        if (streamElement.hasAndNotNull("error_code")) {
            errorCode = streamElement.optLong("error_code", -1L);
        }
        if (streamElement.hasAndNotNull("error_msg")) {
            errorMsg = streamElement.optString("error_msg", null);
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

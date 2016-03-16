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

package com.openexchange.messaging.generic;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.generic.internet.MimeContentType;

/**
 * {@link MessageParser} - A call-back parser to parse instances of {@link MessagingMessage} by invoking the <code>handleXXX()</code>
 * methods of given {@link MessageHandler} object
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageParser {

    private static interface InlineDetector {

        public boolean isInline(String disposition, String fileName);
    }

    /**
     * If disposition equals ignore-case <code>"INLINE"</code>, then it is treated as inline in any case.<br>
     * Only if disposition is <code>null</code> the file name is examined.
     */
    private static final InlineDetector LENIENT_DETECTOR = new InlineDetector() {

        @Override
        public boolean isInline(final String disposition, final String fileName) {
            return MessagingPart.INLINE.equalsIgnoreCase(disposition) || ((disposition == null) && (fileName == null));
        }
    };

    /**
     * Considered as inline if disposition equals ignore-case <code>"INLINE"</code> OR is <code>null</code>, but in any case the file name
     * must be <code>null</code>.
     */
    private static final InlineDetector STRICT_DETECTOR = new InlineDetector() {

        @Override
        public boolean isInline(final String disposition, final String fileName) {
            return (MessagingPart.INLINE.equalsIgnoreCase(disposition) || (disposition == null)) && (fileName == null);
        }
    };

    /*
     * +++++++++++++++++++ MEMBERS +++++++++++++++++++
     */

    private boolean stop;

    private InlineDetector inlineDetector;

    /**
     * Initializes a new {@link MessageParser}.
     */
    public MessageParser() {
        super();
        inlineDetector = LENIENT_DETECTOR;
    }

    /**
     * Switches the INLINE detector behavior.
     *
     * @param strict <code>true</code> to perform strict INLINE detector behavior; otherwise <code>false</code>
     * @return This parser with new behavior applied
     */
    public MessageParser setInlineDetectorBehavior(final boolean strict) {
        inlineDetector = strict ? STRICT_DETECTOR : LENIENT_DETECTOR;
        return this;
    }

    /**
     * Resets this parser and returns itself
     *
     * @return The parser itself
     */
    public MessageParser reset() {
        stop = false;
        return this;
    }

    /**
     * Parses specified message using given handler as call-back and given initial prefix for message part identifiers; e.g.
     * <code>&quot;1.1&quot;</code>.
     *
     * @param message The message to parse
     * @param handler The call-back handler
     * @throws OXException If parsing specified message fails
     * @throws IllegalArgumentException If either message or handler is <code>null</code>
     */
    public void parseMessage(final MessagingMessage message, final MessageHandler handler) throws OXException {
        if (null == message) {
            throw new IllegalArgumentException("Message is null.");
        }
        if (null == handler) {
            throw new IllegalArgumentException("Handler is null.");
        }
        /*
         * Parse envelope
         */
        parseEnvelope(message, handler);
        /*
         * Parse content
         */
        parsePart(message, handler);
        /*
         * Message end
         */
        handler.handleMessageEnd(message);
    }

    private void parsePart(final MessagingPart part, final MessageHandler handler) throws OXException {
        if (stop) {
            return;
        }
        /*
         * Set part information
         */
        final String disposition = part.getDisposition();
        final ContentType contentType;
        {
            final ContentType ct = part.getContentType();
            if (null == ct) {
                final MimeContentType mct = new MimeContentType();
                mct.setPrimaryType("application");
                mct.setSubType("octet-stream");
                contentType = mct;
            } else {
                contentType = ct;
            }
        }
        final String lcct = toLowerCase(contentType.getBaseType());
        final String name = contentType.getNameParameter();
        /*
         * Parse part dependent on its MIME type
         */
        final boolean isInline = inlineDetector.isInline(disposition, part.getFileName());
        if (isMultipart(lcct)) {
            /*
             * Pass as a common part
             */
            if (!handler.handlePart(part, isInline)) {
                stop = true;
                return;
            }
            /*
             * Handle multipart
             */
            final MultipartContent multipart = (MultipartContent) part.getContent();
            final int count = multipart.getCount();
            if (!handler.handleMultipart(multipart)) {
                stop = true;
                return;
            }
            for (int i = 0; i < count; i++) {
                parsePart(multipart.get(i), handler);
            }
        } else if (isMessage(lcct, name)) {
            /*
             * Pass as a common part
             */
            if (!handler.handlePart(part, isInline)) {
                stop = true;
                return;
            }
            /*
             * Handle nested message if inline
             */
            if (isInline && !handler.handleNestedMessage((MessagingMessage) part.getContent())) {
                stop = true;
                return;
            }
        } else {
            if (!handler.handlePart(part, isInline)) {
                stop = true;
                return;
            }
        }
    }

    private void parseEnvelope(final MessagingMessage message, final MessageHandler handler) throws OXException {
        /*
         * Headers
         */
        handler.handleHeaders(message.getHeaders());
        /*
         * Color label
         */
        handler.handleColorLabel(message.getColorLabel());
        /*
         * Received date
         */
        handler.handleReceivedDate(message.getReceivedDate());
        /*
         * System flags
         */
        handler.handleSystemFlags(message.getFlags());
        /*
         * User flags
         */
        handler.handleUserFlags(message.getUserFlags());
    }

    private static String toLowerCase(final String str) {
        final char[] buf = new char[str.length()];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = Character.toLowerCase(str.charAt(i));
        }
        return new String(buf);
    }

    private static final String PRIMARY_MULTI = "multipart/";

    /**
     * Checks if content type matches <code>multipart/*</code> content type.
     *
     * @param contentType The content type
     * @return <code>true</code> if content type matches <code>multipart/*</code>; otherwise <code>false</code>
     */
    private static boolean isMultipart(final String contentType) {
        return contentType.startsWith(PRIMARY_MULTI, 0);
    }

    private static final String PRIMARY_RFC822 = "message/rfc822";

    /**
     * Checks if content type matches <code>message/rfc822</code> content type.
     *
     * @param contentType The content type
     * @param name the file name, may be null
     * @return <code>true</code> if content type matches <code>message/rfc822</code>; otherwise <code>false</code>
     */
    private static boolean isMessage(final String contentType, String name) {
        if (name != null && name.endsWith(".eml")) {
            return true;
        }
        return contentType.startsWith(PRIMARY_RFC822, 0);
    }

}

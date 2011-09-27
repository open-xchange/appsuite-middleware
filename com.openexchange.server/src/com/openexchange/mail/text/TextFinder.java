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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.text;

import java.io.IOException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.utils.CharsetDetector;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedMultiPart;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.textxtraction.TextXtractService;

/**
 * {@link TextFinder} - Looks-up the primary text content of the message.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TextFinder {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(TextFinder.class));

    private boolean textIsHtml = false;

    private final TextXtractService textXtractService;

    /**
     * Initializes a new {@link TextFinder}.
     */
    public TextFinder() {
        super();
        textXtractService = ServerServiceRegistry.getInstance().getService(TextXtractService.class);
    }

    private String extractPlainText(final String content) throws OXException {
        return textXtractService.extractFrom(new UnsynchronizedByteArrayInputStream(content.getBytes(Charsets.UTF_8)), null);
    }

    /**
     * Gets the primary text content of the message.
     *
     * @param p The part
     * @return The primary text content or <code>null</code>
     * @throws OXException If primary text content cannot be returned
     */
    public String getText(final MailPart p) throws OXException {
        textIsHtml = false;
        try {
            final ContentType ct = p.getContentType();
            if (ct.startsWith("text/")) {
                String content = readContent(p, ct);
                if (ct.startsWith("text/plain")) {
                    final UUEncodedMultiPart uuencodedMP = new UUEncodedMultiPart(content);
                    if (uuencodedMP.isUUEncoded()) {
                        content = uuencodedMP.getCleanText();
                    }
                    textIsHtml = false;
                } else {
                    textIsHtml = ct.startsWith("text/htm");
                }
                return textIsHtml ? extractPlainText(content) : content;
            }
            if (ct.startsWith("multipart/alternative")) {
                /*
                 * Prefer HTML text over plain text
                 */
                final int count = p.getEnclosedCount();
                String text = null;
                for (int i = 0; i < count; i++) {
                    final MailPart bp = p.getEnclosedMailPart(i);
                    final ContentType bct = bp.getContentType();
                    if (bct.startsWith("text/plain")) {
                        if (text == null) {
                            text = getText(bp);
                        }
                        continue;
                    } else if (bct.startsWith("text/htm")) {
                        final String s = getText(bp);
                        if (s != null) {
                            return s;
                        }
                    } else {
                        return getText(bp);
                    }
                }
                return text;
            } else if (ct.startsWith("multipart/")) {
                final int count = p.getEnclosedCount();
                for (int i = 0; i < count; i++) {
                    final String s = getText(p.getEnclosedMailPart(i));
                    if (s != null) {
                        return s;
                    }
                }
            }
            return null;
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static String readContent(final MailPart mailPart, final ContentType contentType) throws OXException, IOException {
        final String charset = getCharset(mailPart, contentType);
        try {
            return MessageUtility.readMailPart(mailPart, charset);
        } catch (final java.io.CharConversionException e) {
            // Obviously charset was wrong or bogus implementation of character conversion
            final String fallback = "US-ASCII";
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    new StringBuilder("Character conversion exception while reading content with charset \"").append(charset).append(
                        "\". Using fallback charset \"").append(fallback).append("\" instead."),
                    e);
            }
            return MessageUtility.readMailPart(mailPart, fallback);
        }
    }

    private static String getCharset(final MailPart mailPart, final ContentType contentType) throws OXException {
        final String charset;
        if (mailPart.containsHeader(MessageHeaders.HDR_CONTENT_TYPE)) {
            String cs = contentType.getCharsetParameter();
            if (!CharsetDetector.isValid(cs)) {
                StringBuilder sb = null;
                if (null != cs) {
                    sb = new StringBuilder(64).append("Illegal or unsupported encoding: \"").append(cs).append("\".");
                }
                if (contentType.startsWith("text/")) {
                    cs = CharsetDetector.detectCharset(mailPart.getInputStream());
                    if (LOG.isWarnEnabled() && null != sb) {
                        sb.append(" Using auto-detected encoding: \"").append(cs).append('"');
                        LOG.warn(sb.toString());
                    }
                } else {
                    cs = MailProperties.getInstance().getDefaultMimeCharset();
                    if (LOG.isWarnEnabled() && null != sb) {
                        sb.append(" Using fallback encoding: \"").append(cs).append('"');
                        LOG.warn(sb.toString());
                    }
                }
            }
            charset = cs;
        } else {
            if (contentType.startsWith("text/")) {
                charset = CharsetDetector.detectCharset(mailPart.getInputStream());
            } else {
                charset = MailProperties.getInstance().getDefaultMimeCharset();
            }
        }
        return charset;
    }

}

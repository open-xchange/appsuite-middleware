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

package com.openexchange.mail.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import com.openexchange.log.LogFactory;

import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.utils.CharsetDetector;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link BodyTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BodyTerm extends SearchTerm<String> {

    private static final long serialVersionUID = -7824562914196872458L;

    private final String pattern;

    /**
     * Initializes a new {@link BodyTerm}
     */
    public BodyTerm(final String pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void addMailField(final Collection<MailField> col) {
        col.add(MailField.BODY);
    }

    @Override
    public boolean matches(final MailMessage mailMessage) throws OXException {
        final String text = getTextContent(mailMessage);
        if (text == null) {
            if (null == pattern) {
                return true;
            }
            return false;
        }
        if (null == pattern) {
            return false;
        }
        if (containsWildcard()) {
            return toRegex(pattern).matcher(text).find();
        }
        return (text.toLowerCase(Locale.ENGLISH).indexOf(pattern.toLowerCase(Locale.ENGLISH)) > -1);
    }

    @Override
    public boolean matches(final Message msg) throws OXException {
        try {
            final String text = getTextContent(msg);
            if (text == null) {
                if (null == pattern) {
                    return true;
                }
                return false;
            }
            if (null == pattern) {
                return false;
            }
            if (containsWildcard()) {
                return toRegex(pattern).matcher(text).find();
            }
            return (text.toLowerCase(Locale.ENGLISH).indexOf(pattern.toLowerCase(Locale.ENGLISH)) > -1);
        } catch (final OXException e) {
            com.openexchange.log.Log.valueOf(LogFactory.getLog(FromTerm.class)).warn("Error during search.", e);
            return false;
        } catch (final RuntimeException e) {
            com.openexchange.log.Log.valueOf(LogFactory.getLog(FromTerm.class)).warn("Error during search.", e);
            return false;
        }
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        return new javax.mail.search.BodyTerm(pattern);
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return new javax.mail.search.BodyTerm(getNonWildcardPart(pattern));
    }

    @Override
    public boolean isAscii() {
        return isAscii(pattern);
    }

    @Override
    public boolean containsWildcard() {
        return null == pattern ? false : pattern.indexOf('*') >= 0 || pattern.indexOf('?') >= 0;
    }

    /**
     * Extracts textual content out of given message's body
     *
     * @param part The message whose textual content shall be extracted
     * @return The textual content or <code>null</code> if none found
     * @throws OXException If text extraction fails
     */
    private static String getTextContent(final Part part) throws OXException {
        try {
            if (ContentType.isMimeType(part.getContentType(), "multipart/*")) {
                final Multipart multipart = (Multipart) part.getContent();
                final int count = multipart.getCount();
                for (int i = 0; i < count; i++) {
                    final String text = getTextContent(multipart.getBodyPart(i));
                    if (text != null) {
                        return text;
                    }
                }
            }
            return getPartTextContent(part);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Extracts textual content out of given mail part's body
     *
     * @param mailPart The mail message whose textual content shall be extracted
     * @return The textual content or <code>null</code> if none found
     * @throws OXException If text extraction fails
     */
    private static String getTextContent(final MailPart mailPart) throws OXException {
        final int count = mailPart.getEnclosedCount();
        if (count != MailPart.NO_ENCLOSED_PARTS) {
            /*
             * No textual content
             */
            for (int i = 0; i < count; i++) {
                final String text = getTextContent(mailPart.getEnclosedMailPart(i));
                if (text != null) {
                    return text;
                }
            }
        }
        final ContentType contentType = mailPart.getContentType();
        if (!contentType.startsWith("text/")) {
            /*
             * No textual content
             */
            return null;
        }
        /*
         * Try to extract textual content out of current part's body
         */
        String charset = contentType.getCharsetParameter();
        if (!CharsetDetector.isValid(charset)) {
            charset = CharsetDetector.detectCharset(mailPart.getInputStream());
        }
        try {
            if (contentType.startsWith("text/htm")) {
                final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                return htmlService.html2text(htmlService.getConformHTML(MessageUtility.readMailPart(mailPart, charset), charset), false);
            }
            return MessageUtility.readMailPart(mailPart, charset);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Extracts textual content out of given part's body
     *
     * @param part The part
     * @return The textual content or <code>null</code> if none found
     * @throws OXException If text extraction fails
     */
    private static String getPartTextContent(final Part part) throws OXException {
        try {
            final ContentType ct = new ContentType(part.getContentType());
            if (!ct.startsWith("text/")) {
                /*
                 * No textual content
                 */
                return null;
            }
            String charset = ct.getCharsetParameter();
            if (!CharsetDetector.isValid(charset)) {
                charset = CharsetDetector.detectCharset(part.getInputStream());
            }
            if (ct.startsWith("text/htm")) {
                final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                return htmlService.html2text(htmlService.getConformHTML(MessageUtility.readMimePart(part, charset), charset), false);
            }
            return MessageUtility.readMimePart(part, charset);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

}

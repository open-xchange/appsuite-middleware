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

package com.openexchange.mail.search;

import java.io.IOException;
import java.util.Collection;
import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.util.MessageRemovedIOException;

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
    public BodyTerm(String pattern) {
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
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.BODY);
    }

    @Override
    public boolean matches(MailMessage mailMessage) throws OXException {
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
        return (Strings.asciiLowerCase(text).indexOf(Strings.asciiLowerCase(pattern)) > -1);
    }

    @Override
    public boolean matches(Message msg) throws OXException {
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
            return (Strings.asciiLowerCase(text).indexOf(Strings.asciiLowerCase(pattern)) > -1);
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(FromTerm.class).warn("Error during search.", e);
            return false;
        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(FromTerm.class).warn("Error during search.", e);
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
    public void contributeTo(FetchProfile fetchProfile) {
        if (!fetchProfile.contains(IMAPFolder.FetchProfileItem.MESSAGE)) {
            fetchProfile.add(IMAPFolder.FetchProfileItem.MESSAGE);
        }
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
    private static String getTextContent(Part part) throws OXException {
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
        } catch (MessageRemovedIOException e) {
            return null;
        } catch (IOException e) {
            if (e.getCause() instanceof MessageRemovedException) {
                return null;
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (MessageRemovedException e) {
            return null;
        } catch (MessagingException e) {
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
    private static String getTextContent(MailPart mailPart) throws OXException {
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
                HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                return htmlService.extractText(MessageUtility.readMailPart(mailPart, charset));
            }
            return MessageUtility.readMailPart(mailPart, charset);
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
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
    private static String getPartTextContent(Part part) throws OXException {
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
                HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                return  htmlService.extractText(MessageUtility.readMimePart(part, charset));
            }
            return MessageUtility.readMimePart(part, charset);
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

}

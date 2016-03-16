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

package com.openexchange.mail.mime;

import static com.openexchange.mail.mime.converters.MimeMessageConverter.getContentType;
import static com.openexchange.mail.mime.converters.MimeMessageConverter.multipartFor;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.mail.BodyPart;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.utils.MessageUtility;

/**
 * {@link MimeFilter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MimeFilter {

    private static final String MESSAGE_ID = MessageHeaders.HDR_MESSAGE_ID;

    /**
     * Gets the MIME filter for specified alias.
     *
     * @param alias The alias
     * @return The appropriate MIME filter or <code>null</code> if alias is unknown
     */
    public static MimeFilter filterFor(final String alias) {
        if ("ics".equalsIgnoreCase(alias)) {
            return IcsMimeFilter.getInstance();
        }
        return null;
    }

    /**
     * Gets the MIME filter for specified ignorable <code>Content-Type</code>s
     *
     * @param ignorableContentTypes The ignorable <code>Content-Type</code>s
     * @return The appropriate MIME filter
     */
    public static MimeFilter filterFor(final String... ignorableContentTypes) {
        return new MimeFilter(Arrays.asList(ignorableContentTypes));
    }

    /**
     * Gets the MIME filter for specified ignorable <code>Content-Type</code>s
     *
     * @param ignorableContentTypes The ignorable <code>Content-Type</code>s
     * @return The appropriate MIME filter
     */
    public static MimeFilter filterFor(final List<String> ignorableContentTypes) {
        return new MimeFilter(ignorableContentTypes);
    }

    /*-
     * ---------------------------------------------------------------------
     */

    protected final List<String> ignorableContentTypes;

    /**
     * Initializes a new {@link MimeFilter}.
     */
    protected MimeFilter(final List<String> ignorableContentTypes) {
        super();
        this.ignorableContentTypes = ignorableContentTypes;
    }

    /**
     * Gets the ignorable <code>Content-Type</code>s
     *
     * @return The ignorable <code>Content-Type</code>s
     */
    public List<String> getIgnorableContentTypes() {
        return ignorableContentTypes;
    }

    /**
     * Filters matching parts from specified MIME message.
     *
     * @param mimeMessage The MIME message to filter
     * @return The filtered MIME message
     * @throws OXException If filter operation fails
     */
    public MimeMessage filter(final MimeMessage mimeMessage) throws OXException {
        if (null == mimeMessage) {
            return null;
        }
        try {
            final String messageId = mimeMessage.getHeader(MESSAGE_ID, null);
            final ContentType contentType = getContentType(mimeMessage);
            if (!contentType.startsWith("multipart/")) {
                // Nothing to filter
                return mimeMessage;
            }
            final MimeMultipart newMultipart = new MimeMultipart(contentType.getSubType());
            handlePart(multipartFor(mimeMessage, contentType), newMultipart);
            MessageUtility.setContent(newMultipart, mimeMessage);
            // mimeMessage.setContent(newMultipart);
            MimeMessageConverter.saveChanges(mimeMessage);
            // Restore original Message-Id header
            if (null == messageId) {
                mimeMessage.removeHeader(MESSAGE_ID);
            } else {
                mimeMessage.setHeader(MESSAGE_ID, messageId);
            }
            return mimeMessage;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Invoked to detect if passed body part should be ignored.
     *
     * @param contentType The part's Content-Type
     * @param bodyPart The body part
     * @return <code>true</code> to ignore; otherwise <code>false</code>
     */
    public boolean ignorable(final String contentType, @SuppressWarnings("unused") final BodyPart bodyPart) {
        for (final String baseType : ignorableContentTypes) {
            if (contentType.startsWith(baseType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Invoked to detect if passed body part should be ignored.
     *
     * @param contentType The part's Content-Type
     * @param bodyPart The body part
     * @return <code>true</code> to ignore; otherwise <code>false</code>
     */
    public boolean ignorable(final String contentType, @SuppressWarnings("unused") final MailPart bodyPart) {
        for (final String baseType : ignorableContentTypes) {
            if (contentType.startsWith(baseType)) {
                return true;
            }
        }
        return false;
    }

    private void handlePart(final Multipart multipart, final MimeMultipart newMultipart) throws MessagingException, IOException, OXException {
        final int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            final BodyPart bodyPart = multipart.getBodyPart(i);
            String contentType = bodyPart.getContentType();
            String name = new ContentType(contentType).getNameParameter();
            if (com.openexchange.java.Strings.isEmpty(contentType)) {
                newMultipart.addBodyPart(bodyPart);
            } else {
                contentType = com.openexchange.java.Strings.toLowerCase(contentType.trim());
                if (contentType.startsWith("multipart/")) {
                    final MimeMultipart newSubMultipart = new MimeMultipart(getSubType(contentType, "mixed"));
                    {
                        final Object content = bodyPart.getContent();
                        if (content instanceof Multipart) {
                            handlePart((Multipart) content, newSubMultipart);
                        } else {
                            handlePart(new MimeMultipart(bodyPart.getDataHandler().getDataSource()), newSubMultipart);
                        }
                    }
                    final MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    MessageUtility.setContent(newSubMultipart, mimeBodyPart);
                    // mimeBodyPart.setContent(newSubMultipart);
                    newMultipart.addBodyPart(mimeBodyPart);
                } else if (contentType.startsWith("message/rfc822") || (name != null && name.endsWith(".eml"))) {
                    final MimeFilter nestedFilter = new MimeFilter(ignorableContentTypes);
                    final MimeMessage filteredMessage;
                    {
                        final Object content = bodyPart.getContent();
                        if (content instanceof MimeMessage) {
                            filteredMessage = nestedFilter.filter((MimeMessage) content);
                        } else {
                            filteredMessage = nestedFilter.filter(new MimeMessage(MimeDefaultSession.getDefaultSession(), bodyPart.getInputStream()));
                        }
                    }
                    final MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    MessageUtility.setContent(filteredMessage, mimeBodyPart);
                    // mimeBodyPart.setContent(filteredMessage, "message/rfc822");
                    newMultipart.addBodyPart(mimeBodyPart);
                } else if (!ignorable(contentType, bodyPart)) {
                    newMultipart.addBodyPart(bodyPart);
                }
            }
        }
    }

    private static String getSubType(final String contentType, final String defaultType) {
        try {
            return new ContentType(contentType).getSubType();
        } catch (final Exception e) {
            return defaultType;
        }
    }
}

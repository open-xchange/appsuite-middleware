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

package com.openexchange.messaging.generic.internet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.messaging.BinaryContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MimeBinaryContent} - A MIME {@link BinaryContent binary content}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class MimeBinaryContent implements BinaryContent {

    /**
     * The MIME part.
     */
    protected final Part part;

    /**
     * The cached content.
     */
    protected final byte[] cachedContent;

    /**
     * Initializes a new {@link MimeBinaryContent}.
     *
     * @param inputStream The binary content as an input stream; gets closed
     * @throws OXException If an I/O error occurs
     */
    public MimeBinaryContent(final InputStream inputStream) throws OXException {
        super();
        part = null;
        try {
            final ByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream(8192 << 1);
            final byte[] buf = new byte[8192];
            int read;
            while ((read = inputStream.read(buf, 0, buf.length)) > 0) {
                buffer.write(buf, 0, read);
            }
            cachedContent = buffer.toByteArray();
        } catch (final IOException e) {
            throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(inputStream);
        }
    }


    /**
     * Initializes a new {@link MimeBinaryContent}.
     *
     * @param content The binary content
     */
    public MimeBinaryContent(final byte[] content) {
        super();
        part = null;
        cachedContent = new byte[content.length];
        System.arraycopy(content, 0, cachedContent, 0, content.length);
    }

    /**
     * Initializes a new {@link MimeBinaryContent}.
     *
     * @param part The MIME part
     */
    protected MimeBinaryContent(final Part part) {
        super();
        this.part = part;
        cachedContent = null;
    }

    @Override
    public InputStream getData() throws OXException {
        return getBodyPartInputStream();
    }

    /**
     * Gets MIME part's input stream in a safe manner: If {@link Part#getInputStream()} fails, the {@link MimeBodyPart#getRawInputStream()}
     * is invoked.
     *
     * @return The either decoded or raw input stream
     * @throws OXException If neither input stream nor raw input stream can be returned
     */
    private InputStream getBodyPartInputStream() throws OXException {
        if (cachedContent != null) {
            return new UnsynchronizedByteArrayInputStream(cachedContent);
        }
        if (null == part) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create("No content");
        }
        try {
            return part.getInputStream();
        } catch (final IOException e) {
            try {
                if (part instanceof MimeBodyPart) {
                    return ((MimeBodyPart) part).getRawInputStream();
                }
                throw e;
            } catch (final javax.mail.MessagingException me) {
                me.setNextException(e);
                throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
            } catch (final IOException ioe) {
                throw MessagingExceptionCodes.IO_ERROR.create(ioe, ioe.getMessage());
            }
        } catch (final javax.mail.MessagingException e) {
            try {
                if (part instanceof MimeMessage) {
                    return ((MimeMessage) part).getRawInputStream();
                }
                throw e;
            } catch (final javax.mail.MessagingException me) {
                me.setNextException(e);
                throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }
    }

}

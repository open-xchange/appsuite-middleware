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

package com.openexchange.mail.uuencode;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.datasource.StreamDataSource;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link UUEncodedPart} - UUEncode part containing all needed information about the attachment.
 *
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UUEncodedPart extends UUEncodedMultiPart {

    private final String sPossibleFileName;

    private final byte[] bodyPart;

    private final int startIndex;

    private final int endIndex;

    /**
     * Constructs a {@link UUEncodedPart} object containing all information about the attachment.
     */
    UUEncodedPart(final int startIndex, final int endIndex, final String bodyPart, final String filename) {
        super();
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.bodyPart = Charsets.toAsciiBytes(bodyPart);
        sPossibleFileName = filename;
    }

    /**
     * Return the filename attribute of the UUEncodedPart object
     *
     * @return filename - The filename
     */
    public String getFileName() {
        return (sPossibleFileName);
    }

    /**
     * Return the file size attribute of the UUEncodedPart object. Note: This value may be different from the saved file. This is normal
     * because this is the size of the raw (not encoded) object.
     *
     * @return The file size
     */
    public int getFileSize() {
        try {
            return (bodyPart.length);
        } catch (final NumberFormatException nfe) {
            return (-1);
        }
    }

    /**
     * Return the start position of the attachment within the content.
     *
     * @return beginIndex - The start position
     */
    public int getIndexStart() {
        return (startIndex);
    }

    /**
     * Return the end position of the attachment within the content.
     *
     * @return beginIndex - The start position
     */
    public int getIndexEnd() {
        return (endIndex);
    }

    /**
     * Gets the inputStream attribute of the UUEncodedPart object
     *
     * @return inStreamPart - The inputStream
     */
    public InputStream getInputStream() {
        final ByteArrayInputStream bStream = new UnsynchronizedByteArrayInputStream(bodyPart);
        try {
            final InputStream inStreamPart = MimeUtility.decode(bStream, "uuencode");
            return (inStreamPart);
        } catch (final Exception e) {
            org.slf4j.LoggerFactory.getLogger(UUEncodedPart.class).error("", e);
            return (null);
        }
    }

    /**
     * Creates a data handler for this uuencoded part.
     *
     * @param contentType The content type to apply to data handler
     * @return A data handler for this uuencoded part
     */
    public DataHandler getDataHandler(final String contentType) {
        final byte[] data = bodyPart;
        final StreamDataSource.InputStreamProvider isp = new StreamDataSource.InputStreamProvider() {

            @Override
            public InputStream getInputStream() throws IOException {
                try {
                    return MimeUtility.decode(new UnsynchronizedByteArrayInputStream(data), "uuencode");
                } catch (final MessagingException e) {
                    final IOException io = new IOException(e.getMessage());
                    io.initCause(e);
                    throw io;
                }
            }

            @Override
            public String getName() {
                return null;
            }
        };
        return new DataHandler(new StreamDataSource(isp, contentType));
    }

    /**
     * Gets the encoded part as StringBuffer
     *
     * @return The part
     */
    public StringBuilder getPart() {
        final StringBuilder encodedPart = new StringBuilder();
        try {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(getInputStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    encodedPart.append(line).append('\n');
                }
            } finally {
                Streams.close(br);
            }
        } catch (final Exception e) {
            org.slf4j.LoggerFactory.getLogger(UUEncodedPart.class).error("", e);
        }
        return (encodedPart);
    }

    /**
     * Output an appropriately encoded byte stream to the given OutputStream.
     *
     * @param out - The inputStream
     * @throws java.io.IOException if an error occurs writing to the stream
     */
    public void writeTo(final OutputStream out) throws IOException {
        BufferedOutputStream bos = null;
        final InputStream in = getInputStream();
        try {
            bos = new BufferedOutputStream(out);
            int iChar;
            while ((iChar = in.read()) != -1) {
                bos.write(iChar);
            }
        } catch (final IOException ioe) {
            org.slf4j.LoggerFactory.getLogger(UUEncodedPart.class).error("", ioe);
            throw ioe;
        } finally {
            Streams.close(bos);
        }
    }
}

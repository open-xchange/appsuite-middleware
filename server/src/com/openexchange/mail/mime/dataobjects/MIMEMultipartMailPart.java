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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.mime.dataobjects;

import static com.openexchange.mail.mime.utils.MIMEMessageUtility.extractHeader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MIMEMultipartMailPart} - An implementation of {@link MailPart} for mail parts of MIME type <code>multipart/*</code>.
 * <p>
 * Parsing of multipart data is based on <b>Knuth&#045;Morris&#045;Pratt (KMP)</b> algorithm.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MIMEMultipartMailPart extends MailPart {

    private static final long serialVersionUID = -3130161956976376243L;

    private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MIMEMultipartMailPart.class);

    private static final int BUFSIZE = 8192; // 8K

    private static final int SIZE = 32768; // 32K

    private static final String STR_CONTENT_TYPE = "Content-Type";

    private static final String STR_BD_START = "--";

    private byte[] data;

    private DataSource dataSource;

    private byte[] boundaryBytes;

    private int count = -1;

    private int[] positions;

    /**
     * Initializes a new {@link MIMEMultipartMailPart}.
     * 
     * @param dataSource The data source
     * @throws MailException If reading input stream fails
     */
    public MIMEMultipartMailPart(final DataSource dataSource) throws MailException {
        this(null, dataSource);
    }

    /**
     * Initializes a new {@link MIMEMultipartMailPart}.
     * 
     * @param contentType The content type; may be <code>null</code>
     * @param dataSource The data source
     * @throws MailException If reading input stream fails
     */
    public MIMEMultipartMailPart(final ContentType contentType, final DataSource dataSource) throws MailException {
        super();
        if (contentType == null) {
            try {
                setContentType(extractHeader(STR_CONTENT_TYPE, dataSource.getInputStream(), true));
            } catch (final IOException e) {
                throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
            }
        } else {
            setContentType(contentType);
        }
        this.dataSource = dataSource;
    }

    /**
     * Initializes a new {@link MIMEMultipartMailPart}.
     * 
     * @param inputStream The input stream
     * @throws MailException If reading input stream fails
     */
    public MIMEMultipartMailPart(final InputStream inputStream) throws MailException {
        this(null, inputStream);
    }

    /**
     * Initializes a new {@link MIMEMultipartMailPart}.
     * 
     * @param contentType The content type; may be <code>null</code>
     * @param inputStream The input stream
     * @throws MailException If reading input stream fails
     */
    public MIMEMultipartMailPart(final ContentType contentType, final InputStream inputStream) throws MailException {
        super();
        if (contentType == null) {
            try {
                setContentType(extractHeader(STR_CONTENT_TYPE, inputStream, false));
            } catch (final IOException e) {
                throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
            }
        } else {
            setContentType(contentType);
        }
        try {
            data = copyStream(inputStream);
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
    }

    /**
     * Initializes a new {@link MIMEMultipartMailPart}.
     * 
     * @param inputData The input data
     * @throws MailException If reading input stream fails
     */
    public MIMEMultipartMailPart(final byte[] inputData) throws MailException {
        this(null, inputData);
    }

    /**
     * Initializes a new {@link MIMEMultipartMailPart}.
     * 
     * @param contentType The content type; may be <code>null</code>
     * @param inputData The input data
     * @throws MailException If reading input stream fails
     */
    public MIMEMultipartMailPart(final ContentType contentType, final byte[] inputData) throws MailException {
        super();
        if (contentType == null) {
            try {
                setContentType(extractHeader(STR_CONTENT_TYPE, new UnsynchronizedByteArrayInputStream(inputData), false));
            } catch (final IOException e) {
                throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
            }
        } else {
            setContentType(contentType);
        }
        data = inputData;
    }

    @Override
    public Object getContent() throws MailException {
        return null;
    }

    @Override
    public DataHandler getDataHandler() throws MailException {
        return null;
    }

    @Override
    public int getEnclosedCount() throws MailException {
        if (count != -1) {
            return count;
        }
        final byte[] boundaryBytes = getBoundaryBytes();
        final byte[] dataBytes;
        try {
            dataBytes = getInputBytes();
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
        count = 0;
        positions = new int[5];
        int index = 0;
        final int[] computedFailures = computeFailure(boundaryBytes);
        boolean endingBoundaryFound = false;
        try {
            while (!endingBoundaryFound && (index = indexOf(dataBytes, boundaryBytes, index, dataBytes.length, computedFailures)) != -1) {
                final int newIndex = index + boundaryBytes.length;
                final byte first = dataBytes[newIndex];
                final byte second = dataBytes[newIndex + 1];
                if ('-' == first && '-' == second && isLineBreakOrEOF(dataBytes, newIndex + 2)) {
                    /*
                     * Ending boundary found: <boundary> + "--\r?\n"
                     */
                    endingBoundaryFound = true;
                    if (count + 1 > positions.length) {
                        final int newbuf[] = new int[Math.max(positions.length << 1, count)];
                        System.arraycopy(positions, 0, newbuf, 0, positions.length);
                        positions = newbuf;
                    }
                    positions[count] = index > 0 && '\r' == dataBytes[index - 1] ? index - 1 : index;
                } else {
                    /*
                     * Ensure CRLF or LF immediately follows boundary, else continue boundary look-up
                     */
                    if (isLineBreak(first, second)) {
                        if (++count > positions.length) {
                            final int newbuf[] = new int[Math.max(positions.length << 1, count)];
                            System.arraycopy(positions, 0, newbuf, 0, positions.length);
                            positions = newbuf;
                        }
                        positions[count - 1] = index > 0 && '\r' == dataBytes[index - 1] ? index - 1 : index;
                    }
                    index = newIndex;
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, new StringBuilder(64).append(
                "Illegal access to multipart data at index ").append(e.getMessage()).append(", but total length is ").append(
                dataBytes.length).toString());
        }
        if (!endingBoundaryFound) {
            if (0 == count) {
                /*
                 * No boundary found
                 */
                if (LOG.isDebugEnabled()) {
                    final StringBuilder sb = new StringBuilder(dataBytes.length + 128);
                    sb.append("No boundary found in Multipart-Mail:\n");
                    try {
                        sb.append(new String(dataBytes, "US-ASCII"));
                    } catch (final UnsupportedEncodingException e) {
                        sb.append("<not available>");
                    }
                    LOG.debug(sb.toString());
                }
                /*
                 * Take complete data as one part
                 */
                String headerBreak = "\n\r\n";
                int bodyStart = indexOf(dataBytes, headerBreak.getBytes(), 0, dataBytes.length, null);
                if (-1 == bodyStart) {
                    headerBreak = "\n\n";
                    bodyStart = indexOf(dataBytes, headerBreak.getBytes(), 0, dataBytes.length, null);
                }
                positions[count++] = -1 == bodyStart ? 0 : bodyStart + headerBreak.length();
                positions[count] = dataBytes.length;
                this.boundaryBytes = new byte[0];
            } else {
                /*-
                 * Missing ending boundary: <boundary> + "--"
                 * Take complete length as ending boundary.
                 */
                if (LOG.isDebugEnabled()) {
                    final StringBuilder sb = new StringBuilder(dataBytes.length + 128);
                    sb.append("Missing ending boundary in Multipart-Mail:\n");
                    try {
                        sb.append(new String(dataBytes, "US-ASCII"));
                    } catch (final UnsupportedEncodingException e) {
                        sb.append("<not available>");
                    }
                    LOG.debug(sb.toString());
                }
                if (count + 1 > positions.length) {
                    final int newbuf[] = new int[Math.max(positions.length << 1, count)];
                    System.arraycopy(positions, 0, newbuf, 0, positions.length);
                    positions = newbuf;
                }
                positions[count] = dataBytes.length;
            }
        }
        return count;
    }

    private static boolean isLineBreak(final byte first, final byte second) {
        return ('\n' == first || ('\r' == first && '\n' == second));
    }

    private static boolean isLineBreakOrEOF(final byte[] dataBytes, final int startIndex) {
        // Test for EOF
        if (startIndex >= dataBytes.length) {
            return true;
        }
        // Test for LF
        if ('\n' == dataBytes[startIndex]) {
            return true;
        }
        // Test for CRLF
        final int next = startIndex + 1;
        return ('\r' == dataBytes[startIndex] && (next >= dataBytes.length || '\n' == dataBytes[next]));
    }

    @Override
    public MailPart getEnclosedMailPart(final int index) throws MailException {
        getEnclosedCount();
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        final byte[] dataBytes;
        try {
            dataBytes = getInputBytes();
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
        int i = index;
        int startIndex = positions[i++];
        if ('\r' == dataBytes[startIndex]) {
            startIndex += (getBoundaryBytes().length + 1);
        } else {
            startIndex += (getBoundaryBytes().length);
        }
        /*
         * Omit starting CR?LF
         */
        if ('\n' == dataBytes[startIndex]) {
            startIndex++;
        } else if ('\r' == dataBytes[startIndex] && '\n' == dataBytes[startIndex + 1]) {
            startIndex += 2;
        }
        final int endIndex = i >= positions.length ? dataBytes.length : positions[i];
        final byte[] subArr = new byte[endIndex - startIndex];
        System.arraycopy(dataBytes, startIndex, subArr, 0, subArr.length);
        /*
         * Get content-type
         */
        final ContentType ct;
        try {
            ct = new ContentType(extractHeader(STR_CONTENT_TYPE, new UnsynchronizedByteArrayInputStream(subArr), false));
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
        if (ct.isMimeType(MIMETypes.MIME_MULTIPART_ALL)) {
            return new MIMEMultipartMailPart(ct, subArr);
        } else if (ct.isMimeType(MIMETypes.MIME_MESSAGE_RFC822)) {
            return MIMEMessageConverter.convertMessage(subArr);
        } else {
            return MIMEMessageConverter.convertPart(subArr);
        }

    }

    @Override
    public InputStream getInputStream() throws MailException {
        return null;
    }

    @Override
    public void loadContent() throws MailException {
        if (data != null) {
            return;
        }
        try {
            data = copyStream(dataSource.getInputStream());
            dataSource = null;
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
    }

    @Override
    public void prepareForCaching() {
        dataSource = null;
    }

    @Override
    public void writeTo(final OutputStream out) throws MailException {
        final InputStream in;
        try {
            in = dataSource == null ? (data == null ? null : new UnsynchronizedByteArrayInputStream(data)) : dataSource.getInputStream();
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        }
        if (null == in) {
            throw new MailException(MailException.Code.NO_CONTENT);
        }
        try {
            final byte[] buf = new byte[8192];
            int count = -1;
            while ((count = in.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, count);
            }
        } catch (final IOException e) {
            throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
        } finally {
            try {
                in.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Gets the (starting) boundary bytes by determining the <i>boundary</i> parameter from Content-Type header and prepending <i>--</i>.
     * 
     * @return The (starting) boundary bytes
     */
    private byte[] getBoundaryBytes() {
        if (boundaryBytes != null) {
            return boundaryBytes;
        }
        final String boundary = getContentType().getParameter("boundary");
        if (boundary == null || boundary.length() == 0) {
            throw new IllegalStateException("Missing boundary in multipart content-type");
        }
        return (boundaryBytes = getBytes(new StringBuilder(boundary.length() + 3).append('\n').append(STR_BD_START).append(boundary).toString()));
    }

    /**
     * Gets the input bytes either from data source or data array.
     * 
     * @return The input bytes either from data source or data array.
     * @throws IOException If accessing data source's input stream fails
     */
    private byte[] getInputBytes() throws IOException {
        return dataSource == null ? (data == null ? null : data) : copyStream(dataSource.getInputStream());
    }

    /**
     * The readObject method is responsible for reading from the stream and restoring the classes fields.
     * 
     * @param in The object input stream
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If a casting fails
     */
    private void readObject(final java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        /*
         * Restore common fields
         */
        in.defaultReadObject();
        dataSource = null;
    }

    /**
     * The writeObject method is responsible for writing the state of the object for its particular class so that the corresponding
     * readObject method can restore it.
     * 
     * @param out The object output stream
     * @throws IOException If an I/O error occurs
     */
    private void writeObject(final java.io.ObjectOutputStream out) throws java.io.IOException {
        try {
            loadContent();
        } catch (final MailException e) {
            final IOException ioex = new IOException(e.getMessage());
            ioex.initCause(e);
            throw ioex;
        }
        /*
         * Write common fields
         */
        out.defaultWriteObject();
    }

    /*-
     * ################ STATIC HELPERS ################
     */

    /**
     * Copies given input stream into a newly created byte array.
     * 
     * @param inputStream The input stream
     * @return The newly created byte array containing input stream's bytes
     * @throws IOException If reading input stream fails
     */
    private static byte[] copyStream(final InputStream inputStream) throws IOException {
        try {
            final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(SIZE);
            final byte[] buf = new byte[BUFSIZE];
            int len = -1;
            while ((len = inputStream.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, len);
            }
            return baos.toByteArray();
        } finally {
            try {
                inputStream.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Converts given string to a byte array.
     * 
     * @param s The string
     * @return The converted string's byte array
     */
    private static byte[] getBytes(final String s) {
        final char[] chars = s.toCharArray();
        final int size = chars.length;
        final byte[] bytes = new byte[size];
        for (int i = 0; i < size;) {
            bytes[i] = (byte) chars[i++];
        }
        return bytes;
    }

    /**
     * Finds the first occurrence of the pattern in the byte (sub-)array using KMP algorithm.
     * <p>
     * The sub-array to search in begins at the specified <code>beginIndex</code> and extends to the byte at index <code>endIndex - 1</code>
     * . Thus the length of the sub-array is <code>endIndex-beginIndex</code>.
     * 
     * @param data The byte array to search in
     * @param pattern The byte pattern to search for
     * @param beginIndex The beginning index, inclusive.
     * @param endIndex The ending index, exclusive.
     * @param computedFailures The computed failures where the pattern matches against itself; leave to <code>null</code> to compute from
     *            within
     * @return The index of the first occurrence of the pattern in the byte array starting from given index or <code>-1</code> if none
     *         found.
     */
    private static int indexOf(final byte[] data, final byte[] pattern, final int beginIndex, final int endIndex, final int[] computedFailures) {
        if ((beginIndex < 0) || (beginIndex > data.length)) {
            throw new IndexOutOfBoundsException(String.valueOf(beginIndex));
        }
        if ((endIndex < 0) || (endIndex > data.length)) {
            throw new IndexOutOfBoundsException(String.valueOf(endIndex));
        }
        if ((beginIndex > endIndex)) {
            throw new IndexOutOfBoundsException(String.valueOf(endIndex - beginIndex));
        }

        final int[] failure;
        if (computedFailures == null) {
            failure = computeFailure(pattern);
            if (failure == null) {
                throw new IllegalArgumentException("pattern is null");
            }
        } else {
            failure = computedFailures;
        }

        int j = 0;
        if (data.length == 0) {
            return -1;
        }

        for (int i = beginIndex; i < endIndex; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process, where the pattern matches against itself.
     * 
     * @param pattern The pattern
     * @return The failures
     */
    private static int[] computeFailure(final byte[] pattern) {
        if (pattern == null) {
            return null;
        }
        final int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }

}

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

package com.openexchange.mail.mime.dataobjects;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.extractHeader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.UnsupportedCharsetException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.utils.MessageUtility;
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

    private static final transient org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(MIMEMultipartMailPart.class);

    private static final int BUFSIZE = 8192; // 8K

    private static final int SIZE = 32768; // 32K

    private static final String STR_CONTENT_TYPE = "Content-Type";

    private static final String STR_BD_START = "--";

    private final DataAccess dataAccess;

    private byte[] boundaryBytes;

    private int count = -1;

    private int[] positions;

    /**
     * Initializes a new {@link MIMEMultipartMailPart}.
     *
     * @param dataSource The data source
     * @throws OXException If reading input stream fails
     */
    public MIMEMultipartMailPart(final DataSource dataSource) throws OXException {
        this(null, dataSource);
    }

    /**
     * Initializes a new {@link MIMEMultipartMailPart}.
     *
     * @param contentType The content type; may be <code>null</code>
     * @param dataSource The data source
     * @throws OXException If reading input stream fails
     */
    public MIMEMultipartMailPart(final ContentType contentType, final DataSource dataSource) throws OXException {
        super();
        if (contentType == null) {
            try {
                setContentType(extractHeader(STR_CONTENT_TYPE, dataSource.getInputStream(), true));
            } catch (final IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        } else {
            setContentType(contentType);
        }
        dataAccess = new DataSourceDataAccess(dataSource);
    }

    /**
     * Initializes a new {@link MIMEMultipartMailPart}.
     *
     * @param inputData The input data
     * @throws OXException If reading input stream fails
     */
    public MIMEMultipartMailPart(final byte[] inputData) throws OXException {
        this(null, inputData);
    }

    /**
     * Initializes a new {@link MIMEMultipartMailPart}.
     *
     * @param contentType The content type; may be <code>null</code>
     * @param inputData The input data
     * @throws OXException If reading input stream fails
     */
    public MIMEMultipartMailPart(final ContentType contentType, final byte[] inputData) throws OXException {
        super();
        if (contentType == null) {
            try {
                setContentType(extractHeader(STR_CONTENT_TYPE, new UnsynchronizedByteArrayInputStream(inputData), false));
            } catch (final IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        } else {
            setContentType(contentType);
        }
        dataAccess = new BytaArrayDataAccess(inputData);
    }

    @Override
    public Object getContent() throws OXException {
        return null;
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        return null;
    }

    @Override
    public int getEnclosedCount() throws OXException {
        if (count != -1) {
            return count;
        }
        final byte[] boundaryBytes = getBoundaryBytes();
        final byte[] dataBytes;
        try {
            dataBytes = dataAccess.full();
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
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
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, new StringBuilder(64).append(
                "Illegal access to multipart data at index ").append(e.getMessage()).append(", but total length is ").append(
                dataBytes.length).toString());
        }
        if (!endingBoundaryFound) {
            if (0 == count) {
                /*
                 * No starting boundary found
                 */
                if (LOG.isDebugEnabled()) {
                    final StringBuilder sb = new StringBuilder(dataBytes.length + 128);
                    sb.append("No boundary found in Multipart-Mail:\n");
                    sb.append(new String(dataBytes, Charsets.ISO_8859_1));
                    LOG.debug(sb.toString());
                }
                /*
                 * Take complete data as one part
                 */
                int hbLen = -1;
                int bodyStart;
                if ((bodyStart = indexOf(dataBytes, DELIM1, 0, dataBytes.length)) >= 0) {
                    hbLen = DELIM1.length;
                } else if ((bodyStart = indexOf(dataBytes, DELIM2, 0, dataBytes.length)) >= 0) {
                    hbLen = DELIM2.length;
                }
                positions[count++] = bodyStart < 0 ? 0 : bodyStart + hbLen;
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
                    sb.append(new String(dataBytes, Charsets.ISO_8859_1));
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

    /**
     * Sequence: LF LF
     */
    private static final byte[] DELIM2 = "\n\n".getBytes();

    /**
     * Sequence: LF CR LF
     */
    private static final byte[] DELIM1 = "\n\r\n".getBytes();

    public static int getHeaderEnd(final byte[] dataBytes) {
        int headerEnd = indexOf(dataBytes, DELIM1, 0, dataBytes.length);
        if (-1 == headerEnd) {
            headerEnd = indexOf(dataBytes, DELIM2, 0, dataBytes.length);
        }
        return headerEnd;
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
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        getEnclosedCount();
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        try {
            final byte[] subArr;
            {
                int i = index;
                int startIndex = positions[i++];
                if (startIndex >= dataAccess.length()) {
                    /*
                     * Empty (text) body
                     */
                    return createTextPart();
                }
                if ('\r' == dataAccess.read(startIndex)) {
                    startIndex += (getBoundaryBytes().length + 1);
                } else {
                    startIndex += (getBoundaryBytes().length);
                }
                /*
                 * Omit starting CR?LF
                 */
                if ('\r' == dataAccess.read(startIndex) && '\n' == dataAccess.read(startIndex + 1)) {
                    startIndex += 2;
                } else if ('\n' == dataAccess.read(startIndex)) {
                    startIndex++;
                }
                final int endIndex = i >= positions.length ? dataAccess.length() : positions[i];
                final int len = endIndex - startIndex;
                if (len <= 0) {
                    /*
                     * Empty (text) body
                     */
                    return createTextPart();
                }
                subArr = dataAccess.subarray(startIndex, len);
            }
            /*
             * Has headers?
             */
            if (getHeaderEnd(subArr) < 0) {
                try {
                    return createTextPart(subArr, CharsetDetector.detectCharset(new UnsynchronizedByteArrayInputStream(subArr)));
                } catch (final UnsupportedCharsetException e) {
                    return createTextPart(subArr, "ISO-8859-1");
                }
            }
            /*
             * Get content-type
             */
            final ContentType ct = new ContentType(extractHeader(STR_CONTENT_TYPE, new UnsynchronizedByteArrayInputStream(subArr), false));
            if (ct.isMimeType(MimeTypes.MIME_MULTIPART_ALL)) {
                return new MIMEMultipartMailPart(ct, subArr);
            }
//            else if (ct.startsWith(MIMETypes.MIME_MESSAGE_RFC822)) {
//                return MIMEMessageConverter.convertMessage(subArr);
//            }
            else {
                return MimeMessageConverter.convertPart(subArr);
            }
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static MailPart createTextPart() throws OXException {
        try {
            final MimeBodyPart mbp = new MimeBodyPart();
            MessageUtility.setText("", "us-ascii", mbp);
            // mbp.setText("", "US-ASCII");
            mbp.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            mbp.setHeader(MessageHeaders.HDR_CONTENT_TYPE, "text/plain; charset=\"US-ASCII\"");
            return MimeMessageConverter.convertPart(mbp);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static MailPart createTextPart(final byte[] subArr, final String charset) throws UnsupportedCharsetException, OXException {
        try {
            final MimeBodyPart mbp = new MimeBodyPart();
            MessageUtility.setText(new String(subArr, Charsets.forName(charset)), charset, mbp);
            // mbp.setText(new String(subArr, Charsets.forName(charset)), charset);
            mbp.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            mbp.setHeader(
                MessageHeaders.HDR_CONTENT_TYPE,
                new StringBuilder("text/plain; charset=\"").append(charset).append('"').toString());
            return MimeMessageConverter.convertPart(mbp);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return null;
    }

    @Override
    public void loadContent() throws OXException {
        try {
            dataAccess.load();
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void prepareForCaching() {
        dataAccess.prepareForCaching();
    }

    @Override
    public void writeTo(final OutputStream out) throws OXException {
        try {
            dataAccess.writeTo(out);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
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
        return (boundaryBytes =
            getBytes(new StringBuilder(boundary.length() + 3).append('\n').append(STR_BD_START).append(boundary).toString()));
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
        } catch (final OXException e) {
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
    static byte[] copyStream(final InputStream inputStream) throws IOException {
        try {
            final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(SIZE);
            final byte[] buf = new byte[BUFSIZE];
            int len = -1;
            while ((len = inputStream.read(buf, 0, buf.length)) > 0) {
                baos.write(buf, 0, len);
            }
            return baos.toByteArray();
        } finally {
            Streams.close(inputStream);
        }
    }

    /**
     * Converts given string to a byte array.
     *
     * @param s The string
     * @return The converted string's byte array
     */
    private static byte[] getBytes(final String s) {
        final int length = s.length();
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) s.charAt(i);
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
     * @param computedFailures The computed failures where the pattern matches against itself
     * @return The index of the first occurrence of the pattern in the byte array starting from given index or <code>-1</code> if none
     *         found.
     * @throws IndexOutOfBoundsException If <code>beginIndex</code> and/or <code>endIndex</code> is invalid
     * @throws IllegalArgumentException If given pattern is <code>null</code>
     */
    private static int indexOf(final byte[] data, final byte[] pattern, final int beginIndex, final int endIndex, final int[] computedFailures) {
        if (null == pattern) {
            throw new IllegalArgumentException("pattern is null");
        }
        if ((beginIndex < 0) || (beginIndex > data.length)) {
            throw new IndexOutOfBoundsException(String.valueOf(beginIndex));
        }
        if ((endIndex < 0) || (endIndex > data.length)) {
            throw new IndexOutOfBoundsException(String.valueOf(endIndex));
        }
        if ((beginIndex > endIndex)) {
            throw new IndexOutOfBoundsException(String.valueOf(endIndex - beginIndex));
        }

        int j = 0;
        if (data.length == 0) {
            return -1;
        }

        for (int i = beginIndex; i < endIndex; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = computedFailures[j - 1];
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
     * Finds the first occurrence of the pattern in the byte (sub-)array using KMP algorithm.
     * <p>
     * The sub-array to search in begins at the specified <code>beginIndex</code> and extends to the byte at index <code>endIndex - 1</code>
     * . Thus the length of the sub-array is <code>endIndex-beginIndex</code>.
     *
     * @param data The byte array to search in
     * @param pattern The byte pattern to search for
     * @param beginIndex The beginning index, inclusive.
     * @param endIndex The ending index, exclusive.
     * @return The index of the first occurrence of the pattern in the byte array starting from given index or <code>-1</code> if none
     *         found.
     * @throws IndexOutOfBoundsException If <code>beginIndex</code> and/or <code>endIndex</code> is invalid
     * @throws IllegalArgumentException If given pattern is <code>null</code>
     */
    private static int indexOf(final byte[] data, final byte[] pattern, final int beginIndex, final int endIndex) {
        if ((beginIndex < 0) || (beginIndex > data.length)) {
            throw new IndexOutOfBoundsException(String.valueOf(beginIndex));
        }
        if ((endIndex < 0) || (endIndex > data.length)) {
            throw new IndexOutOfBoundsException(String.valueOf(endIndex));
        }
        if ((beginIndex > endIndex)) {
            throw new IndexOutOfBoundsException(String.valueOf(endIndex - beginIndex));
        }

        final int[] failure = computeFailure(pattern);
        if (failure == null) {
            throw new IllegalArgumentException("pattern is null");
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

    private interface DataAccess {

        int length() throws IOException;

        int read(int index) throws IOException;

        byte[] subarray(int off, int len) throws IOException;

        byte[] full() throws IOException;

        void load() throws IOException;

        void prepareForCaching();

        void writeTo(final OutputStream out) throws IOException;
    }

    private static final class BytaArrayDataAccess implements DataAccess {

        private final byte[] data;

        public BytaArrayDataAccess(final byte[] data) {
            super();
            this.data = data;
        }

        @Override
        public byte[] full() {
            return data;
        }

        @Override
        public int length() {
            return data.length;
        }

        @Override
        public int read(final int index) {
            return (data[index] & 0xff); // As unsigned integer
        }

        @Override
        public byte[] subarray(final int off, final int len) {
            final byte[] ret = new byte[len];
            System.arraycopy(data, off, ret, 0, len);
            return ret;
        }

        @Override
        public void load() throws IOException {
            // Nothing to do
        }

        @Override
        public void prepareForCaching() {
            // Nothing to do
        }

        @Override
        public void writeTo(final OutputStream out) throws IOException {
            out.write(data, 0, data.length);
        }

    }

    private static final class RandomAccessDataAccess implements DataAccess {

        private RandomAccessFile randomAccess;

        private ByteBuffer roBuf;

        private int length;

        public RandomAccessDataAccess(final RandomAccessFile randomAccess) {
            super();
            this.randomAccess = randomAccess;
            length = -1;
        }

        @Override
        public byte[] full() throws IOException {
            final ByteBuffer roBuf = getByteBuffer();
            final int size = length();
            final byte[] bytes = new byte[size];
            roBuf.get(bytes, 0, size);
            return bytes;
        }

        private ByteBuffer getByteBuffer() throws IOException {
            if (null == roBuf) {
                final int size = length();
                roBuf = randomAccess.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, size);
            }
            return roBuf;
        }

        @Override
        public int length() throws IOException {
            if (length < 0) {
                length = (int) randomAccess.length();
            }
            return length;
        }

        @Override
        public int read(final int index) throws IOException {
            return (getByteBuffer().get(index) & 0xff); // As unsigned integer
        }

        @Override
        public byte[] subarray(final int off, final int len) throws IOException {
            final byte[] ret = new byte[len];
            getByteBuffer().get(ret, off, len);
            return ret;
        }

        @Override
        public void load() throws IOException {
            getByteBuffer();
        }

        @Override
        public void prepareForCaching() {
            randomAccess = null;
            roBuf = null;
        }

        @Override
        public void writeTo(final OutputStream out) throws IOException {
            out.write(full());
        }

    }

    private static final class DataSourceDataAccess implements DataAccess {

        private DataSource dataSource;

        private DataAccess delegate;

        public DataSourceDataAccess(final DataSource dataSource) {
            super();
            this.dataSource = dataSource;
        }

        private DataAccess getDelegate() throws IOException {
            if (null == delegate) {
                delegate = new BytaArrayDataAccess(copyStream(dataSource.getInputStream()));
            }
            return delegate;
        }

        @Override
        public byte[] full() throws IOException {
            return getDelegate().full();
        }

        @Override
        public int length() throws IOException {
            return getDelegate().length();
        }

        @Override
        public int read(final int index) throws IOException {
            return getDelegate().read(index);
        }

        @Override
        public byte[] subarray(final int off, final int len) throws IOException {
            return getDelegate().subarray(off, len);
        }

        @Override
        public void load() throws IOException {
            getDelegate();
        }

        @Override
        public void prepareForCaching() {
            dataSource = null;
        }

        @Override
        public void writeTo(final OutputStream out) throws IOException {
            final InputStream in = dataSource.getInputStream();
            if (null == in) {
                return;
            }
            try {
                final byte[] buf = new byte[8192];
                int count = -1;
                while ((count = in.read(buf, 0, buf.length)) > 0) {
                    out.write(buf, 0, count);
                }
            } finally {
                try {
                    in.close();
                } catch (final IOException e) {
                    LoggerFactory.getLogger(MIMEMultipartMailPart.DataSourceDataAccess.class).error("", e);
                }
            }
        }

    }

}

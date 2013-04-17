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

package com.openexchange.imap.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;

/**
 * {@link ThresholdInputStreamProvider} - Backs data in a <code>byte</code> array as long as specified threshold is not exceeded, but
 * streams data to a temporary file otherwise.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThresholdInputStreamProvider implements Closeable, InputStreamProvider {

    /** The default in-memory threshold of 500KB. */
    public static final int DEFAULT_IN_MEMORY_THRESHOLD = 500 * 1024; // 500KB

    /** The in-memory buffer where data is stored */
    private ByteArrayOutputStream buf;

    /** The number of valid bytes that were already written */
    private long count;

    /** The temporary file to stream to if threshold exceeded */
    private File tempFile;

    /** The in-memory threshold */
    private final int threshold;

    /** The initial capacity */
    private final int initalCapacity;

    /**
     * Initializes a new {@link ThresholdInputStreamProvider} with default threshold and default initial capacity.
     */
    public ThresholdInputStreamProvider() {
        this(-1, -1);
    }

    /**
     * Initializes a new {@link ThresholdInputStreamProvider} with default initial capacity.
     *
     * @param threshold The threshold
     */
    public ThresholdInputStreamProvider(final int threshold) {
        this(threshold, -1);
    }

    /**
     * Initializes a new {@link ThresholdInputStreamProvider}.
     *
     * @param threshold The threshold
     * @param initalCapacity The initial capacity
     */
    public ThresholdInputStreamProvider(final int threshold, final int initalCapacity) {
        super();
        count = 0;
        this.threshold = threshold > 0 ? threshold : DEFAULT_IN_MEMORY_THRESHOLD;
        this.initalCapacity = initalCapacity > 0 ? initalCapacity : 2048;
    }

    /**
     * Gets the {@link OutputStream} view on this file holder.
     *
     * @return An {@link OutputStream} that writes data into this file holder
     */
    public OutputStream asOutputStream() {
        return new TransferringOutStream(this);
    }

    /**
     * Writes the specified content to this file holder.
     *
     * @param bytes The content to be written.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @return This file holder with content written
     * @throws IOException If write attempt fails
     * @throws IndexOutOfBoundsException If illegal arguments are specified
     */
    public ThresholdInputStreamProvider write(final byte[] bytes, final int off, final int len) throws IOException {
        if (bytes == null) {
            return this;
        }
        if ((off < 0) || (off > bytes.length) || (len < 0) || ((off + len) > bytes.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return this;
        }
        return write(new com.openexchange.java.UnsynchronizedByteArrayInputStream(bytes, off, len));
    }

    /**
     * Writes the specified content to this file holder.
     *
     * @param bytes The content to be written.
     * @return This file holder with content written
     * @throws IOException If write attempt fails
     */
    public ThresholdInputStreamProvider write(final byte[] bytes) throws IOException {
        if (bytes == null) {
            return this;
        }
        if (null == tempFile && null == buf && bytes.length > threshold) {
            // Nothing written & content does exceed threshold
            final File tempFile = newTempFile();
            this.tempFile = tempFile;
            OutputStream out = null;
            try {
                out = new FileOutputStream(tempFile);
                out.write(bytes, 0, bytes.length);
                out.flush();
            } finally {
                Streams.close(out);
            }
            return this;
        }
        // Deal with possible available content
        return write(Streams.newByteArrayInputStream(bytes));
    }

    /**
     * Writes the specified content to this file holder.
     * <p>
     * Orderly closes specified {@link InputStream} instance.
     *
     * @param in The content to be written.
     * @return This file holder with content written
     * @throws IOException If write attempt fails
     */
    public ThresholdInputStreamProvider write(final InputStream in) throws IOException {
        if (null == in) {
            return this;
        }
        OutputStream out = null;
        try {
            File tempFile = this.tempFile;
            long count = this.count;
            if (null == tempFile) {
                // Threshold not yet exceeded
                ByteArrayOutputStream baos = buf;
                if (null == baos) {
                    baos = Streams.newByteArrayOutputStream(initalCapacity);
                    this.buf = baos;
                }
                out = baos;
                final int inMemoryThreshold = threshold;
                final int buflen = 0xFFFF; // 64KB
                final byte[] buffer = new byte[buflen];
                for (int len; (len = in.read(buffer, 0, buflen)) > 0;) {
                    // Count bytes
                    count += len;
                    if ((null == tempFile) && (count > inMemoryThreshold)) {
                        // Stream to file because threshold is exceeded
                        tempFile = newTempFile();
                        this.tempFile = tempFile;
                        out = new FileOutputStream(tempFile);
                        out.write(baos.toByteArray());
                        baos = null;
                        buf = null;
                    }
                    out.write(buffer, 0, len);
                }
                out.flush();
            } else {
                // Threshold already exceeded. Stream to file.
                out = new FileOutputStream(tempFile);
                final int buflen = 0xFFFF; // 64KB
                final byte[] buffer = new byte[buflen];
                for (int len; (len = in.read(buffer, 0, buflen)) > 0;) {
                    // Count bytes
                    count += len;
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            this.count = count;
        } finally {
            Streams.close(in);
            Streams.close(out);
        }
        return this;
    }

    /**
     * Gets the number of valid bytes written to this file holder.
     *
     * @return The number of bytes
     */
    public long getCount() {
        return count;
    }

    @Override
    public void close() {
        final File tempFile = this.tempFile;
        if (null != tempFile) {
            tempFile.delete();
            this.tempFile = null;
        }
        this.buf = null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            close();
        } catch (final Exception ignore) {
            // Ignore
        }
    }

    /**
     * Gets this file holder content as a byte array.
     *
     * @return The byte array
     * @throws OXException If byte array cannot be returned for any reason
     */
    public byte[] toByteArray() throws OXException {
        final ByteArrayOutputStream buf = this.buf;
        if (null != buf) {
            return buf.toByteArray();
        }
        final File tempFile = this.tempFile;
        if (null == tempFile) {
            final IOException e = new IOException("Already closed.");
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
        InputStream in = null;
        try {
            in = new FileInputStream(tempFile);
            final ByteArrayOutputStream baos = Streams.newByteArrayOutputStream(in.available());
            final int buflen = 0xFFFF; // 64KB
            final byte[] buffer = new byte[buflen];
            for (int len; (len = in.read(buffer, 0, buflen)) > 0;) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos.toByteArray();
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.imap.util.InputStreamProvider#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws OXException {
        final ByteArrayOutputStream buf = this.buf;
        if (null != buf) {
            return Streams.asInputStream(buf);
        }
        final File tempFile = this.tempFile;
        if (null == tempFile) {
            final IOException e = new IOException("Already closed.");
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
        try {
            return new FileInputStream(tempFile);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the length.
     *
     * @return The length or <code>-1</code>
     */
    public long getLength() {
        final ByteArrayOutputStream buf = this.buf;
        if (null != buf) {
            return buf.size();
        }
        final File tempFile = this.tempFile;
        if (null == tempFile) {
            throw new IllegalStateException("Already closed.");
        }
        return tempFile.length();
    }

    private static final class TransferringOutStream extends OutputStream {

        private final ThresholdInputStreamProvider fileHolder;

        TransferringOutStream(final ThresholdInputStreamProvider fileHolder) {
            super();
            this.fileHolder = fileHolder;
        }

        @Override
        public void write(final int b) throws IOException {
            fileHolder.write(new byte[] { (byte) b });
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            fileHolder.write(b, off, len);
        }
    } // End of class TransferringOutStream

    private static volatile File uploadDirectory;

    private static File uploadDirectory() {
        File tmp = uploadDirectory;
        if (null == tmp) {
            synchronized (ThresholdInputStreamProvider.class) {
                tmp = uploadDirectory;
                if (null == tmp) {
                    final ConfigurationService service = IMAPServiceRegistry.getService(ConfigurationService.class);
                    tmp = new File(null == service ? "/tmp" : service.getProperty("UPLOAD_DIRECTORY", "/tmp"));
                    uploadDirectory = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Creates a new empty file. If this method returns successfully then it is guaranteed that:
     * <ol>
     * <li>The file denoted by the returned abstract pathname did not exist before this method was invoked, and
     * <li>Neither this method nor any of its variants will return the same abstract pathname again in the current invocation of the virtual
     * machine.
     * </ol>
     *
     * @return An abstract pathname denoting a newly-created empty file
     * @throws IOException If a file could not be created
     */
    private static File newTempFile() throws IOException {
        final File tmpFile = File.createTempFile("open-xchange-", ".tmp", uploadDirectory());
        tmpFile.deleteOnExit();
        return tmpFile;
    }

}

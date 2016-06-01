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

package com.openexchange.ajax.container;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import javax.mail.internet.SharedInputStream;
import javax.mail.util.SharedByteArrayInputStream;
import javax.mail.util.SharedFileInputStream;
import com.openexchange.ajax.fileholder.ByteArrayRandomAccess;
import com.openexchange.ajax.fileholder.FileRandomAccess;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.fileholder.InputStreamReadable;
import com.openexchange.ajax.fileholder.Readable;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ThresholdFileHolder} - A {@link IFileHolder} that backs data in a <code>byte</code> array as long as specified threshold is not
 * exceeded, but streams data to a temporary file otherwise.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThresholdFileHolder implements IFileHolder {

    /** The default in-memory threshold of 500 KB. */
    public static final int DEFAULT_IN_MEMORY_THRESHOLD = 500 * 1024; // 500KB

    /** The in-memory buffer where data is stored */
    private ByteArrayOutputStream buf;

    /** The number of valid bytes that were already written */
    private long count;

    /** The temporary file to stream to if threshold exceeded */
    private File tempFile;

    /** The file name */
    private String name;

    /** The <code>Content-Type</code> value */
    private String contentType;

    /** The disposition */
    private String disposition;

    /** The delivery model */
    private String delivery;

    /** The in-memory threshold */
    private final int threshold;

    /** The initial capacity */
    private final int initalCapacity;

    /** The list for post-processing tasks */
    private final List<Runnable> tasks;

    /** <code>true</code> to signal automatic management for the created file (deleted after processing threads terminates); otherwise <code>false</code> to let the caller control file's life-cycle */
    private final boolean autoManaged;

    /**
     * Initializes a new {@link ThresholdFileHolder} with default threshold (500 KB) and default initial capacity (64 KB).
     */
    public ThresholdFileHolder() {
        this(-1, -1);
    }

    /**
     * Initializes a new {@link ThresholdFileHolder} with default threshold (500 KB) and default initial capacity (64 KB).
     *
     * @param autoManaged <code>true</code> to signal automatic management for the created file (deleted after processing threads terminates); otherwise <code>false</code> to let the caller control file's life-cycle
     */
    public ThresholdFileHolder(boolean autoManaged) {
        this(-1, -1, autoManaged);
    }

    /**
     * Initializes a new {@link ThresholdFileHolder} with default initial capacity (64 KB).
     *
     * @param threshold The threshold
     */
    public ThresholdFileHolder(int threshold) {
        this(threshold, -1);
    }

    /**
     * Initializes a new {@link ThresholdFileHolder}.
     *
     * @param threshold The threshold
     * @param initalCapacity The initial capacity
     */
    public ThresholdFileHolder(int threshold, int initalCapacity) {
        this(threshold, initalCapacity, true);
    }

    /**
     * Initializes a new {@link ThresholdFileHolder}.
     *
     * @param threshold The threshold
     * @param initalCapacity The initial capacity
     * @param autoManaged <code>true</code> to signal automatic management for the created file (deleted after processing threads terminates); otherwise <code>false</code> to let the caller control file's life-cycle
     */
    public ThresholdFileHolder(int threshold, int initalCapacity, boolean autoManaged) {
        super();
        this.autoManaged = autoManaged;
        count = 0;
        this.threshold = threshold > 0 ? threshold : DEFAULT_IN_MEMORY_THRESHOLD;
        contentType = "application/octet-stream";
        this.initalCapacity = initalCapacity > 0 ? initalCapacity : 65536;
        tasks = new LinkedList<Runnable>();
    }

    /**
     * Creates a copy from given {@link IFileHolder}.
     *
     * @param source The source file holder
     * @throws OXException If an error occurs
     */
    public ThresholdFileHolder(IFileHolder source) throws OXException {
        this();
        write(source.getStream());
        name = source.getName();
        contentType = source.getContentType();
        delivery = source.getDelivery();
        disposition = source.getDisposition();
    }

    /**
     * Resets this file holder.
     * <p>
     * Deletes associated file (if set) and resets internal buffer.
     */
    public void reset() {
        final File tempFile = this.tempFile;
        if (null != tempFile) {
            tempFile.delete();
            this.tempFile = null;
        }
        final ByteArrayOutputStream  baos = this.buf;
        if (null != baos) {
            baos.reset();
        }
    }

    /**
     * Gets the internal buffer.
     *
     * @return The buffer
     * @see #isInMemory()
     */
    public ByteArrayOutputStream getBuffer() {
        return buf;
    }

    /**
     * Checks if file holder's content is completely held in memory.
     *
     * @return <code>true</code> if in memory; otherwise <code>false</code>
     */
    public boolean isInMemory() {
        return (null == tempFile) && (buf != null);
    }

    /**
     * Gets the optional temporary file.
     * <p>
     * If {@link #isInMemory()} signals <code>true</code>, then this method will return <code>null</code>, and the content should rather be obtained by {@link #getBuffer()}.
     *
     * @return The temporary file or <code>null</code>
     * @see #isInMemory()
     */
    public File getTempFile() {
        return tempFile;
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
     * @throws OXException If write attempt fails
     * @throws IndexOutOfBoundsException If illegal arguments are specified
     */
    public ThresholdFileHolder write(final byte[] bytes, final int off, final int len) throws OXException {
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
     * @throws OXException If write attempt fails
     */
    public ThresholdFileHolder write(final byte[] bytes) throws OXException {
        if (bytes == null) {
            return this;
        }
        if (null == tempFile && null == buf && bytes.length > threshold) {
            // Nothing written & content does exceed threshold
            final File tempFile = TmpFileFileHolder.newTempFile(autoManaged);
            this.tempFile = tempFile;
            OutputStream out = null;
            try {
                out = new FileOutputStream(tempFile);
                out.write(bytes, 0, bytes.length);
                out.flush();
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
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
     * @throws OXException If write attempt fails
     */
    public ThresholdFileHolder write(final InputStream in) throws OXException {
        if (null == in) {
            return this;
        }
        return write(new InputStreamReadable(in));
    }

    /**
     * Writes the specified content to this file holder.
     * <p>
     * Orderly closes specified {@link InputStream} instance.
     *
     * @param in The content to be written.
     * @return This file holder with content written
     * @throws OXException If write attempt fails
     */
    public ThresholdFileHolder write(final Readable in) throws OXException {
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
                        tempFile = TmpFileFileHolder.newTempFile(autoManaged);
                        this.tempFile = tempFile;
                        out = new FileOutputStream(tempFile);
                        baos.writeTo(out);
                        baos = null;
                        buf = null;
                    }
                    out.write(buffer, 0, len);
                }
                out.flush();
            } else {
                // Threshold already exceeded. Stream to file.
                out = new FileOutputStream(tempFile, true);
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
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
            Streams.close(out);
        }
        return this;
    }

    /**
     * Gets the MD5 sum for this file holder's content
     *
     * @return The MD5 sum
     * @throws OXException If MD5 sum cannot be returned
     */
    public String getMD5() throws OXException {
        File tempFile = this.tempFile;
        if (null != tempFile) {
            DigestInputStream digestStream = null;
            try {
                digestStream = new DigestInputStream(new FileInputStream(tempFile), MessageDigest.getInstance("MD5"));
                byte[] buf = new byte[8192];
                for (int read; (read = digestStream.read(buf, 0, 8192)) > 0;) {
                    ;
                }
                byte[] digest = digestStream.getMessageDigest().digest();
                return jonelo.jacksum.util.Service.format(digest);
            } catch (NoSuchAlgorithmException e) {
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } finally {
                Streams.close(digestStream);
            }
        }

        // In memory...
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(Streams.stream2bytes(getStream()));
            return jonelo.jacksum.util.Service.format(digest);
        } catch (NoSuchAlgorithmException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
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
    public boolean repetitive() {
        return true;
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
     * Writes the complete contents of this file holder to the specified output stream argument, as if by calling the output stream's write
     * method using <code>out.write(buf, 0, count)</code>.
     *
     * @param out the output stream to which to write the data.
     * @throws OXException If an I/O error occurs.
     */
    public void writeTo(OutputStream out) throws OXException {
        if (count <= 0) {
            return;
        }
        final ByteArrayOutputStream buf = this.buf;
        if (null == buf) {
            final File tempFile = this.tempFile;
            if (null == tempFile) {
                final IOException e = new IOException("Already closed.");
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
            InputStream in = null;
            try {
                in = new FileInputStream(tempFile);
                final int buflen = 0xFFFF; // 64KB
                final byte[] buffer = new byte[buflen];
                for (int len; (len = in.read(buffer, 0, buflen)) > 0;) {
                    out.write(buffer, 0, len);
                }
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } finally {
                Streams.close(in);
            }
        } else {
            try {
                buf.writeTo(out);
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
    }

    /**
     * Gets this file holder content as a byte array.
     *
     * @return The byte array
     * @throws OXException If byte array cannot be returned for any reason
     */
    public byte[] toByteArray() throws OXException {
        if (count <= 0) {
            return new byte[0];
        }
        final ByteArrayOutputStream buf = this.buf;
        if (null != buf) {
            return buf.toByteArray();
        }
        final File tempFile = this.tempFile;
        if (null == tempFile) {
            final IOException e = new IOException("Already closed.");
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
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
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
        }
    }

    /**
     * Creates a copy of this file holder.
     *
     * @return A copy
     * @throws OXException If returning a copy fails
     */
    public ThresholdFileHolder copy() throws OXException {
        final ThresholdFileHolder copy = new ThresholdFileHolder();
        copy.count = count;
        copy.contentType = contentType;
        copy.delivery = delivery;
        copy.disposition = disposition;
        copy.name = name;

        // Check if content is available
        if (count <= 0) {
            // No content to make a copy of
            return copy;
        }

        // Check internal buffer vs temp. file
        final ByteArrayOutputStream buf = this.buf;
        if (null != buf) {
            copy.buf = new UnsynchronizedByteArrayOutputStream(buf);
        } else if (null != tempFile) {
            try {
                final File newTempFile = TmpFileFileHolder.newTempFile(autoManaged);
                copyFile(tempFile, newTempFile);
                copy.tempFile = newTempFile;
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }

        return copy;
    }

    /**
     * Gets the input stream for this file holder's content.
     * <p>
     * Closing the stream will also {@link #close() close} this file holder.
     *
     * @return The input stream
     * @throws OXException If input stream cannot be returned
     */
    public InputStream getClosingStream() throws OXException {
        return new ClosingInputStream(this);
    }

    @Override
    public InputStream getStream() throws OXException {
        if (count <= 0) {
            return Streams.EMPTY_INPUT_STREAM;
        }
        ByteArrayOutputStream buf = this.buf;
        if (null != buf) {
            return Streams.asInputStream(buf);
        }
        File tempFile = this.tempFile;
        if (null == tempFile) {
            IOException e = new IOException("Already closed.");
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
        try {
            return new FileInputStream(tempFile);
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the random access for this file holder's content.
     * <p>
     * Closing the random access will also {@link #close() close} this file holder.
     *
     * @return The random access
     * @throws OXException If random access cannot be returned
     */
    public RandomAccess getClosingRandomAccess() throws OXException {
        return new ClosingRandomAccess(this);
    }

    @Override
    public RandomAccess getRandomAccess() throws OXException {
        if (count <= 0) {
            return new ByteArrayRandomAccess(new byte[0]);
        }

        ByteArrayOutputStream buf = this.buf;
        if (null != buf) {
            return new ByteArrayRandomAccess(buf.toByteArray());
        }

        File tempFile = this.tempFile;
        if (null != tempFile) {
            try {
                return new FileRandomAccess(tempFile);
            } catch (FileNotFoundException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }

        return null;
    }

    /**
     * Gets this instance's content as a {@link SharedInputStream} appropriate to create MIME resources from it
     *
     * @return The shared input stream
     * @throws IOException If an I/O error occurs
     */
    public SharedInputStream getSharedStream() throws IOException {
        if (count <= 0) {
            return new SharedByteArrayInputStream(new byte[0]);
        }
        ByteArrayOutputStream buf = this.buf;
        if (null != buf) {
            return new SharedByteArrayInputStream(buf.toByteArray());
        }
        File tempFile = this.tempFile;
        if (null == tempFile) {
            throw new IOException("Already closed.");
        }
        return new SharedFileInputStream(tempFile);
    }

    @Override
    public long getLength() {
        if (count <= 0) {
            return 0;
        }
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

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisposition() {
        return disposition;
    }

    @Override
    public String getDelivery() {
        return delivery;
    }

    /**
     * Sets the content information retrieved from passed file holder.
     * <ul>
     * <li>MIME type</li>
     * <li>Disposition</li>
     * <li>Name</li>
     * <li>Delivery</li>
     * </ul>
     *
     * @param fileHolder The file holder to get the content information from
     * @return This file holder instance with content information applied
     */
    public ThresholdFileHolder setContentInfo(IFileHolder fileHolder) {
        if (null != fileHolder) {
            setContentType(fileHolder.getContentType());
            setDelivery(fileHolder.getDelivery());
            setDisposition(fileHolder.getDisposition());
            setName(fileHolder.getName());
        }
        return this;
    }

    @Override
    public List<Runnable> getPostProcessingTasks() {
        return tasks;
    }

    @Override
    public void addPostProcessingTask(Runnable task) {
        if (null != task) {
            tasks.add(task);
        }
    }

    /**
     * Sets the disposition.
     *
     * @param disposition The disposition
     */
    public void setDisposition(final String disposition) {
        this.disposition = disposition;
    }

    /**
     * Sets the content type; e.g. "application/octet-stream"
     *
     * @param contentType The content type
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the (file) name.
     *
     * @param name The name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the delivery
     *
     * @param delivery The delivery to set
     */
    public void setDelivery(final String delivery) {
        this.delivery = delivery;
    }

    private static final class TransferringOutStream extends OutputStream {

        private final ThresholdFileHolder fileHolder;

        TransferringOutStream(final ThresholdFileHolder fileHolder) {
            super();
            this.fileHolder = fileHolder;
        }

        @Override
        public void write(final int b) throws IOException {
            try {
                fileHolder.write(new byte[] { (byte) b });
            } catch (final OXException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }
                throw new IOException(e);
            }
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            try {
                fileHolder.write(b, off, len);
            } catch (final OXException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }
                throw new IOException(e);
            }
        }
    } // End of class TransferringOutStream

    private static final class ClosingInputStream extends FilterInputStream {

        private final ThresholdFileHolder fileHolder;

        /**
         * Initializes a new {@link ClosingInputStream}.
         */
        protected ClosingInputStream(final ThresholdFileHolder fileHolder) throws OXException {
            super(fileHolder.getStream());
            this.fileHolder = fileHolder;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                fileHolder.close();
            }
        }
    }

    private static final class ClosingRandomAccess implements RandomAccess {

        private final ThresholdFileHolder fileHolder;
        private final RandomAccess randomAccess;

        /**
         * Initializes a new {@link ClosingInputStream}.
         */
        protected ClosingRandomAccess(ThresholdFileHolder fileHolder) throws OXException {
            super();
            this.fileHolder = fileHolder;
            this.randomAccess = fileHolder.getRandomAccess();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return randomAccess.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return randomAccess.read(b, off, len);
        }

        @Override
        public void seek(long pos) throws IOException {
            randomAccess.seek(pos);
        }

        @Override
        public long length() throws IOException {
            return randomAccess.length();
        }

        @Override
        public void close() throws IOException {
            try {
                randomAccess.close();
            } finally {
                fileHolder.close();
            }
        }
    }

    private static void copyFile(final File in, final File out) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(in).getChannel();
            outChannel = new FileOutputStream(out).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (final IOException e) {
            throw e;
        } finally {
            Streams.close(inChannel, outChannel);
        }
    }

    private static final char[] hexadecimal = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Encodes the 128 bit (16 bytes) MD5 into a 32 character String.
     *
     * @param binaryData The digest
     * @return The encoded MD5, or <code>null</code> if encoding failed
     */
    public static String md5Encode(byte[] binaryData) {
        if (null == binaryData || binaryData.length != 16) {
            return null;
        }

        char[] buffer = new char[32];

        for (int i = 0; i < 16; i++) {
            int low = binaryData[i] & 0x0f;
            int high = (binaryData[i] & 0xf0) >> 4;
            buffer[i << 1] = hexadecimal[high];
            buffer[(i << 1) + 1] = hexadecimal[low];
        }

        return new String(buffer);

    }

}

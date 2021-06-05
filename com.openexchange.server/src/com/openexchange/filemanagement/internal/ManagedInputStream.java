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

package com.openexchange.filemanagement.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link ManagedInputStream} - A managed input stream which spools data to disk if necessary.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagedInputStream extends InputStream {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ManagedInputStream.class);

    private static final int SIZE_LIMIT = 1048576; // 1MB

    /*-
     * ###################### MEMBER SECTION ############################
     */

    private final InputStream delegate;

    /**
     * Initializes a new {@link ManagedInputStream} from specified bytes with default capacity of <code>1048576</code> (1 MB).
     *
     * @param bytes The bytes held by this input stream
     * @param management The file management possibly used
     * @throws OXException If size exceeds memory limit and an appropriate managed file cannot be created.
     */
    public ManagedInputStream(final byte[] bytes, final ManagedFileManagement management) throws OXException {
        this(bytes, SIZE_LIMIT, management);
    }

    /**
     * Initializes a new {@link ManagedInputStream} from specified bytes.
     *
     * @param bytes The bytes held by this input stream
     * @param capacity The number of bytes allowed being kept in memory rather than being spooled to disk.
     * @param management The file management possibly used
     * @throws OXException If size exceeds memory limit and an appropriate managed file cannot be created.
     */
    public ManagedInputStream(final byte[] bytes, final int capacity, final ManagedFileManagement management) throws OXException {
        super();
        if (bytes.length <= capacity) {
            // keep in memory
            delegate = new UnsynchronizedByteArrayInputStream(bytes);
        } else {
            // write to disk;
            delegate = management.createManagedFile(bytes).getInputStream();
        }
    }

    /**
     * Initializes a new {@link ManagedInputStream} from specified input stream with default capacity of <code>1048576</code> (1 MB) and
     * unknown stream size.
     *
     * @param in The input stream to manage
     * @param management The file management possibly used
     * @throws OXException If an appropriate managed file cannot be created.
     */
    public ManagedInputStream(final InputStream in, final ManagedFileManagement management) throws OXException {
        this(in, -1, SIZE_LIMIT, management);
    }

    /**
     * Initializes a new {@link ManagedInputStream} from specified input stream with unknown stream size.
     *
     * @param in The input stream to manage
     * @param capacity The number of bytes allowed being kept in memory rather than being spooled to disk.
     * @param management The file management possibly used
     * @throws OXException If an appropriate managed file cannot be created.
     */
    public ManagedInputStream(final InputStream in, final int capacity, final ManagedFileManagement management) throws OXException {
        this(in, -1, capacity, management);
    }

    /**
     * Initializes a new {@link ManagedInputStream} from specified input stream.
     *
     * @param in The input stream to manage
     * @param size The stream's size; leave to <code>-1</code> if unknown
     * @param capacity The number of bytes allowed being kept in memory rather than being spooled to disk.
     * @param management The file management possibly used
     * @throws OXException If size exceeds memory limit and an appropriate managed file cannot be created.
     */
    public ManagedInputStream(final InputStream in, final int size, final int capacity, final ManagedFileManagement management) throws OXException {
        super();
        if (size >= 0 && size <= capacity) {
            // keep in memory
            delegate = in;
        } else {
            // write to disk if necessary
            try {
                final ByteArrayOutputStream tmp = new UnsynchronizedByteArrayOutputStream(8192);
                final byte[] buf = new byte[2048];
                int len = -1;
                int nob = 0;
                // read up to SIZE_LIMIT bytes
                while (nob < capacity && (len = in.read(buf, 0, buf.length)) > 0) {
                    tmp.write(buf, 0, len);
                    nob += len;
                }
                if (nob >= capacity) {
                    delegate = management.createManagedFile(new CombinedInputStream(tmp.toByteArray(), in)).getInputStream();
                } else {
                    delegate = new UnsynchronizedByteArrayInputStream(tmp.toByteArray());
                    // Input stream completely copied to memory, so close it
                    in.close();
                }
            } catch (IOException e) {
                throw ManagedFileExceptionErrorMessage.IO_ERROR.create(e, e.getMessage());
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void mark(final int readAheadLimit) {
        delegate.mark(readAheadLimit);
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(final byte b[], final int off, final int len) throws IOException {
        return delegate.read(b, off, len);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public long skip(final long n) throws IOException {
        return delegate.skip(n);
    }

    /*-
     * ###################### INNER CLASS ############################
     */

    private static final class CombinedInputStream extends InputStream {

        private final byte[] consumed;

        private final InputStream remaining;

        private int count;

        /**
         * Initializes a new {@link CombinedInputStream}.
         *
         * @param consumed The bytes already consumed from <code>remaining</code> input stream
         * @param remaining The remaining stream
         */
        public CombinedInputStream(final byte[] consumed, final InputStream remaining) {
            super();
            this.consumed = consumed;
            this.remaining = remaining;
            count = 0;
        }

        @Override
        public int read() throws IOException {
            if (count < consumed.length) {
                return consumed[count++];
            }
            return remaining.read();
        }

        @Override
        public int read(final byte b[]) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(final byte b[], final int off, final int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return 0;
            }
            if (count < consumed.length) {
                final int buffered = consumed.length - count;
                if (buffered >= len) {
                    // Buffered content offers up to len bytes.
                    final int retval = (buffered <= len) ? buffered : len; // Math.min(int a, int b)
                    System.arraycopy(consumed, count, b, off, retval);
                    count += retval;
                    return retval;
                }
                // Buffered content does NOT offer up to len bytes.
                System.arraycopy(consumed, count, b, off, buffered);
                count += buffered;
                return buffered + remaining.read(b, off + buffered, len - buffered);
            }
            return remaining.read(b, off, len);
        }
    } // End of CombinedInputStream class

}

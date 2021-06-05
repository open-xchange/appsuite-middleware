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

package com.openexchange.filestore.sproxyd;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.sproxyd.chunkstorage.Chunk;
import com.openexchange.filestore.sproxyd.impl.SproxydClient;
import com.openexchange.java.FastBufferedInputStream;
import com.openexchange.java.Streams;

/**
 * {@link SproxydBufferedInputStream} - The Sproxyd buffered input stream.
 * <p>
 * This implementation is not thread-safe.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@NotThreadSafe
public class SproxydBufferedInputStream extends InputStream {

    private final List<Chunk> documentChunks;
    private final int numberOfChunks;
    private final SproxydClient client;
    private InputStream current;
    private int chunkPos;
    private final long start;
    private final long end;

    /**
     * Initializes a new {@link SproxydBufferedInputStream}.
     *
     * @param documentChunks The document's chunks
     * @param client The client to use
     */
    public SproxydBufferedInputStream(List<Chunk> documentChunks, SproxydClient client) {
        this(documentChunks, client, 0, -1);
    }

    /**
     * Initializes a new {@link SproxydBufferedInputStream}.
     *
     * @param documentChunks The document's chunks
     * @param client The client to use
     * @param start The range start (inclusive), or <code>0</code> to start reading from the beginning
     * @param end The range end (inclusive), or <code>-1</code> to read until the end
     */
    public SproxydBufferedInputStream(List<Chunk> documentChunks, SproxydClient client, long start, long end) {
        super();
        this.documentChunks = documentChunks;
        this.numberOfChunks = documentChunks.size();
        this.client = client;
        this.start = start;
        this.end = end;
        this.chunkPos = 0;
    }

    private InputStream initNext() throws OXException {
        // Initialize next applicable chunk
        while (true) {
            if (chunkPos >= numberOfChunks) {
                // No more chunks available
                return null;
            }
            // Next chunk
            Chunk chunk = documentChunks.get(chunkPos++);
            long[] relativeRange = getRelativeRange(chunk, start, end);
            if (null != relativeRange) {
                InputStream in = (0 == relativeRange.length) ? client.get(chunk.getScalityId()) : client.get(chunk.getScalityId(), relativeRange[0], relativeRange[1]);
                if (!(in instanceof BufferedInputStream) && !(in instanceof ByteArrayInputStream)) {
                    in = new FastBufferedInputStream(in);
                }

                current = in;
                return in;
            }
        }
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        if (b == null) {
            throw new NullPointerException("Byte array is null");
        } else if (offset < 0) {
            throw new IndexOutOfBoundsException("Offset is negative");
        } else if (length < 0) {
            throw new IndexOutOfBoundsException("Length is negative");
        } else if (length > b.length - offset) {
            throw new IndexOutOfBoundsException("The number of bytes to read ("+length+") does not fit into byte array capacity " + (b.length - offset));
        }

        if (length == 0) {
            return 0;
        }

        try {
            InputStream in = current;

            if (null == in) {
                in = initNext();
                if (null == in) {
                    return -1;
                }
            }

            int off = offset;
            int len = length;

            int retval = -1;
            while (retval < length) {
                int read = in.read(b, off, len);
                if (read <= 0) {
                    // Current stream reached EOF
                    Streams.close(in);
                    current = null;
                    in = initNext();
                    if (null == in) {
                        return retval;
                    }
                } else {
                    retval = (0 > retval ? read : (retval + read));
                    if (retval >= length) {
                        return retval;
                    }

                    off += read;
                    len -= read;
                }
            }
            return retval;

        } catch (OXException e) {
            throw toIOException(e);
        }
    }

    @Override
    public int read() throws IOException {
        try {
            InputStream in = current;

            if (null == in) {
                in = initNext();
                if (null == in) {
                    return -1;
                }
            }

            int read = in.read();
            if (read < 0) {
                Streams.close(in);
                current = null;
                return read();
            }

            return read;
        } catch (OXException e) {
            throw toIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        Streams.close(current);
        super.close();
    }

    private IOException toIOException(OXException e) {
        Throwable cause = e.getCause();
        if (cause instanceof IOException) {
            return (IOException) cause;
        }
        return new IOException(e.getMessage(), e);
    }

    /**
     * Gets an array representing the relative byte range for a specific chunk, based on the defined parent range-start and -end applied
     * to this combined stream. If the supplied chunk can be consumed "as-is", or if no range is defined, an empty array is returned. If
     * the chunk is out of the specified range and should not be included at all in the combined stream, <code>null</code> is returned.
     *
     * @param chunk The chunk to get the relative range for
     * @param start The (inclusive) range-start of the parent stream
     * @param end The (inclusive) range-end of the parent stream
     * @return The relative range, with the range-start in the first, and the range-end in the second array element, an empty array if no
     *         range has to be applied for this chunk, or <code>null</code> if not applicable
     */
    static long[] getRelativeRange(Chunk chunk, long start, long end) {
        if (0 >= start && 0 > end) {
            return new long[0]; // no range
        }
        long chunkStart = chunk.getOffset();
        long chunkEnd = chunkStart + chunk.getLength() - 1;
        if (0 < start && start > chunkEnd) {
            return null; // requested range is behind this chunk
        }
        if (0 <= end && end < chunkStart) {
            return null; // requested range is before this chunk
        }
        if (start <= chunkStart && (0 > end || end >= chunkEnd)) {
            return new long[0]; // whole chunk requested
        }
        /*
         * transform to relative range
         */
        long[] range = new long[2];
        range[0] = 0 >= start || start < chunkStart ? 0 : start - chunkStart;
        range[1] = 0 > end || end > chunkEnd ? chunkEnd - chunkStart : end - chunkStart;
        return range;
    }

}

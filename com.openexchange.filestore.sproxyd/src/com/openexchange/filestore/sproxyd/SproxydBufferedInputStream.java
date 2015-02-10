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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.filestore.sproxyd;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.sproxyd.chunkstorage.Chunk;
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
    private final int chunksSize;
    private final SproxydClient client;
    private InputStream current;
    private int pos;
    private long start;
    private long end;

    /**
     * Initializes a new {@link SproxydBufferedInputStream}.
     *
     * @param documentChunks The document's chunks
     * @param client The client to use
     */
    public SproxydBufferedInputStream(List<Chunk> documentChunks, SproxydClient client) {
        this(documentChunks, documentChunks.size(), client);
    }

    /**
     * Initializes a new {@link SproxydBufferedInputStream}.
     *
     * @param documentChunks The document's chunks
     * @param chunksSize The number of chunks
     * @param client The client to use
     */
    public SproxydBufferedInputStream(List<Chunk> documentChunks, int chunksSize, SproxydClient client) {
        super();
        this.documentChunks = documentChunks;
        this.chunksSize = chunksSize;
        this.client = client;
        pos = 0;
        start = -1;
        end = -1;
    }

    private InputStream initNext() throws OXException {
        if (pos >= chunksSize) {
            return null;
        }

        // Next chunk
        Chunk chunk = documentChunks.get(pos++);

        // Range specified?
        InputStream in;
        if (start >= 0 && end > 0 && (chunk.getOffset() + chunk.getLength() > end)) {
            in = client.get(chunk.getScalityId(), 0L, end - chunk.getOffset());
        } else {
            in = client.get(chunk.getScalityId());
        }
        if (!(in instanceof BufferedInputStream) && !(in instanceof ByteArrayInputStream)) {
            in = new FastBufferedInputStream(in);
        }
        current = in;
        return in;
    }

    /**
     * Applies the given range to this Sproxyd buffered input stream.
     *
     * @param start The start (inclusive)
     * @param end The end (exclusive)
     * @return This Sproxyd buffered input stream. with range applied
     * @throws IOException If range cannot be applied
     */
    public SproxydBufferedInputStream applyRange(long start, long end) throws IOException {
        try {
            // Find appropriate start chunk
            Chunk startChunk = null;
            int pos = 0;
            for (; null == startChunk && pos < chunksSize; pos++) {
                Chunk c = documentChunks.get(pos);
                if (c.getOffset() <= start && c.getOffset() + c.getLength() > start) {
                    startChunk = c;
                }
            }
            if (null == startChunk) {
                throw new IOException("Start is out of range");
            }

            // Initialize first chunk
            InputStream in;
            if (startChunk.getOffset() + startChunk.getLength() < end || startChunk.getOffset() < start) {
                long off = start - startChunk.getOffset();
                long len = end - start;
                in = client.get(startChunk.getScalityId(), off, off + len);
            } else {
                in = client.get(startChunk.getScalityId());
            }
            if (!(in instanceof BufferedInputStream) && !(in instanceof ByteArrayInputStream)) {
                in = new FastBufferedInputStream(in);
            }
            this.pos = pos;
            current = in;
            this.start = start;
            this.end = end;
            return this;
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
                in = initNext();
                if (null == in) {
                    return -1;
                }
            }

            return in.read();
        } catch (OXException e) {
            throw toIOException(e);
        }
    }

    private IOException toIOException(OXException e) {
        Throwable cause = e.getCause();
        if (cause instanceof IOException) {
            return (IOException) cause;
        }
        return new IOException(e.getMessage(), e);
    }

}

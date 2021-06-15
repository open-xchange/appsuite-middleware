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

package com.openexchange.contact.vcard.impl.internal;

import static com.openexchange.java.Autoboxing.L;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link VCardFileIterator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.org">Tobias Friedrich</a>
 * @since 7.10.6
 */
public class VCardFileIterator implements SearchIterator<ThresholdFileHolder> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VCardFileIterator.class);
    private static final int BUFFER_THRESHOLD = 256 * 1024;
    private static final String BEGIN_VCARD = "BEGIN:VCARD";
    private static final String END_VCARD = "END:VCARD";
    private static final String UID = "UID:";

    private final InputStream inputStream;
    private final List<OXException> warnings;

    private ThresholdFileHolder next;

    /**
     * Initializes a new {@link VCardFileIterator}.
     * 
     * @param inputStream The input stream to read from
     */
    public VCardFileIterator(InputStream inputStream) {
        super();
        this.inputStream = new BufferedInputStream(inputStream, 64 * 1024);
        this.warnings = new ArrayList<OXException>();
    }
    
    @Override
    public boolean hasNext() throws OXException {
        if (null == next) {
            next = readNext();
            return null != next;
        }
        return true;
    }

    @Override
    public ThresholdFileHolder next() throws OXException {
        ThresholdFileHolder next = null != this.next ? this.next : readNext();
        this.next = null;
        return next;
    }

    @Override
    public int size() {
        return -1;
    }

    @Override
    public boolean hasWarnings() {
        return 0 < warnings.size();
    }

    @Override
    public void addWarning(OXException warning) {
        warnings.add(warning);
    }

    @Override
    public OXException[] getWarnings() {
        return warnings.toArray(new OXException[warnings.size()]);
    }

    @Override
    public void close() {
        Streams.close(inputStream);
    }

    private ThresholdFileHolder readNext() throws OXException {
        long start = System.nanoTime();
        ByteArrayOutputStream buffer = null;
        ThresholdFileHolder sink = null;
        boolean finished = false;
        try {
            buffer = new UnsynchronizedByteArrayOutputStream(64 * 1024);
            sink = new ThresholdFileHolder();
            int componentDepth = 0;
            byte[] currentLine;
            while (null != (currentLine = readLine(inputStream))) {
                /*
                 * while not in component, scan for BEGIN:VCARD, otherwise skip
                 */
                String peekedString = peekAsciiString(currentLine, 12);
                if (0 == componentDepth && false == peekedString.regionMatches(true, 0, BEGIN_VCARD, 0, BEGIN_VCARD.length())) {
                    continue;
                }
                /*
                 * consume line & flush to file holder if threshold exceeded
                 */
                buffer.write(currentLine);
                if (buffer.size() > BUFFER_THRESHOLD) {
                    sink.write(buffer.toByteArray());
                    buffer.reset();
                }
                if (peekedString.regionMatches(true, 0, BEGIN_VCARD, 0, BEGIN_VCARD.length())) {
                    /*
                     * enter VCARD component
                     */
                    componentDepth++;
                } else if (peekedString.regionMatches(true, 0, END_VCARD, 0, END_VCARD.length())) {
                    /*
                     * leave VCARD component, return sink if outer component is closed (buffer is flushed to sink in finally-block)
                     */
                    componentDepth--;
                    if (0 == componentDepth) {
                        finished = true;
                        return sink;
                    } else if (0 > componentDepth) {
                        componentDepth = 0; // ignore                          
                    }
                } else if (peekedString.regionMatches(true, 0, UID, 0, UID.length())) {
                    /*
                     * track UID value
                     */
                    if (1 == componentDepth && peekedString.length() < UID.length()) {
                        sink.setName(peekedString.substring(UID.length()).trim());
                    }
                }
            }
        } catch (IOException e) {
            addWarning(VCardExceptionCodes.IO_ERROR.create(e, e.getMessage()));
        } finally {
            /*
             * flush remaining buffer contents to file holder
             */
            Long elapsed = L(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
            if (finished) {
                if (null != sink && null != buffer) {
                    sink.write(buffer.toByteArray());
                    Streams.close(buffer);
                    LOG.trace("Successfully transferred {} bytes from vCard input stream to file holder in {}ms.", L(sink.getCount()), elapsed);
                }
            } else {
                Streams.close(buffer, sink);
                LOG.trace("No valid vCard input stream transferred to file holder, {}ms elapsed.", elapsed);
            }
        }
        return null;
    }

    private static byte[] readLine(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream()) {
            byte buf[] = new byte[1];
            int length = -1;
            while (0 < (length = inputStream.read(buf))) {
                baos.write(buf, 0, length);
                if ('\n' == buf[0]) {
                    return baos.toByteArray();
                }
            }
            if (0 < baos.size()) {
                baos.write((byte) 10); // add final newline
                return baos.toByteArray();
            }
            return null;
        }
    }

    private static String peekAsciiString(byte[] bytes, int maxLength) {
        int length = Math.min(bytes.length, maxLength);
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) (bytes[i] & 0xff));
        }
        return sb.toString();
    }

}

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
            String currentLine;
            while (null != (currentLine = readLine(inputStream, buffer, 12))) {
                /*
                 * flush to file holder if threshold exceeded
                 */
                if (buffer.size() > BUFFER_THRESHOLD) {
                    sink.write(buffer.toByteArray());
                    buffer.reset();
                }
                if (currentLine.regionMatches(true, 0, BEGIN_VCARD, 0, BEGIN_VCARD.length())) {
                    /*
                     * enter VCARD component
                     */
                    componentDepth++;
                } else if (currentLine.regionMatches(true, 0, END_VCARD, 0, END_VCARD.length())) {
                    /*
                     * leave VCARD component, return sink if outer component is closed
                     */
                    componentDepth--;
                    if (0 == componentDepth) {
                        sink.write(buffer.toByteArray());
                        finished = true;
                        return sink;
                    } else if (0 > componentDepth) {
                        componentDepth = 0; // ignore                          
                    }
                } else if (currentLine.regionMatches(true, 0, UID, 0, UID.length())) {
                    /*
                     * track UID value
                     */
                    if (1 == componentDepth && currentLine.length() < UID.length()) {
                        sink.setName(currentLine.substring(UID.length()).trim());
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


    private static String readLine(InputStream inputStream, ByteArrayOutputStream sink, int peekedLineLength) throws IOException {
        try (ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream()) {
            byte buf[] = new byte[1];
            int length = -1;
            while (0 < (length = inputStream.read(buf))) {
                baos.write(buf, 0, length);
                if ('\n' == buf[0]) {
                    byte[] line = baos.toByteArray();
                    sink.write(line);
                    return peekAsciiString(line, peekedLineLength);
                }
            }
            if (0 < baos.size()) {
                baos.write((byte) 10); // add final newline
                byte[] line = baos.toByteArray();
                sink.write(line);
                return peekAsciiString(line, peekedLineLength);
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

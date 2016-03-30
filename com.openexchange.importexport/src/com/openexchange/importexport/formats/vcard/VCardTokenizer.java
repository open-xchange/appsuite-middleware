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

package com.openexchange.importexport.formats.vcard;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * This mighty class splits a given VCard file into several chunks/tokens (from each BEGIN to END), returns them, plus the VersitDefinition,
 * which basically is an information of which version they are and what parser would be recommended. State: Good enough.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class VCardTokenizer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VCardTokenizer.class);

    public static final String VCARD_V3 = "3.0";

    public static final String VCARD_V21 = "2.1";

    public static final String VCALENDAR = "1.0";

    public static final String ICALENDAR = "2.0";

    public static final String ASCII_ENC = "ASCII";

    private final InputStream vcard;

    private int entriesFound;

    private int entriesRecognized;

    private ByteArrayOutputStream streamAsBytes;

    private boolean streamEnded;

    /**
     * Creates a new instance that reads the content to be parsed from a reader.
     *
     * @param reader: Reader of the content of a VCard file. Reader will be closed.
     * @throws IOException
     */
    public VCardTokenizer(final InputStream is) throws IOException {
        streamAsBytes = new UnsynchronizedByteArrayOutputStream();
        vcard = new BufferedInputStream(is, 65536);
    }

    /**
     * TODO throw exception if no tokens can be found.
     */
    public List<VCardFileToken> split() {
        final List<VCardFileToken> chunks = new LinkedList<VCardFileToken>();

        VCardFileToken currentChunk = new VCardFileToken();
        boolean potentialCalendar = false;
        boolean potentialCard = false;
        String currLine;
        try {
            while ((currLine = readLine()) != null) {
                final String compLine = currLine.trim().toUpperCase();

                if (compLine.startsWith("VERSION")) {
                    if (potentialCard && currLine.trim().endsWith(VCARD_V3)) {
                        currentChunk.setVersitDefinition(VCARD_V3);
                    } else if (potentialCard && compLine.endsWith(VCARD_V21)) {
                        currentChunk.setVersitDefinition(VCARD_V21);
                    } else if (potentialCalendar && compLine.endsWith(VCALENDAR)) {
                        currentChunk.setVersitDefinition(VCALENDAR);
                    } else if (potentialCalendar && compLine.endsWith(ICALENDAR)) {
                        currentChunk.setVersitDefinition(ICALENDAR);
                    }
                } else if (compLine.startsWith("BEGIN") && compLine.endsWith("VCALENDAR")) {
                    potentialCalendar = true;
                } else if (compLine.startsWith("BEGIN") && compLine.endsWith("VCARD")) {
                    potentialCard = true;
                } else if (compLine.startsWith("END") && (compLine.endsWith("VCARD") || compLine.endsWith("VCALENDAR"))) {
                    currentChunk.setContent(streamAsBytes.toByteArray());
                    streamAsBytes = new UnsynchronizedByteArrayOutputStream();
                    chunks.add(currentChunk);
                    entriesFound++;
                    potentialCalendar = false;
                    potentialCard = false;
                    if (currentChunk.getVersitDefinition() != null) {
                        entriesRecognized++;
                    }
                    currentChunk = new VCardFileToken();
                }
            }
        } catch (final IOException e) {
            LOG.error("I/O error while trying to tokenize stream that was a vCard (supposedly)", e);
            Streams.close(vcard);
        }
        return chunks;
    }

    public int getNumberOfEntriesFound() {
        return entriesFound;
    }

    public int getNumberOfEntriesRecognized() {
        return entriesRecognized;
    }

    protected String readLine() throws IOException {
        if (streamEnded) {
            return null;
        }
        final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
        final byte buf[] = new byte[1];
        int length = -1;
        while ((length = vcard.read(buf)) > 0) {
            baos.write(buf, 0, length);
            if ('\n' == buf[0]) {
                final byte[] ret = baos.toByteArray();
                streamAsBytes.write(ret);
                return Charsets.toAsciiString(ret);
            }
        }
        streamEnded = true;

        // cleaning up
        if (baos.size() != 0) {
            baos.write((byte) 10); // add final newline
            final byte[] ret = baos.toByteArray();
            streamAsBytes.write(ret);
            return Charsets.toAsciiString(ret);
        }
        return null;
    }

    protected byte[] toByteArray(final List<Byte> list) {
        final byte[] returnValues = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            returnValues[i] = list.get(i).byteValue();
        }
        return returnValues;
    }
}

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

package com.openexchange.tools.versit.filetokenizer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.tools.versit.ICalendar;
import com.openexchange.tools.versit.VCard;
import com.openexchange.tools.versit.old.VCalendar10;
import com.openexchange.tools.versit.old.VCard21;

/**
 * This mighty class splits a given VCard file into several chunks/tokens (from
 * each BEGIN to END), returns them, plus the VersitDefinition, which basically
 * is an information of which version they are and what parser would be
 * recommended.
 * 
 * State: Good enough.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb'
 *         Prinz</a>
 * 
 */
public class VCardTokenizer {
    private static final Log LOG = LogFactory.getLog(VCardTokenizer.class);

    public static final String VCARD_V3 = "3.0";

    public static final String VCARD_V21 = "2.1";

    public static final String VCALENDAR = "1.0";

    public static final String ICALENDAR = "2.0";

    public static final String ASCII_ENC = "ASCII";

    private final InputStream vcard;

    private int entriesFound = 0;

    private int entriesRecognized = 0;

    private List<Byte> streamAsBytes;

    private boolean streamEnded = false;

    /**
     * Creates a new instance that reads the content to be parsed from a reader.
     * 
     * @param reader:
     *            Reader of the content of a VCard file. Reader will be closed.
     * @throws IOException
     */
    public VCardTokenizer(InputStream is) throws IOException {
        streamAsBytes = new LinkedList<Byte>();
        vcard = new BufferedInputStream(is);
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
                String compLine = currLine.trim().toUpperCase();

                if (compLine.startsWith("VERSION")) {
                    if (potentialCard && currLine.trim().endsWith(VCARD_V3)) {
                        currentChunk.setVersitDefinition(VCard.definition);
                    } else if (potentialCard && compLine.endsWith(VCARD_V21)) {
                        currentChunk.setVersitDefinition(VCard21.definition);
                    } else if (potentialCalendar
                        && compLine.endsWith(VCALENDAR)) {
                        currentChunk
                            .setVersitDefinition(VCalendar10.definition);
                    } else if (potentialCalendar
                        && compLine.endsWith(ICALENDAR)) {
                        currentChunk.setVersitDefinition(ICalendar.definition);
                    }
                } else if (compLine.startsWith("BEGIN")
                    && compLine.endsWith("VCALENDAR")) {
                    potentialCalendar = true;
                } else if (compLine.startsWith("BEGIN")
                    && compLine.endsWith("VCARD")) {
                    potentialCard = true;
                } else if (compLine.startsWith("END")
                    && (compLine.endsWith("VCARD") || compLine
                        .endsWith("VCALENDAR"))) {
                    currentChunk.setContent(toByteArray(streamAsBytes));
                    streamAsBytes = new LinkedList<Byte>();
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
        } catch (IOException e) {
            LOG
                .error(
                    "IOException while trying to tokenize stream that was a VCARD (supposedly)",
                    e);
            if (vcard != null) {
                try {
                    vcard.close();
                } catch (IOException e1) {
                    LOG
                        .error(
                            "Tried to close stream of VCARD that was closed already",
                            e);
                }
            }
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
        final List<Byte> bytez = new LinkedList<Byte>();
        final byte buf[] = new byte[1];
        while (vcard.read(buf) != -1) {
            bytez.add(Byte.valueOf(buf[0]));
            if ('\n' == buf[0]) {
                final byte[] ret = toByteArray(bytez);
                streamAsBytes.addAll(bytez);
                return new String(ret, ASCII_ENC);
            }
        }
        streamEnded = true;

        // cleaning up
        if (bytez.size() != 0) {
            bytez.add(Byte.valueOf((byte) 10)); // add final newline
            final byte[] ret = toByteArray(bytez);
            streamAsBytes.addAll(bytez);
            return new String(ret, ASCII_ENC);
        }
        return null;
    }

    protected byte[] toByteArray(List<Byte> list) {
        byte[] returnValues = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            returnValues[i] = list.get(i).byteValue();
        }
        return returnValues;
    }
}

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

package com.openexchange.textxtraction.internal;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;
import com.openexchange.java.Streams;

/**
 * {@link ReadFileFormat}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ReadFileFormat {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReadFileFormat.class);

    private final StringBuilder sb;

    /**
     * Initializes a new {@link ReadFileFormat}.
     */
    public ReadFileFormat() {
        super();
        sb = new StringBuilder(8192);
    }

    /**
     * Expects the input to be a MS document.
     *
     * @param in The input stream
     * @return The extracted text or <code>null</code>
     */
    public String ms2text(final InputStream in) {
        POITextExtractor extractor = null;
        try {
            extractor = ExtractorFactory.createExtractor(in);
            return extractor.getText();
        } catch (Exception e) {
            LOG.debug("", e);
        } finally {
            Streams.close(in, extractor);
        }
        return null;
    }

    /**
     *
     *
     * @param is
     * @return
     * @throws Exception
     */
    public String rtf2text(final InputStream is) throws Exception {
        final DefaultStyledDocument styledDoc = new DefaultStyledDocument();
        new RTFEditorKit().read(is, styledDoc, 0);
        return styledDoc.getText(0, styledDoc.getLength());
    }

    public String ppt2text(final String fileName) throws Exception {
        InputStream in = null;
        try {
            final POIFSReader poifReader = new POIFSReader();
            poifReader.registerListener(new ReadFileFormat.MyPOIFSReaderListener());
            in = new FileInputStream(fileName);
            poifReader.read(in);
            return sb.toString();
        } finally {
            Streams.close(in);
        }
    }

    class MyPOIFSReaderListener implements POIFSReaderListener {

        @SuppressWarnings("synthetic-access")
        @Override
        public void processPOIFSReaderEvent(final POIFSReaderEvent event) {
            final char ch0 = (char) 0;
            final char ch11 = (char) 11;
            try {
                DocumentInputStream dis = null;
                dis = event.getStream();
                final byte btoWrite[] = new byte[dis.available()];
                dis.read(btoWrite, 0, dis.available());
                for (int i = 0; i < btoWrite.length - 20; i++) {
                    final long type = LittleEndian.getUShort(btoWrite, i + 2);
                    final long size = LittleEndian.getUInt(btoWrite, i + 4);
                    if (type == 4008) {
                        try {
                            final String s = new String(btoWrite, i + 4 + 1, (int) size + 3, StandardCharsets.ISO_8859_1).replace(ch0, ' ').replace(ch11, ' ');
                            if (s.trim().startsWith("Click to edit") == false) {
                                sb.append(s);
                            }
                        } catch (Exception ee) {
                            System.out.println("error:" + ee);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("", e);
                return;
            }
        }
    }
}

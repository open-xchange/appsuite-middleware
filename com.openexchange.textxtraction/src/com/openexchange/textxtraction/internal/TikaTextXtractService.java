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

package com.openexchange.textxtraction.internal;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import org.apache.commons.logging.Log;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.xmlbeans.XmlException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.textxtraction.AbstractTextXtractService;
import com.openexchange.textxtraction.DelegateTextXtraction;
import com.openexchange.textxtraction.TextXtractExceptionCodes;

/**
 * {@link TikaTextXtractService} - The text extraction service based on Apache Tika.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TikaTextXtractService extends AbstractTextXtractService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(TikaTextXtractService.class);

    private static final int DEFAULT_BUFSIZE = 8192;

    private static enum DirectType {
        /**
         * The media type(s) belonging to PDF
         */
        PDF,
        /**
         * The media type(s) belonging to Open Office documents
         */
        OPEN_OFFICE,
    }

    private static final String UTF_8 = Charsets.UTF_8_NAME;

    private static final Object PRESENT = new Object();

    private final ConcurrentMap<DelegateTextXtraction, Object> delegatees;

    private final Tika tika;

    private final Map<DirectType, Set<MediaType>> directTypes;

    /**
     * Initializes a new {@link TikaTextXtractService}.
     */
    public TikaTextXtractService() {
        super();
        tika = new Tika();
        delegatees = new ConcurrentHashMap<DelegateTextXtraction, Object>(4);
        // Direct types map
        directTypes = new EnumMap<DirectType, Set<MediaType>>(DirectType.class);
        // PDF media types
        Set<MediaType> set = new HashSet<MediaType>(2);
        set.add(MediaType.application("pdf"));
        directTypes.put(DirectType.PDF, set);
        // OpenOffice documents
        /*-
         * == Documents
         * OpenDocument-Text   .odt    application/vnd.oasis.opendocument.text
         * OpenDocument-Tabellendokument   .ods    application/vnd.oasis.opendocument.spreadsheet
         * OpenDocument-Praesentation   .odp    application/vnd.oasis.opendocument.presentation
         * OpenDocument-Zeichnung  .odg    application/vnd.oasis.opendocument.graphics
         * OpenDocument-Diagramm   .odc    application/vnd.oasis.opendocument.chart
         * OpenDocument-Formel .odf    application/vnd.oasis.opendocument.formula
         * OpenDocument-Bild   .odi    application/vnd.oasis.opendocument.image
         * OpenDocument-Globaldokument .odm    application/vnd.oasis.opendocument.text-master
         * == Template
         * OpenDocument-Textvorlage    .ott    application/vnd.oasis.opendocument.text-template
         * OpenDocument-Tabellenvorlage    .ots    application/vnd.oasis.opendocument.spreadsheet-template
         * OpenDocument-Praesentationsvorlage   .otp    application/vnd.oasis.opendocument.presentation-template
         * OpenDocument-Zeichnungsvorlage  .otg    application/vnd.oasis.opendocument.graphics-template
         */
        set = new HashSet<MediaType>(8);
        set.add(MediaType.application("vnd.oasis.opendocument.text"));
        set.add(MediaType.application("vnd.oasis.opendocument.spreadsheet"));
        set.add(MediaType.application("vnd.oasis.opendocument.presentation"));
        set.add(MediaType.application("vnd.oasis.opendocument.text-master"));
        set.add(MediaType.application("vnd.oasis.opendocument.text-template"));
        set.add(MediaType.application("vnd.oasis.opendocument.spreadsheet-template"));
        set.add(MediaType.application("vnd.oasis.opendocument.presentation-template"));
        directTypes.put(DirectType.OPEN_OFFICE, set);
    }

    /**
     * Adds given delegate.
     * 
     * @param delegateTextXtraction The delegate to add
     * @return <code>true</code> on success; otherwise <code>false</code>
     */
    public boolean addDelegateTextXtraction(final DelegateTextXtraction delegateTextXtraction) {
        return null == delegatees.putIfAbsent(delegateTextXtraction, PRESENT);
    }

    /**
     * Removes given delegate.
     * 
     * @param delegateTextXtraction The delegate to remove
     */
    public void removeDelegateTextXtraction(final DelegateTextXtraction delegateTextXtraction) {
        delegatees.remove(delegateTextXtraction);
    }

    private TikaDocumentHandler newDefaultHandler() throws OXException {
        return new TikaDocumentHandler(UTF_8);
    }

    private TikaDocumentHandler newHandler(final String mimeType) throws OXException {
        return new TikaDocumentHandler(mimeType, UTF_8);
    }

    @Override
    public String extractFrom(final String content, final String optMimeType) throws OXException {
        if (null == content) {
            return null;
        }
        if (null != optMimeType) {
            if (optMimeType.toLowerCase(Locale.ENGLISH).startsWith("text/htm")) {
                final Source source = new Source(content);
                return new Renderer(new Segment(source, 0, source.getEnd())).setMaxLineLength(9999).setIncludeHyperlinkURLs(false).toString();
            }
        }
        return super.extractFrom(content, optMimeType);
    }

    @Override
    public String extractFrom(final InputStream inputStream, final String optMimeType) throws OXException {
        if (null == inputStream) {
            return null;
        }
        if (null != optMimeType) {
            if (optMimeType.toLowerCase(Locale.ENGLISH).startsWith("text/htm")) {
                try {
                    final Source source = new Source(inputStream);
                    return new Renderer(new Segment(source, 0, source.getEnd())).setMaxLineLength(9999).setIncludeHyperlinkURLs(false).toString();
                } catch (final IOException e) {
                    throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
                } finally {
                    Streams.close(inputStream);
                }
            }
        }
        try {
            for (final DelegateTextXtraction delegatee : delegatees.keySet()) {
                final String text = delegatee.extractFrom(inputStream, optMimeType);
                if (null != text) {
                    Streams.close(inputStream);
                    return text;
                }
            }
        } catch (final Exception e) {
            // Ignore
        }
        /*
         * Detect MIME type & continue processing
         */
        try {
            /*
             * The stream is marked and reset to the original position
             */
            final InputStream in = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream, DEFAULT_BUFSIZE);
            /*
             * Check with POI (that automatically checks support by magic bytes)
             */
            {
                final String text = poitotext(in);
                if (null != text) {
                    Streams.close(in);
                    return text;
                }
            }
            /*
             * Detect media type
             */
            final MediaType mediaType = isEmpty(optMimeType) ? tika.getDetector().detect(in, new Metadata()) : MediaType.parse(optMimeType);
            /*
             * Check for direct support
             */
            try {
                final Set<MediaType> set = directTypes.get(DirectType.PDF);
                for (final MediaType directType : set) {
                    if (directType.getBaseType().equals(mediaType.getBaseType())) {
                        return pdftotext(in);
                    }
                }
            } catch (final Exception e) {
                // Ignore
            }
            try {
                final Set<MediaType> set = directTypes.get(DirectType.OPEN_OFFICE);
                for (final MediaType directType : set) {
                    if (directType.getBaseType().equals(mediaType.getBaseType())) {
                        return OpenOfficeExtractor.getText(in);
                    }
                }
            } catch (final Exception e) {
                // Ignore
            }
            /*
             * Otherwise handle with almighty Tika
             */
            final TikaDocumentHandler documentHandler = isEmpty(optMimeType) ? newDefaultHandler() : newHandler(optMimeType);
            return documentHandler.getDocumentContent(in);
        } catch (final IOException e) {
            throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Checks the input for POIFS (OLE2) or OOXML (zip) header at start. If either present, plain text is extracted from input.
     * 
     * @param in The input stream
     * @return The extracted text or <code>null</code>
     * @throws IOException If an I/O error occurs
     */
    private String poitotext(final InputStream in) throws IOException {
        if (null == in) {
            return null;
        }
        try {
            if (POIFSFileSystem.hasPOIFSHeader(in)) {
                return ExtractorFactory.createExtractor(new POIFSFileSystem(in)).getText();
            }
            if (POIXMLDocument.hasOOXMLHeader(in)) {
                final NonClosableInputStream ncis = new NonClosableInputStream(in);
                boolean resetMark = true;
                try {
                    ncis.mark(8192);
                    return ExtractorFactory.createExtractor(OPCPackage.open(ncis)).getText();
                } catch (final Exception e) {
                    if (ncis.closed) {
                        // Stream has been closed unexpectedly
                        ncis.reset();
                        resetMark = false;
                    }
                } finally {
                    if (resetMark) {
                        ncis.mark(0);
                    }
                }
            }
            return null;
        } catch (final InvalidFormatException e) {
            LOG.debug(e.getMessage(), e);
        } catch (final OpenXML4JException e) {
            LOG.debug(e.getMessage(), e);
        } catch (final XmlException e) {
            LOG.debug(e.getMessage(), e);
        } catch (final RuntimeException e) {
            LOG.debug(e.getMessage(), e);
        } catch (final Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        return null;
    }

    private String pdftotext(final InputStream in) {
        final PDFParser parser;
        try {
            parser = new PDFParser(in);
        } catch (final Exception e) {
            return null;
        }
        PDDocument pdDoc = null;
        COSDocument cosDoc = null;
        try {
            parser.parse();
            cosDoc = parser.getDocument();
            pdDoc = new PDDocument(cosDoc);
            return new PDFTextStripper().getText(pdDoc);
        } catch (final Exception e) {
            LOG.debug(e.getMessage(), e);
            return null;
        } finally {
            if (pdDoc != null) {
                try {
                    pdDoc.close();
                } catch (final IOException e) {
                    // Ignore
                }
            }
            if (cosDoc != null) {
                try {
                    cosDoc.close();
                } catch (final IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        if (0 == len) {
            return true;
        }
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static final class NonClosableInputStream extends FilterInputStream {

        protected volatile boolean closed;

        protected NonClosableInputStream(final InputStream _inputStream) {
            super(_inputStream);
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }
}

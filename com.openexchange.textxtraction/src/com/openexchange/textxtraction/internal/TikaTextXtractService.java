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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import org.apache.commons.logging.Log;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.tika.Tika;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.xmlbeans.XmlException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.textxtraction.TextXtractService;

/**
 * {@link TikaTextXtractService} - The text extraction service based on Apache Tika.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TikaTextXtractService implements TextXtractService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(TikaTextXtractService.class);

    private static enum DirectType {
        /**
         * The media type(s) belonging to MS Word
         */
        WORD,
    }

    private static final String UTF_8 = Charsets.UTF_8_NAME;

    private final TextXtractService outsideInXtractService;

    private final Tika tika;

    private final Map<DirectType, Set<MediaType>> directTypes;

    /**
     * Initializes a new {@link TikaTextXtractService}.
     */
    public TikaTextXtractService() {
        super();
        tika = new Tika();
        outsideInXtractService = new CleanContentExtractor();
        // Direct types map
        directTypes = new EnumMap<DirectType, Set<MediaType>>(DirectType.class);
        // MS Word media types
        final Set<MediaType> set = new HashSet<MediaType>(2);
        set.add(MediaType.application("msword"));
        set.add(MediaType.application("vnd.ms-word"));
        directTypes.put(DirectType.WORD, set);
    }

    private TikaDocumentHandler newDefaultHandler() throws OXException {
        return new TikaDocumentHandler(UTF_8);
    }

    private TikaDocumentHandler newHandler(final String mimeType) throws OXException {
        return new TikaDocumentHandler(mimeType, UTF_8);
    }

    @Override
    public String extractFromResource(final String resource, final String optMimeType) throws OXException {
        try {
            final File file = new File(resource);
            if (null != optMimeType) {
                if (optMimeType.toLowerCase(Locale.ENGLISH).startsWith("text/htm")) {
                    InputStream input = null;
                    try {
                        if (file.isFile()) {
                            input = new FileInputStream(file);
                        } else {
                            input = TikaInputStream.get(new URL(resource));
                        }
                        final Source source = new Source(input);
                        return new Renderer(new Segment(source, 0, source.getEnd())).setMaxLineLength(9999).setIncludeHyperlinkURLs(false).toString();
                    } finally {
                        Streams.close(input);
                    }
                }
            }
            try {
                final String text = outsideInXtractService.extractFromResource(resource, optMimeType);
                if (null != text) {
                    return text;
                }
            } catch (final Exception e) {
                // Ignore
            }
            /*
             * Detect MIME type & continue processing
             */
            try {
                /*
                 * Set input stream
                 */
                final InputStream in;
                if (file.isFile()) {
                    in = new BufferedInputStream(new FileInputStream(file));
                } else {
                    in = TikaInputStream.get(new URL(resource), new Metadata());
                }
                /*
                 * Check with POI
                 */
                {
                    final String text = poi2text(in);
                    if (null != text) {
                        Streams.close(in);
                        return text;
                    }
                }
                /*
                 * The stream is marked and reset to the original position
                 */
                final MediaType mediaType;
                if (isEmpty(optMimeType)) {
                    final Detector detector = tika.getDetector();
                    mediaType = detector.detect(in, new Metadata());
                } else {
                    mediaType = MediaType.parse(optMimeType);
                }
                /*
                 * Check for direct support
                 */
                try {
                    final Set<MediaType> set = directTypes.get(DirectType.WORD);
                    for (final MediaType directType : set) {
                        if (directType.getBaseType().equals(mediaType.getBaseType())) {
                            return poi2text(in);
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
        } catch (final MalformedURLException e) {
            throw TextXtractExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String extractFrom(final String content, final String optMimeType) throws OXException {
        if (null != optMimeType) {
            if (optMimeType.toLowerCase(Locale.ENGLISH).startsWith("text/htm")) {
                final InputStream input = null;
                try {
                    final Source source = new Source(content);
                    return new Renderer(new Segment(source, 0, content.length())).setMaxLineLength(9999).setIncludeHyperlinkURLs(false).toString();
                } finally {
                    Streams.close(input);
                }
            }
        }
        try {
            final String text = outsideInXtractService.extractFrom(content, optMimeType);
            if (null != text) {
                return text;
            }
        } catch (final Exception e) {
            // Ignore
        }
        /*
         * Detect MIME type & continue processing
         */
        try {
            final ByteArrayInputStream in = Streams.newByteArrayInputStream(content.getBytes(Charsets.UTF_8));
            /*
             * Check with POI
             */
            {
                final String text = poi2text(in);
                if (null != text) {
                    return text;
                }
            }
            /*
             * The stream is marked and reset to the original position
             */
            final MediaType mediaType;
            if (isEmpty(optMimeType)) {
                final Detector detector = tika.getDetector();
                mediaType = detector.detect(in, new Metadata());
            } else {
                mediaType = MediaType.parse(optMimeType);
            }
            /*
             * Check for direct support
             */
            try {
                final Set<MediaType> set = directTypes.get(DirectType.WORD);
                for (final MediaType directType : set) {
                    if (directType.getBaseType().equals(mediaType.getBaseType())) {
                        return poi2text(in);
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

    @Override
    public String extractFrom(final InputStream inputStream, final String optMimeType) throws OXException {
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
            final String text = outsideInXtractService.extractFrom(inputStream, optMimeType);
            if (null != text) {
                return text;
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
            final InputStream in;
            if (inputStream == null || inputStream.markSupported()) {
                in = inputStream;
            } else {
                in = new BufferedInputStream(inputStream);
            }
            /*
             * Check with POI
             */
            {
                final String text = poi2text(in);
                if (null != text) {
                    Streams.close(in);
                    return text;
                }
            }
            /*
             * Detect media type
             */
            final MediaType mediaType;
            if (isEmpty(optMimeType)) {
                final Detector detector = tika.getDetector();
                mediaType = detector.detect(in, new Metadata());
            } else {
                mediaType = MediaType.parse(optMimeType);
            }
            /*
             * Check for direct support
             */
            try {
                final Set<MediaType> set = directTypes.get(DirectType.WORD);
                for (final MediaType directType : set) {
                    if (directType.getBaseType().equals(mediaType.getBaseType())) {
                        return poi2text(in);
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
    private String poi2text(final InputStream in) throws IOException {
        if (null == in) {
            return null;
        }
        try {
            if (POIFSFileSystem.hasPOIFSHeader(in)) {
                return ExtractorFactory.createExtractor(new POIFSFileSystem(in)).getText();
            }
            if (POIXMLDocument.hasOOXMLHeader(in)) {
                return ExtractorFactory.createExtractor(OPCPackage.open(in)).getText();
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
        }
        return null;
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
}

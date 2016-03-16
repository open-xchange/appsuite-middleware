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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.ContainerExtractor;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.language.ProfilingHandler;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.textxtraction.TextXtractExceptionCodes;

/**
 * {@link TikaDocumentHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TikaDocumentHandler {

    protected static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(TikaDocumentHandler.class);

    protected final Detector detector;

    protected final Parser parser;

    protected final ParseContext context;

    protected final String encoding;

    protected final Metadata metadata;

    protected ContainerExtractor extractor;

    protected MediaType mediaType;

    protected String language;

    /**
     * Initializes a new {@link TikaDocumentHandler}.
     *
     * @param encoding The character encoding (default is UTF-8)
     * @throws OXException Cannot occur
     */
    public TikaDocumentHandler(final String encoding) throws OXException {
        this(null, encoding);
    }

    /**
     * Initializes a new {@link TikaDocumentHandler}.
     *
     * @param mimeType The MIME type; e.g. <code>"application/pdf"</code> or <code>"application/vnd.ms-powerpoint"</code>
     * @param encoding The character encoding (default is UTF-8)
     * @throws OXException If no appropriate parser could be found for specified content type
     */
    public TikaDocumentHandler(final String mimeType, final String encoding) throws OXException {
        super();
        try {
            this.encoding = null == encoding ? "UTF-8" : encoding;
            metadata = new Metadata();
            context = new ParseContext();
            detector = new DefaultDetector();
            parser = new AutoDetectParser(detector);
            context.set(Parser.class, parser);
        } catch (final Exception e) {
            throw TextXtractExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    private static TikaInputStream getTikaInputStream(final InputStream in) {
        if (in instanceof TikaInputStream) {
            return (TikaInputStream) in;
        }
        return TikaInputStream.get(in);
    }

    /**
     * Gets the meta data.
     *
     * @return The meta data
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Gets the document's type & ensures specified {@link InputStream stream} is closed.
     *
     * @param in The document's input stream
     * @return The type
     * @throws OXException If an error occurs
     */
    public String getDocumentType(final InputStream in) throws OXException {
        final InputStream stream = getTikaInputStream(in);
        try {
            DETECT.process(in, null, this);
            return mediaType.toString();
        } finally {
            Streams.close(stream);
        }
    }

    /**
     * Gets the document's language & ensures specified {@link InputStream stream} is closed.
     *
     * @param in The document's input stream
     * @return The language
     * @throws OXException If an error occurs
     */
    public String getDocumentLanguage(final InputStream in) throws OXException {
        final InputStream stream = getTikaInputStream(in);
        try {
            LANGUAGE.process(in, Streams.newByteArrayOutputStream(8192), this);
            return language;
        } finally {
            Streams.close(stream);
        }
    }

    /**
     * Gets the document's content & ensures specified {@link InputStream stream} is closed.
     *
     * @param in The document's input stream
     * @return The content according to output format
     * @throws OXException If an error occurs
     */
    public String getDocumentContent(final InputStream in) throws OXException {
        final InputStream stream = getTikaInputStream(in);
        try {
            final ByteArrayOutputStream bout = Streams.newByteArrayOutputStream(8192);
            TEXT.process(stream, bout, this);
            return bout.toString(encoding);
        } catch (final UnsupportedEncodingException e) {
            throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(stream);
        }
    }

    private static class OutputType {

        /**
         * Initializes a new {@link TikaDocumentHandler.OutputType}.
         */
        protected OutputType() {
            super();
        }

        public void process(final InputStream input, final OutputStream output, final TikaDocumentHandler documentHandler) throws OXException {
            try {
                documentHandler.parser.parse(
                    input,
                    getContentHandler(output, documentHandler),
                    documentHandler.metadata,
                    documentHandler.context);
            } catch (final IOException e) {
                throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } catch (final SAXException e) {
                throw TextXtractExceptionCodes.ERROR.create(e, e.getMessage());
            } catch (final TikaException e) {
                throw TextXtractExceptionCodes.ERROR.create(e, e.getMessage());
            }
        }

        protected ContentHandler getContentHandler(final OutputStream output, final TikaDocumentHandler documentHandler) throws OXException {
            throw new UnsupportedOperationException();
        }

    }

    private static final OutputType TEXT = new OutputType() {

        @Override
        protected ContentHandler getContentHandler(final OutputStream output, final TikaDocumentHandler documentHandler) throws OXException {
            try {
                return new BodyContentHandler(getOutputWriter(output, documentHandler.encoding));
            } catch (final UnsupportedEncodingException e) {
                throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
    };

    private static final OutputType METADATA = new OutputType() {

        @Override
        protected ContentHandler getContentHandler(final OutputStream output, final TikaDocumentHandler documentHandler) {
            return new DefaultHandler();
        }
    };

    private static final OutputType LANGUAGE = new OutputType() {

        @Override
        protected ContentHandler getContentHandler(final OutputStream output, final TikaDocumentHandler documentHandler) {
            return new ProfilingHandler() {

                @Override
                public void endDocument() {
                    documentHandler.language = getLanguage().getLanguage();
                }
            };
        }
    };

    private static final OutputType DETECT = new OutputType() {

        @Override
        public void process(final InputStream stream, final OutputStream output, final TikaDocumentHandler documentHandler) throws OXException {
            try {
                documentHandler.mediaType = documentHandler.detector.detect(stream, documentHandler.metadata);
            } catch (final IOException e) {
                throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
    };

    /**
     * Returns a transformer handler that serializes incoming SAX events to XHTML or HTML (depending the given method) using the given
     * output encoding.
     *
     * @param output The output stream
     * @param method Either "xml" or "html"
     * @param encoding The output encoding, or <code>null</code> for the platform default
     * @return The transformer handler
     * @throws TransformerConfigurationException if the transformer can not be created
     */
    protected static TransformerHandler getTransformerHandler(final OutputStream output, final String method, final String encoding) throws TransformerConfigurationException {
        final SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
        final TransformerHandler handler = factory.newTransformerHandler();
        handler.getTransformer().setOutputProperty(OutputKeys.METHOD, method);
        handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        if (encoding != null) {
            handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, encoding);
        }
        handler.setResult(new StreamResult(output));
        return handler;
    }

    /**
     * Returns a output writer with the given encoding.
     *
     * @param output output stream
     * @param encoding output encoding, or <code>null</code> for the platform default
     * @return output writer
     * @throws UnsupportedEncodingException if the given encoding is not supported
     */
    protected static Writer getOutputWriter(final OutputStream output, final String encoding) throws UnsupportedEncodingException {
        if (encoding != null) {
            return new OutputStreamWriter(output, encoding);
        } else if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            /*
             * Override the default encoding on Mac OS X
             */
            return new OutputStreamWriter(output, "UTF-8");
        } else {
            return new OutputStreamWriter(output);
        }
    }

}

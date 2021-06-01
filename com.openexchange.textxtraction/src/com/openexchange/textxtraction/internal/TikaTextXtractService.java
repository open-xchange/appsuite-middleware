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

package com.openexchange.textxtraction.internal;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.textxtraction.AbstractTextXtractService;
import com.openexchange.textxtraction.DelegateTextXtraction;
import com.openexchange.textxtraction.TextXtractExceptionCodes;
import com.openexchange.xml.util.XMLUtils;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

/**
 * {@link TikaTextXtractService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class TikaTextXtractService extends AbstractTextXtractService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TikaTextXtractService.class);

    private static final Object PRESENT = new Object();

    static final Set<String> PARSERS = ImmutableSet.of(
        "org.apache.tika.parser.html.HtmlParser",
        "org.apache.tika.parser.microsoft.OfficeParser",
        "org.apache.tika.parser.microsoft.ooxml.OOXMLParser",
        "org.apache.tika.parser.odf.OpenDocumentParser",
        "org.apache.tika.parser.pdf.PDFParser",
        "org.apache.tika.parser.rtf.RTFParser",
        "org.apache.tika.parser.txt.TXTParser",
        "org.apache.tika.parser.xml.DcXMLParser");

    private final ConcurrentMap<DelegateTextXtraction, Object> delegatees;

    final Tika tika;

    /**
     * Initializes a new {@link TikaTextXtractService}.
     * @param service
     */
    public TikaTextXtractService() {
        super();
        delegatees = new ConcurrentHashMap<DelegateTextXtraction, Object>(4, 0.9f, 1);

        Document document;
        {
            try {
                StringBuilder xmlBuilder = new StringBuilder(512);
                xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(Strings.getLineSeparator());
                xmlBuilder.append("<properties>").append(Strings.getLineSeparator());
                xmlBuilder.append("  <parsers>").append(Strings.getLineSeparator());
                for (String parser : PARSERS) {
                    xmlBuilder.append("    <parser class=\"").append(parser).append("\"/>").append(Strings.getLineSeparator());
                }
                xmlBuilder.append("  </parsers>").append(Strings.getLineSeparator());
                xmlBuilder.append("</properties>").append(Strings.getLineSeparator());

                DocumentBuilderFactory factory = XMLUtils.safeDbf(DocumentBuilderFactory.newInstance());
                DocumentBuilder builder = factory.newDocumentBuilder();
                document = builder.parse(new InputSource(new StringReader( xmlBuilder.toString())));
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to build Tika config document", e);
            }
        }

        Tika tika;
        try {
            TikaConfig config = new TikaConfig(document);
            tika = new Tika(config);
        } catch (TikaException e) {
            LOG.error("", e);
            tika = null;
        } catch (IOException e) {
            LOG.error("", e);
            tika = null;
        }
        this.tika = tika;
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

    @Override
    public String extractFrom(InputStream inputStream, String optMimeType) throws OXException {
        if (tika == null) {
            throw new IllegalStateException("Tika must not be null. The service has not been initalized correctly.");
        }

        long start = System.currentTimeMillis();
        File tempFile = null;
        FileOutputStream fos = null;
        FileInputStream fis = null;
        String text;
        for (DelegateTextXtraction delegatee : delegatees.keySet()) {
            /*
             * FileInputStreams will not be wrapped with BufferedInputStreams here.
             * The delegates are responsible for efficient InputStream handling.
             */
            try {
                if (tempFile == null) {
                    if (delegatee.isDestructive()) {
                        tempFile = File.createTempFile(Long.toString(start), "ox.tmp");
                        fos = new FileOutputStream(tempFile);
                        IOUtils.copy(inputStream, fos);

                        fis = new FileInputStream(tempFile);
                        text = delegatee.extractFrom(fis, optMimeType);
                    } else {
                        text = delegatee.extractFrom(inputStream, optMimeType);
                    }
                } else {
                    fis = new FileInputStream(tempFile);
                    text = delegatee.extractFrom(fis, optMimeType);
                }
            } catch (IOException e) {
                throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } finally {
                if (tempFile != null) {
                    closeQuietly(inputStream);
                }
                closeQuietly(fos);
                closeQuietly(fis);
            }

            if (null != text) {
                return text;
            }
        }
        /*
         * None of the delegates could extract some text.
         */
        InputStream tikaInputStream = null;
        try {
            if (tempFile == null) {
                tikaInputStream = inputStream;
            } else {
                tikaInputStream = new BufferedInputStream(new FileInputStream(tempFile), 65536);
            }
            return tika.parseToString(tikaInputStream);
        } catch (IOException e) {
            throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (TikaException e) {
            throw TextXtractExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeQuietly(tikaInputStream);
        }
    }

    @Override
    public String extractFrom(String content, String optMimeType) throws OXException {
        if (null == content) {
            return null;
        }
        if (null != optMimeType) {
            if (optMimeType.toLowerCase(Locale.ENGLISH).startsWith("text/htm")) {
                try {
                    Source source = new Source(content);
                    return new Renderer(new Segment(source, 0, source.getEnd())).setMaxLineLength(9999).setIncludeHyperlinkURLs(false).toString();
                } catch (StackOverflowError e) {
                    LOG.warn("StackOverflowError while rendering html content. Returning null.");
                }
            }
        }
        return super.extractFrom(content, optMimeType);
    }
    
    private static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }
}

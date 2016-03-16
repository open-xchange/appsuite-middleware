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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.joox.JOOX;
import org.joox.Match;
import com.openexchange.exception.OXException;
import com.openexchange.textxtraction.AbstractTextXtractService;
import com.openexchange.textxtraction.DelegateTextXtraction;
import com.openexchange.textxtraction.TextXtractExceptionCodes;


/**
 * {@link TikaTextXtractService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class TikaTextXtractService extends AbstractTextXtractService {

    static final Set<String> PARSERS = new HashSet<String>();

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TikaTextXtractService.class);

    private static final Object PRESENT = new Object();

    private final ConcurrentMap<DelegateTextXtraction, Object> delegatees;

    Tika tika = null;

    static {
        PARSERS.add("org.apache.tika.parser.html.HtmlParser");
        PARSERS.add("org.apache.tika.parser.microsoft.OfficeParser");
        PARSERS.add("org.apache.tika.parser.microsoft.ooxml.OOXMLParser");
        PARSERS.add("org.apache.tika.parser.odf.OpenDocumentParser");
        PARSERS.add("org.apache.tika.parser.pdf.PDFParser");
        PARSERS.add("org.apache.tika.parser.rtf.RTFParser");
        PARSERS.add("org.apache.tika.parser.txt.TXTParser");
        PARSERS.add("org.apache.tika.parser.xml.DcXMLParser");
    }


    /**
     * Initializes a new {@link TikaTextXtractService}.
     * @param service
     */
    public TikaTextXtractService() {
        super();
        delegatees = new ConcurrentHashMap<DelegateTextXtraction, Object>(4, 0.9f, 1);

        Match configMatch = JOOX.$("config");
        for (String parser : PARSERS) {
            configMatch.append(JOOX.$("parser").attr("class", parser));
        }

        try {
            TikaConfig config = new TikaConfig(configMatch.document());
            tika = new Tika(config);
        } catch (TikaException e) {
            LOG.error("", e);
        } catch (IOException e) {
            LOG.error("", e);
        }
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
                    IOUtils.closeQuietly(inputStream);
                }
                IOUtils.closeQuietly(fos);
                IOUtils.closeQuietly(fis);
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
            IOUtils.closeQuietly(tikaInputStream);
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

}

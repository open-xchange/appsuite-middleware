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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.langdetect.internal;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.olivo.lc4j.LanguageCategorization;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.langdetect.LanguageDetectionExceptionCodes;
import com.openexchange.langdetect.LanguageDetectionService;

/**
 * {@link Lc4jLanguageDetectionService} - The {@link LanguageDetectionService language detection service} based on <b>lc4j</b>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Lc4jLanguageDetectionService implements LanguageDetectionService {

    /**
     * The singleton instance.
     */
    private static final Lc4jLanguageDetectionService INSTANCE = new Lc4jLanguageDetectionService();

    /**
     * Gets the instance
     * 
     * @return The instance
     */
    public static Lc4jLanguageDetectionService getInstance() {
        return INSTANCE;
    }

    private static final int BUFFER_SIZE = 2048;

    private final LanguageCategorization defaultLanguageCategorization;

    private final AtomicReference<String> languageModelsDir;

    /**
     * Initializes a new {@link Lc4jLanguageDetectionService}.
     */
    private Lc4jLanguageDetectionService() {
        super();
        languageModelsDir = new AtomicReference<String>();
        defaultLanguageCategorization = new LanguageCategorization();
        defaultLanguageCategorization.setMaxLanguages(10);
        defaultLanguageCategorization.setNumCharsToExamine(1000);
        defaultLanguageCategorization.setUseTopmostNgrams(400);
        defaultLanguageCategorization.setUnknownThreshold(1.01f);
    }

    /**
     * Sets the directory path containing the language models.
     * 
     * @param languageModelsDir The directory path
     */
    public void setLanguageModelsDir(final String languageModelsDir) {
        this.languageModelsDir.set(languageModelsDir);
        defaultLanguageCategorization.setLanguageModelsDir(languageModelsDir);
        // Initialize
        defaultLanguageCategorization.findLanguage(new ByteArrayList("Hello world!".getBytes()));
    }

    @Override
    public List<String> findLanguages(final InputStream inputStream) throws OXException {
        // Read from stream
        final ByteArrayOutputStream tmp = Streams.newByteArrayOutputStream(BUFFER_SIZE << 1);
        try {
            final byte[] b = new byte[BUFFER_SIZE];
            for (int read; (read = inputStream.read(b, 0, BUFFER_SIZE)) > 0;) {
                tmp.write(b, 0, read);
            }
            // No flush for ByteArrayOutputStream
        } catch (final IOException e) {
            throw LanguageDetectionExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(inputStream);
        }
        return defaultLanguageCategorization.findLanguage(new ByteArrayList(tmp.toByteArray()));
    }

}

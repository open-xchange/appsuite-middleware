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

package com.openexchange.langdetect.internal;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import net.olivo.lc4j.LanguageCategorization;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.langdetect.LanguageDetectionExceptionCodes;
import com.openexchange.langdetect.LanguageDetectionService;

/**
 * {@link Lc4jLanguageDetectionService} - The {@link LanguageDetectionService language detection service} based on <a
 * href="http://olivo.net/software/lc4j/">lc4j</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Lc4jLanguageDetectionService implements LanguageDetectionService {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(Lc4jLanguageDetectionService.class));

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

    private final AtomicReference<File> languageModelsDir;

    private final ConcurrentMap<String, Locale> languageCodes;

    private final Locale defaultLocale;

	private boolean warnAboutUnknownModel;

    /**
     * Initializes a new {@link Lc4jLanguageDetectionService}.
     */
    private Lc4jLanguageDetectionService() {
        super();
        warnAboutUnknownModel = false;
        defaultLocale = DEFAULT_LOCALE;
        languageCodes = new ConcurrentHashMap<String, Locale>(64);
        languageModelsDir = new AtomicReference<File>();
        defaultLanguageCategorization = new LanguageCategorization();
        defaultLanguageCategorization.setMaxLanguages(10);
        defaultLanguageCategorization.setNumCharsToExamine(1000);
        defaultLanguageCategorization.setUseTopmostNgrams(400);
        defaultLanguageCategorization.setUnknownThreshold(1.01f);
    }

    /**
     * Sets whether to output a warning if an unknown model is detected.
     *
     * @param warnAboutUnknownModel <code>true</code> to warn if an unknown model is detected; otherwise <code>false</code>
     */
    public void setWarnAboutUnknownModel(final boolean warnAboutUnknownModel) {
        this.warnAboutUnknownModel = warnAboutUnknownModel;
    }

    /**
     * Loads specified language code file.
     *
     * @param languageCodesFile The file name
     * @throws OXException If loading file fails
     */
    public void loadLanguageCodes(final File languageCodesFile) throws OXException {
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(languageCodesFile));
            final Properties properties = new Properties();
            properties.load(inputStream);
            languageCodes.clear();
            for (final Entry<Object, Object> entry : properties.entrySet()) {
                languageCodes.put(
                    entry.getKey().toString().toLowerCase(defaultLocale),
                    new Locale(entry.getValue().toString().toLowerCase(defaultLocale)));
            }
        } catch (final IOException e) {
            throw LanguageDetectionExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(inputStream);
        }
    }

    /**
     * Sets the directory path containing the language models.
     *
     * @param languageModelsDir The directory path
     */
    public void setLanguageModelsDir(final File languageModelsDir) {
        this.languageModelsDir.set(languageModelsDir);
        defaultLanguageCategorization.setLanguageModelsDir(languageModelsDir.getPath());
        // Initialize
        defaultLanguageCategorization.findLanguage(new ByteArrayList("Hello world!".getBytes()));
    }

    @Override
    public List<Locale> findLanguages(final InputStream inputStream) throws OXException {
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
        final List<String> languages = defaultLanguageCategorization.findLanguage(new ByteArrayList(tmp.toByteArray()));
        final List<Locale> locales = new ArrayList<Locale>(languages.size());
        for (final String language : languages) {
            int pos = language.indexOf('.');
            final String lang = (pos > 0 ? language.substring(0, pos) : language).toLowerCase(defaultLocale);
            pos = lang.indexOf('-');
            Locale locale = languageCodes.get(pos < 0 ? lang : lang.substring(0, pos));
            if (null == locale) {
                if (warnAboutUnknownModel) {
					LOG.warn("No language code for model: " + language
							+ ". Using default " + defaultLocale);
				}
				locale = defaultLocale;
            }
            locales.add(locale);
        }
        return locales;
    }

    @Override
    public List<Locale> findLanguages(final Reader reader) throws OXException {
        // Read from stream
        final StringBuilder tmp = new StringBuilder(BUFFER_SIZE << 1);
        try {
            final BufferedReader br = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, BUFFER_SIZE);
            for (String line; (line = br.readLine()) != null;) {
                tmp.append(line).append('\n');
            }
        } catch (final IOException e) {
            throw LanguageDetectionExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader);
        }
        return findLanguages(tmp.toString());
    }

    private static final String UNKNOWN = "UNKNOWN";

    @Override
    public List<Locale> findLanguages(final String input) throws OXException {
        try {
            final List<String> languages = defaultLanguageCategorization.findLanguage(new ByteArrayList(input.getBytes(Charsets.UTF_8)));
            final Set<Locale> locales = new LinkedHashSet<Locale>(languages.size());
            for (final String language : languages) {
                if (UNKNOWN.equals(language)) {
                    locales.add(defaultLocale);
                } else {
                    int pos = language.indexOf('.');
                    String lang = (pos > 0 ? language.substring(0, pos) : language).toLowerCase(defaultLocale);
                    pos = lang.indexOf('-');
                    lang = (pos > 0 ? lang.substring(0, pos) : lang);
                    pos = lang.indexOf('_');
                    lang = (pos > 0 ? lang.substring(0, pos) : lang);
                    Locale locale = languageCodes.get(lang);
                    if (null == locale) {
                        if (warnAboutUnknownModel) {
                            LOG.warn("No language code for model: " + language + ". Using default \"" + defaultLocale + '"');
                        }
                        locale = defaultLocale;
                    }
                    locales.add(locale);
                }
            }
            return new ArrayList<Locale>(locales);
        } catch (final UnsupportedCharsetException e) {
            // Cannot occur
            throw LanguageDetectionExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            // Cannot occur
            throw LanguageDetectionExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }
}

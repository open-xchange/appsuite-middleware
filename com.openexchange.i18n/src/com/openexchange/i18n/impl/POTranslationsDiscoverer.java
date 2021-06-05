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

package com.openexchange.i18n.impl;

import static com.openexchange.java.Autoboxing.L;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.parsing.POParser;
import com.openexchange.i18n.parsing.Translations;
import com.openexchange.java.Streams;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class POTranslationsDiscoverer extends FileDiscoverer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(POTranslationsDiscoverer.class);

    /**
     * Initializes a new {@link POTranslationsDiscoverer}.
     *
     * @param dir The directory
     * @throws FileNotFoundException If directory could not be found
     */
    public POTranslationsDiscoverer(final File dir) throws FileNotFoundException {
        super(dir);
    }

    /**
     * Gets the translations available by <code>.po</code> files.
     *
     * @return The translations
     */
    public List<Translations> getTranslations() {
        final String[] files = getFilesFromLanguageFolder(".po");
        if (files.length == 0) {
            LOG.info("No .po files found in directory \"{}\"", getDirectory());
            return Collections.emptyList();
        }
        final List<Translations> list = new ArrayList<Translations>(files.length);
        final File directory = getDirectory();
        for (final String file : files) {
            InputStream input = null;
            try {
                final Locale l = getLocale(file);
                if (null == l) {
                    LOG.warn(".po file does not match name pattern: {}", file);
                } else {
                    final File poFile = new File(directory, file);
                    input = new BufferedInputStream(new FileInputStream(poFile), 65536);
                    // POParser remembers headers of PO file. Therefore a new one is needed for every file.
                    long start = System.currentTimeMillis();
                    final Translations translations = new POParser().parse(input, poFile.getAbsolutePath());
                    LOG.trace("Parsing translations for locale {} took {}ms.", l, L(System.currentTimeMillis() - start));
                    translations.setLocale(l);
                    list.add(translations);
                    LOG.info("Parsed .po file \"{}\" for locale: {}", file, l);
                }
            } catch (FileNotFoundException e) {
                LOG.error("File disappeared?", e);
            } catch (OXException e) {
                LOG.error("Could not parse po file: ", e);
            } finally {
                Streams.close(input);
            }
        }
        return list;
    }
}

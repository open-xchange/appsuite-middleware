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
            } catch (final FileNotFoundException e) {
                LOG.error("File disappeared?", e);
            } catch (final OXException e) {
                LOG.error("Could not parse po file: ", e);
            } finally {
                Streams.close(input);
            }
        }
        return list;
    }
}

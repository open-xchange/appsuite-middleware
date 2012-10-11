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

package com.openexchange.mail.smal.impl.adapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.langdetect.LanguageDetectionService;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link IndexAdapters} - A utility class for index adapters.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexAdapters {

    private static final Locale DEFAULT_LOCALE = LanguageDetectionService.DEFAULT_LOCALE;

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(IndexAdapters.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link IndexAdapters}.
     */
    private IndexAdapters() {
        super();
    }

    /**
     * Checks if specified string is empty.
     *
     * @param str The string to check
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public static boolean isEmpty(final String str) {
        if (null == str) {
            return true;
        }
        final int len = str.length();
        boolean empty = true;
        for (int i = 0; empty && i < len; i++) {
            empty = Character.isWhitespace(str.charAt(i));
        }
        return empty;
    }

    /**
     * Currently known languages.
     */
    public static final Set<Locale> KNOWN_LOCALES;

    static {
        final Set<Locale> set = new HashSet<Locale>(10);
        set.add(new Locale("en"));
        set.add(new Locale("de"));
        // set.add(new Locale("fr"));
        // set.add(new Locale("nl"));
        // set.add(new Locale("sv"));
        // set.add(new Locale("es"));
        // set.add(new Locale("ja"));
        // set.add(new Locale("pl"));
        // set.add(new Locale("it"));
        // set.add(new Locale("zh"));
        // set.add(new Locale("hu"));
        // set.add(new Locale("sk"));
        // set.add(new Locale("cs"));
        // set.add(new Locale("lv"));
        KNOWN_LOCALES = Collections.unmodifiableSet(set);
    }

    /**
     * Detects the locale.
     *
     * @param str The string source
     * @return The detected locale
     * @throws OXException If language detection fails
     */
    public static Locale detectLocale(final String str) throws OXException {
        try {
            final LanguageDetectionService detectionService = SmalServiceLookup.getServiceStatic(LanguageDetectionService.class);
            if (null == detectionService) {
                LOG.warn("Missing language detection service. Using fall-back locale \"" + DEFAULT_LOCALE + "\".");
                return DEFAULT_LOCALE;
            }
            final Locale locale = detectionService.findLanguages(str).get(0);
            if (KNOWN_LOCALES.contains(locale)) {
                return locale;
            }
            if (DEBUG) {
                LOG.debug("Detected locale \"" + locale + "\" is not supported. Using fall-back locale \"" + DEFAULT_LOCALE + "\".");
            }
            return DEFAULT_LOCALE;
        } catch (final IllegalStateException e) {
            // Missing service
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(e, LanguageDetectionService.class.getName());
        }
    }
}

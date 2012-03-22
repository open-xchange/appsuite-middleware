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

package com.openexchange.index.solr.internal;

import java.util.Locale;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexConstants;
import com.openexchange.langdetect.LanguageDetectionService;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link SolrUtils} - Utility methods for Solr.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SolrUtils {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(SolrUtils.class);
    
    private static final Locale DEFAULT_LOCALE = LanguageDetectionService.DEFAULT_LOCALE;

    private static final Set<Locale> KNOWN_LOCALES = IndexConstants.KNOWN_LOCALES;

    /**
     * Initializes a new {@link SolrUtils}.
     */
    private SolrUtils() {
        super();
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
            final LanguageDetectionService detectionService = Services.getService(LanguageDetectionService.class);
            if (null == detectionService) {
                LOG.warn("Missing language detection service. Using fall-back locale \"" + DEFAULT_LOCALE + "\".");
                return DEFAULT_LOCALE;
            }
            final Locale locale = detectionService.findLanguages(str).get(0);
            if (KNOWN_LOCALES.contains(locale)) {
                return locale;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Detected locale \"" + locale + "\" is not supported. Using fall-back locale \"" + DEFAULT_LOCALE + "\".");
            }
            return DEFAULT_LOCALE;
        } catch (final IllegalStateException e) {
            // Missing service
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(e, LanguageDetectionService.class.getName());
        }
    }

    private static final String MARKER = " ---=== /!\\ ===--- ";

    /**
     * Checks whether the supplied <tt>Throwable</tt> is one that needs to be rethrown and swallows all others.
     *
     * @param t The <tt>Throwable</tt> to check
     */
    public static void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            LOG.fatal(MARKER + "Thread death" + MARKER, t);
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            LOG.fatal(
                MARKER + "The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating." + MARKER,
                t);
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

}

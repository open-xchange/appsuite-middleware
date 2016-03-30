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

package com.openexchange.i18n;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tool methods for handling locales.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LocaleTools {

    /**
     * The default locale: <code>en_US</code>
     */
    public static final Locale DEFAULT_LOCALE = Locale.US;

    private static final String STR_EMPTY = "";

    private static final Pattern identifierPattern = Pattern.compile("(\\p{Lower}{2})(?:[_-]([a-zA-Z]{2}))?(?:[_-]([a-zA-Z]{2}))?");

    /**
     * Prevent instantiation
     */
    private LocaleTools() {
        super();
    }

    /**
     * Gets the sane (non-<code>null</code>) locale for specified locale.
     *
     * @param locale The locale to check for <code>null</code>
     * @return The passed locale or <tt>en_US</tt> if passed locale is <code>null</code>
     */
    public static Locale getSaneLocale(final Locale locale) {
        return locale == null ? DEFAULT_LOCALE : locale;
    }

    /**
     * Splits the full locale identifier into its parts and creates the corresponding locale. Currently the fullIdentifier must match the
     * pattern <code>&lt;language&gt; + &quot;_&quot; + &lt;country&gt; + &quot;_&quot; + &lt;variant&gt;</code>.
     *
     * @param fullIdentifier full locale identifier compliant to RFC 2798 and 2068.
     * @return the locale or <code>null</code> if the pattern doesn't match.
     */
    public static Locale getLocale(final String fullIdentifier) {
        if (null == fullIdentifier) {
            return null;
        }
        final Matcher match = identifierPattern.matcher(fullIdentifier);
        Locale retval = null;
        if (match.matches()) {
            final String country = match.group(2);
            final String variant = match.group(3);
            retval = new Locale(toLowerCase(match.group(1)), country == null ? STR_EMPTY : toUpperCase(country), variant == null ? STR_EMPTY : variant);
        }
        return retval;
    }

    /**
     * An own implementation of toLowerCase() to avoid circularity problems between Locale and String. The most straightforward algorithm is
     * used. Look at optimizations later.
     */
    public static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

    /**
     * An own implementation of toUpperCase() to avoid circularity problems between Locale and String. The most straightforward algorithm is
     * used. Look at optimizations later.
     */
    public static String toUpperCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
        }
        return builder.toString();
    }

}

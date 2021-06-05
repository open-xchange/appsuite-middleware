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
     * @return The locale or <code>null</code> if the pattern doesn't match.
     */
    public static Locale getLocale(final String fullIdentifier) {
        if (null == fullIdentifier) {
            return null;
        }

        Matcher match = identifierPattern.matcher(fullIdentifier);
        if (!match.matches()) {
            return null;
        }

        String country = match.group(2);
        String variant = match.group(3);
        return new Locale(toLowerCase(match.group(1)), country == null ? STR_EMPTY : toUpperCase(country), variant == null ? STR_EMPTY : variant);
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

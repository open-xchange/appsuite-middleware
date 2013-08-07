/*
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers.  Instead of sorting numbers in ASCII order like
 * a standard sort, this algorithm sorts numbers in numeric order.
 *
 * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.davekoelle;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.java.StringAllocator;

/**
 * This is an updated version with enhancements made by Daniel Migowski,
 * Andre Bogus, and David Koelle.
 *
 * To convert to use Templates (Java 1.5+):
 *   - Change "implements Comparator" to "implements Comparator<String>"
 *   - Change "compare(Object o1, Object o2)" to "compare(String s1, String s2)"
 *   - Remove the type checking and casting in compare().
 *
 * To use this class:
 *   Use the static "sort" method from the java.util.Collections class:
 *   Collections.sort(your list, new AlphanumComparator());
 *
 * Improvements by Marcus Klein:
 *   - Converted to Java 1.5.
 *   - Using Character.isDigit(). Hopefully all digits of other languages have same sorting like ISO-LATIN-1 digits.
 *   - Shrunk getChunk() to a single for loop.
 */
public class AlphanumComparator implements Comparator<String> {

    private static final Log LOG = LogFactory.getLog(AlphanumComparator.class);

    private static final Map<Locale, Collator> COLLATOR_OVERRIDES;

    private static final Locale DEFAULT_LOCALE = Locale.US;

    private static final Collator DEFAULT_COLLATOR;

    static {
        final Collator collator = Collator.getInstance(DEFAULT_LOCALE);
        collator.setStrength(Collator.SECONDARY);
        DEFAULT_COLLATOR = collator;
        COLLATOR_OVERRIDES = Collections.unmodifiableMap(initCollatorOverrides());
    }

    private Collator collator;

    private final Locale locale;

    /**
     * Default constructor with default location US.
     */
    public AlphanumComparator() {
        this(null);
    }

    /**
     * Initializes a new {@link AlphanumComparator}.
     *
     * @param locale The locale
     */
    public AlphanumComparator(final Locale locale) {
        super();
        if (null == locale) {
            collator = DEFAULT_COLLATOR;
            this.locale = DEFAULT_LOCALE;
        } else if (COLLATOR_OVERRIDES.containsKey(locale)) {
            this.locale = locale;
            this.collator = COLLATOR_OVERRIDES.get(locale);
        } else {
            this.locale = locale;
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }
    }

    /**
     * Length of string is passed in for improved efficiency (only need to calculate it once).
     */
    private final String getChunk(final String s, final int length, final int start) {
        char c = s.charAt(start);
        final boolean digit = Character.isDigit(c);
        final com.openexchange.java.StringAllocator chunk = new com.openexchange.java.StringAllocator(length - start);
        chunk.append(c);
        for (int marker = start + 1; marker < length && digit == Character.isDigit(c = s.charAt(marker)); marker++) {
            chunk.append(c);
        }
        return chunk.toString();
    }

    @Override
    public int compare(final String s1, final String s2) {
        int thisMarker = 0;
        int thatMarker = 0;
        final int s1Length = s1.length();
        final int s2Length = s2.length();
        while (thisMarker < s1Length && thatMarker < s2Length) {
            final String thisChunk = getChunk(s1, s1Length, thisMarker);
            thisMarker += thisChunk.length();
            final String thatChunk = getChunk(s2, s2Length, thatMarker);
            thatMarker += thatChunk.length();
            // If both chunks contain numeric characters, sort them numerically
            int result;
            if (Character.isDigit(thisChunk.charAt(0)) && Character.isDigit(thatChunk.charAt(0))) {
                // Simple chunk comparison by length.
                final int thisChunkLength = thisChunk.length();
                result = thisChunkLength - thatChunk.length();
                // If equal, the first different number counts
                if (result == 0) {
                    for (int i = 0; i < thisChunkLength; i++) {
                        result = thisChunk.charAt(i) - thatChunk.charAt(i);
                        if (result != 0) {
                            return result;
                        }
                    }
                }
            } else {
                result = collator.compare(thisChunk.toLowerCase(locale), thatChunk.toLowerCase(locale));
            }
            if (result != 0) {
                return result;
            }
        }
        return s1Length - s2Length;
    }

    private static Map<Locale, Collator> initCollatorOverrides() {
        Map<Locale, Collator> overrides = new HashMap<Locale, Collator>();
        Collator defaultJapaneseCollator = Collator.getInstance(Locale.JAPANESE);
        if (null != defaultJapaneseCollator && RuleBasedCollator.class.isInstance(defaultJapaneseCollator)) {
            StringAllocator customRules = new StringAllocator(((RuleBasedCollator)defaultJapaneseCollator).getRules());
            /*
             * insert custom rules after the 0 character to sort those characters before any others
             */
            customRules.append("& \u0000");
            /*
             * Hiragana and Katakana characters first...
             */
            for (int c = 0x3040; c <= 0x30ff; c++) {
                customRules.append('<').append((char)c);
            }
            /*
             * ... and then Kanji
             */
            for (int c = 0x4e00; c <= 0x9fbf; c++) {
                customRules.append('<').append((char)c);
            }
            /*
             * Initialize collator and add to overrides
             */
            RuleBasedCollator customJapaneseCollator = null;
            try {
                customJapaneseCollator = new RuleBasedCollator(customRules.toString());
            } catch (ParseException e) {
                LOG.warn("Error initializing custom collator", e);
            }
            if (null != customJapaneseCollator) {
                customJapaneseCollator.setStrength(Collator.SECONDARY);
                overrides.put(Locale.JAPAN, customJapaneseCollator);
                overrides.put(Locale.JAPANESE, customJapaneseCollator);
            }
        }
        return overrides;
    }

}

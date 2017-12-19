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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.mail.authenticity.impl.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.mail.authenticity.MailAuthenticityAttribute;

/**
 * {@link StringUtil} - Helper methods for parsing String values
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@SuppressWarnings("unchecked")
final class StringUtil {

    private static interface CollectorAdder {

        /**
         * Adds the specified key/value to the specified {@link T} collector
         *
         * @param key The key
         * @param value The value
         * @param collector The {@link T} collector
         */
        <T> void add(String key, String value, T collector);
    }

    private static final Map<Class<?>, CollectorAdder> collectorAdders = new HashMap<>();
    static {
        collectorAdders.put(HashMap.class, new CollectorAdder() {

            @Override
            public <T> void add(String key, String value, T collector) {
                Map<String, String> m = (Map<String, String>) collector;
                m.put(key, value);
            }
        });

        collectorAdders.put(ArrayList.class, new CollectorAdder() {

            @Override
            public <T> void add(String key, String value, T collector) {
                List<MailAuthenticityAttribute> l = (List<MailAuthenticityAttribute>) collector;
                l.add(new MailAuthenticityAttribute(key, value));
            }
        });
    }

    /**
     * Parses the specified element as a key/value {@link Map}
     *
     * @param element The element to parse
     * @return A {@link Map} with the key/value attributes of the element
     */
    static Map<String, String> parseMap(String element) {
        Map<String, String> mapCollector = new HashMap<>();
        return parseToCollector(element, mapCollector);
    }

    /**
     * Parses the specified element as a {@link List} of {@link MailAuthenticityAttribute}s
     *
     * @param element The element to parse
     * @return a {@link List} with the {@link MailAuthenticityAttribute}s
     */
    static List<MailAuthenticityAttribute> parseList(String element) {
        List<MailAuthenticityAttribute> listCollector = new ArrayList<>();
        return parseToCollector(element, listCollector);
    }

    /**
     * Parses the attributes (key value pairs separated by an equals '=' sign) of the specified element to the specified {@link T} collector.
     *
     * @param element The element to parse
     * @return A {@link T} with the key/value attributes of the element
     */
    private static <T> T parseToCollector(String element, T collector) {
        // No pairs; return as a singleton collector with the line being both the key and the value
        if (!element.contains("=")) {
            add(element, element, collector);
            return collector;
        }

        StringBuilder keyBuffer = new StringBuilder(32);
        StringBuilder valueBuffer = new StringBuilder(64);
        String key = null;
        boolean valueMode = false;
        boolean backtracking = false;
        boolean comment = false;
        int backtrackIndex = 0;
        for (int index = 0; index < element.length();) {
            char c = element.charAt(index);
            switch (c) {
                case '=':
                    if (valueMode) {
                        if (comment) {
                            valueBuffer.append(c);
                            index++;
                            break;
                        }
                        // A key found while in value mode, so we backtrack
                        backtracking = true;
                        valueMode = false;
                        index--;
                    } else {
                        // Retain the key and switch to value mode
                        key = keyBuffer.toString();
                        keyBuffer.setLength(0);
                        valueMode = true;
                        index++;
                    }
                    break;
                case '(':
                    if (valueMode) {
                        comment = true;
                        valueBuffer.append(c);
                        index++;
                    }
                    break;
                case ')':
                    if (valueMode) {
                        comment = false;
                        valueBuffer.append(c);
                        index++;
                    }
                    break;
                case ' ':
                    if (!valueMode) {
                        //Remove the key from the value buffer
                        valueBuffer.setLength(valueBuffer.length() - backtrackIndex);
                        add(key, valueBuffer.toString().trim(), collector);
                        // Retain the new key (and reverse if that key came from backtracking)
                        key = backtracking ? keyBuffer.reverse().toString() : keyBuffer.toString();
                        // Reset counters
                        keyBuffer.setLength(0);
                        valueBuffer.setLength(0);
                        // Skip to the value of the retained new key (position after the '=' sign)
                        index += backtrackIndex + 2;
                        backtrackIndex = 0;
                        backtracking = false;
                        valueMode = true;
                        break;
                    }
                    // while in value mode spaces are considered as literals, hence fall-through to 'default'
                default:
                    if (valueMode) {
                        // While in value mode append all literals to the value buffer
                        valueBuffer.append(c);
                        index++;
                    } else {
                        // While in key mode append all key literals to the key buffer...
                        keyBuffer.append(c);
                        if (backtracking) {
                            // ... and if we are backtracking, update the counters
                            index--;
                            backtrackIndex++;
                        } else {
                            // ... if we are not backtracking and we are in key mode, go forth
                            index++;
                        }
                    }
            }
        }
        // Add the last pair
        if (valueBuffer.length() > 0) {
            add(key, valueBuffer.toString(), collector);
        }
        return collector;
    }

    /**
     * Adds the specified key/value to the specified {@link T} collector
     *
     * @param key The key
     * @param value The value
     * @param collector The {@link T} collector
     */
    private static <T> void add(String key, String value, T collector) {
        CollectorAdder collectorAdder = collectorAdders.get(collector.getClass());
        if (collectorAdder == null) {
            throw new IllegalArgumentException("Unsupported collector type '" + collector.getClass() + "'");
        }
        collectorAdder.add(key, value, collector);
    }

    /**
     * Splits the parametrised header to single elements using the semicolon (';')
     * as the split character
     *
     * @param header The header to split
     * @return A {@link List} with the split elements
     */
    static List<String> splitElements(CharSequence header) {
        List<String> split = new ArrayList<>();
        boolean openQuotes = false;
        boolean openParenthesis = false;
        StringBuilder lineBuffer = new StringBuilder(128);
        for (int index = 0; index < header.length(); index++) {
            char c = header.charAt(index);
            switch (c) {
                case '(':
                    openParenthesis = true;
                    lineBuffer.append(c);
                    break;
                case ')':
                    openParenthesis = false;
                    lineBuffer.append(c);
                    break;
                case '"':
                    openQuotes = !openQuotes;
                    lineBuffer.append(c);
                    break;
                case ';':
                    if (!openQuotes && !openParenthesis) {
                        split.add(lineBuffer.toString().trim());
                        lineBuffer.setLength(0);
                        break;
                    }
                default:
                    lineBuffer.append(c);
            }
        }
        // Add last one
        if (lineBuffer.length() > 0) {
            split.add(lineBuffer.toString().trim());
        }
        return split;
    }
}

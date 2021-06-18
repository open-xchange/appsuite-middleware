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

package com.openexchange.mail.authenticity.impl.core;

import static com.openexchange.java.Autoboxing.L;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.mail.authenticity.MailAuthenticityAttribute;

/**
 * {@link StringUtil} - Helper methods for parsing String values
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@SuppressWarnings("unchecked")
final class StringUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

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

    private static final Map<Class<?>, CollectorAdder> COLLECTOR_ADDERS = ImmutableMap.<Class<?>, CollectorAdder> builder().put(HashMap.class, new CollectorAdder() {

        @Override
        public <T> void add(String key, String value, T collector) {
            ((Map<String, String>) collector).put(key, value);
        }
    }).put(LinkedHashMap.class, new CollectorAdder() {

        @Override
        public <T> void add(String key, String value, T collector) {
            ((Map<String, String>) collector).put(key, value);
        }
    }).put(ArrayList.class, new CollectorAdder() {

        @Override
        public <T> void add(String key, String value, T collector) {
            ((List<MailAuthenticityAttribute>) collector).add(new MailAuthenticityAttribute(key, value));
        }
    }).put(LinkedList.class, new CollectorAdder() {

        @Override
        public <T> void add(String key, String value, T collector) {
            ((List<MailAuthenticityAttribute>) collector).add(new MailAuthenticityAttribute(key, value));
        }
    }).build();

    /**
     * Parses the specified element as a key/value {@link Map}
     *
     * @param element The element to parse
     * @return A {@link Map} with the key/value attributes of the element
     */
    static Map<String, String> parseMap(CharSequence element) {
        Map<String, String> mapCollector = new HashMap<>();
        return parseToCollector(element, mapCollector);
    }

    /**
     * Parses the specified element as a {@link List} of {@link MailAuthenticityAttribute}s
     *
     * @param element The element to parse
     * @return a {@link List} with the {@link MailAuthenticityAttribute}s
     */
    static List<MailAuthenticityAttribute> parseList(CharSequence element) {
        List<MailAuthenticityAttribute> listCollector = new ArrayList<>();
        return parseToCollector(element, listCollector);
    }

    private static final Pattern REGEX_PAIR;
    static {
        String quotedString = "\"(?:(?:\\\\\\\")|[^\"])+?\"";
        String token = "[[\\p{L}\\p{ASCII}]&&[^\\p{Cntrl}()<>,;:\\\"/\\[\\]?={}\\p{Blank}]]+";
        String comment = "\\([^)]*\\)";
        String value = "(?:" + quotedString + "|" + token + ")(?:\\p{Space}?" + comment + ")?";

        REGEX_PAIR = Pattern.compile("([a-zA-Z0-9-._]+)=(" + value + ")(?:\r?\n)?( |;|$)");
    }

    private static final int MAX_NUMBER_OF_ATTRIBUTES = 250;

    /**
     * Parses the attributes (key value pairs separated by an equals '=' sign) of the specified element to the specified {@link T} collector.
     *
     * @param element The element to parse
     * @return A {@link T} with the key/value attributes of the element
     */
    private static <T> T parseToCollector(CharSequence element, T collector) {
        CollectorAdder collectorAdder = COLLECTOR_ADDERS.get(collector.getClass());
        if (collectorAdder == null) {
            throw new IllegalArgumentException("Unsupported collector type '" + collector.getClass() + "'");
        }

        if (element.toString().indexOf('=') < 0) {
            // No pairs; return as a singleton collector with the line being both the key and the value
            String kv = element.toString();
            collectorAdder.add(kv, kv, collector);
            return collector;
        }

        Matcher m = REGEX_PAIR.matcher(element);
        int maxAttrs = MAX_NUMBER_OF_ATTRIBUTES;
        while (maxAttrs-- > 0 && m.find()) {
            String key = m.group(1);
            String value = m.group(2);
            collectorAdder.add(key, value, collector);
        }
        return collector;
    }

    /**
     * Splits the parameterized header to single elements using the semicolon (';')
     * as the split character
     *
     * @param header The header to split
     * @return A {@link List} with the split elements
     */
    static List<String> splitElements(CharSequence header) {
        LOGGER.debug("Splitting header: {}", header);
        long start = System.currentTimeMillis();

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
                    //$FALL-THROUGH$
                default:
                    lineBuffer.append(c);
            }
        }
        // Add last one
        if (lineBuffer.length() > 0) {
            split.add(lineBuffer.toString().trim());
        }
        LOGGER.trace("Header '{}' split in {} msec.", header, L(System.currentTimeMillis() - start));
        return split;
    }
}

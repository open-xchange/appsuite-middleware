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

package com.openexchange.subscribe.microformats.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.microformats.OXMFParser;
import com.openexchange.subscribe.microformats.OXMFSubscriptionErrorMessage;

/**
 * {@link OXMFParserImpl} - Implements {@link OXMFParser} based on <a href='http://stax.codehaus.org/'>StAX</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXMFParserImpl implements OXMFParser {

    private static final String ELEM_BODY = "body";

    private static final String ATTR_CLASS = "class";

    private static final String ATTR_SRC = "src";

    private static final String ELEM_IMG = "img";

    /*
     * Member section
     */

    private final Set<String> containerElements;

    private final Set<String> attributePrefixes;

    private final List<Map<String, String>> parsedContainerElements;

    private boolean body;

    private int level;

    /**
     * Initializes a new {@link OXMFParserImpl}.
     */
    public OXMFParserImpl() {
        super();
        containerElements = new HashSet<String>();
        attributePrefixes = new HashSet<String>();
        parsedContainerElements = new ArrayList<Map<String, String>>();
    }

    @Override
    public void reset() {
        containerElements.clear();
        attributePrefixes.clear();
        parsedContainerElements.clear();
        body = false;
        level = 0;
    }

    @Override
    public void addContainerElement(final String containerElement) {
        containerElements.add(containerElement);
    }

    @Override
    public void addAttributePrefix(final String prefix) {
        attributePrefixes.add(prefix);
    }

    @Override
    public List<Map<String, String>> parse(final String html) throws OXException {
        return parse(new StringReader(html));
    }

    @Override
    public List<Map<String, String>> parse(final Reader html) throws OXException {
        /*
         * Create XMLStreamReader instance
         */
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader parser;
        try {
            parser = factory.createXMLStreamReader(html);
        } catch (final XMLStreamException e) {
            throw OXMFSubscriptionErrorMessage.ParseException.create(e, e.getMessage());
        }
        try {
            /*
             * Iterate through nodes
             */
            for (int event = parser.next(); XMLStreamConstants.END_DOCUMENT != event; event = parser.next()) {
                /*
                 * Check for starting element
                 */
                if (XMLStreamConstants.START_ELEMENT == event) {
                    handleStartElement(parser);
                }
            }
            return parsedContainerElements;
        } catch (final XMLStreamException e) {
            throw OXMFSubscriptionErrorMessage.ParseException.create(e, e.getMessage());
        } finally {
            try {
                parser.close();
            } catch (final XMLStreamException e) {
                org.slf4j.LoggerFactory.getLogger(OXMFParserImpl.class).error("", e);
            }
        }
    }

    /**
     * Pattern to split by any whitespace character: [ \t\n\x0B\f\r].
     */
    private static final Pattern SPLIT = Pattern.compile("\\s+");

    /**
     * Handles a starting element. Checks if it contains a "class" attribute whose value is contained in set of container elements.
     *
     * @param parser The XMLStreamReader instance
     * @throws XMLStreamException If parsing fails
     */
    private void handleStartElement(final XMLStreamReader parser) throws XMLStreamException {
        if (ELEM_BODY.equalsIgnoreCase(parser.getLocalName())) {
            body = true;
        } else if (body) {
            final int count = parser.getAttributeCount();
            boolean found = false;
            for (int i = 0; i < count; i++) {
                final String attributeName = parser.getAttributeLocalName(i);
                if (ATTR_CLASS.equalsIgnoreCase(attributeName)) {
                    final String attributeValue = parser.getAttributeValue(i);
                    final String[] classes = SPLIT.split(attributeValue, 0);
                    for (final String clazz : classes) {
                        if (containerElements.contains(clazz)) {
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (found) {
                parsedContainerElements.add(parseContainerElement(parser));
            }

        }
    }

    /**
     * Parses a container element. Precondition is that current event is XMLStreamConstants.START_ELEMENT from container element.
     *
     * @param parser The XMLStreamReader instance
     * @return A {@link Map map} containing container element's name-value-pairs
     * @throws XMLStreamException If parsing fails
     */
    private Map<String, String> parseContainerElement(final XMLStreamReader parser) throws XMLStreamException {
        final Map<String, String> map = new HashMap<String, String>();
        // Entered with current event set to XMLStreamConstants.START_ELEMENT. Therefore increase level
        level++;
        do {
            final int event = parser.next();
            if (XMLStreamConstants.START_ELEMENT == event) {
                level++;
                parseNestedElement(parser, map);
            } else if (XMLStreamConstants.END_ELEMENT == event) {
                level--;
            }
        } while (level > 0);
        return map;
    }

    /**
     * Parses a nested element inside a container element
     *
     * @param parser The XMLStreamReader instance
     * @param map The map backing container element's name-value-pairs
     * @throws XMLStreamException If parsing fails
     */
    private void parseNestedElement(final XMLStreamReader parser, final Map<String, String> map) throws XMLStreamException {
        final int count = parser.getAttributeCount();
        final List<String> classList = new LinkedList<String>();
        String text = null;
        boolean collectSrc = false;
        if (ELEM_IMG.equalsIgnoreCase(parser.getLocalName())) {
            collectSrc = true;
        }
        for (int i = 0; i < count; i++) {
            final String attributeName = parser.getAttributeLocalName(i);
            if (ATTR_CLASS.equalsIgnoreCase(attributeName)) {
                final String attributeValue = parser.getAttributeValue(i);
                final String[] classes = SPLIT.split(attributeValue, 0);
                for (final String clazz : classes) {
                    if (attributePrefixes.contains(clazz) || startsWith(clazz)) {
                        classList.add(clazz);
                    }
                }
            } else if (collectSrc && ATTR_SRC.equalsIgnoreCase(attributeName)) {
                text = parser.getAttributeValue(i);
            }
        }

        if (!classList.isEmpty()) {
            if (text == null) {
                text = parser.getElementText();
                level--;
            }
            for (final String clazz : classList) {
                map.put(clazz, text);
            }
        }

    }

    /**
     * Checks if specified "class" attribute's value starts with one of this parser's prefixes.
     *
     * @param attributeValue The "class" attribute's value
     * @return <code>true</code> if specified "class" attribute's value starts with one of this parser's prefixes; otherwise
     *         <code>false</code>
     */
    private boolean startsWith(final String attributeValue) {
        for (final String prefix : attributePrefixes) {
            if (attributeValue.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}

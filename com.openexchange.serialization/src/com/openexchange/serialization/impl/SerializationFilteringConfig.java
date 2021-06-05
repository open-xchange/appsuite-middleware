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

package com.openexchange.serialization.impl;

import com.google.common.collect.ImmutableList;
import com.openexchange.xml.util.XMLUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * {@link SerializationFilteringConfig} is a configuration for a {@link FilteringObjectInputStream}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class SerializationFilteringConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SerializationFilteringConfig.class);

    private final List<Pattern> blacklist;

    /**
     * Initializes a new {@link SerializationFilteringConfig}.
     *
     * @param configFile The configuration file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    SerializationFilteringConfig(final File configFile) throws ParserConfigurationException, SAXException, IOException {

        if (!configFile.exists()) {
            throw new IOException("File not found");
        }
        DocumentBuilderFactory dbFactory = XMLUtils.safeDbf(DocumentBuilderFactory.newInstance());
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(configFile);
        doc.getDocumentElement().normalize();

        // Load blacklist
        NodeList regexList = ((Element) doc.getElementsByTagName("blacklist").item(0)).getElementsByTagName("regexp");
        int length = regexList.getLength();
        if (length <= 0) {
            blacklist = Collections.emptyList();
        } else {
            ImmutableList.Builder<Pattern> patterns = ImmutableList.builderWithExpectedSize(length);
            for (int x = 0; x < length; x++) {
                Node item = regexList.item(x);
                try {
                    patterns.add(Pattern.compile(item.getTextContent()));
                } catch (PatternSyntaxException e) {
                    LOG.error("Unable to parse java deserialization filter config. Please check serialkiller.xml for errors.", e);
                    blacklist = Collections.unmodifiableList(Collections.emptyList());
                    return;
                }
            }
            blacklist = patterns.build();
        }
    }

    /**
     * Gets the blacklist; a list of {@link Pattern regular expressions} for class names, which are not allowed being serialized/deserialized.
     *
     * @return The blacklist
     */
    List<Pattern> getBlacklist() {
        return blacklist;
    }

}

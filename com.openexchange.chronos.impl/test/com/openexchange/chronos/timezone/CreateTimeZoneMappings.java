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

package com.openexchange.chronos.timezone;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.openexchange.java.Strings;
import com.openexchange.xml.util.XMLUtils;

/**
 * {@link CreateTimeZoneMappings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CreateTimeZoneMappings {

    private static final String URL = "https://raw.githubusercontent.com/unicode-org/cldr/master/common/supplemental/windowsZones.xml";

    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = XMLUtils.safeDbf(DocumentBuilderFactory.newInstance());;
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Map<String, String> mapppings;
        try (InputStream inputStream = new java.net.URL(URL).openStream()) {
            Document document = documentBuilder.parse(inputStream);
            mapppings = parse(document);
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("java.util.Map<String, String> mappings = ImmutableMap.<String, String>builder()\n");
        for (Entry<String, String> entry : mapppings.entrySet()) {
            stringBuilder.append("    .put(\"").append(entry.getKey()).append("\", \"").append(entry.getValue()).append("\")\n");
        }
        stringBuilder.append(".build();");
        System.out.println(stringBuilder.toString());
    }

    private static Map<String, String> parse(Document document) {
        Set<String> availableIDs = new HashSet<String>(Arrays.asList(TimeZone.getAvailableIDs()));
        Map<String, String> mappings = new HashMap<String, String>(150);
        NodeList nodeList = document.getElementsByTagName("mapZone");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            String windowsName = element.getAttribute("other");
            if (mappings.containsKey(windowsName)) {
                continue; // only consider first mapping
            }
            String olsonName = element.getAttribute("type");
            if (Strings.isNotEmpty(windowsName) && Strings.isNotEmpty(olsonName) && availableIDs.contains(olsonName)) {
                mappings.put(windowsName, olsonName);
            }
        }
        return mappings;
    }

}

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

/**
 * {@link CreateTimeZoneMappings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CreateTimeZoneMappings {

    private static final String URL = "http://unicode.org/repos/cldr/trunk/common/supplemental/windowsZones.xml";

    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/validation", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
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
        stringBuilder.append("build();");
        System.out.println(stringBuilder.toString());
    }

    private static Map<String, String> parse(Document document) throws Exception {
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

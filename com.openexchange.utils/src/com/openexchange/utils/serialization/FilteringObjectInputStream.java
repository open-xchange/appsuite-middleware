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

package com.openexchange.utils.serialization;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * {@link FilteringObjectInputStream} prevents invalid deserialization by using a blacklist
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class FilteringObjectInputStream extends ObjectInputStream {

    private static final Logger LOG = LoggerFactory.getLogger(FilteringObjectInputStream.class.getName());
    private final Configuration config;

    /**
     * Initializes a new {@link FilteringObjectInputStream}.
     * 
     * @param in The {@link InputStream}
     * @throws IOException
     */
    FilteringObjectInputStream(InputStream in, Configuration config) throws IOException {
        super(in);
        this.config = config;
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass input) throws IOException, ClassNotFoundException {

        for (Pattern blackPattern : config.blacklist()) {
            Matcher blackMatcher = blackPattern.matcher(input.getName());

            if (blackMatcher.find()) {
                LOG.error(String.format("Blocked by blacklist '%s'. Match found for '%s'", new Object[] { blackPattern.pattern(), input.getName() }));
                throw new InvalidClassException(input.getName(), "Class blocked from deserialization (blacklist)");
            }
        }

        return super.resolveClass(input);
    }

    static final class Configuration {

        private List<Pattern> blacklist;

        Configuration(final File configFile) throws ParserConfigurationException, SAXException, IOException {

            if(!configFile.exists()) {
                throw new IOException("File not found");
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);
            doc.getDocumentElement().normalize();

            // load blacklist
            List<Pattern> patterns = new ArrayList<>();
            NodeList regexList = ((Element) doc.getElementsByTagName("blacklist").item(0)).getElementsByTagName("regexp");
            for (int x = 0; x < regexList.getLength(); x++) {
                Node item = regexList.item(x);
                patterns.add(Pattern.compile(item.getTextContent()));
            }

            blacklist = Collections.unmodifiableList(patterns);
        }

        Iterable<Pattern> blacklist() {
            return blacklist;
        }

    }

}

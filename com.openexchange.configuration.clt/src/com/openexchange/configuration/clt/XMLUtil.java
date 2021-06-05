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

package com.openexchange.configuration.clt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * {@link XMLUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public final class XMLUtil {

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    /**
     * Parses either the {@link System#in} input stream or the input stream
     * denoted by the specified file as a Properties file.
     * 
     * @param stdin <code>true</code> to parse from {@link System#in}
     * @param filename The filename
     * @return The parsed {@link Properties} from the input stream
     */
    public static final Properties parseInput(boolean stdin, String filename) {
        Properties properties = new Properties();
        try {
            InputStream is = IOUtil.determineInput(stdin, filename);
            if (null == is) {
                return null;
            }
            try {
                properties.load(is);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            System.err.println("Can not read XML file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return properties;
    }

    /**
     * Parses the specified {@link InputStream} as a {@link Document}
     * 
     * @param is The {@link InputStream} to parse
     * @return The parsed {@link Document}
     */
    public static final Document parseInput(InputStream is) {
        if (null == is) {
            return null;
        }
        final DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(new ClassloaderEntityResolver());
        } catch (ParserConfigurationException e) {
            System.err.println("Can not configure XML parser: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        final Document document;
        try {
            try {
                document = db.parse(is);
            } finally {
                is.close();
            }
        } catch (SAXException e) {
            System.err.println("Can not parse XML document: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("Can not read XML file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return document;
    }
}

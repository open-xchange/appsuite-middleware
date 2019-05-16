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

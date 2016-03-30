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

package com.openexchange.textxtraction.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;

/**
 * {@link OpenOfficeExtractor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OpenOfficeExtractor {

    private OpenOfficeExtractor() {
        super();
    }

    private static void processElement(final Content content, final StringBuilder textBuilder) {
        if (content instanceof Element) {
            final Element e = (Element) content;
            final String elementName = e.getQualifiedName();
            if (elementName.startsWith("text")) {
                if (elementName.equals("text:tab")) {
                    textBuilder.append("\\t");
                } else if (elementName.equals("text:s")) {
                    textBuilder.append(" ");
                } else {
                    for (final Content child : e.getContent()) {
                        // If Child is a Text Node, then append the text
                        if (child instanceof Text) {
                            final Text t = (Text) child;
                            textBuilder.append(t.getValue());
                        } else {
                            processElement(child, textBuilder); // Recursively process the child element
                        }
                    }
                }
                if (elementName.equals("text:p")) {
                    textBuilder.append("\\n");
                }
            } else {
                for (final Content nonTextChild : e.getContent()) {
                    processElement(nonTextChild, textBuilder);
                }
            }
        }
    }

    /**
     * Extracts text from OpenOffice Document's input stream.
     *
     * @param in The input stream
     * @return The extracted text
     * @throws IOException If an I/O error occurs
     */
    public static String getText(final InputStream in) throws IOException {
        // Unzip the openOffice Document
        final ZipInputStream zis = new ZipInputStream(in);
        final StringBuilder textBuilder = new StringBuilder(512);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().equals("content.xml")) {
                try {
                    final SAXBuilder sax = new SAXBuilder();
                    final Document doc = sax.build(zis);
                    final Element rootElement = doc.getRootElement();
                    processElement(rootElement, textBuilder);
                    return textBuilder.toString();
                } catch (final JDOMException e) {
                    throw new IOException(e.getMessage(), e);
                }
            }
        }
        return textBuilder.toString();
    }

}

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
                } catch (JDOMException e) {
                    throw new IOException(e.getMessage(), e);
                }
            }
        }
        return textBuilder.toString();
    }

}

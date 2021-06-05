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

package com.openexchange.plist;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import com.openexchange.plist.xml.StaxUtils;

/**
 * {@link PListWriter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class PListWriter {

    /**
     * Initializes a new {@link PListWriter}.
     */
    public PListWriter() {
        super();
    }

    /**
     * Writes this plist to given XML writer.
     *
     * @param pListDict The PLIst to write
     * @param writer The writer to write to
     * @throws IOException If writing fails
     */
    public void write(PListDict pListDict, Writer writer) throws IOException {
        try {
            write(pListDict, StaxUtils.createXMLStreamWriter(writer));
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Writes this plist to given XML writer.
     *
     * @param pListDict The PLIst to write
     * @param out The output stream to write
     * @throws IOException If writing fails
     */
    public void write(PListDict pListDict, OutputStream out) throws IOException {
        try {
            write(pListDict, StaxUtils.createXMLStreamWriter(out));
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Writes this plist to given XML writer.
     *
     * @param pListDict The PLIst to write
     * @param writer The XML writer to write to
     * @throws XMLStreamException If writing XML fails
     */
    private void write(PListDict pListDict, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeDTD("\n<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n");
        writer.writeStartElement("plist");
        writer.writeAttribute("version", "1.0");

        pListDict.write(writer);

        writer.writeEndElement();
        writer.writeEndDocument();
    }

}

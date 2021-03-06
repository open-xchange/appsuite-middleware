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

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * {@link PListArrayValue}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class PListArrayValue extends AbstractPListKeyValue {

    private final List<AbstractPListElement> array;

    /**
     * Initializes a new {@link PListArrayValue}.
     */
    public PListArrayValue(String key) {
        super(key);
        array = new ArrayList<AbstractPListElement>(2);
    }

    public PListArrayValue add(PListElement element) {
        array.add((AbstractPListElement) element);
        return this;
    }

    @Override
    protected void writeValue(XMLStreamWriter writer) throws XMLStreamException {
        if (array.isEmpty()) {
            writer.writeEmptyElement("array");
            return;
        }

        writer.writeStartElement("array");
        for (AbstractPListElement pListElement : array) {
            pListElement.write(writer);
        }
        writer.writeEndElement();
    }

}

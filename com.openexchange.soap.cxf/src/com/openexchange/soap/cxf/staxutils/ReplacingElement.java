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

package com.openexchange.soap.cxf.staxutils;

import javax.xml.namespace.QName;
import org.apache.ws.commons.schema.XmlSchemaElement;

/**
 * {@link ReplacingElement} remembers all values when some XML tag needs to be replaced within the {@link ReplacingXMLStreamReader}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
class ReplacingElement {

    private final QName original;
    private QName expected;
    private XmlSchemaElement xmlSchema;
    private int childPosition = 0;
    private boolean onlyWithName = false;

    ReplacingElement(QName original) {
        super();
        this.original = original;
    }

    ReplacingElement(QName original, QName expected) {
        super();
        this.original = original;
        this.expected = expected;
    }

    public QName getExpected() {
        return expected;
    }

    public void setExpected(QName expected) {
        this.expected = expected;
    }

    public XmlSchemaElement getXmlSchema() {
        return xmlSchema;
    }

    public void setXmlSchema(XmlSchemaElement xmlSchema) {
        this.xmlSchema = xmlSchema;
    }

    public void setChildPosition(int childPosition) {
        this.childPosition = childPosition;
    }

    public int nextChildPosition() {
        return childPosition++;
    }

    public void resetChildPosition() {
        childPosition = 0;
    }

    public boolean isOnlyWithName() {
        return onlyWithName;
    }

    public void setOnlyWithName() {
        this.onlyWithName = true;
    }

    @Override
    public String toString() {
        return "ReplacingElement \"" + original + "\" -> \"" + expected + "\"";
    }
}

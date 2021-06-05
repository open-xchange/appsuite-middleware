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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * {@link DroppingXMLStreamReader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DroppingXMLStreamReader extends StreamReaderDelegate {

    private final Set<String> inDropSet;
    private int depth;

    /**
     * Initializes a new {@link DroppingXMLStreamReader}.
     *
     * @param reader The XML stream reader to wrap
     * @param inDropSet The set containing the elements to drop in incoming SOAP request
     */
    public DroppingXMLStreamReader(XMLStreamReader reader, Set<String> inDropSet) {
        super(reader);
        this.inDropSet = null == inDropSet ? null : ImmutableSet.copyOf(inDropSet);
    }

    /**
     * Gets current element depth count
     *
     * @return The depth count
     */
    public int getDepth() {
        return depth;
    }

    @Override
    public String getElementText() throws XMLStreamException {
        XMLStreamReader reader = getParent();
        String ret = reader.getElementText();
        //workaround bugs in some readers that aren't properly advancing to
        //the END_ELEMENT (*cough*jettison*cough*)
        while (reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
            reader.next();
        }
        depth--;
        return ret;
    }

    @Override
    public int next() throws XMLStreamException {
        int event = super.next();
        if (XMLStreamConstants.START_ELEMENT == event) {
            depth++;
            QName name = super.getName();
            boolean dropped = inDropSet.contains(name.getLocalPart());
            if (dropped) {
                // skip the current element (deep drop)
                handleDeepDrop();
                event = next();
            }
        } else if (XMLStreamConstants.END_ELEMENT == event) {
            depth--;
        }
        return event;
    }

    private void handleDeepDrop() throws XMLStreamException {
        int depth = getDepth();
        while (depth != getDepth() || super.next() != XMLStreamConstants.END_ELEMENT) {
            // get to the matching end element event
        }
    }

}

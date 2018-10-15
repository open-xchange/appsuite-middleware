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
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DroppingXMLStreamReader extends StreamReaderDelegate {

    private final Set<String> inDropSet;
    private int depth;

    public DroppingXMLStreamReader(XMLStreamReader reader, Set<String> inDropSet) {
        super(reader);
        this.inDropSet = null == inDropSet ? null : ImmutableSet.copyOf(inDropSet);
    }

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

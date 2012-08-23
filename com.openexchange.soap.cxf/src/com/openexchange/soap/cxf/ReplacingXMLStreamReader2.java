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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.soap.cxf;

import java.util.Locale;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSequence;

/**
 * {@link ReplacingXMLStreamReader2}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ReplacingXMLStreamReader2 extends StreamReaderDelegate {

    private final Stack<ReplacingElement> stack = new Stack<ReplacingElement>();
    private ReplacingElement current;

    public ReplacingXMLStreamReader2(BindingOperationInfo bop, XMLStreamReader reader) {
        super(reader);
        QName name = super.getName();
        ReplacingElement method = new ReplacingElement(name, name);
        stack.push(method);
        method.setXmlSchema((XmlSchemaElement) bop.getOperationInfo().getInput().getMessagePart(0).getXmlSchema());
        current = null;
    }

    @Override
    public int next() throws XMLStreamException {
        int event = super.next();
        if (XMLStreamConstants.START_ELEMENT == event) {
            final QName name = super.getName();
            ReplacingElement element = new ReplacingElement(name);
            stack.push(element);
            if (isGeneric(name)) {
                QName expected = getExpected(stack.get(stack.size() - 2));
                element.setExpected(expected);
                current = element;
            } else {
                element.setExpected(name);
                current = null;
            }
            stack.push(element);
        } else if (XMLStreamConstants.END_ELEMENT == event) {
            current = stack.pop();
        } else {
            current = null;
        }
        return event;
    }

    @Override
    public String getLocalName() {
        return null == current ? super.getLocalName() : current.getExpected().getLocalPart();
    }

    @Override
    public String getNamespaceURI() {
        return null == current ? super.getNamespaceURI() : current.getExpected().getNamespaceURI();
    }

    private static QName getExpected(ReplacingElement parent) throws XMLStreamException {
        XmlSchemaElement schema = parent.getXmlSchema();
        if (schema.getSchemaType() instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType cplxType = (XmlSchemaComplexType) schema.getSchemaType();
            XmlSchemaSequence seq = (XmlSchemaSequence) cplxType.getParticle();
            XmlSchemaElement child = (XmlSchemaElement) seq.getItems().get(parent.nextChildPosition());
            return child.getQName();
        }
        throw new XMLStreamException("Complex type expected.");
    }

    private static boolean isGeneric(final QName name) {
        final String localPart = name.getLocalPart();
        return null != localPart && localPart.toLowerCase(Locale.US).startsWith("c-gensym");
    }
}

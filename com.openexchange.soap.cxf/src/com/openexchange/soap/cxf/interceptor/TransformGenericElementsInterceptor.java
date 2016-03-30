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

package com.openexchange.soap.cxf.interceptor;

import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.DocLiteralInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.staxutils.transform.TransformUtils;
import com.openexchange.soap.cxf.staxutils.ReplacingXMLStreamReader;

/**
 * {@link TransformGenericElementsInterceptor}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TransformGenericElementsInterceptor extends AbstractInDatabindingInterceptor {

    public TransformGenericElementsInterceptor() {
        super(Phase.UNMARSHAL);
        addBefore(DocLiteralInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        XMLStreamReader reader = message.getContent(XMLStreamReader.class);
        if (null != reader && reader.hasName()) {
            QName name = reader.getName();
            reader = TransformUtils.createNewReaderIfNeeded(reader, message.getContent(InputStream.class));
            Exchange exchange = message.getExchange();
            BindingOperationInfo bop = getBindingOperationInfo(exchange, name, isRequestor(message));
            if (null != bop) {
                // Create transforming reader
                reader = new ReplacingXMLStreamReader(bop, reader);
                message.setContent(XMLStreamReader.class, reader);
                message.removeContent(InputStream.class);
            }
            // SOAP method is not found, so normal exception from CXF framework is sent.
        }
    }
}

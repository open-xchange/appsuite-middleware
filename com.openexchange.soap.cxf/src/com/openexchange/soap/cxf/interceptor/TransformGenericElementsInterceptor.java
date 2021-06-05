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

package com.openexchange.soap.cxf.interceptor;

import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.staxutils.transform.TransformUtils;
import org.apache.cxf.wsdl.interceptors.DocLiteralInInterceptor;
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

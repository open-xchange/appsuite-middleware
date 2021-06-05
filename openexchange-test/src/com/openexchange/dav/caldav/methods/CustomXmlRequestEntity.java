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

package com.openexchange.dav.caldav.methods;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.jackrabbit.webdav.client.methods.XmlRequestEntity;
import org.apache.jackrabbit.webdav.xml.ResultHelper;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * {@link CustomXmlRequestEntity}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CustomXmlRequestEntity implements RequestEntity {

    private final ByteArrayOutputStream xml = new ByteArrayOutputStream();

    public CustomXmlRequestEntity(Document xmlDocument, String encoding) throws IOException {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            if (null != encoding) {
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, encoding);
            }
            transformer.transform(new DOMSource(xmlDocument), ResultHelper.getResult(new StreamResult(xml)));
        } catch (TransformerException e) {
            LoggerFactory.getLogger(XmlRequestEntity.class).error(e.getMessage());
            throw new IOException(e.getMessage());
        } catch (SAXException e) {
            LoggerFactory.getLogger(XmlRequestEntity.class).error(e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public String getContentType() {
        // TODO: Shouldn't this be application/xml? See JCR-1621
        return "text/xml; charset=UTF-8";
    }

    @Override
    public void writeRequest(OutputStream out) throws IOException {
        xml.writeTo(out);
    }

    @Override
    public long getContentLength() {
        return xml.size();
    }

}

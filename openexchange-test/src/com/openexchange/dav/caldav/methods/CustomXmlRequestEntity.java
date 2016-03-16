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


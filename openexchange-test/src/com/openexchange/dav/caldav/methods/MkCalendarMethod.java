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

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethodBase;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.dav.PropertyNames;

/**
 * {@link MkCalendarMethod}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MkCalendarMethod extends DavMethodBase {

    public static final String METHOD_MKCALENDAR = "MKCALENDAR";

    /**
     * Initializes a new {@link MkCalendarMethod}.
     *
     * @param uri
     * @param setProperties
     * @throws IOException
     */
    public MkCalendarMethod(String uri, DavPropertySet setProperties) throws IOException {
        super(uri);
        if (null == setProperties) {
            throw new IllegalArgumentException("setProperties must not be null.");
        }
        if (setProperties.isEmpty()) {
            throw new IllegalArgumentException("setProperties cannot be empty.");
        }
        try {
            Document document = DomUtil.createDocument();
            Element propupdate = DomUtil.addChildElement(document, PropertyNames.MKCALENDAR.getName(), PropertyNames.MKCALENDAR.getNamespace());
            Element set = DomUtil.addChildElement(propupdate, XML_SET, NAMESPACE);
            set.appendChild(setProperties.toXml(document));
            setRequestBody(document);
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void setRequestBody(Document requestBody) throws IOException {
        setRequestEntity(new CustomXmlRequestEntity(requestBody, "UTF-16"));
    }

    @Override
    public String getName() {
        return METHOD_MKCALENDAR;
    }

    @Override
    protected boolean isSuccess(int statusCode) {
        return statusCode == DavServletResponse.SC_CREATED;
    }

    @Override
    protected void processMultiStatusBody(MultiStatus multiStatus, HttpState httpState, HttpConnection httpConnection) {
        MultiStatusResponse[] resp = multiStatus.getResponses();
        if (0 < resp.length) {
            super.processMultiStatusBody(multiStatus, httpState, httpConnection);
        }
    }

}

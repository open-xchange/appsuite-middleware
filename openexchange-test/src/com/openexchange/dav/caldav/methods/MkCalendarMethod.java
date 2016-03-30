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
            Element propupdate = DomUtil.addChildElement(
                document, PropertyNames.MKCALENDAR.getName(), PropertyNames.MKCALENDAR.getNamespace());
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


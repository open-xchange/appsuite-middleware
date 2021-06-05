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

package com.openexchange.dav.caldav.reports;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * {@link CalendarMultiGetReportInfo} - Encapsulates the BODY of a
 * {@link CalendarMultiGetReport} request ("calendar-multiget").
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalendarMultiGetReportInfo extends ReportInfo {

    private final String[] hrefs;

    /**
     * Creates a new {@link CalendarMultiGetReportInfo}.
     * 
     * @param hrefs The resource data references to include in the request
     */
    public CalendarMultiGetReportInfo(String[] hrefs) {
        this(hrefs, null);
    }

    /**
     * Creates a new {@link CalendarMultiGetReportInfo}.
     * 
     * @param hrefs The resource data references to include in the request
     * 
     * @param propertyNames the properties to include in the request
     */
    public CalendarMultiGetReportInfo(String[] hrefs, DavPropertyNameSet propertyNames) {
        super(CalendarMultiGetReport.CALENDAR_MULTIGET, DavConstants.DEPTH_0, propertyNames);
        this.hrefs = hrefs;
    }

    @Override
    public Element toXml(final Document document) {
        /*
         * create calendar-multiget element
         */
        Element multiGetElement = DomUtil.createElement(document, CalendarMultiGetReport.CALENDAR_MULTIGET.getLocalName(), CalendarMultiGetReport.CALENDAR_MULTIGET.getNamespace());
        multiGetElement.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(), Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(), DavConstants.NAMESPACE.getURI());
        /*
         * append properties element
         */
        multiGetElement.appendChild(super.getPropertyNameSet().toXml(document));
        /*
         * append hrefs
         */
        for (String href : hrefs) {
            multiGetElement.appendChild(DomUtil.createElement(document, DavConstants.XML_HREF, DavConstants.NAMESPACE, href));
        }
        return multiGetElement;
    }
}

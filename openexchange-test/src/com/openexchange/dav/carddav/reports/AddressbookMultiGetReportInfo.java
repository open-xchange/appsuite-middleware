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

package com.openexchange.dav.carddav.reports;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.PropContainer;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.dav.caldav.reports.CalendarMultiGetReport;

/**
 * {@link AddressbookMultiGetReportInfo}
 *
 * Encapsulates the BODY of a {@link CalendarMultiGetReport} request ("calendar-multiget").
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AddressbookMultiGetReportInfo extends ReportInfo {

    private final String[] hrefs;
    private final PropContainer propertyNames;

    /**
     * Creates a new {@link AddressbookMultiGetReportInfo}.
     * 
     * @param hrefs The contact data references to include in the request
     */
    public AddressbookMultiGetReportInfo(final String[] hrefs) {
        this(hrefs, null);
    }

    /**
     * Creates a new {@link AddressbookMultiGetReportInfo}.
     * 
     * @param hrefs The contact data references to include in the request
     * @param propertyNames the properties to include in the request
     */
    public AddressbookMultiGetReportInfo(final String[] hrefs, PropContainer propertyNames) {
        super(AddressbookMultiGetReport.ADDRESSBOOK_MULTI_GET, DavConstants.DEPTH_0, null);
        this.hrefs = hrefs;
        this.propertyNames = propertyNames;
    }

    @Override
    public Element toXml(final Document document) {
        /*
         * create addressbook-multi-get element
         */
        final Element addressbookMultiGetElement = DomUtil.createElement(document, AddressbookMultiGetReport.ADDRESSBOOK_MULTI_GET.getLocalName(), AddressbookMultiGetReport.ADDRESSBOOK_MULTI_GET.getNamespace());
        addressbookMultiGetElement.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(), Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(), DavConstants.NAMESPACE.getURI());
        /*
         * append properties element
         */
        if (null != propertyNames) {
            addressbookMultiGetElement.appendChild(propertyNames.toXml(document));
        }
        /*
         * append hrefs
         */
        for (final String href : hrefs) {
            addressbookMultiGetElement.appendChild(DomUtil.createElement(document, DavConstants.XML_HREF, DavConstants.NAMESPACE, href));
        }
        return addressbookMultiGetElement;
    }
}

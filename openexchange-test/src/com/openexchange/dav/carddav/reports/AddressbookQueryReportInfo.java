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

import java.util.Collections;
import java.util.List;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.PropContainer;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.dav.PropertyNames;

/**
 * {@link AddressbookQueryReportInfo}
 *
 * Encapsulates the BODY of a {@link AddressbookQueryReport} request ("addressbook-query").
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class AddressbookQueryReportInfo extends ReportInfo {

    private final List<PropFilter> filters;
    private final PropContainer propertyNames;
    private final String filterTest;

    /**
     * Creates a new {@link AddressbookQueryReportInfo}.
     *
     * @param filters The property filters
     */
    public AddressbookQueryReportInfo(List<PropFilter> filters) {
        this(filters, null, null);
    }

    /**
     * Creates a new {@link AddressbookQueryReportInfo}.
     *
     * @param filter The property filter
     * @param propertyNames the properties to include in the request
     */
    public AddressbookQueryReportInfo(PropFilter filter, PropContainer propertyNames) {
        this(Collections.singletonList(filter), propertyNames, null);
    }

    /**
     * Creates a new {@link AddressbookQueryReportInfo}.
     *
     * @param filters The property filters
     * @param propertyNames the properties to include in the request
     * @param filterTest <code>allof</code> to combine the filters with a logical <code>AND</code>, <code>anyof</code>
     *            for a logical <code>OR</code>, or <code>null</code> for the default behavior
     */
    public AddressbookQueryReportInfo(List<PropFilter> filters, PropContainer propertyNames, String filterTest) {
        super(AddressbookMultiGetReport.ADDRESSBOOK_MULTI_GET, DavConstants.DEPTH_0, null);
        this.filters = filters;
        this.filterTest = filterTest;
        this.propertyNames = propertyNames;
    }

    @Override
    public Element toXml(final Document document) {
        /*
         * create addressbook-query element
         */
        Element addressbookQueryElement = DomUtil.createElement(document, AddressbookQueryReport.ADDRESSBOOK_QUERY.getLocalName(), AddressbookQueryReport.ADDRESSBOOK_QUERY.getNamespace());
        addressbookQueryElement.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(), Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(), DavConstants.NAMESPACE.getURI());
        /*
         * append properties element
         */
        if (null != propertyNames) {
            addressbookQueryElement.appendChild(propertyNames.toXml(document));
        }
        /*
         * append filters
         */
        Element filterElement = DomUtil.createElement(document, "filter", PropertyNames.NS_CARDDAV);
        if (null != filterTest) {
            filterElement.setAttribute("test", filterTest);
        }
        for (PropFilter filter : filters) {
            Element propfilterElement = DomUtil.createElement(document, "prop-filter", PropertyNames.NS_CARDDAV);
            propfilterElement.setAttribute("name", filter.getName());
            Element textMatchElement = DomUtil.createElement(document, "text-match", PropertyNames.NS_CARDDAV, filter.getTextMatch());
            if (null != filter.getMatchType()) {
                textMatchElement.setAttribute("match-type", filter.getMatchType());
            }
            propfilterElement.appendChild(textMatchElement);
            filterElement.appendChild(propfilterElement);
        }
        addressbookQueryElement.appendChild(filterElement);
        return addressbookQueryElement;
    }
}

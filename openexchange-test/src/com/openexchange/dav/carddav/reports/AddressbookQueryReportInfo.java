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

package com.openexchange.dav.carddav.reports;

import java.util.Collections;
import java.util.List;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
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
    public AddressbookQueryReportInfo(PropFilter filter, DavPropertyNameSet propertyNames) {
        this(Collections.singletonList(filter), propertyNames, null);
    }

    /**
     * Creates a new {@link AddressbookQueryReportInfo}.
     *
     * @param filters The property filters
     * @param propertyNames the properties to include in the request
     * @param filterTest <code>allof</code> to combine the filters with a logical <code>AND</code>, <code>anyof</code>
     *        for a logical <code>OR</code>, or <code>null</code> for the default behavior
     */
    public AddressbookQueryReportInfo(List<PropFilter> filters, DavPropertyNameSet propertyNames, String filterTest) {
        super(AddressbookMultiGetReport.ADDRESSBOOK_MULTI_GET, DavConstants.DEPTH_0, propertyNames);
        this.filters = filters;
        this.filterTest = filterTest;
    }

    @Override
    public Element toXml(final Document document) {
    	/*
    	 * create addressbook-query element
    	 */
    	Element addressbookQueryElement = DomUtil.createElement(document,
			AddressbookQueryReport.ADDRESSBOOK_QUERY.getLocalName(),
			AddressbookQueryReport.ADDRESSBOOK_QUERY.getNamespace());
    	addressbookQueryElement.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(),
    			Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(), DavConstants.NAMESPACE.getURI());
    	/*
    	 * append properties element
    	 */
    	addressbookQueryElement.appendChild(getPropertyNameSet().toXml(document));
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

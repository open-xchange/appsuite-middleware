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
    	Element multiGetElement = DomUtil.createElement(document, CalendarMultiGetReport.CALENDAR_MULTIGET.getLocalName(), 
    			CalendarMultiGetReport.CALENDAR_MULTIGET.getNamespace());
    	multiGetElement.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(), 
    			Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(), DavConstants.NAMESPACE.getURI());
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

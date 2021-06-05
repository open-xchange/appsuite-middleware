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

import java.util.List;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.dav.PropertyNames;

/**
 * {@link CalendarQueryReportInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class CalendarQueryReportInfo extends ReportInfo {

    private final CompFilter filter;

    public CalendarQueryReportInfo(CompFilter filter, DavPropertyNameSet propertyNames, int depth) {
        super(CalendarQueryReport.CALENDAR_QUERY, depth, propertyNames);
        this.filter = filter;
    }

    @Override
    public Element toXml(final Document document) {
        Element element = super.toXml(document);
        Element filterElement = DomUtil.createElement(document, "filter", PropertyNames.NS_CALDAV);
        if (null != filter) {
            filterElement.appendChild(getFilterElement(document, filter));
        }
        element.appendChild(filterElement);
        return element;
    }

    private static Element getFilterElement(Document document, CompFilter filter) {
        Element element = DomUtil.createElement(document, "comp-filter", PropertyNames.NS_CALDAV);
        element.setAttribute("name", filter.getName());
        TimeRangeFilter timeRangeFilter = filter.getTimeRangeFilter();
        if (null != timeRangeFilter) {
            Element timeRangeElement = DomUtil.createElement(document, "time-range", PropertyNames.NS_CALDAV);
            if (null != timeRangeFilter.getStart()) {
                timeRangeElement.setAttribute("start", timeRangeFilter.getStart());
            }
            if (null != timeRangeFilter.getEnd()) {
                timeRangeElement.setAttribute("end", timeRangeFilter.getEnd());
            }
            element.appendChild(timeRangeElement);
        }
        List<CompFilter> subFilters = filter.getSubFilters();
        if (null != subFilters) {
            for (CompFilter subFilter : subFilters) {
                element.appendChild(getFilterElement(document, subFilter));
            }
        }
        return element;
    }

}

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

package com.openexchange.caldav.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Attribute;
import org.jdom2.Element;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;


/**
 * {@link FilterParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FilterParser {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FilterParser.class);

    public Filter parse(Element rootElement) throws WebdavProtocolException {
        Filter filter = new Filter();
        initChildFilters(filter, rootElement);
        return filter;
    }

    private static final String DATETIME_PATTERN = "yyyyMMdd'T'HHmmss'Z'";

    private void initChildFilters(Filter filter, Element rootElement) throws WebdavProtocolException {
        List<Element> children = rootElement.getChildren("comp-filter", CaldavProtocol.CAL_NS);
        for (Object cObj : children) {
            Element compFilterElement = (Element) cObj;
            CompFilter compFilter = new CompFilter();
            compFilter.setName(compFilterElement.getAttributeValue("name"));
            initChildFilters(compFilter, compFilterElement);
            filter.addFilter(compFilter);
        }

        children = rootElement.getChildren("time-range", CaldavProtocol.CAL_NS);
        for (Object cObj : children) {
            Element timeRangeElement = (Element) cObj;
            TimeRange tr = new TimeRange();

            tr.setStart(parseDate(timeRangeElement.getAttribute("start")));
            tr.setEnd(parseDate(timeRangeElement.getAttribute("end")));

            filter.addFilter(tr);
        }
    }

    private long parseDate(Attribute startAttr) throws WebdavProtocolException {
        SimpleDateFormat format = new SimpleDateFormat(DATETIME_PATTERN);
        format.setTimeZone(TimeZone.getTimeZone("utc"));
        if (startAttr != null && startAttr.getValue() != null) {
            try {
                Date startDate = format.parse(startAttr.getValue());
                return startDate.getTime();
            } catch (ParseException e) {
                LOG.error("", e);
                throw WebdavProtocolException.generalError(new WebdavPath(), HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        return TimeRange.NOT_SET;
    }
}

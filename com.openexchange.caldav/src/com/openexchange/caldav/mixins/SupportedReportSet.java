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

package com.openexchange.caldav.mixins;

import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;


/**
 * {@link SupportedReportSet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SupportedReportSet extends SingleXMLPropertyMixin {

    public static final String CALENDAR_MULTIGET = "<CAL:calendar-multiget/>";
    public static final String CALENDAR_QUERY = "<CAL:calendar-query/>";
    public static final String ACL_PRINCIPAL_PROP_SET = "<D:acl-principal-prop-set/>";
    public static final String PRINCIPAL_MATCH = "<D:principal-match/>";
    public static final String PRINCIPAL_PROPERTY_SEARCH = "<D:principal-property-search/>";
    public static final String EXPAND_PROPERTY = "<D:expand-property/>";
    public static final String CALENDARSERVER_PRINCIPAL_SEARCH = "<CS:calendarserver-principal-search/>";
    public static final String SYNC_COLLECTION = "<D:sync-collection/>";

    public static final String[] ALL_SUPPORTED = new String[] { 
        CALENDAR_MULTIGET, CALENDAR_QUERY, ACL_PRINCIPAL_PROP_SET, PRINCIPAL_MATCH, PRINCIPAL_PROPERTY_SEARCH, EXPAND_PROPERTY, CALENDARSERVER_PRINCIPAL_SEARCH, SYNC_COLLECTION
    };

    private static final String NAME = "supported-report-set";

    private final String[] supportedReports;

    /**
     * Initializes a new {@link SupportedReportSet} with all supported reports.
     */
    public SupportedReportSet() {
        this(ALL_SUPPORTED);
    }

    /**
     * Initializes a new {@link SupportedReportSet} with sepcific supported reports.
     *
     * @param supportedReports The supported reports
     */
    public SupportedReportSet(String... supportedReports) {
        super(Protocol.DAV_NS.getURI(), NAME);
        this.supportedReports = supportedReports;
    }

    @Override
    protected String getValue() {
        StringBuilder stringBuilder = new StringBuilder(1024);
        if (null != supportedReports) {
            for (String supportedReport : supportedReports) {
                stringBuilder.append("<D:supported-report><D:report>").append(supportedReport).append("</D:report></D:supported-report>");
            }
        }
        return stringBuilder.toString();
    }

}

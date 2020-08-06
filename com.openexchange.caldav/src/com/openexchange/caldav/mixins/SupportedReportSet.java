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

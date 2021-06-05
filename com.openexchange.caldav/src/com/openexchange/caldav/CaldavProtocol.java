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

package com.openexchange.caldav;

import java.util.Arrays;
import java.util.List;
import org.jdom2.Namespace;
import com.openexchange.caldav.reports.CaldavMultigetReport;
import com.openexchange.caldav.reports.CalendarQueryReport;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.reports.ACLPrincipalPropSet;
import com.openexchange.dav.reports.SyncCollection;
import com.openexchange.webdav.action.WebdavAction;

/**
 * The {@link CaldavProtocol} contains constants useful for our caldav implementation
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavProtocol extends DAVProtocol {

    private static final List<Namespace> ADDITIONAL_NAMESPACES = Arrays.asList(CAL_NS, APPLE_NS, CALENDARSERVER_NS);

    public static final String CALENDAR = "<CAL:calendar />";

    @Override
    public List<Namespace> getAdditionalNamespaces() {
        return ADDITIONAL_NAMESPACES;
    }

    @Override
    public WebdavAction getReportAction(String namespace, String name) {
        if (namespace.equals(CaldavMultigetReport.NAMESPACE) && name.equals(CaldavMultigetReport.NAME)) {
            return new CaldavMultigetReport(this);
        }
        if (namespace.equals(CalendarQueryReport.NAMESPACE) && name.equals(CalendarQueryReport.NAME)) {
            return new CalendarQueryReport(this);
        }
        if (namespace.equals(ACLPrincipalPropSet.NAMESPACE) && name.equals(ACLPrincipalPropSet.NAME)) {
            return new ACLPrincipalPropSet(this);
        }
        if (namespace.equals(SyncCollection.NAMESPACE) && name.equals(SyncCollection.NAME)) {
            return new SyncCollection(this);
        }
        return super.getReportAction(namespace, name);
    }

}

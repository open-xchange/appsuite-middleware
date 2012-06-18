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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.caldav;

import java.util.Arrays;
import java.util.List;
import org.jdom2.Namespace;
import com.openexchange.caldav.reports.CaldavMultigetReport;
import com.openexchange.caldav.reports.CalendarQueryReport;
import com.openexchange.caldav.reports.SyncCollection;
import com.openexchange.webdav.action.WebdavAction;
import com.openexchange.webdav.protocol.Protocol;

/**
 * The {@link CaldavProtocol} contains constants useful for our caldav implementation
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavProtocol extends Protocol {

    public static final Namespace CAL_NS = Namespace.getNamespace("CAL", "urn:ietf:params:xml:ns:caldav");

    public static final Namespace APPLE_NS = Namespace.getNamespace("APPLE", "http://apple.com/ns/ical/");
    
    private static final List<Namespace> ADDITIONAL_NAMESPACES = Arrays.asList(CAL_NS, APPLE_NS);

    public static final String CAL_NAMESPACE = "CAL:";

    public static final String CALENDAR = "<CAL:calendar />";
    
    @Override
    public List<Namespace> getAdditionalNamespaces() {
        return ADDITIONAL_NAMESPACES;
    }
    
    @Override
    public WebdavAction getReportAction(String ns, String name) {
        if (ns.equals(CaldavMultigetReport.NAMESPACE) && name.equals(CaldavMultigetReport.NAME)) {
            return new CaldavMultigetReport(this);
        } else if (ns.equals(CalendarQueryReport.NAMESPACE) && name.equals(CalendarQueryReport.NAME)) {
            return new CalendarQueryReport(this);
        } else if (ns.equals(SyncCollection.NAMESPACE) && name.equals(SyncCollection.NAME)) {
            return new SyncCollection(this);
        }
        return null;
    }
}

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

package com.openexchange.report.appsuite.defaultHandlers;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.report.LoginCounterService;
import com.openexchange.report.appsuite.ReportSystemHandler;
import com.openexchange.report.appsuite.internal.Services;
import com.openexchange.report.appsuite.serialization.Report;


/**
 * The {@link ClientLoginCount} reports on the login count per client in the system.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ClientLoginCount implements ReportSystemHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ClientLoginCount.class);

    @Override
    public boolean appliesTo(String reportType) {
        return "default".equals(reportType);
    }

    @Override
    public void runSystemReport(Report report) {
        Calendar cal = Calendar.getInstance();
        // We have to add the counts for two periods: one month and one year
        try {
            // For a year
            Date endDate = cal.getTime();
            cal.add(Calendar.YEAR, -1);
            Date startDate = cal.getTime();
            runReport(report, "clientlogincountyear", startDate, endDate);

            // For a month
            cal = Calendar.getInstance();

            endDate = cal.getTime();
            cal.add(Calendar.DATE, -30);
            startDate = cal.getTime();
            runReport(report, "clientlogincount", startDate, endDate);
        } catch (OXException x) {
            LOG.error("", x);
        }

    }

    private void runReport(Report report, String ns, Date startDate, Date endDate) throws OXException {
        // Use the LoginCounterService to retrieve the sum of logins for the clients (usm, olox2, mobileapp, carddav, caldav) in the given timeframe
        LoginCounterService loginCounterService = Services.getService(LoginCounterService.class);

        Map<String, Integer> usmEasResult = loginCounterService.getNumberOfLogins(startDate, endDate, true, "USM-EAS");
        Integer usmEas = usmEasResult.get(LoginCounterService.SUM);
        report.set(ns, "usm-eas", usmEas.toString());

        Map<String, Integer> olox2Result = loginCounterService.getNumberOfLogins(startDate, endDate, true, "OpenXchange.HTTPClient.OXAddIn");
        Integer olox2 = olox2Result.get(LoginCounterService.SUM);
        report.set(ns, "olox2", olox2.toString());

        Map<String, Integer> mobileAppResult = loginCounterService.getNumberOfLogins(startDate, endDate, true, "com.openexchange.mobileapp");
        Integer mobileApp = mobileAppResult.get(LoginCounterService.SUM);
        report.set(ns, "mobileapp", mobileApp.toString());

        Map<String, Integer> cardDavResults = loginCounterService.getNumberOfLogins(startDate, endDate, true, "CARDDAV");
        Integer cardDav = cardDavResults.get(LoginCounterService.SUM);
        report.set(ns, "carddav", cardDav.toString());

        Map<String, Integer> calDavResults = loginCounterService.getNumberOfLogins(startDate, endDate, true, "CALDAV");
        Integer calDav = calDavResults.get(LoginCounterService.SUM);
        report.set(ns, "caldav", calDav.toString());

        Map<String, Integer> ox6Results = loginCounterService.getNumberOfLogins(startDate, endDate, true, "com.openexchange.ox.gui.dhtml");
        Integer ox6 = ox6Results.get(LoginCounterService.SUM);
        report.set(ns, "ox6", ox6.toString());

        Map<String, Integer> appsuiteResults = loginCounterService.getNumberOfLogins(startDate, endDate, true, "open-xchange-appsuite");
        Integer appsuite = appsuiteResults.get(LoginCounterService.SUM);
        report.set(ns, "appsuite", appsuite.toString());


    }

}

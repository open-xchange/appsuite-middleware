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
    private Report report;

    @Override
    public boolean appliesTo(String reportType) {
        return "default".equals(reportType);
    }

    @Override
    public void runSystemReport(Report report) {
        Calendar cal = Calendar.getInstance();
        // We have to add the counts for two periods: one month and one year
        try {
            this.report = report;
            // For a year
            Date endDate = cal.getTime();
            cal.add(Calendar.YEAR, -1);
            Date startDate = cal.getTime();
            addClientLoginsToNamespaceInReport("clientlogincountyear", startDate, endDate);

            // For a month
            cal = Calendar.getInstance();

            endDate = cal.getTime();
            cal.add(Calendar.DATE, -30);
            startDate = cal.getTime();
            addClientLoginsToNamespaceInReport("clientlogincount", startDate, endDate);
        } catch (OXException x) {
            LOG.error("", x);
        }
    }
    
    private void addClientLoginsToNamespaceInReport(String ns, Date startDate, Date endDate) throws OXException {
        // Use the LoginCounterService to retrieve the sum of logins for the clients (usm, olox2, mobileapp, carddav, caldav) in the given timeframe
        LoginCounterService loginCounterService = Services.getService(LoginCounterService.class);

        Map<String, Integer> usmEasResult = loginCounterService.getNumberOfLogins(startDate, endDate, true, "USM-EAS");
        Integer usmEas = usmEasResult.get(LoginCounterService.SUM);
        this.report.set(ns, "usm-eas", usmEas.toString());

        Map<String, Integer> olox2Result = loginCounterService.getNumberOfLogins(startDate, endDate, true, "OpenXchange.HTTPClient.OXAddIn");
        Integer olox2 = olox2Result.get(LoginCounterService.SUM);
        this.report.set(ns, "olox2", olox2.toString());

        Map<String, Integer> mobileAppResult = loginCounterService.getNumberOfLogins(startDate, endDate, true, "com.openexchange.mobileapp");
        Integer mobileApp = mobileAppResult.get(LoginCounterService.SUM);
        this.report.set(ns, "mobileapp", mobileApp.toString());

        Map<String, Integer> cardDavResults = loginCounterService.getNumberOfLogins(startDate, endDate, true, "CARDDAV");
        Integer cardDav = cardDavResults.get(LoginCounterService.SUM);
        this.report.set(ns, "carddav", cardDav.toString());

        Map<String, Integer> calDavResults = loginCounterService.getNumberOfLogins(startDate, endDate, true, "CALDAV");
        Integer calDav = calDavResults.get(LoginCounterService.SUM);
        this.report.set(ns, "caldav", calDav.toString());

        Map<String, Integer> ox6Results = loginCounterService.getNumberOfLogins(startDate, endDate, true, "com.openexchange.ox.gui.dhtml");
        Integer ox6 = ox6Results.get(LoginCounterService.SUM);
        this.report.set(ns, "ox6", ox6.toString());

        Map<String, Integer> appsuiteResults = loginCounterService.getNumberOfLogins(startDate, endDate, true, "open-xchange-appsuite");
        Integer appsuite = appsuiteResults.get(LoginCounterService.SUM);
        this.report.set(ns, "appsuite", appsuite.toString());
    }
}

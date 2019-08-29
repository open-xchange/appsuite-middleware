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

import static com.openexchange.java.Autoboxing.L;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.serialization.Report;


/**
 * The {@link Total} cumulator sums up the number of contexts and users. It is based on the results of the {@link CapabilityHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public class Total implements ContextReportCumulator{

    @Override
    public boolean appliesTo(String reportType) {
        return reportType.equals("default") || reportType.equals("extended");
    }

    @Override
    public void merge(ContextReport contextReport, Report report) {
        // Count up one for each context
        long contexts = report.get(Report.TOTAL, Report.CONTEXTS, L(0l), Long.class).longValue();
        long contextsDisabled = report.get(Report.TOTAL, Report.CONTEXTS_DISABLED, L(0l), Long.class).longValue();

        if (!contextReport.getContext().isEnabled()) {
            contextsDisabled++;
        }

        report.set(Report.TOTAL, Report.CONTEXTS, L(contexts + 1));
        report.set(Report.TOTAL, Report.CONTEXTS_DISABLED, L(contextsDisabled));
        // Sum up the this.TOTALs of the capabilities combinations from the CapabilityHandler
        long users = report.get(Report.TOTAL, Report.USERS, L(0l), Long.class).longValue();
        long guests = report.get(Report.TOTAL, Report.GUESTS, L(0l), Long.class).longValue();
        long links = report.get(Report.TOTAL, Report.LINKS, L(0l), Long.class).longValue();
        long usersDisabled = report.get(Report.TOTAL, Report.USERS_DISABLED, L(0l), Long.class).longValue();

        Map<String, Object> macdetail = contextReport.getNamespace(Report.MACDETAIL);
        long contextOnlyUsers = 0l;

        for (Map.Entry<String, Object> entry : macdetail.entrySet()) {
            HashMap<String, Long> counts = (HashMap) entry.getValue();
            if (counts != null) {
                if (counts.containsKey(Report.TOTAL)) {
                    users += counts.get(Report.TOTAL).longValue();
                    contextOnlyUsers += counts.get(Report.TOTAL).longValue();

                }

	            if (counts.containsKey(Report.GUESTS)) {
	                guests += counts.get(Report.GUESTS).longValue();
	            }

	            if (counts.containsKey(Report.LINKS)) {
	                links += counts.get(Report.LINKS).longValue();
	            }

	            if (counts.containsKey(Report.DISABLED)) {
	                usersDisabled += counts.get(Report.DISABLED).longValue();
	            }

            }
            report.set(Report.TOTAL, Report.USERS, L(users));
            report.set(Report.TOTAL, Report.USERS_DISABLED, L(usersDisabled));
            report.set(Report.TOTAL, Report.GUESTS, L(guests));
            report.set(Report.TOTAL, Report.LINKS, L(links));

        }
        report.set(Report.TOTAL, "report-format", "appsuite-short");
        Long reportMax = report.get(Report.TOTAL, Report.CONTEXT_USERS_MAX, L(0l), Long.class);
        Long reportMin = report.get(Report.TOTAL, Report.CONTEXT_USERS_MIN, L(0l), Long.class);

        if (contextOnlyUsers > reportMax.longValue()) {
        	reportMax = L(contextOnlyUsers);
		}
        if (contextOnlyUsers < reportMin.longValue() || reportMin.longValue() == 0l) {
        	reportMin = L(contextOnlyUsers);
		}
        report.set(Report.TOTAL, Report.CONTEXT_USERS_MAX, reportMax);
        report.set(Report.TOTAL, Report.CONTEXT_USERS_MIN, reportMin);
        report.set(Report.TOTAL, Report.CONTEXT_USERS_AVG, L(users / report.get(Report.TOTAL, Report.CONTEXTS, Long.class).longValue()));
        HashMap<String, Long> timeframeMap = new HashMap<>();
        timeframeMap.put(Report.TIMEFRAME_START, report.getConsideredTimeframeStart());
        timeframeMap.put(Report.TIMEFRAME_END, report.getConsideredTimeframeEnd());
        report.set(Report.TOTAL, Report.TIMEFRAME, timeframeMap);
    }

    @Override
    public void storeAndMergeReportParts(Report report) {
        // Method not needed in this class, nothing to do here
    }
}

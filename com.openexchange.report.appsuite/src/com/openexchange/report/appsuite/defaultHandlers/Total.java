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
        long contexts = report.get(Report.TOTAL, Report.CONTEXTS, 0l, Long.class);
        long contextsDisabled = report.get(Report.TOTAL, Report.CONTEXTS_DISABLED, 0l, Long.class);
        
        if (contextReport.getContext().isEnabled() == false) {
            contextsDisabled++;
        }
        
        report.set(Report.TOTAL, Report.CONTEXTS, contexts + 1);
        report.set(Report.TOTAL, Report.CONTEXTS_DISABLED, contextsDisabled);
        // Sum up the this.TOTALs of the capabilities combinations from the CapabilityHandler
        long users = report.get(Report.TOTAL, Report.USERS, 0l, Long.class);
        long guests = report.get(Report.TOTAL, Report.GUESTS, 0l, Long.class);
        long links = report.get(Report.TOTAL, Report.LINKS, 0l, Long.class);
        long usersDisabled = report.get(Report.TOTAL, Report.USERS_DISABLED, 0l, Long.class);
        
        Map<String, Object> macdetail = contextReport.getNamespace(Report.MACDETAIL);
        long contextOnlyUsers = 0l;

        for (Map.Entry<String, Object> entry : macdetail.entrySet()) {
            HashMap<String, Long> counts = (HashMap) entry.getValue();
            if (counts != null) {
                if (counts.containsKey(Report.TOTAL)) {
                    users += counts.get(Report.TOTAL);
                    contextOnlyUsers += counts.get(Report.TOTAL);
                    
                }

	            if (counts.containsKey(Report.GUESTS)) {
	                guests += counts.get(Report.GUESTS);
	            }
	            
	            if (counts.containsKey(Report.LINKS)) {
	                links += counts.get(Report.LINKS);
	            }
	            
	            if (counts.containsKey(Report.DISABLED)) {
	                usersDisabled += counts.get(Report.DISABLED);
	            }

            }
            report.set(Report.TOTAL, Report.USERS, users);
            report.set(Report.TOTAL, Report.USERS_DISABLED, usersDisabled);
            report.set(Report.TOTAL, Report.GUESTS, guests);
            report.set(Report.TOTAL, Report.LINKS, links);
            
        }
        report.set(Report.TOTAL, "report-format", "appsuite-short");// TODO QS: what does this parameter say and what is it good for?
        Long reportMax = report.get(Report.TOTAL, Report.CONTEXT_USERS_MAX, 0l, Long.class);
        Long reportMin = report.get(Report.TOTAL, Report.CONTEXT_USERS_MIN, 0l, Long.class);
        
        if (contextOnlyUsers > reportMax) {
        	reportMax = contextOnlyUsers;
		}
        if (contextOnlyUsers < reportMin || reportMin == 0l) {
        	reportMin = contextOnlyUsers;
		}
        report.set(Report.TOTAL, Report.CONTEXT_USERS_MAX, reportMax);
        report.set(Report.TOTAL, Report.CONTEXT_USERS_MIN, reportMin);
        report.set(Report.TOTAL, Report.CONTEXT_USERS_AVG, users / report.get(Report.TOTAL, Report.CONTEXTS, Long.class));
        HashMap<String, Long> timeframeMap = new HashMap<>();
        timeframeMap.put(Report.TIMEFRAME_START, report.getConsideredTimeframeStart());
        timeframeMap.put(Report.TIMEFRAME_END, report.getConsideredTimeframeEnd());
        report.set(Report.TOTAL, Report.TIMEFRAME, timeframeMap);
    }

    @Override
    public void storeAndMergeReportParts(Report report) {
        // TODO Auto-generated method stub
        
    }
}
